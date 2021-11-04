package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.render.rendering.ModelRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

import java.awt.*;
import java.util.ArrayList;

public class Breadcrumbs extends Module {
    public static ArrayList<Vec3d> vertices = new ArrayList<>();

    public static Setting<Boolean> onlyRender = new Setting<>("OnlyRender", false);
    private Setting<Integer> maxVertices = new Setting<>("MaxVertices", 50, 250, 25, 25);
    private Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(Color.RED.hashCode()));
    private Setting<Boolean> rainbow = new Setting<>("Rainbow", false).withVisibility(() -> color.getValue().isCycle());

    public Breadcrumbs() {
        super("breadcrumbs", "Draws a trial behind you", 0xFFF84A0E, Category.Render);
    }

    private DimensionType prevDimension = null;

    public void onEnable() {
        prevDimension = null;
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Pre event) {
        if (onlyRender.getValue()) return;
        if (prevDimension != null) {
            if (!mc.world.getDimension().equals(prevDimension)) {
                vertices.clear();
            }
        }
        prevDimension = mc.world.getDimension();
        if (mc.player.getX() != mc.player.prevX || mc.player.getY() != mc.player.prevY || mc.player.getZ() != mc.player.prevZ) {
            vertices.add(mc.player.getPos());
            if (vertices.size() >= maxVertices.getValue() * 10000) {
                vertices.remove(0);
                vertices.remove(1);
            }
        }
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        for (int i = 0; i < vertices.size() - 1; i++) {
            Vec3d min = vertices.get(i);
            Vec3d max = vertices.get(i + 1);
            int clr = color.getValue().getOffsetColor(rainbow.getValue() ? i * 10 : 0);
            ModelRenderer.LINES.line(min.x, min.y, min.z, max.x, max.y, max.z, new com.konasclient.konas.util.render.Color(clr));
        }
    }
}
