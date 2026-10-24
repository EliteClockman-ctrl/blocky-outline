package charaz.blockoutline.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import charaz.blockoutline.client.compat.CompatBridge;
import charaz.blockoutline.client.compat.TargetInfo;
import charaz.blockoutline.client.renderer.OutlineRenderer;
import charaz.blockoutline.config.BlockyOutlineSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

public final class BlockyOutlineClient implements ClientModInitializer {
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

        CompatBridge.registerWorldRenderEvent((context, targetObj) -> {
            if (targetObj == null) {
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

            TargetInfo target = CompatBridge.TARGET_EXTRACTOR != null
                    ? CompatBridge.TARGET_EXTRACTOR.extract(targetObj, mc)
                    : null;
            if (target == null) {
                hasTarget = false;
                return true;
            }

            BlockPos pos = target.pos();
            VoxelShape shape = target.shape();
            if (pos == null || shape == null || shape.isEmpty()) {
                hasTarget = false;
                return true;
            }

            PoseStack matrices = CompatBridge.getPoseStack(context);
            MultiBufferSource consumers = CompatBridge.getBufferSource(context);
            if (matrices == null || consumers == null) {
                return false;
            }

            BlockState state = mc.level.getBlockState(pos);
            Vec3 offset = CompatBridge.getBlockOffset(state, mc.level, pos);
            double tx = (double) pos.getX() + offset.x;
            double ty = (double) pos.getY() + offset.y;
            double tz = (double) pos.getZ() + offset.z;

            long nowMs = System.currentTimeMillis();
            long nowNano = System.nanoTime();

            long timeSinceLast = nowMs - lastTargetTimeMs;
            lastTargetTimeMs = nowMs;

            double diffX = tx - lastTargetX;
            double diffY = ty - lastTargetY;
            double diffZ = tz - lastTargetZ;
            double targetDiffSq = diffX * diffX + diffY * diffY + diffZ * diffZ;

            // Snap immediately with 0ms delay if crosshair just arrived from air (>100ms air gap),
            // or moved across a large jump (>2.5 blocks away).
            boolean isContinuousNeighbor = hasTarget && (timeSinceLast <= 100L) && (targetDiffSq <= 6.25);

            if (!isContinuousNeighbor) {
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
            } else if (targetDiffSq > 0.0001) {
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

            if (s.smoothTransition && isContinuousNeighbor) {
                updateMotion(s, tx, ty, tz, dt, nowMs);
            } else {
                currX = tx;
                currY = ty;
                currZ = tz;
                velX = 0.0;
                velY = 0.0;
                velZ = 0.0;
            }

            if (mc.gameRenderer == null || mc.gameRenderer.getMainCamera() == null) {
                return false;
            }

            Vec3 cam = CompatBridge.getCameraPos(mc.gameRenderer.getMainCamera());
            double ox = currX - cam.x;
            double oy = currY - cam.y;
            double oz = currZ - cam.z;

            boolean xray = s.xrayMode;

            if (renderOutline && CompatBridge.BUFFER_PROVIDER != null) {
                int outlineArgb = s.getOutlineArgb(nowMs);
                if ((outlineArgb >>> 24) > 0) {
                    VertexConsumer lines = CompatBridge.BUFFER_PROVIDER.getLineBuffer(consumers, xray);
                    if (lines != null) {
                        OutlineRenderer.renderOutline(matrices, lines, shape, ox, oy, oz, outlineArgb,
                                s.outlineWidth, s.cornerMode, s.cornerLength, s.hitboxExpand);
                    }
                }
            }

            if (renderFill && CompatBridge.BUFFER_PROVIDER != null) {
                VertexConsumer quads = CompatBridge.BUFFER_PROVIDER.getQuadBuffer(consumers, xray);
                if (quads != null) {
                    OutlineRenderer.renderFilledBox(matrices, quads, shape, ox, oy, oz,
                            s.getFillArgb(nowMs), s.getFillArgb2(nowMs), s.hitboxExpand);
                }
            }

            if (CompatBridge.BUFFER_PROVIDER != null) {
                CompatBridge.BUFFER_PROVIDER.postRender(consumers, xray);
            }

            return false;
        });

        menuKeyBinding = KeyBindingHelper.registerKeyBinding(CompatBridge.createMenuKeyMapping(
                "key.blocky-outline.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "key.categories.misc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKeyBinding != null && menuKeyBinding.consumeClick()) {
                if (mc.screen == null) {
                    CompatBridge.openMenuScreen(mc);
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("report")
                    .executes(context -> {
                        CompatBridge.openUrl("https://github.com/EliteClockman-ctrl/blocky-outline/issues");
                        context.getSource().sendFeedback(CompatBridge.createReportMessage());
                        return 1;
                    })
            );
            dispatcher.register(ClientCommandManager.literal("blockyoutline")
                    .then(ClientCommandManager.literal("report")
                            .executes(context -> {
                                CompatBridge.openUrl("https://github.com/EliteClockman-ctrl/blocky-outline/issues");
                                context.getSource().sendFeedback(CompatBridge.createReportMessage());
                                return 1;
                            })
                    )
            );
        });
    }

    private static void updateMotion(BlockyOutlineSettings s, double tx, double ty, double tz, double dt, long nowMs) {
        if (currX == tx && currY == ty && currZ == tz && velX == 0.0 && velY == 0.0 && velZ == 0.0) {
            return;
        }

        switch (s.motionMode) {
            case 0 -> { // Snappy Spring (4 sub-steps, ultra-stable, physical feel)
                double k = Math.max(150.0, (double) s.springStiffness);
                double c = Math.max(18.0, (double) s.springDamping);
                double subDt = dt * 0.25;
                for (int step = 0; step < 4; step++) {
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
                        && Math.abs(velX) < 0.005 && Math.abs(velY) < 0.005 && Math.abs(velZ) < 0.005) {
                    currX = tx;
                    currY = ty;
                    currZ = tz;
                    velX = 0.0;
                    velY = 0.0;
                    velZ = 0.0;
                }
            }
            case 1 -> { // Modern Ease-Out Back (Snappy 1-bounce overshoot)
                double dur = Math.max(25.0, (double) s.motionDurationMs);
                double progress = Math.min(1.0, (double) (nowMs - animStartTimeMs) / dur);
                if (progress >= 1.0) {
                    currX = tx;
                    currY = ty;
                    currZ = tz;
                } else {
                    double p1 = progress - 1.0;
                    double c1 = Math.min(1.8, Math.max(0.5, (double) s.backOvershoot));
                    double c3 = c1 + 1.0;
                    double ease = 1.0 + c3 * p1 * p1 * p1 + c1 * p1 * p1;
                    currX = startX + (tx - startX) * ease;
                    currY = startY + (ty - startY) * ease;
                    currZ = startZ + (tz - startZ) * ease;
                }
            }
            case 2 -> { // Fast Quintic (Instant eye lock-on, zero latency perception)
                double dur = Math.max(25.0, (double) s.motionDurationMs);
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
            case 3 -> { // Fluid Apple Bezier
                double dur = Math.max(25.0, (double) s.motionDurationMs);
                double progress = Math.min(1.0, (double) (nowMs - animStartTimeMs) / dur);
                if (progress >= 1.0) {
                    currX = tx;
                    currY = ty;
                    currZ = tz;
                } else {
                    double ease = solveCubicBezier(0.16, 1.0, 0.3, 1.0, progress);
                    currX = startX + (tx - startX) * ease;
                    currY = startY + (ty - startY) * ease;
                    currZ = startZ + (tz - startZ) * ease;
                }
            }
            case 4 -> { // Smooth Lerp (Exponential decay, responsive)
                double dx = tx - currX;
                double dy = ty - currY;
                double dz = tz - currZ;
                double speedFactor = Math.max(0.1, (double) s.smoothSpeed);
                double lambda = 30.0 + speedFactor * 45.0;
                double blend = 1.0 - Math.exp(-lambda * dt);
                currX += dx * blend;
                currY += dy * blend;
                currZ += dz * blend;
            }
            default -> { // Instant (Raw)
                currX = tx;
                currY = ty;
                currZ = tz;
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
