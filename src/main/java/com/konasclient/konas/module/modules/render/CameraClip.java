package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.event.events.render.CameraClipEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;

public class CameraClip extends Module {
    private final Setting<Double> distance = new Setting<>("Distance", 4D, 10D, -10D, 0.5D);

    public CameraClip() {
        super("camera-clip", "Cancel camera clipping", 0xFF5C87C8, Category.Render);
    }

    @EventHandler
    public void onCameraClip(CameraClipEvent event) {
        event.distance = distance.getValue();
        event.cancel();
    }
}
