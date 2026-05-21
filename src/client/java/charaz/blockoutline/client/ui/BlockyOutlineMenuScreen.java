package charaz.blockoutline.client.ui;

import charaz.blockoutline.config.BlockyOutlineSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * Block Outline Customizer Menu
 * Layout: two columns — Outline Settings (left) | Fill Settings (right)
 * Fully responsive and scales to fit any GUI scale / window size!
 */
public class BlockyOutlineMenuScreen extends Screen {

    // ── Dynamic Layout Fields ──────────────────────────────────────────────────
    private int panelW;
    private int panelH;
    private int px;
    private int py;
    private int colW;
    private int colGap;
    private int margin;
    private int topPad;
    private int rowH;
    private int rowGap;
    private int sliderW;

    // ── Row Definitions ────────────────────────────────────────────────────────
    private static final int O_RAINBOW   = 0;
    private static final int O_RGB_SPEED = 1;
    private static final int O_COLOR     = 2;
    private static final int O_OPACITY   = 3;
    private static final int O_WIDTH     = 4;
    private static final int O_ROWS      = 5;

    private static final int F_ENABLE    = 0;
    private static final int F_RAINBOW   = 1;
    private static final int F_RGB_SPEED = 2;
    private static final int F_COLOR     = 3;
    private static final int F_OPACITY   = 4;
    private static final int F_ROWS      = 5;

    private static final String[] O_LABELS = {
            "Rainbow Outline", "RGB Speed", "Colors", "Opacity", "Width"
    };
    private static final String[] F_LABELS = {
            "Enable Fill", "Rainbow Fill", "RGB Speed", "Colors", "Opacity"
    };

    private final BlockyOutlineSettings settings = BlockyOutlineSettings.get();

    // Dragging state
    private int draggingCol = -1;
    private int draggingRow = -1;

    public BlockyOutlineMenuScreen() {
        super(Component.literal("Block Outline Customizer"));
    }

    // ── Layout calculation ─────────────────────────────────────────────────────

    private void updateLayout() {
        // Leave at least 8px margin on left/right and top/bottom
        this.panelW = Math.min(this.width - 16, 600);
        this.panelH = Math.min(this.height - 16, 260); // 5 rows need less vertical space
        this.px = (this.width - panelW) / 2;
        this.py = (this.height - panelH) / 2;

        this.colGap = 16;
        this.margin = 12;
        this.colW = (panelW - margin * 2 - colGap) / 2;
        this.topPad = 44; // Start rows below title/headers

        // Dynamically scale row height and gap to fit screen height perfectly!
        int availableH = panelH - topPad - 28; // 28px reserved for Done button
        this.rowH = Math.max(14, Math.min(22, availableH / 5 - 2));
        this.rowGap = Math.max(2, (availableH - (rowH * 5)) / 4);

        // Dynamically scale slider width to avoid label overlapping
        this.sliderW = Math.max(50, Math.min(100, colW - 110));
    }

