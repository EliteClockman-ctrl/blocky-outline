package charaz.blockoutline.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class OutlineRenderer {
    private static final float INV_255 = 0.003921569f;

    private OutlineRenderer() {
    }

    public static void renderOutline(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double dx, double dy, double dz, float r, float g, float b, float a, float lineWidth) {
        PoseStack.Pose pose = poseStack.last();
        float width = Math.max(1.0f, lineWidth);
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float nx = (float)(x2 - x1);
            float ny = (float)(y2 - y1);
            float nz = (float)(z2 - z1);
            float lenSq = nx * nx + ny * ny + nz * nz;
            if (lenSq > 0.000001f) {
                float invLen = (float) Mth.fastInvSqrt(lenSq);
                nx *= invLen;
                ny *= invLen;
                nz *= invLen;
            }
            consumer.addVertex(pose, (float)(x1 + dx), (float)(y1 + dy), (float)(z1 + dz))
                    .setColor(r, g, b, a)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(width);
            consumer.addVertex(pose, (float)(x2 + dx), (float)(y2 + dy), (float)(z2 + dz))
                    .setColor(r, g, b, a)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(width);
        });
    }

    public static void renderFilledBox(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double dx, double dy, double dz, int colorARGB, int colorARGB2) {
        PoseStack.Pose pose = poseStack.last();
        float r1 = (float)(colorARGB >> 16 & 0xFF) * INV_255;
        float g1 = (float)(colorARGB >> 8 & 0xFF) * INV_255;
        float b1 = (float)(colorARGB & 0xFF) * INV_255;
        float a1 = (float)(colorARGB >>> 24) * INV_255;

        float r2 = (float)(colorARGB2 >> 16 & 0xFF) * INV_255;
        float g2 = (float)(colorARGB2 >> 8 & 0xFF) * INV_255;
        float b2 = (float)(colorARGB2 & 0xFF) * INV_255;
        float a2 = (float)(colorARGB2 >>> 24) * INV_255;

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float x0 = (float)(minX - 0.001 + dx);
            float y0 = (float)(minY - 0.001 + dy);
            float z0 = (float)(minZ - 0.001 + dz);
            float x1 = (float)(maxX + 0.001 + dx);
            float y1 = (float)(maxY + 0.001 + dy);
            float z1 = (float)(maxZ + 0.001 + dz);

            consumer.addVertex(pose, x0, y0, z0).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x1, y0, z0).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x1, y0, z1).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x0, y0, z1).setColor(r2, g2, b2, a2);

            consumer.addVertex(pose, x0, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y1, z0).setColor(r1, g1, b1, a1);

            consumer.addVertex(pose, x0, y0, z0).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x0, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y0, z0).setColor(r2, g2, b2, a2);

            consumer.addVertex(pose, x1, y0, z1).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y0, z1).setColor(r2, g2, b2, a2);

            consumer.addVertex(pose, x0, y0, z1).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x0, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y0, z0).setColor(r2, g2, b2, a2);

            consumer.addVertex(pose, x1, y0, z0).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x1, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y0, z1).setColor(r2, g2, b2, a2);
        });
    }
}
