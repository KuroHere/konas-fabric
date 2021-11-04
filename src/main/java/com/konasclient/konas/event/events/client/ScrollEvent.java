package com.konasclient.konas.event.events.client;

public class ScrollEvent {
    private static final ScrollEvent INSTANCE = new ScrollEvent();

    public double scroll;

    public static ScrollEvent get(double scroll) {
        INSTANCE.scroll = scroll;
        return INSTANCE;
    }
}
