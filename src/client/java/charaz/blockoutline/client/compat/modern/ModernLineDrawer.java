package charaz.blockoutline.client.compat.modern;

import charaz.blockoutline.client.compat.LineVertexDrawer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class ModernLineDrawer implements LineVertexDrawer {
    @Override
    public void line(VertexConsumer consumer, PoseStack.Pose pose,
                     float x1, float y1, float z1, float x2, float y2, float z2,
                     float nx, float ny, float nz, int r, int g, int b, int a, float width) {
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(width);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(width);
    }
}
