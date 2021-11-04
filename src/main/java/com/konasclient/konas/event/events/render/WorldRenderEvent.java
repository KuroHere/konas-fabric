package com.konasclient.konas.event.events.render;

public class WorldRenderEvent {
    private static WorldRenderEvent INSTANCE = new WorldRenderEvent();

    public float tickDelta;

    public static WorldRenderEvent get(float tickDelta) {
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
