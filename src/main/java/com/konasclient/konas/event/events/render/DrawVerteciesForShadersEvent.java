package com.konasclient.konas.event.events.render;

public class DrawVerteciesForShadersEvent {
    private static DrawVerteciesForShadersEvent INSTANCE = new DrawVerteciesForShadersEvent();

    public float tickDelta;

    public static DrawVerteciesForShadersEvent get(float tickDelta) {
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
