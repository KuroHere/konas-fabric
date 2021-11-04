package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;

public class RenderBossBarEvent extends Cancellable {
    private static RenderBossBarEvent INSTANCE = new RenderBossBarEvent();

    public static RenderBossBarEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
