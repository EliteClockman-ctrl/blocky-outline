package charaz.blockoutline.client.ui;

import charaz.blockoutline.config.BlockyOutlineSettings;
import net.minecraft.client.Minecraft;
import charaz.blockoutline.client.Compat26;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class BlockyOutlineMenuScreen extends Screen {

    private static float clamp(float val, float min, float max) {
        return val < min ? min : Math.min(val, max);
    }

    private static final String MOD_TITLE = "Blocky Outline";
    private static final String MOD_VERSION = "v1.1.6";

    // Navigation tabs
    private static final String[] TABS = {"Outline", "Face Fill", "Smooth", "Presets"};

    // Colors
    private static final int OVERLAY_BG = 0xB8060A12;
    private static final int WINDOW_BG = 0xF70C121E;
    private static final int WINDOW_BORDER = 0xFF1E2B3E;
    private static final int HEADER_BG = 0xF9101826;
    private static final int HEADER_BORDER = 0xFF1C2A3C;
    private static final int FOOTER_BG = 0xF9101826;

    // Cards
    private static final int CARD_BG = 0xEE141E2C;
    private static final int CARD_HOVER = 0xFF1C293D;
    private static final int CARD_BORDER = 0xFF223247;
    private static final int CARD_BORDER_ACTIVE = 0xFF3B82F6;

    // Accents
    private static final int PRIMARY_BLUE = 0xFF3B82F6;
    private static final int ACCENT_EMERALD = 0xFF10B981;
    private static final int ACCENT_RED = 0xFFDC2626;

    // Text
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_BODY = 0xFFE2E8F0;
    private static final int TEXT_MUTED = 0xFF94A3B8;
    private static final int TEXT_DISABLED = 0xFF64748B;

    // Hue palette
    private static final int[] HUE_COLORS = {
            0xFFFF0000, 0xFFFF4D00, 0xFFFF9900, 0xFFFFEE00,
            0xFF88FF00, 0xFF00FF44, 0xFF00FFAA, 0xFF00CCFF,
            0xFF0044FF, 0xFF7700FF, 0xFFFF00CC, 0xFFFF0044,
            0xFFFF0000
    };

    // Swatches
    private static final int[] QUICK_SWATCHES = {
            0xFF10B981, 0xFF06B6D4, 0xFF3B82F6, 0xFFA855F7,
            0xFFF59E0B, 0xFFEF4444, 0xFFFFFFFF, 0xFF475569
    };

    // Built-in Presets - Crisp, short, natural descriptions
    private static final String[] PRESET_NAMES = {
            "Cyber Cyan", "Silver", "Amethyst", "Solar Gold", "Chroma"
    };
    private static final String[] PRESET_DESCS = {
            "Cyan corners & glow",
            "Clean white lines",
            "Purple gradient fade",
            "Gold outline & glow",
            "Full rainbow cycle"
    };

    // Responsive Layout Dimensions
    private int winW;
    private int winH;
    private int winX;
    private int winY;

    // Active state
    private int activeTab = 0; // 0=Outline, 1=Face Fill, 2=Smooth, 3=Presets
    private String tooltip = null;

    // 3D Viewport state
    private float cubeYaw = 35.0f;
    private float cubePitch = 25.0f;
    private boolean autoRotate = true;
    private int previewShape = 0; // 0=Block, 1=Slab
    private boolean draggingCube = false;
    private double dragStartMouseX;
    private double dragStartMouseY;
    private float dragStartYaw;
    private float dragStartPitch;

    // Slider Dragging state
    private boolean draggingSlider = false;
    private int activeSliderTab = -1;
    private int activeSliderRow = -1;

    // Color Picker Modal
    private int pickerTarget = -1; // 0=Outline, 1=Fill1, 2=Fill2
    private boolean dragging2D = false;
    private boolean draggingHue = false;

    private final BlockyOutlineSettings settings = BlockyOutlineSettings.get();

    public BlockyOutlineMenuScreen() {
        super(Component.literal("Blocky Outline Settings"));
    }

    @Override
    protected void init() {
        super.init();
        // Dynamically auto-adjusts to any Minecraft resolution & GUI Scale (Auto, 1, 2, 3, 4)
        this.winW = Math.min((int) (this.width * 0.94f), 640);
        this.winH = Math.min((int) (this.height * 0.92f), 380);

        // Safeguard minimum responsive boundaries without window overflowing
        if (this.winW > this.width - 8) this.winW = Math.max(260, this.width - 8);
        if (this.winH > this.height - 8) this.winH = Math.max(180, this.height - 8);

        this.winX = (this.width - this.winW) / 2;
        this.winY = (this.height - this.winH) / 2;
    }

    private void playClick() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f)
        );
    }

    private static void drawBorder(GuiGraphicsExtractor gui, int x1, int y1, int x2, int y2, int color) {
        gui.fill(x1, y1, x2, y1 + 1, color);
        gui.fill(x1, y2 - 1, x2, y2, color);
        gui.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
        gui.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    private String trimText(String text, int maxWidth) {
        if (text == null || maxWidth <= 0) return "";
        if (this.font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int ellipsisW = this.font.width(ellipsis);
        if (maxWidth <= ellipsisW) return text.substring(0, Math.min(1, text.length()));
        while (text.length() > 1 && this.font.width(text) + ellipsisW > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipsis;
    }

    private void getRowBounds(int row, int totalRows, int cy, int ch, int[] outBounds) {
        int gap = (ch >= 240) ? 4 : (ch >= 180 ? 3 : 2);
        int availH = ch - (totalRows - 1) * gap;
        int cardH = Math.max(16, Math.min(38, availH / totalRows));
        outBounds[0] = cy + row * (cardH + gap);
        outBounds[1] = cardH;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        this.tooltip = null;

        // Auto rotate 3D cube preview
        if (this.autoRotate && !this.draggingCube) {
            this.cubeYaw = (this.cubeYaw + 0.6f) % 360.0f;
        }

        // Background overlay
        guiGraphics.fill(0, 0, this.width, this.height, OVERLAY_BG);

        // Main window
        guiGraphics.fill(this.winX, this.winY, this.winX + this.winW, this.winY + this.winH, WINDOW_BG);
        drawBorder(guiGraphics, this.winX, this.winY, this.winX + this.winW, this.winY + this.winH, WINDOW_BORDER);

        // Header
        int headerH = Math.max(26, Math.min(34, this.winH / 10));
        guiGraphics.fill(this.winX, this.winY, this.winX + this.winW, this.winY + headerH, HEADER_BG);
        guiGraphics.fill(this.winX, this.winY + headerH - 1, this.winX + this.winW, this.winY + headerH, PRIMARY_BLUE);

        // Title
        String titleStr = (this.winW >= 420) ? MOD_TITLE : "Blocky Outline";
        int titleY = this.winY + (headerH - 8) / 2;
        guiGraphics.text(this.font, titleStr, this.winX + 10, titleY, TEXT_WHITE, false);
        int brandW = this.font.width(titleStr);
        if (this.winW >= 540) {
            guiGraphics.text(this.font, MOD_VERSION, this.winX + 10 + brandW + 6, titleY + 1, TEXT_MUTED, false);
        }

        // Close '✕' Button at Top Right
        int closeW = Math.max(16, headerH - 12);
        int closeX = this.winX + this.winW - closeW - 8;
        int closeY = this.winY + (headerH - closeW) / 2;
        boolean closeHov = mouseX >= closeX && mouseX <= closeX + closeW && mouseY >= closeY && mouseY <= closeY + closeW;
        guiGraphics.fill(closeX, closeY, closeX + closeW, closeY + closeW, closeHov ? 0xFFDC2626 : 0xFF1E2838);
        drawBorder(guiGraphics, closeX, closeY, closeX + closeW, closeY + closeW, closeHov ? ACCENT_RED : CARD_BORDER);
        guiGraphics.centeredText(this.font, "✕", closeX + closeW / 2, closeY + (closeW - 8) / 2, closeHov ? TEXT_WHITE : TEXT_MUTED);

        // Navigation tabs
        int brandEndX = this.winX + 10 + brandW + 8;
        int maxTabsW = closeX - brandEndX - 8;
        int tabW = Math.max(34, Math.min(62, (maxTabsW - (TABS.length - 1) * 4) / TABS.length));
        int tabH = Math.max(16, headerH - 10);
        int totalTabsW = TABS.length * tabW + (TABS.length - 1) * 4;
        int tabStartX = brandEndX + Math.max(0, (maxTabsW - totalTabsW) / 2);
        int tabY = this.winY + (headerH - tabH) / 2;

        for (int i = 0; i < TABS.length; i++) {
            int tx = tabStartX + i * (tabW + 4);
            boolean selected = (this.activeTab == i);
            boolean hov = mouseX >= tx && mouseX <= tx + tabW && mouseY >= tabY && mouseY <= tabY + tabH;

            int bg = selected ? PRIMARY_BLUE : (hov ? 0xFF24344B : 0xFF141C28);
            guiGraphics.fill(tx, tabY, tx + tabW, tabY + tabH, bg);
            drawBorder(guiGraphics, tx, tabY, tx + tabW, tabY + tabH, selected ? 0xFF93C5FD : CARD_BORDER);
            guiGraphics.centeredText(this.font, TABS[i], tx + tabW / 2, tabY + (tabH - 8) / 2, selected ? TEXT_WHITE : (hov ? TEXT_WHITE : TEXT_MUTED));
        }

        // Content area
        int footerH = Math.max(24, Math.min(30, this.winH / 11));
        int contentY = this.winY + headerH + 6;
        int contentH = this.winH - headerH - footerH - 12;

        boolean showPreview = (this.activeTab != 3) && (this.winW >= 480) && (this.winH >= 210);
        int col1X = this.winX + 10;
        int col1W = showPreview ? (int) ((this.winW - 28) * 0.58f) : (this.winW - 20);

        if (this.activeTab == 0) {
            this.renderOutlineCards(guiGraphics, col1X, contentY, col1W, contentH, mouseX, mouseY);
        } else if (this.activeTab == 1) {
            this.renderFaceFillCards(guiGraphics, col1X, contentY, col1W, contentH, mouseX, mouseY);
        } else if (this.activeTab == 2) {
            this.renderSmoothCards(guiGraphics, col1X, contentY, col1W, contentH, mouseX, mouseY);
        } else if (this.activeTab == 3) {
            this.renderPresetsTab(guiGraphics, col1X, contentY, col1W, contentH, mouseX, mouseY);
        }

        if (showPreview) {
            int col2X = col1X + col1W + 8;
            int col2W = this.winX + this.winW - 10 - col2X;
            this.renderPreviewColumn(guiGraphics, col2X, contentY, col2W, contentH, mouseX, mouseY);
        }

        // Footer
        int footerY = this.winY + this.winH - footerH;
        guiGraphics.fill(this.winX, footerY, this.winX + this.winW, this.winY + this.winH, FOOTER_BG);
        guiGraphics.fill(this.winX, footerY, this.winX + this.winW, footerY + 1, HEADER_BORDER);

        // Shortcut hint
        int hintY = footerY + (footerH - 8) / 2;
        if (this.winW >= 360) {
            guiGraphics.text(this.font, "[M] or [ESC] to save & close", this.winX + 12, hintY, TEXT_MUTED, false);
        }

        // Done Button (Right bottom)
        int doneW = Math.max(50, Math.min(68, this.winW / 7));
        int doneH = Math.max(16, footerH - 10);
        int doneX = this.winX + this.winW - doneW - 10;
        int doneY = footerY + (footerH - doneH) / 2;
        boolean doneHov = mouseX >= doneX && mouseX <= doneX + doneW && mouseY >= doneY && mouseY <= doneY + doneH;
        guiGraphics.fill(doneX, doneY, doneX + doneW, doneY + doneH, doneHov ? 0xFF2563EB : PRIMARY_BLUE);
        drawBorder(guiGraphics, doneX, doneY, doneX + doneW, doneY + doneH, doneHov ? 0xFF93C5FD : 0xFF1D4ED8);
        guiGraphics.centeredText(this.font, "Done", doneX + doneW / 2, doneY + (doneH - 8) / 2, TEXT_WHITE);

        // Report Bug Button (Left of Done)
        int reportW = Math.max(68, Math.min(84, this.winW / 6));
        int reportH = doneH;
        int reportX = doneX - reportW - 6;
        int reportY = doneY;
        boolean reportHov = mouseX >= reportX && mouseX <= reportX + reportW && mouseY >= reportY && mouseY <= reportY + reportH;
        guiGraphics.fill(reportX, reportY, reportX + reportW, reportY + reportH, reportHov ? 0xFF0D9488 : 0xFF0F766E);
        drawBorder(guiGraphics, reportX, reportY, reportX + reportW, reportY + reportH, reportHov ? 0xFF5EEAD4 : 0xFF115E59);
        guiGraphics.centeredText(this.font, "Report Bug", reportX + reportW / 2, reportY + (reportH - 8) / 2, TEXT_WHITE);
        if (reportHov) {
            this.tooltip = "Click to open GitHub Issues & report bugs";
        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);

        // 6. Color Picker Modal Popover
        if (this.pickerTarget != -1) {
            this.renderPickerModal(guiGraphics, mouseX, mouseY);
        }

        // 7. Tooltip Popover
        if (this.tooltip != null) {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    // =========================================================================
    // TAB 0: OUTLINE SETTINGS CARDS
    // =========================================================================
    private void renderOutlineCards(GuiGraphicsExtractor gui, int cx, int cy, int cw, int ch, int mx, int my) {
        int[] b = new int[2];
        int totalRows = 8;

        // Row 0: Rainbow Outline Toggle
        this.getRowBounds(0, totalRows, cy, ch, b);
        this.renderCardToggle(gui, cx, b[0], cw, b[1], "Rainbow Outline", "Cycle colors automatically",
                this.settings.rainbowOutline, false, mx, my);

        // Row 1: RGB Wave Speed Slider (if rainbow) OR Outline Color Swatch (if static)
        this.getRowBounds(1, totalRows, cy, ch, b);
        if (this.settings.rainbowOutline) {
            float speedPct = (this.settings.outlineRgbSpeed - 0.1f) / 4.9f;
            String speedStr = String.format("%.1fx", this.settings.outlineRgbSpeed);
            this.renderCardSlider(gui, cx, b[0], cw, b[1], "Cycle Speed", "Speed of color cycle",
                    speedPct, speedStr, false, mx, my);
        } else {
            int curArgb = this.settings.getOutlineArgb(System.currentTimeMillis());
            this.renderCardColor(gui, cx, b[0], cw, b[1], "Outline Color", "Wireframe color",
                    curArgb, false, "", mx, my);
        }

        // Row 2: Corner Accents Mode
        this.getRowBounds(2, totalRows, cy, ch, b);
        this.renderCardToggle(gui, cx, b[0], cw, b[1], "Corner Mode", "Draw corners only",
                this.settings.cornerMode, false, mx, my);

        // Row 3: Corner Bracket Size Slider
        this.getRowBounds(3, totalRows, cy, ch, b);
        float cLenPct = (this.settings.cornerLength - 0.10f) / 0.38f;
        String cLenStr = String.format("%d%%", (int) (this.settings.cornerLength * 100));
        this.renderCardSlider(gui, cx, b[0], cw, b[1], "Corner Size", "Size of corner brackets",
                cLenPct, cLenStr, !this.settings.cornerMode, mx, my);

        // Row 4: Depth Test / Through Walls Mode (X-Ray)
        this.getRowBounds(4, totalRows, cy, ch, b);
        this.renderCardToggle(gui, cx, b[0], cw, b[1], "X-Ray Mode", "Visible through blocks",
                this.settings.xrayMode, false, mx, my);

        // Row 5: Hitbox Floating Offset (Expand) Slider
        this.getRowBounds(5, totalRows, cy, ch, b);
        float expPct = this.settings.hitboxExpand / 0.030f;
        String expStr = String.format("+%.3f", this.settings.hitboxExpand);
        this.renderCardSlider(gui, cx, b[0], cw, b[1], "Box Offset", "Expand outline box",
                expPct, expStr, false, mx, my);

        // Row 6: Line Thickness Slider
        this.getRowBounds(6, totalRows, cy, ch, b);
        float widthPct = (this.settings.outlineWidth - 0.5f) / 9.5f;
        String widthStr = String.format("%.1f px", this.settings.outlineWidth);
        this.renderCardSlider(gui, cx, b[0], cw, b[1], "Line Width", "Outline thickness",
                widthPct, widthStr, false, mx, my);

        // Row 7: Line Opacity Slider
        this.getRowBounds(7, totalRows, cy, ch, b);
        String opacStr = String.format("%d%%", (int) (this.settings.outlineOpacity * 100));
        this.renderCardSlider(gui, cx, b[0], cw, b[1], "Opacity", "Outline opacity",
                this.settings.outlineOpacity, opacStr, false, mx, my);
    }

    // =========================================================================
    // TAB 1: FACE FILL SETTINGS CARDS
    // =========================================================================
    private void renderFaceFillCards(GuiGraphicsExtractor gui, int cx, int cy, int cw, int ch, int mx, int my) {
        int[] b = new int[2];
        int totalRows = 6;

        // Row 0: Enable Fill Toggle
        this.getRowBounds(0, totalRows, cy, ch, b);
        this.renderCardToggle(gui, cx, b[0], cw, b[1], "Block Fill", "Fill block faces",
                this.settings.fillEnabled, false, mx, my);

        // Row 1: Rainbow Chroma Fill Toggle
        this.getRowBounds(1, totalRows, cy, ch, b);
        this.renderCardToggle(gui, cx, b[0], cw, b[1], "Rainbow Fill", "Cycle face colors",
                this.settings.rainbowFill, !this.settings.fillEnabled, mx, my);

        // Row 2: Color 1 (Top Face)
        this.getRowBounds(2, totalRows, cy, ch, b);
        float[] c1 = BlockyOutlineSettings.hsvToRgb(this.settings.fillHue, this.settings.fillSaturation, this.settings.fillValue);
        int col1Argb = 0xFF000000 | ((int) (c1[0] * 255.0f) << 16) | ((int) (c1[1] * 255.0f) << 8) | (int) (c1[2] * 255.0f);
        boolean col1Disabled = !this.settings.fillEnabled;
        String col1Desc = this.settings.rainbowFill ? "Primary color (rainbow active)" : "Top face color";
        this.renderCardColor(gui, cx, b[0], cw, b[1], "Top Color", col1Desc,
                col1Argb, col1Disabled, "Disabled (Turn Fill ON)", mx, my);

        // Row 3: Enable Color 2 (Gradient) Toggle - STRICT RULE ENFORCEMENT
        this.getRowBounds(3, totalRows, cy, ch, b);
        boolean col2ToggleDisabled = !this.settings.fillEnabled || this.settings.rainbowFill;
        this.renderCardToggle(gui, cx, b[0], cw, b[1], "Gradient",
                this.settings.rainbowFill ? "Locked (rainbow active)" : "Top to bottom color fade",
                !this.settings.rainbowFill && this.settings.fillGradientEnabled, col2ToggleDisabled, mx, my);

        // Row 4: Color 2 (Bottom Face)
        this.getRowBounds(4, totalRows, cy, ch, b);
        float[] c2 = BlockyOutlineSettings.hsvToRgb(this.settings.fillHue2, this.settings.fillSaturation2, this.settings.fillValue2);
        int col2Argb = 0xFF000000 | ((int) (c2[0] * 255.0f) << 16) | ((int) (c2[1] * 255.0f) << 8) | (int) (c2[2] * 255.0f);
        boolean col2Disabled = col2ToggleDisabled || !this.settings.fillGradientEnabled;
        String lockMsg = this.settings.rainbowFill ? "Locked (rainbow active)" : "Disabled (Turn Gradient ON)";
        this.renderCardColor(gui, cx, b[0], cw, b[1], "Bottom Color", "Bottom face color",
                col2Argb, col2Disabled, lockMsg, mx, my);

        // Row 5: Fill Opacity Slider
        this.getRowBounds(5, totalRows, cy, ch, b);
        String opacStr = String.format("%d%%", (int) (this.settings.fillOpacity * 100));
        this.renderCardSlider(gui, cx, b[0], cw, b[1], "Fill Opacity", "Face transparency",
                this.settings.fillOpacity, opacStr, !this.settings.fillEnabled, mx, my);
    }

    // =========================================================================
    // TAB 2: SMOOTH MOVEMENT & ADVANCED PHYSICS ENGINES
    // =========================================================================
    private void renderSmoothCards(GuiGraphicsExtractor gui, int cx, int cy, int cw, int ch, int mx, int my) {
        int[] b = new int[2];
        int totalRows = 5;

        // Row 0: Smooth Movement Toggle
        this.getRowBounds(0, totalRows, cy, ch, b);
        this.renderCardToggle(gui, cx, b[0], cw, b[1], "Smooth Move", "Glide between blocks",
                this.settings.smoothTransition, false, mx, my);

        // Row 1: Motion Engine / Easing Curve Selector
        this.getRowBounds(1, totalRows, cy, ch, b);
        int mode = this.settings.motionMode;
        String modeName = (mode >= 0 && mode < BlockyOutlineSettings.MOTION_MODE_NAMES.length)
                ? BlockyOutlineSettings.MOTION_MODE_NAMES[mode] : "Spring Physics";
        String modeDesc = (mode >= 0 && mode < BlockyOutlineSettings.MOTION_MODE_DESCS.length)
                ? BlockyOutlineSettings.MOTION_MODE_DESCS[mode] : "Physical spring";
        this.renderCardChoice(gui, cx, b[0], cw, b[1], "Motion Curve", modeDesc,
                modeName, !this.settings.smoothTransition, mx, my);

        // Row 2: Dynamic Parameter Slider based on active Motion Engine
        this.getRowBounds(2, totalRows, cy, ch, b);
        float paramPct;
        String paramStr;
        String paramTitle;
        String paramDesc;

        boolean disabledSlider = !this.settings.smoothTransition;
        switch (mode) {
            case 0 -> { // Snappy Spring
                paramPct = (this.settings.springStiffness - 150.0f) / 350.0f;
                paramStr = String.format("%.0fk", this.settings.springStiffness);
                paramTitle = "Spring Tension";
                paramDesc = "Snap stiffness";
            }
            case 1 -> { // Ease-Out Back (Overshoot)
                paramPct = (this.settings.backOvershoot - 0.6f) / 1.4f;
                paramStr = String.format("%.2fx", this.settings.backOvershoot);
                paramTitle = "Overshoot Push";
                paramDesc = "1-bounce intensity";
            }
            case 2, 3 -> { // Quintic, Fluid Bezier
                paramPct = (this.settings.motionDurationMs - 35.0f) / 95.0f;
                paramStr = String.format("%dms", (int) (this.settings.motionDurationMs + 0.5f));
                paramTitle = "Glide Duration";
                paramDesc = "Transition length";
            }
            case 4 -> { // Smooth Lerp
                paramPct = this.settings.smoothSpeed;
                paramStr = String.format("%d%%", (int) (this.settings.smoothSpeed * 100 + 0.5f));
                paramTitle = "Glide Speed";
                paramDesc = "Movement speed";
            }
            default -> { // Instant (Raw)
                paramPct = 1.0f;
                paramStr = "0ms";
                paramTitle = "Instant Snap";
                paramDesc = "Raw 0ms latency";
                disabledSlider = true;
            }
        }
        this.renderCardSlider(gui, cx, b[0], cw, b[1], paramTitle, paramDesc,
                paramPct, paramStr, disabledSlider, mx, my);

        // Row 3: Breathing Pulse Glow Toggle
        this.getRowBounds(3, totalRows, cy, ch, b);
        this.renderCardToggle(gui, cx, b[0], cw, b[1], "Pulse Glow", "Rhythmic pulse effect",
                this.settings.breathingEffect, false, mx, my);

        // Row 4: Breathing Wave Speed Slider
        this.getRowBounds(4, totalRows, cy, ch, b);
        float breathPct = (this.settings.breathingSpeed - 0.2f) / 2.8f;
        String breathStr = String.format("%.1fx", this.settings.breathingSpeed);
        this.renderCardSlider(gui, cx, b[0], cw, b[1], "Pulse Speed", "Speed of pulse",
                breathPct, breathStr, !this.settings.breathingEffect, mx, my);
    }

    // =========================================================================
    // RIGHT COLUMN: CLEAN 3D PREVIEW & TOOLBAR
    // =========================================================================
    private void renderPreviewColumn(GuiGraphicsExtractor gui, int px, int py, int pw, int ph, int mx, int my) {
        // Container Card
        gui.fill(px, py, px + pw, py + ph, CARD_BG);
        drawBorder(gui, px, py, px + pw, py + ph, CARD_BORDER);

        // Header Title
        gui.text(this.font, "3D Preview", px + 10, py + 8, TEXT_WHITE, false);
        gui.text(this.font, (this.activeTab == 2) ? "Motion preview" : "Drag to rotate", px + 10, py + 18, TEXT_MUTED, false);

        // X-Ray status chip badge in preview
        if (this.settings.xrayMode) {
            int tagW = 54;
            int tagH = 12;
            int tagX = px + pw - tagW - 8;
            int tagY = py + 8;
            gui.fill(tagX, tagY, tagX + tagW, tagY + tagH, 0x442563EB);
            drawBorder(gui, tagX, tagY, tagX + tagW, tagY + tagH, PRIMARY_BLUE);
            gui.centeredText(this.font, "👁 X-RAY", tagX + tagW / 2, tagY + 2, 0xFF93C5FD);
        }

        // Viewport bounds
        int vpY = py + 28;
        int vpH = ph - 28 - 26;
        int cx = px + pw / 2;
        int cy = vpY + vpH / 2;
        float scale = Math.min(pw, vpH) * (0.35f + this.settings.hitboxExpand * 1.5f);

        // Live preview of active motion engine in Tab 2
        float demoShiftX = 0.0f;
        if (this.activeTab == 2 && this.settings.smoothTransition) {
            long nowTime = System.currentTimeMillis();
            long cycleMs = 1000;
            long phase = nowTime % cycleMs;
            float dir = ((nowTime / cycleMs) % 2 == 0) ? -1.0f : 1.0f;
            float from = -dir * 0.22f;
            float to = dir * 0.22f;
            float p = Math.min(1.0f, (float) phase / 200.0f);
            float ease;
            switch (this.settings.motionMode) {
                case 0 -> {
                    float t = phase * 0.012f;
                    ease = (p >= 1.0f) ? 1.0f : (1.0f - (float) Math.exp(-t * 4.5f) * (float) Math.cos(t * 14.0f));
                }
                case 1 -> {
                    if (p >= 1.0f) ease = 1.0f;
                    else {
                        float p1 = p - 1.0f;
                        float s = this.settings.backOvershoot;
                        ease = 1.0f + (s + 1.0f) * p1 * p1 * p1 + s * p1 * p1;
                    }
                }
                case 2 -> {
                    if (p >= 1.0f) ease = 1.0f;
                    else {
                        float p1 = 1.0f - p;
                        ease = 1.0f - p1 * p1 * p1 * p1 * p1;
                    }
                }
                case 3 -> {
                    float t = p;
                    ease = (p >= 1.0f) ? 1.0f : (3.0f * (1.0f - t) * (1.0f - t) * t * 0.16f + 3.0f * (1.0f - t) * t * t * 1.0f + t * t * t);
                }
                case 4 -> {
                    ease = (p >= 1.0f) ? 1.0f : (1.0f - (float) Math.exp(-p * 6.0f));
                }
                default -> {
                    ease = 1.0f;
                }
            }
            demoShiftX = (from + (to - from) * ease) * scale;
        }

        // Shape vertices
        float yTop = (this.previewShape == 1) ? 0.0f : 1.0f; // Half slab vs full block
        float yBot = -1.0f;
        float[][] localPts = {
                {-1.0f, yBot, -1.0f}, { 1.0f, yBot, -1.0f}, { 1.0f, yTop, -1.0f}, {-1.0f, yTop, -1.0f},
                {-1.0f, yBot,  1.0f}, { 1.0f, yBot,  1.0f}, { 1.0f, yTop,  1.0f}, {-1.0f, yTop,  1.0f}
        };

        float radY = (float) Math.toRadians(this.cubeYaw);
        float radX = (float) Math.toRadians(this.cubePitch);
        float cosY = (float) Math.cos(radY), sinY = (float) Math.sin(radY);
        float cosX = (float) Math.cos(radX), sinX = (float) Math.sin(radX);

        float[][] v = new float[8][3];
        for (int i = 0; i < 8; i++) {
            float ox = localPts[i][0];
            float oy = localPts[i][1];
            float oz = localPts[i][2];

            float rx = ox * cosY + oz * sinY;
            float ry = oy;
            float rz = -ox * sinY + oz * cosY;

            float py2 = ry * cosX - rz * sinX;
            float pz2 = ry * sinX + rz * cosX;

            v[i][0] = cx + rx * scale + demoShiftX;
            v[i][1] = cy - py2 * scale;
            v[i][2] = pz2;
        }

        long now = System.currentTimeMillis();
        int outlineCol = this.settings.getOutlineArgb(now);
        int fillCol1 = this.settings.getFillArgb(now);
        int fillCol2 = this.settings.getFillArgb2(now);

        // Draw 6 Shaded Faces with Backface Culling
        if (this.settings.fillEnabled) {
            int[][] faces = {
                    {0, 1, 2, 3}, // North
                    {5, 4, 7, 6}, // South
                    {4, 0, 3, 7}, // West
                    {1, 5, 6, 2}, // East
                    {3, 2, 6, 7}, // Top
                    {4, 5, 1, 0}  // Bottom
            };

            for (int[] f : faces) {
                float ax = v[f[0]][0], ay = v[f[0]][1];
                float bx = v[f[1]][0], by = v[f[1]][1];
                float cx2 = v[f[2]][0], cy2 = v[f[2]][1];
                float crossZ = (bx - ax) * (cy2 - ay) - (by - ay) * (cx2 - ax);
                if (crossZ > 0) {
                    this.drawConvexQuad(gui, v[f[0]], v[f[1]], v[f[2]], v[f[3]], fillCol1, fillCol2);
                }
            }
        }

        // Draw 12 Wireframe Edges (Full vs Corner Accents)
        int[][] edges = {
                {0,1}, {1,2}, {2,3}, {3,0},
                {4,5}, {5,6}, {6,7}, {7,4},
                {0,4}, {1,5}, {2,6}, {3,7}
        };
        float strokeW = Math.max(1.0f, Math.min(5.0f, this.settings.outlineWidth));
        float cLen = this.settings.cornerLength;
        for (int[] e : edges) {
            float x1 = v[e[0]][0], y1 = v[e[0]][1];
            float x2 = v[e[1]][0], y2 = v[e[1]][1];
            if (this.settings.cornerMode) {
                float mx1 = x1 + (x2 - x1) * cLen;
                float my1 = y1 + (y2 - y1) * cLen;
                float mx2 = x2 - (x2 - x1) * cLen;
                float my2 = y2 - (y2 - y1) * cLen;
                this.drawLine(gui, x1, y1, mx1, my1, outlineCol, strokeW);
                this.drawLine(gui, mx2, my2, x2, y2, outlineCol, strokeW);
            } else {
                this.drawLine(gui, x1, y1, x2, y2, outlineCol, strokeW);
            }
        }

        // Bottom Controls Toolbar inside Preview Card
        int tbH = 20;
        int tbY = py + ph - tbH - 6;
        int tbW = pw - 12;
        int tbX = px + 6;
        int btnW = tbW / 3;

        // Button 1: Auto Spin
        boolean b1Hov = mx >= tbX && mx <= tbX + btnW && my >= tbY && my <= tbY + tbH;
        gui.fill(tbX, tbY, tbX + btnW, tbY + tbH, this.autoRotate ? 0xFF1E3A5F : (b1Hov ? 0xFF1F2937 : 0xFF111827));
        drawBorder(gui, tbX, tbY, tbX + btnW, tbY + tbH, this.autoRotate ? PRIMARY_BLUE : CARD_BORDER);
        gui.centeredText(this.font, this.autoRotate ? "⟳ Spin" : "⏸ Pause", tbX + btnW / 2, tbY + (tbH - 8) / 2, this.autoRotate ? TEXT_WHITE : TEXT_MUTED);

        // Button 2: Shape Switch
        int b2X = tbX + btnW;
        boolean b2Hov = mx >= b2X && mx <= b2X + btnW && my >= tbY && my <= tbY + tbH;
        gui.fill(b2X, tbY, b2X + btnW, tbY + tbH, b2Hov ? 0xFF1F2937 : 0xFF111827);
        drawBorder(gui, b2X, tbY, b2X + btnW, tbY + tbH, CARD_BORDER);
        gui.centeredText(this.font, (this.previewShape == 0) ? "Cube" : "Slab", b2X + btnW / 2, tbY + (tbH - 8) / 2, TEXT_BODY);

        // Button 3: Reset View
        int b3X = tbX + btnW * 2;
        boolean b3Hov = mx >= b3X && mx <= tbX + tbW && my >= tbY && my <= tbY + tbH;
        gui.fill(b3X, tbY, tbX + tbW, tbY + tbH, b3Hov ? 0xFF1F2937 : 0xFF111827);
        drawBorder(gui, b3X, tbY, tbX + tbW, tbY + tbH, CARD_BORDER);
        gui.centeredText(this.font, "⌂ Reset", b3X + (tbX + tbW - b3X) / 2, tbY + (tbH - 8) / 2, TEXT_BODY);
    }

    // =========================================================================
    // TAB 3: PRESETS TAB & REFINED CUSTOM USER SLOTS WITH DELETE BUTTON
    // =========================================================================
    private void renderPresetsTab(GuiGraphicsExtractor gui, int cx, int cy, int cw, int ch, int mx, int my) {
        int halfW = (cw - 10) / 2;

        // Left Column: 5 Built-in Curated Themes
        gui.fill(cx, cy, cx + halfW, cy + ch, CARD_BG);
        drawBorder(gui, cx, cy, cx + halfW, cy + ch, CARD_BORDER);
        gui.text(this.font, "Themes", cx + 10, cy + 8, TEXT_WHITE, false);

        int curY = cy + 22;
        int tGap = (ch >= 220) ? 4 : 2;
        int tCardH = Math.max(20, (ch - 28 - (PRESET_NAMES.length - 1) * tGap) / PRESET_NAMES.length);

        int[] presetDotColors = {0xFF10B981, 0xFF94A3B8, 0xFFA855F7, 0xFFF59E0B, 0xFFEC4899};

        for (int i = 0; i < PRESET_NAMES.length; i++) {
            boolean hov = mx >= cx + 6 && mx <= cx + halfW - 6 && my >= curY && my <= curY + tCardH;
            gui.fill(cx + 6, curY, cx + halfW - 6, curY + tCardH, hov ? 0xFF1E2D42 : 0xFF111A24);
            drawBorder(gui, cx + 6, curY, cx + halfW - 6, curY + tCardH, hov ? PRIMARY_BLUE : CARD_BORDER);

            int dotY = curY + (tCardH - 6) / 2;
            gui.fill(cx + 10, dotY, cx + 16, dotY + 6, presetDotColors[i]);

            int btnW = 38;
            int btnH = Math.max(14, tCardH - 6);
            int btnX = cx + halfW - btnW - 10;
            int btnY = curY + (tCardH - btnH) / 2;

            int maxTextW = btnX - (cx + 20) - 4;
            if (tCardH < 25) {
                gui.text(this.font, trimText(PRESET_NAMES[i], maxTextW), cx + 20, curY + (tCardH - 8) / 2, TEXT_WHITE, false);
            } else {
                gui.text(this.font, trimText(PRESET_NAMES[i], maxTextW), cx + 20, curY + 3, TEXT_WHITE, false);
                gui.text(this.font, trimText(PRESET_DESCS[i], maxTextW), cx + 20, curY + 13, TEXT_MUTED, false);
            }

            boolean bHov = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
            gui.fill(btnX, btnY, btnX + btnW, btnY + btnH, bHov ? 0xFF1D4ED8 : PRIMARY_BLUE);
            drawBorder(gui, btnX, btnY, btnX + btnW, btnY + btnH, bHov ? 0xFF93C5FD : 0xFF1E40AF);
            gui.centeredText(this.font, "Apply", btnX + btnW / 2, btnY + (btnH - 8) / 2, TEXT_WHITE);

            curY += tCardH + tGap;
        }

        // Right Column: 3 Custom User Slots with [Load] [Save] [Del]
        int rightX = cx + halfW + 10;
        gui.fill(rightX, cy, rightX + halfW, cy + ch, CARD_BG);
        drawBorder(gui, rightX, cy, rightX + halfW, cy + ch, CARD_BORDER);
        gui.text(this.font, "Saved Profiles", rightX + 10, cy + 8, TEXT_WHITE, false);

        int uY = cy + 22;
        int uGap = (ch >= 220) ? 6 : 4;
        int uCardH = Math.max(34, (ch - 28 - 2 * uGap) / 3);
        BlockyOutlineSettings.UserPreset[] slots = {
                this.settings.userPreset1, this.settings.userPreset2, this.settings.userPreset3
        };

        for (int i = 0; i < slots.length; i++) {
            BlockyOutlineSettings.UserPreset slot = slots[i];
            gui.fill(rightX + 6, uY, rightX + halfW - 6, uY + uCardH, 0xFF111A24);
            drawBorder(gui, rightX + 6, uY, rightX + halfW - 6, uY + uCardH, CARD_BORDER);

            int btnW = 30;
            int btnH = Math.max(14, Math.min(18, uCardH - 12));
            int delX = rightX + halfW - btnW - 8;
            int saveX = delX - btnW - 4;
            int loadX = saveX - btnW - 4;
            int bY = uY + (uCardH - btnH) / 2;

            int maxTextW = loadX - (rightX + 10) - 4;
            if (uCardH < 40) {
                gui.text(this.font, trimText("Profile " + (i + 1), maxTextW), rightX + 10, uY + (uCardH - 8) / 2, TEXT_WHITE, false);
            } else {
                int textY = uY + (uCardH - 22) / 2;
                gui.text(this.font, "Profile " + (i + 1), rightX + 10, textY, TEXT_WHITE, false);

                int chipW = slot.hasData ? 42 : 36;
                int chipH = 10;
                int chipX = rightX + 10;
                int chipY = textY + 12;
                int chipBg = slot.hasData ? 0x2210B981 : 0x22475569;
                int chipBorder = slot.hasData ? 0xFF059669 : 0xFF334155;
                gui.fill(chipX, chipY, chipX + chipW, chipY + chipH, chipBg);
                drawBorder(gui, chipX, chipY, chipX + chipW, chipY + chipH, chipBorder);
                gui.centeredText(this.font, slot.hasData ? "SAVED" : "EMPTY", chipX + chipW / 2, chipY + 1, slot.hasData ? 0xFF6EE7B7 : 0xFF94A3B8);
            }

            // 1. Load Button
            boolean lHov = slot.hasData && mx >= loadX && mx <= loadX + btnW && my >= bY && my <= bY + btnH;
            int lBg = !slot.hasData ? 0xFF141D28 : (lHov ? 0xFF059669 : ACCENT_EMERALD);
            gui.fill(loadX, bY, loadX + btnW, bY + btnH, lBg);
            drawBorder(gui, loadX, bY, loadX + btnW, bY + btnH, slot.hasData ? (lHov ? 0xFF6EE7B7 : 0xFF047857) : 0xFF1E293B);
            gui.centeredText(this.font, "Load", loadX + btnW / 2, bY + (btnH - 8) / 2, slot.hasData ? TEXT_WHITE : TEXT_DISABLED);

            // 2. Save Button
            boolean sHov = mx >= saveX && mx <= saveX + btnW && my >= bY && my <= bY + btnH;
            gui.fill(saveX, bY, saveX + btnW, bY + btnH, sHov ? 0xFF1D4ED8 : PRIMARY_BLUE);
            drawBorder(gui, saveX, bY, saveX + btnW, bY + btnH, sHov ? 0xFF93C5FD : 0xFF1E40AF);
            gui.centeredText(this.font, "Save", saveX + btnW / 2, bY + (btnH - 8) / 2, TEXT_WHITE);

            // 3. Delete Button (Del)
            boolean dHov = slot.hasData && mx >= delX && mx <= delX + btnW && my >= bY && my <= bY + btnH;
            int dBg = !slot.hasData ? 0xFF141D28 : (dHov ? 0xFFB91C1C : ACCENT_RED);
            gui.fill(delX, bY, delX + btnW, bY + btnH, dBg);
            drawBorder(gui, delX, bY, delX + btnW, bY + btnH, slot.hasData ? (dHov ? 0xFFFCA5A5 : 0xFF991B1B) : 0xFF1E293B);
            gui.centeredText(this.font, "Del", delX + btnW / 2, bY + (btnH - 8) / 2, slot.hasData ? TEXT_WHITE : TEXT_DISABLED);

            uY += uCardH + uGap;
        }
    }

    // =========================================================================
    // REUSABLE CARD COMPONENTS (TOGGLE, SLIDER, COLOR, CHOICE)
    // =========================================================================
    private void renderCardChoice(GuiGraphicsExtractor gui, int x, int y, int w, int h, String title, String desc, String choiceStr, boolean disabled, int mx, int my) {
        boolean hov = !disabled && mx >= x && mx <= x + w && my >= y && my <= y + h;
        gui.fill(x, y, x + w, y + h, hov ? CARD_HOVER : CARD_BG);
        drawBorder(gui, x, y, x + w, y + h, hov ? CARD_BORDER_ACTIVE : CARD_BORDER);

        int btnW = Math.max(120, Math.min(150, w / 2));
        int btnH = Math.max(14, Math.min(20, h - 8));
        int btnX = x + w - btnW - 8;
        int btnY = y + (h - btnH) / 2;

        int maxTextW = btnX - (x + 10) - 6;
        int titleCol = disabled ? TEXT_DISABLED : TEXT_WHITE;
        int descCol = disabled ? 0xFF475569 : TEXT_MUTED;

        if (h < 25) {
            gui.text(this.font, trimText(title, maxTextW), x + 10, y + (h - 8) / 2, titleCol, false);
        } else {
            gui.text(this.font, trimText(title, maxTextW), x + 10, y + 3, titleCol, false);
            gui.text(this.font, trimText(desc, maxTextW), x + 10, y + 13, descCol, false);
        }

        boolean btnHov = !disabled && mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
        gui.fill(btnX, btnY, btnX + btnW, btnY + btnH, disabled ? 0xFF131D28 : (btnHov ? 0xFF2563EB : 0xFF141E2C));
        drawBorder(gui, btnX, btnY, btnX + btnW, btnY + btnH, disabled ? 0xFF162230 : (btnHov ? 0xFF93C5FD : PRIMARY_BLUE));
        gui.centeredText(this.font, trimText(choiceStr, btnW - 8), btnX + btnW / 2, btnY + (btnH - 8) / 2, disabled ? TEXT_DISABLED : (btnHov ? TEXT_WHITE : 0xFF93C5FD));
    }

    private void renderCardToggle(GuiGraphicsExtractor gui, int x, int y, int w, int h, String title, String desc, boolean checked, boolean disabled, int mx, int my) {
        boolean hov = !disabled && mx >= x && mx <= x + w && my >= y && my <= y + h;
        gui.fill(x, y, x + w, y + h, hov ? CARD_HOVER : CARD_BG);
        drawBorder(gui, x, y, x + w, y + h, hov ? CARD_BORDER_ACTIVE : CARD_BORDER);

        // Switch button
        int swW = 32;
        int swH = Math.max(12, Math.min(16, h - 6));
        int swX = x + w - swW - 8;
        int swY = y + (h - swH) / 2;

        int maxTextW = swX - (x + 10) - 6;
        int titleCol = disabled ? TEXT_DISABLED : TEXT_WHITE;
        int descCol = disabled ? 0xFF475569 : TEXT_MUTED;

        if (h < 25) {
            gui.text(this.font, trimText(title, maxTextW), x + 10, y + (h - 8) / 2, titleCol, false);
        } else {
            gui.text(this.font, trimText(title, maxTextW), x + 10, y + 3, titleCol, false);
            gui.text(this.font, trimText(desc, maxTextW), x + 10, y + 13, descCol, false);
        }

        int trackBg = disabled ? 0xFF131D28 : (checked ? ACCENT_EMERALD : 0xFF1E293B);
        gui.fill(swX, swY, swX + swW, swY + swH, trackBg);
        drawBorder(gui, swX, swY, swX + swW, swY + swH, disabled ? 0xFF1A2634 : (checked ? 0xFF6EE7B7 : CARD_BORDER));

        int knobW = swH - 4;
        int knobX = checked ? swX + swW - knobW - 2 : swX + 2;
        int knobCol = disabled ? 0xFF334155 : TEXT_WHITE;
        gui.fill(knobX, swY + 2, knobX + knobW, swY + swH - 2, knobCol);
    }

    private void renderCardSlider(GuiGraphicsExtractor gui, int x, int y, int w, int h, String title, String desc, float pct, String badgeStr, boolean disabled, int mx, int my) {
        boolean hov = !disabled && mx >= x && mx <= x + w && my >= y && my <= y + h;
        gui.fill(x, y, x + w, y + h, hov ? CARD_HOVER : CARD_BG);
        drawBorder(gui, x, y, x + w, y + h, hov ? CARD_BORDER_ACTIVE : CARD_BORDER);

        int sW = Math.max(40, Math.min(68, w / 4));
        int sX = x + w - sW - 8;

        // Badge
        int badgeW = this.font.width(badgeStr) + 6;
        int badgeH = Math.max(12, Math.min(14, h - 6));
        int badgeX = sX - badgeW - 6;
        int badgeY = y + (h - badgeH) / 2;

        int maxTextW = badgeX - (x + 10) - 6;
        int titleCol = disabled ? TEXT_DISABLED : TEXT_WHITE;
        int descCol = disabled ? 0xFF475569 : TEXT_MUTED;

        if (h < 25) {
            gui.text(this.font, trimText(title, maxTextW), x + 10, y + (h - 8) / 2, titleCol, false);
        } else {
            gui.text(this.font, trimText(title, maxTextW), x + 10, y + 3, titleCol, false);
            gui.text(this.font, trimText(desc, maxTextW), x + 10, y + 13, descCol, false);
        }

        gui.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, 0xFF111A24);
        drawBorder(gui, badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, disabled ? 0xFF162230 : CARD_BORDER);
        gui.centeredText(this.font, badgeStr, badgeX + badgeW / 2, badgeY + (badgeH - 8) / 2, disabled ? TEXT_DISABLED : PRIMARY_BLUE);

        // Slider Track
        int trackH = (h < 25) ? 4 : 5;
        int sY = y + (h - trackH) / 2;
        gui.fill(sX, sY, sX + sW, sY + trackH, 0xFF0D141E);
        drawBorder(gui, sX - 1, sY - 1, sX + sW + 1, sY + trackH + 1, disabled ? 0xFF141F2C : CARD_BORDER);

        int filledW = (int) (pct * sW);
        if (!disabled && filledW > 0) {
            gui.fill(sX, sY, sX + filledW, sY + trackH, PRIMARY_BLUE);
        }

        // Thumb
        int thX = sX + filledW;
        int thW = 4;
        int thH = Math.max(8, trackH + 6);
        int thCol = disabled ? 0xFF334155 : TEXT_WHITE;
        gui.fill(thX - thW / 2, sY - (thH - trackH) / 2, thX + thW / 2, sY - (thH - trackH) / 2 + thH, thCol);
    }

    private void renderCardColor(GuiGraphicsExtractor gui, int x, int y, int w, int h, String title, String desc, int argb, boolean disabled, String lockMsg, int mx, int my) {
        boolean hov = !disabled && mx >= x && mx <= x + w && my >= y && my <= y + h;
        gui.fill(x, y, x + w, y + h, hov ? CARD_HOVER : CARD_BG);
        drawBorder(gui, x, y, x + w, y + h, hov ? CARD_BORDER_ACTIVE : CARD_BORDER);

        // Swatch
        int bSize = Math.max(12, Math.min(16, h - 6));
        int bX = x + w - bSize - 8;
        int bY = y + (h - bSize) / 2;

        // Hex badge
        String hexStr = String.format("#%06X", argb & 0xFFFFFF);
        int hexW = 48;
        int hexH = Math.max(12, Math.min(14, h - 6));
        int hexX = bX - hexW - 6;
        int hexY = y + (h - hexH) / 2;

        int maxTextW = hexX - (x + 10) - 6;
        int titleCol = disabled ? TEXT_DISABLED : TEXT_WHITE;

        if (disabled) {
            gui.text(this.font, trimText(title, maxTextW), x + 10, y + (h - 8) / 2, titleCol, false);
            return;
        }

        if (h < 25) {
            gui.text(this.font, trimText(title, maxTextW), x + 10, y + (h - 8) / 2, titleCol, false);
        } else {
            gui.text(this.font, trimText(title, maxTextW), x + 10, y + 3, titleCol, false);
            gui.text(this.font, trimText(desc, maxTextW), x + 10, y + 13, TEXT_MUTED, false);
        }

        gui.fill(bX, bY, bX + bSize, bY + bSize, argb);
        drawBorder(gui, bX - 1, bY - 1, bX + bSize + 1, bY + bSize + 1, TEXT_WHITE);

        boolean hexHov = mx >= hexX && mx <= hexX + hexW && my >= hexY && my <= hexY + hexH;
        gui.fill(hexX, hexY, hexX + hexW, hexY + hexH, hexHov ? 0xFF24344B : 0xFF111A24);
        drawBorder(gui, hexX, hexY, hexX + hexW, hexY + hexH, hexHov ? PRIMARY_BLUE : CARD_BORDER);
        gui.centeredText(this.font, hexStr, hexX + hexW / 2, hexY + (hexH - 8) / 2, hexHov ? TEXT_WHITE : TEXT_BODY);
    }

    // =========================================================================
    // COLOR PICKER MODAL (2D SAT/VAL BOX + HUE STRIP + QUICK SWATCHES)
    // =========================================================================
    private void renderPickerModal(GuiGraphicsExtractor gui, int mx, int my) {
        gui.fill(0, 0, this.width, this.height, 0x88000000);

        int mW = 210;
        int mH = 180;
        int mX = (this.width - mW) / 2;
        int mY = (this.height - mH) / 2;

        gui.fill(mX, mY, mX + mW, mY + mH, WINDOW_BG);
        drawBorder(gui, mX, mY, mX + mW, mY + mH, PRIMARY_BLUE);

        String targetTitle = switch (this.pickerTarget) {
            case 0 -> "Outline Color Picker";
            case 1 -> "Face Fill Color 1";
            case 2 -> "Face Fill Color 2";
            default -> "Color Picker";
        };
        gui.text(this.font, targetTitle, mX + 12, mY + 8, PRIMARY_BLUE, false);

        int boxX = mX + 12;
        int boxY = mY + 24;
        int boxSize = 90;

        float hue = this.currentHue();
        float sat = this.currentSat();
        float val = this.currentVal();

        // 2D Sat / Val Box
        for (int py = 0; py < boxSize; py += 2) {
            float v = 1.0f - (float) py / (float) boxSize;
            int colLeft = 0xFF000000 | BlockyOutlineSettings.hsvToRgbPacked(hue, 0.0f, v);
            int colRight = 0xFF000000 | BlockyOutlineSettings.hsvToRgbPacked(hue, 1.0f, v);
            gui.fillGradient(boxX, boxY + py, boxX + boxSize, boxY + py + 2, colLeft, colRight);
        }
        drawBorder(gui, boxX - 1, boxY - 1, boxX + boxSize + 1, boxY + boxSize + 1, CARD_BORDER);

        // Crosshair
        int thumbX = boxX + (int) (sat * boxSize);
        int thumbY = boxY + (int) ((1.0f - val) * boxSize);
        drawBorder(gui, thumbX - 3, thumbY - 3, thumbX + 4, thumbY + 4, 0xFFFFFFFF);

        // Vertical Hue Strip
        int hStripX = boxX + boxSize + 10;
        int hStripW = 14;
        int numSegments = HUE_COLORS.length - 1;
        float segH = (float) boxSize / (float) numSegments;
        for (int i = 0; i < numSegments; ++i) {
            int yStart = boxY + (int) (i * segH);
            int yEnd = (i == numSegments - 1) ? (boxY + boxSize) : (boxY + (int) ((i + 1) * segH));
            gui.fillGradient(hStripX, yStart, hStripX + hStripW, yEnd, HUE_COLORS[i], HUE_COLORS[i + 1]);
        }
        drawBorder(gui, hStripX - 1, boxY - 1, hStripX + hStripW + 1, boxY + boxSize + 1, CARD_BORDER);

        int hueY = boxY + (int) (hue * boxSize);
        gui.fill(hStripX - 2, hueY - 2, hStripX + hStripW + 2, hueY + 2, 0xFFFFFFFF);

        // Current Color Swatch Preview
        int curSwX = hStripX + hStripW + 10;
        int curSwW = 50;
        int curCol = 0xFF000000 | BlockyOutlineSettings.hsvToRgbPacked(hue, sat, val);
        gui.fill(curSwX, boxY, curSwX + curSwW, boxY + 28, curCol);
        drawBorder(gui, curSwX, boxY, curSwX + curSwW, boxY + 28, TEXT_WHITE);
        gui.text(this.font, String.format("#%06X", curCol & 0xFFFFFF), curSwX, boxY + 34, TEXT_MUTED, false);

        // Swatches Bar
        int swY = boxY + boxSize + 8;
        int swSize = 13;
        int swGap = 4;
        for (int i = 0; i < QUICK_SWATCHES.length; i++) {
            int sx = boxX + i * (swSize + swGap);
            boolean swHov = mx >= sx && mx <= sx + swSize && my >= swY && my <= swY + swSize;
            gui.fill(sx, swY, sx + swSize, swY + swSize, QUICK_SWATCHES[i]);
            drawBorder(gui, sx, swY, sx + swSize, swY + swSize, swHov ? TEXT_WHITE : 0xFF14202D);
        }

        // Done / Close Button
        int btnW = 54;
        int btnH = 16;
        int btnX = mX + mW - btnW - 12;
        int btnY = mY + mH - btnH - 10;
        boolean bHov = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
        gui.fill(btnX, btnY, btnX + btnW, btnY + btnH, bHov ? 0xFF2563EB : PRIMARY_BLUE);
        gui.centeredText(this.font, "OK", btnX + btnW / 2, btnY + 4, TEXT_WHITE);
    }

    private void renderTooltip(GuiGraphicsExtractor gui, int mx, int my) {
        int textW = this.font.width(this.tooltip);
        int tx = mx + 10;
        int ty = my - 14;
        gui.fill(tx - 6, ty - 5, tx + textW + 6, ty + 13, 0xF50A0F16);
        drawBorder(gui, tx - 6, ty - 5, tx + textW + 6, ty + 13, PRIMARY_BLUE);
        gui.text(this.font, this.tooltip, tx, ty, TEXT_WHITE, false);
    }

    // =========================================================================
    // 3D GEOMETRY UTILITIES
    // =========================================================================
    private void drawConvexQuad(GuiGraphicsExtractor gui, float[] v0, float[] v1, float[] v2, float[] v3, int colTop, int colBot) {
        float minY = Math.min(Math.min(v0[1], v1[1]), Math.min(v2[1], v3[1]));
        float maxY = Math.max(Math.max(v0[1], v1[1]), Math.max(v2[1], v3[1]));
        int startY = (int) Math.floor(minY);
        int endY = (int) Math.ceil(maxY);

        float[][] pts = {v0, v1, v2, v3};
        float totalH = Math.max(1.0f, maxY - minY);
        boolean solid = (colTop == colBot);

        for (int y = startY; y <= endY; y += 2) {
            float minX = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            int count = 0;

            for (int i = 0; i < 4; i++) {
                float[] pA = pts[i];
                float[] pB = pts[(i + 1) % 4];

                if ((pA[1] <= y && pB[1] > y) || (pB[1] <= y && pA[1] > y)) {
                    float t = (y - pA[1]) / (pB[1] - pA[1]);
                    float x = pA[0] + t * (pB[0] - pA[0]);
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    count++;
                }
            }

            if (count >= 2) {
                int col = solid ? colTop : interpolateColor(colTop, colBot, (y - minY) / totalH);
                gui.fill((int) minX, y, (int) Math.ceil(maxX), Math.min(y + 2, endY + 1), col);
            }
        }
    }

    private static int interpolateColor(int c1, int c2, float t) {
        t = clamp(t, 0.0f, 1.0f);
        int a1 = (c1 >>> 24) & 0xFF, r1 = (c1 >>> 16) & 0xFF, g1 = (c1 >>> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF, r2 = (c2 >>> 16) & 0xFF, g2 = (c2 >>> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + t * (a2 - a1));
        int r = (int) (r1 + t * (r2 - r1));
        int g = (int) (g1 + t * (g2 - g1));
        int b = (int) (b1 + t * (b2 - b1));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawLine(GuiGraphicsExtractor gui, float x1, float y1, float x2, float y2, int color, float width) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.5f) return;

        float stepSize = Math.max(1.2f, width * 0.75f);
        int steps = Math.max(1, (int) Math.ceil(len / stepSize));
        float stepX = dx / steps;
        float stepY = dy / steps;
        int halfW = Math.max(1, Math.round(width / 2.0f));

        float curX = x1;
        float curY = y1;
        for (int i = 0; i <= steps; i++) {
            int ix = Math.round(curX);
            int iy = Math.round(curY);
            gui.fill(ix - halfW + 1, iy - halfW + 1, ix + halfW, iy + halfW, color);
            curX += stepX;
            curY += stepY;
        }
    }

    // =========================================================================
    // COLOR ACCESSORS & MODIFIERS
    // =========================================================================
    private float currentHue() {
        return switch (this.pickerTarget) {
            case 0 -> this.settings.outlineHue;
            case 1 -> this.settings.fillHue;
            case 2 -> this.settings.fillHue2;
            default -> 0.0f;
        };
    }

    private float currentSat() {
        return switch (this.pickerTarget) {
            case 0 -> this.settings.outlineSaturation;
            case 1 -> this.settings.fillSaturation;
            case 2 -> this.settings.fillSaturation2;
            default -> 1.0f;
        };
    }

    private float currentVal() {
        return switch (this.pickerTarget) {
            case 0 -> this.settings.outlineValue;
            case 1 -> this.settings.fillValue;
            case 2 -> this.settings.fillValue2;
            default -> 1.0f;
        };
    }

    private void updateColorFromHSV(float h, float s, float v) {
        h = clamp(h, 0.0f, 1.0f);
        s = clamp(s, 0.0f, 1.0f);
        v = clamp(v, 0.0f, 1.0f);

        switch (this.pickerTarget) {
            case 0 -> {
                this.settings.outlineHue = h;
                this.settings.outlineSaturation = s;
                this.settings.outlineValue = v;
            }
            case 1 -> {
                this.settings.fillHue = h;
                this.settings.fillSaturation = s;
                this.settings.fillValue = v;
            }
            case 2 -> {
                this.settings.fillHue2 = h;
                this.settings.fillSaturation2 = s;
                this.settings.fillValue2 = v;
            }
        }
    }

    private void applyPreset(int index) {
        switch (index) {
            case 0 -> { // Cyber Cyan
                this.settings.outlineWidth = 2.0f;
                this.settings.outlineOpacity = 1.0f;
                this.settings.rainbowOutline = false;
                this.settings.outlineHue = 0.50f;
                this.settings.outlineSaturation = 0.9f;
                this.settings.outlineValue = 0.95f;
                this.settings.cornerMode = true;
                this.settings.cornerLength = 0.25f;
                this.settings.fillEnabled = true;
                this.settings.fillGradientEnabled = true;
                this.settings.rainbowFill = false;
                this.settings.fillHue = 0.50f;
                this.settings.fillHue2 = 0.58f;
                this.settings.fillOpacity = 0.25f;
                this.settings.breathingEffect = true;
                this.settings.breathingSpeed = 1.0f;
            }
            case 1 -> { // Silver
                this.settings.outlineWidth = 1.8f;
                this.settings.outlineOpacity = 0.85f;
                this.settings.rainbowOutline = false;
                this.settings.outlineHue = 0.6f;
                this.settings.outlineSaturation = 0.05f;
                this.settings.outlineValue = 0.9f;
                this.settings.cornerMode = false;
                this.settings.fillEnabled = true;
                this.settings.fillGradientEnabled = false;
                this.settings.rainbowFill = false;
                this.settings.fillHue = 0.6f;
                this.settings.fillSaturation = 0.05f;
                this.settings.fillValue = 0.9f;
                this.settings.fillOpacity = 0.15f;
                this.settings.breathingEffect = false;
            }
            case 2 -> { // Amethyst
                this.settings.outlineWidth = 2.5f;
                this.settings.outlineOpacity = 0.95f;
                this.settings.rainbowOutline = false;
                this.settings.outlineHue = 0.77f;
                this.settings.outlineSaturation = 0.85f;
                this.settings.outlineValue = 0.95f;
                this.settings.cornerMode = false;
                this.settings.fillEnabled = true;
                this.settings.fillGradientEnabled = true;
                this.settings.rainbowFill = false;
                this.settings.fillHue = 0.77f;
                this.settings.fillHue2 = 0.68f;
                this.settings.fillOpacity = 0.25f;
                this.settings.breathingEffect = true;
                this.settings.breathingSpeed = 0.8f;
            }
            case 3 -> { // Solar Gold
                this.settings.outlineWidth = 2.5f;
                this.settings.outlineOpacity = 1.0f;
                this.settings.rainbowOutline = false;
                this.settings.outlineHue = 0.12f;
                this.settings.outlineSaturation = 0.95f;
                this.settings.outlineValue = 1.0f;
                this.settings.cornerMode = false;
                this.settings.fillEnabled = true;
                this.settings.fillGradientEnabled = true;
                this.settings.rainbowFill = false;
                this.settings.fillHue = 0.12f;
                this.settings.fillHue2 = 0.08f;
                this.settings.fillOpacity = 0.3f;
                this.settings.breathingEffect = true;
                this.settings.breathingSpeed = 1.2f;
            }
            case 4 -> { // Chroma
                this.settings.rainbowOutline = true;
                this.settings.outlineRgbSpeed = 1.5f;
                this.settings.outlineOpacity = 1.0f;
                this.settings.outlineWidth = 2.5f;
                this.settings.cornerMode = false;
                this.settings.fillEnabled = true;
                this.settings.fillGradientEnabled = false;
                this.settings.rainbowFill = true;
                this.settings.fillRgbSpeed = 1.5f;
                this.settings.fillOpacity = 0.3f;
                this.settings.breathingEffect = false;
            }
        }
        BlockyOutlineSettings.save();
    }

    // =========================================================================
    // MOUSE & KEY EVENT HANDLING
    // =========================================================================
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        // 1. Color Picker Modal Clicks
        if (this.pickerTarget != -1) {
            int mW = 210;
            int mH = 180;
            int mX = (this.width - mW) / 2;
            int mY = (this.height - mH) / 2;
            int boxX = mX + 12;
            int boxY = mY + 24;
            int boxSize = 90;
            int hStripX = boxX + boxSize + 10;
            int hStripW = 14;

            if (mx >= boxX && mx <= boxX + boxSize && my >= boxY && my <= boxY + boxSize) {
                this.dragging2D = true;
                float sat = (float) (mx - boxX) / (float) boxSize;
                float val = 1.0f - (float) (my - boxY) / (float) boxSize;
                this.updateColorFromHSV(this.currentHue(), sat, val);
                return true;
            }

            if (mx >= hStripX - 4 && mx <= hStripX + hStripW + 4 && my >= boxY && my <= boxY + boxSize) {
                this.draggingHue = true;
                float hue = (float) (my - boxY) / (float) boxSize;
                this.updateColorFromHSV(hue, this.currentSat(), this.currentVal());
                return true;
            }

            int swY = boxY + boxSize + 8;
            int swSize = 13;
            int swGap = 4;
            for (int i = 0; i < QUICK_SWATCHES.length; i++) {
                int sx = boxX + i * (swSize + swGap);
                if (mx >= sx && mx <= sx + swSize && my >= swY && my <= swY + swSize) {
                    int c = QUICK_SWATCHES[i];
                    float r = ((c >> 16) & 0xFF) / 255.0f;
                    float g = ((c >> 8) & 0xFF) / 255.0f;
                    float b = (c & 0xFF) / 255.0f;
                    float[] hsv = BlockyOutlineSettings.rgbToHsv(r, g, b);
                    this.updateColorFromHSV(hsv[0], hsv[1], hsv[2]);
                    BlockyOutlineSettings.save();
                    this.playClick();
                    return true;
                }
            }

            int btnW = 54;
            int btnH = 16;
            int btnX = mX + mW - btnW - 12;
            int btnY = mY + mH - btnH - 10;
            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                this.pickerTarget = -1;
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            if (mx < mX || mx > mX + mW || my < mY || my > mY + mH) {
                this.pickerTarget = -1;
                BlockyOutlineSettings.save();
                return true;
            }
            return true;
        }

        // Header Metrics
        int headerH = Math.max(26, Math.min(34, this.winH / 10));
        int closeW = Math.max(16, headerH - 12);
        int closeX = this.winX + this.winW - closeW - 8;
        int closeY = this.winY + (headerH - closeW) / 2;

        // 2. Close ✕ Button at Top Right
        if (mx >= closeX && mx <= closeX + closeW && my >= closeY && my <= closeY + closeW) {
            this.playClick();
            this.onClose();
            return true;
        }

        // 3. Header Navigation Tabs
        String titleStr = (this.winW >= 420) ? MOD_TITLE : "Blocky Outline";
        int brandW = this.font.width(titleStr);
        int brandEndX = this.winX + 10 + brandW + 8;
        int maxTabsW = closeX - brandEndX - 8;
        int tabW = Math.max(34, Math.min(62, (maxTabsW - (TABS.length - 1) * 4) / TABS.length));
        int tabH = Math.max(16, headerH - 10);
        int totalTabsW = TABS.length * tabW + (TABS.length - 1) * 4;
        int tabStartX = brandEndX + Math.max(0, (maxTabsW - totalTabsW) / 2);
        int tabY = this.winY + (headerH - tabH) / 2;

        for (int i = 0; i < TABS.length; i++) {
            int tx = tabStartX + i * (tabW + 4);
            if (mx >= tx && mx <= tx + tabW && my >= tabY && my <= tabY + tabH) {
                if (this.activeTab != i) {
                    this.activeTab = i;
                    this.playClick();
                }
                return true;
            }
        }

        // 4. Footer Done Button
        int footerH = Math.max(24, Math.min(30, this.winH / 11));
        int doneW = Math.max(50, Math.min(68, this.winW / 7));
        int doneH = Math.max(16, footerH - 10);
        int doneX = this.winX + this.winW - doneW - 10;
        int doneY = this.winY + this.winH - footerH + (footerH - doneH) / 2;
        if (mx >= doneX && mx <= doneX + doneW && my >= doneY && my <= doneY + doneH) {
            this.playClick();
            this.onClose();
            return true;
        }

        // Report Bug Button
        int reportW = Math.max(68, Math.min(84, this.winW / 6));
        int reportX = doneX - reportW - 6;
        if (mx >= reportX && mx <= reportX + reportW && my >= doneY && my <= doneY + doneH) {
            this.playClick();
            Compat26.openUrl(Compat26.GITHUB_ISSUES_URL);
            return true;
        }

        // Column Layout calculation
        boolean showPreview = (this.activeTab != 3) && (this.winW >= 480) && (this.winH >= 210);
        int col1X = this.winX + 10;
        int col1W = showPreview ? (int) ((this.winW - 28) * 0.58f) : (this.winW - 20);
        int contentY = this.winY + headerH + 6;
        int contentH = this.winH - headerH - footerH - 12;

        int sW = Math.max(40, Math.min(68, col1W / 4));
        int sX = col1X + col1W - sW - 8;

        // 5. Tab 0: Outline Clicks
        if (this.activeTab == 0) {
            int[] b = new int[2];
            int totalRows = 8;

            // Row 0: Rainbow Toggle
            this.getRowBounds(0, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.rainbowOutline = !this.settings.rainbowOutline;
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            // Row 1: Speed Slider (if rainbow) OR Color Picker (if static)
            this.getRowBounds(1, totalRows, contentY, contentH, b);
            if (this.settings.rainbowOutline) {
                if (mx >= sX - 45 && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                    this.draggingSlider = true;
                    this.activeSliderTab = 0;
                    this.activeSliderRow = 1;
                    this.updateSliderValue(0, 1, mx, sX, sW);
                    return true;
                }
            } else {
                if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                    this.pickerTarget = 0;
                    this.playClick();
                    return true;
                }
            }

            // Row 2: Corner Accents Toggle
            this.getRowBounds(2, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.cornerMode = !this.settings.cornerMode;
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            // Row 3: Corner Bracket Size Slider
            this.getRowBounds(3, totalRows, contentY, contentH, b);
            if (this.settings.cornerMode && mx >= sX - 45 && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.draggingSlider = true;
                this.activeSliderTab = 0;
                this.activeSliderRow = 3;
                this.updateSliderValue(0, 3, mx, sX, sW);
                return true;
            }

            // Row 4: Through Walls (X-Ray) Mode Toggle
            this.getRowBounds(4, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.xrayMode = !this.settings.xrayMode;
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            // Row 5: Hitbox Floating Offset Slider
            this.getRowBounds(5, totalRows, contentY, contentH, b);
            if (mx >= sX - 45 && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.draggingSlider = true;
                this.activeSliderTab = 0;
                this.activeSliderRow = 5;
                this.updateSliderValue(0, 5, mx, sX, sW);
                return true;
            }

            // Row 6: Thickness Slider
            this.getRowBounds(6, totalRows, contentY, contentH, b);
            if (mx >= sX - 45 && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.draggingSlider = true;
                this.activeSliderTab = 0;
                this.activeSliderRow = 6;
                this.updateSliderValue(0, 6, mx, sX, sW);
                return true;
            }

            // Row 7: Opacity Slider
            this.getRowBounds(7, totalRows, contentY, contentH, b);
            if (mx >= sX - 45 && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.draggingSlider = true;
                this.activeSliderTab = 0;
                this.activeSliderRow = 7;
                this.updateSliderValue(0, 7, mx, sX, sW);
                return true;
            }
        }

        // 6. Tab 1: Face Fill Clicks
        if (this.activeTab == 1) {
            int[] b = new int[2];
            int totalRows = 6;

            // Row 0: Enable Fill Toggle
            this.getRowBounds(0, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.fillEnabled = !this.settings.fillEnabled;
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            // Row 1: Rainbow Fill Toggle
            this.getRowBounds(1, totalRows, contentY, contentH, b);
            if (this.settings.fillEnabled && mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.rainbowFill = !this.settings.rainbowFill;
                if (this.settings.rainbowFill) {
                    this.settings.fillGradientEnabled = false;
                }
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            // Row 2: Color 1 (Top Face) - Click anywhere on card
            this.getRowBounds(2, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.fillEnabled = true;
                this.settings.rainbowFill = false;
                this.pickerTarget = 1;
                this.playClick();
                return true;
            }

            // Row 3: Enable Color 2 (Gradient) Toggle
            this.getRowBounds(3, totalRows, contentY, contentH, b);
            if (this.settings.fillEnabled && !this.settings.rainbowFill && mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.fillGradientEnabled = !this.settings.fillGradientEnabled;
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            // Row 4: Color 2 (Bottom Face) - Click anywhere on card
            this.getRowBounds(4, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                if (!this.settings.rainbowFill) {
                    this.settings.fillEnabled = true;
                    this.settings.fillGradientEnabled = true;
                    this.pickerTarget = 2;
                    this.playClick();
                    return true;
                }
            }

            // Row 5: Fill Opacity Slider
            this.getRowBounds(5, totalRows, contentY, contentH, b);
            if (this.settings.fillEnabled && mx >= sX - 45 && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.draggingSlider = true;
                this.activeSliderTab = 1;
                this.activeSliderRow = 5;
                this.updateSliderValue(1, 5, mx, sX, sW);
                return true;
            }
        }

        // 7. Tab 2: Smooth Movement & Advanced Motion Engine Clicks
        if (this.activeTab == 2) {
            int[] b = new int[2];
            int totalRows = 5;

            // Row 0: Smooth Movement Toggle
            this.getRowBounds(0, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.smoothTransition = !this.settings.smoothTransition;
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            // Row 1: Motion Engine / Easing Curve Selector (Left click = next, Right click = prev)
            this.getRowBounds(1, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                if (button == 1) {
                    this.settings.motionMode = (this.settings.motionMode + 5) % 6;
                } else {
                    this.settings.motionMode = (this.settings.motionMode + 1) % 6;
                }
                this.settings.smoothTransition = true;
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            // Row 2: Dynamic Parameter Slider
            this.getRowBounds(2, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.smoothTransition = true;
                this.draggingSlider = true;
                this.activeSliderTab = 2;
                this.activeSliderRow = 2;
                this.updateSliderValue(2, 2, mx, sX, sW);
                return true;
            }

            // Row 3: Breathing Pulse Glow Toggle
            this.getRowBounds(3, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.breathingEffect = !this.settings.breathingEffect;
                BlockyOutlineSettings.save();
                this.playClick();
                return true;
            }

            // Row 4: Pulse Speed Slider
            this.getRowBounds(4, totalRows, contentY, contentH, b);
            if (mx >= col1X && mx <= col1X + col1W && my >= b[0] && my <= b[0] + b[1]) {
                this.settings.breathingEffect = true;
                this.draggingSlider = true;
                this.activeSliderTab = 2;
                this.activeSliderRow = 4;
                this.updateSliderValue(2, 4, mx, sX, sW);
                return true;
            }
        }

        // 8. Right Column: Preview Viewport Interaction
        if (showPreview) {
            int col2X = col1X + col1W + 8;
            int col2W = this.winX + this.winW - 10 - col2X;

            int tbH = 20;
            int tbY = contentY + contentH - tbH - 6;
            int tbW = col2W - 12;
            int tbX = col2X + 6;

            if (mx >= tbX && mx <= tbX + tbW && my >= tbY && my <= tbY + tbH) {
                int bW = tbW / 3;
                if (mx >= tbX && mx <= tbX + bW) {
                    this.autoRotate = !this.autoRotate;
                    this.playClick();
                    return true;
                }
                int b2X = tbX + bW;
                if (mx >= b2X && mx <= b2X + bW) {
                    this.previewShape = (this.previewShape + 1) % 2;
                    this.playClick();
                    return true;
                }
                int b3X = tbX + bW * 2;
                if (mx >= b3X && mx <= tbX + tbW) {
                    this.cubeYaw = 35.0f;
                    this.cubePitch = 25.0f;
                    this.playClick();
                    return true;
                }
            }

            // Drag 3D cube
            if (mx >= col2X && mx <= col2X + col2W && my >= contentY + 28 && my <= tbY) {
                this.draggingCube = true;
                this.dragStartMouseX = mx;
                this.dragStartMouseY = my;
                this.dragStartYaw = this.cubeYaw;
                this.dragStartPitch = this.cubePitch;
                return true;
            }
        }

        // 9. Tab 3: Presets & Custom Slots Clicks
        if (this.activeTab == 3) {
            int halfW = (col1W - 10) / 2;

            // 5 Built-in Presets
            int curY = contentY + 22;
            int tGap = (contentH >= 220) ? 4 : 2;
            int tCardH = Math.max(20, (contentH - 28 - (PRESET_NAMES.length - 1) * tGap) / PRESET_NAMES.length);

            for (int i = 0; i < PRESET_NAMES.length; i++) {
                int btnW = 38;
                int btnH = Math.max(14, tCardH - 6);
                int btnX = col1X + halfW - btnW - 10;
                int btnY = curY + (tCardH - btnH) / 2;
                if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                    this.applyPreset(i);
                    this.playClick();
                    return true;
                }
                curY += tCardH + tGap;
            }

            // 3 Custom Slots: [Load] [Save] [Del]
            int rightX = col1X + halfW + 10;
            int uY = contentY + 22;
            int uGap = (contentH >= 220) ? 6 : 4;
            int uCardH = Math.max(34, (contentH - 28 - 2 * uGap) / 3);
            BlockyOutlineSettings.UserPreset[] slots = {
                    this.settings.userPreset1, this.settings.userPreset2, this.settings.userPreset3
            };

            for (int i = 0; i < slots.length; i++) {
                BlockyOutlineSettings.UserPreset slot = slots[i];
                int btnW = 30;
                int btnH = Math.max(14, Math.min(18, uCardH - 12));
                int delX = rightX + halfW - btnW - 8;
                int saveX = delX - btnW - 4;
                int loadX = saveX - btnW - 4;
                int bY = uY + (uCardH - btnH) / 2;

                // Load
                if (slot.hasData && mx >= loadX && mx <= loadX + btnW && my >= bY && my <= bY + btnH) {
                    slot.applyTo(this.settings);
                    this.playClick();
                    return true;
                }

                // Save
                if (mx >= saveX && mx <= saveX + btnW && my >= bY && my <= bY + btnH) {
                    slot.saveCurrent(this.settings);
                    this.playClick();
                    return true;
                }

                // Delete (Clear)
                if (slot.hasData && mx >= delX && mx <= delX + btnW && my >= bY && my <= bY + btnH) {
                    slot.clear();
                    this.playClick();
                    return true;
                }

                uY += uCardH + uGap;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.dragging2D || this.draggingHue) {
            this.dragging2D = false;
            this.draggingHue = false;
            BlockyOutlineSettings.save();
            return true;
        }
        if (this.draggingSlider) {
            this.draggingSlider = false;
            this.activeSliderTab = -1;
            this.activeSliderRow = -1;
            BlockyOutlineSettings.save();
            return true;
        }
        if (this.draggingCube) {
            this.draggingCube = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mx = event.x();
        double my = event.y();

        if (this.draggingCube) {
            this.cubeYaw = (this.dragStartYaw + (float) (mx - this.dragStartMouseX) * 0.8f) % 360.0f;
            this.cubePitch = clamp(this.dragStartPitch - (float) (my - this.dragStartMouseY) * 0.8f, -80.0f, 80.0f);
            return true;
        }

        if (this.pickerTarget != -1) {
            int mW = 210;
            int mH = 180;
            int mX = (this.width - mW) / 2;
            int mY = (this.height - mH) / 2;
            int boxX = mX + 12;
            int boxY = mY + 24;
            int boxSize = 90;

            if (this.dragging2D) {
                float sat = clamp((float) (mx - boxX) / (float) boxSize, 0.0f, 1.0f);
                float val = clamp(1.0f - (float) (my - boxY) / (float) boxSize, 0.0f, 1.0f);
                this.updateColorFromHSV(this.currentHue(), sat, val);
                return true;
            }
            if (this.draggingHue) {
                float hue = clamp((float) (my - boxY) / (float) boxSize, 0.0f, 1.0f);
                this.updateColorFromHSV(hue, this.currentSat(), this.currentVal());
                return true;
            }
            return true;
        }

        if (this.draggingSlider && this.activeSliderTab != -1 && this.activeSliderRow != -1) {
            boolean showPreview = (this.activeTab != 3) && (this.winW >= 480) && (this.winH >= 210);
            int col1X = this.winX + 10;
            int col1W = showPreview ? (int) ((this.winW - 28) * 0.58f) : (this.winW - 20);
            int sW = Math.max(40, Math.min(68, col1W / 4));
            int sX = col1X + col1W - sW - 8;
            this.updateSliderValue(this.activeSliderTab, this.activeSliderRow, mx, sX, sW);
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    private void updateSliderValue(int tabIndex, int rowIndex, double mx, int sX, int sW) {
        float pct = clamp((float) (mx - sX) / (float) sW, 0.0f, 1.0f);

        if (tabIndex == 0) {
            switch (rowIndex) {
                case 1 -> this.settings.outlineRgbSpeed = 0.1f + pct * 4.9f;
                case 3 -> this.settings.cornerLength = 0.10f + pct * 0.38f;
                case 5 -> this.settings.hitboxExpand = pct * 0.030f;
                case 6 -> this.settings.outlineWidth = 0.5f + pct * 9.5f;
                case 7 -> this.settings.outlineOpacity = pct;
            }
        } else if (tabIndex == 1) {
            if (rowIndex == 5) {
                this.settings.fillOpacity = pct;
            }
        } else if (tabIndex == 2) {
            switch (rowIndex) {
                case 2 -> {
                    switch (this.settings.motionMode) {
                        case 0 -> this.settings.springStiffness = 150.0f + pct * 350.0f;
                        case 1 -> this.settings.backOvershoot = 0.6f + pct * 1.4f;
                        case 2, 3 -> this.settings.motionDurationMs = 35.0f + pct * 95.0f;
                        case 4 -> this.settings.smoothSpeed = Math.max(0.1f, pct);
                    }
                }
                case 4 -> this.settings.breathingSpeed = 0.2f + pct * 2.8f;
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_M) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        BlockyOutlineSettings.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
