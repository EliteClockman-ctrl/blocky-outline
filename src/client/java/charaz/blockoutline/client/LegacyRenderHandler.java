package charaz.blockoutline.client;

import charaz.blockoutline.config.BlockyOutlineSettings;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class LegacyRenderHandler {
    private LegacyRenderHandler() {
    }

    public static void register() {
        try {
            Class<?> eventsClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents");
            Object beforeOutlineEvent = eventsClass.getField("BEFORE_BLOCK_OUTLINE").get(null);
            Class<?> listenerClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents$BeforeBlockOutline");

            Object listenerProxy = Proxy.newProxyInstance(
                    LegacyRenderHandler.class.getClassLoader(),
                    new Class<?>[]{listenerClass},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (args != null && args.length >= 2) {
                                return handle121Render(args[0], args[1]);
                            }
                            return Boolean.TRUE;
                        }
                    }
            );

            for (Method m : beforeOutlineEvent.getClass().getMethods()) {
                if (m.getName().equals("register")) {
                    m.invoke(beforeOutlineEvent, listenerProxy);
                    break;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean handle121Render(Object context, Object hitResult) {
        try {
            if (context == null || hitResult == null) {
                return true;
            }

            BlockyOutlineSettings s = BlockyOutlineSettings.get();

            // 1. Get matrixStack (class_4587)
            Object matrices = invokeAny(context, "matrixStack", "getMatrices");
            if (matrices == null) return true;

            // 2. Get consumers (class_4597)
            Object consumers = invokeAny(context, "consumers", "getConsumers");
            if (consumers == null) return true;

            // 3. Get camera (class_4184)
            Object camera = invokeAny(context, "camera", "getCamera");
            if (camera == null) return true;

            // 4. Get camera pos (class_243)
            Object camPos = invokeAny(camera, "getPos", "pos");
            if (camPos == null) return true;

            double camX = getDouble(camPos, "getX", "x", "field_1352");
            double camY = getDouble(camPos, "getY", "y", "field_1351");
            double camZ = getDouble(camPos, "getZ", "z", "field_1350");

            // 5. Get BlockPos (class_2338) from hitResult (class_239 / class_3965)
            Object blockPos = invokeAny(hitResult, "getBlockPos", "method_17777");
            if (blockPos == null) return true;

            int bx = getInt(blockPos, "getX", "x", "field_10255");
            int by = getInt(blockPos, "getY", "y", "field_10254");
            int bz = getInt(blockPos, "getZ", "z", "field_10253");

            // 6. Get world (class_638)
            Object world = invokeAny(context, "world", "getWorld");
            if (world == null) return true;

            // 7. Get blockState (class_2680)
            Object blockState = invokeWithArg(world, "getBlockState", blockPos);
            if (blockState == null) {
                blockState = invokeWithArg(world, "method_8320", blockPos); // Intermediary getBlockState
            }
            if (blockState == null) return true;

            // 8. Get outline shape (class_265)
            Object shape = invokeWithArgs(blockState, "getOutlineShape", world, blockPos);
            if (shape == null) {
                shape = invokeWithArgs(blockState, "method_26218", world, blockPos); // Intermediary getOutlineShape
            }
            if (shape == null) return true;

            Boolean isEmpty = (Boolean) invokeAny(shape, "isEmpty", "method_1110");
            if (isEmpty != null && isEmpty) {
                return true;
            }

            long now = System.currentTimeMillis();
            int outlineColor = s.getOutlineArgb(now);
            float a = (float) (outlineColor >> 24 & 0xFF) / 255.0f;
            float r = (float) (outlineColor >> 16 & 0xFF) / 255.0f;
            float g = (float) (outlineColor >> 8 & 0xFF) / 255.0f;
            float b = (float) (outlineColor & 0xFF) / 255.0f;

            // 9. Get RenderLayer lines & VertexConsumer
            Object linesLayer = null;
            try {
                Class<?> renderLayerClass = Class.forName("net.minecraft.class_1921"); // Intermediary for RenderLayer
                linesLayer = invokeAny(renderLayerClass, "method_23580", "getLines");
            } catch (Throwable e) {
                try {
                    Class<?> renderLayerClass = Class.forName("net.minecraft.client.render.RenderLayer");
                    linesLayer = invokeAny(renderLayerClass, "getLines");
                } catch (Throwable ignored) {}
            }

            if (linesLayer == null) return true;

            Object vertexConsumer = invokeWithArg(consumers, "getBuffer", linesLayer);
            if (vertexConsumer == null) {
                vertexConsumer = invokeWithArg(consumers, "method_22991", linesLayer); // Intermediary getBuffer
            }
            if (vertexConsumer == null) return true;

            // 10. Draw shape outline via WorldRenderer (class_761)
            Class<?> worldRendererClass = null;
            try {
                worldRendererClass = Class.forName("net.minecraft.class_761");
            } catch (Throwable e) {
                try {
                    worldRendererClass = Class.forName("net.minecraft.client.render.WorldRenderer");
                } catch (Throwable ignored) {}
            }

            if (worldRendererClass != null) {
                for (Method m : worldRendererClass.getMethods()) {
                    if (m.getName().equals("drawShapeOutline") || m.getName().equals("method_22983")) {
                        Class<?>[] p = m.getParameterTypes();
                        if (p.length == 9) {
                            m.invoke(null, matrices, vertexConsumer, shape, (double) bx - camX, (double) by - camY, (double) bz - camZ, r, g, b, a);
                            return false; // Successfully rendered and cancelled vanilla outline!
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    private static Object invokeAny(Object target, String... names) {
        if (target == null) return null;
        Class<?> clazz = (target instanceof Class<?>) ? (Class<?>) target : target.getClass();
        Object obj = (target instanceof Class<?>) ? null : target;
        for (String name : names) {
            try {
                Method m = clazz.getMethod(name);
                return m.invoke(obj);
            } catch (Throwable ignored) {}
        }
        for (String name : names) {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == 0) {
                    try {
                        return m.invoke(obj);
                    } catch (Throwable ignored) {}
                }
            }
        }
        return null;
    }

    private static Object invokeWithArg(Object target, String name, Object arg) {
        if (target == null || arg == null) return null;
        for (Method m : target.getClass().getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) {
                try {
                    return m.invoke(target, arg);
                } catch (Throwable ignored) {}
            }
        }
        return null;
    }

    private static Object invokeWithArgs(Object target, String name, Object arg1, Object arg2) {
        if (target == null) return null;
        for (Method m : target.getClass().getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 2) {
                try {
                    return m.invoke(target, arg1, arg2);
                } catch (Throwable ignored) {}
            }
        }
        return null;
    }

    private static double getDouble(Object target, String... names) {
        for (String name : names) {
            try {
                Method m = target.getClass().getMethod(name);
                return ((Number) m.invoke(target)).doubleValue();
            } catch (Throwable ignored) {}
            try {
                return target.getClass().getField(name).getDouble(target);
            } catch (Throwable ignored) {}
        }
        return 0.0;
    }

    private static int getInt(Object target, String... names) {
        for (String name : names) {
            try {
                Method m = target.getClass().getMethod(name);
                return ((Number) m.invoke(target)).intValue();
            } catch (Throwable ignored) {}
            try {
                return target.getClass().getField(name).getInt(target);
            } catch (Throwable ignored) {}
        }
        return 0;
    }
}
