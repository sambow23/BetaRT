package mcrtx.render;

/** Game sky color is display RGB; transport radiance is linear exactly once. */
public final class DirectEnvironment {
    private DirectEnvironment() { }
    private static float clamp(float x) { return Math.max(0,Math.min(1,x)); }
    public static float linear(float x) {
        x=clamp(x);return x<=.04045f?x/12.92f:(float)Math.pow((x+.055f)/1.055f,2.4);
    }
    // Sky RGB, sun travel direction, sun RGB. The visible sun rotates around X.
    public static float[] lighting(boolean enabled,float r,float g,float b,float angle,float rain,float thunder) {
        double theta=angle*Math.PI*2;
        float cosine=(float)Math.cos(theta),sine=(float)Math.sin(theta);
        float daylight=clamp(cosine*2);
        float power=enabled?(float)Math.PI*daylight*(1-clamp(rain))*(1-clamp(thunder)):0;
        return new float[]{enabled?linear(r):0,enabled?linear(g):0,enabled?linear(b):0,
            0,-cosine,-sine,power,power,power};
    }
}
