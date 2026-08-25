package charaz.blockoutline.client;

import charaz.blockoutline.client.renderer.OutlineRenderer;
import charaz.blockoutline.client.ui.BlockyOutlineMenuScreen;
import charaz.blockoutline.config.BlockyOutlineSettings;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockyOutlineClient implements ClientModInitializer {
    private static KeyMapping menuKeyBinding = null;

    private static boolean initializedPos = false;
    private static double smoothedX = 0.0;
    private static double smoothedY = 0.0;
    private static double smoothedZ = 0.0;
    private static long lastRenderTimeMs = 0L;

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();
        this.registerMenuHotkey();

        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, renderState) -> {
            try {
                if (renderState == null || context == null) {
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
                    long dt = now - lastRenderTimeMs;
                    if (dt < 1L) dt = 1L;
                    else if (dt > 100L) dt = 100L;
                    lastRenderTimeMs = now;

                    double deltaX = targetX - smoothedX;
                    double deltaY = targetY - smoothedY;
                    double deltaZ = targetZ - smoothedZ;
                    double distSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

                    if (distSq > 36.0 || distSq < 0.00005) {
                        smoothedX = targetX;
                        smoothedY = targetY;
                        smoothedZ = targetZ;
                    } else {
                        // Hardcoded optimal 60% smooth speed factor (-0.132)
                        double step = 1.0 - Math.exp(-0.132 * (double) dt);
                        smoothedX += deltaX * step;
                        smoothedY += deltaY * step;
                        smoothedZ += deltaZ * step;
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
                int fillColor2 = s.getFillArgb2(now);
                boolean twoColor = s.fillTwoColor;
                context.submitNodeCollector().submitCustomGeometry(
                        matrices,
                        RenderTypes.debugFilledBox(),
                        (pose, consumer) -> OutlineRenderer.renderFilledBox(consumer, pose, shape, fillColor, fillColor2, twoColor)
                );
            }

            matrices.popPose();
            return false;
        } catch (Throwable t) {
            return true; // Graceful fallback: let vanilla render if any version mismatch occurs
        }
    });
}

    private void registerMenuHotkey() {
        menuKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.blocky-outline.open_menu",
                InputConstants.Type.KEYSYM,
                77,
                KeyMapping.Category.MISC
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            while (menuKeyBinding.consumeClick()) {
                if (!(client.gui.screen() instanceof BlockyOutlineMenuScreen)) {
                    client.setScreenAndShow(new BlockyOutlineMenuScreen());
                    continue;
                }
                client.setScreenAndShow(null);
            }
        });
    }
}
