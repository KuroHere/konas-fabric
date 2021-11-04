package com.konasclient.konas.event.events.world;

import com.konasclient.konas.event.Cancellable;

public class RecalculateSkylightEvent extends Cancellable {
    private static RecalculateSkylightEvent INSTANCE = new RecalculateSkylightEvent();

    public static RecalculateSkylightEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
