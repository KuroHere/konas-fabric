package com.konasclient.konas.event.events.player;

import com.konasclient.konas.event.Cancellable;

public class PushOutOfBlocksEvent extends Cancellable {
    private static final PushOutOfBlocksEvent INSTANCE = new PushOutOfBlocksEvent();

    public static PushOutOfBlocksEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
