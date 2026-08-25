package charaz.blockoutline.client;

import charaz.blockoutline.client.renderer.OutlineRenderer;
import charaz.blockoutline.config.BlockyOutlineSettings;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockyOutlineClient implements ClientModInitializer {
    private static Object menuKeyBinding = null;

    private static boolean initializedPos = false;
    private static double smoothedX = 0.0;
    private static double smoothedY = 0.0;
    private static double smoothedZ = 0.0;
    private static long lastRenderTimeMs = 0L;

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();
        this.registerMenuHotkey();

        try {
            LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, renderState) -> {
                if (renderState == null) {
                    return true;
                }
                BlockPos pos = renderState.pos();
                VoxelShape shape = renderState.shape();
                if (pos == null || shape == null || shape.isEmpty()) {
                    initializedPos = false;
                    return true;
                }
                BlockyOutlineSettings s = BlockyOutlineSettings.get();
                PoseStack matrices = context.poseStack();
                if (matrices == null) {
                    return true;
                }
                if (context.levelState() == null || context.levelState().cameraRenderState == null) {
                    return true;
                }
                Vec3 cam = context.levelState().cameraRenderState.pos;
                if (cam == null) {
                    return true;
                }

                double targetX = (double) pos.getX();
                double targetY = (double) pos.getY();
                double targetZ = (double) pos.getZ();

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
                        if (distSq > 25.0) {
                            smoothedX = targetX;
                            smoothedY = targetY;
                            smoothedZ = targetZ;
                        } else if (distSq < 0.0001) {
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

                double dx = smoothedX - cam.x;
                double dy = smoothedY - cam.y;
                double dz = smoothedZ - cam.z;

                matrices.pushPose();
                matrices.translate(dx, dy, dz);

                int outlineColor = s.getOutlineArgb(now);
                context.submitNodeCollector().submitShapeOutline(matrices, shape, RenderTypes.lines(), outlineColor, s.outlineWidth, false);

                if (s.fillEnabled) {
                    int fillColor = s.getFillArgb(now);
                    context.submitNodeCollector().submitCustomGeometry(
                            matrices,
                            RenderTypes.debugFilledBox(),
                            (pose, consumer) -> OutlineRenderer.renderFilledBox(consumer, pose, shape, fillColor)
                    );
                }

                matrices.popPose();
                return false;
            });
        } catch (Throwable ignored) {
        }
    }

    private void registerMenuHotkey() {
        try {
            Class<?> keyClass = null;
            try {
                keyClass = Class.forName("net.minecraft.client.KeyMapping");
            } catch (ClassNotFoundException e) {
                try {
                    keyClass = Class.forName("net.minecraft.client.option.KeyBinding");
                } catch (ClassNotFoundException ignored) {}
            }

            if (keyClass != null) {
                Object keyInstance = null;
                for (java.lang.reflect.Constructor<?> c : keyClass.getConstructors()) {
                    Class<?>[] p = c.getParameterTypes();
                    if (p.length >= 3 && p[0] == String.class && (p[1] == int.class || p[1] == InputConstants.Type.class)) {
                        try {
                            if (p.length == 4) {
                                keyInstance = c.newInstance("key.blocky-outline.open_menu", InputConstants.Type.KEYSYM, 77, "key.categories.misc");
                            } else if (p.length == 3) {
                                keyInstance = c.newInstance("key.blocky-outline.open_menu", 77, "key.categories.misc");
                            }
                            if (keyInstance != null) break;
                        } catch (Throwable ignored) {}
                    }
                }

                if (keyInstance != null) {
                    try {
                        Class<?> helperClass = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
                        for (java.lang.reflect.Method m : helperClass.getMethods()) {
                            if (m.getName().startsWith("registerKey")) {
                                menuKeyBinding = m.invoke(null, keyInstance);
                                break;
                            }
                        }
                    } catch (Throwable e) {
                        try {
                            Class<?> helperClass = Class.forName("net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper");
                            for (java.lang.reflect.Method m : helperClass.getMethods()) {
                                if (m.getName().startsWith("registerKey")) {
                                    menuKeyBinding = m.invoke(null, keyInstance);
                                    break;
                                }
                            }
                        } catch (Throwable ignored2) {}
                    }
                    if (menuKeyBinding == null) {
                        menuKeyBinding = keyInstance;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            if (menuKeyBinding != null) {
                try {
                    java.lang.reflect.Method consumeMethod = menuKeyBinding.getClass().getMethod("consumeClick");
                    while ((boolean) consumeMethod.invoke(menuKeyBinding)) {
                        openScreen(client);
                    }
                } catch (Throwable err) {
                    try {
                        java.lang.reflect.Method wasPressed = menuKeyBinding.getClass().getMethod("wasPressed");
                        while ((boolean) wasPressed.invoke(menuKeyBinding)) {
                            openScreen(client);
                        }
                    } catch (Throwable err2) {}
                }
            }
        });
    }

    private static void openScreen(net.minecraft.client.Minecraft client) {
        try {
            Class<?> menuClass = Class.forName("charaz.blockoutline.client.ui.BlockyOutlineMenuScreen");
            Object currentScreen = client.gui.getClass().getMethod("screen").invoke(client.gui);
            if (currentScreen != null && menuClass.isInstance(currentScreen)) {
                for (java.lang.reflect.Method m : client.getClass().getMethods()) {
                    if (m.getName().equals("setScreenAndShow") || m.getName().equals("setScreen")) {
                        m.invoke(client, new Object[]{null});
                        return;
                    }
                }
            } else {
                Object newScreen = menuClass.getDeclaredConstructor().newInstance();
                for (java.lang.reflect.Method m : client.getClass().getMethods()) {
                    if (m.getName().equals("setScreenAndShow") || m.getName().equals("setScreen")) {
                        m.invoke(client, newScreen);
                        return;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
