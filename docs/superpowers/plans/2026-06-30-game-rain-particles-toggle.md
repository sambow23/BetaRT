# Game Rain Particles Toggle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a persistent in-game toggle that keeps Minecraft rain particles enabled by default and lets players hide them from the Remix-rendered scene.

**Architecture:** Implement this entirely in the Java capture layer. `McrtxRuntimeSettings` owns persistence, `MinecraftRemixHooks` and `McrtxQuickSettingsScreen` expose the in-game toggle, and `RemixParticleCapture` gates `/environment/rain.png` weather quad submission while continuing to suppress the original vanilla tessellator draw.

**Tech Stack:** Java 8 patched-client sources, existing source-level Java tests, PowerShell build script.

---

### Task 1: Add Rain Toggle Regression Tests

**Files:**
- Create: `tests/java/McrtxRainParticlesToggleTest.java`

- [ ] **Step 1: Write the failing test**

Create `tests/java/McrtxRainParticlesToggleTest.java` with source-level assertions:

```java
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import mcrtx.bridge.McrtxRuntimeSettings;

public final class McrtxRainParticlesToggleTest {
  public static void main(String[] args) throws Exception {
    require(McrtxRuntimeSettings.isGameRainParticlesEnabled(), "game rain should default on");

    Path tempDir = Files.createTempDirectory("mcrtx-rain-toggle");
    System.setProperty("user.dir", tempDir.toString());
    McrtxRuntimeSettings.setGameRainParticlesEnabled(false);
    require(!McrtxRuntimeSettings.isGameRainParticlesEnabled(), "setter should disable game rain");

    String saved = new String(Files.readAllBytes(tempDir.resolve("mcrtx-runtime.env")), StandardCharsets.US_ASCII);
    require(saved.indexOf("MCRTX_GAME_RAIN_PARTICLES_ENABLED=0") >= 0, "saved config should include disabled game rain");

    String runtimeSettings = read("java-src/mcrtx/bridge/McrtxRuntimeSettings.java");
    requireContains(runtimeSettings, "GAME_RAIN_PARTICLES_ENABLED_KEY", "rain config key");
    requireContains(runtimeSettings, "DEFAULT_GAME_RAIN_PARTICLES_ENABLED", "rain default");
    requireContains(runtimeSettings, "isGameRainParticlesEnabled()", "rain getter");
    requireContains(runtimeSettings, "setGameRainParticlesEnabled(boolean enabled)", "rain setter");

    String remixHooks = read("java-src/MinecraftRemixHooks.java");
    requireContains(remixHooks, "isGameRainParticlesEnabled()", "hook getter");
    requireContains(remixHooks, "setGameRainParticlesEnabled(boolean enabled)", "hook setter");
    requireContains(remixHooks, "getGameRainParticlesButtonLabel()", "hook label");

    String quickSettings = read("java-src/McrtxQuickSettingsScreen.java");
    requireContains(quickSettings, "GAME_RAIN_PARTICLES_BUTTON_ID", "button id");
    requireContains(quickSettings, "MinecraftRemixHooks.setGameRainParticlesEnabled", "button toggles setting");
    requireContains(quickSettings, "MinecraftRemixHooks.getGameRainParticlesButtonLabel()", "button label");

    String particleCapture = read("java-src/RemixParticleCapture.java");
    requireContains(particleCapture, "McrtxRuntimeSettings.isGameRainParticlesEnabled()", "particle capture reads setting");
    requireContains(particleCapture, "shouldSubmitWeatherRainQuad()", "weather rain submission gate");
    requireContains(particleCapture, "if (!shouldSubmitWeatherRainQuad())", "rain quads skipped when disabled");
  }

  private static String read(String path) throws Exception {
    return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
  }

  private static void requireContains(String haystack, String needle, String message) {
    if (haystack.indexOf(needle) < 0) {
      throw new AssertionError(message + " missing: " + needle);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
New-Item -ItemType Directory -Force -Path out\test-classes | Out-Null
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\javac.exe" -cp java-src -d out\test-classes tests\java\McrtxRainParticlesToggleTest.java java-src\mcrtx\bridge\McrtxRuntimeSettings.java java-src\mcrtx\bridge\McrtxRuntimeConfig.java
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\java.exe" -cp out\test-classes McrtxRainParticlesToggleTest
```

