package com.konasclient.konas.event.events.client;

public class LoadResourcePackEvent {
    private static LoadResourcePackEvent INSTANCE = new LoadResourcePackEvent();

    public static LoadResourcePackEvent get() {
        return INSTANCE;
    }
}
