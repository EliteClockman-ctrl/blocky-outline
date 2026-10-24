package charaz.blockoutline.client.compat.modern;

import charaz.blockoutline.client.compat.TargetExtractor;
import charaz.blockoutline.client.compat.TargetInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;

public final class ModernTargetExtractor implements TargetExtractor {
    @Override
    public TargetInfo extract(Object targetObj, Minecraft mc) {
        if (targetObj instanceof BlockOutlineRenderState state) {
            return new TargetInfo(state.pos(), state.shape());
        }
        return null;
    }
}
