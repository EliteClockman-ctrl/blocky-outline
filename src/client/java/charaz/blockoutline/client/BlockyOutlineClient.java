package charaz.blockoutline.client;

import charaz.blockoutline.config.BlockyOutlineSettings;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.fabricmc.api.ClientModInitializer;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockyOutlineClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("blocky-outline");
    private static Object menuKeyBinding = null;
    private static boolean wasMKeyDown = false;

    @Override
    public void onInitializeClient() {
        BlockyOutlineSettings.load();
        registerMenuHotkey();
        initRenderEvents();
        LOGGER.info("[Blocky Outline] Client initialization complete!");
    }

    private static void initRenderEvents() {
        try {
            Class.forName("charaz.blockoutline.client.ModernRenderHandler")
                 .getMethod("register")
                 .invoke(null);
            LOGGER.info("[Blocky Outline] ModernRenderHandler registered.");
        } catch (Throwable ignored) {
        }

        try {
            Class.forName("charaz.blockoutline.client.LegacyRenderHandler")
                 .getMethod("register")
                 .invoke(null);
            LOGGER.info("[Blocky Outline] LegacyRenderHandler registered.");
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
                    LOGGER.info("[Blocky Outline] KeyBinding registered successfully.");
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[Blocky Outline] Failed to register keybind: " + t);
        }

        try {
            Class<?> clientTickEventsClass = Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents");
            Object endClientTickEvent = clientTickEventsClass.getField("END_CLIENT_TICK").get(null);
            
            Class<?> endTickClass = Class.forName("net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents$EndTick");
            Object listenerProxy = Proxy.newProxyInstance(
                    BlockyOutlineClient.class.getClassLoader(),
                    new Class<?>[]{endTickClass},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (args != null && args.length > 0) {
                                onTick(args[0]);
                            }
                            return null;
                        }
                    }
            );

            Class<?> eventClass = Class.forName("net.fabricmc.fabric.api.event.Event");
            Method registerMethod = eventClass.getMethod("register", Object.class);
            registerMethod.invoke(endClientTickEvent, listenerProxy);
            LOGGER.info("[Blocky Outline] ClientTickEvents.END_CLIENT_TICK registered successfully!");
        } catch (Throwable t) {
            LOGGER.error("[Blocky Outline] Failed to register ClientTickEvents: " + t);
        }
    }

    private static void onTick(Object client) {
        if (client == null) return;
        try {
            boolean triggered = false;

            // Method 1: Check standard KeyBinding
            if (menuKeyBinding != null) {
                try {
                    Method wasPressed = menuKeyBinding.getClass().getMethod("method_1434"); // Intermediary wasPressed
                    while ((boolean) wasPressed.invoke(menuKeyBinding)) {
                        triggered = true;
                    }
                } catch (Throwable e) {
                    try {
                        Method wasPressed = menuKeyBinding.getClass().getMethod("wasPressed");
                        while ((boolean) wasPressed.invoke(menuKeyBinding)) {
                            triggered = true;
                        }
                    } catch (Throwable e2) {
                        try {
                            Method consumeMethod = menuKeyBinding.getClass().getMethod("consumeClick");
                            while ((boolean) consumeMethod.invoke(menuKeyBinding)) {
                                triggered = true;
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }

            // Method 2: Direct GLFW Input check (Fallback)
            try {
                long windowHandle = 0;
                Object window = null;
                try {
                    window = client.getClass().getMethod("method_22683").invoke(client); // getWindow
                } catch (Throwable e) {
                    try {
                        window = client.getClass().getMethod("getWindow").invoke(client);
                    } catch (Throwable ignored) {}
                }

                if (window != null) {
                    try {
                        windowHandle = (long) window.getClass().getMethod("method_4490").invoke(window); // getHandle
                    } catch (Throwable e) {
                        try {
                            windowHandle = (long) window.getClass().getMethod("getHandle").invoke(window);
                        } catch (Throwable ignored) {}
                    }
                }

                if (windowHandle != 0) {
                    Object currentScreen = null;
                    try {
                        currentScreen = client.getClass().getField("field_1755").get(client);
                    } catch (Throwable e) {
                        try {
                            currentScreen = client.getClass().getField("screen").get(client);
                        } catch (Throwable ignored) {}
                    }

                    boolean isMDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_M) == GLFW.GLFW_PRESS;
                    if (isMDown && !wasMKeyDown) {
                        Class<?> menuClass = null;
                        try {
                            menuClass = Class.forName("charaz.blockoutline.client.ui.BlockyOutlineMenuScreen");
                        } catch (Throwable ignored) {}

                        if (currentScreen == null || (menuClass != null && menuClass.isInstance(currentScreen))) {
                            triggered = true;
                        }
                    }
                    wasMKeyDown = isMDown;
                }
            } catch (Throwable ignored) {}

            if (triggered) {
                LOGGER.info("[Blocky Outline] Key M triggered! Opening/closing menu...");
                openScreen(client);
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
                        LOGGER.info("[Blocky Outline] Closed existing menu screen.");
                        return;
                    }
                }
            } else {
                Object newScreen = menuClass.getDeclaredConstructor().newInstance();
                for (Method m : client.getClass().getMethods()) {
                    if (m.getName().equals("setScreen") || m.getName().equals("method_1507") || m.getName().equals("setScreenAndShow")) {
                        m.invoke(client, newScreen);
                        LOGGER.info("[Blocky Outline] Opened menu screen successfully!");
                        return;
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[Blocky Outline] Error opening menu screen: " + t, t);
        }
    }
}
