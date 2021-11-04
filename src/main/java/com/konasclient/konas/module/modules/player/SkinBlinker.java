package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.entity.PlayerModelPart;

public class SkinBlinker extends Module {
    private static final Setting<Float> delay = new Setting<>("Delay", 0.0F, 20.0F, 0.0F, 0.1F);
    private static final Setting<Boolean> random = new Setting<>("Random", true);

    private com.konasclient.konas.util.client.Timer timer = new com.konasclient.konas.util.client.Timer();

    public SkinBlinker() {
        super("skin-blinker", "Flashes your skin parts", 0xFFEB6834, Category.Player);
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (timer.hasPassed(delay.getValue() * 1000.0f)) {
            PlayerModelPart[] parts = PlayerModelPart.values();
            int i = 0;
            while (i < parts.length) {
                PlayerModelPart enumPlayerModelParts = parts[i];
                mc.options.setPlayerModelPart(enumPlayerModelParts, random.getValue() ? Math.random() < 0.5 : !mc.options.getEnabledPlayerModelParts().contains(enumPlayerModelParts));
                ++i;
            }
            timer.reset();
        }
    }
}
