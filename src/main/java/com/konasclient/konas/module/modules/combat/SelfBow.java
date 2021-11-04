package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class SelfBow extends Module {
    public static final Setting<Boolean> speed = new Setting<>("Swiftness", true);
    public static final Setting<Boolean> strength = new Setting<>("Strength", true);
    public static final Setting<Boolean> toggle = new Setting<>("Toggle", true);
    public static final Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", true);
    public static final Setting<Boolean> ignoreEffects = new Setting<>("IgnoreEffects", false);
    public static final Setting<Boolean> rearrange = new Setting<>("Rearrange", false);
    public static final Setting<Boolean> noEatSwitch = new Setting<>("NoEatSwitch", false);
    public static final Setting<Integer> charge = new Setting<>("Charge", 7, 15, 1, 1);
    public static final Setting<Integer> health = new Setting<>("MinHealth", 5, 36, 1, 1);

    public SelfBow() {
        super("self-bow", "Shoots yourself", 0xFFEB6834, Category.Combat);
    }

    public void onDisable() {
        finishUsingBow(false);
    }

    @EventHandler(priority = 98)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() < health.getValue()) return;

        if (noEatSwitch.getValue() && mc.player.getActiveItem().getItem().isFood()) return;

        if (mc.player.getItemUseTime() >= charge.getValue()) finishUsingBow(true);

        if (ignoreEffects.getValue()) {
            if (strength.getValue()) run("effect.minecraft.strength");
            if (speed.getValue()) run("effect.minecraft.speed");
        } else {
            if (strength.getValue() && !mc.player.hasStatusEffect(StatusEffects.STRENGTH)) run("effect.minecraft.strength");
            if (speed.getValue() && !mc.player.hasStatusEffect(StatusEffects.SPEED)) run("effect.minecraft.speed");
        }
    }

    private void run(String type) {
        for (int i = 35; i >= 0; i--) {
            if (isFirstAmmoValid(type, i)) {
                if (rearrange.getValue()) rearrangeArrow(i, type);
                shootBow();
            }
        }
    }

    private void shootBow() {
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(0, -90, mc.player.isOnGround()));

        if (autoSwitch.getValue() && getBowSlot() != -1 && mc.player.inventory.selectedSlot != getBowSlot()) {
            mc.player.inventory.selectedSlot = getBowSlot();
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket());
        }

        if (mc.player.getItemUseTime() >= charge.getValue()) finishUsingBow(false);
        else mc.options.keyUse.setPressed(true);

    }

    private void finishUsingBow(boolean shouldToggle) {
        mc.options.keyUse.setPressed(false);
        if (toggle.getValue() && shouldToggle) toggle();
    }

    public int getBowSlot() {
        int bowSlot = -1;

        if (mc.player.getMainHandStack().getItem() == Items.BOW) bowSlot = mc.player.inventory.selectedSlot;

        if (bowSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStack(l).getItem() instanceof BowItem) {
                    bowSlot = l;
                    break;
                }
            }
        }

        return bowSlot;
    }

    private boolean isFirstAmmoValid(String type, int i) {
        if (mc.player.inventory.getStack(i).getItem() == Items.TIPPED_ARROW) {
            List<StatusEffectInstance> effectList = PotionUtil.getPotion(mc.player.inventory.getStack(i)).getEffects();
            if (effectList.size() > 0) {
                StatusEffectInstance effect = effectList.get(0);
                return effect.getTranslationKey().equals(type);
            }
        }
        return false;
    }

    private void rearrangeArrow(int fakeSlot, String type){
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = mc.player.inventory.getStack(i);
            if (itemStack.getItem() == Items.TIPPED_ARROW) {
                if (itemStack.getTranslationKey().equalsIgnoreCase(type)) {
                    mc.interactionManager.clickSlot(0, fakeSlot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, i, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(0, fakeSlot, 0, SlotActionType.PICKUP, mc.player);
                    return;
                }
            }
        }
    }
}