package com.konasclient.konas.event.events.player;

import net.minecraft.util.hit.HitResult;

public class ItemUseEvent {
    private static final ItemUseEvent INSTANCE = new ItemUseEvent();

    public HitResult crosshairTarget;

    public static ItemUseEvent get(HitResult target) {
        INSTANCE.crosshairTarget = target;
        return INSTANCE;
    }
}