package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;

public class HurtBobEvent extends Cancellable {
    private static HurtBobEvent INSTANCE = new HurtBobEvent();

    public static HurtBobEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
