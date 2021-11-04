package com.konasclient.konas.event.events.render;

public class DrawFramebuffersEvent {
    private static DrawFramebuffersEvent INSTANCE = new DrawFramebuffersEvent();

    public static DrawFramebuffersEvent get() {
        return INSTANCE;
    }
}
