package mcrtx.lod.format;

/**
 * Derives a coarse tile from the four finer tiles it covers.
 *
 * <p>This is the <b>contract between every producer of LOD data</b>. A client
 * that walked the ground, a server that read it off disk and an offline bake of
 * the same world must all arrive at byte-identical coarse tiles, because that is
 * what makes provenance comparable: if two producers can disagree about a
 * derived tile, then "the server's copy differs from mine" stops meaning
 * "the world changed" and starts meaning nothing at all. Every rule below is
 * therefore total and every tie is broken explicitly. Nothing here may depend on
 * iteration order of a hash container, on floating point, or on anything outside
 * its inputs.
 *
 * <p>Reduction only ever runs upward, from the canonical finest level toward
 * coarser ones. Observations enter the store at {@link LodTileKey#CANONICAL_LEVEL}
 * and coarse levels are a pure function of them, which is why the store needs no
 * separate record per detail level: ground seen up close once can serve every
 * level that will ever draw it.
 */
public final class LodReducer {
    /**
     * Order the four children of a cell are visited in.
     *
     * <p>Fixed and documented because ties are broken by child index: two
     * producers scanning in different orders would resolve a tie differently and
     * emit different bytes. X varies fastest, matching cell index order within a
     * tile.
     */
    public static final int CHILDREN_PER_CELL = 4;

    /** Offsets, in child-cell units, of the four children in scan order. */
    private static final int[] CHILD_DX = {0, 1, 0, 1};
    private static final int[] CHILD_DZ = {0, 0, 1, 1};

    private LodReducer() {
    }

    /**
     * Reduces four child tiles into their parent.
     *
     * <p>Children are indexed {@code quadrantZ * 2 + quadrantX} and any of them
     * may be null, which is the ordinary case: a coarse tile whose ground is
     * only partly explored still derives, with the unexplored quarters left as
     * holes. A parent cell with no known child stays unknown, and the mesher
     * skips it.
     *
     * @param children four child cell arrays in quadrant order; entries may be null
     * @param parentOut {@link LodTileKey#TILE_BYTES} of output
     */
    public static void reduceTile(byte[][] children, byte[] parentOut) {
        if (children == null || children.length != 4) {
            throw new IllegalArgumentException("expected four child slots");
        }
        if (parentOut == null || parentOut.length != LodTileKey.TILE_BYTES) {
            throw new IllegalArgumentException("parent buffer must be a whole tile");
        }
        for (byte[] child : children) {
            if (child != null && child.length != LodTileKey.TILE_BYTES) {
                throw new IllegalArgumentException("child buffer must be a whole tile");
            }
        }

        int half = LodTileKey.TILE_CELLS >> 1;
        byte[][] sourceArrays = new byte[CHILDREN_PER_CELL][];
        int[] sourceOffsets = new int[CHILDREN_PER_CELL];

        for (int parentZ = 0; parentZ < LodTileKey.TILE_CELLS; parentZ++) {
            for (int parentX = 0; parentX < LodTileKey.TILE_CELLS; parentX++) {
                // Which child tile this parent cell falls in, and where inside it.
                int quadrantX = parentX / half;
                int quadrantZ = parentZ / half;
                byte[] child = children[quadrantZ * 2 + quadrantX];

                int parentOffset = LodTileKey.cellOffset(parentX, parentZ);
                if (child == null) {
                    LodCell.clear(parentOut, parentOffset);
                    continue;
                }

                int childX = (parentX % half) << 1;
                int childZ = (parentZ % half) << 1;
                for (int i = 0; i < CHILDREN_PER_CELL; i++) {
                    sourceArrays[i] = child;
                    sourceOffsets[i] = LodTileKey.cellOffset(childX + CHILD_DX[i], childZ + CHILD_DZ[i]);
                }

                reduceCell(sourceArrays, sourceOffsets, CHILDREN_PER_CELL, parentOut, parentOffset);
            }
        }
    }

