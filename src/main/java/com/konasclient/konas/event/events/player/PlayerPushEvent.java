package com.konasclient.konas.event.events.player;

import net.minecraft.util.math.Vec3d;

public class PlayerPushEvent {
    private static final PlayerPushEvent INSTANCE = new PlayerPushEvent();

    public Vec3d movement;

    public static PlayerPushEvent get(Vec3d movement) {
        INSTANCE.movement = movement;
        return INSTANCE;
    }
}