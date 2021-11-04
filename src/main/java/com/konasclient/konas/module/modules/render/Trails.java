package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.render.mesh.DrawMode;
import com.konasclient.konas.util.render.mesh.MeshBuilder;
import com.konasclient.konas.util.render.rendering.ModelRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;

public class Trails extends Module {

    private final ConcurrentHashMap<Integer, ThrownEntity> thrownEntities = new ConcurrentHashMap<>();

    private Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(Color.ORANGE.hashCode()));
    private Setting<Float> width = new Setting<>("Widthg", 1F, 5F, 1F, 0.1F);
    private Setting<Boolean> timeout = new Setting<>("Timeout", true);
    private Setting<Integer> timeoutSeconds = new Setting<>("Seconds", 10, 100, 0, 1).withVisibility(timeout::getValue);

    public Trails() {
        super("trails", "Draws trails behind throw entities", 0xFFCCDDCA, Category.Render);
    }

    private MeshBuilder meshBuilder = new MeshBuilder();

    @EventHandler
    public void onRender(RenderEvent event) {
        thrownEntities.forEach((id, thrownEntity) -> {
            meshBuilder.lineWidth = width.getValue();
            meshBuilder.begin(event, DrawMode.LineStrip, VertexFormats.POSITION_COLOR);
            for (Vec3d vertex : thrownEntity.getVertices()) {
                meshBuilder.pos(vertex.x, vertex.y, vertex.z).color(color.getValue().getRenderColor()).endVertex();
            }
            meshBuilder.end();
        });
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Post event) {
        mc.world.getEntities().forEach(entity -> {
            if (entity != mc.player) {
                if (entity.age > 1 && isProjectile(entity)) {
                    if (!thrownEntities.containsKey(entity.getEntityId())) {
                        final ArrayList<Vec3d> list = new ArrayList<>();

                        list.add(new Vec3d(entity.prevX, entity.prevY, entity.prevZ));

                        thrownEntities.put(entity.getEntityId(), new ThrownEntity(System.currentTimeMillis(), list));
                    } else {
                        thrownEntities.get(entity.getEntityId()).getVertices().add(new Vec3d(entity.prevX, entity.prevY, entity.prevZ));
                        thrownEntities.get(entity.getEntityId()).setTime(System.currentTimeMillis());
                    }
                }
            }
        });

        if(timeout.getValue()) {
            thrownEntities.forEach((id, thrownEntity) -> {
                if(System.currentTimeMillis() - thrownEntity.getTime() > 1000L * timeoutSeconds.getValue()) {
                    thrownEntities.remove(id);
                }
            });
        }
    }

    private boolean isProjectile(Entity entity) {
        if (entity instanceof SnowballEntity) return true;

        else if (entity instanceof ArrowEntity) return true;

        else if (entity instanceof EnderPearlEntity) return true;

        else if (entity instanceof EggEntity) return true;

        return false;
    }

    private static class ThrownEntity {

        private long time;
        private ArrayList<Vec3d> vertices;

        public ThrownEntity(long time, ArrayList<Vec3d> vertices) {
            this.time = time;
            this.vertices = vertices;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public ArrayList<Vec3d> getVertices() {
            return vertices;
        }

        public void setVertices(ArrayList<Vec3d> vertices) {
            this.vertices = vertices;
        }

    }
}
