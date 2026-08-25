package charaz.blockoutline.client;

import charaz.blockoutline.config.BlockyOutlineSettings;
import net.fabricmc.api.ClientModInitializer;

public class BlockyOutlineClient implements ClientModInitializer {
    private static Object menuKeyBinding = null;

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();
        registerMenuHotkey();
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

    private static void registerMenuHotkey() {
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

        try {
            Class<?> clientTickEventsClass = Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents");
            Object endClientTickEvent = clientTickEventsClass.getField("END_CLIENT_TICK").get(null);
            
            // Register reflection listener to avoid baking net.minecraft.client.Minecraft into LambdaMetafactory
            Class<?> endTickClass = Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents$EndTick");
            Object listenerProxy = java.lang.reflect.Proxy.newProxyInstance(
                    BlockyOutlineClient.class.getClassLoader(),
                    new Class<?>[]{endTickClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("onEndTick") && args != null && args.length > 0) {
                            Object client = args[0];
                            onTick(client);
                        }
                        return null;
                    }
            );

            for (java.lang.reflect.Method m : endClientTickEvent.getClass().getMethods()) {
                if (m.getName().equals("register")) {
                    m.invoke(endClientTickEvent, listenerProxy);
                    break;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void onTick(Object client) {
        if (client == null) return;
        try {
            Object player = null;
            try {
                player = client.getClass().getField("player").get(client);
            } catch (Throwable e) {
                try {
                    player = client.getClass().getMethod("getPlayer").invoke(client);
                } catch (Throwable ignored) {}
            }
            if (player == null) return;

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
        } catch (Throwable ignored) {}
    }

    private static void openScreen(Object client) {
        if (client == null) return;
        try {
            Class<?> menuClass = Class.forName("charaz.blockoutline.client.ui.BlockyOutlineMenuScreen");
            Object gui = null;
            try {
                gui = client.getClass().getField("gui").get(client);
            } catch (Throwable e) {
                try {
                    gui = client.getClass().getMethod("getGui").invoke(client);
                } catch (Throwable ignored) {}
            }
            if (gui != null) {
                Object currentScreen = null;
                for (java.lang.reflect.Method m : gui.getClass().getMethods()) {
                    if (m.getName().equals("screen") || m.getName().equals("getScreen")) {
                        currentScreen = m.invoke(gui);
                        break;
                    }
                }
                if (currentScreen != null && menuClass.isInstance(currentScreen)) {
                    for (java.lang.reflect.Method m : client.getClass().getMethods()) {
                        if (m.getName().equals("setScreenAndShow") || m.getName().equals("setScreen")) {
                            m.invoke(client, new Object[]{null});
                            return;
                        }
                    }
                }
            }

            Object newScreen = menuClass.getDeclaredConstructor().newInstance();
            for (java.lang.reflect.Method m : client.getClass().getMethods()) {
                if (m.getName().equals("setScreenAndShow") || m.getName().equals("setScreen")) {
                    m.invoke(client, newScreen);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
