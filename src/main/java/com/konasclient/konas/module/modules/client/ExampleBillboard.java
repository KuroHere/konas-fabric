package com.konasclient.konas.module.modules.client;

import com.konasclient.konas.event.events.render.RenderHudEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.util.math.InterpolationUtil;
import com.konasclient.konas.util.math.MatrixUtil;
import com.konasclient.konas.util.math.Vec2d;
import com.konasclient.konas.util.render.Color;
import com.konasclient.konas.util.render.mesh.DrawMode;
import com.konasclient.konas.util.render.rendering.ModelRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class ExampleBillboard extends Module {
    public ExampleBillboard() {
        super("ExampleBillboard", 0xFF42f5dd, Category.Client);
    }

    @EventHandler(priority = 40)
    public void onRenderHUD(RenderHudEvent event) {
        ModelRenderer.NORMAL.begin(null, DrawMode.Triangles, VertexFormats.POSITION_COLOR);
        for (PlayerEntity pe : mc.world.getPlayers()) {
            if (pe == mc.player) continue;
            Vec2d playerPos = MatrixUtil.getBillboardPos(InterpolationUtil.lerpEntity(pe).add(0, pe.getHeight(), 0)).getProjectedPos();
            if (playerPos != null) {
                System.out.println(playerPos);
                ModelRenderer.NORMAL.quad(playerPos.x - 5, playerPos.y - 5, 10F, 10F, new Color(255, 255, 255, 255));
            }
        }
        ModelRenderer.NORMAL.end();
    }
}