Expected: FAIL because `isGameRainParticlesEnabled()` is missing.

### Task 2: Add Runtime Setting and Quick Settings Wiring

**Files:**
- Modify: `java-src/mcrtx/bridge/McrtxRuntimeSettings.java`
- Modify: `java-src/MinecraftRemixHooks.java`
- Modify: `java-src/McrtxQuickSettingsScreen.java`

- [ ] **Step 1: Add persisted runtime setting**

In `McrtxRuntimeSettings`, add:

```java
public static final String GAME_RAIN_PARTICLES_ENABLED_KEY = "MCRTX_GAME_RAIN_PARTICLES_ENABLED";
public static final boolean DEFAULT_GAME_RAIN_PARTICLES_ENABLED = true;
private static boolean gameRainParticlesEnabled = DEFAULT_GAME_RAIN_PARTICLES_ENABLED;
```

Add getter/setter:

```java
public static boolean isGameRainParticlesEnabled() {
    synchronized (LOCK) {
        ensureLoaded();
        return gameRainParticlesEnabled;
    }
}

public static void setGameRainParticlesEnabled(boolean enabled) {
    synchronized (LOCK) {
        ensureLoaded();
        if (gameRainParticlesEnabled == enabled) {
            return;
        }
        gameRainParticlesEnabled = enabled;
        saveLocked();
    }
}
```

Load it in `ensureLoaded()`:

```java
gameRainParticlesEnabled = readBooleanSetting(
    fileValues,
    GAME_RAIN_PARTICLES_ENABLED_KEY,
    DEFAULT_GAME_RAIN_PARTICLES_ENABLED);
```

Save it in `saveLocked()`:

```java
fileValues.put(GAME_RAIN_PARTICLES_ENABLED_KEY, formatBoolean(gameRainParticlesEnabled));
```

- [ ] **Step 2: Add hook facade methods**

In `MinecraftRemixHooks`, add:

```java
public static boolean isGameRainParticlesEnabled() {
    return McrtxRuntimeSettings.isGameRainParticlesEnabled();
}

public static String getGameRainParticlesButtonLabel() {
    return "Game Rain: " + formatToggleState(isGameRainParticlesEnabled());
}

public static void setGameRainParticlesEnabled(boolean enabled) {
    McrtxRuntimeSettings.setGameRainParticlesEnabled(enabled);
}
```

- [ ] **Step 3: Add quick settings button**

In `McrtxQuickSettingsScreen`, add a button id:

```java
private static final int GAME_RAIN_PARTICLES_BUTTON_ID = 31;
```

Add button handling:

```java
if (button.f == GAME_RAIN_PARTICLES_BUTTON_ID) {
    MinecraftRemixHooks.setGameRainParticlesEnabled(!MinecraftRemixHooks.isGameRainParticlesEnabled());
    refreshButtons();
    return;
}
```

Refresh the label:

```java
ke gameRainParticlesButton = findButton(GAME_RAIN_PARTICLES_BUTTON_ID);
if (gameRainParticlesButton != null) {
    gameRainParticlesButton.e = MinecraftRemixHooks.getGameRainParticlesButtonLabel();
}
```

Add it to Graphics controls after Remix Clouds:

```java
addControl(new ke(
        GAME_RAIN_PARTICLES_BUTTON_ID,
        getControlX(),
        takeNextRowY(),
        getControlWidth(),
        CONTROL_HEIGHT,
        MinecraftRemixHooks.getGameRainParticlesButtonLabel()));
```

