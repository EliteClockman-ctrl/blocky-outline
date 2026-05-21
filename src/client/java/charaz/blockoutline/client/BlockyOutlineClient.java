package charaz.blockoutline.client;

import charaz.blockoutline.config.BlockyOutlineSettings;
import charaz.blockoutline.client.renderer.OutlineRenderer;
import charaz.blockoutline.client.ui.BlockyOutlineMenuScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.lwjgl.glfw.GLFW;

public class BlockyOutlineClient implements ClientModInitializer {

    private static BlockPos targetPos = null;
    private static VoxelShape targetShape = null;
    private static boolean hasTargetThisFrame = false;
    private static KeyMapping menuKeyBinding = null;

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();
        registerMenuHotkey();

        // BEFORE_ENTITIES: Reset the block cache at the start of each frame render
        WorldRenderEvents.BEFORE_ENTITIES.register(context -> {
            targetPos = null;
            targetShape = null;
            hasTargetThisFrame = false;
        });

        // BEFORE_BLOCK_OUTLINE: draw outline and store block target in cache
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineContext) -> {
            BlockPos pos     = outlineContext.pos();
            VoxelShape shape = outlineContext.shape();
            if (pos == null || shape == null || shape.isEmpty()) return false;

            // Cache the targeted block information for the translucent fill pass
            targetPos = pos;
            targetShape = shape;
            hasTargetThisFrame = true;

            PoseStack matrices       = context.matrices();
            MultiBufferSource consumers = context.consumers();
            if (matrices == null || consumers == null) return false;

            if (context.worldState() == null || context.worldState().cameraRenderState == null) return false;
            Vec3 cam = context.worldState().cameraRenderState.pos;
            if (cam == null) return false;

            double dx = pos.getX() - cam.x;
            double dy = pos.getY() - cam.y;
            double dz = pos.getZ() - cam.z;

            long now = System.currentTimeMillis();

            // ── Draw Outline ─────────────────────────────────────────────────────
            BlockyOutlineSettings s = BlockyOutlineSettings.get();
            float[] oc = s.getOutlineRgb(now);
            int outlineColor = toARGB(oc[0], oc[1], oc[2], s.outlineOpacity);
            VertexConsumer lineVC = consumers.getBuffer(RenderTypes.lines());
            OutlineRenderer.renderOutline(matrices, lineVC, shape, dx, dy, dz, outlineColor, s.outlineWidth);

            return false;
        });

        // BEFORE_TRANSLUCENT: draw fill using the frame-synchronized cached block
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
            BlockyOutlineSettings s = BlockyOutlineSettings.get();
            if (!s.fillEnabled) return;

            // Only render fill if we actually targeted a block in the outline pass of this frame
            if (!hasTargetThisFrame || targetPos == null || targetShape == null) return;

            PoseStack matrices       = context.matrices();
            MultiBufferSource consumers = context.consumers();
            if (matrices == null || consumers == null) return;

            if (context.worldState() == null || context.worldState().cameraRenderState == null) return;
            Vec3 cam = context.worldState().cameraRenderState.pos;
            if (cam == null) return;

            double dx = targetPos.getX() - cam.x;
            double dy = targetPos.getY() - cam.y;
            double dz = targetPos.getZ() - cam.z;

            long now = System.currentTimeMillis();
            float[] fc = s.getFillRgb(now);
            int fillColor = toARGB(fc[0], fc[1], fc[2], s.fillOpacity);

            // At BEFORE_TRANSLUCENT the consumer is the real MultiBufferSource,
            // not the outline-wrapped one. debugFilledBox has NO_CULL + TRANSLUCENT
            // blending so all 6 faces render correctly.
            VertexConsumer fillVC = consumers.getBuffer(RenderTypes.debugFilledBox());
            OutlineRenderer.renderFilledBox(matrices, fillVC, targetShape, dx, dy, dz, fillColor);

            // Flush the buffer immediately so it draws with the active 3D matrices/shaders
            if (consumers instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch(RenderTypes.debugFilledBox());
            }
        });
    }

    private void registerMenuHotkey() {
        // Register standard keybinding with Minecraft KeyMapping system
        menuKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.blocky-outline.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("blocky-outline", "general"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (menuKeyBinding.consumeClick()) {
                if (!(client.screen instanceof BlockyOutlineMenuScreen)) {
                    client.setScreen(new BlockyOutlineMenuScreen());
                } else {
                    client.setScreen(null);
                }
            }
        });
    }

    private static int toARGB(float r, float g, float b, float a) {
        return ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
}
