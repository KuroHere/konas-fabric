package com.konasclient.konas.event.events.render;

import net.minecraft.entity.Entity;

public class EntityScaleEvent {
    private static EntityScaleEvent INSTANCE = new EntityScaleEvent();

    public Entity entity;
    public float x;
    public float y;
    public float z;

    public static EntityScaleEvent get(Entity entity, float x, float y, float z) {
        INSTANCE.entity = entity;
        INSTANCE.x = x;
        INSTANCE.y = y;
        INSTANCE.z = z;
        return INSTANCE;
    }
}
