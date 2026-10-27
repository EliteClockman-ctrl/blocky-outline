package charaz.blockoutline.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

public final class CompatBridge {
    public static final boolean IS_MODERN;

    public static final LineVertexDrawer LINE_DRAWER;
    public static final RenderBufferProvider BUFFER_PROVIDER;
    public static final TargetExtractor TARGET_EXTRACTOR;

    private static java.lang.invoke.MethodHandle matricesHandle;
    private static java.lang.invoke.MethodHandle consumersHandle;
    private static java.lang.invoke.MethodHandle cameraPosHandle;
    private static java.lang.invoke.MethodHandle blockOffsetHandle;
    private static int blockOffsetParamCount = 0;

    static {
        boolean modern = false;
        try {
            Class.forName("net.minecraft.client.renderer.state.BlockOutlineRenderState");
            modern = true;
        } catch (Throwable t1) {
            try {
                Class.forName("net.minecraft.class_12074");
                modern = true;
            } catch (Throwable t2) {
                modern = false;
            }
        }
        IS_MODERN = modern;

        LineVertexDrawer lineDrawer = null;
        RenderBufferProvider bufferProvider = null;
        TargetExtractor targetExtractor = null;

        try {
            if (IS_MODERN) {
                lineDrawer = (LineVertexDrawer) Class.forName("charaz.blockoutline.client.compat.modern.ModernLineDrawer")
                        .getDeclaredConstructor().newInstance();
                bufferProvider = (RenderBufferProvider) Class.forName("charaz.blockoutline.client.compat.modern.ModernRenderBufferProvider")
                        .getDeclaredConstructor().newInstance();
                targetExtractor = (TargetExtractor) Class.forName("charaz.blockoutline.client.compat.modern.ModernTargetExtractor")
                        .getDeclaredConstructor().newInstance();
            } else {
                lineDrawer = (LineVertexDrawer) Class.forName("charaz.blockoutline.client.compat.legacy.LegacyLineDrawer")
                        .getDeclaredConstructor().newInstance();
                bufferProvider = (RenderBufferProvider) Class.forName("charaz.blockoutline.client.compat.legacy.LegacyRenderBufferProvider")
                        .getDeclaredConstructor().newInstance();
                targetExtractor = (TargetExtractor) Class.forName("charaz.blockoutline.client.compat.legacy.LegacyTargetExtractor")
                        .getDeclaredConstructor().newInstance();
            }
        } catch (Throwable t) {
            System.err.println("[BlockyOutline] Failed to initialize compat bridge adapters: " + t.getMessage());
            t.printStackTrace();
        }

        LINE_DRAWER = lineDrawer;
        BUFFER_PROVIDER = bufferProvider;
        TARGET_EXTRACTOR = targetExtractor;
    }

    private CompatBridge() {
    }

