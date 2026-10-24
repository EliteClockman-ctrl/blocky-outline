package charaz.blockoutline.client.compat.legacy;

import charaz.blockoutline.client.ui.BlockyOutlineMenuScreen;

public final class LegacyMenuScreen extends BlockyOutlineMenuScreen {

    public boolean mouseClicked(double mx, double my, int button) {
        return handleMouseClicked(mx, my, button);
    }

    public boolean mouseReleased(double mx, double my, int button) {
        return handleMouseReleased(mx, my, button);
    }

    public boolean mouseDragged(double mx, double my, int button, double dragX, double dragY) {
        return handleMouseDragged(mx, my, button, dragX, dragY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return handleKeyPressed(keyCode, scanCode, modifiers);
    }
}
