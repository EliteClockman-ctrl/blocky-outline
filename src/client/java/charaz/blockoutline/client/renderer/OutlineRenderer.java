package charaz.blockoutline.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class OutlineRenderer {

    private OutlineRenderer() {
    }

    public static void renderOutline(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape,
                                     double dx, double dy, double dz, int argb, float lineWidth, boolean cornerMode) {
        renderOutline(poseStack, consumer, shape, dx, dy, dz, argb, lineWidth, cornerMode, 0.28f, 0.002f);
    }

    public static void renderOutline(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape,
                                     double dx, double dy, double dz, int argb, float lineWidth,
                                     boolean cornerMode, float cornerLength, float expand) {
        int a = (argb >>> 24) & 0xFF;
        if (a == 0) return;

        PoseStack.Pose pose = poseStack.last();
        float width = Math.max(1.0f, lineWidth);
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        float cLen = Mth.clamp(cornerLength, 0.10f, 0.48f);
        float exp = Math.max(0.0f, expand);

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float x0 = (float) (minX - exp + dx);
            float y0 = (float) (minY - exp + dy);
            float z0 = (float) (minZ - exp + dz);
            float x1 = (float) (maxX + exp + dx);
            float y1 = (float) (maxY + exp + dy);
            float z1 = (float) (maxZ + exp + dz);

            renderBoxOutline(consumer, pose, x0, y0, z0, x1, y1, z1, r, g, b, a, width, cornerMode, cLen);
        });
    }

    private static void renderBoxOutline(VertexConsumer consumer, PoseStack.Pose pose,
                                         float x0, float y0, float z0, float x1, float y1, float z1,
                                         int r, int g, int b, int a, float width, boolean cornerMode, float cLen) {
        if (!cornerMode) {
            line(consumer, pose, x0, y0, z0, x1, y0, z0, 1.0f, 0.0f, 0.0f, r, g, b, a, width);
            line(consumer, pose, x0, y0, z1, x1, y0, z1, 1.0f, 0.0f, 0.0f, r, g, b, a, width);
            line(consumer, pose, x0, y0, z0, x0, y0, z1, 0.0f, 0.0f, 1.0f, r, g, b, a, width);
            line(consumer, pose, x1, y0, z0, x1, y0, z1, 0.0f, 0.0f, 1.0f, r, g, b, a, width);

            line(consumer, pose, x0, y1, z0, x1, y1, z0, 1.0f, 0.0f, 0.0f, r, g, b, a, width);
            line(consumer, pose, x0, y1, z1, x1, y1, z1, 1.0f, 0.0f, 0.0f, r, g, b, a, width);
            line(consumer, pose, x0, y1, z0, x0, y1, z1, 0.0f, 0.0f, 1.0f, r, g, b, a, width);
            line(consumer, pose, x1, y1, z0, x1, y1, z1, 0.0f, 0.0f, 1.0f, r, g, b, a, width);

            line(consumer, pose, x0, y0, z0, x0, y1, z0, 0.0f, 1.0f, 0.0f, r, g, b, a, width);
            line(consumer, pose, x1, y0, z0, x1, y1, z0, 0.0f, 1.0f, 0.0f, r, g, b, a, width);
            line(consumer, pose, x0, y0, z1, x0, y1, z1, 0.0f, 1.0f, 0.0f, r, g, b, a, width);
            line(consumer, pose, x1, y0, z1, x1, y1, z1, 0.0f, 1.0f, 0.0f, r, g, b, a, width);
        } else {
            float dx = (x1 - x0) * cLen;
            float dy = (y1 - y0) * cLen;
            float dz = (z1 - z0) * cLen;

            cornerLineX(consumer, pose, x0, x1, dx, y0, z0, r, g, b, a, width);
            cornerLineX(consumer, pose, x0, x1, dx, y0, z1, r, g, b, a, width);
            cornerLineX(consumer, pose, x0, x1, dx, y1, z0, r, g, b, a, width);
            cornerLineX(consumer, pose, x0, x1, dx, y1, z1, r, g, b, a, width);

            cornerLineY(consumer, pose, y0, y1, dy, x0, z0, r, g, b, a, width);
            cornerLineY(consumer, pose, y0, y1, dy, x1, z0, r, g, b, a, width);
            cornerLineY(consumer, pose, y0, y1, dy, x0, z1, r, g, b, a, width);
            cornerLineY(consumer, pose, y0, y1, dy, x1, z1, r, g, b, a, width);

            cornerLineZ(consumer, pose, z0, z1, dz, x0, y0, r, g, b, a, width);
            cornerLineZ(consumer, pose, z0, z1, dz, x1, y0, r, g, b, a, width);
            cornerLineZ(consumer, pose, z0, z1, dz, x0, y1, r, g, b, a, width);
            cornerLineZ(consumer, pose, z0, z1, dz, x1, y1, r, g, b, a, width);
        }
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float nx, float ny, float nz, int r, int g, int b, int a, float width) {
        charaz.blockoutline.client.compat.CompatBridge.LINE_DRAWER.line(consumer, pose, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b, a, width);
    }

    private static void cornerLineX(VertexConsumer consumer, PoseStack.Pose pose,
                                    float x0, float x1, float dx, float y, float z,
                                    int r, int g, int b, int a, float width) {
        line(consumer, pose, x0, y, z, x0 + dx, y, z, 1.0f, 0.0f, 0.0f, r, g, b, a, width);
        line(consumer, pose, x1 - dx, y, z, x1, y, z, 1.0f, 0.0f, 0.0f, r, g, b, a, width);
    }

    private static void cornerLineY(VertexConsumer consumer, PoseStack.Pose pose,
                                    float y0, float y1, float dy, float x, float z,
                                    int r, int g, int b, int a, float width) {
        line(consumer, pose, x, y0, z, x, y0 + dy, z, 0.0f, 1.0f, 0.0f, r, g, b, a, width);
        line(consumer, pose, x, y1 - dy, z, x, y1, z, 0.0f, 1.0f, 0.0f, r, g, b, a, width);
    }

    private static void cornerLineZ(VertexConsumer consumer, PoseStack.Pose pose,
                                    float z0, float z1, float dz, float x, float y,
                                    int r, int g, int b, int a, float width) {
        line(consumer, pose, x, y, z0, x, y, z0 + dz, 0.0f, 0.0f, 1.0f, r, g, b, a, width);
        line(consumer, pose, x, y, z1 - dz, x, y, z1, 0.0f, 0.0f, 1.0f, r, g, b, a, width);
    }

    public static void renderFilledBox(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape,
                                       double dx, double dy, double dz, int colorARGB, int colorARGB2) {
        renderFilledBox(poseStack, consumer, shape, dx, dy, dz, colorARGB, colorARGB2, 0.002f);
    }

    public static void renderFilledBox(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape,
                                       double dx, double dy, double dz, int colorARGB, int colorARGB2, float expand) {
        int a1 = (colorARGB >>> 24) & 0xFF;
        int a2 = (colorARGB2 >>> 24) & 0xFF;
        if ((a1 | a2) == 0) return;

        PoseStack.Pose pose = poseStack.last();
        int r1 = (colorARGB >> 16) & 0xFF;
        int g1 = (colorARGB >> 8) & 0xFF;
        int b1 = colorARGB & 0xFF;

        int r2 = (colorARGB2 >> 16) & 0xFF;
        int g2 = (colorARGB2 >> 8) & 0xFF;
        int b2 = colorARGB2 & 0xFF;

        float exp = Math.max(0.0005f, expand);

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float x0 = (float) (minX - exp + dx);
            float y0 = (float) (minY - exp + dy);
            float z0 = (float) (minZ - exp + dz);
            float x1 = (float) (maxX + exp + dx);
            float y1 = (float) (maxY + exp + dy);
            float z1 = (float) (maxZ + exp + dz);

            // Bottom
            consumer.addVertex(pose, x0, y0, z0).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x1, y0, z0).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x1, y0, z1).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x0, y0, z1).setColor(r2, g2, b2, a2);

            // Top
            consumer.addVertex(pose, x0, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y1, z0).setColor(r1, g1, b1, a1);

            // North
            consumer.addVertex(pose, x0, y0, z0).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x0, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y0, z0).setColor(r2, g2, b2, a2);

            // East
            consumer.addVertex(pose, x1, y0, z0).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x1, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x1, y0, z1).setColor(r2, g2, b2, a2);

            // South
            consumer.addVertex(pose, x1, y0, z1).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y0, z1).setColor(r2, g2, b2, a2);

            // West
            consumer.addVertex(pose, x0, y0, z1).setColor(r2, g2, b2, a2);
            consumer.addVertex(pose, x0, y1, z1).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y1, z0).setColor(r1, g1, b1, a1);
            consumer.addVertex(pose, x0, y0, z0).setColor(r2, g2, b2, a2);
        });
    }
}
