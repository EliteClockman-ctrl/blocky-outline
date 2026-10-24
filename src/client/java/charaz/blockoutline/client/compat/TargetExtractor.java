package charaz.blockoutline.client.compat;

import net.minecraft.client.Minecraft;

public interface TargetExtractor {
    TargetInfo extract(Object targetObj, Minecraft mc);
}
