package com.konasclient.konas.event.events.player;

import com.konasclient.konas.event.Cancellable;

public class ItemSlowdownEvent extends Cancellable {
    private static final ItemSlowdownEvent INSTANCE = new ItemSlowdownEvent();

    public static ItemSlowdownEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
