package charaz.blockoutline.client;

public final class LegacyRenderHandler {
    private LegacyRenderHandler() {
    }

    public static void register() {
        try {
            Class<?> eventsClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents");
            Object beforeOutlineEvent = eventsClass.getField("BEFORE_BLOCK_OUTLINE").get(null);
            Class<?> listenerClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents$BeforeBlockOutline");

            Object listenerProxy = java.lang.reflect.Proxy.newProxyInstance(
                    LegacyRenderHandler.class.getClassLoader(),
                    new Class<?>[]{listenerClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("beforeBlockOutline") && args != null && args.length >= 2) {
                            return handle121Render(args[0], args[1]);
                        }
                        return true;
                    }
            );

            for (java.lang.reflect.Method m : beforeOutlineEvent.getClass().getMethods()) {
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
            charaz.blockoutline.config.BlockyOutlineSettings s = charaz.blockoutline.config.BlockyOutlineSettings.get();
            
            // Invoke context.matrixStack()
            Object matrices = null;
            for (java.lang.reflect.Method m : context.getClass().getMethods()) {
                if (m.getName().equals("matrixStack") || m.getName().equals("getMatrices")) {
                    matrices = m.invoke(context);
                    break;
                }
            }
            if (matrices == null) return true;

            // Invoke context.consumers()
            Object consumers = null;
            for (java.lang.reflect.Method m : context.getClass().getMethods()) {
                if (m.getName().equals("consumers") || m.getName().equals("getConsumers")) {
                    consumers = m.invoke(context);
                    break;
                }
            }
            if (consumers == null) return true;

            // Invoke context.camera()
            Object camera = null;
            for (java.lang.reflect.Method m : context.getClass().getMethods()) {
                if (m.getName().equals("camera") || m.getName().equals("getCamera")) {
                    camera = m.invoke(context);
                    break;
                }
            }
            if (camera == null) return true;

            // Get camera pos
            Object camPos = null;
            for (java.lang.reflect.Method m : camera.getClass().getMethods()) {
                if (m.getName().equals("getPos") || m.getName().equals("pos")) {
                    camPos = m.invoke(camera);
                    break;
                }
            }
            if (camPos == null) return true;

            double camX = (double) camPos.getClass().getMethod("getX").invoke(camPos);
            double camY = (double) camPos.getClass().getMethod("getY").invoke(camPos);
            double camZ = (double) camPos.getClass().getMethod("getZ").invoke(camPos);

            // Get BlockPos from hitResult
            Object blockPos = null;
            for (java.lang.reflect.Method m : hitResult.getClass().getMethods()) {
                if (m.getName().equals("getBlockPos")) {
                    blockPos = m.invoke(hitResult);
                    break;
                }
            }
            if (blockPos == null) return true;

            int bx = (int) blockPos.getClass().getMethod("getX").invoke(blockPos);
            int by = (int) blockPos.getClass().getMethod("getY").invoke(blockPos);
            int bz = (int) blockPos.getClass().getMethod("getZ").invoke(blockPos);

            // Get world from context
            Object world = null;
            for (java.lang.reflect.Method m : context.getClass().getMethods()) {
                if (m.getName().equals("world") || m.getName().equals("getWorld")) {
                    world = m.invoke(context);
                    break;
                }
            }
            if (world == null) return true;

            // Get block state & outline shape
            Object blockState = world.getClass().getMethod("getBlockState", blockPos.getClass()).invoke(world, blockPos);
            if (blockState == null) return true;

            Object shape = blockState.getClass().getMethod("getOutlineShape", world.getClass(), blockPos.getClass()).invoke(blockState, world, blockPos);
            if (shape == null || (boolean) shape.getClass().getMethod("isEmpty").invoke(shape)) {
                return true;
            }

            long now = System.currentTimeMillis();
            int outlineColor = s.getOutlineArgb(now);
            float a = (float)(outlineColor >> 24 & 0xFF) / 255.0f;
            float r = (float)(outlineColor >> 16 & 0xFF) / 255.0f;
            float g = (float)(outlineColor >> 8 & 0xFF) / 255.0f;
            float b = (float)(outlineColor & 0xFF) / 255.0f;

            // Get lines RenderLayer/RenderType
            Class<?> renderLayerClass = Class.forName("net.minecraft.client.render.RenderLayer");
            Object linesLayer = renderLayerClass.getMethod("getLines").invoke(null);
            Object vertexConsumer = consumers.getClass().getMethod("getBuffer", renderLayerClass).invoke(consumers, linesLayer);

            // Draw outline via WorldRenderer.drawShapeOutline
            Class<?> worldRendererClass = Class.forName("net.minecraft.client.render.WorldRenderer");
            for (java.lang.reflect.Method m : worldRendererClass.getMethods()) {
                if (m.getName().equals("drawShapeOutline")) {
                    Class<?>[] p = m.getParameterTypes();
                    if (p.length == 9) {
                        m.invoke(null, matrices, vertexConsumer, shape, (double)bx - camX, (double)by - camY, (double)bz - camZ, r, g, b, a);
                        return false;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return true;
    }
}