    @Override
    protected void init() {
        super.init();
        updateLayout();

        // Add Done button at the bottom center of the panel
        this.addRenderableWidget(Button.builder(
                Component.literal("Done"),
                btn -> this.onClose()
            ).bounds(px + (panelW - 100) / 2, py + panelH - 22, 100, 18).build());
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float tick) {
        g.fill(0, 0, this.width, this.height, 0xC0000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float tick) {
        // Ensure layout is fully updated in case of resizing
        updateLayout();

        super.renderBackground(g, mouseX, mouseY, tick);

        // 1. Panel Background
        g.fill(px, py, px + panelW, py + panelH, 0xE6101520);
        
        // Header bar
        g.fillGradient(px, py, px + panelW, py + 24, 0xFF182236, 0xFF0E1526);
        g.fill(px, py, px + panelW, py + 1, 0xFF3A5080); // Accent top line

        // Title
        g.drawCenteredString(this.font, "Block Outline Customizer", px + panelW / 2, py + 8, 0xFFFFFFFF);

        // 2. Column Headers
        int leftColX  = px + margin;
        int rightColX = px + margin + colW + colGap;

        g.drawCenteredString(this.font, "Outline Settings", leftColX  + colW / 2, py + 29, 0xFF4ECFE8);
        g.drawCenteredString(this.font, "Fill Settings",   rightColX + colW / 2, py + 29, 0xFFFF6060);

        // Column divider line
        int divX = px + margin + colW + colGap / 2;
        g.fill(divX, py + 24, divX + 1, py + panelH - 26, 0x33FFFFFF);

        // 3. Render Rows
        int rowsY = py + topPad;
        for (int row = 0; row < O_ROWS; row++) {
            renderRow(g, leftColX, rowsY + row * (rowH + rowGap), 0, row, mouseX, mouseY);
        }
        for (int row = 0; row < F_ROWS; row++) {
            renderRow(g, rightColX, rowsY + row * (rowH + rowGap), 1, row, mouseX, mouseY);
        }

        // 4. Render standard widgets (Done button)
        super.render(g, mouseX, mouseY, tick);
    }

    private void renderRow(GuiGraphics g, int rx, int ry, int col, int row, int mx, int my) {
        boolean disabled = isRowDisabled(col, row);
        boolean hovered = !disabled && mx >= rx && mx <= rx + colW && my >= ry && my <= ry + rowH;
        int bg = disabled ? 0x4410141C : (hovered ? 0x991E2E4A : 0x7714202F);
        
        g.fill(rx, ry, rx + colW, ry + rowH, bg);
        g.fill(rx, ry + rowH - 1, rx + colW, ry + rowH, 0x22FFFFFF); // Row divider

        String label = col == 0 ? O_LABELS[row] : F_LABELS[row];
        int labelColor = disabled ? 0xFF667788 : 0xFFCCDDFF;
        g.drawString(this.font, label, rx + 6, ry + (rowH - 8) / 2, labelColor, false);

        if (isCheckboxRow(col, row)) {
            renderCheckbox(g, rx, ry, col, row);
        } else {
            renderSlider(g, rx, ry, col, row, disabled);
        }
    }

    private void renderCheckbox(GuiGraphics g, int rx, int ry, int col, int row) {
        boolean checked = getCheckboxValue(col, row);
        int boxSize = Math.min(14, rowH - 6);
        int boxX = rx + colW - boxSize - 6;
        int boxY = ry + (rowH - boxSize) / 2;

        g.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, checked ? 0xFF2EC27E : 0xFF3A3A4A);
        g.fill(boxX + 1, boxY + 1, boxX + boxSize - 1, boxY + boxSize - 1, checked ? 0xFF38D88A : 0xFF282830);
        if (checked) {
            // Draw simple checkmark
            g.fill(boxX + 3, boxY + boxSize - 6, boxX + 5, boxY + boxSize - 2, 0xFFFFFFFF);
            g.fill(boxX + 5, boxY + boxSize - 4, boxX + boxSize - 3, boxY + boxSize - 8, 0xFFFFFFFF);
        }
    }

    private void renderSlider(GuiGraphics g, int rx, int ry, int col, int row, boolean disabled) {
        float pct = getSliderPct(col, row);
        String val = getSliderValueStr(col, row);

        int sliderX = rx + colW - sliderW - 6;
        int sliderY = ry + (rowH - 4) / 2;
        int filled  = (int)(sliderW * pct);

        boolean isColorRow = (col == 0 && row == O_COLOR) || (col == 1 && row == F_COLOR);

        if (isColorRow) {
            // Draw a gorgeous spectrum color bar inside the track! (Desaturated if disabled)
            for (int i = 0; i < sliderW; i++) {
                float hue = (float) i / sliderW;
                float[] rgb = disabled ? new float[]{0.3f, 0.3f, 0.3f} : BlockyOutlineSettings.hsvToRgb(hue);
                int color = 0xFF000000 | ((int)(rgb[0] * 255) << 16) | ((int)(rgb[1] * 255) << 8) | (int)(rgb[2] * 255);
                g.fill(sliderX + i, sliderY - 1, sliderX + i + 1, sliderY + 5, color);
            }
        } else {
            // Standard slider track (dimmed if disabled)
            int trackBg = disabled ? 0xFF202630 : 0xFF1A2234;
            int fillBg  = disabled ? 0xFF446688 : 0xFF5FA8FF;
            g.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 4, trackBg);
            g.fill(sliderX, sliderY, sliderX + filled, sliderY + 4, fillBg);
        }

