package mcrtx.bridge;

public final class MinecraftPlatformRuntime {
    private static final String BACKEND_PROPERTY = "mcrtx.platformBackend";
    private static final String BACKEND_ENVIRONMENT = "MCRTX_PLATFORM_BACKEND";
    private static final MinecraftPlatform DEFAULT_PLATFORM = new Lwjgl2MinecraftPlatform();

    private static volatile MinecraftPlatform currentPlatform = DEFAULT_PLATFORM;
    private static volatile String currentBackendSelection = DEFAULT_PLATFORM.backendId();
    private static volatile String selectionStatus = "Selected default LWJGL 2 platform backend";

    static {
        reset();
    }

    private MinecraftPlatformRuntime() {
    }

    public static MinecraftPlatform current() {
        return currentPlatform;
    }

    public static String currentBackendSelection() {
        return currentBackendSelection;
    }

    public static String selectionStatus() {
        return selectionStatus;
    }

    public static synchronized void install(MinecraftPlatform platform) {
        currentPlatform = platform == null ? DEFAULT_PLATFORM : platform;
        currentBackendSelection = currentPlatform.backendId();
        selectionStatus = "Installed explicit platform backend '" + currentBackendSelection + "'";
    }

    public static synchronized void reset() {
        PlatformSelection selection = resolveConfiguredPlatform();
        currentPlatform = selection.platform;
        currentBackendSelection = currentPlatform.backendId();
        selectionStatus = selection.status;
    }

    private static PlatformSelection resolveConfiguredPlatform() {
        String requestedBackend = readConfiguredBackend();
        if (requestedBackend == null || requestedBackend.isEmpty()) {
            if (isGlfwRuntimeAvailable()) {
                try {
                    return new PlatformSelection(new Lwjgl3MinecraftPlatform(), "Auto-detected GLFW compatibility runtime; selected LWJGL 3 platform backend");
                } catch (RuntimeException exception) {
                    return new PlatformSelection(
                            DEFAULT_PLATFORM,
                            "Detected GLFW compatibility runtime but failed to initialize LWJGL 3 backend: "
                                    + exception.getMessage()
                                    + "; falling back to LWJGL 2");
                }
            }

            return new PlatformSelection(DEFAULT_PLATFORM, "Selected LWJGL 2 platform backend");
        }

        if ("lwjgl2".equalsIgnoreCase(requestedBackend)) {
            return new PlatformSelection(DEFAULT_PLATFORM, "Selected LWJGL 2 platform backend");
        }

        if ("lwjgl3".equalsIgnoreCase(requestedBackend) || "glfw".equalsIgnoreCase(requestedBackend)) {
            try {
                MinecraftPlatform platform = new Lwjgl3MinecraftPlatform();
                return new PlatformSelection(platform, "Selected LWJGL 3 platform backend");
            } catch (RuntimeException exception) {
                return new PlatformSelection(
                        DEFAULT_PLATFORM,
                        "Failed to initialize requested LWJGL 3 backend: " + exception.getMessage() + "; falling back to LWJGL 2");
            }
        }

        return new PlatformSelection(
                DEFAULT_PLATFORM,
                "Unknown platform backend '" + requestedBackend + "'; falling back to LWJGL 2");
    }

    private static String readConfiguredBackend() {
        String backendProperty = System.getProperty(BACKEND_PROPERTY);
        if (backendProperty != null && !backendProperty.isEmpty()) {
            return backendProperty.trim();
        }

        String backendEnvironment = System.getenv(BACKEND_ENVIRONMENT);
        if (backendEnvironment != null && !backendEnvironment.isEmpty()) {
            return backendEnvironment.trim();
        }

        return "";
    }

    private static boolean isGlfwRuntimeAvailable() {
        try {
            Class.forName("org.lwjgl.glfw.GLFW");
            Class.forName("org.lwjgl.glfw.GLFWNativeWin32");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    private static final class PlatformSelection {
        private final MinecraftPlatform platform;
        private final String status;

        private PlatformSelection(MinecraftPlatform platform, String status) {
            this.platform = platform;
            this.status = status;
        }
    }
}