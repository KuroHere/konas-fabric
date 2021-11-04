package com.konasclient.konas.event.events.player;

import com.konasclient.konas.event.Cancellable;

public class UpdateWalkingPlayerEvent extends Cancellable {
    private static final UpdateWalkingPlayerEvent INSTANCE = new UpdateWalkingPlayerEvent();

    public static UpdateWalkingPlayerEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
