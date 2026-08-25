package charaz.blockoutline.client;

import charaz.blockoutline.config.BlockyOutlineSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class BlockyOutlineClient implements ClientModInitializer {
    private static Object menuKeyBinding = null;

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();
        this.registerMenuHotkey();
        initRenderEvents();
    }

    private static void initRenderEvents() {
        try {
            Class.forName("charaz.blockoutline.client.ModernRenderHandler")
                 .getMethod("register")
                 .invoke(null);
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
                Class<?> inputTypeClass = null;
                Object keySymType = null;
                try {
                    inputTypeClass = Class.forName("com.mojang.blaze3d.platform.InputConstants$Type");
                    for (Object enumConst : inputTypeClass.getEnumConstants()) {
                        if ("KEYSYM".equals(enumConst.toString())) {
                            keySymType = enumConst;
                            break;
                        }
                    }
                } catch (Throwable ignored) {}

                for (java.lang.reflect.Constructor<?> c : keyClass.getConstructors()) {
                    Class<?>[] p = c.getParameterTypes();
                    if (p.length >= 3 && p[0] == String.class) {
                        try {
                            if (p.length == 4 && inputTypeClass != null && keySymType != null && p[1] == inputTypeClass) {
                                keyInstance = c.newInstance("key.blocky-outline.open_menu", keySymType, 77, "key.categories.misc");
                            } else if (p.length == 3 && p[1] == int.class) {
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
