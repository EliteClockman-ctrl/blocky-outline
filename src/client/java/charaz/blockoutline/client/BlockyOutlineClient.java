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
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

public final class BlockyOutlineClient implements ClientModInitializer {
    private static KeyMapping menuKeyBinding;

    private static double smoothedX = 0.0;
    private static double smoothedY = 0.0;
    private static double smoothedZ = 0.0;
    private static boolean initializedPos = false;

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) -> {
            Minecraft client = Minecraft.getInstance();
            HitResult hitResult = client.hitResult;
            if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult blockHit)) {
                return true;
            }
            BlockPos pos = blockHit.getBlockPos();
            ClientLevel level = client.level;
            if (pos == null || level == null) {
                initializedPos = false;
                return true;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                initializedPos = false;
                return true;
            }

            VoxelShape shape = state.getShape(level, pos);
            if (shape.isEmpty()) {
                initializedPos = false;
                return true;
            }

            Vec3 offset = state.getOffset(pos);
            if (offset.x != 0.0 || offset.y != 0.0 || offset.z != 0.0) {
                shape = shape.move(offset.x, offset.y, offset.z);
            }

            Vec3 cam = client.gameRenderer.getMainCamera().position();
            PoseStack matrices = context.matrices();
            if (matrices == null) return true;

            BlockyOutlineSettings s = BlockyOutlineSettings.get();
            double targetX = pos.getX();
            double targetY = pos.getY();
            double targetZ = pos.getZ();

            if (s.smoothTransition) {
                if (!initializedPos) {
                    smoothedX = targetX;
                    smoothedY = targetY;
                    smoothedZ = targetZ;
                    initializedPos = true;
                } else {
                    double dx = targetX - smoothedX;
                    double dy = targetY - smoothedY;
                    double dz = targetZ - smoothedZ;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > 36.0 || distSq < 0.000001) {
                        smoothedX = targetX;
                        smoothedY = targetY;
                        smoothedZ = targetZ;
                    } else {
                        smoothedX += dx * 0.75;
                        smoothedY += dy * 0.75;
                        smoothedZ += dz * 0.75;
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

            if (context.consumers() != null) {
                long now = System.currentTimeMillis();
                int outlineColor = s.getOutlineArgb(now);
                float a = (float)(outlineColor >>> 24) * 0.003921569f;
                if (a > 0.001f) {
                    float r = (float)((outlineColor >>> 16) & 0xFF) * 0.003921569f;
                    float g = (float)((outlineColor >>> 8) & 0xFF) * 0.003921569f;
                    float b = (float)(outlineColor & 0xFF) * 0.003921569f;
                    VertexConsumer lineConsumer = context.consumers().getBuffer(RenderTypes.lines());
                    OutlineRenderer.renderOutline(matrices, lineConsumer, shape, dx, dy, dz, r, g, b, a, s.outlineWidth);
                }

                if (s.fillEnabled && s.fillOpacity > 0.01f) {
                    int fillColor = s.getFillArgb(now);
                    int fillColor2 = s.getFillArgb2(now);
                    VertexConsumer fillConsumer = context.consumers().getBuffer(RenderTypes.debugQuads());
                    OutlineRenderer.renderFilledBox(matrices, fillConsumer, shape, dx, dy, dz, fillColor, fillColor2);
                }
            }

            return false;
        });

        menuKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.blocky-outline.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKeyBinding != null && menuKeyBinding.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new BlockyOutlineMenuScreen());
                }
            }
        });
    }
}
