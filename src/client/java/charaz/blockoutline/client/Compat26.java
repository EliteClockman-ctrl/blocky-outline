package charaz.blockoutline.client;

import charaz.blockoutline.BlockyOutlineMod;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.function.Consumer;

/**
 * Cross-version compatibility bridge for Minecraft 26.1.x, 26.2, and 26.3.
 * Handles API differences between minor 26.x releases using safe reflection
 * with zero static references to removed or renamed fields/classes.
 */
public final class Compat26 {
    public static final String GITHUB_ISSUES_URL = "https://github.com/EliteClockman-ctrl/blocky-outline/issues";

    private static Field mcScreenField;
    private static Method mcSetScreenMethod;
    private static Method guiScreenMethod;
    private static Method guiSetScreenMethod;
    private static boolean screenApiResolved = false;

    private static Field cameraStateField;
    private static Field cameraPosField;
    private static Field directCameraPosField;
    private static boolean cameraApiResolved = false;

    private Compat26() {
    }

    public static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, Compat26.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Creates a KeyMapping without any static bytecode reference to InputConstants$Type.KEYSYM,
     * preventing NoSuchFieldError across 26.1, 26.2, and 26.3.
     */
    public static KeyMapping createMenuKeyMapping(String translationKey, int keyCode) {
        KeyMapping.Category category = resolveCategory();

        // 1. Prefer the 3-arg constructor (String, int, KeyMapping$Category) available in 26.x,
        // which internally delegates to Minecraft's own keyboard InputConstants$Type constant.
        try {
            Constructor<KeyMapping> ctor3 = KeyMapping.class.getConstructor(
                    String.class, int.class, KeyMapping.Category.class);
            return ctor3.newInstance(translationKey, keyCode, category);
        } catch (Throwable ignored) {
        }

        // 2. Fallback: dynamically resolve keyboard InputConstants$Type enum constant and invoke 4/5-arg constructor
        try {
            Object keyboardType = resolveKeyboardInputType();
            if (keyboardType != null) {
                for (Constructor<?> ctor : KeyMapping.class.getConstructors()) {
                    Class<?>[] params = ctor.getParameterTypes();
                    if (params.length == 4
                            && params[0] == String.class
                            && params[1].isInstance(keyboardType)
                            && params[2] == int.class
                            && params[3] == KeyMapping.Category.class) {
                        return (KeyMapping) ctor.newInstance(translationKey, keyboardType, keyCode, category);
                    }
                    if (params.length == 5
                            && params[0] == String.class
                            && params[1].isInstance(keyboardType)
                            && params[2] == int.class
                            && params[3] == KeyMapping.Category.class
                            && params[4] == int.class) {
                        return (KeyMapping) ctor.newInstance(translationKey, keyboardType, keyCode, category, 0);
                    }
                }
            }
        } catch (Throwable t) {
            BlockyOutlineMod.LOGGER.error("Failed to create KeyMapping for {}", translationKey, t);
        }
        return null;
    }

    private static KeyMapping.Category resolveCategory() {
        try {
            return KeyMapping.Category.register(Identifier.fromNamespaceAndPath("blocky-outline", "general"));
        } catch (Throwable ignored) {
            try {
                Field miscField = KeyMapping.Category.class.getField("MISC");
                return (KeyMapping.Category) miscField.get(null);
            } catch (Throwable t) {
                for (Field f : KeyMapping.Category.class.getFields()) {
                    if (f.getType() == KeyMapping.Category.class) {
                        try {
                            return (KeyMapping.Category) f.get(null);
                        } catch (Throwable ignored2) {
                        }
                    }
                }
            }
        }
        return null;
    }

