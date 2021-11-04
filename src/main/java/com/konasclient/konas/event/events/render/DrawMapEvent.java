package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;

public class DrawMapEvent extends Cancellable {
    private static DrawMapEvent INSTANCE = new DrawMapEvent();

    public static DrawMapEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
