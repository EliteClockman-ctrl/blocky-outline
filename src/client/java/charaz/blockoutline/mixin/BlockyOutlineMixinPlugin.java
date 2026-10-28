package charaz.blockoutline.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Dynamically selects the appropriate LevelRenderer mixin depending on whether the runtime is
 * Minecraft 26.1.x (has MultiBufferSource$BufferSource) or 26.2 / 26.3 (uses SubmitNodeCollector).
 */
public class BlockyOutlineMixinPlugin implements IMixinConfigPlugin {
    private boolean hasMultiBufferSource;

    @Override
    public void onLoad(String mixinPackage) {
        ClassLoader cl = BlockyOutlineMixinPlugin.class.getClassLoader();
        this.hasMultiBufferSource = cl.getResource(
                "net/minecraft/client/renderer/MultiBufferSource$BufferSource.class") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("LevelRendererMixin26_1")) {
            return this.hasMultiBufferSource;
        }
        if (mixinClassName.endsWith("LevelRendererMixin26_2")) {
            return !this.hasMultiBufferSource;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
