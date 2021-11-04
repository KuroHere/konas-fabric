package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.entity.effect.StatusEffect;

public class RenderStatusEffectBackgroundEvent extends Cancellable {
    private static RenderStatusEffectBackgroundEvent INSTANCE = new RenderStatusEffectBackgroundEvent();

    public StatusEffect effect;

    public static RenderStatusEffectBackgroundEvent get(StatusEffect effect) {
        INSTANCE.effect = effect;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