    /**
     * Reduces up to four child cells into one coarse cell.
     *
     * <p>Exposed separately from {@link #reduceTile} because the sampler uses it
     * to fold block-resolution samples into canonical cells, which is the same
     * operation over a different source.
     */
    public static void reduceCell(
            byte[][] sources, int[] offsets, int count, byte[] out, int outOffset) {
        int knownCount = 0;
        int heightSum = 0;
        int heightCount = 0;

        for (int i = 0; i < count; i++) {
            if (!LodCell.isKnown(sources[i], offsets[i])) {
                continue;
            }
            knownCount++;
            int groundY = LodCell.groundY(sources[i], offsets[i]);
            if (groundY != LodCell.NO_HEIGHT) {
                heightSum += groundY;
                heightCount++;
            }
        }

        if (knownCount == 0) {
            LodCell.clear(out, outOffset);
            return;
        }

        // Mean height, rounded half up. Heights are never negative, so plain
        // integer arithmetic rounds correctly without a branch. Using the mean
        // rather than a representative child's height is what gives coarse
        // terrain smooth slopes instead of quantised plateaus -- and it is the
        // one number the tie-breaks below are measured against, so it is
        // computed before anything else.
        int groundY = LodCell.NO_HEIGHT;
        if (heightCount > 0) {
            groundY = (heightSum + (heightCount >> 1)) / heightCount;
        }

        int flags = LodCell.FLAG_KNOWN;

        // ---- surface block: mode of (id, meta) taken as one symbol ----
        //
        // Reducing id and meta independently would let a coarse cell take its id
        // from one child and its metadata from another -- wool of a colour that
        // was never there, a slab variant nothing placed. They travel together.
        int bestBlock = 0;
        int bestMeta = 0;
        if (heightCount > 0) {
            int bestVotes = -1;
            int bestHeightDelta = Integer.MAX_VALUE;

            for (int i = 0; i < count; i++) {
                if (!LodCell.isKnown(sources[i], offsets[i])) {
                    continue;
                }
                int candidateHeight = LodCell.groundY(sources[i], offsets[i]);
                if (candidateHeight == LodCell.NO_HEIGHT) {
                    continue;
                }
                int candidateBlock = LodCell.groundBlock(sources[i], offsets[i]);
                int candidateMeta = LodCell.groundMeta(sources[i], offsets[i]);

                int votes = 0;
                int nearestDelta = Integer.MAX_VALUE;
                for (int j = 0; j < count; j++) {
                    if (!LodCell.isKnown(sources[j], offsets[j])) {
                        continue;
                    }
                    int otherHeight = LodCell.groundY(sources[j], offsets[j]);
                    if (otherHeight == LodCell.NO_HEIGHT) {
                        continue;
                    }
                    if (LodCell.groundBlock(sources[j], offsets[j]) != candidateBlock
                            || LodCell.groundMeta(sources[j], offsets[j]) != candidateMeta) {
                        continue;
                    }
                    votes++;
                    int delta = Math.abs(otherHeight - groundY);
                    if (delta < nearestDelta) {
                        nearestDelta = delta;
                    }
                }

                // Most common symbol wins; ties go to whichever symbol has a
                // child closest to the cell's mean height, so the surface a
                // coarse cell shows is the one actually at the height it draws
                // at. Scan order settles anything still tied, which is why that
                // order is fixed above.
                if (votes > bestVotes
                        || (votes == bestVotes && nearestDelta < bestHeightDelta)) {
                    bestVotes = votes;
                    bestHeightDelta = nearestDelta;
                    bestBlock = candidateBlock;
                    bestMeta = candidateMeta;
                }
            }
        }

        // ---- snow: strict majority of known children ----
        int snowCount = 0;
        for (int i = 0; i < count; i++) {
            if (LodCell.isKnown(sources[i], offsets[i]) && LodCell.hasSnow(sources[i], offsets[i])) {
                snowCount++;
            }
        }
        if (snowCount * 2 > knownCount) {
            flags |= LodCell.FLAG_SNOW;
        }

        // ---- trunk: any known child is enough ----
        //
        // Not a majority, unlike snow. A trunk is one block in a tree about five
        // across, so it never holds a majority of anything -- requiring one would
        // delete every trunk at the first reduction and leave the canopies
        // floating again, which is the whole reason the flag exists. Erring the
        // other way costs a coarse cell a trunk it only partly deserves, and a
        // cell that coarse is drawing its canopy as ground anyway.
        for (int i = 0; i < count; i++) {
            if (LodCell.isKnown(sources[i], offsets[i]) && LodCell.hasTrunk(sources[i], offsets[i])) {
                flags |= LodCell.FLAG_TRUNK;
                break;
            }
        }

        // ---- water: kept when at least half the known children have it ----
        //
        // Measured against known children rather than a flat "two of four" so
        // that a partially explored ocean tile still reads as ocean. The intent
        // is unchanged: a stream narrower than a coarse cell disappears, because
        // it never reaches half of the cell's area.
        int waterCount = 0;
        int waterY = LodCell.NO_HEIGHT;
        for (int i = 0; i < count; i++) {
            if (!LodCell.isKnown(sources[i], offsets[i])) {
                continue;
            }
            int candidateWater = LodCell.waterY(sources[i], offsets[i]);
            if (candidateWater == LodCell.NO_HEIGHT) {
                continue;
            }
            waterCount++;
            if (candidateWater > waterY) {
                waterY = candidateWater;
            }
        }
        if (waterCount * 2 < knownCount) {
            waterY = LodCell.NO_HEIGHT;
        }

        // ---- canopy: coverage averages, heights and kind weighted by it ----
        //
        // Coverage is the field that survives downsampling. Without it a coarse
        // forest cell has to choose between "all canopy" -- the plateau of
        // leaves this format exists to stop -- and nothing at all. Weighting the
        // canopy heights by coverage keeps a cell that is one quarter forest
        // from reporting its canopy at the height of a single tree.
        int coverageSum = 0;
        int canopyTopWeighted = 0;
        int canopyBottomWeighted = 0;
        int canopyWeight = 0;
        int[] kindWeights = new int[16];

        for (int i = 0; i < count; i++) {
            if (!LodCell.isKnown(sources[i], offsets[i])) {
                continue;
            }
            int coverage = LodCell.canopyCoverage(sources[i], offsets[i]);
            coverageSum += coverage;
            if (coverage <= 0) {
                continue;
            }
            int top = LodCell.canopyTopY(sources[i], offsets[i]);
            int bottom = LodCell.canopyBottomY(sources[i], offsets[i]);
            if (top == LodCell.NO_HEIGHT) {
                continue;
            }
            if (bottom == LodCell.NO_HEIGHT) {
                bottom = top;
            }
            canopyTopWeighted += top * coverage;
            canopyBottomWeighted += bottom * coverage;
            canopyWeight += coverage;
            kindWeights[LodCell.canopyKind(sources[i], offsets[i])] += coverage;
        }

        int coverage = (coverageSum + (knownCount >> 1)) / knownCount;
        if (coverage > LodCell.MAX_COVERAGE) {
            coverage = LodCell.MAX_COVERAGE;
        }

        int canopyTopY = LodCell.NO_HEIGHT;
        int canopyBottomY = LodCell.NO_HEIGHT;
        int canopyKind = LodCell.KIND_OAK;
        if (canopyWeight > 0 && coverage > 0) {
            canopyTopY = (canopyTopWeighted + (canopyWeight >> 1)) / canopyWeight;
            canopyBottomY = (canopyBottomWeighted + (canopyWeight >> 1)) / canopyWeight;

            // Heaviest kind wins; the lowest kind index settles a tie, which is
            // a fixed rule rather than a meaningful preference.
            int heaviest = 0;
            for (int kind = 1; kind < kindWeights.length; kind++) {
                if (kindWeights[kind] > kindWeights[heaviest]) {
                    heaviest = kind;
                }
            }
            canopyKind = heaviest;
        } else {
            coverage = 0;
        }

        // ---- climate: mean over children that carry it ----
        int temperatureSum = 0;
        int humiditySum = 0;
        int tintCount = 0;
        for (int i = 0; i < count; i++) {
            if (!LodCell.isKnown(sources[i], offsets[i]) || !LodCell.hasTint(sources[i], offsets[i])) {
                continue;
            }
            temperatureSum += LodCell.temperature(sources[i], offsets[i]);
            humiditySum += LodCell.humidity(sources[i], offsets[i]);
            tintCount++;
        }

        int temperature = 0;
        int humidity = 0;
        if (tintCount > 0) {
            flags |= LodCell.FLAG_TINT;
            temperature = (temperatureSum + (tintCount >> 1)) / tintCount;
            humidity = (humiditySum + (tintCount >> 1)) / tintCount;
        }

        LodCell.write(
                out,
                outOffset,
                groundY,
                bestBlock,
                bestMeta,
                flags,
                waterY,
                canopyTopY,
                canopyBottomY,
                coverage,
                canopyKind,
                temperature,
                humidity);
    }
}
