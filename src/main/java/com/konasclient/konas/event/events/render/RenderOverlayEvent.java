package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;

public class RenderOverlayEvent extends Cancellable {
    private static RenderOverlayEvent INSTANCE = new RenderOverlayEvent();

    public Type type;

    public static RenderOverlayEvent get(Type type) {
        INSTANCE.type = type;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }

    public enum Type {
        Fire, Liquid, Pumpkin
    }
}
