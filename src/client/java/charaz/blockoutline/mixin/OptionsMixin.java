package charaz.blockoutline.mixin;

import charaz.blockoutline.client.BlockyOutlineClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Standalone fallback for keybind registration when Fabric API (fabric-key-mapping-api-v1) is not installed.
 */
@Mixin(Options.class)
public class OptionsMixin {
    @Shadow
    @Final
    @Mutable
    public KeyMapping[] keyMappings;

    @Inject(method = "load()V", at = @At("HEAD"), require = 0)
    private void blockyOutline$onLoadOptions(CallbackInfo ci) {
        if (BlockyOutlineClient.isKeyRegisteredViaFabricApi()) {
            return;
        }
        KeyMapping menuKey = BlockyOutlineClient.getOpenMenuKey();
        if (menuKey == null || this.keyMappings == null) {
            return;
        }
        for (KeyMapping existing : this.keyMappings) {
            if (existing == menuKey) {
                return;
            }
        }
        KeyMapping[] expanded = Arrays.copyOf(this.keyMappings, this.keyMappings.length + 1);
        expanded[this.keyMappings.length] = menuKey;
        this.keyMappings = expanded;
    }
}
