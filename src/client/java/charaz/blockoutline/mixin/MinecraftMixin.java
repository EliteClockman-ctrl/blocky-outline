package charaz.blockoutline.mixin;

import charaz.blockoutline.client.BlockyOutlineClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Standalone fallback for client tick handling when Fabric API (fabric-lifecycle-events-v1) is not installed.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "tick()V", at = @At("TAIL"), require = 0)
    private void blockyOutline$onEndClientTick(CallbackInfo ci) {
        if (!BlockyOutlineClient.isTickRegisteredViaFabricApi()) {
            BlockyOutlineClient.onClientTick((Minecraft) (Object) this);
        }
    }
}
