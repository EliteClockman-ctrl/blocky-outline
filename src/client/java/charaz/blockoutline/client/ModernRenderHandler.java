package charaz.blockoutline.client;

import charaz.blockoutline.client.renderer.OutlineRenderer;
import charaz.blockoutline.config.BlockyOutlineSettings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ModernRenderHandler {
    private static boolean initializedPos = false;
    private static double smoothedX = 0.0;
    private static double smoothedY = 0.0;
    private static double smoothedZ = 0.0;
    private static long lastRenderTimeMs = 0L;

    private ModernRenderHandler() {
    }

    public static void register() {
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, renderState) -> {
            if (renderState == null) {
                return true;
            }
            BlockPos pos = renderState.pos();
            VoxelShape shape = renderState.shape();
            if (pos == null || shape == null || shape.isEmpty()) {
                initializedPos = false;
                return true;
            }
            BlockyOutlineSettings s = BlockyOutlineSettings.get();
            PoseStack matrices = context.poseStack();
            if (matrices == null) {
                return true;
            }
            if (context.levelState() == null || context.levelState().cameraRenderState == null) {
                return true;
            }
            Vec3 cam = context.levelState().cameraRenderState.pos;
            if (cam == null) {
                return true;
            }

            double targetX = (double) pos.getX();
            double targetY = (double) pos.getY();
            double targetZ = (double) pos.getZ();

            long now = System.currentTimeMillis();

            if (s.smoothTransition) {
                if (!initializedPos) {
                    smoothedX = targetX;
                    smoothedY = targetY;
                    smoothedZ = targetZ;
                    initializedPos = true;
                    lastRenderTimeMs = now;
                } else {
                    long dt = Math.max(1L, Math.min(100L, now - lastRenderTimeMs));
                    lastRenderTimeMs = now;
                    double distSq = (targetX - smoothedX) * (targetX - smoothedX) +
                                    (targetY - smoothedY) * (targetY - smoothedY) +
                                    (targetZ - smoothedZ) * (targetZ - smoothedZ);
                    if (distSq > 25.0 || distSq < 0.0001) {
                        smoothedX = targetX;
                        smoothedY = targetY;
                        smoothedZ = targetZ;
                    } else {
                        double step = 1.0 - Math.exp(-0.120 * (double) dt);
                        smoothedX += (targetX - smoothedX) * step;
                        smoothedY += (targetY - smoothedY) * step;
                        smoothedZ += (targetZ - smoothedZ) * step;
                    }
                }
            } else {
                smoothedX = targetX;
                smoothedY = targetY;
                smoothedZ = targetZ;
                initializedPos = true;
            }

            double dx = smoothedX - cam.x;
            double dy = smoothedY - cam.y;
            double dz = smoothedZ - cam.z;

            matrices.pushPose();
            matrices.translate(dx, dy, dz);

            int outlineColor = s.getOutlineArgb(now);
            context.submitNodeCollector().submitShapeOutline(matrices, shape, RenderTypes.lines(), outlineColor, s.outlineWidth, false);

            if (s.fillEnabled) {
                int fillColor = s.getFillArgb(now);
                context.submitNodeCollector().submitCustomGeometry(
                        matrices,
                        RenderTypes.debugFilledBox(),
                        (pose, consumer) -> OutlineRenderer.renderFilledBox(consumer, pose, shape, fillColor)
                );
            }

            matrices.popPose();
            return false;
        });
    }
}
