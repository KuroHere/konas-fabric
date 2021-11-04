package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Random;

public class ChestStealer extends Module {

    private final Setting<Integer> delay = new Setting<>("Delay", 100, 1000, 1, 1);
    private final Setting<Boolean> random = new Setting<>("Random", false);

    public ChestStealer() {
        super("chest-stealer", "Automatically takes items out of chests", 0xFFEB6834, Category.Player);
    }

    private final com.konasclient.konas.util.client.Timer timer = new com.konasclient.konas.util.client.Timer();

    @EventHandler
    public void onUpdate(UpdateEvent.Post event) {
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            GenericContainerScreenHandler mxChest = (GenericContainerScreenHandler) mc.player.currentScreenHandler;
            for (int i = 0; i < mxChest.getInventory().size(); i++) {
                Slot slot = mxChest.getSlot(i);
                if (slot.hasStack()) {
                    Random random = new Random();
                    if (timer.hasPassed(delay.getValue() + (this.random.getValue() ? random.nextInt(delay.getValue()) : 0))) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                        timer.reset();
                    }
                }
            }
            if (isContainerEmpty(mxChest))
                mc.player.currentScreenHandler.close(mc.player);
        }
    }


    private boolean isContainerEmpty(GenericContainerScreenHandler container) {
        boolean empty = true;
        int i = 0;
        int slotAmount = container.getInventory().size() == 90 ? 54 : 27;
        while (i < slotAmount) {
            if (container.getSlot(i).hasStack()) {
                empty = false;
            }
            ++i;
        }
        return empty;
    }

}
