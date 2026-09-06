public final class FrameRateTest {
    private static void expect(String actual, String expected) {
        if (!actual.equals(expected)) {
            throw new AssertionError(actual + " != " + expected);
        }
    }

    public static void main(String[] args) {
        McrtxFrameRate rate = new McrtxFrameRate();
        expect(rate.sample(1000000000L, 10, 1, "300 fps, 2 chunk updates"),
                "Remix -- fps, Java 300 fps, 2 chunk updates");
        expect(rate.sample(1500000000L, 40, 1, "320 fps"), "Remix 60 fps (16.7 ms), Java 320 fps");
        expect(rate.sample(2000000000L, 40, 1, "200 fps"), "Remix 0 fps (-- ms), Java 200 fps");
        expect(rate.sample(2500000000L, 50, 2, "100 fps"), "Remix -- fps, Java 100 fps");
        expect(rate.sample(3000000000L, -1, 2, "100 fps"), "Remix -- fps, Java 100 fps");
        expect(rate.sample(3500000000L, 100, 3, "100 fps"), "Remix -- fps, Java 100 fps");
        expect(rate.sample(4000000000L, 200, 3, "100 fps"), "Remix 200 fps (5.0 ms), Java 100 fps");
        expect(rate.sample(4500000000L, 0, 3, "100 fps"), "Remix -- fps, Java 100 fps");
        expect(rate.sample(4500000000L, 2, 3, "100 fps"), "Remix -- fps, Java 100 fps");
    }
}
