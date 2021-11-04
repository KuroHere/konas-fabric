package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.player.BlockReachDistanceEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;

public class Reach extends Module {

    private final Setting<Float> reach = new Setting<>("Reach", 4f, 10f, 0.5f, 0.5f);

    public Reach() {
        super("reach", "Increaces your block reach range", 0xFFDFD692, Category.Player);
    }

    @EventHandler
    public void onBlockReachDistance(BlockReachDistanceEvent event) {
        event.reachDistance = reach.getValue();
    }

}
