import java.lang.reflect.Field;
import java.util.Properties;

final class McrtxAnchorBlockBootstrap {
    private static final int ANCHOR_BLOCK_ID = 97;
    private static final int ANCHOR_TILE_INDEX = 2;
    private static final String ANCHOR_BLOCK_KEY = "mcrtxAnchor";
    private static final String ANCHOR_BLOCK_NAME = "Anchor Block";

    private static boolean registered;

    private McrtxAnchorBlockBootstrap() {
    }

    static synchronized void ensureRegistered() {
        if (registered) {
            return;
        }

        uu existingBlock = uu.m[ANCHOR_BLOCK_ID];
        if (existingBlock != null) {
            if (!("tile." + ANCHOR_BLOCK_KEY).equals(existingBlock.o())) {
                System.out.println("[mcrtx] anchor block slot " + ANCHOR_BLOCK_ID + " is already occupied by " + existingBlock.o());
                return;
            }
            registered = true;
            ensureLanguageEntry();
            return;
        }

        uu anchorBlock = new uu(ANCHOR_BLOCK_ID, ANCHOR_TILE_INDEX, ln.c)
                .c(0.5f)
                .a(uu.f)
                .a(ANCHOR_BLOCK_KEY);
        if (gm.c[ANCHOR_BLOCK_ID] == null) {
            gm.c[ANCHOR_BLOCK_ID] = new ck(ANCHOR_BLOCK_ID - 256);
        }

        anchorBlock.k();
        hk.a().a(new iz(anchorBlock, 1), "##", "##", Character.valueOf('#'), uu.w);
        ensureLanguageEntry();

        registered = true;
        System.out.println("[mcrtx] registered anchor block id=" + ANCHOR_BLOCK_ID);
    }

    private static void ensureLanguageEntry() {
        try {
            Field translationsField = nh.class.getDeclaredField("b");
            translationsField.setAccessible(true);
            Properties translations = (Properties) translationsField.get(nh.a());
            translations.put("tile." + ANCHOR_BLOCK_KEY + ".name", ANCHOR_BLOCK_NAME);
        } catch (ReflectiveOperationException exception) {
            System.out.println("[mcrtx] failed to register anchor block language entry: " + exception);
        }
    }
}