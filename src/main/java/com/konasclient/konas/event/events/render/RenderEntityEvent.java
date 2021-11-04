package com.konasclient.konas.event.events.render;

import net.minecraft.entity.Entity;

public class RenderEntityEvent {
    public Entity entity;

    public static class Pre extends RenderEntityEvent {
        private static RenderEntityEvent.Pre INSTANCE = new RenderEntityEvent.Pre();

        public static RenderEntityEvent.Pre get(Entity entity) {
            INSTANCE.entity = entity;
            return INSTANCE;
        }
    }

    public static class Post extends RenderEntityEvent {
        private static RenderEntityEvent.Post INSTANCE = new RenderEntityEvent.Post();

        public static RenderEntityEvent.Post get(Entity entity) {
            INSTANCE.entity = entity;
            return INSTANCE;
        }
    }
}
