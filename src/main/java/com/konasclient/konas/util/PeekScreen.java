package com.konasclient.konas.util;

import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.Tooltips;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

import static com.konasclient.konas.module.modules.render.Tooltips.hasItems;

public class PeekScreen extends ShulkerBoxScreen {
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/shulker_box.png");

    private static final ItemStack[] ITEMS = new ItemStack[27];

    private final Block block;

    public PeekScreen(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title, Block block) {
        super(handler, inventory, title);
        this.block = block;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.inventory.getCursorStack().isEmpty()) {
            Tooltips toolips = (Tooltips) ModuleManager.get(Tooltips.class);
            ItemStack itemStack = focusedSlot.getStack();

            if (hasItems(itemStack) && toolips.middleClickOpen.getValue()) {

                Arrays.fill(ITEMS, ItemStack.EMPTY);
                CompoundTag nbt = itemStack.getTag();

                if (nbt != null && nbt.contains("BlockEntityTag")) {
                    CompoundTag nbt2 = nbt.getCompound("BlockEntityTag");
                    if (nbt2.contains("Items")) {
                        ListTag nbt3 = (ListTag) nbt2.get("Items");
                        for (int i = 0; i < nbt3.size(); i++) {
                            ITEMS[nbt3.getCompound(i).getByte("Slot")] = ItemStack.fromTag(nbt3.getCompound(i));
                        }
                    }
                }

                client.openScreen(new PeekScreen(new ShulkerBoxScreenHandler(0, client.player.inventory, new SimpleInventory(ITEMS)), client.player.inventory, focusedSlot.getStack().getName(), ((BlockItem) focusedSlot.getStack().getItem()).getBlock()));
                return true;
            } else if (focusedSlot.getStack().getItem() == Items.ENDER_CHEST && toolips.previewEChest()) {
                for (int i = 0; i < EChestMemory.ITEMS.size(); i++) ITEMS[i] = EChestMemory.ITEMS.get(i);
                client.openScreen(new PeekScreen(new ShulkerBoxScreenHandler(0, client.player.inventory, new SimpleInventory(ITEMS)), client.player.inventory, focusedSlot.getStack().getName(), ((BlockItem) focusedSlot.getStack().getItem()).getBlock()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        if (block instanceof ShulkerBoxBlock) {
            float[] colors = ((ShulkerBoxBlock) block).getColor().getColorComponents();
            RenderSystem.color4f(colors[0], colors[1], colors[2], 1.0F);
        } else if (block instanceof EnderChestBlock) {
            RenderSystem.color4f(0F, 50F / 255F, 50F / 255F, 1.0F);
        } else {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        this.client.getTextureManager().bindTexture(TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}