public final class DirectWaterIceTransparencyTest {
    private DirectWaterIceTransparencyTest() {
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        require(RemixChunkBlockCapture.renderPassForCapture(8, 1, true) == 0,
                "still water was not made opaque");
        require(RemixChunkBlockCapture.renderPassForCapture(9, 1, true) == 0,
                "flowing water was not made opaque");
        require(RemixChunkBlockCapture.renderPassForCapture(79, 1, true) == 0,
                "ice was not made opaque");
        require(RemixChunkBlockCapture.renderPassForCapture(10, 1, true) == 1,
                "lava unexpectedly changed pass");
        require(RemixChunkBlockCapture.renderPassForCapture(90, 1, true) == 1,
                "portal unexpectedly changed pass");
        require(RemixChunkBlockCapture.renderPassForCapture(8, 1, false) == 1,
                "water toggle could not restore transparency");
        require(RemixChunkBlockCapture.renderPassForCapture(79, 1, false) == 1,
                "ice toggle could not restore transparency");
        require(RemixChunkBlockCapture.renderPassForCapture(8, 0, true) == 0,
                "opaque water pass was changed unexpectedly");
    }
}
