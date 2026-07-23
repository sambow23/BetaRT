import java.util.LinkedHashMap;
import java.util.Map;

final class CreeperFuseTracker {
    private static final int MAX_TRACKED_CREEPERS = 256;
    private static final int MAX_FUSE_TICKS = 30;
    private static final float FUSE_PROGRESS_TICKS = 28.0f;

    private final Map<Integer, FuseState> states =
            new LinkedHashMap<Integer, FuseState>(16, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<Integer, FuseState> eldest) {
                    return size() > MAX_TRACKED_CREEPERS;
                }
            };

    float resolveProgress(
            int entityId,
            int entityAge,
            int synchronizedFuseState,
            int previousFuseTicks,
            int currentFuseTicks,
            float partialTicks) {
        FuseState state = states.get(Integer.valueOf(entityId));
        if (state == null || entityAge < state.lastEntityAge) {
            state = new FuseState(entityAge);
            states.put(Integer.valueOf(entityId), state);
        }

        if (previousFuseTicks > 0 || currentFuseTicks > 0) {
            state.previousFuseTicks = clampFuseTicks(previousFuseTicks);
            state.currentFuseTicks = clampFuseTicks(currentFuseTicks);
            state.lastEntityAge = entityAge;
        } else {
            advanceSynchronizedState(state, entityAge, synchronizedFuseState);
        }

        float clampedPartialTicks = clampUnit(partialTicks);
        float interpolatedFuseTicks = state.previousFuseTicks
                + ((state.currentFuseTicks - state.previousFuseTicks) * clampedPartialTicks);
        return clampUnit(interpolatedFuseTicks / FUSE_PROGRESS_TICKS);
    }

    private static void advanceSynchronizedState(FuseState state, int entityAge, int synchronizedFuseState) {
        if (synchronizedFuseState < 0
                && state.lastSynchronizedFuseState > 0
                && state.currentFuseTicks <= 2) {
            state.transientFuseLatched = true;
        }
        state.lastSynchronizedFuseState = synchronizedFuseState;

        int elapsedTicks = Math.max(0, entityAge - state.lastEntityAge);
        if (elapsedTicks == 0) {
            return;
        }

        if (synchronizedFuseState > 0 || state.transientFuseLatched) {
            state.currentFuseTicks = clampFuseTicks(state.currentFuseTicks + elapsedTicks);
            state.previousFuseTicks = clampFuseTicks(state.currentFuseTicks - 1);
            if (state.currentFuseTicks >= MAX_FUSE_TICKS) {
                state.transientFuseLatched = false;
            }
        } else if (synchronizedFuseState < 0) {
            state.currentFuseTicks = clampFuseTicks(state.currentFuseTicks - elapsedTicks);
            state.previousFuseTicks = clampFuseTicks(state.currentFuseTicks + 1);
        } else {
            state.previousFuseTicks = state.currentFuseTicks;
        }
        state.lastEntityAge = entityAge;
    }

    private static int clampFuseTicks(int fuseTicks) {
        return Math.max(0, Math.min(MAX_FUSE_TICKS, fuseTicks));
    }

    private static float clampUnit(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static final class FuseState {
        int previousFuseTicks;
        int currentFuseTicks;
        int lastEntityAge;
        int lastSynchronizedFuseState = -1;
        boolean transientFuseLatched;

        FuseState(int entityAge) {
            lastEntityAge = entityAge - 1;
        }
    }
}
