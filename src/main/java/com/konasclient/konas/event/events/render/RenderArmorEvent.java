package com.konasclient.konas.event.events.render;

import com.konasclient.konas.event.Cancellable;
import net.minecraft.entity.EquipmentSlot;

public class RenderArmorEvent extends Cancellable {
    private static RenderArmorEvent INSTANCE = new RenderArmorEvent();

    public EquipmentSlot equipmentSlot;

    public static RenderArmorEvent get(EquipmentSlot equipmentSlot) {
        INSTANCE.equipmentSlot = equipmentSlot;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