        // Thumb styling (Color row has a colored thumb with a white border)
        int thumbColor = disabled ? 0xFF555555 : 0xFFEAF2FF;
        int thumbBorder = disabled ? 0xFF777777 : 0xFFEAF2FF;
        if (isColorRow && !disabled) {
            float[] rgb = BlockyOutlineSettings.hsvToRgb(pct);
            thumbColor = 0xFF000000 | ((int)(rgb[0] * 255) << 16) | ((int)(rgb[1] * 255) << 8) | (int)(rgb[2] * 255);
        }

        g.fill(sliderX + filled - 3, sliderY - 3, sliderX + filled + 3, sliderY + 7, thumbBorder);
        g.fill(sliderX + filled - 2, sliderY - 2, sliderX + filled + 2, sliderY + 6, thumbColor);

        // Value text (grayed out if disabled)
        if (!val.isEmpty()) {
            int valColor = disabled ? 0xFF667788 : 0xFFAABBDD;
            g.drawString(this.font, val, sliderX - this.font.width(val) - 4, ry + (rowH - 8) / 2, valColor, false);
        }
    }

    // ── Mouse handling ────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent ev, boolean dbl) {
        if (ev.button() != GLFW.GLFW_MOUSE_BUTTON_1) return super.mouseClicked(ev, dbl);
        double mx = ev.x(), my = ev.y();

        int rowsY = py + topPad;

        // Left Column
        int lx = px + margin;
        for (int row = 0; row < O_ROWS; row++) {
            if (isRowDisabled(0, row)) continue;
            int ry = rowsY + row * (rowH + rowGap);
            if (inBounds(mx, my, lx, ry, colW, rowH)) {
                if (isCheckboxRow(0, row)) { toggleCheckbox(0, row); BlockyOutlineSettings.save(); return true; }
                draggingCol = 0; draggingRow = row;
                applySlider(0, row, mx, lx); BlockyOutlineSettings.save(); return true;
            }
        }
        // Right Column
        int rx2 = px + margin + colW + colGap;
        for (int row = 0; row < F_ROWS; row++) {
            if (isRowDisabled(1, row)) continue;
            int ry = rowsY + row * (rowH + rowGap);
            if (inBounds(mx, my, rx2, ry, colW, rowH)) {
                if (isCheckboxRow(1, row)) { toggleCheckbox(1, row); BlockyOutlineSettings.save(); return true; }
                draggingCol = 1; draggingRow = row;
                applySlider(1, row, mx, rx2); BlockyOutlineSettings.save(); return true;
            }
        }
        return super.mouseClicked(ev, dbl);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent ev, double dx, double dy) {
        if (ev.button() == GLFW.GLFW_MOUSE_BUTTON_1 && draggingCol != -1) {
            if (isRowDisabled(draggingCol, draggingRow)) {
                draggingCol = -1;
                draggingRow = -1;
                return false;
            }
            int colX = px + margin + draggingCol * (colW + colGap);
            applySlider(draggingCol, draggingRow, ev.x(), colX);
            BlockyOutlineSettings.save();
            return true;
        }
        return super.mouseDragged(ev, dx, dy);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent ev) {
        draggingCol = -1; draggingRow = -1;
        return super.mouseReleased(ev);
    }

    @Override
    public boolean keyPressed(KeyEvent ev) {
        if (ev.key() == GLFW.GLFW_KEY_M || ev.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose(); return true;
        }
        return super.keyPressed(ev);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void removed() { BlockyOutlineSettings.save(); super.removed(); }

    // ── Helper methods ────────────────────────────────────────────────────────

    private boolean isRowDisabled(int col, int row) {
        if (col == 0) {
            // Outline column
            if (row == O_RGB_SPEED) return !settings.rainbowOutline; // RGB Speed only useful when rainbow ON
            if (row == O_COLOR)     return settings.rainbowOutline;  // Colors disabled when rainbow ON
        } else {
            // Fill column
            if (row == F_RAINBOW)   return !settings.fillEnabled;    // Can't toggle rainbow if fill off
            if (row == F_RGB_SPEED) return !settings.fillEnabled || !settings.rainbowFill; // Only when fill+rainbow ON
            if (row == F_COLOR)     return !settings.fillEnabled || settings.rainbowFill;  // Disabled when fill off OR rainbow ON
            if (row == F_OPACITY)   return !settings.fillEnabled;    // Opacity only when fill on
        }
        return false;
    }

    private boolean isCheckboxRow(int col, int row) {
        if (col == 0) return row == O_RAINBOW;
        return row == F_ENABLE || row == F_RAINBOW;
    }

    private boolean getCheckboxValue(int col, int row) {
        if (col == 0) return settings.rainbowOutline;
        return row == F_ENABLE ? settings.fillEnabled : settings.rainbowFill;
    }

    private void toggleCheckbox(int col, int row) {
        if (col == 0) { settings.rainbowOutline = !settings.rainbowOutline; return; }
        if (row == F_ENABLE) settings.fillEnabled = !settings.fillEnabled;
        else                 settings.rainbowFill  = !settings.rainbowFill;
    }

    private float getSliderPct(int col, int row) {
        if (col == 0) return switch (row) {
            case O_RGB_SPEED -> (settings.outlineRgbSpeed - 0.1f) / 4.9f;
            case O_COLOR     -> settings.outlineHue;
            case O_OPACITY   -> settings.outlineOpacity;
            case O_WIDTH     -> (settings.outlineWidth - 0.5f) / 4.5f;
            default -> 0;
        };
        return switch (row) {
            case F_RGB_SPEED -> (settings.fillRgbSpeed - 0.1f) / 4.9f;
            case F_COLOR     -> settings.fillHue;
            case F_OPACITY   -> settings.fillOpacity;
            default -> 0;
        };
    }

    private String getSliderValueStr(int col, int row) {
        if (col == 0) return switch (row) {
            case O_RGB_SPEED -> String.format("%.1f", settings.outlineRgbSpeed);
            case O_COLOR     -> ""; // Empty since we use visual color bar + thumb
            case O_OPACITY   -> String.format("%.2f", settings.outlineOpacity);
            case O_WIDTH     -> String.format("%.1f", settings.outlineWidth);
            default -> "";
        };
        return switch (row) {
            case F_RGB_SPEED -> String.format("%.1f", settings.fillRgbSpeed);
            case F_COLOR     -> ""; // Empty since we use visual color bar + thumb
            case F_OPACITY   -> String.format("%.2f", settings.fillOpacity);
            default -> "";
        };
    }

    private void applySlider(int col, int row, double mouseX, int colX) {
        int sliderX = colX + colW - sliderW - 6;
        float pct = (float) Mth.clamp((mouseX - sliderX) / (double) sliderW, 0.0, 1.0);

        if (col == 0) {
            switch (row) {
                case O_RGB_SPEED -> settings.outlineRgbSpeed = Math.round((0.1f + pct * 4.9f) * 10f) / 10f;
                case O_COLOR     -> settings.outlineHue      = pct;
                case O_OPACITY   -> settings.outlineOpacity  = Math.round(pct * 100f) / 100f;
                case O_WIDTH     -> settings.outlineWidth    = Math.round((0.5f + pct * 4.5f) * 10f) / 10f;
            }
        } else {
            switch (row) {
                case F_RGB_SPEED -> settings.fillRgbSpeed = Math.round((0.1f + pct * 4.9f) * 10f) / 10f;
                case F_COLOR     -> settings.fillHue      = pct;
                case F_OPACITY   -> settings.fillOpacity  = Math.round(pct * 100f) / 100f;
            }
        }
    }

    private boolean inBounds(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
