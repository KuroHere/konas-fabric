package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;


public class Tooltips extends Module {

    public final Setting<Boolean> middleClickOpen = new Setting<>("MiddleClickOpen", true);
    public final Setting<Boolean> storage = new Setting<>("Storage", true);
    public final Setting<Boolean> echest = new Setting<>("EChest", true);
    public final Setting<Boolean> maps = new Setting<>("Maps", true);

    public Tooltips() {
        super("tooltips", "Displays useful tooltips when hovering certain items.", 0xFFD0CE3B, Category.Render);
    }

    public boolean previewStorage() {
        return isActive() && storage.getValue();
    }

    public boolean previewEChest() {
        return isActive() && echest.getValue();
    }

    public boolean previewMaps() {
        return isActive() && maps.getValue();
    }

    public static boolean hasItems(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getSubTag("BlockEntityTag");
        return compoundTag != null && compoundTag.contains("Items", 9);
    }

}
