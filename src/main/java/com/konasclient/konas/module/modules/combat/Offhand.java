package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.ThreadUtils;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.math.DamageCalculator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class Offhand extends Module {
    private final Setting<Boolean> force = new Setting<>("Force", true);
    private final Setting<Float> health = new Setting<>("Health", 12F, 20F, 1F, 0.5F);
    private final Setting<Float> delay = new Setting<>("Delay", 0F, 5F, 0F, 0.05F);
    private final Setting<Action> action = new Setting<>("Action", Action.Integration);
    private final Setting<Boolean> swapBack = new Setting<>("SwapBack", false).withVisibility(() -> action.getValue() == Action.Integration);
    private final Setting<Safety> safety = new Setting("Safety", Safety.Lethal);
    private final Setting<Boolean> cancelMotion = new Setting<>("CancelMotion", false);

    private enum Action {
        None,
        Totem,
        GApple,
        Crystal,
        Integration
    }

    private enum Safety {
        None,
        Lethal,
        Health
    }

    public Offhand() {
        super("offhand", "Automatically manages your offhand", 0xFFDB4747, Category.Combat);
    }

    private Timer timer = new Timer();
    private Item itemTarget = null;
    private boolean hasTotem = false;
    private boolean rightClick = false;
    private int swapBackSlot = -1;

    public void onEnable() {
        itemTarget = null;
        hasTotem = false;
        rightClick = false;
        swapBackSlot = -1;
    }

    @Override
    public String getMetadata() {
        return mc.player.getOffHandStack() == null ? "" : mc.player.getOffHandStack().getName().getString();
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (!ThreadUtils.canUpdate()) return;

        if (mc.player.playerScreenHandler != mc.player.currentScreenHandler || mc.currentScreen instanceof AbstractInventoryScreen || mc.player.isCreative())
            return;

        if (!hasTotem) {
            itemTarget = getItemTarget();
            if (itemTarget == mc.player.getOffHandStack().getItem()) {
                itemTarget = null;
            }
        }

        if (itemTarget == null) {
            if (swapBackSlot != -1 && mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE && !mc.options.keyUse.isPressed()) {
                if (cancelMotion.getValue() && mc.player.getVelocity().length() >= 9.0E-4D) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
                }
                mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, swapBackSlot, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.inventory.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);
                }
                swapBackSlot = -1;
            }
            return;
        }

        if (timer.hasPassed(delay.getValue() * 100F) && mc.player.inventory.getCursorStack().getItem() != itemTarget) {
            int index = 44;
            while (index >= 9) {
                if (mc.player.inventory.getStack(index >= 36 ? index - 36 : index).getItem() == itemTarget) {
                    hasTotem = true;
                    if (cancelMotion.getValue() && mc.player.getVelocity().length() >= 9.0E-4D) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
                    }
                    mc.interactionManager.clickSlot(0, index, 0, SlotActionType.PICKUP, mc.player);
                    if (rightClick) {
                        rightClick = false;
                        swapBackSlot = index;
                    } else {
                        swapBackSlot = -1;
                    }
            }
            index--;
        }
    }

        if (timer.hasPassed(delay.getValue() * 200F) && mc.player.inventory.getCursorStack().getItem() == itemTarget) {
            if (cancelMotion.getValue() && mc.player.getVelocity().length() >= 9.0E-4D) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
            }
            mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);
            if (mc.player.inventory.getCursorStack().isEmpty()) {
                hasTotem = false;
                return;
            }
        }

        if (timer.hasPassed(delay.getValue() * 300F) && !mc.player.inventory.getCursorStack().isEmpty() && mc.player.getOffHandStack().getItem() == itemTarget) {
            int index = 44;
            while (index >= 9) {
                if (mc.player.inventory.getStack(index >= 36 ? index - 36 : index).isEmpty()) {
                    if (timer.hasPassed(delay.getValue() * 1000F) && mc.player.inventory.getCursorStack().getItem() != itemTarget) {
                        if (cancelMotion.getValue() && mc.player.getVelocity().length() >= 9.0E-4D) {
                            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
                        }
                        mc.interactionManager.clickSlot(0, index, 0, SlotActionType.PICKUP, mc.player);
                        hasTotem = false;
                        if (rightClick) {
                            rightClick = false;
                            swapBackSlot = index;
                        } else {
                            swapBackSlot = -1;
                        }
                    }
                }
                index--;
            }
        }
    }

    private Item getItemTarget() {
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.getValue()) {
            return Items.TOTEM_OF_UNDYING;
        }

        if (safety.getValue() != Safety.None) {
            if (mc.player.fallDistance > 8F) return Items.TOTEM_OF_UNDYING;
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof EndCrystalEntity && entity.distanceTo(mc.player) < 6F) {
                    if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) - DamageCalculator.getExplosionDamage((EndCrystalEntity) entity, mc.player) <= ((safety.getValue() == Safety.Lethal) ? 1 : health.getValue())) {
                        return Items.TOTEM_OF_UNDYING;
                    }
                }
            }
        }

        if (action.getValue() == Action.Totem) return Items.TOTEM_OF_UNDYING;
        else if (action.getValue() == Action.GApple) return Items.ENCHANTED_GOLDEN_APPLE;
        else if (action.getValue() == Action.Crystal) return Items.END_CRYSTAL;

        if (action.getValue() == Action.Integration) {
            if (mc.player.isFallFlying()) {
                return Items.TOTEM_OF_UNDYING;
            } else if (mc.player.getMainHandStack().getItem() instanceof SwordItem && mc.options.keyUse.isPressed()) {
                if (swapBack.getValue()) {
                    rightClick = true;
                }
                return Items.ENCHANTED_GOLDEN_APPLE;
            } else if (ModuleManager.get(AutoCrystal.class).isActive()) {
                return Items.END_CRYSTAL;
            }
            if (force.getValue()) {
                return Items.TOTEM_OF_UNDYING;
            }
            return null;
        }

        if (force.getValue()) {
            return Items.TOTEM_OF_UNDYING;
        }
        return null;
    }
}
