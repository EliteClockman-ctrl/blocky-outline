package charaz.blockoutline.client.compat;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
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

    public static Vec3 getBlockOffset(BlockState state, BlockGetter level, BlockPos pos) {
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

    public static KeyMapping createMenuKeyMapping(String name, InputConstants.Type type, int keyCode, String legacyCategory) {
        for (Constructor<?> ctor : KeyMapping.class.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length >= 4 && params[0] == String.class && params[1] == InputConstants.Type.class && (params[2] == int.class || params[2] == Integer.TYPE)) {
                try {
                    ctor.setAccessible(true);
                    if (params[3] == String.class) {
                        return (KeyMapping) ctor.newInstance(name, type, keyCode, legacyCategory);
                    } else {
                        Class<?> categoryClass = params[3];
                        Object miscCategory = null;
                        for (Field f : categoryClass.getFields()) {
                            if (Modifier.isStatic(f.getModifiers()) && categoryClass.isAssignableFrom(f.getType())) {
                                if ("MISC".equalsIgnoreCase(f.getName()) || f.getName().contains("MISC") || f.getName().startsWith("field_")) {
                                    miscCategory = f.get(null);
                                    break;
                                }
                            }
                        }
                        if (miscCategory != null) {
                            if (params.length == 4) {
                                return (KeyMapping) ctor.newInstance(name, type, keyCode, miscCategory);
                            } else if (params.length == 5 && (params[4] == int.class || params[4] == Integer.TYPE)) {
                                return (KeyMapping) ctor.newInstance(name, type, keyCode, miscCategory, 0);
                            }
                        }
                    }
                } catch (Throwable t) {
                    System.err.println("[BlockyOutline] Failed to invoke KeyMapping constructor: " + t.getMessage());
                    t.printStackTrace();
                }
            }
        }
        throw new IllegalStateException("[BlockyOutline] Could not find compatible KeyMapping constructor!");
    }
}
