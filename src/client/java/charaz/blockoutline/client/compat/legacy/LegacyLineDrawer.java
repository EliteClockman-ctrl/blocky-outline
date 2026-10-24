package charaz.blockoutline.client.compat.legacy;

import charaz.blockoutline.client.compat.LineVertexDrawer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.lang.reflect.Method;

public final class LegacyLineDrawer implements LineVertexDrawer {
    private static Method lineWidthMethod;
    private static boolean lineWidthInit = false;
    private static float lastWidth = -1.0f;

    private static void applyLineWidth(float width) {
        if (Math.abs(width - lastWidth) < 0.05f) return;
        lastWidth = width;
        if (!lineWidthInit) {
            lineWidthInit = true;
            try {
                Class<?> rsClass = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
                lineWidthMethod = rsClass.getMethod("lineWidth", float.class);
            } catch (Throwable ignored) {
            }
        }
        if (lineWidthMethod != null) {
            try {
                lineWidthMethod.invoke(null, width);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void line(VertexConsumer consumer, PoseStack.Pose pose,
                     float x1, float y1, float z1, float x2, float y2, float z2,
                     float nx, float ny, float nz, int r, int g, int b, int a, float width) {
        applyLineWidth(width);
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
    }
}
