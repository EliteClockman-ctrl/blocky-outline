package charaz.blockoutline.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4fc;

public final class OutlineRenderer {
    private OutlineRenderer() {
    }

    public static void renderFilledBox(VertexConsumer vertexConsumer, PoseStack.Pose pose, VoxelShape shape, int colorARGB, int colorARGB2, boolean isTwoColor) {
        int a1 = (colorARGB >>> 24) & 0xFF;
        if (a1 == 0) return;
        int r1 = (colorARGB >>> 16) & 0xFF;
        int g1 = (colorARGB >>> 8) & 0xFF;
        int b1 = colorARGB & 0xFF;

        int a2 = isTwoColor ? ((colorARGB2 >>> 24) & 0xFF) : a1;
        int r2 = isTwoColor ? ((colorARGB2 >>> 16) & 0xFF) : r1;
        int g2 = isTwoColor ? ((colorARGB2 >>> 8) & 0xFF) : g1;
        int b2 = isTwoColor ? (colorARGB2 & 0xFF) : b1;

        Matrix4fc matrix = pose.pose();
        final float inflate = 0.0025f;

        var boxList = shape.toAabbs();
        int size = boxList.size();
        for (int i = 0; i < size; ++i) {
            var box = boxList.get(i);
            float x0 = (float) box.minX - inflate;
            float y0 = (float) box.minY - inflate;
            float z0 = (float) box.minZ - inflate;
            float x1 = (float) box.maxX + inflate;
            float y1 = (float) box.maxY + inflate;
            float z1 = (float) box.maxZ + inflate;

            // Bottom Face (Pure Color 1)
            vertexConsumer.addVertex(matrix, x0, y0, z1).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x1, y0, z1).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x1, y0, z0).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x0, y0, z0).setColor(r1, g1, b1, a1);

            // Top Face (Pure Color 2)
            vertexConsumer.addVertex(matrix, x0, y1, z1).setColor(r2, g2, b2, a2);
            vertexConsumer.addVertex(matrix, x1, y1, z1).setColor(r2, g2, b2, a2);
            vertexConsumer.addVertex(matrix, x1, y1, z0).setColor(r2, g2, b2, a2);
            vertexConsumer.addVertex(matrix, x0, y1, z0).setColor(r2, g2, b2, a2);

            // North Face (y0 = Color 1 -> y1 = Color 2)
            vertexConsumer.addVertex(matrix, x1, y0, z0).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x0, y0, z0).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x0, y1, z0).setColor(r2, g2, b2, a2);
            vertexConsumer.addVertex(matrix, x1, y1, z0).setColor(r2, g2, b2, a2);

            // South Face (y0 = Color 1 -> y1 = Color 2)
            vertexConsumer.addVertex(matrix, x0, y0, z1).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x1, y0, z1).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x1, y1, z1).setColor(r2, g2, b2, a2);
            vertexConsumer.addVertex(matrix, x0, y1, z1).setColor(r2, g2, b2, a2);

            // West Face (y0 = Color 1 -> y1 = Color 2)
            vertexConsumer.addVertex(matrix, x0, y0, z0).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x0, y0, z1).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x0, y1, z1).setColor(r2, g2, b2, a2);
            vertexConsumer.addVertex(matrix, x0, y1, z0).setColor(r2, g2, b2, a2);

            // East Face (y0 = Color 1 -> y1 = Color 2)
            vertexConsumer.addVertex(matrix, x1, y0, z1).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x1, y0, z0).setColor(r1, g1, b1, a1);
            vertexConsumer.addVertex(matrix, x1, y1, z0).setColor(r2, g2, b2, a2);
            vertexConsumer.addVertex(matrix, x1, y1, z1).setColor(r2, g2, b2, a2);
        }
    }

    public static void renderFilledBox(VertexConsumer vertexConsumer, PoseStack.Pose pose, VoxelShape shape, int colorARGB) {
        renderFilledBox(vertexConsumer, pose, shape, colorARGB, colorARGB, false);
    }
}
