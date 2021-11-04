package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.ThreadUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class FakePlayer extends Module {
    private static Setting<Boolean> copyInventory = new Setting<>("CopyInventory", false);

    public FakePlayer() {
        super("fake-player", "Spawns a fake player", 0xFF69B757, Category.Player);
    }

    public void onEnable() {
        if (!ThreadUtils.canUpdate()) {
            toggle();
            return;
        }

        OtherClientPlayerEntity  fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("66666666-6666-6666-6666-666666666600"), "soulbond"));
        fakePlayer.copyPositionAndRotation(mc.player);

        if (copyInventory.getValue()) {
            fakePlayer.inventory.armor.clear();
            for (ItemStack itemStack : mc.player.inventory.armor) {
                fakePlayer.inventory.armor.add(itemStack);
            }
            fakePlayer.inventory.main.clear();
            for (ItemStack itemStack : mc.player.inventory.main) {
                fakePlayer.inventory.main.add(itemStack);
            }
            fakePlayer.inventory.selectedSlot = mc.player.inventory.selectedSlot;
            fakePlayer.inventory.setStack(mc.player.inventory.selectedSlot, mc.player.getMainHandStack());
            fakePlayer.inventory.setStack(45, mc.player.getOffHandStack());
        }

        mc.world.addPlayer(-101, fakePlayer);
    }

    public void onDisable() {
        if (!ThreadUtils.canUpdate()) {
            return;
        }

        mc.world.removeEntity(-101);
    }
}
