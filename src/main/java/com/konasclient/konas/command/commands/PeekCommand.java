package com.konasclient.konas.command.commands;

import com.konasclient.konas.command.Command;
import com.konasclient.konas.util.PeekScreen;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.screen.ShulkerBoxScreenHandler;

import java.util.Arrays;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PeekCommand extends Command {

    private static final ItemStack[] ITEMS = new ItemStack[27];

    public PeekCommand() {
        super("peek", "Peek the Shulker that's currently in your hand.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Arrays.fill(ITEMS, ItemStack.EMPTY);

            if (!mc.player.getMainHandStack().isEmpty() && mc.player.getMainHandStack().getItem() == Items.SHULKER_BOX) {
                CompoundTag nbt = mc.player.getMainHandStack().getTag();

                if (nbt != null && nbt.contains("BlockEntityTag")) {
                    CompoundTag nbt2 = nbt.getCompound("BlockEntityTag");
                    if (nbt2.contains("Items")) {
                        ListTag nbt3 = (ListTag) nbt2.get("Items");
                        for (int i = 0; i < nbt3.size(); i++) {
                            ITEMS[nbt3.getCompound(i).getByte("Slot")] = ItemStack.fromTag(nbt3.getCompound(i));
                        }
                    }
                }
                mc.openScreen(new PeekScreen(new ShulkerBoxScreenHandler(0, mc.player.inventory, new SimpleInventory(ITEMS)), mc.player.inventory, mc.player.getMainHandStack().getName(), ((BlockItem) mc.player.getMainHandStack().getItem()).getBlock()));
            }
            return SINGLE_SUCCESS;
        });
    }
}
