package charaz.blockoutline.mixin;

import charaz.blockoutline.client.BlockyOutlineClient;
import charaz.blockoutline.client.Compat26;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Standalone fallback for block outline rendering on Minecraft 26.1.x when Fabric API is not installed.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin26_1 {
    @Inject(
            method = "renderBlockOutline(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;ZLnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void blockyOutline$onRenderBlockOutline(
            MultiBufferSource.BufferSource bufferSource,
            PoseStack poseStack,
            boolean renderTranslucent,
            LevelRenderState levelRenderState,
            CallbackInfo ci) {
        if (BlockyOutlineClient.isRenderRegisteredViaFabricApi()) {
            return;
        }
        if (levelRenderState == null || levelRenderState.blockOutlineRenderState == null) {
            return;
        }
        BlockOutlineRenderState outlineState = levelRenderState.blockOutlineRenderState;
        if (outlineState.isTranslucent() != renderTranslucent) {
            return;
        }
        Vec3 camPos = Compat26.getCameraPos(levelRenderState);
        boolean allowVanilla = BlockyOutlineClient.renderOutline(
                poseStack,
                null,
                bufferSource,
                camPos,
                outlineState
        );
        if (!allowVanilla) {
            ci.cancel();
        }
    }
}