    private static Object resolveKeyboardInputType() {
        try {
            Object[] constants = InputConstants.Type.class.getEnumConstants();
            if (constants != null && constants.length > 0) {
                for (Object c : constants) {
                    String name = c.toString().toUpperCase();
                    if (name.contains("KEY")) {
                        return c;
                    }
                }
                return constants[0];
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static synchronized void resolveScreenApi(Minecraft mc) {
        if (screenApiResolved) return;
        screenApiResolved = true;
        try {
            // 26.2 / 26.3: mc.gui.screen() and mc.gui.setScreen(Screen)
            if (mc.gui != null) {
                Class<?> guiClass = mc.gui.getClass();
                try {
                    guiScreenMethod = guiClass.getMethod("screen");
                    guiSetScreenMethod = guiClass.getMethod("setScreen", Screen.class);
                    return;
                } catch (NoSuchMethodException ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            // 26.1.x: mc.screen and mc.setScreen(Screen)
            mcScreenField = Minecraft.class.getField("screen");
            mcSetScreenMethod = Minecraft.class.getMethod("setScreen", Screen.class);
        } catch (Throwable ignored) {
        }
    }

    public static boolean isNoScreenOpen(Minecraft mc) {
        if (mc == null) return false;
        resolveScreenApi(mc);
        try {
            if (guiScreenMethod != null && mc.gui != null) {
                return guiScreenMethod.invoke(mc.gui) == null;
            }
            if (mcScreenField != null) {
                return mcScreenField.get(mc) == null;
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    public static void openScreen(Minecraft mc, Screen screen) {
        if (mc == null) return;
        resolveScreenApi(mc);
        try {
            if (guiSetScreenMethod != null && mc.gui != null) {
                guiSetScreenMethod.invoke(mc.gui, screen);
                return;
            }
            if (mcSetScreenMethod != null) {
                mcSetScreenMethod.invoke(mc, screen);
            }
        } catch (Throwable t) {
            BlockyOutlineMod.LOGGER.error("Failed to open screen", t);
        }
    }

    private static synchronized void resolveCameraApi(LevelRenderState state) {
        if (cameraApiResolved || state == null) return;
        cameraApiResolved = true;
        try {
            cameraStateField = state.getClass().getField("cameraRenderState");
            Object camState = cameraStateField.get(state);
            if (camState != null) {
                cameraPosField = camState.getClass().getField("pos");
                return;
            }
        } catch (Throwable ignored) {
        }
        try {
            directCameraPosField = state.getClass().getField("cameraPos");
        } catch (Throwable ignored) {
        }
    }

    public static Vec3 getCameraPos(LevelRenderState levelState) {
        if (levelState != null) {
            try {
                if (levelState.cameraRenderState != null && levelState.cameraRenderState.pos != null) {
                    return levelState.cameraRenderState.pos;
                }
            } catch (Throwable ignored) {
            }
            resolveCameraApi(levelState);
            try {
                if (cameraStateField != null && cameraPosField != null) {
                    Object camState = cameraStateField.get(levelState);
                    if (camState != null) {
                        return (Vec3) cameraPosField.get(camState);
                    }
                }
                if (directCameraPosField != null) {
                    return (Vec3) directCameraPosField.get(levelState);
                }
            } catch (Throwable ignored) {
            }
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameRenderer != null) {
                Method camMethod = null;
                try {
                    camMethod = mc.gameRenderer.getClass().getMethod("mainCamera");
                } catch (NoSuchMethodException e) {
                    try {
                        camMethod = mc.gameRenderer.getClass().getMethod("getMainCamera");
                    } catch (NoSuchMethodException ignored) {
                    }
                }
                if (camMethod != null) {
                    Object cam = camMethod.invoke(mc.gameRenderer);
                    if (cam != null) {
                        Method posMethod = cam.getClass().getMethod("position");
                        return (Vec3) posMethod.invoke(cam);
                    }
                }
            }
            if (mc != null && mc.player != null) {
                return mc.player.getEyePosition();
            }
        } catch (Throwable ignored) {
        }
        return Vec3.ZERO;
    }

    /**
     * Reflectively retrieves bufferSource() from LevelRenderContext on 26.1.x,
     * returning null on 26.2+ where MultiBufferSource was removed.
     */
    public static Object getContextBufferSource(Object context) {
        if (context == null) return null;
        try {
            Method m = context.getClass().getMethod("bufferSource");
            return m.invoke(context);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Draws using MultiBufferSource on 26.1.x via reflection so that no bytecode reference
     * to net.minecraft.client.renderer.MultiBufferSource exists in any class loaded on 26.2/26.3.
     */
    public static boolean drawWithBufferSource(Object bufferSource, RenderType renderType, Consumer<VertexConsumer> drawer) {
        if (bufferSource == null || renderType == null || drawer == null) return false;
        try {
            Method getBuffer = null;
            for (Method m : bufferSource.getClass().getMethods()) {
                if (m.getName().equals("getBuffer") && m.getParameterCount() == 1) {
                    getBuffer = m;
                    break;
                }
            }
            if (getBuffer == null) return false;
            VertexConsumer consumer = (VertexConsumer) getBuffer.invoke(bufferSource, renderType);
            if (consumer == null) return false;
            drawer.accept(consumer);

            // Flush custom X-Ray layer immediately if endBatch(RenderType) exists
            for (Method m : bufferSource.getClass().getMethods()) {
                if (m.getName().equals("endBatch") && m.getParameterCount() == 1) {
                    m.invoke(bufferSource, renderType);
                    break;
                }
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void openUrl(String url) {
        try {
            Util.getPlatform().openUri(new URI(url));
        } catch (Throwable t) {
            BlockyOutlineMod.LOGGER.error("Failed to open URL: {}", url, t);
        }
    }

    public static void sendReportChatMessage(Minecraft client) {
        if (client == null) return;
        try {
            MutableComponent message = Component.literal("§b[Blocky Outline] §fFound a bug? ")
                    .append(Component.literal("§e§n[Click here to report it on GitHub!]")
                            .withStyle((Style style) -> style
                                    .withClickEvent(new ClickEvent.OpenUrl(URI.create(GITHUB_ISSUES_URL)))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Open GitHub Issues in your browser")))));

            if (client.gui != null) {
                Object chatObj = null;
                for (String mName : new String[]{"chat", "getChat"}) {
                    try {
                        chatObj = client.gui.getClass().getMethod(mName).invoke(client.gui);
                        if (chatObj != null) break;
                    } catch (Throwable ignored) {
                    }
                }
                if (chatObj != null) {
                    for (String addName : new String[]{"addClientSystemMessage", "addMessage"}) {
                        try {
                            Method m = chatObj.getClass().getMethod(addName, Component.class);
                            m.invoke(chatObj, message);
                            return;
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                }
            }
            if (client.player != null) {
                for (Method m : client.player.getClass().getMethods()) {
                    if (m.getName().equals("displayClientMessage") && m.getParameterCount() == 2) {
                        m.invoke(client.player, message, false);
                        return;
                    }
                    if (m.getName().equals("sendSystemMessage") && m.getParameterCount() == 1) {
                        m.invoke(client.player, message);
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            BlockyOutlineMod.LOGGER.error("Failed to send report chat message", t);
        }
    }
}
