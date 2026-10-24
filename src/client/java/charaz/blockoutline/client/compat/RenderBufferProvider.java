package charaz.blockoutline.client.compat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

public interface RenderBufferProvider {
    VertexConsumer getLineBuffer(MultiBufferSource consumers, boolean xray);

    VertexConsumer getQuadBuffer(MultiBufferSource consumers, boolean xray);

    default void postRender(boolean xray) {
    }
}
