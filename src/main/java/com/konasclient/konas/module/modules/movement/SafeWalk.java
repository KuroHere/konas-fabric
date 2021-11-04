package com.konasclient.konas.module.modules.movement;

import baritone.api.BaritoneAPI;
import com.konasclient.konas.event.events.player.ClipAtLedgeEvent;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import meteordevelopment.orbit.EventHandler;

public class SafeWalk extends Module {
    public SafeWalk() {
        super("safe-walk", "Clip on ledges", 0xFFD9D753, Category.Movement);
    }

    public void onDisable() {
        if (BaritoneAPI.getSettings().assumeSafeWalk != null) {
            BaritoneAPI.getSettings().assumeSafeWalk.value = false;
        }
    }

    @EventHandler
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        BaritoneAPI.getSettings().assumeSafeWalk.value = true;
    }

    @EventHandler
    public void onClipAtLedge(ClipAtLedgeEvent event) {
        event.cancel();
    }
}
