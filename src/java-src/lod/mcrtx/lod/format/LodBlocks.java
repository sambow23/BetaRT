package mcrtx.lod.format;

/**
 * How Beta's block ids are classified when a column is sampled.
 *
 * <p>Plain tables rather than probes against the game's own block objects, for
 * three reasons. The obfuscated predicates that look like {@code isOpaqueCube}
 * change meaning with the fancy-graphics setting, and the LOD surface must not
 * depend on a render option. The server plugin and the offline bake tool have no
 * block objects to probe. And the classification has to be identical across all
 * four producers or they would disagree about where the ground is -- which the
 * whole format exists to prevent.
 *
 * <p>Ids are Beta 1.7.3's. The table is fixed at 256 entries because that is the
 * whole of Beta's id space.
 */
public final class LodBlocks {
    public static final int AIR = 0;
    public static final int WATER_STILL = 8;
    public static final int WATER_FLOWING = 9;
    public static final int LAVA_STILL = 10;
    public static final int LAVA_FLOWING = 11;
    public static final int LOG = 17;
    public static final int LEAVES = 18;
    public static final int SNOW_LAYER = 78;

    /** Leaf metadata's low two bits select the species. */
    private static final int LEAF_SPECIES_MASK = 0x03;

    /**
     * Blocks that exist in the world but must not define the LOD surface:
     * decorations, thin plates, and anything that is not a full cube. Leaving
     * them in floats the terrain skin above the ground it sits on.
     */
    private static final boolean[] NON_SURFACE = new boolean[256];

    /** Leaves and logs, which form the canopy layer rather than the ground. */
    private static final boolean[] CANOPY = new boolean[256];

    static {
        int[] excluded = {
            6,   // sapling
            31,  // tall grass
            32,  // dead bush
            37, 38, 39, 40,  // flowers and mushrooms
            50,  // torch
            51,  // fire
            55,  // redstone wire
            59,  // crops
            63, 68,  // signs
            64, 71,  // doors
            65,  // ladder
            66,  // rail
            69,  // lever
            70, 72,  // pressure plates
            75, 76,  // redstone torch
            77,  // button
            78,  // snow layer -- the ground beneath it is the real surface
            83,  // reeds
            85,  // fence
            90,  // portal
            92,  // cake
            93, 94,  // repeater
        };
        for (int blockId : excluded) {
            NON_SURFACE[blockId] = true;
        }

        CANOPY[LOG] = true;
        CANOPY[LEAVES] = true;
    }

    private LodBlocks() {
    }

    public static boolean isWater(int blockId) {
        return blockId == WATER_STILL || blockId == WATER_FLOWING;
    }

    public static boolean isLava(int blockId) {
        return blockId == LAVA_STILL || blockId == LAVA_FLOWING;
    }

    /**
     * Part of a tree rather than of the ground.
     *
     * <p>Both leaves and logs are excluded from the ground surface, but only
     * leaves define the canopy slab -- see {@link #isCanopyMass}. A trunk that
     * ran to the ground would drag the canopy's underside down with it and the
     * slab would render as a solid green block from the treetops to the dirt.
     */
    public static boolean isCanopy(int blockId) {
        return CANOPY[blockId & 0xFF];
    }

    /** Leaves specifically: the part of a tree a canopy slab is drawn from. */
    public static boolean isCanopyMass(int blockId) {
        return blockId == LEAVES;
    }

    /**
     * Blocks that are skipped when looking for the surface.
     *
     * <p>Lava is deliberately absent: it reads as an opaque surface at LOD
     * range, and its own material only matters close up where real chunk
     * geometry has taken over.
     */
    public static boolean isNonSurface(int blockId) {
        return NON_SURFACE[blockId & 0xFF];
    }

    /** Canopy kind for a leaf block's metadata. */
    public static int canopyKind(int leafMeta) {
        switch (leafMeta & LEAF_SPECIES_MASK) {
            case 1:
                return LodCell.KIND_SPRUCE;
            case 2:
                return LodCell.KIND_BIRCH;
            case 0:
                return LodCell.KIND_OAK;
            default:
                return LodCell.KIND_OTHER;
        }
    }
}
