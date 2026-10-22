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

    private static double currX, currY, currZ;
    private static double velX, velY, velZ;
    private static double startX, startY, startZ;
    private static double lastTargetX, lastTargetY, lastTargetZ;
    private static long animStartTimeMs = 0;
    private static boolean hasTarget = false;
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

            Vec3 offset = state.getOffset(mc.level, pos);
            double tx = (double) pos.getX() + offset.x;
            double ty = (double) pos.getY() + offset.y;
            double tz = (double) pos.getZ() + offset.z;

            long nowMs = System.currentTimeMillis();
            long nowNano = System.nanoTime();

            boolean continuous = hasTarget && (nowMs - lastTargetTimeMs < 120);
            lastTargetTimeMs = nowMs;

            double diffX = tx - lastTargetX;
            double diffY = ty - lastTargetY;
            double diffZ = tz - lastTargetZ;
            double targetDiffSq = diffX * diffX + diffY * diffY + diffZ * diffZ;

            if (!continuous || targetDiffSq > 25.0) {
                currX = tx;
                currY = ty;
                currZ = tz;
                velX = 0.0;
                velY = 0.0;
                velZ = 0.0;
                startX = tx;
                startY = ty;
                startZ = tz;
                animStartTimeMs = nowMs;
            } else if (targetDiffSq > 0.00005) {
                startX = currX;
                startY = currY;
                startZ = currZ;
                animStartTimeMs = nowMs;
            }
            lastTargetX = tx;
            lastTargetY = ty;
            lastTargetZ = tz;
            hasTarget = true;

            double dt = (lastFrameNano == 0 || (nowNano - lastFrameNano > 100_000_000L))
                    ? 0.016
                    : (nowNano - lastFrameNano) * 1e-9;
            lastFrameNano = nowNano;
            if (dt < 0.001) dt = 0.001;
            else if (dt > 0.05) dt = 0.05;

            if (s.smoothTransition && continuous) {
                updateMotion(s, tx, ty, tz, dt, nowMs);
            } else {
                currX = tx;
                currY = ty;
                currZ = tz;
                velX = 0.0;
                velY = 0.0;
                velZ = 0.0;
            }

            Vec3 cam = context.camera().getPosition();
            double ox = currX - cam.x;
            double oy = currY - cam.y;
            double oz = currZ - cam.z;

            if (renderOutline) {
                int outlineArgb = s.getOutlineArgb(nowMs);
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
                        s.getFillArgb(nowMs), s.getFillArgb2(nowMs), s.hitboxExpand);
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

    private static void updateMotion(BlockyOutlineSettings s, double tx, double ty, double tz, double dt, long nowMs) {
        switch (s.motionMode) {
            case 0 -> { // Damped Spring Physics (preserves velocity across frames)
                double speedMult = 0.5 + (double) s.smoothSpeed * 0.8;
                double k = (double) s.springStiffness * speedMult;
                double c = (double) s.springDamping;
                double subDt = dt * 0.5;
                for (int step = 0; step < 2; step++) {
                    double fx = -k * (currX - tx) - c * velX;
                    double fy = -k * (currY - ty) - c * velY;
                    double fz = -k * (currZ - tz) - c * velZ;
                    velX += fx * subDt;
                    velY += fy * subDt;
                    velZ += fz * subDt;
                    currX += velX * subDt;
                    currY += velY * subDt;
                    currZ += velZ * subDt;
                }
                if (Math.abs(currX - tx) < 0.0005 && Math.abs(currY - ty) < 0.0005 && Math.abs(currZ - tz) < 0.0005
                        && Math.abs(velX) < 0.01 && Math.abs(velY) < 0.01 && Math.abs(velZ) < 0.01) {
                    currX = tx;
                    currY = ty;
                    currZ = tz;
                    velX = 0.0;
                    velY = 0.0;
                    velZ = 0.0;
                }
            }
            case 1 -> { // Ease-Out Back (Snappy Overshoot & Pullback)
                double dur = Math.max(40.0, (double) s.motionDurationMs / Math.max(0.2, (double) s.smoothSpeed));
                double progress = Math.min(1.0, (double) (nowMs - animStartTimeMs) / dur);
                if (progress >= 1.0) {
                    currX = tx;
                    currY = ty;
                    currZ = tz;
                } else {
                    double p1 = progress - 1.0;
                    double c1 = (double) s.backOvershoot;
                    double c3 = c1 + 1.0;
                    double ease = 1.0 + c3 * p1 * p1 * p1 + c1 * p1 * p1;
                    currX = startX + (tx - startX) * ease;
                    currY = startY + (ty - startY) * ease;
                    currZ = startZ + (tz - startZ) * ease;
                }
            }
            case 2 -> { // Ease-Out Circ (High initial speed, soft landing)
                double dur = Math.max(40.0, (double) s.motionDurationMs / Math.max(0.2, (double) s.smoothSpeed));
                double progress = Math.min(1.0, (double) (nowMs - animStartTimeMs) / dur);
                if (progress >= 1.0) {
                    currX = tx;
                    currY = ty;
                    currZ = tz;
                } else {
                    double p1 = progress - 1.0;
                    double ease = Math.sqrt(Math.max(0.0, 1.0 - p1 * p1));
                    currX = startX + (tx - startX) * ease;
                    currY = startY + (ty - startY) * ease;
                    currZ = startZ + (tz - startZ) * ease;
                }
            }
            case 3 -> { // Ease-Out Quint (Extreme contrast deceleration)
                double dur = Math.max(40.0, (double) s.motionDurationMs / Math.max(0.2, (double) s.smoothSpeed));
                double progress = Math.min(1.0, (double) (nowMs - animStartTimeMs) / dur);
                if (progress >= 1.0) {
                    currX = tx;
                    currY = ty;
                    currZ = tz;
                } else {
                    double p1 = 1.0 - progress;
                    double ease = 1.0 - p1 * p1 * p1 * p1 * p1;
                    currX = startX + (tx - startX) * ease;
                    currY = startY + (ty - startY) * ease;
                    currZ = startZ + (tz - startZ) * ease;
                }
            }
            case 4 -> { // Fluid Cubic Bezier (Apple / Material fluid curve: 0.25, 1.0, 0.5, 1.0)
                double dur = Math.max(40.0, (double) s.motionDurationMs / Math.max(0.2, (double) s.smoothSpeed));
                double progress = Math.min(1.0, (double) (nowMs - animStartTimeMs) / dur);
                if (progress >= 1.0) {
                    currX = tx;
                    currY = ty;
                    currZ = tz;
                } else {
                    double ease = solveCubicBezier(0.25, 1.0, 0.5, 1.0, progress);
                    currX = startX + (tx - startX) * ease;
                    currY = startY + (ty - startY) * ease;
                    currZ = startZ + (tz - startZ) * ease;
                }
            }
            default -> { // Classic Exponential Lerp
                double dx = tx - currX;
                double dy = ty - currY;
                double dz = tz - currZ;
                double speedFactor = Math.max(0.1, (double) s.smoothSpeed);
                double lambda = 24.0 + speedFactor * 24.0;
                double blend = 1.0 - Math.exp(-lambda * dt);
                currX += dx * blend;
                currY += dy * blend;
                currZ += dz * blend;
            }
        }
    }

    private static double solveCubicBezier(double x1, double y1, double x2, double y2, double x) {
        if (x <= 0.0) return 0.0;
        if (x >= 1.0) return 1.0;
        double t = x;
        for (int i = 0; i < 4; i++) {
            double currentX = 3.0 * (1.0 - t) * (1.0 - t) * t * x1 + 3.0 * (1.0 - t) * t * t * x2 + t * t * t;
            double dx = 3.0 * (1.0 - t) * (1.0 - t) * x1 + 6.0 * (1.0 - t) * t * (x2 - x1) + 3.0 * t * t * (1.0 - x2);
            if (Math.abs(dx) < 1e-6) break;
            t -= (currentX - x) / dx;
            t = Math.max(0.0, Math.min(1.0, t));
        }
        return 3.0 * (1.0 - t) * (1.0 - t) * t * y1 + 3.0 * (1.0 - t) * t * t * y2 + t * t * t;
    }
}
