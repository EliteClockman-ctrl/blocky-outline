package charaz.blockoutline.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import charaz.blockoutline.client.renderer.OutlineRenderer;
import charaz.blockoutline.client.ui.BlockyOutlineMenuScreen;
import charaz.blockoutline.config.BlockyOutlineSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

public final class BlockyOutlineClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("blocky-outline", "general"));
    private static KeyMapping menuKeyBinding;

    private static double smoothedX;
    private static double smoothedY;
    private static double smoothedZ;
    private static boolean hasTarget;
    private static long lastTargetTimeMs = 0;
    private static long lastFrameNano = 0;

    private static java.lang.reflect.Method levelStateMethod;
    private static java.lang.reflect.Field cameraRenderStateField;
    private static java.lang.reflect.Field cameraPosField;
    private static java.lang.reflect.Method mainCameraMethod;

    static {
        try {
            levelStateMethod = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext")
                    .getMethod("levelState");
            cameraRenderStateField = Class.forName("net.minecraft.client.renderer.state.LevelRenderState")
                    .getField("cameraRenderState");
            cameraPosField = Class.forName("net.minecraft.client.renderer.state.CameraRenderState")
                    .getField("pos");
        } catch (Throwable ignored) {
            levelStateMethod = null;
        }

        try {
            var grClass = net.minecraft.client.renderer.GameRenderer.class;
            try {
                mainCameraMethod = grClass.getMethod("mainCamera");
            } catch (Throwable t) {
                mainCameraMethod = grClass.getMethod("getMainCamera");
            }
        } catch (Throwable ignored) {
            mainCameraMethod = null;
        }
    }

    private static Vec3 getCameraPos(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
        if (levelStateMethod != null) {
            try {
                Object ls = levelStateMethod.invoke(context);
                if (ls != null) {
                    Object crs = cameraRenderStateField.get(ls);
                    if (crs != null) {
                        return (Vec3) cameraPosField.get(crs);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        if (mainCameraMethod != null) {
            try {
                net.minecraft.client.Camera cam = (net.minecraft.client.Camera) mainCameraMethod.invoke(Minecraft.getInstance().gameRenderer);
                if (cam != null) {
                    return cam.position();
                }
            } catch (Throwable ignored) {
            }
        }
        var player = Minecraft.getInstance().player;
        return player != null ? player.getEyePosition() : Vec3.ZERO;
    }

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();

        // Pre-warm custom render pipelines early to avoid first-frame shader compilation lag
        try {
            net.minecraft.client.renderer.rendertype.BlockyOutlineRenderTypes.xrayLines();
            net.minecraft.client.renderer.rendertype.BlockyOutlineRenderTypes.xrayQuads();
        } catch (Throwable ignored) {
        }

        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) -> {
            BlockyOutlineSettings s = BlockyOutlineSettings.get();
            boolean renderOutline = s.outlineOpacity > 0.001f;
            boolean renderFill = s.fillEnabled && s.fillOpacity > 0.001f;

            if (!renderOutline && !renderFill) {
                return false;
            }

            if (outlineRenderState == null) {
                hasTarget = false;
                return true;
            }

            BlockPos pos = outlineRenderState.pos();
            VoxelShape shape = outlineRenderState.shape();
            if (pos == null || shape == null || shape.isEmpty()) {
                hasTarget = false;
                return true;
            }

            PoseStack matrices = context.poseStack();
            SubmitNodeCollector collector = context.submitNodeCollector();
            if (matrices == null || collector == null) {
                return false;
            }

            double tx = pos.getX(), ty = pos.getY(), tz = pos.getZ();

            long nowMs = System.currentTimeMillis();
            long nowNano = System.nanoTime();

            // Detect whether the player was continuously targeting blocks
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

                // Smoothly glide between nearby blocks (<= 3 blocks away)
                if (distSq > 9.0 || distSq < 0.00002) {
                    smoothedX = tx;
                    smoothedY = ty;
                    smoothedZ = tz;
                } else {
                    // Snappy, responsive exponential ease-out (like Custom Block Highlight)
                    double speedFactor = Math.max(0.1, (double) s.smoothSpeed);
                    double lambda = 24.0 + speedFactor * 24.0;
                    double blend = 1.0 - Math.exp(-lambda * dt);
                    smoothedX += dx * blend;
                    smoothedY += dy * blend;
                    smoothedZ += dz * blend;
                }
            } else {
                // Instant appearance with zero delay when freshly aiming from air or far away
                smoothedX = tx;
                smoothedY = ty;
                smoothedZ = tz;
            }

            Vec3 cam = getCameraPos(context);
            double ox = smoothedX - cam.x, oy = smoothedY - cam.y, oz = smoothedZ - cam.z;
            long now = System.currentTimeMillis();

            if (renderOutline) {
                int outlineArgb = s.getOutlineArgb(now);
                if ((outlineArgb >>> 24) > 0) {
                    net.minecraft.client.renderer.rendertype.RenderType lineType = s.xrayMode
                            ? net.minecraft.client.renderer.rendertype.BlockyOutlineRenderTypes.xrayLines()
                            : RenderTypes.lines();
                    collector.submitCustomGeometry(matrices, lineType, (pose, lines) -> {
                        OutlineRenderer.renderOutline(matrices, lines, shape, ox, oy, oz, outlineArgb, s.outlineWidth, s.cornerMode, s.cornerLength, s.hitboxExpand);
                    });
                }
            }

            if (renderFill) {
                net.minecraft.client.renderer.rendertype.RenderType quadType = s.xrayMode
                            ? net.minecraft.client.renderer.rendertype.BlockyOutlineRenderTypes.xrayQuads()
                            : RenderTypes.debugQuads();
                collector.submitCustomGeometry(matrices, quadType, (pose, quads) -> {
                    OutlineRenderer.renderFilledBox(matrices, quads, shape, ox, oy, oz, s.getFillArgb(now), s.getFillArgb2(now), s.hitboxExpand);
                });
            }

            return false;
        });

        menuKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.blocky-outline.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.hitResult == null || mc.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
                hasTarget = false;
            }
            while (menuKeyBinding != null && menuKeyBinding.consumeClick()) {
                if (mc.gui.screen() == null) {
                    mc.gui.setScreen(new BlockyOutlineMenuScreen());
                }
            }
        });
    }
}
