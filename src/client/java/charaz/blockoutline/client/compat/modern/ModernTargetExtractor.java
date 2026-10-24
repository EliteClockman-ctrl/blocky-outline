package charaz.blockoutline.client.compat.modern;

import charaz.blockoutline.client.compat.TargetExtractor;
import charaz.blockoutline.client.compat.TargetInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;

public final class ModernTargetExtractor implements TargetExtractor {
    private final TargetInfo cached = new TargetInfo();

    @Override
    public TargetInfo extract(Object targetObj, Minecraft mc) {
        if (targetObj instanceof BlockOutlineRenderState state) {
            cached.set(state.pos(), state.shape());
            return cached;
        }
        return null;
    }
}
