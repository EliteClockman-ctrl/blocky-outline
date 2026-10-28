package charaz.blockoutline.client;

import charaz.blockoutline.BlockyOutlineMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Isolated Fabric API hook class.
 * Only loaded when the corresponding Fabric API modules are present at runtime,
 * allowing Blocky Outline to run 100% standalone when Fabric API is not installed.
 */
public final class FabricApiHook {
    private FabricApiHook() {
    }

    public static boolean tryRegisterKeyMapping(KeyMapping keyMapping) {
        if (keyMapping == null) return false;
        try {
            KeyMappingHelper.registerKeyMapping(keyMapping);
            return true;
        } catch (Throwable t) {
            BlockyOutlineMod.LOGGER.warn("Fabric KeyMappingHelper unavailable, falling back to OptionsMixin", t);
            return false;
        }
    }

    public static boolean tryRegisterTickEvent() {
        try {
            ClientTickEvents.END_CLIENT_TICK.register(BlockyOutlineClient::onClientTick);
            return true;
        } catch (Throwable t) {
            BlockyOutlineMod.LOGGER.warn("Fabric ClientTickEvents unavailable, falling back to MinecraftMixin", t);
            return false;
        }
    }

    public static boolean tryRegisterRenderEvent() {
        try {
            LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) -> {
                LevelRenderState levelState = context.levelState();
                Vec3 camPos = Compat26.getCameraPos(levelState);
                Object bufferSource = Compat26.getContextBufferSource(context);
                return BlockyOutlineClient.renderOutline(
                        context.poseStack(),
                        context.submitNodeCollector(),
                        bufferSource,
                        camPos,
                        outlineRenderState
                );
            });
            return true;
        } catch (Throwable t) {
            BlockyOutlineMod.LOGGER.warn("Fabric LevelRenderEvents unavailable, falling back to LevelRendererMixin", t);
            return false;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean tryRegisterReportCommand() {
        try {
            Class<?> callbackClass = Class.forName("net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback");
            Object eventObj = callbackClass.getField("EVENT").get(null);
            Method registerMethod = eventObj.getClass().getMethod("register", Object.class);

            Object proxy = Proxy.newProxyInstance(
                    FabricApiHook.class.getClassLoader(),
                    new Class<?>[]{callbackClass},
                    (p, method, args) -> {
                        if (args != null && args.length >= 1 && args[0] instanceof CommandDispatcher dispatcher) {
                            LiteralArgumentBuilder reportCmd = LiteralArgumentBuilder.literal("report")
                                    .executes(ctx -> {
                                        Compat26.openUrl(Compat26.GITHUB_ISSUES_URL);
                                        Compat26.sendReportChatMessage(Minecraft.getInstance());
                                        return 1;
                                    });
                            dispatcher.register(reportCmd);

                            LiteralArgumentBuilder rootCmd = LiteralArgumentBuilder.literal("blockyoutline")
                                    .then(LiteralArgumentBuilder.literal("report")
                                            .executes(ctx -> {
                                                Compat26.openUrl(Compat26.GITHUB_ISSUES_URL);
                                                Compat26.sendReportChatMessage(Minecraft.getInstance());
                                                return 1;
                                            }));
                            dispatcher.register(rootCmd);
                        }
                        return null;
                    }
            );
            registerMethod.invoke(eventObj, proxy);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
