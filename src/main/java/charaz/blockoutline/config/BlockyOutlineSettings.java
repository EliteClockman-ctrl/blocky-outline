package charaz.blockoutline.config;

import charaz.blockoutline.BlockyOutlineMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

public final class BlockyOutlineSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("blocky-outline.json");
    private static final BlockyOutlineSettings INSTANCE = new BlockyOutlineSettings();

    public boolean rainbowOutline = false;
    public float outlineRgbSpeed = 1.0f;
    public float outlineHue = 0.16f;
    public float outlineSaturation = 1.0f;
    public float outlineValue = 1.0f;
    public float outlineOpacity = 1.0f;
    public float outlineWidth = 2.0f;
    public boolean cornerMode = false;
    public float cornerLength = 0.28f;
    public boolean xrayMode = false;
    public float hitboxExpand = 0.002f;
    public boolean smoothTransition = true;
    public float smoothSpeed = 0.90f;
    public int motionMode = 1; // 0: Snappy Spring, 1: Back (Overshoot), 2: Quintic, 3: Fluid Bezier, 4: Smooth Lerp, 5: Instant (Raw)
    public float springStiffness = 320.0f;
    public float springDamping = 30.0f;
    public float backOvershoot = 1.35f;
    public float motionDurationMs = 65.0f;
    public boolean breathingEffect = false;
    public float breathingSpeed = 1.0f;

    public static final String[] MOTION_MODE_NAMES = {
        "Snappy Spring",
        "Back (Overshoot)",
        "Quintic (Instant)",
        "Fluid Bezier",
        "Smooth Lerp",
        "Instant (Raw)"
    };

    public static final String[] MOTION_MODE_DESCS = {
        "Physical spring with fast snap & preserved momentum",
        "Fast glide with subtle 1-bounce overshoot",
        "Ultra-fast 5th-degree curve, zero visual delay",
        "Apple iOS fluid acceleration & soft landing",
        "High-framerate responsive exponential glide",
        "Immediate 0ms snap with zero smoothing"
    };

    public boolean fillEnabled = false;
    public boolean fillGradientEnabled = false;
    public boolean rainbowFill = false;
    public float fillRgbSpeed = 1.0f;
    public float fillHue = 0.16f;
    public float fillSaturation = 1.0f;
    public float fillValue = 1.0f;
    public float fillHue2 = 0.83f;
    public float fillSaturation2 = 1.0f;
    public float fillValue2 = 1.0f;
    public float fillOpacity = 0.25f;

    public static BlockyOutlineSettings get() {
        return INSTANCE;
    }

    public static void load() {
        if (!Files.exists(FILE_PATH)) {
            save();
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            BlockyOutlineSettings loaded = GSON.fromJson(reader, BlockyOutlineSettings.class);
            if (loaded != null) {
                INSTANCE.copyFrom(loaded);
            }
        } catch (JsonSyntaxException e) {
            BlockyOutlineMod.LOGGER.error("Failed to parse config file, creating backup: {}", e.getMessage());
            backupCorruptedConfig();
            save();
        } catch (IOException e) {
            BlockyOutlineMod.LOGGER.error("Failed to read config file: {}", e.getMessage());
        }
    }

    public static void save() {
        INSTANCE.invalidateCache();
        try {
            Path parent = FILE_PATH.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            BlockyOutlineMod.LOGGER.error("Failed to save config file: {}", e.getMessage());
        }
    }

    private static void backupCorruptedConfig() {
        try {
            Path backupPath = FILE_PATH.resolveSibling("blocky-outline.json.bak");
            Files.copy(FILE_PATH, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            BlockyOutlineMod.LOGGER.error("Failed to backup corrupted config: {}", e.getMessage());
        }
    }

    public UserPreset userPreset1 = new UserPreset("My Preset 1");
    public UserPreset userPreset2 = new UserPreset("My Preset 2");
    public UserPreset userPreset3 = new UserPreset("My Preset 3");

    public static class UserPreset {
        public String name;
        public boolean hasData = false;
        public boolean rainbowOutline = false;
        public float outlineRgbSpeed = 1.0f;
        public float outlineHue = 0.45f;
        public float outlineSaturation = 1.0f;
        public float outlineValue = 1.0f;
        public float outlineOpacity = 1.0f;
        public float outlineWidth = 2.5f;
        public boolean cornerMode = false;
        public float cornerLength = 0.28f;
        public boolean xrayMode = false;
        public float hitboxExpand = 0.002f;
        public boolean smoothTransition = true;
        public float smoothSpeed = 0.85f;
        public int motionMode = 1;
        public float springStiffness = 200.0f;
        public float springDamping = 22.0f;
        public float backOvershoot = 1.70158f;
        public float motionDurationMs = 90.0f;
        public boolean breathingEffect = true;
        public float breathingSpeed = 1.0f;

        public boolean fillEnabled = false;
        public boolean fillGradientEnabled = false;
        public boolean rainbowFill = false;
        public float fillRgbSpeed = 1.0f;
        public float fillHue = 0.45f;
        public float fillSaturation = 1.0f;
        public float fillValue = 1.0f;
        public float fillHue2 = 0.55f;
        public float fillSaturation2 = 1.0f;
        public float fillValue2 = 1.0f;
        public float fillOpacity = 0.25f;

        public UserPreset() {
            this.name = "Custom Slot";
        }

        public UserPreset(String name) {
            this.name = name;
        }

        public void clear() {
            this.hasData = false;
            BlockyOutlineSettings.save();
        }

        public void saveCurrent(BlockyOutlineSettings s) {
            this.hasData = true;
            this.rainbowOutline = s.rainbowOutline;
            this.outlineRgbSpeed = s.outlineRgbSpeed;
            this.outlineHue = s.outlineHue;
            this.outlineSaturation = s.outlineSaturation;
            this.outlineValue = s.outlineValue;
            this.outlineOpacity = s.outlineOpacity;
            this.outlineWidth = s.outlineWidth;
            this.cornerMode = s.cornerMode;
            this.cornerLength = s.cornerLength;
            this.xrayMode = s.xrayMode;
            this.hitboxExpand = s.hitboxExpand;
            this.smoothTransition = s.smoothTransition;
            this.smoothSpeed = s.smoothSpeed;
            this.motionMode = s.motionMode;
            this.springStiffness = s.springStiffness;
            this.springDamping = s.springDamping;
            this.backOvershoot = s.backOvershoot;
            this.motionDurationMs = s.motionDurationMs;
            this.breathingEffect = s.breathingEffect;
            this.breathingSpeed = s.breathingSpeed;

            this.fillEnabled = s.fillEnabled;
            this.fillGradientEnabled = s.fillGradientEnabled;
            this.rainbowFill = s.rainbowFill;
            this.fillRgbSpeed = s.fillRgbSpeed;
            this.fillHue = s.fillHue;
            this.fillSaturation = s.fillSaturation;
            this.fillValue = s.fillValue;
            this.fillHue2 = s.fillHue2;
            this.fillSaturation2 = s.fillSaturation2;
            this.fillValue2 = s.fillValue2;
            this.fillOpacity = s.fillOpacity;
            BlockyOutlineSettings.save();
        }

        public void applyTo(BlockyOutlineSettings s) {
            if (!this.hasData) return;
            s.rainbowOutline = this.rainbowOutline;
            s.outlineRgbSpeed = this.outlineRgbSpeed;
            s.outlineHue = this.outlineHue;
            s.outlineSaturation = this.outlineSaturation;
            s.outlineValue = this.outlineValue;
            s.outlineOpacity = this.outlineOpacity;
            s.outlineWidth = this.outlineWidth;
            s.cornerMode = this.cornerMode;
            s.cornerLength = this.cornerLength;
            s.xrayMode = this.xrayMode;
            s.hitboxExpand = this.hitboxExpand;
            s.smoothTransition = this.smoothTransition;
            s.smoothSpeed = this.smoothSpeed;
            s.motionMode = this.motionMode;
            s.springStiffness = this.springStiffness;
            s.springDamping = this.springDamping;
            s.backOvershoot = this.backOvershoot;
            s.motionDurationMs = this.motionDurationMs;
            s.breathingEffect = this.breathingEffect;
            s.breathingSpeed = this.breathingSpeed;

            s.fillEnabled = this.fillEnabled;
            s.fillGradientEnabled = this.fillGradientEnabled;
            s.rainbowFill = this.rainbowFill;
            s.fillRgbSpeed = this.fillRgbSpeed;
            s.fillHue = this.fillHue;
            s.fillSaturation = this.fillSaturation;
            s.fillValue = this.fillValue;
            s.fillHue2 = this.fillHue2;
            s.fillSaturation2 = this.fillSaturation2;
            s.fillValue2 = this.fillValue2;
            s.fillOpacity = this.fillOpacity;
            BlockyOutlineSettings.save();
        }
    }

    public void copyFrom(BlockyOutlineSettings other) {
        this.rainbowOutline = other.rainbowOutline;
        this.outlineRgbSpeed = Mth.clamp(other.outlineRgbSpeed, 0.1f, 5.0f);
        this.outlineHue = Mth.clamp(other.outlineHue, 0.0f, 1.0f);
        this.outlineSaturation = Mth.clamp(other.outlineSaturation, 0.0f, 1.0f);
        this.outlineValue = Mth.clamp(other.outlineValue, 0.0f, 1.0f);
        this.outlineOpacity = Mth.clamp(other.outlineOpacity, 0.0f, 1.0f);
        this.outlineWidth = Mth.clamp(other.outlineWidth, 0.5f, 10.0f);
        this.cornerMode = other.cornerMode;
        this.cornerLength = Mth.clamp(other.cornerLength, 0.10f, 0.50f);
        this.xrayMode = other.xrayMode;
        this.hitboxExpand = Mth.clamp(other.hitboxExpand, 0.0f, 0.05f);
        this.smoothTransition = other.smoothTransition;
        this.smoothSpeed = Mth.clamp(other.smoothSpeed, 0.05f, 1.0f);
        this.motionMode = (other.motionMode >= 0 && other.motionMode <= 5) ? other.motionMode : 1;
        this.springStiffness = Mth.clamp(other.springStiffness, 60.0f, 600.0f);
        this.springDamping = Mth.clamp(other.springDamping, 10.0f, 60.0f);
        this.backOvershoot = Mth.clamp(other.backOvershoot, 0.5f, 3.0f);
        this.motionDurationMs = Mth.clamp(other.motionDurationMs, 25.0f, 250.0f);
        this.breathingEffect = other.breathingEffect;
        this.breathingSpeed = Mth.clamp(other.breathingSpeed, 0.2f, 3.0f);

        this.fillEnabled = other.fillEnabled;
        this.fillGradientEnabled = other.fillGradientEnabled;
        this.rainbowFill = other.rainbowFill;
        this.fillRgbSpeed = Mth.clamp(other.fillRgbSpeed, 0.1f, 5.0f);
        this.fillHue = Mth.clamp(other.fillHue, 0.0f, 1.0f);
        this.fillSaturation = Mth.clamp(other.fillSaturation, 0.0f, 1.0f);
        this.fillValue = Mth.clamp(other.fillValue, 0.0f, 1.0f);
        this.fillHue2 = Mth.clamp(other.fillHue2, 0.0f, 1.0f);
        this.fillSaturation2 = Mth.clamp(other.fillSaturation2, 0.0f, 1.0f);
        this.fillValue2 = Mth.clamp(other.fillValue2, 0.0f, 1.0f);
        this.fillOpacity = Mth.clamp(other.fillOpacity, 0.0f, 1.0f);

        if (other.userPreset1 != null) this.userPreset1 = other.userPreset1;
        if (other.userPreset2 != null) this.userPreset2 = other.userPreset2;
        if (other.userPreset3 != null) this.userPreset3 = other.userPreset3;
        this.invalidateCache();
    }

    // Fast Cached Colors to eliminate per-frame HSV math
    private transient int cachedOutlineArgb = -1;
    private transient int cachedFillArgb = -1;
    private transient int cachedFillArgb2 = -1;
    private transient boolean cacheValid = false;

    private long lastWaveCalcMs = -1;
    private float cachedWaveOutline = 1.0f;
    private float cachedWaveFill = 1.0f;

    public void invalidateCache() {
        this.cacheValid = false;
        this.lastWaveCalcMs = -1;
    }

    private void updateCache() {
        int aOut = (int)(this.outlineOpacity * 255.0f);
        this.cachedOutlineArgb = (aOut << 24) | hsvToRgbPacked(this.outlineHue, this.outlineSaturation, this.outlineValue);

        int aFill = (int)(this.fillOpacity * 255.0f);
        this.cachedFillArgb = (aFill << 24) | hsvToRgbPacked(this.fillHue, this.fillSaturation, this.fillValue);

        if (this.fillGradientEnabled) {
            this.cachedFillArgb2 = (aFill << 24) | hsvToRgbPacked(this.fillHue2, this.fillSaturation2, this.fillValue2);
        } else {
            this.cachedFillArgb2 = this.cachedFillArgb;
        }
        this.cacheValid = true;
    }

    private void updateWave(long nowMs) {
        if (nowMs == this.lastWaveCalcMs) return;
        this.lastWaveCalcMs = nowMs;
        if (this.breathingEffect) {
            float timeFactor = (nowMs * 0.0032f) * this.breathingSpeed;
            float sinVal = (float) Math.sin(timeFactor);
            this.cachedWaveOutline = 0.90f + 0.10f * sinVal;
            this.cachedWaveFill = 0.85f + 0.15f * sinVal;
        } else {
            this.cachedWaveOutline = 1.0f;
            this.cachedWaveFill = 1.0f;
        }
    }

    public int getOutlineArgb(long nowMs) {
        if (!this.cacheValid) updateCache();
        if (!this.rainbowOutline && !this.breathingEffect) {
            return this.cachedOutlineArgb;
        }
        updateWave(nowMs);
        float baseOpac = this.breathingEffect ? Mth.clamp(this.outlineOpacity * this.cachedWaveOutline, 0.0f, 1.0f) : this.outlineOpacity;
        int a = (int)(baseOpac * 255.0f);
        if (this.rainbowOutline) {
            float speed = Math.max(0.1f, this.outlineRgbSpeed);
            float period = 4000.0f / speed;
            float hue = (float)((nowMs % (long) period) / period);
            return (a << 24) | hsvToRgbPacked(hue, 1.0f, 1.0f);
        }
        return (a << 24) | (this.cachedOutlineArgb & 0x00FFFFFF);
    }

    public int getFillArgb(long nowMs) {
        if (!this.cacheValid) updateCache();
        if (!this.rainbowFill && !this.breathingEffect) {
            return this.cachedFillArgb;
        }
        updateWave(nowMs);
        float baseOpac = this.breathingEffect ? Mth.clamp(this.fillOpacity * this.cachedWaveFill, 0.0f, 1.0f) : this.fillOpacity;
        int a = (int)(baseOpac * 255.0f);
        if (this.rainbowFill) {
            float speed = Math.max(0.1f, this.fillRgbSpeed);
            float period = 4000.0f / speed;
            float hue = (float)((nowMs % (long) period) / period);
            return (a << 24) | hsvToRgbPacked(hue, 1.0f, 1.0f);
        }
        return (a << 24) | (this.cachedFillArgb & 0x00FFFFFF);
    }

    public int getFillArgb2(long nowMs) {
        if (!this.cacheValid) updateCache();
        if (!this.fillGradientEnabled || this.rainbowFill) {
            return getFillArgb(nowMs);
        }
        if (!this.breathingEffect) {
            return this.cachedFillArgb2;
        }
        updateWave(nowMs);
        float baseOpac = Mth.clamp(this.fillOpacity * this.cachedWaveFill, 0.0f, 1.0f);
        int a = (int)(baseOpac * 255.0f);
        return (a << 24) | (this.cachedFillArgb2 & 0x00FFFFFF);
    }

    public static int hsvToRgbPacked(float h, float s, float v) {
        h = Mth.clamp(h, 0.0f, 1.0f);
        s = Mth.clamp(s, 0.0f, 1.0f);
        v = Mth.clamp(v, 0.0f, 1.0f);

        if (s <= 0.00001f) {
            int val = (int)(v * 255.0f);
            return (val << 16) | (val << 8) | val;
        }

        float h6 = h * 6.0f;
        int i = (int)h6;
        float f = h6 - (float)i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * f);
        float t = v * (1.0f - s * (1.0f - f));

        int r, g, b;
        int sector = i % 6;
        if (sector == 0) {
            r = (int)(v * 255.0f); g = (int)(t * 255.0f); b = (int)(p * 255.0f);
        } else if (sector == 1) {
            r = (int)(q * 255.0f); g = (int)(v * 255.0f); b = (int)(p * 255.0f);
        } else if (sector == 2) {
            r = (int)(p * 255.0f); g = (int)(v * 255.0f); b = (int)(t * 255.0f);
        } else if (sector == 3) {
            r = (int)(p * 255.0f); g = (int)(q * 255.0f); b = (int)(v * 255.0f);
        } else if (sector == 4) {
            r = (int)(t * 255.0f); g = (int)(p * 255.0f); b = (int)(v * 255.0f);
        } else {
            r = (int)(v * 255.0f); g = (int)(p * 255.0f); b = (int)(q * 255.0f);
        }
        return (r << 16) | (g << 8) | b;
    }

    public static float[] hsvToRgb(float h, float s, float v) {
        int packed = hsvToRgbPacked(h, s, v);
        return new float[]{
            (float)((packed >> 16) & 0xFF) * 0.003921569f,
            (float)((packed >> 8) & 0xFF) * 0.003921569f,
            (float)(packed & 0xFF) * 0.003921569f
        };
    }

    public static float[] rgbToHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0.0f;
        if (delta > 0.00001f) {
            if (max == r) {
                h = ((g - b) / delta) % 6.0f;
            } else if (max == g) {
                h = ((b - r) / delta) + 2.0f;
            } else {
                h = ((r - g) / delta) + 4.0f;
            }
            h /= 6.0f;
            if (h < 0.0f) h += 1.0f;
        }

        float s = (max <= 0.00001f) ? 0.0f : (delta / max);
        float v = max;

        return new float[]{ h, s, v };
    }
}
