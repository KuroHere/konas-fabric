package com.konasclient.konas.module.modules.misc;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.event.events.player.ItemUseEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AutoEat extends Module {

    private final Setting<Float> health = new Setting<>("Health", 10f, 36f, 0f, 1f);
    private final Setting<Float> hunger = new Setting<>("Hunger", 15f, 20f, 0f, 1f);
    private final Setting<Integer> tickDelay = new Setting<>("TickDelay", 0, 10, 0, 1);
    private final Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", true);
    private final Setting<Boolean> switchBack = new Setting<>("SwitchBack", true).withVisibility(autoSwitch::getValue);
    private final Setting<Boolean> preferGaps = new Setting<>("PreferGaps", false);

    public AutoEat() {
        super("auto-eat", 0xFFEB6834, Category.Misc);
    }

    private int prevSlot, tickDelayLeft;
    private boolean isEating = false;

    public void onDisable() {
        if (isEating) stopEating();
        tickDelayLeft = 0;
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Post event) {
        if (mc.player.abilities.creativeMode) return;
        eat();
        tickDelayLeft--;
    }

    @EventHandler
    private void onUseItem(ItemUseEvent event) {
        if (isEating) {
            event.crosshairTarget = null;
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            tickDelayLeft = tickDelay.getValue();
        }
    }

    private void eat() {
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.getValue() || mc.player.getHungerManager().getFoodLevel() <= hunger.getValue()) {
            startEating();
        } else if (isEating) {
            stopEating();
        }
    }

    private void startEating() {
        // If the player's hunger level is 20
        if (mc.player.getHungerManager().getFoodLevel() == 20) {
            // If the item that is being held is eatable at 20 hunger and findFoodSlot is not equal to -1
            if (isAlwaysEatable()) {
                if (findFoodSlot() != -1 && getHand() == Hand.MAIN_HAND) switchSlot(findFoodSlot());
                rightClick();
            } else if (findFoodSlot() != -1 && getHand() == Hand.MAIN_HAND && isAlwaysEatable(mc.player.inventory.getStack(findFoodSlot()).getItem())) {
                switchSlot(findFoodSlot());
                rightClick();
            } else if (findFoodSlot() == -1 && !mc.player.getOffHandStack().getItem().isFood()
                    || findFoodSlot() != -1 && !isAlwaysEatable(mc.player.inventory.getStack(findFoodSlot()).getItem())
                    || findFoodSlot() == -1 && !isAlwaysEatable(mc.player.getOffHandStack().getItem())) {
                mc.options.keyUse.setPressed(false);
                isEating = false;
                stopEating();
            }
        } else {
            if (getHand() == Hand.MAIN_HAND) switchSlot(findFoodSlot());
            rightClick();
        }
    }

    private void rightClick() {
        if (tickDelayLeft <= 0) {
            tickDelayLeft = tickDelay.getValue();
            mc.options.keyUse.setPressed(true);
            isEating = true;
        }
    }

    private void stopEating() {
        isEating = false;
        mc.options.keyUse.setPressed(false);

        if (switchBack.getValue() && mc.player.inventory.selectedSlot != prevSlot) switchSlot(prevSlot);
    }

    private int findFoodSlot() {
        int foodSlot = -1;
        float bestHealAmount = 0F;

        for (int l = 0; l < 9; ++l) {
            ItemStack item = mc.player.inventory.getStack(l);

            if (item.getItem() instanceof EnchantedGoldenAppleItem || item.getItem().isFood()) {
                if (hunger.getValue() == 20) {
                    foodSlot = l;
                } else {
                    float healAmount = item.getItem().getFoodComponent().getHunger();

                    if (healAmount > bestHealAmount) {
                        bestHealAmount = healAmount;
                        foodSlot = l;
                    }
                }

                if (preferGaps.getValue() && (item.getItem() == Items.GOLDEN_APPLE || item.getItem() instanceof EnchantedGoldenAppleItem)) {
                    foodSlot = l;
                    break;
                }
            }

            if (getHand() == Hand.OFF_HAND) foodSlot = -1;
        }
        return foodSlot;
    }

    private void switchSlot(int slot) {
        if (mc.player.inventory.selectedSlot != slot && slot != -1) {
            prevSlot = mc.player.inventory.selectedSlot;
            mc.player.inventory.selectedSlot = slot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
    }

    private Hand getHand() {
        Hand hand = Hand.MAIN_HAND;
        if (mc.player.getOffHandStack().getItem().isFood() || mc.player.getOffHandStack().getItem() instanceof EnchantedGoldenAppleItem) hand = Hand.OFF_HAND;
        return hand;
    }

    private boolean isAlwaysEatable(Item item) {
        return item instanceof EnchantedGoldenAppleItem
                || item == Items.GOLDEN_APPLE
                || item == Items.CHORUS_FRUIT;
    }

    private boolean isAlwaysEatable() {
        return mc.player.getMainHandStack().getItem() instanceof EnchantedGoldenAppleItem
                || mc.player.getMainHandStack().getItem() == Items.GOLDEN_APPLE
                || mc.player.getMainHandStack().getItem() == Items.CHORUS_FRUIT
                || mc.player.getOffHandStack().getItem() instanceof EnchantedGoldenAppleItem
                || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE
                || mc.player.getOffHandStack().getItem() == Items.CHORUS_FRUIT;
    }
}
