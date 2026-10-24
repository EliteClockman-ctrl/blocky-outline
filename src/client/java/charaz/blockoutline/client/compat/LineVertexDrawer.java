package charaz.blockoutline.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface LineVertexDrawer {
    default void prepare(float width) {
    }

    void line(VertexConsumer consumer, PoseStack.Pose pose,
              float x1, float y1, float z1, float x2, float y2, float z2,
              float nx, float ny, float nz, int r, int g, int b, int a, float width);
}
