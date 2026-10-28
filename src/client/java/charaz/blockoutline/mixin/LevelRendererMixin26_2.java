package charaz.blockoutline.mixin;

import charaz.blockoutline.client.BlockyOutlineClient;
import charaz.blockoutline.client.Compat26;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Standalone fallback for block outline rendering on Minecraft 26.2 and 26.3 when Fabric API is not installed.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin26_2 {
    @Inject(
            method = "submitBlockOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void blockyOutline$onSubmitBlockOutline(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            LevelRenderState levelRenderState,
            CallbackInfo ci) {
        if (BlockyOutlineClient.isRenderRegisteredViaFabricApi()) {
            return;
        }
        if (levelRenderState == null || levelRenderState.blockOutlineRenderState == null) {
            return;
        }
        BlockOutlineRenderState outlineState = levelRenderState.blockOutlineRenderState;
        Vec3 camPos = Compat26.getCameraPos(levelRenderState);
        boolean allowVanilla = BlockyOutlineClient.renderOutline(
                poseStack,
                submitNodeCollector,
                null,
                camPos,
                outlineState
        );
        if (!allowVanilla) {
            ci.cancel();
        }
    }
}
