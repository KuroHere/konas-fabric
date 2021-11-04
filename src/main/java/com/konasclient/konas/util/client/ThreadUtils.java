package com.konasclient.konas.util.client;

import com.konasclient.konas.Konas;

public class ThreadUtils {

    public static boolean canUpdate() {
        return Konas.mc != null && Konas.mc.world != null && Konas.mc.player != null;
    }

    public static boolean isThreadSafe() {
        return Konas.mc.isOnThread() || !Konas.isTickRunning;
    }

}