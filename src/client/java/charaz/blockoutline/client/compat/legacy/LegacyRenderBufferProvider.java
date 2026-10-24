package charaz.blockoutline.client.compat.legacy;

import charaz.blockoutline.client.compat.RenderBufferProvider;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class LegacyRenderBufferProvider implements RenderBufferProvider {
    private static Object legacyLinesType;
    private static Object legacyQuadsType;
    private static MethodHandle getBufferHandle;
    private static boolean initialized = false;

    private static MethodHandle disableDepthTestHandle;
    private static MethodHandle enableDepthTestHandle;
    private static boolean depthMethodsInitialized = false;

    private static void init() {
        if (initialized) return;
        initialized = true;
        try {
            Class<?> rtClass = null;
            for (String name : new String[]{"net.minecraft.client.renderer.RenderType", "net.minecraft.class_1921"}) {
                try {
                    rtClass = Class.forName(name);
                    break;
                } catch (Throwable ignored) {
                }
            }

            if (rtClass != null) {
                for (Method m : rtClass.getDeclaredMethods()) {
                    if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0 && m.getReturnType() == rtClass) {
                        String mName = m.getName();
                        if ("lines".equals(mName) || "method_23594".equals(mName)) {
                            m.setAccessible(true);
                            legacyLinesType = m.invoke(null);
                        } else if ("debugQuads".equals(mName) || "method_23589".equals(mName)) {
                            m.setAccessible(true);
                            legacyQuadsType = m.invoke(null);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void setDepthTest(boolean enable) {
        if (!depthMethodsInitialized) {
            depthMethodsInitialized = true;
            try {
                Class<?> rsClass = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                for (Method m : rsClass.getMethods()) {
                    if (m.getParameterCount() == 0) {
                        if ("disableDepthTest".equals(m.getName())) {
                            m.setAccessible(true);
                            disableDepthTestHandle = lookup.unreflect(m);
                        } else if ("enableDepthTest".equals(m.getName())) {
                            m.setAccessible(true);
                            enableDepthTestHandle = lookup.unreflect(m);
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        try {
            if (enable && enableDepthTestHandle != null) {
                enableDepthTestHandle.invokeExact();
            } else if (!enable && disableDepthTestHandle != null) {
                disableDepthTestHandle.invokeExact();
            }
        } catch (Throwable ignored) {
        }
    }

    private static void flushBuffer(MultiBufferSource consumers) {
        if (consumers instanceof MultiBufferSource.BufferSource bs) {
            try {
                bs.endBatch();
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public VertexConsumer getLineBuffer(MultiBufferSource consumers, boolean xray) {
        init();
        if (xray) {
            flushBuffer(consumers);
            setDepthTest(false);
        }
        return invokeGetBuffer(consumers, legacyLinesType);
    }

    @Override
    public VertexConsumer getQuadBuffer(MultiBufferSource consumers, boolean xray) {
        init();
        if (xray) {
            flushBuffer(consumers);
            setDepthTest(false);
        }
        return invokeGetBuffer(consumers, legacyQuadsType);
    }

    @Override
    public void postRender(MultiBufferSource consumers, boolean xray) {
        if (xray) {
            flushBuffer(consumers);
            setDepthTest(true);
        }
    }

    private static VertexConsumer invokeGetBuffer(MultiBufferSource consumers, Object renderType) {
        if (consumers == null || renderType == null) return null;
        try {
            if (getBufferHandle != null) {
                return (VertexConsumer) getBufferHandle.invoke(consumers, renderType);
            }
            for (Method m : consumers.getClass().getMethods()) {
                if (m.getParameterCount() == 1 && VertexConsumer.class.isAssignableFrom(m.getReturnType())) {
                    m.setAccessible(true);
                    getBufferHandle = MethodHandles.lookup().unreflect(m);
                    return (VertexConsumer) getBufferHandle.invoke(consumers, renderType);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}
