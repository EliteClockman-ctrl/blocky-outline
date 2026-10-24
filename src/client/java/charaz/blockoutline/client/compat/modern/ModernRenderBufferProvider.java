package charaz.blockoutline.client.compat.modern;

import charaz.blockoutline.client.compat.RenderBufferProvider;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.BlockyOutlineRenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class ModernRenderBufferProvider implements RenderBufferProvider {
    @Override
    public VertexConsumer getLineBuffer(MultiBufferSource consumers, boolean xray) {
        RenderType type = xray ? BlockyOutlineRenderTypes.XRAY_LINES : RenderTypes.lines();
        return consumers.getBuffer(type);
    }

    @Override
    public VertexConsumer getQuadBuffer(MultiBufferSource consumers, boolean xray) {
        RenderType type = xray ? BlockyOutlineRenderTypes.XRAY_QUADS : RenderTypes.debugQuads();
        return consumers.getBuffer(type);
    }
}