- [ ] **Step 4: Run test to verify runtime/UI wiring passes**

Run:

```powershell
New-Item -ItemType Directory -Force -Path out\test-classes | Out-Null
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\javac.exe" -cp java-src -d out\test-classes tests\java\McrtxRainParticlesToggleTest.java java-src\mcrtx\bridge\McrtxRuntimeSettings.java java-src\mcrtx\bridge\McrtxRuntimeConfig.java
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\java.exe" -cp out\test-classes McrtxRainParticlesToggleTest
```

Expected: may still fail on particle-capture assertions until Task 3.

### Task 3: Gate Weather Rain Particle Submission

**Files:**
- Modify: `java-src/RemixParticleCapture.java`

- [ ] **Step 1: Import runtime settings**

Add:

```java
import mcrtx.bridge.McrtxRuntimeSettings;
```

- [ ] **Step 2: Add weather rain submission predicate**

Add:

```java
private static boolean shouldSubmitWeatherRainQuad() {
    return activeWeatherTextureKind != WEATHER_TEXTURE_KIND_RAIN
            || McrtxRuntimeSettings.isGameRainParticlesEnabled();
}
```

- [ ] **Step 3: Skip rain submissions when disabled**

Inside the weather tessellator loop, before `MinecraftRenderHooks.captureParticleQuad(...)`, add:

```java
if (!shouldSubmitWeatherRainQuad()) {
    continue;
}
```

Keep `shouldSuppressVanillaTessellatorDraw()` returning true whenever rain weather capture is active so disabling game rain hides both the Remix-submitted rain quads and the original vanilla draw.

- [ ] **Step 4: Run red/green test**

Run:

```powershell
New-Item -ItemType Directory -Force -Path out\test-classes | Out-Null
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\javac.exe" -cp java-src -d out\test-classes tests\java\McrtxRainParticlesToggleTest.java java-src\mcrtx\bridge\McrtxRuntimeSettings.java java-src\mcrtx\bridge\McrtxRuntimeConfig.java
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\java.exe" -cp out\test-classes McrtxRainParticlesToggleTest
```

Expected: PASS.

### Task 4: Verify and Commit

**Files:**
- Test: `tests/java/McrtxRainParticlesToggleTest.java`
- Verify: existing Java source tests and patched-client build

- [ ] **Step 1: Run source-level Java tests**

Run:

```powershell
New-Item -ItemType Directory -Force -Path out\test-classes | Out-Null
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\javac.exe" -d out\test-classes tests\java\McrtxRainParticlesToggleTest.java tests\java\McrtxCloudModeTest.java tests\java\McrtxCloudModeWiringTest.java tests\java\McrtxRuntimeCloudSettingsDefaultTest.java tests\java\McrtxRuntimeCloudSettingsConfigTest.java
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\java.exe" -cp out\test-classes McrtxRainParticlesToggleTest
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\java.exe" -cp out\test-classes McrtxCloudModeTest
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\java.exe" -cp out\test-classes McrtxCloudModeWiringTest
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\java.exe" -cp out\test-classes McrtxRuntimeCloudSettingsDefaultTest
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\java.exe" -cp out\test-classes McrtxRuntimeCloudSettingsConfigTest
```

Expected: all exit 0.

- [ ] **Step 2: Build patched client**

Run:

```powershell
.\scripts\build-patched-client.ps1
```

Expected: exit 0 and `out\patched-client\minecraft-b1.7.3-client-mcrtx.jar` produced.

- [ ] **Step 3: Commit implementation**

Run:

```powershell
git add java-src/RemixParticleCapture.java java-src/MinecraftRemixHooks.java java-src/McrtxQuickSettingsScreen.java java-src/mcrtx/bridge/McrtxRuntimeSettings.java tests/java/McrtxRainParticlesToggleTest.java docs/superpowers/plans/2026-06-30-game-rain-particles-toggle.md
git commit -m "feat: add game rain particle toggle"
```
