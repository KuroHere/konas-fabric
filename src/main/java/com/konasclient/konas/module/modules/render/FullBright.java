package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.mixin.DimensionTypeAccesor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ListenableSettingDecorator;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.ThreadUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;

public class FullBright extends Module {
    public ListenableSettingDecorator<Float> factor = new ListenableSettingDecorator<>("Factor", 1F, 1F, 0.1F, 0.05F, (value) -> {
        fullBrightBrightnessTable = generateBrightnessTable(value);
    });
    public static Setting<Boolean> gamma = new Setting<>("Gamma", false);

    public FullBright() {
        super("full-bright", "Makes your world brighter!", 0xFF96AAD8, Category.Render);
    }

    private float[] fullBrightBrightnessTable = generateBrightnessTable(factor.getValue());

    public void onDisable() {
        if (mc.world != null && mc.world.getDimension() != null) {
            ((DimensionTypeAccesor) mc.world.getDimension()).setfield_24767(generateBrightnessTable(((DimensionTypeAccesor) mc.world.getDimension()).getAmbientLight()));
        }
    }

    @EventHandler
    public void onRender(RenderEvent evgent) {
        if (!ThreadUtils.canUpdate()) return;
        ((DimensionTypeAccesor) mc.world.getDimension()).setfield_24767(fullBrightBrightnessTable);
    }

    private static float[] generateBrightnessTable(float f) {
        float[] fs = new float[16];

        for(int i = 0; i <= 15; ++i) {
            float g = (float)i / 15.0F;
            float h = g / (4.0F - 3.0F * g);
            fs[i] = MathHelper.lerp(f, h, 1.0F);
        }

        return fs;
    }
}
