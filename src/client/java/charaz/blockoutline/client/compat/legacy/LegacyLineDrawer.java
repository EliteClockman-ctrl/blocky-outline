package charaz.blockoutline.client.compat.legacy;

import charaz.blockoutline.client.compat.LineVertexDrawer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public final class LegacyLineDrawer implements LineVertexDrawer {
    private static MethodHandle lineWidthHandle;
    private static boolean lineWidthInit = false;
    private static float lastWidth = -1.0f;

    @Override
    public void prepare(float width) {
        if (Math.abs(width - lastWidth) < 0.05f) return;
        lastWidth = width;
        if (!lineWidthInit) {
            lineWidthInit = true;
            try {
                Class<?> rsClass = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
                Method m = rsClass.getMethod("lineWidth", float.class);
                m.setAccessible(true);
                lineWidthHandle = MethodHandles.lookup().unreflect(m);
            } catch (Throwable ignored) {
            }
        }
        if (lineWidthHandle != null) {
            try {
                lineWidthHandle.invokeExact(width);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void line(VertexConsumer consumer, PoseStack.Pose pose,
                     float x1, float y1, float z1, float x2, float y2, float z2,
                     float nx, float ny, float nz, int r, int g, int b, int a, float width) {
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
    }
}
