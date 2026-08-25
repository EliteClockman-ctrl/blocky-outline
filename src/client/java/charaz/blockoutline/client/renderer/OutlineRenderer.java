package charaz.blockoutline.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4fc;

public final class OutlineRenderer {
    private OutlineRenderer() {
    }

    public static void renderFilledBox(VertexConsumer vertexConsumer, PoseStack.Pose pose, VoxelShape shape, int colorARGB) {
        float r = (float)(colorARGB >> 16 & 0xFF) / 255.0f;
        float g = (float)(colorARGB >> 8 & 0xFF) / 255.0f;
        float b = (float)(colorARGB & 0xFF) / 255.0f;
        float a = (float)(colorARGB >> 24 & 0xFF) / 255.0f;

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double inflate = 0.003;
            float x0 = (float)(minX - inflate);
            float y0 = (float)(minY - inflate);
            float z0 = (float)(minZ - inflate);
            float x1 = (float)(maxX + inflate);
            float y1 = (float)(maxY + inflate);
            float z1 = (float)(maxZ + inflate);

            addQuad(vertexConsumer, pose, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, r, g, b, a);
            addQuad(vertexConsumer, pose, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, r, g, b, a);
            addQuad(vertexConsumer, pose, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a);
            addQuad(vertexConsumer, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a);
            addQuad(vertexConsumer, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a);
            addQuad(vertexConsumer, pose, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a);
        });
    }

    private static void addQuad(VertexConsumer vertexConsumer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float r, float g, float b, float a) {
        vertexConsumer.addVertex((Matrix4fc)pose.pose(), x0, y0, z0).setColor(r, g, b, a);
        vertexConsumer.addVertex((Matrix4fc)pose.pose(), x1, y1, z1).setColor(r, g, b, a);
        vertexConsumer.addVertex((Matrix4fc)pose.pose(), x2, y2, z2).setColor(r, g, b, a);
        vertexConsumer.addVertex((Matrix4fc)pose.pose(), x3, y3, z3).setColor(r, g, b, a);
    }
}
