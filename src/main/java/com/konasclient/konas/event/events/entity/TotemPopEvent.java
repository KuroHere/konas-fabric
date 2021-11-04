package com.konasclient.konas.event.events.entity;

import net.minecraft.entity.Entity;

public class TotemPopEvent {
    public final Entity player;
    public final int pops;

    public TotemPopEvent(Entity player, int pops) {
        this.player = player;
        this.pops = pops;
    }
}
