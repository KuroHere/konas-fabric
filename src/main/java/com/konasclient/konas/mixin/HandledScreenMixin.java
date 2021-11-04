package com.konasclient.konas.mixin;

import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.Tooltips;
import com.konasclient.konas.util.EChestMemory;
import com.konasclient.konas.util.PeekScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;

import static com.konasclient.konas.Konas.mc;
import static com.konasclient.konas.module.modules.render.Tooltips.hasItems;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    @Shadow @Nullable protected Slot focusedSlot;
    @Shadow protected int x;
    @Shadow protected int y;

    private static final Identifier CONTAINER_BACKGROUND = new Identifier("textures/container.png");
    private static final Identifier MAP_BACKGROUND = new Identifier("textures/map/map_background.png");
    private static final ItemStack[] ITEMS = new ItemStack[27];

    public HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
        if (focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.inventory.getCursorStack().isEmpty()) {
            Tooltips toolips = (Tooltips) ModuleManager.get(Tooltips.class);

            if (hasItems(focusedSlot.getStack()) && toolips.previewStorage()) {
                CompoundTag compoundTag = focusedSlot.getStack().getSubTag("BlockEntityTag");
                DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
                Inventories.fromTag(compoundTag, itemStacks);
                float[] colors = new float[]{1F, 1F, 1F};
                Item focusedItem = focusedSlot.getStack().getItem();
                if (focusedItem instanceof BlockItem && ((BlockItem) focusedItem).getBlock() instanceof ShulkerBoxBlock) {
                    try {
                        colors = Objects.requireNonNull(ShulkerBoxBlock.getColor(focusedSlot.getStack().getItem())).getColorComponents();
                    } catch (NullPointerException npe) {
                        colors = new float[]{1F, 1F, 1F};
                    }
                }
                draw(matrices, itemStacks, mouseX, mouseY, colors);
            }
            else if (focusedSlot.getStack().getItem() == Items.ENDER_CHEST && toolips.previewEChest()) {
                draw(matrices, EChestMemory.ITEMS, mouseX, mouseY, new float[]{0F, 50F / 255F, 50F / 255F});
            }
            else if (focusedSlot.getStack().getItem() == Items.FILLED_MAP && toolips.maps.getValue()) {
                drawMapPreview(matrices, focusedSlot.getStack(), mouseX, mouseY);
            }
        }
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawMouseoverTooltip(MatrixStack matrices, int x, int y, CallbackInfo info) {
        if (focusedSlot != null && !focusedSlot.getStack().isEmpty() && client.player.inventory.getCursorStack().isEmpty()) {
            if (focusedSlot.getStack().getItem() == Items.FILLED_MAP && ((Tooltips) ModuleManager.get(Tooltips.class)).previewMaps()) info.cancel();
        }
    }

    private void draw(MatrixStack matrices, DefaultedList<ItemStack> itemStacks, int mouseX, int mouseY, float[] colors) {
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        mouseX += 8;
        mouseY -= 12;

        drawBackground(matrices, mouseX, mouseY, colors);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        DiffuseLighting.enable();

        int row = 0;
        int i = 0;
        for (ItemStack itemStack : itemStacks) {
            mc.getItemRenderer().renderGuiItemIcon(itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
            mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18, null);

            i++;
            if (i >= 9) {
                i = 0;
                row++;
            }
        }

        DiffuseLighting.disable();
        RenderSystem.enableDepthTest();
    }

    private void drawBackground(MatrixStack matrices, int x, int y, float[] colors) {
        RenderSystem.color4f(colors[0], colors[1], colors[2], 1F);

        client.getTextureManager().bindTexture(CONTAINER_BACKGROUND);
        DrawableHelper.drawTexture(matrices, x, y, 0, 0, 0, 176, 67, 67, 176);
    }

    private void drawMapPreview(MatrixStack matrices, ItemStack stack, int x, int y) {
        GL11.glEnable(GL11.GL_BLEND);
        RenderSystem.pushMatrix();
        RenderSystem.disableLighting();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        int y1 = y - 12;
        int y2 = y1 + 100;
        int x1 = x + 8;
        int x2 = x1 + 100;
        int z = 300;

        client.getTextureManager().bindTexture(MAP_BACKGROUND);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(x1, y2, z).texture(0.0f, 1.0f).next();
        buffer.vertex(x2, y2, z).texture(1.0f, 1.0f).next();
        buffer.vertex(x2, y1, z).texture(1.0f, 0.0f).next();
        buffer.vertex(x1, y1, z).texture(0.0f, 0.0f).next();
        tessellator.draw();

        MapState mapState = FilledMapItem.getOrCreateMapState(stack, client.world);

        if (mapState != null) {
            mapState.getPlayerSyncData(client.player);

            x1 += 8;
            y1 += 8;
            z = 310;
            double scale = (double) (100 - 16) / 128.0D;

            RenderSystem.translatef(x1, y1, z);
            RenderSystem.scaled(scale, scale, 0);
            VertexConsumerProvider.Immediate consumer = client.getBufferBuilders().getEntityVertexConsumers();
            client.gameRenderer.getMapRenderer().draw(matrices, consumer, mapState, false, 0xF000F0);
        }

        RenderSystem.enableLighting();
        RenderSystem.popMatrix();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
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
                cir.setReturnValue(true);
            } else if (focusedSlot.getStack().getItem() == Items.ENDER_CHEST && toolips.previewEChest()) {
                for (int i = 0; i < EChestMemory.ITEMS.size(); i++) ITEMS[i] = EChestMemory.ITEMS.get(i);
                client.openScreen(new PeekScreen(new ShulkerBoxScreenHandler(0, client.player.inventory, new SimpleInventory(ITEMS)), client.player.inventory, focusedSlot.getStack().getName(), ((BlockItem) focusedSlot.getStack().getItem()).getBlock()));
                cir.setReturnValue(true);
            }
        }
    }

}

