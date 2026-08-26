package charaz.blockoutline.client;

import charaz.blockoutline.config.BlockyOutlineSettings;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

        try {
            Class.forName("charaz.blockoutline.client.LegacyRenderHandler")
                 .getMethod("register")
                 .invoke(null);
        } catch (Throwable ignored) {
        }
    }

    private static void registerMenuHotkey() {
        try {
            Class<?> keyClass = null;
            try {
                keyClass = Class.forName("net.minecraft.class_304"); // Intermediary for KeyBinding (1.21)
            } catch (ClassNotFoundException e) {
                try {
                    keyClass = Class.forName("net.minecraft.client.option.KeyBinding");
                } catch (ClassNotFoundException e2) {
                    try {
                        keyClass = Class.forName("net.minecraft.client.KeyMapping");
                    } catch (ClassNotFoundException ignored) {}
                }
            }

            if (keyClass != null) {
                Object keyInstance = null;
                Class<?> inputTypeClass = null;
                Object keySymType = null;
                try {
                    inputTypeClass = Class.forName("net.minecraft.class_3675$class_307"); // Intermediary InputUtil.Type
                    for (Object enumConst : inputTypeClass.getEnumConstants()) {
                        if ("KEYSYM".equalsIgnoreCase(enumConst.toString())) {
                            keySymType = enumConst;
                            break;
                        }
                    }
                } catch (Throwable ignored) {
                    try {
                        inputTypeClass = Class.forName("com.mojang.blaze3d.platform.InputConstants$Type");
                        for (Object enumConst : inputTypeClass.getEnumConstants()) {
                            if ("KEYSYM".equalsIgnoreCase(enumConst.toString())) {
                                keySymType = enumConst;
                                break;
                            }
                        }
                    } catch (Throwable ignored2) {}
                }

                for (Constructor<?> c : keyClass.getConstructors()) {
                    Class<?>[] p = c.getParameterTypes();
                    if (p.length >= 3 && p[0] == String.class) {
                        try {
                            if (p.length == 4 && inputTypeClass != null && keySymType != null && p[1] == inputTypeClass) {
                                keyInstance = c.newInstance("key.blocky-outline.open_menu", keySymType, 77, "key.category.misc");
                            } else if (p.length == 3 && p[1] == int.class) {
                                keyInstance = c.newInstance("key.blocky-outline.open_menu", 77, "key.category.misc");
                            }
                            if (keyInstance != null) break;
                        } catch (Throwable ignored) {}
                    }
                }

                if (keyInstance != null) {
                    try {
                        Class<?> helperClass = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
                        for (Method m : helperClass.getMethods()) {
                            if (m.getName().equals("registerKeyBinding") || m.getName().startsWith("registerKey")) {
                                menuKeyBinding = m.invoke(null, keyInstance);
                                break;
                            }
                        }
                    } catch (Throwable e) {
                        try {
                            Class<?> helperClass = Class.forName("net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper");
                            for (Method m : helperClass.getMethods()) {
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
            
            Class<?> endTickClass = Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents$EndTick");
            Object listenerProxy = Proxy.newProxyInstance(
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

            for (Method m : endClientTickEvent.getClass().getMethods()) {
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
                player = client.getClass().getField("field_1724").get(client); // Intermediary for player
            } catch (Throwable e) {
                try {
                    player = client.getClass().getField("player").get(client);
                } catch (Throwable e2) {
                    try {
                        player = client.getClass().getMethod("getPlayer").invoke(client);
                    } catch (Throwable ignored) {}
                }
            }
            if (player == null) return;

            if (menuKeyBinding != null) {
                boolean pressed = false;
                try {
                    Method wasPressed = menuKeyBinding.getClass().getMethod("method_1434"); // Intermediary wasPressed
                    while ((boolean) wasPressed.invoke(menuKeyBinding)) {
                        pressed = true;
                    }
                } catch (Throwable e) {
                    try {
                        Method wasPressed = menuKeyBinding.getClass().getMethod("wasPressed");
                        while ((boolean) wasPressed.invoke(menuKeyBinding)) {
                            pressed = true;
                        }
                    } catch (Throwable e2) {
                        try {
                            Method consumeMethod = menuKeyBinding.getClass().getMethod("consumeClick");
                            while ((boolean) consumeMethod.invoke(menuKeyBinding)) {
                                pressed = true;
                            }
                        } catch (Throwable ignored) {}
                    }
                }
                if (pressed) {
                    openScreen(client);
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void openScreen(Object client) {
        if (client == null) return;
        try {
            Class<?> menuClass = Class.forName("charaz.blockoutline.client.ui.BlockyOutlineMenuScreen");
            Object currentScreen = null;
            try {
                currentScreen = client.getClass().getField("field_1755").get(client); // Intermediary currentScreen
            } catch (Throwable e) {
                try {
                    currentScreen = client.getClass().getField("screen").get(client);
                } catch (Throwable e2) {
                    try {
                        Object gui = client.getClass().getField("gui").get(client);
                        currentScreen = gui.getClass().getMethod("screen").invoke(gui);
                    } catch (Throwable ignored) {}
                }
            }

            if (currentScreen != null && menuClass.isInstance(currentScreen)) {
                for (Method m : client.getClass().getMethods()) {
                    if (m.getName().equals("setScreen") || m.getName().equals("method_1507") || m.getName().equals("setScreenAndShow")) {
                        m.invoke(client, new Object[]{null});
                        return;
                    }
                }
            } else {
                Object newScreen = menuClass.getDeclaredConstructor().newInstance();
                for (Method m : client.getClass().getMethods()) {
                    if (m.getName().equals("setScreen") || m.getName().equals("method_1507") || m.getName().equals("setScreenAndShow")) {
                        m.invoke(client, newScreen);
                        return;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
