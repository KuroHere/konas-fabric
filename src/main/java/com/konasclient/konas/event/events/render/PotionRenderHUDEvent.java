package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;

public class PotionRenderHUDEvent extends Cancellable {
    private static final PotionRenderHUDEvent INSTANCE = new PotionRenderHUDEvent();

    public static PotionRenderHUDEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
