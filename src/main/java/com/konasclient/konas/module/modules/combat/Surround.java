package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.mixin.ClientPlayerEntityAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.Interactions;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.chat.Chat;
import com.konasclient.konas.util.client.ThreadUtils;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.entity.PlayerUtils;
import com.konasclient.konas.util.interaction.InteractionUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ConcurrentHashMap;

public class Surround extends Module {
    private static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private static final Setting<Boolean> swing = new Setting<>("Swing", true);
    private static final Setting<Integer> actionShift = new Setting<>("ActionShift", 3, 4, 1, 1);
    private static final Setting<Integer> tickDelay = new Setting<>("ActionInterval", 0, 5, 0, 1);

    private static final Setting<Boolean> predict = new Setting<>("Predict", true);

    private final Setting<Boolean> full = new Setting<>("Full", true);
    private Setting<Boolean> eChest = new Setting<>("EChests", false);

    private final Setting<Boolean> autoCenter = new Setting<>("Center", true);
    public static final Setting<Boolean> onlyWhileSneaking = new Setting<>("OnlyWhileSneaking", false);

    private static final Setting<Parent> triggers = new Setting<>("AutoDisable", new Parent(false));
    public static final Setting<Boolean> disableOnJump = new Setting<>("DisableOnJump", false).withParent(triggers);
    public static final Setting<Boolean> disableOnTP = new Setting<>("DisableOnTP", true).withParent(triggers);
    public static final Setting<Boolean> disableWhenDone = new Setting<>("DisableWhenDone", false).withParent(triggers);

    public Surround() {
        super("surround", "Places obsidian around you", 0xFFEB6834, Category.Combat);
    }

    private static final Vec3d[] STRICT = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1)
    };

    private static final Vec3d[] NORMAL = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1),
            new Vec3d(1, -1, 0),
            new Vec3d(0, -1, 1),
            new Vec3d(-1, -1, 0),
            new Vec3d(0, -1, -1),
            new Vec3d(0, -1, 0)
    };

    private int offsetStep = 0;
    private int delayStep = 0;

    private Timer inactivityTimer = new Timer();

    private ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }

        if (autoCenter.getValue()) {
            PlayerUtils.centerPlayerHorizontally();
        }
    }


    @Override
    public String getMetadata() {
        return full.getValue() ? "Full" : "Strict";
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (!ThreadUtils.canUpdate()) return;
        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 500) {
                renderPoses.remove(pos);
            } else {
                Interactions.renderPlace(pos);
            }
        });
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && disableOnTP.getValue()) {
            toggle();
        }
    }

    @EventHandler(priority = 70)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        if (disableOnJump.getValue() && mc.player.getY() > ((ClientPlayerEntityAccessor) mc.player).getLastY()) {
            toggle();
            return;
        }

        if (disableWhenDone.getValue() && inactivityTimer.hasPassed(650)) {
            toggle();
            return;
        }

        if (onlyWhileSneaking.getValue() && !mc.player.input.sneaking) return;

        if (delayStep < tickDelay.getValue()) {
            delayStep++;
            return;
        } else {
            delayStep = 0;
        }

        Vec3d[] offsetPattern = new Vec3d[0];
        int maxSteps = 0;

        if (full.getValue()) {
            offsetPattern = NORMAL;
            maxSteps = NORMAL.length;
        } else {
            offsetPattern = STRICT;
            maxSteps = STRICT.length;
        }

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            if (offsetStep >= maxSteps) {
                offsetStep = 0;
                break;
            }

            BlockPos offsetPos = new BlockPos(offsetPattern[offsetStep]);
            BlockPos targetPos = new BlockPos(mc.player.getPos()).add(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());

            int slot = getSlot();

            if (slot == -1) {
                toggle();
                Chat.warning(ModuleManager.MESSAGE_ID, "No Blocks Found, disabling surround!");
                return;
            }

            if (InteractionUtil.place(targetPos, rotate.getValue(), swing.getValue(), false, slot == -2 ? Hand.OFF_HAND : Hand.MAIN_HAND, slot) != null) {
                renderPoses.put(targetPos, System.currentTimeMillis());
                blocksPlaced++;
                inactivityTimer.reset();
                if (predict.getValue()) {
                    InteractionUtil.ghostBlocks.put(targetPos, System.currentTimeMillis());
                }
            }

            offsetStep++;
        }
    }

    private int getSlot() {
        int slot = -1;

        final ItemStack mainhandStack = mc.player.getMainHandStack();
        if (mainhandStack != ItemStack.EMPTY && mainhandStack.getItem() instanceof BlockItem) {
            final Block blockFromMainhandItem = ((BlockItem) mainhandStack.getItem()).getBlock();
            if (blockFromMainhandItem == Blocks.OBSIDIAN || (eChest.getValue() && blockFromMainhandItem == Blocks.ENDER_CHEST)) {
                slot = mc.player.inventory.selectedSlot;
            }
        }

        final ItemStack offhandStack = mc.player.getOffHandStack();
        if (offhandStack != ItemStack.EMPTY && offhandStack.getItem() instanceof BlockItem) {
            final Block blockFromOffhandItem = ((BlockItem) offhandStack.getItem()).getBlock();
            if (blockFromOffhandItem == Blocks.OBSIDIAN || (eChest.getValue() && blockFromOffhandItem == Blocks.ENDER_CHEST)) {
                slot = -2;
            }
        }

        if (slot == -1) {
            for (int i = 0; i < 9; i++) {
                final ItemStack stack = mc.player.inventory.getStack(i);
                if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                    final Block blockFromItem = ((BlockItem) stack.getItem()).getBlock();
                    if (blockFromItem == Blocks.OBSIDIAN || (eChest.getValue() && blockFromItem == Blocks.ENDER_CHEST)) {
                        slot = i;
                        break;
                    }
                }
            }
        }

        return slot;
    }
}
