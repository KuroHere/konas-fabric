package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;

public class RenderNauseaWobbleEvent extends Cancellable {
    private static RenderNauseaWobbleEvent INSTANCE = new RenderNauseaWobbleEvent();

    public static RenderNauseaWobbleEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
