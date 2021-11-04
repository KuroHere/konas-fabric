package com.konasclient.konas.event.events.render;

public class LastPassEvent {
    private static final LastPassEvent INSTANCE = new LastPassEvent();

    public float tickDelta;

    public static LastPassEvent get(float tickDelta) {
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
