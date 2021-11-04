package com.konasclient.konas.event.listeners;

import com.konasclient.konas.event.events.client.KeyEvent;
import com.konasclient.konas.module.ModuleManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class KeyListener {

    @EventHandler
    public void onKey(KeyEvent event) {
        if (MinecraftClient.getInstance().currentScreen == null) {
            ModuleManager.getModules().forEach(module -> {
                if (module.getKeybind() == event.keyCode) {
                    if (module.isHold()) {
                        if (event.state == GLFW.GLFW_PRESS) {
                            module.toggle(true);
                        } else if (event.state == GLFW.GLFW_RELEASE) {
                            module.toggle(false);
                        }
                    } else if (event.state == GLFW.GLFW_PRESS) {
                        module.toggle();
                    }
                }
            });
        }
    }
}
