package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;

public class CameraClipEvent extends Cancellable {
    private static CameraClipEvent INSTANCE = new CameraClipEvent();

    public double distance;

    public static CameraClipEvent get(double distance) {
        INSTANCE.distance = distance;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
