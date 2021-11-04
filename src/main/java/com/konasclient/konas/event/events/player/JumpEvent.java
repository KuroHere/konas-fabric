package com.konasclient.konas.event.events.player;

import com.konasclient.konas.event.Cancellable;

public class JumpEvent extends Cancellable {
    private static final JumpEvent INSTANCE = new JumpEvent();

    public float yaw;

    public static JumpEvent get(float yaw) {
        INSTANCE.yaw = yaw;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

}

