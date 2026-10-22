package charaz.blockoutline.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import charaz.blockoutline.client.renderer.OutlineRenderer;
import charaz.blockoutline.client.ui.BlockyOutlineMenuScreen;
import charaz.blockoutline.config.BlockyOutlineSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

public final class BlockyOutlineClient implements ClientModInitializer {
    private static final String CATEGORY = "key.categories.misc";
    private static KeyMapping menuKeyBinding;

    private static double smoothedX;
    private static double smoothedY;
    private static double smoothedZ;
    private static boolean hasTarget;
    private static long lastTargetTimeMs = 0;
    private static long lastFrameNano = 0;

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hitResult) -> {
            if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult blockHit)) {
                hasTarget = false;
                return true;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return true;
            }

            BlockyOutlineSettings s = BlockyOutlineSettings.get();
            boolean renderOutline = s.outlineOpacity > 0.001f;
            boolean renderFill = s.fillEnabled && s.fillOpacity > 0.001f;

            if (!renderOutline && !renderFill) {
                return false;
            }

            BlockPos pos = blockHit.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            VoxelShape shape = state.getShape(mc.level, pos);
            if (shape.isEmpty()) {
                hasTarget = false;
                return true;
            }

            PoseStack matrices = context.matrixStack();
            MultiBufferSource consumers = context.consumers();
            if (matrices == null || consumers == null) {
                return false;
            }

            double tx = pos.getX();
            double ty = pos.getY();
            double tz = pos.getZ();

            long nowMs = System.currentTimeMillis();
            long nowNano = System.nanoTime();

            boolean continuous = hasTarget && (nowMs - lastTargetTimeMs < 120);
            lastTargetTimeMs = nowMs;
            hasTarget = true;

            double dt = (lastFrameNano == 0 || (nowNano - lastFrameNano > 100_000_000L))
                    ? 0.016
                    : (nowNano - lastFrameNano) * 1e-9;
            lastFrameNano = nowNano;
            if (dt < 0.001) dt = 0.001;
            else if (dt > 0.05) dt = 0.05;

            if (s.smoothTransition && continuous) {
                double dx = tx - smoothedX;
                double dy = ty - smoothedY;
                double dz = tz - smoothedZ;
                double distSq = dx * dx + dy * dy + dz * dz;

                if (distSq > 9.0 || distSq < 0.00002) {
                    smoothedX = tx;
                    smoothedY = ty;
                    smoothedZ = tz;
                } else {
                    double speedFactor = Math.max(0.1, (double) s.smoothSpeed);
                    double lambda = 24.0 + speedFactor * 24.0;
                    double blend = 1.0 - Math.exp(-lambda * dt);
                    smoothedX += dx * blend;
                    smoothedY += dy * blend;
                    smoothedZ += dz * blend;
                }
            } else {
                smoothedX = tx;
                smoothedY = ty;
                smoothedZ = tz;
            }

            Vec3 cam = context.camera().getPosition();
            double ox = smoothedX - cam.x;
            double oy = smoothedY - cam.y;
            double oz = smoothedZ - cam.z;
            long now = System.currentTimeMillis();

            if (renderOutline) {
                int outlineArgb = s.getOutlineArgb(now);
                if ((outlineArgb >>> 24) > 0) {
                    RenderType lineType = RenderType.lines();
                    VertexConsumer lines = consumers.getBuffer(lineType);
                    OutlineRenderer.renderOutline(matrices, lines, shape, ox, oy, oz, outlineArgb,
                            s.outlineWidth, s.cornerMode, s.cornerLength, s.hitboxExpand);
                }
            }

            if (renderFill) {
                RenderType quadType = RenderType.debugQuads();
                VertexConsumer quads = consumers.getBuffer(quadType);
                OutlineRenderer.renderFilledBox(matrices, quads, shape, ox, oy, oz,
                        s.getFillArgb(now), s.getFillArgb2(now), s.hitboxExpand);
            }

            return false;
        });

        menuKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.blocky-outline.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
                hasTarget = false;
            }
            while (menuKeyBinding != null && menuKeyBinding.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new BlockyOutlineMenuScreen());
                }
            }
        });
    }
}
