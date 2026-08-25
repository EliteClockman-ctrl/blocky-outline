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
    public boolean smoothTransition = true;
    public float smoothSpeed = 0.60f;

    public boolean fillEnabled = false;
    public boolean rainbowFill = false;
    public float fillRgbSpeed = 1.0f;
    public float fillHue = 0.16f;
    public float fillSaturation = 1.0f;
    public float fillValue = 1.0f;
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

    public void copyFrom(BlockyOutlineSettings other) {
        this.rainbowOutline = other.rainbowOutline;
        this.outlineRgbSpeed = Mth.clamp(other.outlineRgbSpeed, 0.1f, 5.0f);
        this.outlineHue = Mth.clamp(other.outlineHue, 0.0f, 1.0f);
        this.outlineSaturation = Mth.clamp(other.outlineSaturation, 0.0f, 1.0f);
        this.outlineValue = Mth.clamp(other.outlineValue, 0.0f, 1.0f);
        this.outlineOpacity = Mth.clamp(other.outlineOpacity, 0.0f, 1.0f);
        this.outlineWidth = Mth.clamp(other.outlineWidth, 0.5f, 10.0f);
        this.smoothTransition = other.smoothTransition;
        this.smoothSpeed = Mth.clamp(other.smoothSpeed, 0.05f, 1.0f);

        this.fillEnabled = other.fillEnabled;
        this.rainbowFill = other.rainbowFill;
        this.fillRgbSpeed = Mth.clamp(other.fillRgbSpeed, 0.1f, 5.0f);
        this.fillHue = Mth.clamp(other.fillHue, 0.0f, 1.0f);
        this.fillSaturation = Mth.clamp(other.fillSaturation, 0.0f, 1.0f);
        this.fillValue = Mth.clamp(other.fillValue, 0.0f, 1.0f);
        this.fillOpacity = Mth.clamp(other.fillOpacity, 0.0f, 1.0f);
    }

    public float[] getOutlineRgb(long nowMs) {
        if (this.rainbowOutline) {
            float hue = (float)(nowMs % (long)(4000.0f / this.outlineRgbSpeed)) / (4000.0f / this.outlineRgbSpeed);
            return hsvToRgb(hue, 1.0f, 1.0f);
        }
        return hsvToRgb(this.outlineHue, this.outlineSaturation, this.outlineValue);
    }

    public int getOutlineArgb(long nowMs) {
        int a = (int)(this.outlineOpacity * 255.0f);
        if (this.rainbowOutline) {
            float hue = (float)(nowMs % (long)(4000.0f / this.outlineRgbSpeed)) / (4000.0f / this.outlineRgbSpeed);
            return (a << 24) | hsvToRgbPacked(hue, 1.0f, 1.0f);
        }
        return (a << 24) | hsvToRgbPacked(this.outlineHue, this.outlineSaturation, this.outlineValue);
    }

    public int getFillArgb(long nowMs) {
        int a = (int)(this.fillOpacity * 255.0f);
        if (this.rainbowFill) {
            float hue = (float)(nowMs % (long)(4000.0f / this.fillRgbSpeed)) / (4000.0f / this.fillRgbSpeed);
            return (a << 24) | hsvToRgbPacked(hue, 1.0f, 1.0f);
        }
        return (a << 24) | hsvToRgbPacked(this.fillHue, this.fillSaturation, this.fillValue);
    }

    public static int hsvToRgbPacked(float h, float s, float v) {
        h = Mth.clamp(h, 0.0f, 1.0f);
        s = Mth.clamp(s, 0.0f, 1.0f);
        v = Mth.clamp(v, 0.0f, 1.0f);

        if (s == 0.0f) {
            int val = (int)(v * 255.0f);
            return (val << 16) | (val << 8) | val;
        }

        float h6 = h * 6.0f;
        int i = (int)Math.floor(h6);
        float f = h6 - (float)i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * f);
        float t = v * (1.0f - s * (1.0f - f));

        int r, g, b;
        switch (i % 6) {
            case 0 -> { r = (int)(v * 255.0f); g = (int)(t * 255.0f); b = (int)(p * 255.0f); }
            case 1 -> { r = (int)(q * 255.0f); g = (int)(v * 255.0f); b = (int)(p * 255.0f); }
            case 2 -> { r = (int)(p * 255.0f); g = (int)(v * 255.0f); b = (int)(t * 255.0f); }
            case 3 -> { r = (int)(p * 255.0f); g = (int)(q * 255.0f); b = (int)(v * 255.0f); }
            case 4 -> { r = (int)(t * 255.0f); g = (int)(p * 255.0f); b = (int)(v * 255.0f); }
            default -> { r = (int)(v * 255.0f); g = (int)(p * 255.0f); b = (int)(q * 255.0f); }
        }
        return (r << 16) | (g << 8) | b;
    }

    public static float[] hsvToRgb(float h, float s, float v) {
        int packed = hsvToRgbPacked(h, s, v);
        return new float[]{
            (float)((packed >> 16) & 0xFF) / 255.0f,
            (float)((packed >> 8) & 0xFF) / 255.0f,
            (float)(packed & 0xFF) / 255.0f
        };
    }

    public static float[] hsvToRgb(float h) {
        return hsvToRgb(h, 1.0f, 1.0f);
    }
}
