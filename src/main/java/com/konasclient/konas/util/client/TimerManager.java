package com.konasclient.konas.util.client;

import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.movement.IceSpeed;

public class TimerManager {
    private static Module currentModule;
    private static int priority;
    private static float timerSpeed;
    private static boolean active = false;

    public static void updateTimer(Module module, int priority, float timerSpeed) {
        if (module == currentModule) {
            TimerManager.priority = priority;
            TimerManager.timerSpeed = timerSpeed;
            TimerManager.active = true;
        } else if (priority > TimerManager.priority || !TimerManager.active) {
            TimerManager.currentModule = module;
            TimerManager.priority = priority;
            TimerManager.timerSpeed = timerSpeed;
            TimerManager.active = true;
        }
    }

    public static void resetTimer(Module module) {
        if (TimerManager.currentModule == module) {
            active = false;
        }
    }

    public static float getTimerSpeed() {
        if (!ThreadUtils.canUpdate()) active = false;
        return active ? timerSpeed : 1F;
    }
}
