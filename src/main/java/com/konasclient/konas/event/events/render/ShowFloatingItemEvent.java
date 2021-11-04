package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.item.ItemStack;

public class ShowFloatingItemEvent extends Cancellable {
    private static ShowFloatingItemEvent INSTANCE = new ShowFloatingItemEvent();

    public ItemStack floatingItem;

    public static ShowFloatingItemEvent get(ItemStack floatingItem) {
        INSTANCE.floatingItem = floatingItem;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
