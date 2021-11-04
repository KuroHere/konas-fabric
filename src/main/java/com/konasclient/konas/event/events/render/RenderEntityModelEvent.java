package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.entity.Entity;

public class RenderEntityModelEvent extends Cancellable {
    private static RenderEntityModelEvent INSTANCE = new RenderEntityModelEvent();

    public Entity entity;
    public float red;
    public float green;
    public float blue;
    public float alpha;

    public static RenderEntityModelEvent get(Entity entity) {
        INSTANCE.entity = entity;
        INSTANCE.red = 1F;
        INSTANCE.green = 1F;
        INSTANCE.blue = 1F;
        INSTANCE.alpha = 1F;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
