import java.lang.reflect.Method;
import mcrtx.bridge.RemixSceneBridge;

public final class McrtxProfileCelestialAngleTest {
    public static void main(String[] args) throws Exception {
        if (args.length != 0) System.setProperty("mcrtx.profile.celestialAngle",args[0]);
        String authored=System.getProperty("mcrtx.profile.celestialAngle");
        float expected=authored==null ? 0.75f : Float.parseFloat(authored);
        boolean valid=Float.isFinite(expected) && expected>=0 && expected<1;
        Method angle=RemixSceneBridge.class.getDeclaredMethod("celestialAngle",float.class);
        angle.setAccessible(true);
        try {
            float actual=(Float)angle.invoke(null,0.75f);
            if (!valid || actual!=expected) throw new AssertionError("Profiling sky override/default mismatch");
        } catch (ExceptionInInitializerError error) {
            if (valid || !(error.getCause() instanceof IllegalArgumentException)) throw error;
        }
    }
}
