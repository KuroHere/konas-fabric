package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.TimerManager;
import meteordevelopment.orbit.EventHandler;

public class Timer extends Module {

    public Setting<Float> timerSpeed = new Setting<>("TimerSpeed", 4f, 50f, 0.2f, 0.5f);
    public Setting<Boolean> Switch = new Setting<>("Switch", false);
    public Setting<Integer> activeTicks = new Setting<>("Active", 5, 20, 1, 1).withVisibility(Switch::getValue);
    public Setting<Integer> inactiveTicks = new Setting<>("Inactive", 5, 20, 1, 1).withVisibility(Switch::getValue);
    public Setting<Float> inactiveSpeed = new Setting<>("InactiveSpeed", 2f, 50f, 0.2f, 0.5f).withVisibility(Switch::getValue);

    public Timer() {
        super("timer", "Changes game tick length", 0xFFA73D6D, Category.Player);
    }

    private int counter = 0;

    @Override
    public String getMetadata() {
        return TimerManager.getTimerSpeed() + "";
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Pre event) {
        if (mc.world == null || mc.player == null) return;
        float speed = timerSpeed.getValue();
        if (Switch.getValue()) {
            if (counter > activeTicks.getValue() + inactiveTicks.getValue()) {
                counter = 0;
            } if (counter > activeTicks.getValue()) {
                speed = inactiveSpeed.getValue();
            }
        }
        TimerManager.updateTimer(this, 5, speed);
        counter++;
    }

    @Override
    public void onDisable() {
        TimerManager.resetTimer(this);
        counter = 0;
    }
}
