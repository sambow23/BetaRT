import mcrtx.lod.format.LodCell;
import mcrtx.lod.format.LodFragmenter;
import mcrtx.lod.format.LodProvenance;
import mcrtx.lod.format.LodReassembler;
import mcrtx.lod.format.LodReducer;
import mcrtx.lod.format.LodStoreFile;
import mcrtx.lod.format.LodTileCodec;
import mcrtx.lod.format.LodTileKey;
import mcrtx.lod.format.LodWorldId;

import java.util.List;

/**
 * Covers the LOD wire and disk format.
 *
 * <p>The reduction cases are the important ones. Coarse tiles are derived rather
 * than observed, and a client, a server and the offline bake tool all derive
 * them independently -- so if the reducer's rules are not total, two producers
 * can hold different bytes for the same ground and provenance comparison stops
 * meaning anything. These tests pin every rule and every tie-break, including
 * the ones that are settled by scan order.
 */
public final class LodFormatTest {
    public static void main(String[] args) {
        cellRoundTripsEveryField();
        cellSentinelsSurviveEncoding();
        tileKeyArithmeticHoldsForNegativeCoordinates();
        tileKeyPackingRoundTrips();
        tileKeyParentAndChildAreInverses();

        reducerAveragesHeight();
        reducerKeepsBlockAndMetaTogether();
        reducerBreaksSymbolTiesDeterministically();
        reducerDropsNarrowWaterAndKeepsBroadWater();
        reducerKeepsWaterOnPartiallyKnownCells();
        reducerTakesSnowByStrictMajority();
        reducerKeepsATrunkFromAnySingleChild();
        reducerWeightsCanopyByCoverage();
        reducerAveragesClimateOverTintedChildrenOnly();
        reducerLeavesUnknownCellsUnknown();
        reducerIsDeterministic();
        reduceTileMapsQuadrantsCorrectly();
        reduceTileTreatsMissingChildAsHole();

        codecRoundTrips();
        codecRejectsCorruptInput();
        codecPeeksKeyWithoutInflating();

        fragmenterRoundTripsInOrder();
        fragmenterRoundTripsOutOfOrder();
        fragmenterIgnoresDuplicateFragments();
        fragmenterHandlesExactBoundaryAndEmpty();
        reassemblerBoundsPendingStreams();

        provenancePrefersStrongerSourceThenRecency();
        provenanceMergeClaimsTheWeakerSource();
        fillUnknownClosesGapsWithoutOverwriting();
        worldIdFileNameIsStableAndPathIndependent();
        storeFileHeaderRoundTrips();

        System.out.println("LodFormatTest: all checks passed");
    }

    // ---- cells ---------------------------------------------------------

    private static void cellRoundTripsEveryField() {
        byte[] cells = new byte[LodCell.BYTES];
        LodCell.write(
                cells, 0,
                64,                       // groundY
                2,                        // groundBlock
                5,                        // groundMeta
                LodCell.FLAG_KNOWN | LodCell.FLAG_SNOW | LodCell.FLAG_TINT,
                62,                       // waterY
                71,                       // canopyTopY
                66,                       // canopyBottomY
                12,                       // coverage
                LodCell.KIND_SPRUCE,
                180,                      // temperature
                90);                      // humidity

        require(LodCell.groundY(cells, 0) == 64, "groundY");
        require(LodCell.groundBlock(cells, 0) == 2, "groundBlock");
        require(LodCell.groundMeta(cells, 0) == 5, "groundMeta");
        require(LodCell.isKnown(cells, 0), "known flag");
        require(LodCell.hasSnow(cells, 0), "snow flag");
        require(LodCell.hasTint(cells, 0), "tint flag");
        require(LodCell.waterY(cells, 0) == 62, "waterY");
        require(LodCell.canopyTopY(cells, 0) == 71, "canopyTopY");
        require(LodCell.canopyBottomY(cells, 0) == 66, "canopyBottomY");
        require(LodCell.canopyCoverage(cells, 0) == 12, "coverage");
        require(LodCell.canopyKind(cells, 0) == LodCell.KIND_SPRUCE, "kind");
        require(LodCell.temperature(cells, 0) == 180, "temperature");
        require(LodCell.humidity(cells, 0) == 90, "humidity");

        // Metadata must not bleed into the flag nibble, nor flags into metadata.
        require((cells[LodCell.OFFSET_GROUND_META] & 0xFF) == (5 | 0x70), "meta and flags packing");
    }

    private static void cellSentinelsSurviveEncoding() {
        byte[] cells = new byte[LodCell.BYTES];
        LodCell.write(cells, 0, -1, 0, 0, LodCell.FLAG_KNOWN, -1, -1, -1, 0, 0, 0, 0);
        require(LodCell.groundY(cells, 0) == LodCell.NO_HEIGHT, "absent ground");
        require(LodCell.waterY(cells, 0) == LodCell.NO_HEIGHT, "absent water");
        require(LodCell.canopyTopY(cells, 0) == LodCell.NO_HEIGHT, "absent canopy");
        require(LodCell.isKnown(cells, 0), "known cell with no surfaces");

        // Anything above the world's height is stored as absent rather than wrapping.
        LodCell.write(cells, 0, 200, 0, 0, LodCell.FLAG_KNOWN, 0, 0, 0, 0, 0, 0, 0);
        require(LodCell.groundY(cells, 0) == LodCell.NO_HEIGHT, "out-of-range height clamps to absent");

        LodCell.clear(cells, 0);
        require(!LodCell.isKnown(cells, 0), "cleared cell is unknown");
        require(LodCell.groundY(cells, 0) == LodCell.NO_HEIGHT, "cleared height");
        require(LodCell.canopyCoverage(cells, 0) == 0, "cleared coverage");
    }

