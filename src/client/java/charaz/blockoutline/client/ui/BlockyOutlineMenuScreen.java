package charaz.blockoutline.client.ui;

import charaz.blockoutline.config.BlockyOutlineSettings;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public class BlockyOutlineMenuScreen extends Screen {
    private static final String[] TAB_LABELS = {"Outline", "Fill", "Presets", "About"};
    private static final String[] OUTLINE_ROW_LABELS = {"Rainbow outline", "RGB speed", "Colors", "Opacity", "Width", "Smooth movement"};
    private static final String[] FILL_ROW_LABELS = {"Enable fill", "Rainbow fill", "RGB speed", "Colors", "Opacity"};

    private static final String[] PRESET_NAMES = {
            "Minimalist silver", "Executive purple", "Vibrant gold", "Rainbow corporate", "Dark slate"
    };
    private static final String[] PRESET_DESCS = {
            "Sleek silver outline with a faint translucent silver fill.",
            "Deep executive amethyst purple outline with dark slate fill.",
            "Elegant bronze gold outline with champagne gold fill.",
            "Dynamic colorful rainbow cycle with corporate styling.",
            "Subtle dark gray outline with ultra-faint carbon overlay."
    };

    private static final int COLOR_BG_OVERLAY = 0xF50D0C16;
    private static final int COLOR_BG_PANEL = 0xFA141422;
    private static final int COLOR_BG_HEADER = 0xFF1B1B2C;
    private static final int COLOR_BG_CARD = 0xFF1F1F32;
    private static final int COLOR_BG_CARD_HOVER = 0xFF2A2A42;

    private static final int COLOR_PURPLE_PRIMARY = 0xFF9333EA;
    private static final int COLOR_PURPLE_LIGHT = 0xFFA855F7;
    private static final int COLOR_PURPLE_LILAC = 0xFFC084FC;
    private static final int COLOR_GREEN_NEON = 0xFF10B981;
    private static final int COLOR_RED_OFF = 0xFFEF4444;

    private static final int COLOR_TEXT_WHITE = 0xFFF8FAFC;
    private static final int COLOR_TEXT_GRAY = 0xFFCBD5E1;
    private static final int COLOR_TEXT_MUTED = 0xFF94A3B8;

    private static final int COLOR_BORDER_SUBTLE = 0x309333EA;
    private static final int COLOR_BORDER_PURPLE = 0xFF9333EA;
    private static final int COLOR_BORDER_GRAY = 0xFF334155;

    private int panelW;
    private int panelH;
    private int px;
    private int py;
    private int topPad;
    private int rowH;
    private int rowGap;
    private int contentX;
    private int contentW;
    private int sliderW;

    private int activeTab = 0;
    private boolean outlineColorExpanded = false;
    private boolean fillColorExpanded = false;
    private boolean isDraggingSlider = false;
    private int dragCol = -1;
    private int dragRow = -1;

    private boolean isDragging2DPicker = false;
    private boolean isDraggingHueSlider = false;
    private int active2DCol = -1;
    private int active2DRow = -1;

    private int focusedHexCol = -1;
    private String typingHex = "";
    private String hoveredTooltipText;

    private final BlockyOutlineSettings settings = BlockyOutlineSettings.get();

    public BlockyOutlineMenuScreen() {
        super(Component.literal("Block outline customizer"));
    }

    private void playClickSound() {
        try {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
            );
        } catch (Throwable ignored) {}
    }

    private void updateLayout() {
        this.panelW = Math.min(this.width - 16, 560);
        this.panelH = Math.min(this.height - 16, 320);
        this.px = (this.width - this.panelW) / 2;
        this.py = (this.height - this.panelH) / 2;

        this.topPad = 42;
        this.rowH = 22;
        this.rowGap = 4;
        this.contentX = this.px + 12;
        this.contentW = this.panelW - 24;
        this.sliderW = 120;
    }

    @Override
    protected void init() {
        super.init();
        this.updateLayout();

        int btnW = 90;
        int btnH = 20;
        int btnX = this.px + this.panelW - btnW - 12;
        int btnY = this.py + this.panelH - btnH - 8;

        this.addRenderableWidget(new Button(
                btnX, btnY, btnW, btnH,
                Component.literal("Done"),
                button -> this.onClose(),
                supplier -> (MutableComponent) supplier.get()
        ) {
            @Override
            public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
                boolean hovered = this.isHoveredOrFocused();
                int bx = this.getX();
                int by = this.getY();
                int bw = this.getWidth();
                int bh = this.getHeight();

                int topGradient = hovered ? COLOR_PURPLE_LIGHT : COLOR_PURPLE_PRIMARY;
                int botGradient = hovered ? COLOR_PURPLE_PRIMARY : 0xFF7E22CE;
                guiGraphics.fillGradient(bx, by, bx + bw, by + bh, topGradient, botGradient);

                int border = hovered ? COLOR_TEXT_WHITE : COLOR_PURPLE_LILAC;
                guiGraphics.fill(bx, by, bx + bw, by + 1, border);
                guiGraphics.fill(bx, by + bh - 1, bx + bw, by + bh, border);
                guiGraphics.fill(bx, by, bx + 1, by + bh, border);
                guiGraphics.fill(bx + bw - 1, by, bx + bw, by + bh, border);

                guiGraphics.centeredText(
                        BlockyOutlineMenuScreen.this.font,
                        "Done",
                        bx + bw / 2,
                        by + (bh - 8) / 2,
                        COLOR_TEXT_WHITE
                );
            }
        });
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(0, 0, this.width, this.height, COLOR_BG_OVERLAY);
        this.updateLayout();
        this.hoveredTooltipText = null;

        guiGraphics.fillGradient(this.px, this.py, this.px + this.panelW, this.py + this.panelH, 0xFD12121E, COLOR_BG_PANEL);

        guiGraphics.fill(this.px - 1, this.py - 1, this.px + this.panelW + 1, this.py, COLOR_BORDER_SUBTLE);
        guiGraphics.fill(this.px - 1, this.py + this.panelH, this.px + this.panelW + 1, this.py + this.panelH + 1, COLOR_BORDER_SUBTLE);

        guiGraphics.fill(this.px, this.py, this.px + this.panelW, this.py + 1, COLOR_BORDER_PURPLE);
        guiGraphics.fill(this.px, this.py + this.panelH - 1, this.px + this.panelW, this.py + this.panelH, COLOR_BORDER_PURPLE);
        guiGraphics.fill(this.px, this.py, this.px + 1, this.py + this.panelH, COLOR_BORDER_PURPLE);
        guiGraphics.fill(this.px + this.panelW - 1, this.py, this.px + this.panelW, this.py + this.panelH, COLOR_BORDER_PURPLE);

        guiGraphics.fillGradient(this.px + 1, this.py + 1, this.px + this.panelW - 1, this.py + 36, COLOR_BG_HEADER, 0xFF161625);
        guiGraphics.fillGradient(this.px + 1, this.py + 36, this.px + this.panelW - 1, this.py + 37, COLOR_PURPLE_PRIMARY, COLOR_PURPLE_LIGHT);

        int logoX = this.px + 14;
        int logoY = this.py + 10;
        guiGraphics.text(this.font, "Blocky", logoX, logoY + 4, COLOR_TEXT_WHITE, true);
        guiGraphics.text(this.font, "Outline", logoX + this.font.width("Blocky") + 4, logoY + 4, COLOR_PURPLE_LIGHT, false);

        String vTag = "v1.1.0";
        int vW = this.font.width(vTag);
        int vX = this.px + this.panelW - vW - 12;
        guiGraphics.fillGradient(vX - 4, this.py + 9, vX + vW + 4, this.py + 24, COLOR_PURPLE_PRIMARY, 0xFF7E22CE);
        guiGraphics.text(this.font, vTag, vX, this.py + 12, COLOR_TEXT_WHITE, false);

        int logoWidthArea = 14 + this.font.width("Blocky Outline") + 16;
        int tabStartX = this.px + logoWidthArea;
        int tabAvailableW = vX - 8 - tabStartX;
        int gap = 3;
        int tabW = Math.max(50, (tabAvailableW - gap * 3) / 4);
        int tabH = 21;
        int tabY = this.py + 8;

        for (int i = 0; i < TAB_LABELS.length; ++i) {
            this.renderHeaderTab(guiGraphics, tabStartX + i * (tabW + gap), tabY, tabW, tabH, i, mouseX, mouseY);
        }

        int contentY = this.py + this.topPad;
        if (this.activeTab == 0 || this.activeTab == 1) {
            int col = this.activeTab;
            int currentY = contentY;
            int numRows = (col == 0) ? 6 : 5;
            for (int row = 0; row < numRows; ++row) {
                int height = this.getRowHeight(col, row);
                this.renderRow(guiGraphics, this.contentX, currentY, col, row, height, mouseX, mouseY);
                currentY += height + this.rowGap;
            }

            this.renderLive3DBlockCanvas(guiGraphics, this.contentX + this.contentW - 175, contentY, 175, this.panelH - this.topPad - 32);
        } else if (this.activeTab == 2) {
            for (int i = 0; i < PRESET_NAMES.length; ++i) {
                this.renderPresetCard(guiGraphics, this.contentX, contentY + i * 34, i, mouseX, mouseY);
            }
        } else if (this.activeTab == 3) {
            this.renderAboutPanel(guiGraphics, this.contentX, contentY);
        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);

        if (this.hoveredTooltipText != null) {
            int textW = this.font.width(this.hoveredTooltipText);
            int tx = mouseX + 10;
            int ty = mouseY - 14;
            guiGraphics.fill(tx - 6, ty - 5, tx + textW + 6, ty + 13, 0xF0141422);
            guiGraphics.fill(tx - 6, ty - 5, tx + textW + 6, ty - 4, COLOR_PURPLE_PRIMARY);
            guiGraphics.fill(tx - 6, ty + 12, tx + textW + 6, ty + 13, COLOR_PURPLE_PRIMARY);
            guiGraphics.fill(tx - 6, ty - 4, tx - 5, ty + 12, COLOR_PURPLE_PRIMARY);
            guiGraphics.fill(tx + textW + 5, ty - 4, tx + textW + 6, ty + 12, COLOR_PURPLE_PRIMARY);
            guiGraphics.text(this.font, this.hoveredTooltipText, tx, ty, COLOR_TEXT_WHITE, false);
        }
    }

    private void renderHeaderTab(GuiGraphicsExtractor guiGraphics, int tx, int ty, int tw, int th, int index, int mx, int my) {
        boolean selected = (this.activeTab == index);
        boolean hovered = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th;

        int bg = selected ? COLOR_BG_CARD : (hovered ? COLOR_BG_CARD_HOVER : 0xFF141422);
        guiGraphics.fill(tx, ty, tx + tw, ty + th, bg);

        int border = selected ? COLOR_PURPLE_LIGHT : (hovered ? COLOR_PURPLE_LILAC : COLOR_BORDER_GRAY);
        guiGraphics.fill(tx, ty, tx + tw, ty + 1, border);
        guiGraphics.fill(tx, ty + th - 1, tx + tw, ty + th, border);
        guiGraphics.fill(tx, ty, tx + 1, ty + th, border);
        guiGraphics.fill(tx + tw - 1, ty, tx + tw, ty + th, border);

        int textCol = selected ? COLOR_TEXT_WHITE : (hovered ? COLOR_TEXT_GRAY : COLOR_TEXT_MUTED);
        guiGraphics.centeredText(this.font, TAB_LABELS[index], tx + tw / 2, ty + (th - 8) / 2, textCol);
    }

    private int getRowHeight(int col, int row) {
        if ((col == 0 && row == 2 && this.outlineColorExpanded) || (col == 1 && row == 3 && this.fillColorExpanded)) {
            return 80;
        }
        return this.rowH;
    }

    private void renderRow(GuiGraphicsExtractor guiGraphics, int rx, int ry, int col, int row, int height, int mx, int my) {
        boolean disabled = this.isRowDisabled(col, row);
        int settingsW = this.contentW - 185;
        boolean hovered = !disabled && mx >= rx && mx <= rx + settingsW && my >= ry && my <= ry + height;

        int bg = disabled ? 0xFF0F0F1B : (hovered ? COLOR_BG_CARD_HOVER : COLOR_BG_CARD);
        guiGraphics.fill(rx, ry, rx + settingsW, ry + height, bg);

        int borderCol = disabled ? 0x15FFFFFF : (hovered ? COLOR_BORDER_PURPLE : COLOR_BORDER_GRAY);
        guiGraphics.fill(rx, ry, rx + settingsW, ry + 1, borderCol);
        guiGraphics.fill(rx, ry + height - 1, rx + settingsW, ry + height, borderCol);
        guiGraphics.fill(rx, ry, rx + 1, ry + height, borderCol);
        guiGraphics.fill(rx + settingsW - 1, ry, rx + settingsW, ry + height, borderCol);

        String label = (col == 0) ? OUTLINE_ROW_LABELS[row] : FILL_ROW_LABELS[row];
        int labelColor = disabled ? 0xFF64748B : COLOR_TEXT_WHITE;
        guiGraphics.text(this.font, label, rx + 12, ry + (this.rowH - 8) / 2, labelColor, false);

        if (this.isCheckboxRow(col, row)) {
            this.renderToggleSwitch(guiGraphics, rx, ry, col, row, disabled, settingsW);
        } else if ((col == 0 && row == 2) || (col == 1 && row == 3)) {
            boolean expanded = (col == 0) ? this.outlineColorExpanded : this.fillColorExpanded;
            if (expanded) {
                this.renderColorPicker2D(guiGraphics, rx, ry, col, row, height, disabled, mx, my, settingsW);
            } else {
                this.renderColorPickerCollapsed(guiGraphics, rx, ry, col, row, height, disabled, mx, my, settingsW);
            }
        } else {
            this.renderSlider(guiGraphics, rx, ry, col, row, disabled, settingsW);
        }
    }

    private void renderToggleSwitch(GuiGraphicsExtractor guiGraphics, int rx, int ry, int col, int row, boolean disabled, int containerW) {
        boolean checked = this.getCheckboxValue(col, row);
        int switchW = 32;
        int switchH = 15;
        int switchX = rx + containerW - switchW - 12;
        int switchY = ry + (this.rowH - switchH) / 2;

        int trackBg = disabled ? 0xFF1E293B : (checked ? COLOR_PURPLE_PRIMARY : 0xFF2D2D44);
        guiGraphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, trackBg);

        int borderCol = disabled ? 0xFF475569 : (checked ? COLOR_PURPLE_LIGHT : 0xFF475569);
        guiGraphics.fill(switchX, switchY, switchX + switchW, switchY + 1, borderCol);
        guiGraphics.fill(switchX, switchY + switchH - 1, switchX + switchW, switchY + switchH, borderCol);
        guiGraphics.fill(switchX, switchY, switchX + 1, switchY + switchH, borderCol);
        guiGraphics.fill(switchX + switchW - 1, switchY, switchX + switchW, switchY + switchH, borderCol);

        int thumbW = 11;
        int thumbH = 11;
        int thumbX = checked ? switchX + switchW - thumbW - 2 : switchX + 2;
        int thumbY = switchY + 2;
        int thumbColor = disabled ? 0xFF64748B : COLOR_TEXT_WHITE;
        guiGraphics.fill(thumbX, thumbY, thumbX + thumbW, thumbY + thumbH, thumbColor);

        String stateText = checked ? "● on" : "○ off";
        int stateColor = disabled ? 0xFF64748B : (checked ? COLOR_GREEN_NEON : COLOR_RED_OFF);
        guiGraphics.text(this.font, stateText, switchX - this.font.width(stateText) - 6, ry + (this.rowH - 8) / 2, stateColor, false);
    }

    private void renderSlider(GuiGraphicsExtractor guiGraphics, int rx, int ry, int col, int row, boolean disabled, int containerW) {
        float pct = this.getSliderPct(col, row);
        String val = this.getSliderValueStr(col, row);

        int sliderX = rx + containerW - this.sliderW - 12;
        int sliderY = ry + (this.rowH - 4) / 2;
        int filled = (int)((float)this.sliderW * pct);

        boolean isHueSlider = (col == 0 && row == 2) || (col == 1 && row == 3);

        if (isHueSlider) {
            for (int i = 0; i < this.sliderW; ++i) {
                float hue = (float)i / (float)this.sliderW;
                float[] rgb = disabled ? new float[]{0.3f, 0.3f, 0.3f} : BlockyOutlineSettings.hsvToRgb(hue);
                int color = 0xFF000000 | ((int)(rgb[0] * 255.0f) << 16) | ((int)(rgb[1] * 255.0f) << 8) | (int)(rgb[2] * 255.0f);
                guiGraphics.fill(sliderX + i, sliderY - 1, sliderX + i + 1, sliderY + 5, color);
            }
        } else {
            int trackBg = 0xFF141422;
            guiGraphics.fill(sliderX, sliderY, sliderX + this.sliderW, sliderY + 4, trackBg);
            if (!disabled) {
                guiGraphics.fillGradient(sliderX, sliderY, sliderX + filled, sliderY + 4, COLOR_PURPLE_PRIMARY, COLOR_PURPLE_LIGHT);
            } else {
                guiGraphics.fill(sliderX, sliderY, sliderX + filled, sliderY + 4, 0xFF475569);
            }
        }

        int thumbColor = disabled ? 0xFF64748B : COLOR_PURPLE_PRIMARY;
        if (isHueSlider && !disabled) {
            float[] rgb = BlockyOutlineSettings.hsvToRgb(pct);
            thumbColor = 0xFF000000 | ((int)(rgb[0] * 255.0f) << 16) | ((int)(rgb[1] * 255.0f) << 8) | (int)(rgb[2] * 255.0f);
        }

        guiGraphics.fill(sliderX + filled - 3, sliderY - 3, sliderX + filled + 3, sliderY + 7, COLOR_TEXT_WHITE);
        guiGraphics.fill(sliderX + filled - 2, sliderY - 2, sliderX + filled + 2, sliderY + 6, thumbColor);

        if (!val.isEmpty()) {
            int valColor = disabled ? 0xFF64748B : COLOR_TEXT_WHITE;
            guiGraphics.text(this.font, val, sliderX - this.font.width(val) - 6, ry + (this.rowH - 8) / 2, valColor, false);
        }
    }

    private void renderLive3DBlockCanvas(GuiGraphicsExtractor guiGraphics, int cx, int cy, int cw, int ch) {
        guiGraphics.fill(cx, cy, cx + cw, cy + ch, COLOR_BG_CARD);
        guiGraphics.fill(cx, cy, cx + cw, cy + 1, COLOR_BORDER_PURPLE);
        guiGraphics.fill(cx, cy + ch - 1, cx + cw, cy + ch, COLOR_BORDER_PURPLE);
        guiGraphics.fill(cx, cy, cx + 1, cy + ch, COLOR_BORDER_PURPLE);
        guiGraphics.fill(cx + cw - 1, cy, cx + cw, cy + ch, COLOR_BORDER_PURPLE);

        guiGraphics.fillGradient(cx + 1, cy + 1, cx + cw - 1, cy + 20, 0xFF1B1B2C, 0xFF161625);
        guiGraphics.fill(cx + 1, cy + 20, cx + cw - 1, cy + 21, COLOR_PURPLE_PRIMARY);
        guiGraphics.centeredText(this.font, "Live preview", cx + cw / 2, cy + 6, COLOR_TEXT_GRAY);

        long now = System.currentTimeMillis();
        int outlineColor = this.settings.getOutlineArgb(now);
        int fillColor = this.settings.getFillArgb(now);

        int centerX = cx + cw / 2;
        int centerY = cy + (ch - 40) / 2 + 10;
        int size = 26;

        int vTopX = centerX;
        int vTopY = centerY - size;
        int vRightX = centerX + size + 6;
        int vRightY = centerY - size / 2;
        int vMidX = centerX;
        int vMidY = centerY + 2;
        int vLeftX = centerX - size - 6;
        int vLeftY = centerY - size / 2;

        int vBotLeftX = vLeftX;
        int vBotLeftY = vLeftY + size + 4;
        int vBotX = vMidX;
        int vBotY = vMidY + size + 4;
        int vBotRightX = vRightX;
        int vBotRightY = vRightY + size + 4;

        int lw = Math.max(1, (int)this.settings.outlineWidth);

        if (this.settings.fillEnabled) {
            this.drawQuadScanlines(guiGraphics, vTopX, vTopY, vRightX, vRightY, vMidX, vMidY, vLeftX, vLeftY, fillColor);
            this.drawQuadScanlines(guiGraphics, vLeftX, vLeftY, vMidX, vMidY, vBotX, vBotY, vBotLeftX, vBotLeftY, fillColor);
            this.drawQuadScanlines(guiGraphics, vMidX, vMidY, vRightX, vRightY, vBotRightX, vBotRightY, vBotX, vBotY, fillColor);
        }

        this.drawLine(guiGraphics, vTopX, vTopY, vRightX, vRightY, outlineColor, lw);
        this.drawLine(guiGraphics, vRightX, vRightY, vMidX, vMidY, outlineColor, lw);
        this.drawLine(guiGraphics, vMidX, vMidY, vLeftX, vLeftY, outlineColor, lw);
        this.drawLine(guiGraphics, vLeftX, vLeftY, vTopX, vTopY, outlineColor, lw);

        this.drawLine(guiGraphics, vLeftX, vLeftY, vBotLeftX, vBotLeftY, outlineColor, lw);
        this.drawLine(guiGraphics, vMidX, vMidY, vBotX, vBotY, outlineColor, lw);
        this.drawLine(guiGraphics, vRightX, vRightY, vBotRightX, vBotRightY, outlineColor, lw);

        this.drawLine(guiGraphics, vBotLeftX, vBotLeftY, vBotX, vBotY, outlineColor, lw);
        this.drawLine(guiGraphics, vBotX, vBotY, vBotRightX, vBotRightY, outlineColor, lw);

        guiGraphics.fill(centerX - 28, vBotY + 8, centerX + 28, vBotY + 11, 0x33000000);

        int statsY = cy + ch - 48;
        guiGraphics.fill(cx + 8, statsY, cx + cw - 8, cy + ch - 8, 0xFF1B1B2C);
        guiGraphics.fill(cx + 8, statsY, cx + cw - 8, statsY + 1, COLOR_PURPLE_PRIMARY);

        guiGraphics.text(this.font, "Width: " + String.format("%.1fpx", this.settings.outlineWidth), cx + 14, statsY + 6, COLOR_TEXT_WHITE, false);
        guiGraphics.text(this.font, "Alpha: " + String.format("%d%%", (int)(this.settings.outlineOpacity * 100)), cx + 14, statsY + 18, COLOR_TEXT_GRAY, false);
        guiGraphics.text(this.font, "Rainbow: " + (this.settings.rainbowOutline ? "Active" : "Off"), cx + 14, statsY + 30, this.settings.rainbowOutline ? COLOR_GREEN_NEON : COLOR_TEXT_MUTED, false);
    }

    private void drawLine(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color, int thickness) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);

        if (steps == 0) {
            g.fill(x1, y1, x1 + thickness, y1 + thickness, color);
            return;
        }

        float xInc = (float)(x2 - x1) / (float)steps;
        float yInc = (float)(y2 - y1) / (float)steps;
        float x = (float)x1;
        float y = (float)y1;

        for (int i = 0; i <= steps; ++i) {
            int ix = Math.round(x);
            int iy = Math.round(y);
            g.fill(ix, iy, ix + thickness, iy + thickness, color);
            x += xInc;
            y += yInc;
        }
    }

    private void drawQuadScanlines(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, int color) {
        int minY = Math.min(Math.min(y1, y2), Math.min(y3, y4));
        int maxY = Math.max(Math.max(y1, y2), Math.max(y3, y4));
        for (int y = minY; y <= maxY; y += 2) {
            int leftX = 9999;
            int rightX = -9999;

            if (isYBetween(y, y1, y2)) leftX = Math.min(leftX, interpolateX(y, x1, y1, x2, y2));
            if (isYBetween(y, y1, y2)) rightX = Math.max(rightX, interpolateX(y, x1, y1, x2, y2));
            if (isYBetween(y, y2, y3)) leftX = Math.min(leftX, interpolateX(y, x2, y2, x3, y3));
            if (isYBetween(y, y2, y3)) rightX = Math.max(rightX, interpolateX(y, x2, y2, x3, y3));
            if (isYBetween(y, y3, y4)) leftX = Math.min(leftX, interpolateX(y, x3, y3, x4, y4));
            if (isYBetween(y, y3, y4)) rightX = Math.max(rightX, interpolateX(y, x3, y3, x4, y4));
            if (isYBetween(y, y4, y1)) leftX = Math.min(leftX, interpolateX(y, x4, y4, x1, y1));
            if (isYBetween(y, y4, y1)) rightX = Math.max(rightX, interpolateX(y, x4, y4, x1, y1));

            if (leftX <= rightX && leftX != 9999) {
                g.fill(leftX, y, rightX + 1, y + 2, color);
            }
        }
    }

    private static boolean isYBetween(int y, int yA, int yB) {
        return (y >= Math.min(yA, yB) && y <= Math.max(yA, yB)) && yA != yB;
    }

    private static int interpolateX(int y, int xA, int yA, int xB, int yB) {
        if (yA == yB) return xA;
        return xA + (y - yA) * (xB - xA) / (yB - yA);
    }

    private void renderPresetCard(GuiGraphicsExtractor guiGraphics, int pxX, int pxY, int index, int mx, int my) {
        boolean hovered = mx >= pxX && mx <= pxX + this.contentW && my >= pxY && my <= pxY + 28;
        int bg = hovered ? COLOR_BG_CARD_HOVER : COLOR_BG_CARD;
        int borderCol = hovered ? COLOR_BORDER_PURPLE : COLOR_BORDER_GRAY;

        guiGraphics.fill(pxX, pxY, pxX + this.contentW, pxY + 28, bg);
        guiGraphics.fill(pxX, pxY, pxX + this.contentW, pxY + 1, borderCol);
        guiGraphics.fill(pxX, pxY + 27, pxX + this.contentW, pxY + 28, borderCol);
        guiGraphics.fill(pxX, pxY, pxX + 1, pxY + 28, borderCol);
        guiGraphics.fill(pxX + this.contentW - 1, pxY, pxX + this.contentW, pxY + 28, borderCol);

        int themeCol = switch (index) {
            case 0 -> 0xFFCBD5E1;
            case 1 -> 0xFFA855F7;
            case 2 -> 0xFFF59E0B;
            case 3 -> COLOR_PURPLE_LIGHT;
            default -> 0xFF475569;
        };
        guiGraphics.fill(pxX + 1, pxY + 1, pxX + 5, pxY + 27, themeCol);

        int textCol = hovered ? COLOR_PURPLE_LIGHT : COLOR_TEXT_WHITE;
        guiGraphics.text(this.font, PRESET_NAMES[index], pxX + 14, pxY + 4, textCol, false);
        guiGraphics.text(this.font, PRESET_DESCS[index], pxX + 14, pxY + 15, COLOR_TEXT_MUTED, false);

        int btnW = 68;
        int btnH = 16;
        int btnX = pxX + this.contentW - btnW - 6;
        int btnY = pxY + 6;
        boolean btnHovered = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;

        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHovered ? COLOR_PURPLE_LIGHT : COLOR_PURPLE_PRIMARY);
        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + 1, COLOR_BORDER_PURPLE);
        guiGraphics.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, COLOR_BORDER_PURPLE);
        guiGraphics.fill(btnX, btnY, btnX + 1, btnY + btnH, COLOR_BORDER_PURPLE);
        guiGraphics.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, COLOR_BORDER_PURPLE);

        guiGraphics.centeredText(this.font, "Apply", btnX + btnW / 2, btnY + 4, COLOR_TEXT_WHITE);
    }

    private void renderAboutPanel(GuiGraphicsExtractor guiGraphics, int ax, int ay) {
        int cardW = this.contentW;
        int cardH = 185;

        guiGraphics.fill(ax, ay, ax + cardW, ay + cardH, COLOR_BG_CARD);
        guiGraphics.fill(ax, ay, ax + cardW, ay + 1, COLOR_BORDER_PURPLE);
        guiGraphics.fill(ax, ay + cardH - 1, ax + cardW, ay + cardH, COLOR_BORDER_PURPLE);
        guiGraphics.fill(ax, ay, ax + 1, ay + cardH, COLOR_BORDER_PURPLE);
        guiGraphics.fill(ax + cardW - 1, ay, ax + cardW, ay + cardH, COLOR_BORDER_PURPLE);

        guiGraphics.fillGradient(ax + 1, ay + 1, ax + cardW - 1, ay + 42, 0xFF1B1B2C, 0xFF161625);
        guiGraphics.fillGradient(ax + 1, ay + 42, ax + cardW - 1, ay + 43, COLOR_PURPLE_PRIMARY, COLOR_PURPLE_LIGHT);

        guiGraphics.text(this.font, "Blocky", ax + 16, ay + 12, COLOR_TEXT_WHITE, true);
        guiGraphics.text(this.font, "Outline", ax + 16 + this.font.width("Blocky") + 4, ay + 12, COLOR_PURPLE_LIGHT, false);
        guiGraphics.text(this.font, "Next-generation block outline & fill customizer", ax + 16, ay + 26, COLOR_TEXT_MUTED, false);

        String vStr = "v1.1.0";
        String envStr = "Fabric 26.2";
        int vW = this.font.width(vStr);
        int envW = this.font.width(envStr);
        int badge2X = ax + cardW - envW - 16;
        int badge1X = badge2X - vW - 16;

        guiGraphics.fill(badge1X - 5, ay + 12, badge1X + vW + 5, ay + 27, COLOR_PURPLE_PRIMARY);
        guiGraphics.text(this.font, vStr, badge1X, ay + 15, COLOR_TEXT_WHITE, false);

        guiGraphics.fill(badge2X - 5, ay + 12, badge2X + envW + 5, ay + 27, 0xFF2D2D44);
        guiGraphics.text(this.font, envStr, badge2X, ay + 15, COLOR_GREEN_NEON, false);

        int kbY = ay + 50;
        guiGraphics.fill(ax + 14, kbY, ax + cardW - 14, kbY + 28, 0xFF1B1B2C);
        guiGraphics.fill(ax + 14, kbY, ax + 18, kbY + 28, COLOR_PURPLE_PRIMARY);
        guiGraphics.text(this.font, "Keybind Shortcut:", ax + 26, kbY + 5, COLOR_PURPLE_LIGHT, false);
        guiGraphics.text(this.font, "Press [ M ] anywhere in-game to toggle configuration menu", ax + 26, kbY + 16, COLOR_TEXT_WHITE, false);

        int infoY = ay + 84;
        int infoW = (cardW - 36) / 2;

        guiGraphics.fill(ax + 14, infoY, ax + 14 + infoW, infoY + 38, 0xFF181828);
        guiGraphics.fill(ax + 14, infoY, ax + 14 + infoW, infoY + 1, COLOR_BORDER_GRAY);
        guiGraphics.text(this.font, "Author & Developer", ax + 22, infoY + 7, COLOR_TEXT_MUTED, false);
        guiGraphics.text(this.font, "CharaZ", ax + 22, infoY + 20, COLOR_TEXT_WHITE, true);

        guiGraphics.fill(ax + 22 + infoW, infoY, ax + 22 + infoW * 2, infoY + 38, 0xFF181828);
        guiGraphics.fill(ax + 22 + infoW, infoY, ax + 22 + infoW * 2, infoY + 1, COLOR_BORDER_GRAY);
        guiGraphics.text(this.font, "Render Engine", ax + 30 + infoW, infoY + 7, COLOR_TEXT_MUTED, false);
        guiGraphics.text(this.font, "Zero-GC Executive Engine", ax + 30 + infoW, infoY + 20, COLOR_GREEN_NEON, false);

        int repoY = ay + 128;
        guiGraphics.fill(ax + 14, repoY, ax + cardW - 14, repoY + 44, 0xFF161624);
        guiGraphics.fill(ax + 14, repoY, ax + cardW - 14, repoY + 1, COLOR_BORDER_GRAY);

        guiGraphics.text(this.font, "GitHub Repository:", ax + 24, repoY + 8, COLOR_PURPLE_LILAC, false);
        guiGraphics.text(this.font, "github.com/EliteClockman-ctrl/blocky-outline", ax + 24, repoY + 20, COLOR_TEXT_GRAY, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        int logoWidthArea = 14 + this.font.width("Blocky Outline") + 16;
        int tabStartX = this.px + logoWidthArea;
        String vTag = "v1.1.0";
        int vW = this.font.width(vTag);
        int vX = this.px + this.panelW - vW - 12;
        int tabAvailableW = vX - 8 - tabStartX;
        int gap = 3;
        int tabW = Math.max(50, (tabAvailableW - gap * 3) / 4);
        int tabH = 21;
        int tabY = this.py + 8;

        for (int i = 0; i < TAB_LABELS.length; ++i) {
            int tx = tabStartX + i * (tabW + gap);
            if (mx >= tx && mx <= tx + tabW && my >= tabY && my <= tabY + tabH) {
                this.activeTab = i;
                this.playClickSound();
                return true;
            }
        }

        int contentY = this.py + this.topPad;
        if (this.activeTab == 0 || this.activeTab == 1) {
            int col = this.activeTab;
            int settingsW = this.contentW - 185;
            int currentY = contentY;
            int numRows = (col == 0) ? 6 : 5;

            for (int row = 0; row < numRows; ++row) {
                int height = this.getRowHeight(col, row);
                boolean disabled = this.isRowDisabled(col, row);

                if (!disabled && mx >= this.contentX && mx <= this.contentX + settingsW && my >= currentY && my <= currentY + height) {
                    if (this.isCheckboxRow(col, row)) {
                        int switchW = 32;
                        int switchH = 15;
                        int switchX = this.contentX + settingsW - switchW - 12;
                        int switchY = currentY + (this.rowH - switchH) / 2;

                        if (mx >= switchX - 6 && mx <= switchX + switchW + 6 && my >= switchY - 4 && my <= switchY + switchH + 4) {
                            this.toggleCheckbox(col, row);
                            this.playClickSound();
                            return true;
                        }
                    } else if ((col == 0 && row == 2) || (col == 1 && row == 3)) {
                        boolean expanded = (col == 0) ? this.outlineColorExpanded : this.fillColorExpanded;
                        if (expanded) {
                            int boxX = this.contentX + settingsW - this.sliderW - 12;
                            int boxY = currentY + 4;
                            int boxW = this.sliderW;
                            int boxH = 48;

                            if (mx >= boxX && mx <= boxX + boxW && my >= boxY && my <= boxY + boxH) {
                                this.isDragging2DPicker = true;
                                this.active2DCol = col;
                                this.active2DRow = row;
                                this.update2DPicker(col, mx, my, boxX, boxY, boxW, boxH);
                                return true;
                            }

                            int sliderX = boxX;
                            int sliderY = boxY + boxH + 8;
                            int sliderW_local = boxW;
                            int sliderH = 6;

                            if (mx >= sliderX && mx <= sliderX + sliderW_local && my >= sliderY - 3 && my <= sliderY + sliderH + 3) {
                                this.isDraggingHueSlider = true;
                                this.active2DCol = col;
                                this.active2DRow = row;
                                this.updateHueSlider(col, mx, sliderX, sliderW_local);
                                return true;
                            }

                            int previewW = 34;
                            int hexBoxW = 48;
                            int hexBoxH = 12;
                            int hexBoxX = boxX + (previewW - hexBoxW) / 2;
                            int hexBoxY = sliderY + (sliderH - hexBoxH) / 2;

                            if (mx >= hexBoxX && mx <= hexBoxX + hexBoxW && my >= hexBoxY && my <= hexBoxY + hexBoxH) {
                                this.focusedHexCol = col;
                                this.typingHex = "";
                                return true;
                            }

                            int btnW = 16;
                            int btnH = 12;
                            int previewX = boxX - previewW - 10;
                            int btnX = previewX - btnW - 6;
                            int btnY = currentY + 4;

                            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                                if (col == 0) this.outlineColorExpanded = false;
                                else this.fillColorExpanded = false;
                                this.playClickSound();
                                return true;
                            }
                        } else {
                            int btnW = 16;
                            int btnH = 12;
                            int previewW = 34;
                            int btnX = this.contentX + settingsW - this.sliderW - 12 - previewW - 10 - btnW - 6;
                            int btnY = currentY + (this.rowH - btnH) / 2;

                            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                                if (col == 0) this.outlineColorExpanded = true;
                                else this.fillColorExpanded = true;
                                this.playClickSound();
                                return true;
                            }

                            int sliderX = this.contentX + settingsW - this.sliderW - 12;
                            if (mx >= sliderX - 4 && mx <= sliderX + this.sliderW + 4) {
                                this.isDraggingSlider = true;
                                this.dragCol = col;
                                this.dragRow = row;
                                this.updateSliderValue(col, row, mx, sliderX);
                                return true;
                            }
                        }
                    } else {
                        int sliderX = this.contentX + settingsW - this.sliderW - 12;
                        if (mx >= sliderX - 4 && mx <= sliderX + this.sliderW + 4) {
                            this.isDraggingSlider = true;
                            this.dragCol = col;
                            this.dragRow = row;
                            this.updateSliderValue(col, row, mx, sliderX);
                            return true;
                        }
                    }
                }
                currentY += height + this.rowGap;
            }
        } else if (this.activeTab == 2) {
            for (int i = 0; i < PRESET_NAMES.length; ++i) {
                int pxY = contentY + i * 34;
                int btnW = 68;
                int btnH = 16;
                int btnX = this.contentX + this.contentW - btnW - 6;
                int btnY = pxY + 6;

                if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                    this.applyPreset(i);
                    this.playClickSound();
                    return true;
                }
            }
        }

        this.focusedHexCol = -1;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.isDraggingSlider || this.isDragging2DPicker || this.isDraggingHueSlider) {
            this.isDraggingSlider = false;
            this.isDragging2DPicker = false;
            this.isDraggingHueSlider = false;
            this.dragCol = -1;
            this.dragRow = -1;
            this.active2DCol = -1;
            this.active2DRow = -1;
            BlockyOutlineSettings.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mx = event.x();
        double my = event.y();

        if (this.isDraggingSlider && this.dragCol != -1 && this.dragRow != -1) {
            int settingsW = this.contentW - 185;
            int sliderX = this.contentX + settingsW - this.sliderW - 12;
            this.updateSliderValue(this.dragCol, this.dragRow, mx, sliderX);
            return true;
        }

        if (this.isDragging2DPicker && this.active2DCol != -1) {
            int settingsW = this.contentW - 185;
            int boxX = this.contentX + settingsW - this.sliderW - 12;
            int contentY = this.py + this.topPad;
            int currentY = contentY;
            int numRows = (this.active2DCol == 0) ? 6 : 5;

            for (int r = 0; r < numRows; ++r) {
                if (r == this.active2DRow) break;
                currentY += this.getRowHeight(this.active2DCol, r) + this.rowGap;
            }

            int boxY = currentY + 4;
            int boxW = this.sliderW;
            int boxH = 48;
            this.update2DPicker(this.active2DCol, mx, my, boxX, boxY, boxW, boxH);
            return true;
        }

        if (this.isDraggingHueSlider && this.active2DCol != -1) {
            int settingsW = this.contentW - 185;
            int sliderX = this.contentX + settingsW - this.sliderW - 12;
            this.updateHueSlider(this.active2DCol, mx, sliderX, this.sliderW);
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.focusedHexCol != -1) {
            char c = (char) event.codepoint();
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                if (this.typingHex.length() < 6) {
                    this.typingHex += c;
                    if (this.typingHex.length() == 6) {
                        this.applyHexColor(this.focusedHexCol, this.typingHex);
                    }
                    return true;
                }
            }
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.focusedHexCol != -1) {
            if (event.key() == 259) {
                if (!this.typingHex.isEmpty()) {
                    this.typingHex = this.typingHex.substring(0, this.typingHex.length() - 1);
                }
                return true;
            } else if (event.key() == 257 || event.key() == 335) {
                if (this.typingHex.length() == 6) {
                    this.applyHexColor(this.focusedHexCol, this.typingHex);
                }
                this.focusedHexCol = -1;
                return true;
            }
        }
        return super.keyPressed(event);
    }

    private void applyHexColor(int col, String hex) {
        try {
            int rgb = Integer.parseInt(hex, 16);
            float r = (float) ((rgb >> 16) & 0xFF) / 255.0f;
            float g = (float) ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (float) (rgb & 0xFF) / 255.0f;

            float max = Math.max(r, Math.max(g, b));
            float min = Math.min(r, Math.min(g, b));
            float delta = max - min;

            float h = 0.0f;
            if (delta > 0.00001f) {
                if (max == r) {
                    h = (g - b) / delta;
                    if (h < 0.0f) h += 6.0f;
                } else if (max == g) {
                    h = (b - r) / delta + 2.0f;
                } else {
                    h = (r - g) / delta + 4.0f;
                }
                h /= 6.0f;
            }

            float s = (max <= 0.00001f) ? 0.0f : (delta / max);
            float v = max;

            if (col == 0) {
                this.settings.outlineHue = h;
                this.settings.outlineSaturation = s;
                this.settings.outlineValue = v;
            } else {
                this.settings.fillHue = h;
                this.settings.fillSaturation = s;
                this.settings.fillValue = v;
            }
            BlockyOutlineSettings.save();
        } catch (NumberFormatException ignored) {}
    }

    private void update2DPicker(int col, double mx, double my, int boxX, int boxY, int boxW, int boxH) {
        float sat = (float) (mx - boxX) / (float) boxW;
        float val = 1.0f - (float) (my - boxY) / (float) boxH;
        sat = Mth.clamp(sat, 0.0f, 1.0f);
        val = Mth.clamp(val, 0.0f, 1.0f);

        if (col == 0) {
            this.settings.outlineSaturation = sat;
            this.settings.outlineValue = val;
        } else {
            this.settings.fillSaturation = sat;
            this.settings.fillValue = val;
        }
    }

    private void updateHueSlider(int col, double mx, int sliderX, int sliderW) {
        float hue = (float) (mx - sliderX) / (float) sliderW;
        hue = Mth.clamp(hue, 0.0f, 1.0f);

        if (col == 0) {
            this.settings.outlineHue = hue;
        } else {
            this.settings.fillHue = hue;
        }
    }

    private void renderColorPicker2D(GuiGraphicsExtractor guiGraphics, int rx, int ry, int col, int row, int height, boolean disabled, int mx, int my, int containerW) {
        float hue = (col == 0) ? this.settings.outlineHue : this.settings.fillHue;
        float saturation = (col == 0) ? this.settings.outlineSaturation : this.settings.fillSaturation;
        float value = (col == 0) ? this.settings.outlineValue : this.settings.fillValue;

        int boxX = rx + containerW - this.sliderW - 12;
        int boxW = this.sliderW;
        int boxH = 48;
        int boxY = ry + 4;

        if (disabled) {
            guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xFF141422);
        } else {
            for (int i = 0; i < boxW; ++i) {
                float s = (float) i / (float) (boxW - 1);
                float[] rgbTop = BlockyOutlineSettings.hsvToRgb(hue, s, 1.0f);
                int colorTop = 0xFF000000 | ((int) (rgbTop[0] * 255.0f) << 16) | ((int) (rgbTop[1] * 255.0f) << 8) | (int) (rgbTop[2] * 255.0f);
                int colorBottom = 0xFF000000;
                guiGraphics.fillGradient(boxX + i, boxY, boxX + i + 1, boxY + boxH, colorTop, colorBottom);
            }
        }

        int boxBorderCol = disabled ? 0xFF475569 : COLOR_BORDER_GRAY;
        guiGraphics.fill(boxX - 1, boxY - 1, boxX + boxW + 1, boxY, boxBorderCol);
        guiGraphics.fill(boxX - 1, boxY + boxH, boxX + boxW + 1, boxY + boxH + 1, boxBorderCol);
        guiGraphics.fill(boxX - 1, boxY, boxX, boxY + boxH, boxBorderCol);
        guiGraphics.fill(boxX + boxW, boxY, boxX + boxW + 1, boxY + boxH, boxBorderCol);

        if (!disabled) {
            int handleX = boxX + (int) (saturation * (float) (boxW - 1));
            int handleY = boxY + (int) ((1.0f - value) * (float) (boxH - 1));
            guiGraphics.fill(handleX - 3, handleY - 3, handleX + 4, handleY - 2, COLOR_TEXT_WHITE);
            guiGraphics.fill(handleX - 3, handleY + 3, handleX + 4, handleY + 4, COLOR_TEXT_WHITE);
            guiGraphics.fill(handleX - 3, handleY - 2, handleX - 2, handleY + 3, COLOR_TEXT_WHITE);
            guiGraphics.fill(handleX + 3, handleY - 2, handleX + 4, handleY + 3, COLOR_TEXT_WHITE);
        }

        int sliderX = boxX;
        int sliderY = boxY + boxH + 8;
        int sliderW_local = boxW;
        int sliderH = 6;

        if (disabled) {
            guiGraphics.fill(sliderX, sliderY, sliderX + sliderW_local, sliderY + sliderH, 0xFF141422);
        } else {
            for (int i = 0; i < sliderW_local; ++i) {
                float hueVal = (float) i / (float) (sliderW_local - 1);
                float[] rgb = BlockyOutlineSettings.hsvToRgb(hueVal);
                int color = 0xFF000000 | ((int) (rgb[0] * 255.0f) << 16) | ((int) (rgb[1] * 255.0f) << 8) | (int) (rgb[2] * 255.0f);
                guiGraphics.fill(sliderX + i, sliderY, sliderX + i + 1, sliderY + sliderH, color);
            }
        }

        int sliderBorderCol = disabled ? 0xFF475569 : COLOR_BORDER_GRAY;
        guiGraphics.fill(sliderX - 1, sliderY - 1, sliderX + sliderW_local + 1, sliderY, sliderBorderCol);
        guiGraphics.fill(sliderX - 1, sliderY + sliderH, sliderX + sliderW_local + 1, sliderY + sliderH + 1, sliderBorderCol);
        guiGraphics.fill(sliderX - 1, sliderY, sliderX, sliderY + sliderH, sliderBorderCol);
        guiGraphics.fill(sliderX + sliderW_local, sliderY, sliderX + sliderW_local + 1, sliderY + sliderH, sliderBorderCol);

        if (!disabled) {
            int thumbX = sliderX + (int) (hue * (float) (sliderW_local - 1));
            guiGraphics.fill(thumbX - 1, sliderY - 1, thumbX + 2, sliderY + sliderH + 1, COLOR_TEXT_WHITE);
            guiGraphics.fill(thumbX, sliderY - 1, thumbX + 1, sliderY + sliderH + 1, 0xFF000000);
        }

        int previewW = 34;
        int previewH = 28;
        int previewX = boxX - previewW - 10;
        int previewY = boxY + 4;

        float[] activeRgb = BlockyOutlineSettings.hsvToRgb(hue, saturation, value);
        int activeColor = 0xFF000000 | ((int) (activeRgb[0] * 255.0f) << 16) | ((int) (activeRgb[1] * 255.0f) << 8) | (int) (activeRgb[2] * 255.0f);
        guiGraphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, disabled ? 0xFF475569 : activeColor);

        int previewBorderCol = disabled ? 0xFF475569 : COLOR_TEXT_WHITE;
        guiGraphics.fill(previewX - 1, previewY - 1, previewX + previewW + 1, previewY, previewBorderCol);
        guiGraphics.fill(previewX - 1, previewY + previewH, previewX + previewW + 1, previewY + previewH + 1, previewBorderCol);
        guiGraphics.fill(previewX - 1, previewY, previewX, previewY + previewH, previewBorderCol);
        guiGraphics.fill(previewX + previewW, previewY, previewX + previewW + 1, previewY + previewH, previewBorderCol);

        if (!disabled) {
            String hexStr;
            int textColor;

            if (this.focusedHexCol == col) {
                hexStr = "#" + (this.typingHex + "______").substring(0, 6);
                textColor = COLOR_GREEN_NEON;
            } else {
                hexStr = String.format("#%06X", activeColor & 0xFFFFFF);
                textColor = COLOR_TEXT_WHITE;
            }

            int hexBoxW = 48;
            int hexBoxH = 12;
            int hexBoxX = previewX + (previewW - hexBoxW) / 2;
            int hexBoxY = sliderY + (sliderH - hexBoxH) / 2;

            guiGraphics.fill(hexBoxX, hexBoxY, hexBoxX + hexBoxW, hexBoxY + hexBoxH, 0xFF141422);
            int boxBorderColor = (this.focusedHexCol == col) ? COLOR_PURPLE_PRIMARY : COLOR_BORDER_GRAY;
            guiGraphics.fill(hexBoxX, hexBoxY, hexBoxX + hexBoxW, hexBoxY + 1, boxBorderColor);
            guiGraphics.fill(hexBoxX, hexBoxY + hexBoxH - 1, hexBoxX + hexBoxW, hexBoxY + hexBoxH, boxBorderColor);
            guiGraphics.fill(hexBoxX, hexBoxY, hexBoxX + 1, hexBoxY + hexBoxH, boxBorderColor);
            guiGraphics.fill(hexBoxX + hexBoxW - 1, hexBoxY, hexBoxX + hexBoxW, hexBoxY + hexBoxH, boxBorderColor);

            int textY = hexBoxY + (hexBoxH - 8) / 2;
            guiGraphics.text(this.font, hexStr, hexBoxX + (hexBoxW - this.font.width(hexStr)) / 2, textY, textColor, false);

            boolean hexHovered = mx >= hexBoxX && mx <= hexBoxX + hexBoxW && my >= hexBoxY && my <= hexBoxY + hexBoxH;
            if (hexHovered && this.focusedHexCol == -1) {
                this.hoveredTooltipText = "Click to enter HEX code";
            }
        }

        int btnW = 16;
        int btnH = 12;
        int btnX = previewX - btnW - 6;
        int btnY = ry + 4;
        boolean btnHovered = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;

        int btnBg = btnHovered ? COLOR_PURPLE_PRIMARY : 0xFF1B1B2C;
        int btnBorder = btnHovered ? COLOR_PURPLE_LIGHT : 0xFF475569;

        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + 1, btnBorder);
        guiGraphics.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, btnBorder);
        guiGraphics.fill(btnX, btnY, btnX + 1, btnY + btnH, btnBorder);
        guiGraphics.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, btnBorder);

        int symbolColor = btnHovered ? COLOR_PURPLE_LIGHT : COLOR_TEXT_MUTED;
        guiGraphics.centeredText(this.font, "-", btnX + btnW / 2, btnY + (btnH - 8) / 2, symbolColor);

        if (btnHovered) {
            this.hoveredTooltipText = "Click to collapse";
        }
    }

    private void renderColorPickerCollapsed(GuiGraphicsExtractor guiGraphics, int rx, int ry, int col, int row, int height, boolean disabled, int mx, int my, int containerW) {
        float hue = (col == 0) ? this.settings.outlineHue : this.settings.fillHue;
        float saturation = (col == 0) ? this.settings.outlineSaturation : this.settings.fillSaturation;
        float value = (col == 0) ? this.settings.outlineValue : this.settings.fillValue;

        int sliderX = rx + containerW - this.sliderW - 12;
        int sliderY = ry + (this.rowH - 4) / 2;
        int filled = (int)((float)this.sliderW * hue);

        if (disabled) {
            guiGraphics.fill(sliderX, sliderY - 1, sliderX + this.sliderW, sliderY + 5, 0xFF1B1B2C);
        } else {
            for (int i = 0; i < this.sliderW; ++i) {
                float hueVal = (float)i / (float)(this.sliderW - 1);
                float[] rgb = BlockyOutlineSettings.hsvToRgb(hueVal);
                int color = 0xFF000000 | ((int)(rgb[0] * 255.0f) << 16) | ((int)(rgb[1] * 255.0f) << 8) | (int)(rgb[2] * 255.0f);
                guiGraphics.fill(sliderX + i, sliderY - 1, sliderX + i + 1, sliderY + 5, color);
            }
        }

        int sliderBorderCol = disabled ? 0xFF475569 : COLOR_BORDER_GRAY;
        guiGraphics.fill(sliderX - 1, sliderY - 2, sliderX + this.sliderW + 1, sliderY - 1, sliderBorderCol);
        guiGraphics.fill(sliderX - 1, sliderY + 5, sliderX + this.sliderW + 1, sliderY + 6, sliderBorderCol);
        guiGraphics.fill(sliderX - 1, sliderY - 1, sliderX, sliderY + 5, sliderBorderCol);
        guiGraphics.fill(sliderX + this.sliderW, sliderY - 1, sliderX + this.sliderW + 1, sliderY + 5, sliderBorderCol);

        int thumbColor = disabled ? 0xFF64748B : COLOR_PURPLE_PRIMARY;
        if (!disabled) {
            float[] rgb = BlockyOutlineSettings.hsvToRgb(hue, saturation, value);
            thumbColor = 0xFF000000 | ((int)(rgb[0] * 255.0f) << 16) | ((int)(rgb[1] * 255.0f) << 8) | (int)(rgb[2] * 255.0f);
        }

        guiGraphics.fill(sliderX + filled - 3, sliderY - 4, sliderX + filled + 3, sliderY + 8, COLOR_TEXT_WHITE);
        guiGraphics.fill(sliderX + filled - 2, sliderY - 3, sliderX + filled + 2, sliderY + 7, thumbColor);

        int btnW = 16;
        int btnH = 12;
        int previewW = 34;
        int btnX = sliderX - previewW - 10 - btnW - 6;
        int btnY = ry + (this.rowH - btnH) / 2;
        boolean btnHovered = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;

        int btnBg = btnHovered ? COLOR_PURPLE_PRIMARY : 0xFF1B1B2C;
        int btnBorder = btnHovered ? COLOR_PURPLE_LIGHT : 0xFF475569;

        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + 1, btnBorder);
        guiGraphics.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, btnBorder);
        guiGraphics.fill(btnX, btnY, btnX + 1, btnY + btnH, btnBorder);
        guiGraphics.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, btnBorder);

        int symbolColor = btnHovered ? COLOR_PURPLE_LIGHT : COLOR_TEXT_MUTED;
        guiGraphics.centeredText(this.font, "+", btnX + btnW / 2, btnY + (btnH - 8) / 2, symbolColor);

        int pW = 14;
        int pH = 12;
        int pX = sliderX - pW - 6;
        int pY = ry + (this.rowH - pH) / 2;

        float[] activeRgb = BlockyOutlineSettings.hsvToRgb(hue, saturation, value);
        int activeColor = 0xFF000000 | ((int)(activeRgb[0] * 255.0f) << 16) | ((int)(activeRgb[1] * 255.0f) << 8) | (int)(activeRgb[2] * 255.0f);
        guiGraphics.fill(pX, pY, pX + pW, pY + pH, disabled ? 0xFF475569 : activeColor);

        int previewBorderCol = disabled ? 0xFF475569 : COLOR_TEXT_WHITE;
        guiGraphics.fill(pX - 1, pY - 1, pX + pW + 1, pY, previewBorderCol);
        guiGraphics.fill(pX - 1, pY + pH, pX + pW + 1, pY + pH + 1, previewBorderCol);
        guiGraphics.fill(pX - 1, pY, pX, pY + pH, previewBorderCol);
        guiGraphics.fill(pX + pW, pY, pX + pW + 1, pY + pH, previewBorderCol);

        if (btnHovered) {
            this.hoveredTooltipText = "Click to expand advanced 2D color picker";
        }
    }

    private void applyPreset(int index) {
        switch (index) {
            case 0 -> {
                this.settings.rainbowOutline = false;
                this.settings.outlineHue = 0.6f;
                this.settings.outlineSaturation = 0.05f;
                this.settings.outlineValue = 0.9f;
                this.settings.outlineOpacity = 0.85f;
                this.settings.outlineWidth = 2.0f;
                this.settings.fillEnabled = true;
                this.settings.rainbowFill = false;
                this.settings.fillHue = 0.6f;
                this.settings.fillSaturation = 0.05f;
                this.settings.fillValue = 0.8f;
                this.settings.fillOpacity = 0.15f;
            }
            case 1 -> {
                this.settings.rainbowOutline = false;
                this.settings.outlineHue = 0.75f;
                this.settings.outlineSaturation = 0.85f;
                this.settings.outlineValue = 0.95f;
                this.settings.outlineOpacity = 1.0f;
                this.settings.outlineWidth = 2.5f;
                this.settings.fillEnabled = true;
                this.settings.rainbowFill = false;
                this.settings.fillHue = 0.75f;
                this.settings.fillSaturation = 0.9f;
                this.settings.fillValue = 0.4f;
                this.settings.fillOpacity = 0.35f;
            }
            case 2 -> {
                this.settings.rainbowOutline = false;
                this.settings.outlineHue = 0.12f;
                this.settings.outlineSaturation = 0.9f;
                this.settings.outlineValue = 1.0f;
                this.settings.outlineOpacity = 1.0f;
                this.settings.outlineWidth = 3.0f;
                this.settings.fillEnabled = true;
                this.settings.rainbowFill = false;
                this.settings.fillHue = 0.12f;
                this.settings.fillSaturation = 0.8f;
                this.settings.fillValue = 0.9f;
                this.settings.fillOpacity = 0.25f;
            }
            case 3 -> {
                this.settings.rainbowOutline = true;
                this.settings.outlineRgbSpeed = 1.5f;
                this.settings.outlineOpacity = 1.0f;
                this.settings.outlineWidth = 2.5f;
                this.settings.fillEnabled = true;
                this.settings.rainbowFill = true;
                this.settings.fillRgbSpeed = 1.5f;
                this.settings.fillOpacity = 0.3f;
            }
            case 4 -> {
                this.settings.rainbowOutline = false;
                this.settings.outlineHue = 0.6f;
                this.settings.outlineSaturation = 0.15f;
                this.settings.outlineValue = 0.3f;
                this.settings.outlineOpacity = 0.9f;
                this.settings.outlineWidth = 2.0f;
                this.settings.fillEnabled = true;
                this.settings.rainbowFill = false;
                this.settings.fillHue = 0.6f;
                this.settings.fillSaturation = 0.2f;
                this.settings.fillValue = 0.15f;
                this.settings.fillOpacity = 0.4f;
            }
        }
        BlockyOutlineSettings.save();
    }

    private boolean isCheckboxRow(int col, int row) {
        if (col == 0) return row == 0 || row == 5;
        return row == 0 || row == 1;
    }

    private boolean getCheckboxValue(int col, int row) {
        if (col == 0) {
            if (row == 0) return this.settings.rainbowOutline;
            if (row == 5) return this.settings.smoothTransition;
        } else {
            if (row == 0) return this.settings.fillEnabled;
            if (row == 1) return this.settings.rainbowFill;
        }
        return false;
    }

    private void toggleCheckbox(int col, int row) {
        if (col == 0) {
            if (row == 0) this.settings.rainbowOutline = !this.settings.rainbowOutline;
            if (row == 5) this.settings.smoothTransition = !this.settings.smoothTransition;
        } else {
            if (row == 0) this.settings.fillEnabled = !this.settings.fillEnabled;
            if (row == 1) this.settings.rainbowFill = !this.settings.rainbowFill;
        }
        BlockyOutlineSettings.save();
    }

    private boolean isRowDisabled(int col, int row) {
        if (col == 0) {
            if (this.settings.rainbowOutline && (row == 2)) return true;
            if (!this.settings.rainbowOutline && (row == 1)) return true;
        } else {
            if (!this.settings.fillEnabled && row > 0) return true;
            if (this.settings.rainbowFill && row == 3) return true;
            if (!this.settings.rainbowFill && row == 2) return true;
        }
        return false;
    }

    private float getSliderPct(int col, int row) {
        if (col == 0) {
            return switch (row) {
                case 1 -> (this.settings.outlineRgbSpeed - 0.1f) / 4.9f;
                case 2 -> this.settings.outlineHue;
                case 3 -> this.settings.outlineOpacity;
                case 4 -> (this.settings.outlineWidth - 0.5f) / 9.5f;
                default -> 0.0f;
            };
        } else {
            return switch (row) {
                case 2 -> (this.settings.fillRgbSpeed - 0.1f) / 4.9f;
                case 3 -> this.settings.fillHue;
                case 4 -> this.settings.fillOpacity;
                default -> 0.0f;
            };
        }
    }

    private String getSliderValueStr(int col, int row) {
        if (col == 0) {
            return switch (row) {
                case 1 -> String.format("%.1fx", this.settings.outlineRgbSpeed);
                case 3 -> String.format("%d%%", (int)(this.settings.outlineOpacity * 100));
                case 4 -> String.format("%.1fpx", this.settings.outlineWidth);
                default -> "";
            };
        } else {
            return switch (row) {
                case 2 -> String.format("%.1fx", this.settings.fillRgbSpeed);
                case 4 -> String.format("%d%%", (int)(this.settings.fillOpacity * 100));
                default -> "";
            };
        }
    }

    private void updateSliderValue(int col, int row, double mx, int sliderX) {
        float pct = (float)(mx - sliderX) / (float)this.sliderW;
        pct = Mth.clamp(pct, 0.0f, 1.0f);

        if (col == 0) {
            switch (row) {
                case 1 -> this.settings.outlineRgbSpeed = 0.1f + pct * 4.9f;
                case 2 -> this.settings.outlineHue = pct;
                case 3 -> this.settings.outlineOpacity = pct;
                case 4 -> this.settings.outlineWidth = 0.5f + pct * 9.5f;
            }
        } else {
            switch (row) {
                case 2 -> this.settings.fillRgbSpeed = 0.1f + pct * 4.9f;
                case 3 -> this.settings.fillHue = pct;
                case 4 -> this.settings.fillOpacity = pct;
            }
        }
        BlockyOutlineSettings.save();
    }
}
