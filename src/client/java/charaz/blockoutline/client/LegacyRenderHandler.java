package charaz.blockoutline.client;

import charaz.blockoutline.config.BlockyOutlineSettings;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LegacyRenderHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("blocky-outline");

    private static boolean initializedPos = false;
    private static double smoothedX = 0.0;
    private static double smoothedY = 0.0;
    private static double smoothedZ = 0.0;
    private static long lastRenderTimeMs = 0L;

    private LegacyRenderHandler() {
    }

    public static void register() {
        try {
            Class<?> eventsClass = null;
            try {
                eventsClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents");
            } catch (Throwable t1) {
                try {
                    eventsClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents");
                } catch (Throwable ignored) {}
            }

            if (eventsClass != null) {
                Object beforeOutlineEvent = eventsClass.getField("BEFORE_BLOCK_OUTLINE").get(null);
                Class<?> listenerClass = null;
                try {
                    listenerClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents$BeforeBlockOutline");
                } catch (Throwable t2) {
                    try {
                        listenerClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents$BeforeBlockOutline");
                    } catch (Throwable ignored) {}
                }

                if (listenerClass != null && beforeOutlineEvent != null) {
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

                    Class<?> eventClass = Class.forName("net.fabricmc.fabric.api.event.Event");
                    Method registerMethod = eventClass.getMethod("register", Object.class);
                    registerMethod.invoke(beforeOutlineEvent, listenerProxy);
                    LOGGER.info("[Blocky Outline] LegacyRenderHandler successfully hooked BEFORE_BLOCK_OUTLINE!");
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[Blocky Outline] Failed to hook BEFORE_BLOCK_OUTLINE: " + t, t);
        }
    }

    private static boolean handle121Render(Object context, Object hitOrRenderState) {
        try {
            if (context == null || hitOrRenderState == null) {
                return true;
            }

            BlockyOutlineSettings s = BlockyOutlineSettings.get();

            // 1. Get matrixStack / matrices (class_4587 / fzm)
            Object matrices = invokeAny(context, "matrixStack", "matrices", "getMatrices", "poseStack");
            if (matrices == null) return true;

            // 2. Get consumers / vertexConsumerProvider (class_4597 / fzo)
            Object consumers = invokeAny(context, "consumers", "getConsumers", "vertexConsumers");
            if (consumers == null) return true;

            // 3. Extract BlockPos and VoxelShape from hitOrRenderState (class_12074 iko or HitResult)
            Object blockPos = null;
            Object shape = null;

            // Check if class_12074 (Record: iko)
            try {
                blockPos = invokeAny(hitOrRenderState, "pos", "a", "getBlockPos", "method_17777");
                shape = invokeAny(hitOrRenderState, "shape", "d", "getShape");
            } catch (Throwable ignored) {}

            // Fallback: If shape not in record, resolve from world & blockPos
            if (blockPos == null) {
                blockPos = invokeAny(hitOrRenderState, "getBlockPos", "method_17777");
            }
            if (blockPos == null) return true;

            int bx = getInt(blockPos, "getX", "x", "field_10255", "v", "u", "w");
            int by = getInt(blockPos, "getY", "y", "field_10254");
            int bz = getInt(blockPos, "getZ", "z", "field_10253");

            if (shape == null) {
                Object world = invokeAny(context, "world", "getWorld");
                if (world == null) {
                    try {
                        Class<?> mcClass = Class.forName("net.minecraft.class_310");
                        Object mc = invokeAny(mcClass, "getInstance", "method_1551", "V");
                        if (mc != null) {
                            world = getFieldAny(mc, "world", "level", "r", "field_1687");
                        }
                    } catch (Throwable ignored) {}
                }
                if (world != null) {
                    Object blockState = invokeWithArg(world, "getBlockState", blockPos);
                    if (blockState == null) {
                        blockState = invokeWithArg(world, "method_8320", blockPos);
                    }
                    if (blockState != null) {
                        shape = invokeWithArgs(blockState, "getOutlineShape", world, blockPos);
                        if (shape == null) {
                            shape = invokeWithArgs(blockState, "method_26218", world, blockPos);
                        }
                    }
                }
            }

            if (shape == null) return true;

            Boolean isEmpty = (Boolean) invokeAny(shape, "isEmpty", "method_1110", "b", "c");
            if (isEmpty != null && isEmpty) {
                return true;
            }

            // 4. Get Camera pos
            double camX = 0, camY = 0, camZ = 0;
            boolean camFound = false;
            try {
                Object camera = invokeAny(context, "camera", "getCamera");
                if (camera != null) {
                    Object camPos = invokeAny(camera, "getPos", "pos", "d");
                    if (camPos != null) {
                        camX = getDouble(camPos, "getX", "x", "field_1352", "a");
                        camY = getDouble(camPos, "getY", "y", "field_1351", "b");
                        camZ = getDouble(camPos, "getZ", "z", "field_1350", "c");
                        camFound = true;
                    }
                }
            } catch (Throwable ignored) {}

            if (!camFound) {
                try {
                    Class<?> mcClass = Class.forName("net.minecraft.class_310");
                    Object mc = invokeAny(mcClass, "getInstance", "method_1551", "V");
                    if (mc != null) {
                        Object gameRenderer = getFieldAny(mc, "gameRenderer", "j", "field_1773");
                        if (gameRenderer != null) {
                            Object camera = invokeAny(gameRenderer, "getCamera", "method_3190");
                            if (camera != null) {
                                Object camPos = invokeAny(camera, "getPos", "pos", "d");
                                if (camPos != null) {
                                    camX = getDouble(camPos, "getX", "x", "field_1352", "a");
                                    camY = getDouble(camPos, "getY", "y", "field_1351", "b");
                                    camZ = getDouble(camPos, "getZ", "z", "field_1350", "c");
                                    camFound = true;
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }

            // Smooth transition calculation
            double targetX = (double) bx;
            double targetY = (double) by;
            double targetZ = (double) bz;
            long now = System.currentTimeMillis();

            if (s.smoothTransition) {
                if (!initializedPos) {
                    smoothedX = targetX;
                    smoothedY = targetY;
                    smoothedZ = targetZ;
                    initializedPos = true;
                    lastRenderTimeMs = now;
                } else {
                    long dt = Math.max(1L, Math.min(100L, now - lastRenderTimeMs));
                    lastRenderTimeMs = now;
                    double distSq = (targetX - smoothedX) * (targetX - smoothedX) +
                                    (targetY - smoothedY) * (targetY - smoothedY) +
                                    (targetZ - smoothedZ) * (targetZ - smoothedZ);
                    if (distSq > 25.0 || distSq < 0.0001) {
                        smoothedX = targetX;
                        smoothedY = targetY;
                        smoothedZ = targetZ;
                    } else {
                        double step = 1.0 - Math.exp(-0.120 * (double) dt);
                        smoothedX += (targetX - smoothedX) * step;
                        smoothedY += (targetY - smoothedY) * step;
                        smoothedZ += (targetZ - smoothedZ) * step;
                    }
                }
            } else {
                smoothedX = targetX;
                smoothedY = targetY;
                smoothedZ = targetZ;
                initializedPos = true;
            }

            double renderX = smoothedX - camX;
            double renderY = smoothedY - camY;
            double renderZ = smoothedZ - camZ;

            int outlineColor = s.getOutlineArgb(now);
            float a = (float) (outlineColor >> 24 & 0xFF) / 255.0f;
            float r = (float) (outlineColor >> 16 & 0xFF) / 255.0f;
            float g = (float) (outlineColor >> 8 & 0xFF) / 255.0f;
            float b = (float) (outlineColor & 0xFF) / 255.0f;

            // 5. Get RenderLayer lines & VertexConsumer
            Object linesLayer = null;
            try {
                Class<?> renderLayerClass = Class.forName("net.minecraft.class_1921");
                linesLayer = invokeAny(renderLayerClass, "method_23580", "getLines");
            } catch (Throwable e) {
                try {
                    Class<?> renderLayerClass = Class.forName("net.minecraft.client.render.RenderLayer");
                    linesLayer = invokeAny(renderLayerClass, "getLines");
                } catch (Throwable ignored) {}
            }

            Object vertexConsumer = null;
            if (linesLayer != null) {
                vertexConsumer = invokeWithArg(consumers, "getBuffer", linesLayer);
                if (vertexConsumer == null) {
                    vertexConsumer = invokeWithArg(consumers, "method_22991", linesLayer);
                }
            }

            // Fallback vertexConsumer via bufferSource method
            if (vertexConsumer == null) {
                for (Method m : consumers.getClass().getMethods()) {
                    if (m.getParameterCount() == 1) {
                        try {
                            Object res = m.invoke(consumers, linesLayer);
                            if (res != null) {
                                vertexConsumer = res;
                                break;
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }

            if (vertexConsumer == null) return true;

            // 6. Draw shape outline via 1.21.11 hpi or class_761
            boolean drawn = false;

            // Method A: 1.21.11 hpi.a(matrices, vertexConsumer, shape, renderX, renderY, renderZ, color, lineWidth)
            try {
                Class<?> hpiClass = Class.forName("hpi");
                for (Method m : hpiClass.getMethods()) {
                    if (m.getParameterCount() == 8) {
                        m.invoke(null, matrices, vertexConsumer, shape, renderX, renderY, renderZ, outlineColor, s.outlineWidth);
                        drawn = true;
                        break;
                    }
                }
            } catch (Throwable ignored) {}

            // Method B: Standard WorldRenderer.drawShapeOutline
            if (!drawn) {
                try {
                    Class<?> worldRendererClass = Class.forName("net.minecraft.class_761");
                    for (Method m : worldRendererClass.getMethods()) {
                        if (m.getName().equals("drawShapeOutline") || m.getName().equals("method_22983")) {
                            Class<?>[] p = m.getParameterTypes();
                            if (p.length == 9) {
                                m.invoke(null, matrices, vertexConsumer, shape, renderX, renderY, renderZ, r, g, b, a);
                                drawn = true;
                                break;
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }

            if (drawn) {
                return false; // Cancel vanilla outline!
            }
        } catch (Throwable t) {
            LOGGER.error("[Blocky Outline] Render error: " + t, t);
        }
        return true;
    }

    private static Object getFieldAny(Object target, String... names) {
        if (target == null) return null;
        Class<?> clazz = (target instanceof Class<?>) ? (Class<?>) target : target.getClass();
        Object obj = (target instanceof Class<?>) ? null : target;
        for (String name : names) {
            try {
                Field f = clazz.getField(name);
                return f.get(obj);
            } catch (Throwable ignored) {}
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(obj);
            } catch (Throwable ignored) {}
        }
        return null;
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
