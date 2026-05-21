package charaz.blockoutline.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class OutlineRenderer {

    private OutlineRenderer() {}

    /**
     * Renders the voxel outline of a block using the lines buffer
     */
    public static void renderOutline(PoseStack poseStack, VertexConsumer vc, VoxelShape shape, double dx, double dy, double dz, int colorARGB, float width) {
        ShapeRenderer.renderShape(poseStack, vc, shape, dx, dy, dz, colorARGB, width);
    }

    /**
     * Renders a solid filled box inside the block using the debugFilledBox render type.
     * Iterates over all bounding boxes of the shape and applies a tiny inflation factor so that
     * the translucent faces are drawn slightly in front of solid block textures, passing the depth test.
     */
    public static void renderFilledBox(PoseStack poseStack, VertexConsumer vc, VoxelShape shape, double dx, double dy, double dz, int colorARGB) {
        float r = ((colorARGB >> 16) & 0xFF) / 255f;
        float g = ((colorARGB >>  8) & 0xFF) / 255f;
        float b = ((colorARGB      ) & 0xFF) / 255f;
        float a = ((colorARGB >> 24) & 0xFF) / 255f;

        PoseStack.Pose pose = poseStack.last();

        for (AABB box : shape.toAabbs()) {
            // Apply a tiny inflation factor (0.002 blocks) to push the faces slightly OUTSIDE the block.
            // This allows the depth test to pass in front of solid block textures without Z-fighting.
            double inflate = 0.005D;

            float x0 = (float) (dx + box.minX - inflate);
            float y0 = (float) (dy + box.minY - inflate);
            float z0 = (float) (dz + box.minZ - inflate);
            float x1 = (float) (dx + box.maxX + inflate);
            float y1 = (float) (dy + box.maxY + inflate);
            float z1 = (float) (dz + box.maxZ + inflate);

            // bottom (y-)
            addQuad(vc, pose, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, r, g, b, a);
            addQuad(vc, pose, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a);
            // top (y+)
            addQuad(vc, pose, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, r, g, b, a);
            addQuad(vc, pose, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, r, g, b, a);
            // north (z-)
            addQuad(vc, pose, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a);
            addQuad(vc, pose, x1, y1, z0, x0, y1, z0, x0, y0, z0, x1, y0, z0, r, g, b, a);
            // south (z+)
            addQuad(vc, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a);
            addQuad(vc, pose, x0, y1, z1, x1, y1, z1, x1, y0, z1, x0, y0, z1, r, g, b, a);
            // west (x-)
            addQuad(vc, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a);
            addQuad(vc, pose, x0, y1, z0, x0, y1, z1, x0, y0, z1, x0, y0, z0, r, g, b, a);
            // east (x+)
            addQuad(vc, pose, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a);
            addQuad(vc, pose, x1, y1, z1, x1, y1, z0, x1, y0, z0, x1, y0, z1, r, g, b, a);
        }
    }

    private static void addQuad(VertexConsumer vc, PoseStack.Pose pose,
                                 float x0, float y0, float z0,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3,
                                 float r, float g, float b, float a) {
        vc.addVertex(pose, x0, y0, z0).setColor(r, g, b, a);
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a);
        vc.addVertex(pose, x3, y3, z3).setColor(r, g, b, a);
    }
}
