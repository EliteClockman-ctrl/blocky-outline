package charaz.blockoutline.config;

import charaz.blockoutline.BlockyOutlineMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BlockyOutlineSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("blocky-outline.json");
    private static final BlockyOutlineSettings INSTANCE = new BlockyOutlineSettings();

    // === OUTLINE SETTINGS ===
    public boolean rainbowOutline = false;
    public float outlineRgbSpeed = 1.0f;
    public float outlineHue = 0.16f; // Default: Yellow/Gold (~0.16f)
    public float outlineOpacity = 1.0f;
    public float outlineWidth   = 2.0f;

    // === FILL SETTINGS ===
    public boolean fillEnabled  = false;
    public boolean rainbowFill  = false;
    public float fillRgbSpeed   = 1.0f;
    public float fillHue = 0.16f; // Default: Yellow/Gold (~0.16f)
    public float fillOpacity = 0.30f;

    private BlockyOutlineSettings() {}

    public static BlockyOutlineSettings get() { return INSTANCE; }

    public static void load() {
        if (!Files.exists(FILE_PATH)) { save(); return; }
        try (Reader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            StoredData d = GSON.fromJson(reader, StoredData.class);
            if (d != null) {
                INSTANCE.rainbowOutline  = d.rainbowOutline;
                INSTANCE.outlineRgbSpeed = Mth.clamp(d.outlineRgbSpeed, 0.1f, 5.0f);
                INSTANCE.outlineHue      = Mth.clamp(d.outlineHue, 0.0f, 1.0f);
                INSTANCE.outlineOpacity  = Mth.clamp(d.outlineOpacity, 0.0f, 1.0f);
                INSTANCE.outlineWidth    = Mth.clamp(d.outlineWidth,   0.5f, 5.0f);

                INSTANCE.fillEnabled    = d.fillEnabled;
                INSTANCE.rainbowFill    = d.rainbowFill;
                INSTANCE.fillRgbSpeed   = Mth.clamp(d.fillRgbSpeed, 0.1f, 5.0f);
                INSTANCE.fillHue        = Mth.clamp(d.fillHue, 0.0f, 1.0f);
                INSTANCE.fillOpacity    = Mth.clamp(d.fillOpacity, 0.0f, 1.0f);
            }
        } catch (IOException | JsonSyntaxException ex) {
            BlockyOutlineMod.LOGGER.error("Failed to read config '{}'", FILE_PATH, ex);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(new StoredData(INSTANCE), writer);
            }
        } catch (IOException ex) {
            BlockyOutlineMod.LOGGER.error("Failed to write config '{}'", FILE_PATH, ex);
        }
    }

    /** Returns R,G,B as 0.0-1.0 floats, applying rainbow if enabled */
    public float[] getOutlineRgb(long timeMs) {
        if (rainbowOutline) {
            double hue = ((timeMs / 1000.0) * outlineRgbSpeed) % 1.0;
            return hsvToRgb((float) hue);
        }
        return hsvToRgb(outlineHue);
    }

    public float[] getFillRgb(long timeMs) {
        if (rainbowFill) {
            double hue = (((timeMs / 1000.0) * fillRgbSpeed) + 0.5) % 1.0;
            return hsvToRgb((float) hue);
        }
        return hsvToRgb(fillHue);
    }

    public static float[] hsvToRgb(float h) {
        int sector = (int)(h * 6);
        float f = h * 6 - sector;
        float q = 1 - f, t = f;
        return switch (sector % 6) {
            case 0  -> new float[]{1, t, 0};
            case 1  -> new float[]{q, 1, 0};
            case 2  -> new float[]{0, 1, t};
            case 3  -> new float[]{0, q, 1};
            case 4  -> new float[]{t, 0, 1};
            default -> new float[]{1, 0, q};
        };
    }

    private static final class StoredData {
        boolean rainbowOutline  = false;
        float outlineRgbSpeed   = 1.0f;
        float outlineHue = 0.16f;
        float outlineOpacity = 1.0f;
        float outlineWidth   = 2.0f;

        boolean fillEnabled  = false;
        boolean rainbowFill  = false;
        float fillRgbSpeed   = 1.0f;
        float fillHue = 0.16f;
        float fillOpacity = 0.30f;

        StoredData() {}
        StoredData(BlockyOutlineSettings s) {
            this.rainbowOutline  = s.rainbowOutline;
            this.outlineRgbSpeed = s.outlineRgbSpeed;
            this.outlineHue      = s.outlineHue;
            this.outlineOpacity  = s.outlineOpacity;
            this.outlineWidth    = s.outlineWidth;
            this.fillEnabled     = s.fillEnabled;
            this.rainbowFill     = s.rainbowFill;
            this.fillRgbSpeed    = s.fillRgbSpeed;
            this.fillHue         = s.fillHue;
            this.fillOpacity     = s.fillOpacity;
        }
    }
}
