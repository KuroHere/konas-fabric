package com.konasclient.konas.event.events.sound;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.client.sound.SoundInstance;

public class SoundEvent extends Cancellable {
    private static SoundEvent INSTANCE = new SoundEvent();

    public SoundInstance instance;

    public static SoundEvent get(SoundInstance instance) {
        INSTANCE.instance = instance;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
