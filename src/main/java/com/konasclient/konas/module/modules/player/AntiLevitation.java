package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;

public class AntiLevitation extends Module {
    private final Setting<Boolean> force = new Setting<>("Force", false);

    public AntiLevitation() {
        super("anti-levitation", "Removes levitation effect from the player", 0xFFCEFFFF, Category.Player);
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Pre event) {
        if (mc.player != null && force.getValue()) {
            if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
                mc.player.removeStatusEffect(StatusEffects.LEVITATION);
            }
        }
    }
}
