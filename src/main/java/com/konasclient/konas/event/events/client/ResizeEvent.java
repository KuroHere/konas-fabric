package com.konasclient.konas.event.events.client;

public class ResizeEvent {
    private static ResizeEvent INSTANCE = new ResizeEvent();

    public int width;
    public int height;

    public static ResizeEvent get(int width, int height) {
        INSTANCE.width = width;
        INSTANCE.height = height;
        return INSTANCE;
    }
}