    // ---- tile keys -----------------------------------------------------

    private static void tileKeyArithmeticHoldsForNegativeCoordinates() {
        require(LodTileKey.step(1) == 2, "level 1 step");
        require(LodTileKey.step(4) == 16, "level 4 step");
        require(LodTileKey.sizeBlocks(1) == 64, "level 1 tile size");
        require(LodTileKey.sizeBlocks(4) == 512, "level 4 tile size");

        // Truncating division would put -1 and 0 in the same tile, leaving a
        // seam along both axes at the origin.
        require(LodTileKey.tileCoordinate(-1, 1) == -1, "negative block floors down");
        require(LodTileKey.tileCoordinate(-64, 1) == -1, "negative block on tile edge");
        require(LodTileKey.tileCoordinate(-65, 1) == -2, "negative block past tile edge");
        require(LodTileKey.tileCoordinate(0, 1) == 0, "origin");
        require(LodTileKey.tileCoordinate(63, 1) == 0, "last block of first tile");

        LodTileKey key = LodTileKey.containing(1, -65, 63);
        require(key.originX() == -128, "negative origin");
        require(key.originZ() == 0, "positive origin");
    }

    private static void tileKeyPackingRoundTrips() {
        int[][] cases = {{1, 0, 0}, {5, -1, -1}, {2, 123456, -654321}, {4, -536870912, 536870911}};
        for (int[] test : cases) {
            long packed = LodTileKey.pack(test[0], test[1], test[2]);
            require(LodTileKey.unpackLevel(packed) == test[0], "packed level");
            require(LodTileKey.unpackTileX(packed) == test[1], "packed tileX " + test[1]);
            require(LodTileKey.unpackTileZ(packed) == test[2], "packed tileZ " + test[2]);

            LodTileKey key = new LodTileKey(test[0], test[1], test[2]);
            require(key.packed() == packed, "key packs consistently");
            require(key.equals(LodTileKey.unpack(packed)), "key round trips");
        }
    }

    private static void tileKeyParentAndChildAreInverses() {
        LodTileKey parent = new LodTileKey(3, -5, 7);
        for (int quadrant = 0; quadrant < 4; quadrant++) {
            LodTileKey child = parent.child(quadrant);
            require(child.level() == 2, "child level");
            require(parent.equals(child.parent()), "child's parent is the original, quadrant " + quadrant);
        }

        // A parent covers exactly its children's ground, with no gap or overlap.
        require(parent.sizeBlocks() == parent.child(0).sizeBlocks() * 2, "parent spans two children");
        require(parent.child(0).originX() == parent.originX(), "first child shares origin");
        require(parent.child(3).originX() == parent.originX() + parent.sizeBlocks() / 2, "last child offset");
    }

    // ---- reduction -----------------------------------------------------

    /** Builds four child cells in one buffer, ready to hand to the reducer. */
    private static byte[] children() {
        return new byte[LodCell.BYTES * 4];
    }

    private static byte[][] arrays(byte[] buffer) {
        return new byte[][] {buffer, buffer, buffer, buffer};
    }

    private static int[] offsets() {
        return new int[] {0, LodCell.BYTES, LodCell.BYTES * 2, LodCell.BYTES * 3};
    }

    private static void child(
            byte[] buffer, int index, int groundY, int block, int meta, int flags, int waterY) {
        LodCell.write(
                buffer, index * LodCell.BYTES,
                groundY, block, meta, flags | LodCell.FLAG_KNOWN, waterY,
                LodCell.NO_HEIGHT, LodCell.NO_HEIGHT, 0, 0, 0, 0);
    }

    private static byte[] reduce(byte[] buffer) {
        byte[] out = new byte[LodCell.BYTES];
        LodReducer.reduceCell(arrays(buffer), offsets(), 4, out, 0);
        return out;
    }

