package com.konasclient.konas.event.events.render;

public class RenderHudEvent {
    private static final RenderHudEvent INSTANCE = new RenderHudEvent();

    public float tickDelta;

    public static RenderHudEvent get(float tickDelta) {
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
