package com.konasclient.konas.event.events.client;

import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent {

    private static final OpenScreenEvent INSTANCE = new OpenScreenEvent();

    public Screen screen;

    public static OpenScreenEvent get(Screen screen) {
        INSTANCE.screen = screen;
        return INSTANCE;
    }

}