    private static void reducerAveragesHeight() {
        byte[] buffer = children();
        child(buffer, 0, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        child(buffer, 1, 12, 2, 0, 0, LodCell.NO_HEIGHT);
        child(buffer, 2, 14, 2, 0, 0, LodCell.NO_HEIGHT);
        child(buffer, 3, 16, 2, 0, 0, LodCell.NO_HEIGHT);

        byte[] out = reduce(buffer);
        // Mean of 10,12,14,16 is 13. Taking a representative child's height
        // instead would give 10, 12, 14 or 16 and quantise the slope.
        require(LodCell.groundY(out, 0) == 13, "mean height, got " + LodCell.groundY(out, 0));
        require(LodCell.isKnown(out, 0), "reduced cell is known");
    }

    private static void reducerKeepsBlockAndMetaTogether() {
        byte[] buffer = children();
        child(buffer, 0, 10, 35, 4, 0, LodCell.NO_HEIGHT);   // wool, colour 4
        child(buffer, 1, 10, 35, 4, 0, LodCell.NO_HEIGHT);
        child(buffer, 2, 10, 35, 11, 0, LodCell.NO_HEIGHT);  // wool, colour 11
        child(buffer, 3, 10, 1, 0, 0, LodCell.NO_HEIGHT);    // stone

        byte[] out = reduce(buffer);
        require(LodCell.groundBlock(out, 0) == 35, "majority block");
        // Reducing id and meta independently could pair block 35 with meta 0 or
        // 11, producing a colour that was never there.
        require(LodCell.groundMeta(out, 0) == 4, "metadata travels with its block");
    }

    private static void reducerBreaksSymbolTiesDeterministically() {
        byte[] buffer = children();
        child(buffer, 0, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        child(buffer, 1, 12, 2, 0, 0, LodCell.NO_HEIGHT);
        child(buffer, 2, 14, 3, 0, 0, LodCell.NO_HEIGHT);
        child(buffer, 3, 16, 3, 0, 0, LodCell.NO_HEIGHT);

        // Two votes each, and both symbols have a child one block from the mean
        // of 13, so the fixed scan order settles it. The value matters less than
        // that it is pinned: every producer must land on the same one.
        byte[] out = reduce(buffer);
        require(LodCell.groundBlock(out, 0) == 2, "scan order settles a full tie");

        // When the tie is not total, the symbol nearer the mean height wins,
        // so the surface shown is the one at the height the cell draws at.
        byte[] nearer = children();
        child(nearer, 0, 0, 2, 0, 0, LodCell.NO_HEIGHT);
        child(nearer, 1, 40, 2, 0, 0, LodCell.NO_HEIGHT);
        child(nearer, 2, 19, 3, 0, 0, LodCell.NO_HEIGHT);
        child(nearer, 3, 21, 3, 0, 0, LodCell.NO_HEIGHT);
        byte[] nearerOut = reduce(nearer);
        require(LodCell.groundY(nearerOut, 0) == 20, "mean of 0,40,19,21");
        require(LodCell.groundBlock(nearerOut, 0) == 3, "closer-to-mean symbol wins the tie");
    }

    private static void reducerDropsNarrowWaterAndKeepsBroadWater() {
        byte[] half = children();
        child(half, 0, 10, 2, 0, 0, 14);
        child(half, 1, 10, 2, 0, 0, 15);
        child(half, 2, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        child(half, 3, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        byte[] halfOut = reduce(half);
        require(LodCell.waterY(halfOut, 0) == 15, "half-covered water survives at its highest");

        byte[] narrow = children();
        child(narrow, 0, 10, 2, 0, 0, 14);
        child(narrow, 1, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        child(narrow, 2, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        child(narrow, 3, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        byte[] narrowOut = reduce(narrow);
        require(LodCell.waterY(narrowOut, 0) == LodCell.NO_HEIGHT, "a stream narrower than the cell vanishes");
    }

    private static void reducerKeepsWaterOnPartiallyKnownCells() {
        // Only one child known, and it is water. Measured against a flat "two of
        // four" this would vanish and a half-explored ocean would render as
        // land; measured against known children it survives.
        byte[] buffer = children();
        child(buffer, 0, 8, 2, 0, 0, 12);
        LodCell.clear(buffer, LodCell.BYTES);
        LodCell.clear(buffer, LodCell.BYTES * 2);
        LodCell.clear(buffer, LodCell.BYTES * 3);

        byte[] out = reduce(buffer);
        require(LodCell.isKnown(out, 0), "one known child makes the parent known");
        require(LodCell.waterY(out, 0) == 12, "water survives when it is all we know");
    }

    private static void reducerTakesSnowByStrictMajority() {
        byte[] three = children();
        child(three, 0, 10, 2, 0, LodCell.FLAG_SNOW, LodCell.NO_HEIGHT);
        child(three, 1, 10, 2, 0, LodCell.FLAG_SNOW, LodCell.NO_HEIGHT);
        child(three, 2, 10, 2, 0, LodCell.FLAG_SNOW, LodCell.NO_HEIGHT);
        child(three, 3, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        require(LodCell.hasSnow(reduce(three), 0), "three of four is snowy");

        byte[] two = children();
        child(two, 0, 10, 2, 0, LodCell.FLAG_SNOW, LodCell.NO_HEIGHT);
        child(two, 1, 10, 2, 0, LodCell.FLAG_SNOW, LodCell.NO_HEIGHT);
        child(two, 2, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        child(two, 3, 10, 2, 0, 0, LodCell.NO_HEIGHT);
        require(!LodCell.hasSnow(reduce(two), 0), "an even split is not a majority");
    }

    private static void reducerKeepsATrunkFromAnySingleChild() {
        byte[] buffer = children();
        child(buffer, 0, 64, 2, 0, LodCell.FLAG_TRUNK, LodCell.NO_HEIGHT);
        child(buffer, 1, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(buffer, 2, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(buffer, 3, 64, 2, 0, 0, LodCell.NO_HEIGHT);

        // Deliberately not a majority, unlike snow. A trunk is one block in a
        // tree about five across, so it never holds a majority of anything --
        // requiring one would delete every trunk at the first reduction and
        // leave the canopies floating, which is what the flag exists to stop.
        require(LodCell.hasTrunk(reduce(buffer), 0), "one child in four carries the trunk up");

        byte[] none = children();
        child(none, 0, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(none, 1, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(none, 2, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(none, 3, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        require(!LodCell.hasTrunk(reduce(none), 0), "no child, no trunk");

        // The flag took the last reserved bit, so it must not collide with the
        // metadata nibble or the flags already there.
        byte[] mixed = children();
        child(mixed, 0, 64, 35, 11, LodCell.FLAG_TRUNK | LodCell.FLAG_SNOW, LodCell.NO_HEIGHT);
        child(mixed, 1, 64, 35, 11, LodCell.FLAG_TRUNK | LodCell.FLAG_SNOW, LodCell.NO_HEIGHT);
        child(mixed, 2, 64, 35, 11, LodCell.FLAG_TRUNK | LodCell.FLAG_SNOW, LodCell.NO_HEIGHT);
        child(mixed, 3, 64, 35, 11, LodCell.FLAG_TRUNK | LodCell.FLAG_SNOW, LodCell.NO_HEIGHT);
        byte[] out = reduce(mixed);
        require(LodCell.hasTrunk(out, 0), "trunk survives beside other flags");
        require(LodCell.hasSnow(out, 0), "snow survives beside the trunk");
        require(LodCell.groundMeta(out, 0) == 11, "metadata is not clipped by the new flag");
    }

    private static void reducerWeightsCanopyByCoverage() {
        byte[] buffer = new byte[LodCell.BYTES * 4];
        int known = LodCell.FLAG_KNOWN;
        LodCell.write(buffer, 0, 64, 2, 0, known, -1, 70, 66, 15, LodCell.KIND_OAK, 0, 0);
        LodCell.write(buffer, LodCell.BYTES, 64, 2, 0, known, -1, 72, 68, 15, LodCell.KIND_OAK, 0, 0);
        LodCell.write(buffer, LodCell.BYTES * 2, 64, 2, 0, known, -1, -1, -1, 0, 0, 0, 0);
        LodCell.write(buffer, LodCell.BYTES * 3, 64, 2, 0, known, -1, -1, -1, 0, 0, 0, 0);

        byte[] out = reduce(buffer);
        // Half the cell is forest, so coverage averages to 8 of 15 -- the field
        // that lets a coarse mesher raise ground rather than choose between a
        // full canopy plateau and nothing.
        require(LodCell.canopyCoverage(out, 0) == 8, "coverage averages, got " + LodCell.canopyCoverage(out, 0));
        // Canopy height is weighted by coverage, so the bare children do not
        // drag the canopy down to ground level.
        require(LodCell.canopyTopY(out, 0) == 71, "canopy top weighted, got " + LodCell.canopyTopY(out, 0));
        require(LodCell.canopyBottomY(out, 0) == 67, "canopy bottom weighted, got " + LodCell.canopyBottomY(out, 0));

        // With no canopy anywhere, the cell reports none rather than a height of zero.
        byte[] bare = children();
        child(bare, 0, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(bare, 1, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(bare, 2, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(bare, 3, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        byte[] bareOut = reduce(bare);
        require(LodCell.canopyCoverage(bareOut, 0) == 0, "no coverage");
        require(LodCell.canopyTopY(bareOut, 0) == LodCell.NO_HEIGHT, "no canopy height");
    }

    private static void reducerAveragesClimateOverTintedChildrenOnly() {
        byte[] buffer = new byte[LodCell.BYTES * 4];
        int tinted = LodCell.FLAG_KNOWN | LodCell.FLAG_TINT;
        LodCell.write(buffer, 0, 64, 2, 0, tinted, -1, -1, -1, 0, 0, 100, 40);
        LodCell.write(buffer, LodCell.BYTES, 64, 2, 0, tinted, -1, -1, -1, 0, 0, 200, 80);
        // Untinted children must not pull the average toward zero.
        LodCell.write(buffer, LodCell.BYTES * 2, 64, 2, 0, LodCell.FLAG_KNOWN, -1, -1, -1, 0, 0, 0, 0);
        LodCell.write(buffer, LodCell.BYTES * 3, 64, 2, 0, LodCell.FLAG_KNOWN, -1, -1, -1, 0, 0, 0, 0);

        byte[] out = reduce(buffer);
        require(LodCell.hasTint(out, 0), "tint flag propagates");
        require(LodCell.temperature(out, 0) == 150, "temperature over tinted only, got " + LodCell.temperature(out, 0));
        require(LodCell.humidity(out, 0) == 60, "humidity over tinted only, got " + LodCell.humidity(out, 0));

        byte[] untinted = children();
        child(untinted, 0, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(untinted, 1, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(untinted, 2, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        child(untinted, 3, 64, 2, 0, 0, LodCell.NO_HEIGHT);
        require(!LodCell.hasTint(reduce(untinted), 0), "no tint without a tinted child");
    }

    private static void reducerLeavesUnknownCellsUnknown() {
        byte[] buffer = new byte[LodCell.BYTES * 4];
        for (int i = 0; i < 4; i++) {
            LodCell.clear(buffer, i * LodCell.BYTES);
        }
        byte[] out = reduce(buffer);
        require(!LodCell.isKnown(out, 0), "no known children leaves a hole");
        require(LodCell.groundY(out, 0) == LodCell.NO_HEIGHT, "hole has no height");
    }

    private static void reducerIsDeterministic() {
        // The property the client/server contract actually rests on: identical
        // input bytes produce identical output bytes, every time.
        byte[] buffer = new byte[LodCell.BYTES * 4];
        int known = LodCell.FLAG_KNOWN | LodCell.FLAG_TINT;
        LodCell.write(buffer, 0, 71, 3, 2, known | LodCell.FLAG_SNOW, 64, 78, 74, 9, LodCell.KIND_BIRCH, 33, 200);
        LodCell.write(buffer, LodCell.BYTES, 68, 3, 2, known, 64, 77, 73, 15, LodCell.KIND_SPRUCE, 31, 199);
        LodCell.write(buffer, LodCell.BYTES * 2, 70, 12, 0, known | LodCell.FLAG_SNOW, -1, -1, -1, 0, 0, 30, 198);
        LodCell.write(buffer, LodCell.BYTES * 3, 69, 12, 0, known, -1, 80, 76, 4, LodCell.KIND_OAK, 29, 197);

        byte[] first = reduce(buffer);
        for (int repeat = 0; repeat < 8; repeat++) {
            byte[] again = reduce(buffer);
            require(LodCell.equal(first, 0, again, 0), "reduction is stable across runs");
        }
    }

    private static void reduceTileMapsQuadrantsCorrectly() {
        byte[][] childTiles = new byte[4][];
        for (int quadrant = 0; quadrant < 4; quadrant++) {
            childTiles[quadrant] = blankTile();
            // Mark every cell of this child with its quadrant as the block id,
            // so the parent's cells reveal which child they were drawn from.
            for (int index = 0; index < LodTileKey.CELLS_PER_TILE; index++) {
                LodCell.write(
                        childTiles[quadrant], index * LodCell.BYTES,
                        40 + quadrant, quadrant, 0, LodCell.FLAG_KNOWN,
                        -1, -1, -1, 0, 0, 0, 0);
            }
        }

        byte[] parent = new byte[LodTileKey.TILE_BYTES];
        LodReducer.reduceTile(childTiles, parent);

        // Parent cell (0,0) is covered by the first child; (31,31) by the last.
        require(LodCell.groundBlock(parent, LodTileKey.cellOffset(0, 0)) == 0, "quadrant 0 maps to low corner");
        require(LodCell.groundBlock(parent, LodTileKey.cellOffset(31, 0)) == 1, "quadrant 1 is +X");
        require(LodCell.groundBlock(parent, LodTileKey.cellOffset(0, 31)) == 2, "quadrant 2 is +Z");
        require(LodCell.groundBlock(parent, LodTileKey.cellOffset(31, 31)) == 3, "quadrant 3 is +X+Z");
        // The boundary sits exactly at the halfway cell, not one either side.
        require(LodCell.groundBlock(parent, LodTileKey.cellOffset(15, 15)) == 0, "last cell of quadrant 0");
        require(LodCell.groundBlock(parent, LodTileKey.cellOffset(16, 16)) == 3, "first cell of quadrant 3");
    }

    private static void reduceTileTreatsMissingChildAsHole() {
        byte[][] childTiles = new byte[4][];
        childTiles[0] = blankTile();
        for (int index = 0; index < LodTileKey.CELLS_PER_TILE; index++) {
            LodCell.write(
                    childTiles[0], index * LodCell.BYTES,
                    64, 2, 0, LodCell.FLAG_KNOWN, -1, -1, -1, 0, 0, 0, 0);
        }

        byte[] parent = new byte[LodTileKey.TILE_BYTES];
        LodReducer.reduceTile(childTiles, parent);

        require(LodCell.isKnown(parent, LodTileKey.cellOffset(0, 0)), "covered quadrant is known");
        require(!LodCell.isKnown(parent, LodTileKey.cellOffset(31, 31)), "absent child leaves a hole");
    }

    private static byte[] blankTile() {
        byte[] tile = new byte[LodTileKey.TILE_BYTES];
        for (int index = 0; index < LodTileKey.CELLS_PER_TILE; index++) {
            LodCell.clear(tile, index * LodCell.BYTES);
        }
        return tile;
    }

    // ---- codec ---------------------------------------------------------

    private static byte[] sampleTile() {
        byte[] tile = blankTile();
        for (int cellZ = 0; cellZ < LodTileKey.TILE_CELLS; cellZ++) {
            for (int cellX = 0; cellX < LodTileKey.TILE_CELLS; cellX++) {
                LodCell.write(
                        tile, LodTileKey.cellOffset(cellX, cellZ),
                        60 + ((cellX + cellZ) % 12), 2, 0,
                        LodCell.FLAG_KNOWN | LodCell.FLAG_TINT,
                        cellZ < 4 ? 62 : -1,
                        -1, -1, 0, 0, 140, 70);
            }
        }
        return tile;
    }

    private static void codecRoundTrips() {
        LodTileKey key = new LodTileKey(3, -12, 40);
        byte[] cells = sampleTile();
        byte[] blob = LodTileCodec.encode(key, LodProvenance.DISK, 1234567890123L, cells);

        LodTileCodec.Decoded decoded = LodTileCodec.decode(blob, 0);
        require(decoded != null, "tile decodes");
        require(decoded.key.equals(key), "key survives");
        require(decoded.source == LodProvenance.DISK, "provenance survives");
        require(decoded.stampMillis == 1234567890123L, "stamp survives");
        require(decoded.consumed == blob.length, "consumed length reports the whole blob");
        for (int i = 0; i < cells.length; i++) {
            require(decoded.cells[i] == cells[i], "cell byte " + i + " survives");
        }

        // Terrain is smooth and repetitive, so a tile must compress hard enough
        // to fit a handful of 250-byte fragments.
        require(blob.length < 2048, "encoded tile stays small, was " + blob.length);

        // Two tiles back to back decode independently, which is what lets a
        // cache file be walked without an index.
        byte[] pair = new byte[blob.length * 2];
        System.arraycopy(blob, 0, pair, 0, blob.length);
        System.arraycopy(blob, 0, pair, blob.length, blob.length);
        LodTileCodec.Decoded second = LodTileCodec.decode(pair, blob.length);
        require(second != null && second.key.equals(key), "second tile in a stream decodes");
    }

    private static void codecRejectsCorruptInput() {
        byte[] blob = LodTileCodec.encode(
                new LodTileKey(1, 0, 0), LodProvenance.LIVE, 1L, sampleTile());

        byte[] wrongVersion = blob.clone();
        wrongVersion[0] = (byte) (LodTileCodec.VERSION + 1);
        require(LodTileCodec.decode(wrongVersion, 0) == null, "unknown version refused");

        byte[] truncated = new byte[blob.length / 2];
        System.arraycopy(blob, 0, truncated, 0, truncated.length);
        require(LodTileCodec.decode(truncated, 0) == null, "truncated blob refused");

        byte[] corruptPayload = blob.clone();
        corruptPayload[corruptPayload.length - 1] = (byte) (corruptPayload[corruptPayload.length - 1] ^ 0xFF);
        corruptPayload[corruptPayload.length - 2] = (byte) (corruptPayload[corruptPayload.length - 2] ^ 0xFF);
        // Either the stream fails to inflate or it inflates to the wrong size;
        // both must come back as null rather than as a half-filled tile.
        LodTileCodec.Decoded decoded = LodTileCodec.decode(corruptPayload, 0);
        require(decoded == null || decoded.cells.length == LodTileKey.TILE_BYTES, "corrupt payload never yields a partial tile");

        require(LodTileCodec.decode(new byte[3], 0) == null, "runt input refused");
        require(LodTileCodec.decode(null, 0) == null, "null input refused");
    }

    private static void codecPeeksKeyWithoutInflating() {
        LodTileKey key = new LodTileKey(4, 9, -9);
        byte[] blob = LodTileCodec.encode(key, LodProvenance.SERVER, 7L, sampleTile());
        require(key.equals(LodTileCodec.peekKey(blob, 0)), "key readable without inflating");
    }

    // ---- fragmenting ---------------------------------------------------

    private static byte[] message(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) (i * 31 + 7);
        }
        return data;
    }

    private static void fragmenterRoundTripsInOrder() {
        byte[] original = message(1500);
        List<byte[]> fragments = LodFragmenter.fragment(LodFragmenter.KIND_TILE, 42, original);
        require(fragments.size() == 6, "1500 bytes needs six fragments, got " + fragments.size());

        for (byte[] fragment : fragments) {
            require(fragment.length <= LodFragmenter.MAX_PACKET_PAYLOAD, "fragment fits the packet ceiling");
            require(LodFragmenter.isWellFormed(fragment), "fragment is well formed");
        }

        LodReassembler reassembler = new LodReassembler();
        byte[] rebuilt = null;
        for (byte[] fragment : fragments) {
            byte[] completed = reassembler.accept(fragment);
            if (completed != null) {
                rebuilt = completed;
            }
        }
        requireSameBytes(original, rebuilt, "in-order reassembly");
        require(reassembler.lastCompletedKind() == LodFragmenter.KIND_TILE, "kind survives reassembly");
    }

    private static void fragmenterRoundTripsOutOfOrder() {
        byte[] original = message(900);
        List<byte[]> fragments = LodFragmenter.fragment(LodFragmenter.KIND_TILE, 7, original);

        LodReassembler reassembler = new LodReassembler();
        byte[] rebuilt = null;
        for (int i = fragments.size() - 1; i >= 0; i--) {
            byte[] completed = reassembler.accept(fragments.get(i));
            if (completed != null) {
                rebuilt = completed;
            }
        }
        requireSameBytes(original, rebuilt, "reverse-order reassembly");
    }

    private static void fragmenterIgnoresDuplicateFragments() {
        byte[] original = message(700);
        List<byte[]> fragments = LodFragmenter.fragment(LodFragmenter.KIND_TILE, 3, original);

        LodReassembler reassembler = new LodReassembler();
        int completions = 0;
        byte[] rebuilt = null;

        // A repeated fragment must not count toward completion, or a message
        // could finish before every piece has actually arrived.
        require(reassembler.accept(fragments.get(0)) == null, "first fragment does not complete");
        require(reassembler.accept(fragments.get(0)) == null, "duplicate does not complete");
        for (int i = 1; i < fragments.size(); i++) {
            byte[] completed = reassembler.accept(fragments.get(i));
            if (completed != null) {
                completions++;
                rebuilt = completed;
            }
        }
        require(completions == 1, "message completes exactly once");
        requireSameBytes(original, rebuilt, "reassembly despite a duplicate");
    }

    private static void fragmenterHandlesExactBoundaryAndEmpty() {
        byte[] exact = message(LodFragmenter.FRAGMENT_PAYLOAD_BYTES);
        List<byte[]> single = LodFragmenter.fragment(LodFragmenter.KIND_TILE, 1, exact);
        require(single.size() == 1, "an exactly full payload is one fragment");

        byte[] justOver = message(LodFragmenter.FRAGMENT_PAYLOAD_BYTES + 1);
        require(LodFragmenter.fragment(LodFragmenter.KIND_TILE, 1, justOver).size() == 2, "one byte over spills");

        LodReassembler reassembler = new LodReassembler();
        requireSameBytes(exact, reassembler.accept(single.get(0)), "boundary reassembly");

        List<byte[]> empty = LodFragmenter.fragment(LodFragmenter.KIND_HELLO, 0, new byte[0]);
        require(empty.size() == 1, "an empty message still sends one fragment");
        byte[] rebuiltEmpty = new LodReassembler().accept(empty.get(0));
        require(rebuiltEmpty != null && rebuiltEmpty.length == 0, "empty message round trips");

        require(LodFragmenter.helloFragment().length == LodFragmenter.FRAGMENT_HEADER_BYTES, "hello is header only");
        require(!LodFragmenter.isWellFormed(new byte[] {2, 0, 0, 0, 1}), "wrong version rejected");
        require(!LodFragmenter.isWellFormed(new byte[] {1, 0, 0, 3, 2}), "index past count rejected");
        require(!LodFragmenter.isWellFormed(new byte[] {1, 0, 0, 0, 0}), "zero count rejected");
    }

    private static void reassemblerBoundsPendingStreams() {
        // More partial streams than the reassembler holds, none of them ever
        // finished. The oldest are evicted; nothing grows without bound.
        LodReassembler reassembler = new LodReassembler();
        for (int streamId = 0; streamId < LodReassembler.MAX_PENDING_STREAMS * 3; streamId++) {
            List<byte[]> fragments = LodFragmenter.fragment(
                    LodFragmenter.KIND_TILE, streamId, message(1000));
            require(reassembler.accept(fragments.get(0)) == null, "partial stream does not complete");
        }

        // A fresh stream still completes afterwards, so eviction did not corrupt state.
        List<byte[]> fragments = LodFragmenter.fragment(LodFragmenter.KIND_TILE, 200, message(300));
        byte[] rebuilt = null;
        for (byte[] fragment : fragments) {
            byte[] completed = reassembler.accept(fragment);
            if (completed != null) {
                rebuilt = completed;
            }
        }
        requireSameBytes(message(300), rebuilt, "reassembly after eviction");
    }

    // ---- provenance, identity, file header -----------------------------

    private static void provenancePrefersStrongerSourceThenRecency() {
        require(
                LodProvenance.supersedes(LodProvenance.LIVE, 1L, LodProvenance.SERVER, 999L),
                "a loaded chunk beats a newer server tile");
        require(
                !LodProvenance.supersedes(LodProvenance.DERIVED, 999L, LodProvenance.DISK, 1L),
                "derived data never displaces an observation");
        require(
                LodProvenance.supersedes(LodProvenance.DISK, 5L, LodProvenance.DISK, 4L),
                "newer wins within a source");
        require(
                !LodProvenance.supersedes(LodProvenance.DISK, 4L, LodProvenance.DISK, 4L),
                "re-observing unchanged ground is not a write");
        require(LodProvenance.isValid(LodProvenance.DERIVED), "derived is a valid source");
        require(!LodProvenance.isValid(9), "unknown source rejected");
    }

    private static void provenanceMergeClaimsTheWeakerSource() {
        require(
                LodProvenance.weaker(LodProvenance.LIVE, LodProvenance.DISK) == LodProvenance.DISK,
                "a live tile whose gaps came off disk is only as good as disk");
        require(
                LodProvenance.weaker(LodProvenance.DISK, LodProvenance.LIVE) == LodProvenance.DISK,
                "which source was already there does not change the answer");
        require(
                LodProvenance.weaker(LodProvenance.DISK, LodProvenance.DISK) == LodProvenance.DISK,
                "merging like with like claims that");

        // The point of the rule: the merged tile must still lose to a real
        // observation of the same ground, or the gaps it filled would lock out
        // the sampler that could have filled them properly.
        int merged = LodProvenance.weaker(LodProvenance.LIVE, LodProvenance.DISK);
        require(
                LodProvenance.supersedes(LodProvenance.LIVE, 1L, merged, 999L),
                "a live observation still beats the merge it contributed to");
    }

    private static void fillUnknownClosesGapsWithoutOverwriting() {
        int cells = 4;
        byte[] target = new byte[cells * LodCell.BYTES];
        byte[] source = new byte[cells * LodCell.BYTES];

        // Cell 0: known in both and different. Cell 1: known only in the source.
        // Cell 2: known only in the target. Cell 3: known in neither.
        LodCell.write(target, 0 * LodCell.BYTES, 70, 2, 0, LodCell.FLAG_KNOWN, -1, -1, -1, 0, 0, 0, 0);
        LodCell.write(source, 0 * LodCell.BYTES, 30, 1, 0, LodCell.FLAG_KNOWN, -1, -1, -1, 0, 0, 0, 0);
        LodCell.write(source, 1 * LodCell.BYTES, 64, 3, 0, LodCell.FLAG_KNOWN, -1, -1, -1, 0, 0, 0, 0);
        LodCell.write(target, 2 * LodCell.BYTES, 80, 4, 0, LodCell.FLAG_KNOWN, -1, -1, -1, 0, 0, 0, 0);

        require(LodCell.fillUnknown(target, source) == 1, "only the one gap is filled");
        require(LodCell.groundY(target, 0 * LodCell.BYTES) == 70, "a known cell is never replaced");
        require(LodCell.groundY(target, 1 * LodCell.BYTES) == 64, "an unknown cell takes the source");
        require(LodCell.groundY(target, 2 * LodCell.BYTES) == 80, "a cell the source lacks is left alone");
        require(!LodCell.isKnown(target, 3 * LodCell.BYTES), "neither knew, so it stays unknown");

        // Idempotent, which is what lets the store skip the write and so skip a
        // dirty file and a mesh rebuild for bytes it already holds.
        require(LodCell.fillUnknown(target, source) == 0, "a repeated merge adds nothing");
        require(LodCell.fillUnknown(target, null) == 0, "a missing source is not a crash");

        // Completeness is what lets the store skip asking a second source at all,
        // so it must not report a tile with one hole left as finished.
        require(!LodCell.allKnown(target), "one unknown cell is not a complete tile");
        LodCell.write(target, 3 * LodCell.BYTES, 60, 1, 0, LodCell.FLAG_KNOWN, -1, -1, -1, 0, 0, 0, 0);
        require(LodCell.allKnown(target), "every cell observed is a complete tile");
        require(!LodCell.allKnown(null), "nothing at all is not complete");
        require(!LodCell.allKnown(new byte[cells * LodCell.BYTES]), "a blank tile is not complete");
    }

    private static void worldIdFileNameIsStableAndPathIndependent() {
        LodWorldId first = new LodWorldId(-5169821337498340905L, LodWorldId.DIMENSION_OVERWORLD);
        LodWorldId second = new LodWorldId(-5169821337498340905L, LodWorldId.DIMENSION_OVERWORLD);
        require(first.equals(second), "same seed and dimension is the same world");
        require(first.fileName().equals(second.fileName()), "file name is stable");
        require(first.hashCode() == second.hashCode(), "hash agrees with equality");

        LodWorldId nether = new LodWorldId(-5169821337498340905L, LodWorldId.DIMENSION_NETHER);
        require(!first.equals(nether), "dimensions are different worlds");
        require(!first.fileName().equals(nether.fileName()), "dimensions get different files");

        // Negative seeds must not produce a sign or a variable-length name.
        require(first.fileName().indexOf('-') == 3, "only the separator dash appears before the seed");
        require(first.fileName().startsWith("lod-"), "recognisable prefix");
        require(first.fileName().endsWith(".tiles"), "recognisable suffix");
    }

    private static void storeFileHeaderRoundTrips() {
        LodWorldId worldId = new LodWorldId(42L, LodWorldId.DIMENSION_OVERWORLD);
        int levelMask = (1 << 1) | (1 << 2) | (1 << 3);
        LodStoreFile header = new LodStoreFile(
                worldId, "My World", levelMask, 1234, LodTileCodec.VERSION);

        byte[] encoded = header.encode();
        require(encoded.length == header.headerBytes(), "header reports its own length");

        LodStoreFile decoded = LodStoreFile.decode(encoded);
        require(decoded != null, "header decodes");
        require(decoded.worldId().equals(worldId), "world identity survives");
        require("My World".equals(decoded.label()), "label survives");
        require(decoded.tileCount() == 1234, "tile count survives");
        require(decoded.isReadable(), "matching tile version is readable");
        require(decoded.hasLevel(1) && decoded.hasLevel(3), "present levels reported");
        require(!decoded.hasLevel(4) && !decoded.hasLevel(0), "absent levels reported");

        // A file written by a future build must be refused rather than misread.
        LodStoreFile future = new LodStoreFile(worldId, "x", levelMask, 1, LodTileCodec.VERSION + 1);
        require(!LodStoreFile.decode(future.encode()).isReadable(), "future tile version is not readable");

        require(LodStoreFile.decode(new byte[] {1, 2, 3}) == null, "runt header refused");
        byte[] notOurs = new byte[64];
        require(LodStoreFile.decode(notOurs) == null, "foreign file refused");
    }

    // ---- harness -------------------------------------------------------

    private static void requireSameBytes(byte[] expected, byte[] actual, String message) {
        require(actual != null, message + ": nothing produced");
        require(expected.length == actual.length, message + ": length " + actual.length);
        for (int i = 0; i < expected.length; i++) {
            require(expected[i] == actual[i], message + ": byte " + i);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