    public static PoseStack getPoseStack(Object context) {
        if (context == null) return null;
        try {
            if (matricesHandle != null) {
                return (PoseStack) matricesHandle.invoke(context);
            }
            for (Method m : context.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && PoseStack.class.isAssignableFrom(m.getReturnType())) {
                    m.setAccessible(true);
                    matricesHandle = java.lang.invoke.MethodHandles.lookup().unreflect(m);
                    return (PoseStack) matricesHandle.invoke(context);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static MultiBufferSource getBufferSource(Object context) {
        if (context == null) return null;
        try {
            if (consumersHandle != null) {
                return (MultiBufferSource) consumersHandle.invoke(context);
            }
            for (Method m : context.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && MultiBufferSource.class.isAssignableFrom(m.getReturnType())) {
                    m.setAccessible(true);
                    consumersHandle = java.lang.invoke.MethodHandles.lookup().unreflect(m);
                    return (MultiBufferSource) consumersHandle.invoke(context);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static Vec3 getCameraPos(Camera camera) {
        if (camera == null) return Vec3.ZERO;
        try {
            if (cameraPosHandle != null) {
                return (Vec3) cameraPosHandle.invoke(camera);
            }
            for (Method m : camera.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && Vec3.class.isAssignableFrom(m.getReturnType())) {
                    String name = m.getName();
                    if ("position".equals(name) || "getPos".equals(name) || "getPosition".equals(name)
                            || "method_71156".equals(name) || "method_19326".equals(name)) {
                        m.setAccessible(true);
                        cameraPosHandle = java.lang.invoke.MethodHandles.lookup().unreflect(m);
                        return (Vec3) cameraPosHandle.invoke(camera);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return Vec3.ZERO;
    }

    public static Vec3 getBlockOffset(BlockState state, Object level, BlockPos pos) {
        if (state == null || pos == null) return Vec3.ZERO;
        try {
            if (blockOffsetHandle != null) {
                if (blockOffsetParamCount == 1) {
                    return (Vec3) blockOffsetHandle.invoke(state, pos);
                } else if (blockOffsetParamCount == 2) {
                    return (Vec3) blockOffsetHandle.invoke(state, level, pos);
                }
            }
            for (Method m : state.getClass().getMethods()) {
                if (Vec3.class.isAssignableFrom(m.getReturnType())) {
                    if (m.getParameterCount() == 1 || m.getParameterCount() == 2) {
                        m.setAccessible(true);
                        blockOffsetHandle = java.lang.invoke.MethodHandles.lookup().unreflect(m);
                        blockOffsetParamCount = m.getParameterCount();
                        if (blockOffsetParamCount == 1) {
                            return (Vec3) blockOffsetHandle.invoke(state, pos);
                        } else {
                            return (Vec3) blockOffsetHandle.invoke(state, level, pos);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return Vec3.ZERO;
    }

    public static void registerWorldRenderEvent(RenderOutlineCallback callback) {
        try {
            Class<?> eventsClass = null;
            for (String className : new String[]{
                    "net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents",
                    "net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents"
            }) {
                try {
                    eventsClass = Class.forName(className);
                    break;
                } catch (ClassNotFoundException ignored) {
                }
            }

            if (eventsClass == null) {
                System.err.println("[BlockyOutline] Could not find WorldRenderEvents class in Fabric API!");
                return;
            }

            Field eventField = eventsClass.getField("BEFORE_BLOCK_OUTLINE");
            Object eventObj = eventField.get(null);

            Class<?> listenerInterface = null;
            for (Class<?> inner : eventsClass.getDeclaredClasses()) {
                if ("BeforeBlockOutline".equals(inner.getSimpleName())) {
                    listenerInterface = inner;
                    break;
                }
            }

            if (listenerInterface == null) {
                System.err.println("[BlockyOutline] Could not find BeforeBlockOutline interface!");
                return;
            }

            Object proxyInstance = Proxy.newProxyInstance(
                    listenerInterface.getClassLoader(),
                    new Class<?>[]{listenerInterface},
                    (proxy, method, args) -> {
                        if ("beforeBlockOutline".equals(method.getName()) && args != null && args.length >= 2) {
                            return callback.onBeforeBlockOutline(args[0], args[1]);
                        }
                        if ("equals".equals(method.getName())) return proxy == args[0];
                        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                        if ("toString".equals(method.getName())) return "BlockyOutlineProxy";
                        return null;
                    }
            );

            Method registerMethod = null;
            try {
                Class<?> eventInterface = Class.forName("net.fabricmc.fabric.api.event.Event");
                for (Method m : eventInterface.getMethods()) {
                    if ("register".equals(m.getName()) && m.getParameterCount() == 1) {
                        registerMethod = m;
                        break;
                    }
                }
            } catch (Throwable ignored) {
            }

            if (registerMethod == null) {
                for (Method m : eventObj.getClass().getMethods()) {
                    if ("register".equals(m.getName()) && m.getParameterCount() == 1) {
                        registerMethod = m;
                        break;
                    }
                }
            }

            if (registerMethod != null) {
                registerMethod.setAccessible(true);
                registerMethod.invoke(eventObj, proxyInstance);
                System.out.println("[BlockyOutline] Successfully registered BEFORE_BLOCK_OUTLINE via " + eventsClass.getName());
            } else {
                System.err.println("[BlockyOutline] Could not find register method on BEFORE_BLOCK_OUTLINE event!");
            }
        } catch (Throwable t) {
            System.err.println("[BlockyOutline] Failed to register WorldRenderEvents via proxy: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public static void openMenuScreen(Minecraft mc) {
        try {
            Class<?> clazz;
            if (IS_MODERN) {
                clazz = Class.forName("charaz.blockoutline.client.compat.modern.ModernMenuScreen");
            } else {
                clazz = Class.forName("charaz.blockoutline.client.compat.legacy.LegacyMenuScreen");
            }
            mc.setScreen((Screen) clazz.getDeclaredConstructor().newInstance());
        } catch (Throwable t) {
            System.err.println("[BlockyOutline] Failed to open menu screen: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public static KeyMapping createMenuKeyMapping(String name, int keyCode, String legacyCategory) {
        Object keyType = null;
        try {
            Class<?> typeClass = null;
            for (String cn : new String[]{
                    "com.mojang.blaze3d.platform.InputConstants$Type",
                    "net.minecraft.client.util.InputUtil$Type",
                    "net.minecraft.class_3675$class_307"
            }) {
                try {
                    typeClass = Class.forName(cn);
                    break;
                } catch (Throwable ignored) {
                }
            }
            if (typeClass != null && typeClass.isEnum()) {
                for (Object c : typeClass.getEnumConstants()) {
                    String s = c.toString();
                    if ("KEYSYM".equalsIgnoreCase(s) || s.contains("KEY")) {
                        keyType = c;
                        break;
                    }
                }
                if (keyType == null && typeClass.getEnumConstants().length > 0) {
                    keyType = typeClass.getEnumConstants()[0];
                }
            }
        } catch (Throwable ignored) {
        }

        // 1. Try 3-parameter constructor (String name, int keyCode, Category/String category)
        for (Constructor<?> ctor : KeyMapping.class.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == 3 && params[0] == String.class && (params[1] == int.class || params[1] == Integer.TYPE)) {
                try {
                    ctor.setAccessible(true);
                    if (params[2] == String.class) {
                        return (KeyMapping) ctor.newInstance(name, keyCode, legacyCategory);
                    } else {
                        Object miscCategory = getCategoryObject(params[2]);
                        if (miscCategory != null) {
                            return (KeyMapping) ctor.newInstance(name, keyCode, miscCategory);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        // 2. Try 4 or 5-parameter constructor with Type parameter
        for (Constructor<?> ctor : KeyMapping.class.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length >= 4 && params[0] == String.class && (params[2] == int.class || params[2] == Integer.TYPE)) {
                try {
                    ctor.setAccessible(true);
                    Object typeArg = (keyType != null && params[1].isAssignableFrom(keyType.getClass())) ? keyType : null;
                    if (typeArg == null && params[1].isEnum() && params[1].getEnumConstants().length > 0) {
                        typeArg = params[1].getEnumConstants()[0];
                    }

                    if (typeArg != null) {
                        if (params[3] == String.class) {
                            return (KeyMapping) ctor.newInstance(name, typeArg, keyCode, legacyCategory);
                        } else {
                            Object miscCategory = getCategoryObject(params[3]);
                            if (miscCategory != null) {
                                if (params.length == 4) {
                                    return (KeyMapping) ctor.newInstance(name, typeArg, keyCode, miscCategory);
                                } else if (params.length == 5 && (params[4] == int.class || params[4] == Integer.TYPE)) {
                                    return (KeyMapping) ctor.newInstance(name, typeArg, keyCode, miscCategory, 0);
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    System.err.println("[BlockyOutline] Failed to invoke KeyMapping constructor: " + t.getMessage());
                }
            }
        }
        throw new IllegalStateException("[BlockyOutline] Could not find compatible KeyMapping constructor!");
    }

    private static Object getCategoryObject(Class<?> categoryClass) {
        if (categoryClass == null) return null;
        for (Field f : categoryClass.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && categoryClass.isAssignableFrom(f.getType())) {
                if ("MISC".equalsIgnoreCase(f.getName()) || f.getName().contains("MISC") || f.getName().startsWith("field_")) {
                    try {
                        return f.get(null);
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
        return null;
    }

    public static void openUrl(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(url));
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Throwable ignored) {
        }
    }

    public static Style createOpenUrlStyle(String url) {
        Style style = Style.EMPTY.withUnderlined(true).withColor(0x38BDF8);
        try {
            if (IS_MODERN) {
                Class<?> openUrlClass = Class.forName("net.minecraft.network.chat.ClickEvent$OpenUrl");
                Constructor<?> ctor = openUrlClass.getConstructor(java.net.URI.class);
                Object event = ctor.newInstance(new java.net.URI(url));
                Method m = Style.class.getMethod("withClickEvent", Class.forName("net.minecraft.network.chat.ClickEvent"));
                return (Style) m.invoke(style, event);
            } else {
                Class<?> actionClass = Class.forName("net.minecraft.network.chat.ClickEvent$Action");
                Field openUrlField = actionClass.getField("OPEN_URL");
                Object openUrlAction = openUrlField.get(null);
                Class<?> clickEventClass = Class.forName("net.minecraft.network.chat.ClickEvent");
                Constructor<?> ctor = clickEventClass.getConstructor(actionClass, String.class);
                Object event = ctor.newInstance(openUrlAction, url);
                return style.withClickEvent((net.minecraft.network.chat.ClickEvent) event);
            }
        } catch (Throwable ignored) {
        }
        return style;
    }

    public static Style withHoverTooltip(Style style, Component text) {
        try {
            if (IS_MODERN) {
                Class<?> showTextClass = Class.forName("net.minecraft.network.chat.HoverEvent$ShowText");
                Constructor<?> ctor = showTextClass.getConstructor(Component.class);
                Object event = ctor.newInstance(text);
                Method m = Style.class.getMethod("withHoverEvent", Class.forName("net.minecraft.network.chat.HoverEvent"));
                return (Style) m.invoke(style, event);
            } else {
                Class<?> actionClass = Class.forName("net.minecraft.network.chat.HoverEvent$Action");
                Field showTextField = actionClass.getField("SHOW_TEXT");
                Object showTextAction = showTextField.get(null);
                Class<?> hoverEventClass = Class.forName("net.minecraft.network.chat.HoverEvent");
                Constructor<?> ctor = hoverEventClass.getConstructor(actionClass, Object.class);
                Object event = ctor.newInstance(showTextAction, text);
                return style.withHoverEvent((net.minecraft.network.chat.HoverEvent) event);
            }
        } catch (Throwable ignored) {
        }
        return style;
    }

    public static Component createReportMessage() {
        Component prefix = Component.literal("[Blocky Outline] ").withStyle(Style.EMPTY.withColor(0x38BDF8).withBold(true));
        Component body = Component.literal("Báo cáo lỗi / Góp ý: ").withStyle(Style.EMPTY.withColor(0xE2E8F0));
        Style linkStyle = withHoverTooltip(createOpenUrlStyle("https://github.com/EliteClockman-ctrl/blocky-outline/issues"),
                Component.literal("Nhấp để mở trang báo cáo lỗi GitHub"));
        Component link = Component.literal("https://github.com/EliteClockman-ctrl/blocky-outline/issues").withStyle(linkStyle);
        return Component.empty().append(prefix).append(body).append(link);
    }
}
