package com.konasclient.konas.event.events.client;

public class MouseButtonEvent {

    private static final MouseButtonEvent INSTANCE = new MouseButtonEvent();

    public int button, action;

    public static MouseButtonEvent get(int button, int action) {
        INSTANCE.button = button;
        INSTANCE.action = action;
        return INSTANCE;
    }

}