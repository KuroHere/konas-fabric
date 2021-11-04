package com.konasclient.konas.event.events.player;

import com.konasclient.konas.event.Cancellable;

public class ClipAtLedgeEvent extends Cancellable {
    private static ClipAtLedgeEvent INSTANCE = new ClipAtLedgeEvent();

    public static ClipAtLedgeEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
