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
import net.minecraft.client.renderer.MultiBufferSource;
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
    private static boolean initializedPos;

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();

        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) -> {
            BlockyOutlineSettings s = BlockyOutlineSettings.get();
            boolean renderOutline = s.outlineOpacity > 0.001f;
            boolean renderFill = s.fillEnabled && s.fillOpacity > 0.001f;

            if (!renderOutline && !renderFill) {
                return false;
            }

            if (outlineRenderState == null) {
                initializedPos = false;
                return true;
            }

            BlockPos pos = outlineRenderState.pos();
            VoxelShape shape = outlineRenderState.shape();
            if (pos == null || shape == null || shape.isEmpty()) {
                initializedPos = false;
                return true;
            }

            PoseStack matrices = context.poseStack();
            MultiBufferSource bufferSource = context.bufferSource();
            if (matrices == null || bufferSource == null) {
                return false;
            }

            double tx = pos.getX(), ty = pos.getY(), tz = pos.getZ();

            if (s.smoothTransition) {
                if (!initializedPos) {
                    smoothedX = tx; smoothedY = ty; smoothedZ = tz;
                    initializedPos = true;
                } else {
                    double dx = tx - smoothedX, dy = ty - smoothedY, dz = tz - smoothedZ;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > 36.0 || distSq < 0.000001) {
                        smoothedX = tx; smoothedY = ty; smoothedZ = tz;
                    } else {
                        double factor = Math.min(1.0, Math.max(0.05, (double) s.smoothSpeed));
                        smoothedX += dx * factor;
                        smoothedY += dy * factor;
                        smoothedZ += dz * factor;
                    }
                }
            } else {
                smoothedX = tx; smoothedY = ty; smoothedZ = tz;
                initializedPos = true;
            }

            Minecraft mc = Minecraft.getInstance();
            Vec3 cam = mc.gameRenderer.getMainCamera().position();
            double ox = smoothedX - cam.x, oy = smoothedY - cam.y, oz = smoothedZ - cam.z;
            long now = System.currentTimeMillis();

            if (renderOutline) {
                int outlineArgb = s.getOutlineArgb(now);
                float a = (float)(outlineArgb >>> 24) * 0.003921569f;
                if (a > 0.001f) {
                    float r = (float)((outlineArgb >>> 16) & 0xFF) * 0.003921569f;
                    float g = (float)((outlineArgb >>> 8) & 0xFF) * 0.003921569f;
                    float b = (float)(outlineArgb & 0xFF) * 0.003921569f;
                    VertexConsumer lines = bufferSource.getBuffer(RenderTypes.lines());
                    OutlineRenderer.renderOutline(matrices, lines, shape, ox, oy, oz, r, g, b, a, s.outlineWidth);
                }
            }

            if (renderFill) {
                VertexConsumer quads = bufferSource.getBuffer(RenderTypes.debugQuads());
                OutlineRenderer.renderFilledBox(matrices, quads, shape, ox, oy, oz, s.getFillArgb(now), s.getFillArgb2(now));
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
            while (menuKeyBinding != null && menuKeyBinding.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new BlockyOutlineMenuScreen());
                }
            }
        });
    }
}
