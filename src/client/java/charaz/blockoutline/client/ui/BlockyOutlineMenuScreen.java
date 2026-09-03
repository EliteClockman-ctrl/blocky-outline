package charaz.blockoutline.client.ui;

import charaz.blockoutline.config.BlockyOutlineSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public class BlockyOutlineMenuScreen extends Screen {
    private static final String[] TAB_LABELS = {"Outline", "Fill", "Presets", "About"};
    private static final String[] OUTLINE_ROW_LABELS = {"Rainbow outline", "RGB speed", "Colors", "Opacity", "Width", "Smooth movement"};
    private static final String[] FILL_ROW_LABELS = {"Enable fill", "Rainbow fill", "RGB speed", "Color 1 (Top)", "Enable Color 2", "Color 2 (Bottom)", "Opacity"};

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

    private static final int[] HUE_COLORS = {
            0xFFFF0000,
            0xFFFF4D00,
            0xFFFF9900,
            0xFFFFEE00,
            0xFF88FF00,
            0xFF00FF44,
            0xFF00FFAA,
            0xFF00CCFF,
            0xFF0044FF,
            0xFF7700FF,
            0xFFFF00CC,
            0xFFFF0044,
            0xFFFF0000
    };

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
    private boolean compactMode = false;
    private boolean showPreview = true;

    private int activeTab = 0;
    private boolean isDraggingSlider = false;
    private int dragCol = -1;
    private int dragRow = -1;

    private int activePickerTarget = -1;
    private boolean isDragging2DBox = false;
    private boolean isDraggingPopupHue = false;

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
        int maxRows = 7;

        int availW = Math.max(200, this.width - 16);
        int availH = Math.max(160, this.height - 16);

        this.compactMode = availH < 280 || availW < 450;
        this.showPreview = availW >= 420 && availH >= 240;

        if (this.compactMode) {
            this.topPad = Math.max(30, Math.min(36, availH / 7));
            this.rowH = Math.max(18, Math.min(22, (availH - this.topPad - 20) / maxRows - 3));
            this.rowGap = Math.max(2, Math.min(4, (availH - this.topPad - this.rowH * maxRows) / maxRows));
            this.sliderW = Math.max(50, Math.min(90, availW / 5));
        } else {
            this.topPad = 42;
            int desiredTotalH = this.topPad + (24 * maxRows) + (5 * (maxRows - 1)) + 16;
            if (desiredTotalH > availH) {
                this.rowH = Math.max(20, (availH - this.topPad - 20) / maxRows - 3);
                this.rowGap = Math.max(2, (availH - this.topPad - this.rowH * maxRows) / (maxRows + 1));
            } else {
                this.rowH = 24;
                this.rowGap = 4;
            }
            this.sliderW = Math.max(70, Math.min(105, availW / 4));
        }

        int totalContentH = this.topPad + (this.rowH * maxRows) + (this.rowGap * (maxRows - 1)) + 14;
        this.panelH = Math.min(availH, Math.max(180, totalContentH));
        this.panelW = Math.min(availW, this.showPreview ? 580 : 380);

        this.px = (this.width - this.panelW) / 2;
        this.py = (this.height - this.panelH) / 2;

        this.contentX = this.px + 12;
        this.contentW = this.panelW - 24;
    }

    @Override
    protected void init() {
        super.init();
        this.updateLayout();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
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

        int headerH = this.compactMode ? 28 : 36;
        guiGraphics.fillGradient(this.px + 1, this.py + 1, this.px + this.panelW - 1, this.py + headerH, COLOR_BG_HEADER, 0xFF161625);
        guiGraphics.fillGradient(this.px + 1, this.py + headerH, this.px + this.panelW - 1, this.py + headerH + 1, COLOR_PURPLE_PRIMARY, COLOR_PURPLE_LIGHT);

        int logoX = this.px + (this.compactMode ? 8 : 14);
        int logoY = this.py + (this.compactMode ? 4 : 10);
        int logoW;
        if (this.panelW < 360) {
            guiGraphics.drawString(this.font, "BO", logoX, logoY + (this.compactMode ? 2 : 4), COLOR_PURPLE_LIGHT, true);
            logoW = this.font.width("BO");
        } else {
            guiGraphics.drawString(this.font, "Blocky", logoX, logoY + (this.compactMode ? 2 : 4), COLOR_TEXT_WHITE, true);
            guiGraphics.drawString(this.font, "Outline", logoX + this.font.width("Blocky") + 3, logoY + (this.compactMode ? 2 : 4), COLOR_PURPLE_LIGHT, false);
            logoW = this.font.width("Blocky Outline");
        }

        String vTag = "v1.1.1";
        int vW = this.font.width(vTag);
        boolean showVTag = this.panelW >= 340;
        int vX = this.px + this.panelW - vW - (this.compactMode ? 8 : 12);
        if (showVTag) {
            int tagTop = this.py + (this.compactMode ? 5 : 9);
            int tagBot = tagTop + (this.compactMode ? 13 : 15);
            guiGraphics.fillGradient(vX - 3, tagTop, vX + vW + 3, tagBot, COLOR_PURPLE_PRIMARY, 0xFF7E22CE);
            guiGraphics.drawString(this.font, vTag, vX, tagTop + (this.compactMode ? 2 : 3), COLOR_TEXT_WHITE, false);
        }

        int tabStartX = logoX + logoW + (this.compactMode ? 8 : 14);
        int tabRightLimit = showVTag ? (vX - 6) : (this.px + this.panelW - 8);
        int tabAvailableW = tabRightLimit - tabStartX;
        int gap = 2;
        int tabW = Math.max(34, (tabAvailableW - gap * 3) / 4);
        int tabH = this.compactMode ? 17 : 21;
        int tabY = this.py + (this.compactMode ? 5 : 8);

        for (int i = 0; i < TAB_LABELS.length; ++i) {
            this.renderHeaderTab(guiGraphics, tabStartX + i * (tabW + gap), tabY, tabW, tabH, i, mouseX, mouseY);
        }

        int contentY = this.py + this.topPad;
        if (this.activeTab == 0 || this.activeTab == 1) {
            int col = this.activeTab;
            int currentY = contentY;
            int numRows = (col == 0) ? 6 : 7;
            for (int row = 0; row < numRows; ++row) {
                int height = this.getRowHeight(col, row);
                this.renderRow(guiGraphics, this.contentX, currentY, col, row, height, mouseX, mouseY);
                currentY += height + this.rowGap;
            }

            if (this.showPreview) {
                int pw = this.getPreviewWidth();
                this.renderLive3DBlockCanvas(guiGraphics, this.contentX + this.contentW - pw, contentY, pw, this.panelH - this.topPad - (this.compactMode ? 28 : 40));
            }
        } else if (this.activeTab == 2) {
            for (int i = 0; i < PRESET_NAMES.length; ++i) {
                this.renderPresetCard(guiGraphics, this.contentX, contentY + i * 34, i, mouseX, mouseY);
            }
        } else if (this.activeTab == 3) {
            this.renderAboutPanel(guiGraphics, this.contentX, contentY);
        }

        this.renderDoneButton(guiGraphics, mouseX, mouseY);

        super.render(guiGraphics, mouseX, mouseY, delta);

        if (this.activePickerTarget != -1) {
            this.renderColorPickerModal(guiGraphics, mouseX, mouseY);
        }

        if (this.hoveredTooltipText != null) {
            int textW = this.font.width(this.hoveredTooltipText);
            int tx = mouseX + 10;
            int ty = mouseY - 14;
            guiGraphics.fill(tx - 6, ty - 5, tx + textW + 6, ty + 13, 0xF0141422);
            guiGraphics.fill(tx - 6, ty - 5, tx + textW + 6, ty - 4, COLOR_PURPLE_PRIMARY);
            guiGraphics.fill(tx - 6, ty + 12, tx + textW + 6, ty + 13, COLOR_PURPLE_PRIMARY);
            guiGraphics.fill(tx - 6, ty - 4, tx - 5, ty + 12, COLOR_PURPLE_PRIMARY);
            guiGraphics.fill(tx + textW + 5, ty - 4, tx + textW + 6, ty + 12, COLOR_PURPLE_PRIMARY);
            guiGraphics.drawString(this.font, this.hoveredTooltipText, tx, ty, COLOR_TEXT_WHITE, false);
        }
    }

    private int getSettingsWidth() {
        if (!this.showPreview) {
            return this.contentW;
        }
        return this.contentW - this.getPreviewWidth() - 10;
    }

    private int getPreviewWidth() {
        if (!this.showPreview) return 0;
        return Math.max(130, Math.min(175, this.contentW / 3));
    }

    private void renderDoneButton(GuiGraphics guiGraphics, int mx, int my) {
        int btnW = this.compactMode ? 65 : 85;
        int btnH = this.compactMode ? 18 : 22;
        int btnX = this.px + this.panelW - btnW - (this.compactMode ? 10 : 16);
        int btnY = this.py + this.panelH - btnH - (this.compactMode ? 6 : 10);
        boolean hovered = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;

        int bgTop = hovered ? 0xFFA855F7 : 0xFF9333EA;
        int bgBot = hovered ? 0xFF9333EA : 0xFF7E22CE;
        guiGraphics.fillGradient(btnX, btnY, btnX + btnW, btnY + btnH, bgTop, bgBot);

        int border = hovered ? 0xFFC084FC : 0xFFA855F7;
        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + 1, border);
        guiGraphics.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, border);
        guiGraphics.fill(btnX, btnY, btnX + 1, btnY + btnH, border);
        guiGraphics.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, border);

        int textCol = hovered ? 0xFFFFFFFF : COLOR_TEXT_WHITE;
        guiGraphics.drawCenteredString(this.font, "Done", btnX + btnW / 2, btnY + (btnH - 8) / 2, textCol);
    }

    private void renderHeaderTab(GuiGraphics guiGraphics, int tx, int ty, int tw, int th, int index, int mx, int my) {
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
        guiGraphics.drawCenteredString(this.font, TAB_LABELS[index], tx + tw / 2, ty + (th - 8) / 2, textCol);
    }

    private int getRowHeight(int col, int row) {
        return this.rowH;
    }

    private void renderRow(GuiGraphics guiGraphics, int rx, int ry, int col, int row, int height, int mx, int my) {
        boolean disabled = this.isRowDisabled(col, row);
        int settingsW = this.getSettingsWidth();
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
        guiGraphics.drawString(this.font, label, rx + (this.compactMode ? 8 : 12), ry + (this.rowH - 8) / 2, labelColor, false);

        if (this.isCheckboxRow(col, row)) {
            this.renderToggleSwitch(guiGraphics, rx, ry, col, row, disabled, settingsW);
        } else if ((col == 0 && row == 2) || (col == 1 && (row == 3 || row == 5))) {
            this.renderColorSliderAndHex(guiGraphics, rx, ry, col, row, disabled, mx, my, settingsW);
        } else {
            this.renderSlider(guiGraphics, rx, ry, col, row, disabled, settingsW);
        }
    }

    private void renderToggleSwitch(GuiGraphics guiGraphics, int rx, int ry, int col, int row, boolean disabled, int containerW) {
        boolean checked = this.getCheckboxValue(col, row);
        int switchW = 34;
        int switchH = 16;
        int switchX = rx + containerW - switchW - 12;
        int switchY = ry + (this.rowH - switchH) / 2;

        int trackBg = disabled ? 0xFF1E293B : (checked ? COLOR_PURPLE_PRIMARY : 0xFF2D2D44);
        guiGraphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, trackBg);

        int borderCol = disabled ? 0xFF475569 : (checked ? COLOR_PURPLE_LIGHT : 0xFF475569);
        guiGraphics.fill(switchX, switchY, switchX + switchW, switchY + 1, borderCol);
        guiGraphics.fill(switchX, switchY + switchH - 1, switchX + switchW, switchY + switchH, borderCol);
        guiGraphics.fill(switchX, switchY, switchX + 1, switchY + switchH, borderCol);
        guiGraphics.fill(switchX + switchW - 1, switchY, switchX + switchW, switchY + switchH, borderCol);

        int thumbW = 12;
        int thumbH = 12;
        int thumbX = checked ? switchX + switchW - thumbW - 2 : switchX + 2;
        int thumbY = switchY + 2;
        int thumbColor = disabled ? 0xFF64748B : COLOR_TEXT_WHITE;
        guiGraphics.fill(thumbX, thumbY, thumbX + thumbW, thumbY + thumbH, thumbColor);

        String stateText = checked ? "● ON" : "○ OFF";
        int stateColor = disabled ? 0xFF64748B : (checked ? COLOR_GREEN_NEON : COLOR_RED_OFF);
        guiGraphics.drawString(this.font, stateText, switchX - this.font.width(stateText) - 8, ry + (this.rowH - 8) / 2, stateColor, false);
    }

    private void renderSlider(GuiGraphics guiGraphics, int rx, int ry, int col, int row, boolean disabled, int containerW) {
        float pct = this.getSliderPct(col, row);
        String val = this.getSliderValueStr(col, row);

        int sliderX = rx + containerW - this.sliderW - 12;
        int sliderY = ry + (this.rowH - 6) / 2;
        int filled = (int)((float)this.sliderW * pct);

        int trackBg = 0xFF141422;
        guiGraphics.fill(sliderX, sliderY, sliderX + this.sliderW, sliderY + 6, trackBg);
        if (!disabled) {
            guiGraphics.fillGradient(sliderX, sliderY, sliderX + filled, sliderY + 6, COLOR_PURPLE_PRIMARY, COLOR_PURPLE_LIGHT);
        } else {
            guiGraphics.fill(sliderX, sliderY, sliderX + filled, sliderY + 6, 0xFF475569);
        }

        int thumbColor = disabled ? 0xFF64748B : COLOR_PURPLE_PRIMARY;
        guiGraphics.fill(sliderX + filled - 3, sliderY - 3, sliderX + filled + 3, sliderY + 9, COLOR_TEXT_WHITE);
        guiGraphics.fill(sliderX + filled - 2, sliderY - 2, sliderX + filled + 2, sliderY + 8, thumbColor);

        if (!val.isEmpty()) {
            int valColor = disabled ? 0xFF64748B : COLOR_TEXT_WHITE;
            guiGraphics.drawString(this.font, val, sliderX - this.font.width(val) - 8, ry + (this.rowH - 8) / 2, valColor, false);
        }
    }

    private void renderHueBar(GuiGraphics guiGraphics, int x, int y, int w, int h, boolean disabled) {
        if (disabled) {
            guiGraphics.fill(x, y, x + w, y + h, 0xFF141422);
            return;
        }
        int numSegments = HUE_COLORS.length - 1;
        float segW = (float) w / (float) numSegments;
        for (int i = 0; i < numSegments; ++i) {
            int xStart = x + (int) (i * segW);
            int xEnd = (i == numSegments - 1) ? (x + w) : (x + (int) ((i + 1) * segW));
            guiGraphics.fillGradient(xStart, y, xEnd, y + h, HUE_COLORS[i], HUE_COLORS[i + 1]);
        }
    }

    private void renderColorSliderAndHex(GuiGraphics guiGraphics, int rx, int ry, int col, int row, boolean disabled, int mx, int my, int containerW) {
        boolean isColor2 = (col == 1 && row == 5);
        float hue = (col == 0) ? this.settings.outlineHue : (isColor2 ? this.settings.fillHue2 : this.settings.fillHue);
        float saturation = (col == 0) ? this.settings.outlineSaturation : (isColor2 ? this.settings.fillSaturation2 : this.settings.fillSaturation);
        float value = (col == 0) ? this.settings.outlineValue : (isColor2 ? this.settings.fillValue2 : this.settings.fillValue);
        int hexIndex = isColor2 ? 2 : col;

        int trackH = 8;
        int sliderX = rx + containerW - this.sliderW - 12;
        int sliderY = ry + (this.rowH - trackH) / 2;
        int thumbX = sliderX + (int)((float)this.sliderW * hue);

        int trackBorder = disabled ? 0xFF334155 : 0xFF2D2D44;
        guiGraphics.fill(sliderX - 2, sliderY - 2, sliderX + this.sliderW + 2, sliderY + trackH + 2, 0xFF0D0C16);
        guiGraphics.fill(sliderX - 1, sliderY - 1, sliderX + this.sliderW + 1, sliderY + trackH + 1, trackBorder);

        this.renderHueBar(guiGraphics, sliderX, sliderY, this.sliderW, trackH, disabled);

        if (!disabled) {
            guiGraphics.fill(sliderX, sliderY, sliderX + this.sliderW, sliderY + 1, 0x30FFFFFF);
        }

        float[] activeRgb = BlockyOutlineSettings.hsvToRgb(hue, saturation, value);
        int activeColor = 0xFF000000 | ((int) (activeRgb[0] * 255.0f) << 16) | ((int) (activeRgb[1] * 255.0f) << 8) | (int) (activeRgb[2] * 255.0f);
        int thumbFillColor = disabled ? 0xFF64748B : activeColor;

        int thumbW = 6;
        int thumbH = trackH + 6;
        int thumbY = sliderY - 3;
        int tLeft = thumbX - thumbW / 2;
        int tRight = tLeft + thumbW;

        guiGraphics.fill(tLeft - 1, thumbY - 1, tRight + 1, thumbY + thumbH + 1, 0xAA000000); // Drop shadow
        guiGraphics.fill(tLeft, thumbY, tRight, thumbY + thumbH, COLOR_TEXT_WHITE);           // White casing
        guiGraphics.fill(tLeft + 1, thumbY + 1, tRight - 1, thumbY + thumbH - 1, thumbFillColor); // Active color inside handle

        int pSize = 15;
        int pX = sliderX - pSize - 8;
        int pY = ry + (this.rowH - pSize) / 2;
        boolean swatchHovered = !disabled && mx >= pX && mx <= pX + pSize && my >= pY && my <= pY + pSize;

        guiGraphics.fill(pX - 2, pY - 2, pX + pSize + 2, pY + pSize + 2, 0xFF0D0C16);
        guiGraphics.fill(pX, pY, pX + pSize, pY + pSize, disabled ? 0xFF334155 : activeColor);
        int swatchBorder = disabled ? 0xFF475569 : (swatchHovered ? COLOR_PURPLE_LILAC : 0xFFE2E8F0);
        guiGraphics.fill(pX - 1, pY - 1, pX + pSize + 1, pY, swatchBorder);
        guiGraphics.fill(pX - 1, pY + pSize, pX + pSize + 1, pY + pSize + 1, swatchBorder);
        guiGraphics.fill(pX - 1, pY, pX, pY + pSize, swatchBorder);
        guiGraphics.fill(pX + pSize, pY, pX + pSize + 1, pY + pSize, swatchBorder);

        if (swatchHovered && this.activePickerTarget == -1 && this.focusedHexCol == -1) {
            this.hoveredTooltipText = "Click to open Color Palette";
        }

        if (!disabled) {
            String hexStr;
            int textColor;

            if (this.focusedHexCol == hexIndex) {
                hexStr = "#" + (this.typingHex + "______").substring(0, 6);
                textColor = COLOR_GREEN_NEON;
            } else {
                hexStr = String.format("#%06X", activeColor & 0xFFFFFF);
                textColor = COLOR_TEXT_WHITE;
            }

            int hexBoxW = 46;
            int hexBoxH = 15;
            int hexBoxX = pX - hexBoxW - 6;
            int hexBoxY = ry + (this.rowH - hexBoxH) / 2;
            boolean hexHovered = mx >= hexBoxX && mx <= hexBoxX + hexBoxW && my >= hexBoxY && my <= hexBoxY + hexBoxH;

            int hexBg = (this.focusedHexCol == hexIndex) ? 0xFF1B1B2C : (hexHovered ? 0xFF24243B : 0xFF141422);
            guiGraphics.fill(hexBoxX, hexBoxY, hexBoxX + hexBoxW, hexBoxY + hexBoxH, hexBg);

            int boxBorderColor = (this.focusedHexCol == hexIndex) ? COLOR_PURPLE_LIGHT : (hexHovered ? COLOR_PURPLE_LILAC : 0xFF3B3B54);
            guiGraphics.fill(hexBoxX, hexBoxY, hexBoxX + hexBoxW, hexBoxY + 1, boxBorderColor);
            guiGraphics.fill(hexBoxX, hexBoxY + hexBoxH - 1, hexBoxX + hexBoxW, hexBoxY + hexBoxH, boxBorderColor);
            guiGraphics.fill(hexBoxX, hexBoxY, hexBoxX + 1, hexBoxY + hexBoxH, boxBorderColor);
            guiGraphics.fill(hexBoxX + hexBoxW - 1, hexBoxY, hexBoxX + hexBoxW, hexBoxY + hexBoxH, boxBorderColor);

            int textY = hexBoxY + (hexBoxH - 8) / 2;
            guiGraphics.drawString(this.font, hexStr, hexBoxX + (hexBoxW - this.font.width(hexStr)) / 2, textY, textColor, false);

            if (hexHovered && this.focusedHexCol == -1 && this.activePickerTarget == -1) {
                this.hoveredTooltipText = "Click to enter custom HEX color code";
            }
        }
    }

    private void renderLive3DBlockCanvas(GuiGraphics guiGraphics, int cx, int cy, int cw, int ch) {
        guiGraphics.fill(cx, cy, cx + cw, cy + ch, COLOR_BG_CARD);
        guiGraphics.fill(cx, cy, cx + cw, cy + 1, COLOR_BORDER_PURPLE);
        guiGraphics.fill(cx, cy + ch - 1, cx + cw, cy + ch, COLOR_BORDER_PURPLE);
        guiGraphics.fill(cx, cy, cx + 1, cy + ch, COLOR_BORDER_PURPLE);
        guiGraphics.fill(cx + cw - 1, cy, cx + cw, cy + ch, COLOR_BORDER_PURPLE);

        guiGraphics.fillGradient(cx + 1, cy + 1, cx + cw - 1, cy + 20, 0xFF1B1B2C, 0xFF161625);
        guiGraphics.fill(cx + 1, cy + 20, cx + cw - 1, cy + 21, COLOR_PURPLE_PRIMARY);
        guiGraphics.drawCenteredString(this.font, "Live preview", cx + cw / 2, cy + 6, COLOR_TEXT_GRAY);

        long now = System.currentTimeMillis();
        int outlineColor = this.settings.getOutlineArgb(now);
        int fillColor = this.settings.getFillArgb(now);
        int fillColor2 = this.settings.getFillArgb2(now);

        int centerX = cx + cw / 2;
        int centerY = cy + (ch - 40) / 2 + 6;
        int boxW = 54;
        int boxH = 54;
        int boxX = centerX - boxW / 2;
        int boxY = centerY - boxH / 2;

        if (this.settings.fillEnabled) {
            guiGraphics.fillGradient(boxX, boxY, boxX + boxW, boxY + boxH, fillColor, fillColor2);
        } else {
            guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0x20000000);
        }

        int lw = Math.max(1, Math.min(6, (int) this.settings.outlineWidth));
        guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + lw, outlineColor);
        guiGraphics.fill(boxX, boxY + boxH - lw, boxX + boxW, boxY + boxH, outlineColor);
        guiGraphics.fill(boxX, boxY, boxX + lw, boxY + boxH, outlineColor);
        guiGraphics.fill(boxX + boxW - lw, boxY, boxX + boxW, boxY + boxH, outlineColor);

        guiGraphics.fill(boxX + boxW / 2, boxY, boxX + boxW / 2 + 1, boxY + boxH, 0x15FFFFFF);
        guiGraphics.fill(boxX, boxY + boxH / 2, boxX + boxW, boxY + boxH / 2 + 1, 0x15FFFFFF);

        int statsY = cy + ch - 48;
        guiGraphics.fill(cx + 8, statsY, cx + cw - 8, cy + ch - 8, 0xFF1B1B2C);
        guiGraphics.fill(cx + 8, statsY, cx + cw - 8, statsY + 1, COLOR_PURPLE_PRIMARY);

        guiGraphics.drawString(this.font, "Width: " + String.format("%.1fpx", this.settings.outlineWidth), cx + 14, statsY + 6, COLOR_TEXT_WHITE, false);
        guiGraphics.drawString(this.font, "Alpha: " + String.format("%d%%", (int)(this.settings.outlineOpacity * 100)), cx + 14, statsY + 18, COLOR_TEXT_GRAY, false);
        guiGraphics.drawString(this.font, "Rainbow: " + (this.settings.rainbowOutline ? "Active" : "Off"), cx + 14, statsY + 30, this.settings.rainbowOutline ? COLOR_GREEN_NEON : COLOR_TEXT_MUTED, false);
    }

    private void renderPresetCard(GuiGraphics guiGraphics, int pxX, int pxY, int index, int mx, int my) {
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
        guiGraphics.drawString(this.font, PRESET_NAMES[index], pxX + 14, pxY + 4, textCol, false);
        guiGraphics.drawString(this.font, PRESET_DESCS[index], pxX + 14, pxY + 15, COLOR_TEXT_MUTED, false);

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

        guiGraphics.drawCenteredString(this.font, "Apply", btnX + btnW / 2, btnY + 4, COLOR_TEXT_WHITE);
    }

    private void renderAboutPanel(GuiGraphics guiGraphics, int ax, int ay) {
        int cardW = this.contentW;
        int cardH = 185;

        guiGraphics.fill(ax, ay, ax + cardW, ay + cardH, COLOR_BG_CARD);
        guiGraphics.fill(ax, ay, ax + cardW, ay + 1, COLOR_BORDER_PURPLE);
        guiGraphics.fill(ax, ay + cardH - 1, ax + cardW, ay + cardH, COLOR_BORDER_PURPLE);
        guiGraphics.fill(ax, ay, ax + 1, ay + cardH, COLOR_BORDER_PURPLE);
        guiGraphics.fill(ax + cardW - 1, ay, ax + cardW, ay + cardH, COLOR_BORDER_PURPLE);

        guiGraphics.fillGradient(ax + 1, ay + 1, ax + cardW - 1, ay + 42, 0xFF1B1B2C, 0xFF161625);
        guiGraphics.fillGradient(ax + 1, ay + 42, ax + cardW - 1, ay + 43, COLOR_PURPLE_PRIMARY, COLOR_PURPLE_LIGHT);

        guiGraphics.drawString(this.font, "Blocky", ax + 16, ay + 12, COLOR_TEXT_WHITE, true);
        guiGraphics.drawString(this.font, "Outline", ax + 16 + this.font.width("Blocky") + 4, ay + 12, COLOR_PURPLE_LIGHT, false);
        guiGraphics.drawString(this.font, "Next-generation block outline & fill customizer", ax + 16, ay + 26, COLOR_TEXT_MUTED, false);

        String vStr = "v1.1.1";
        String envStr = "Fabric 1.21.11";
        int vW = this.font.width(vStr);
        int envW = this.font.width(envStr);
        int badge2X = ax + cardW - envW - 16;
        int badge1X = badge2X - vW - 16;

        guiGraphics.fill(badge1X - 5, ay + 12, badge1X + vW + 5, ay + 27, COLOR_PURPLE_PRIMARY);
        guiGraphics.drawString(this.font, vStr, badge1X, ay + 15, COLOR_TEXT_WHITE, false);

        guiGraphics.fill(badge2X - 5, ay + 12, badge2X + envW + 5, ay + 27, 0xFF2D2D44);
        guiGraphics.drawString(this.font, envStr, badge2X, ay + 15, COLOR_GREEN_NEON, false);

        int kbY = ay + 50;
        guiGraphics.fill(ax + 14, kbY, ax + cardW - 14, kbY + 28, 0xFF1B1B2C);
        guiGraphics.fill(ax + 14, kbY, ax + 18, kbY + 28, COLOR_PURPLE_PRIMARY);
        guiGraphics.drawString(this.font, "Keybind Shortcut:", ax + 26, kbY + 5, COLOR_PURPLE_LIGHT, false);
        guiGraphics.drawString(this.font, "Press [ M ] anywhere in-game to toggle configuration menu", ax + 26, kbY + 16, COLOR_TEXT_WHITE, false);

        int infoY = ay + 84;
        int infoW = (cardW - 36) / 2;

        guiGraphics.fill(ax + 14, infoY, ax + 14 + infoW, infoY + 38, 0xFF181828);
        guiGraphics.fill(ax + 14, infoY, ax + 14 + infoW, infoY + 1, COLOR_BORDER_GRAY);
        guiGraphics.drawString(this.font, "Author & Developer", ax + 22, infoY + 7, COLOR_TEXT_MUTED, false);
        guiGraphics.drawString(this.font, "CharaZ", ax + 22, infoY + 20, COLOR_TEXT_WHITE, true);

        guiGraphics.fill(ax + 22 + infoW, infoY, ax + 22 + infoW * 2, infoY + 38, 0xFF181828);
        guiGraphics.fill(ax + 22 + infoW, infoY, ax + 22 + infoW * 2, infoY + 1, COLOR_BORDER_GRAY);
        guiGraphics.drawString(this.font, "Render Engine", ax + 30 + infoW, infoY + 7, COLOR_TEXT_MUTED, false);
        guiGraphics.drawString(this.font, "Smooth Fast Outline Engine", ax + 30 + infoW, infoY + 20, COLOR_GREEN_NEON, false);

        int repoY = ay + 128;
        guiGraphics.fill(ax + 14, repoY, ax + cardW - 14, repoY + 44, 0xFF161624);
        guiGraphics.fill(ax + 14, repoY, ax + cardW - 14, repoY + 1, COLOR_BORDER_GRAY);

        guiGraphics.drawString(this.font, "GitHub Repository:", ax + 24, repoY + 8, COLOR_PURPLE_LILAC, false);
        guiGraphics.drawString(this.font, "github.com/EliteClockman-ctrl/blocky-outline", ax + 24, repoY + 20, COLOR_TEXT_GRAY, false);
    }

    private void renderColorPickerModal(GuiGraphics guiGraphics, int mx, int my) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);

        int mW = 240;
        int mH = 205;
        int mX = (this.width - mW) / 2;
        int mY = (this.height - mH) / 2;

        guiGraphics.fill(mX, mY, mX + mW, mY + mH, 0xFD12121E);
        guiGraphics.fill(mX - 1, mY - 1, mX + mW + 1, mY, COLOR_PURPLE_PRIMARY);
        guiGraphics.fill(mX - 1, mY + mH, mX + mW + 1, mY + mH + 1, COLOR_PURPLE_PRIMARY);
        guiGraphics.fill(mX - 1, mY, mX, mY + mH, COLOR_PURPLE_PRIMARY);
        guiGraphics.fill(mX + mW, mY, mX + mW + 1, mY + mH, COLOR_PURPLE_PRIMARY);

        guiGraphics.fillGradient(mX + 1, mY + 1, mX + mW - 1, mY + 24, 0xFF1E1B2E, 0xFF171424);
        guiGraphics.fill(mX + 1, mY + 24, mX + mW - 1, mY + 25, 0xFF2D2640);

        String title;
        if (this.activePickerTarget == 0) title = "Palette: Outline Color";
        else if (this.activePickerTarget == 1) title = "Palette: Fill Color 1 (Top)";
        else title = "Palette: Fill Color 2 (Bottom)";
        guiGraphics.drawString(this.font, title, mX + 10, mY + 8, COLOR_TEXT_WHITE, false);

        int closeBtnW = 16;
        int closeBtnH = 14;
        int closeBtnX = mX + mW - closeBtnW - 6;
        int closeBtnY = mY + 5;
        boolean closeHover = mx >= closeBtnX && mx <= closeBtnX + closeBtnW && my >= closeBtnY && my <= closeBtnY + closeBtnH;
        guiGraphics.fill(closeBtnX, closeBtnY, closeBtnX + closeBtnW, closeBtnY + closeBtnH, closeHover ? 0xFFEF4444 : 0xFF282538);
        guiGraphics.drawCenteredString(this.font, "×", closeBtnX + closeBtnW / 2, closeBtnY + 2, COLOR_TEXT_WHITE);

        float hue, sat, val;
        if (this.activePickerTarget == 0) {
            hue = this.settings.outlineHue;
            sat = this.settings.outlineSaturation;
            val = this.settings.outlineValue;
        } else if (this.activePickerTarget == 1) {
            hue = this.settings.fillHue;
            sat = this.settings.fillSaturation;
            val = this.settings.fillValue;
        } else {
            hue = this.settings.fillHue2;
            sat = this.settings.fillSaturation2;
            val = this.settings.fillValue2;
        }

        int boxX = mX + 12;
        int boxY = mY + 34;
        int boxW = 180;
        int boxH = 100;

        float[] baseRgb = BlockyOutlineSettings.hsvToRgb(hue, 1.0f, 1.0f);
        int baseColor = 0xFF000000 | ((int) (baseRgb[0] * 255.0f) << 16) | ((int) (baseRgb[1] * 255.0f) << 8) | (int) (baseRgb[2] * 255.0f);

        guiGraphics.fill(boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, 0xFF2D2640);

        int steps = 24;
        float stepW = (float) boxW / (float) steps;
        for (int i = 0; i < steps; ++i) {
            int x0 = boxX + (int) (i * stepW);
            int x1 = (i == steps - 1) ? (boxX + boxW) : (boxX + (int) ((i + 1) * stepW));
            float s0 = (float) i / (float) steps;
            float s1 = (float) (i + 1) / (float) steps;
            int c0 = lerpColor(0xFFFFFFFF, baseColor, s0);
            int c1 = lerpColor(0xFFFFFFFF, baseColor, s1);
            guiGraphics.fillGradient(x0, boxY, x1, boxY + boxH, c0, c1);
        }
        guiGraphics.fillGradient(boxX, boxY, boxX + boxW, boxY + boxH, 0x00000000, 0xFF000000);

        int curX = boxX + (int) (sat * (float) boxW);
        int curY = boxY + (int) ((1.0f - val) * (float) boxH);
        curX = Mth.clamp(curX, boxX, boxX + boxW);
        curY = Mth.clamp(curY, boxY, boxY + boxH);

        guiGraphics.fill(curX - 4, curY - 4, curX + 5, curY - 3, 0xFF000000);
        guiGraphics.fill(curX - 4, curY + 4, curX + 5, curY + 5, 0xFF000000);
        guiGraphics.fill(curX - 4, curY - 3, curX - 3, curY + 4, 0xFF000000);
        guiGraphics.fill(curX + 4, curY - 3, curX + 5, curY + 4, 0xFF000000);

        guiGraphics.fill(curX - 3, curY - 3, curX + 4, curY - 2, 0xFFFFFFFF);
        guiGraphics.fill(curX - 3, curY + 3, curX + 4, curY + 4, 0xFFFFFFFF);
        guiGraphics.fill(curX - 3, curY - 2, curX - 2, curY + 3, 0xFFFFFFFF);
        guiGraphics.fill(curX + 3, curY - 2, curX + 4, curY + 3, 0xFFFFFFFF);

        int vHueX = boxX + boxW + 10;
        int vHueY = boxY;
        int vHueW = 16;
        int vHueH = boxH;

        guiGraphics.fill(vHueX - 1, vHueY - 1, vHueX + vHueW + 1, vHueY + vHueH + 1, 0xFF2D2640);
        int numSegs = HUE_COLORS.length - 1;
        float hSegH = (float) vHueH / (float) numSegs;
        for (int i = 0; i < numSegs; ++i) {
            int y0 = vHueY + (int) (i * hSegH);
            int y1 = (i == numSegs - 1) ? (vHueY + vHueH) : (vHueY + (int) ((i + 1) * hSegH));
            guiGraphics.fillGradient(vHueX, y0, vHueX + vHueW, y1, HUE_COLORS[i], HUE_COLORS[i + 1]);
        }

        int hThumbY = vHueY + (int) (hue * (float) vHueH);
        hThumbY = Mth.clamp(hThumbY, vHueY, vHueY + vHueH);
        guiGraphics.fill(vHueX - 2, hThumbY - 2, vHueX + vHueW + 2, hThumbY + 2, 0xFF000000);
        guiGraphics.fill(vHueX - 1, hThumbY - 1, vHueX + vHueW + 1, hThumbY + 1, 0xFFFFFFFF);

        int activeRgbInt = BlockyOutlineSettings.hsvToRgbPacked(hue, sat, val);
        int activeArgb = 0xFF000000 | activeRgbInt;

        int bottomY = boxY + boxH + 12;
        int pSize = 18;
        guiGraphics.fill(boxX - 1, bottomY - 1, boxX + pSize + 1, bottomY + pSize + 1, 0xFF2D2640);
        guiGraphics.fill(boxX, bottomY, boxX + pSize, bottomY + pSize, activeArgb);

        int modalHexX = boxX + pSize + 8;
        int modalHexW = 54;
        int modalHexH = 18;
        guiGraphics.fill(modalHexX, bottomY, modalHexX + modalHexW, bottomY + modalHexH, 0xFF181524);
        guiGraphics.fill(modalHexX, bottomY, modalHexX + modalHexW, bottomY + 1, COLOR_PURPLE_PRIMARY);
        guiGraphics.fill(modalHexX, bottomY + modalHexH - 1, modalHexX + modalHexW, bottomY + modalHexH, COLOR_PURPLE_PRIMARY);
        guiGraphics.fill(modalHexX, bottomY, modalHexX + 1, bottomY + modalHexH, COLOR_PURPLE_PRIMARY);
        guiGraphics.fill(modalHexX + modalHexW - 1, bottomY, modalHexX + modalHexW, bottomY + modalHexH, COLOR_PURPLE_PRIMARY);
        String hexCode = String.format("#%06X", activeRgbInt & 0xFFFFFF);
        guiGraphics.drawString(this.font, hexCode, modalHexX + (modalHexW - this.font.width(hexCode)) / 2, bottomY + 5, COLOR_TEXT_WHITE, false);

        int[] palette = {
                0xFFFF0044, 0xFFFF7700, 0xFFFFDD00, 0xFF00DD44, 0xFF00CCFF, 0xFF3366FF, 0xFFA855F7, 0xFFFFFFFF
        };
        int dotSize = 9;
        int dotGap = 3;
        int dotStartX = modalHexX + modalHexW + 8;
        for (int i = 0; i < palette.length; ++i) {
            int dx = dotStartX + i * (dotSize + dotGap);
            int dy = bottomY + 4;
            boolean dotHov = mx >= dx && mx <= dx + dotSize && my >= dy && my <= dy + dotSize;
            guiGraphics.fill(dx - 1, dy - 1, dx + dotSize + 1, dy + dotSize + 1, dotHov ? COLOR_PURPLE_LILAC : 0xFF2D2640);
            guiGraphics.fill(dx, dy, dx + dotSize, dy + dotSize, palette[i]);
        }

        int doneBtnW = 60;
        int doneBtnH = 16;
        int doneBtnX = mX + mW - doneBtnW - 12;
        int doneBtnY = mY + mH - doneBtnH - 10;
        boolean doneHov = mx >= doneBtnX && mx <= doneBtnX + doneBtnW && my >= doneBtnY && my <= doneBtnY + doneBtnH;
        guiGraphics.fillGradient(doneBtnX, doneBtnY, doneBtnX + doneBtnW, doneBtnY + doneBtnH, doneHov ? 0xFFA855F7 : 0xFF9333EA, doneHov ? 0xFF9333EA : 0xFF7E22CE);
        guiGraphics.fill(doneBtnX, doneBtnY, doneBtnX + doneBtnW, doneBtnY + 1, COLOR_PURPLE_LILAC);
        guiGraphics.fill(doneBtnX, doneBtnY + doneBtnH - 1, doneBtnX + doneBtnW, doneBtnY + doneBtnH, COLOR_PURPLE_LILAC);
        guiGraphics.fill(doneBtnX, doneBtnY, doneBtnX + 1, doneBtnY + doneBtnH, COLOR_PURPLE_LILAC);
        guiGraphics.fill(doneBtnX + doneBtnW - 1, doneBtnY, doneBtnX + doneBtnW, doneBtnY + doneBtnH, COLOR_PURPLE_LILAC);
        guiGraphics.drawCenteredString(this.font, "OK", doneBtnX + doneBtnW / 2, doneBtnY + 4, COLOR_TEXT_WHITE);
    }

    private static int lerpColor(int c1, int c2, float t) {
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void updateModal2DPicker(double mx, double my) {
        int mW = 240;
        int mX = (this.width - mW) / 2;
        int mY = (this.height - 205) / 2;
        int boxX = mX + 12;
        int boxY = mY + 34;
        int boxW = 180;
        int boxH = 100;

        float sat = (float) (mx - boxX) / (float) boxW;
        float val = 1.0f - (float) (my - boxY) / (float) boxH;
        sat = Mth.clamp(sat, 0.0f, 1.0f);
        val = Mth.clamp(val, 0.0f, 1.0f);

        if (this.activePickerTarget == 0) {
            this.settings.outlineSaturation = sat;
            this.settings.outlineValue = val;
        } else if (this.activePickerTarget == 1) {
            this.settings.fillSaturation = sat;
            this.settings.fillValue = val;
        } else if (this.activePickerTarget == 2) {
            this.settings.fillSaturation2 = sat;
            this.settings.fillValue2 = val;
        }
        BlockyOutlineSettings.save();
    }

    private void updateModalHue(double my) {
        int mY = (this.height - 205) / 2;
        int vHueY = mY + 34;
        int vHueH = 100;

        float hue = (float) (my - vHueY) / (float) vHueH;
        hue = Mth.clamp(hue, 0.0f, 1.0f);

        if (this.activePickerTarget == 0) {
            this.settings.outlineHue = hue;
        } else if (this.activePickerTarget == 1) {
            this.settings.fillHue = hue;
        } else if (this.activePickerTarget == 2) {
            this.settings.fillHue2 = hue;
        }
        BlockyOutlineSettings.save();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        if (this.activePickerTarget != -1) {
            int mW = 240;
            int mH = 205;
            int mX = (this.width - mW) / 2;
            int mY = (this.height - mH) / 2;

            int closeBtnW = 16;
            int closeBtnH = 14;
            int closeBtnX = mX + mW - closeBtnW - 6;
            int closeBtnY = mY + 5;
            if (mx >= closeBtnX && mx <= closeBtnX + closeBtnW && my >= closeBtnY && my <= closeBtnY + closeBtnH) {
                this.activePickerTarget = -1;
                this.playClickSound();
                return true;
            }

            int doneBtnW = 60;
            int doneBtnH = 16;
            int doneBtnX = mX + mW - doneBtnW - 12;
            int doneBtnY = mY + mH - doneBtnH - 10;
            if (mx >= doneBtnX && mx <= doneBtnX + doneBtnW && my >= doneBtnY && my <= doneBtnY + doneBtnH) {
                this.activePickerTarget = -1;
                this.playClickSound();
                return true;
            }

            int boxX = mX + 12;
            int boxY = mY + 34;
            int boxW = 180;
            int boxH = 100;
            if (mx >= boxX && mx <= boxX + boxW && my >= boxY && my <= boxY + boxH) {
                this.isDragging2DBox = true;
                this.updateModal2DPicker(mx, my);
                return true;
            }

            int vHueX = boxX + boxW + 10;
            int vHueY = boxY;
            int vHueW = 16;
            int vHueH = boxH;
            if (mx >= vHueX && mx <= vHueX + vHueW && my >= vHueY && my <= vHueY + vHueH) {
                this.isDraggingPopupHue = true;
                this.updateModalHue(my);
                return true;
            }

            int bottomY = boxY + boxH + 12;
            int modalHexW = 54;
            int modalHexX = boxX + 18 + 8;
            int dotStartX = modalHexX + modalHexW + 8;
            int dotSize = 9;
            int dotGap = 3;
            int[] palette = {
                    0xFFFF0044, 0xFFFF7700, 0xFFFFDD00, 0xFF00DD44, 0xFF00CCFF, 0xFF3366FF, 0xFFA855F7, 0xFFFFFFFF
            };
            for (int i = 0; i < palette.length; ++i) {
                int dx = dotStartX + i * (dotSize + dotGap);
                int dy = bottomY + 4;
                if (mx >= dx && mx <= dx + dotSize && my >= dy && my <= dy + dotSize) {
                    int c = palette[i];
                    String hexStr = String.format("%06X", c & 0xFFFFFF);
                    this.applyHexColor(this.activePickerTarget, hexStr);
                    this.playClickSound();
                    return true;
                }
            }

            if (mx < mX || mx > mX + mW || my < mY || my > mY + mH) {
                this.activePickerTarget = -1;
                this.playClickSound();
                return true;
            }

            return true; // Eat click events inside modal backdrop
        }

        int logoX = this.px + (this.compactMode ? 8 : 14);
        int logoW;
        if (this.panelW < 360) {
            logoW = this.font.width("BO");
        } else {
            logoW = this.font.width("Blocky Outline");
        }

        String vTag = "v1.1.1";
        int vW = this.font.width(vTag);
        boolean showVTag = this.panelW >= 340;
        int vX = this.px + this.panelW - vW - (this.compactMode ? 8 : 12);

        int tabStartX = logoX + logoW + (this.compactMode ? 8 : 14);
        int tabRightLimit = showVTag ? (vX - 6) : (this.px + this.panelW - 8);
        int tabAvailableW = tabRightLimit - tabStartX;
        int gap = 2;
        int tabW = Math.max(34, (tabAvailableW - gap * 3) / 4);
        int tabH = this.compactMode ? 17 : 21;
        int tabY = this.py + (this.compactMode ? 5 : 8);

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
            int settingsW = this.getSettingsWidth();
            int currentY = contentY;
            int numRows = (col == 0) ? 6 : 7;

            for (int row = 0; row < numRows; ++row) {
                int height = this.getRowHeight(col, row);
                boolean disabled = this.isRowDisabled(col, row);

                if (!disabled && mx >= this.contentX && mx <= this.contentX + settingsW && my >= currentY && my <= currentY + height) {
                    if (this.isCheckboxRow(col, row)) {
                        int switchW = 34;
                        int switchH = 16;
                        int switchX = this.contentX + settingsW - switchW - 12;
                        int switchY = currentY + (this.rowH - switchH) / 2;

                        if (mx >= switchX - 6 && mx <= switchX + switchW + 6 && my >= switchY - 4 && my <= switchY + switchH + 4) {
                            this.toggleCheckbox(col, row);
                            this.playClickSound();
                            return true;
                        }
                    } else if ((col == 0 && row == 2) || (col == 1 && (row == 3 || row == 5))) {
                        int sliderX = this.contentX + settingsW - this.sliderW - 12;
                        int pSize = 15;
                        int pX = sliderX - pSize - 8;
                        int pY = currentY + (this.rowH - pSize) / 2;
                        int hexBoxW = 46;
                        int hexBoxH = 15;
                        int hexBoxX = pX - hexBoxW - 6;
                        int hexBoxY = currentY + (this.rowH - hexBoxH) / 2;

                        if (mx >= pX && mx <= pX + pSize && my >= pY && my <= pY + pSize) {
                            this.activePickerTarget = (col == 1 && row == 5) ? 2 : col;
                            this.focusedHexCol = -1;
                            this.playClickSound();
                            return true;
                        }

                        if (mx >= hexBoxX && mx <= hexBoxX + hexBoxW && my >= hexBoxY && my <= hexBoxY + hexBoxH) {
                            this.focusedHexCol = (col == 1 && row == 5) ? 2 : col;
                            this.typingHex = "";
                            return true;
                        }

                        if (mx >= sliderX - 4 && mx <= sliderX + this.sliderW + 4) {
                            this.isDraggingSlider = true;
                            this.dragCol = col;
                            this.dragRow = row;
                            this.updateSliderValue(col, row, mx, sliderX);
                            return true;
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

        int doneW = this.compactMode ? 65 : 85;
        int doneH = this.compactMode ? 18 : 22;
        int doneX = this.px + this.panelW - doneW - (this.compactMode ? 10 : 16);
        int doneY = this.py + this.panelH - doneH - (this.compactMode ? 6 : 10);
        if (mx >= doneX && mx <= doneX + doneW && my >= doneY && my <= doneY + doneH) {
            this.playClickSound();
            this.onClose();
            return true;
        }

        this.focusedHexCol = -1;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mx = event.x();
        double my = event.y();

        if (this.isDragging2DBox || this.isDraggingPopupHue) {
            this.isDragging2DBox = false;
            this.isDraggingPopupHue = false;
            BlockyOutlineSettings.save();
            return true;
        }

        if (this.isDraggingSlider) {
            this.isDraggingSlider = false;
            this.dragCol = -1;
            this.dragRow = -1;
            BlockyOutlineSettings.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mx = event.x();
        double my = event.y();

        if (this.activePickerTarget != -1) {
            if (this.isDragging2DBox) {
                this.updateModal2DPicker(mx, my);
                return true;
            }
            if (this.isDraggingPopupHue) {
                this.updateModalHue(my);
                return true;
            }
            return true;
        }

        if (this.isDraggingSlider && this.dragCol != -1 && this.dragRow != -1) {
            int settingsW = this.getSettingsWidth();
            int sliderX = this.contentX + settingsW - this.sliderW - 12;
            this.updateSliderValue(this.dragCol, this.dragRow, mx, sliderX);
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char c = (char) event.codepoint();

        if (this.focusedHexCol != -1) {
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
        int keyCode = event.key();

        if (keyCode == 256 && this.activePickerTarget != -1) { // GLFW_KEY_ESCAPE
            this.activePickerTarget = -1;
            return true;
        }

        if (this.focusedHexCol != -1) {
            if (keyCode == 259) { // Backspace
                if (!this.typingHex.isEmpty()) {
                    this.typingHex = this.typingHex.substring(0, this.typingHex.length() - 1);
                }
                return true;
            } else if (keyCode == 257 || keyCode == 335) { // Enter
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
            } else if (col == 1) {
                this.settings.fillHue = h;
                this.settings.fillSaturation = s;
                this.settings.fillValue = v;
            } else if (col == 2) {
                this.settings.fillHue2 = h;
                this.settings.fillSaturation2 = s;
                this.settings.fillValue2 = v;
            }
            BlockyOutlineSettings.save();
        } catch (NumberFormatException ignored) {}
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
        return row == 0 || row == 1 || row == 4;
    }

    private boolean getCheckboxValue(int col, int row) {
        if (col == 0) {
            if (row == 0) return this.settings.rainbowOutline;
            if (row == 5) return this.settings.smoothTransition;
        } else {
            if (row == 0) return this.settings.fillEnabled;
            if (row == 1) return this.settings.rainbowFill;
            if (row == 4) return this.settings.fillGradientEnabled;
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
            if (row == 4) this.settings.fillGradientEnabled = !this.settings.fillGradientEnabled;
        }
        BlockyOutlineSettings.save();
    }

    private boolean isRowDisabled(int col, int row) {
        if (col == 0) {
            if (this.settings.rainbowOutline && (row == 2)) return true;
            if (!this.settings.rainbowOutline && (row == 1)) return true;
        } else {
            if (!this.settings.fillEnabled && row > 0) return true;
            if (this.settings.rainbowFill && (row == 3 || row == 4 || row == 5)) return true;
            if (!this.settings.rainbowFill && row == 2) return true;
            if (!this.settings.fillGradientEnabled && row == 5) return true;
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
                case 5 -> this.settings.fillHue2;
                case 6 -> this.settings.fillOpacity;
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
                case 6 -> String.format("%d%%", (int)(this.settings.fillOpacity * 100));
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
                case 5 -> this.settings.fillHue2 = pct;
                case 6 -> this.settings.fillOpacity = pct;
            }
        }
        BlockyOutlineSettings.save();
    }
}
