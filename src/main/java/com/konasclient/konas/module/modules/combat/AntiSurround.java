package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.action.Action;
import com.konasclient.konas.util.action.ActionManager;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.combat.VulnerabilityUtil;
import com.konasclient.konas.util.friend.Friends;
import com.konasclient.konas.util.interaction.LookCalculator;
import com.konasclient.konas.util.render.rendering.ModelRenderer;
import com.konasclient.konas.util.render.rendering.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.konasclient.konas.util.interaction.InteractionUtil.checkAxis;

public class AntiSurround extends Module {
    private Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private Setting<Boolean> swing = new Setting<>("Swing", true);
    private Setting<Float> range = new Setting<>("Range", 4F, 6F, 1F, 0.1F);
    private Setting<Float> delay = new Setting<>("Delay", 2F, 10F, 0.1F, 0.1F);
    private Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);
    private Setting<SwapMode> swap = new Setting<>("Swap", SwapMode.Normal);
    private Setting<Boolean> instant = new Setting<>("Instant", false);
    private Setting<Boolean> limit = new Setting<>("Limit", false).withVisibility(instant::getValue);

    private Setting<Parent> render = new Setting<>("Render", new Parent(false));
    private Setting<Boolean> showMining = new Setting<>("ShowMining", true).withParent(render);
    private Setting<ColorSetting> miningColor = new Setting<>("Mining", new ColorSetting(0x55FF0000)).withParent(render);
    private Setting<ColorSetting> miningLineColor = new Setting<>("MiningOutline", new ColorSetting(Color.RED.hashCode())).withParent(render);
    private Setting<ColorSetting> readyColor = new Setting<>("Ready", new ColorSetting(0x5500FF00)).withParent(render);
    private Setting<ColorSetting> readyLineColor = new Setting<>("ReadyOutline", new ColorSetting(Color.GREEN.hashCode())).withParent(render);

    private enum SwapMode {
        Off, Normal, Silent
    }

    public AntiSurround() {
        super("anti-surround", "Mines enemies surrounds", 0xFFF57C68, Category.Combat);
    }

    private Timer silentTimer = new Timer();

    private BlockPos prevPos;

    private BlockPos currentPos;
    private Direction currentFacing;

    private float curBlockDamage;
    private Timer mineTimer = new Timer();
    private boolean stopped;

    private Timer delayTimer = new Timer();

    private int priorSlot = -1;

    public void onEnable() {
        prevPos = null;
        currentPos = null;
        currentFacing = null;
        curBlockDamage = 0F;
        stopped = false;
        priorSlot = -1;
    }

    @EventHandler
    public void onPlayerUpdate(UpdateEvent.Pre event) {
        if (currentPos != null) {
            if (curBlockDamage >= 1F) {
                if (stopped) {
                    if (mineTimer.hasPassed(1500)) {
                        currentPos = null;
                        currentFacing = null;
                    }
                } else {
                    stopped = true;
                    if (swap.getValue() != SwapMode.Off) {
                        int bestSlot = findBestTool(currentPos);
                        if (bestSlot != -1 && bestSlot != mc.player.inventory.selectedSlot) {
                            if (swap.getValue() == SwapMode.Silent) {
                                priorSlot = mc.player.inventory.selectedSlot;
                                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(bestSlot));
                                silentTimer.reset();
                            } else {
                                mc.player.inventory.selectedSlot = bestSlot;
                                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(bestSlot));
                            }
                        }
                    }
                }
            }
        }
        if (currentPos != null && curBlockDamage < 1F) {
            BlockState iblockstate = mc.world.getBlockState(currentPos);

            if (iblockstate.getMaterial() == Material.AIR) {
                prevPos = currentPos;
                currentPos = null;
                return;
            }

            int bestSlot = findBestTool(currentPos);
            if (bestSlot == -1) bestSlot = mc.player.inventory.selectedSlot;
            int prevItem = mc.player.inventory.selectedSlot;
            mc.player.inventory.selectedSlot = bestSlot;
            curBlockDamage += iblockstate.calcBlockBreakingDelta(mc.player, mc.player.world, currentPos);
            mc.player.inventory.selectedSlot = prevItem;
            mineTimer.reset();
        }
    }

    @EventHandler
    public void onUpdatePost(UpdateEvent.Post event) {
        if (priorSlot != -1 && silentTimer.hasPassed(350)) {
            mc.player.inventory.selectedSlot = priorSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(priorSlot));
            priorSlot = -1;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockUpdateS2CPacket && currentPos != null) {
            if (((BlockUpdateS2CPacket) event.packet).getPos().equals(currentPos) && ((BlockUpdateS2CPacket) event.packet).getState().getBlock() instanceof AirBlock) {
                prevPos = currentPos;
                currentPos = null;
                currentFacing = null;
            }
        }
    }

    @EventHandler(priority = 90)
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent event) {
        if (currentPos == null && delayTimer.hasPassed(delay.getValue() * 1000)) {
            PlayerEntity target = getNearestTarget();

            if (target != null) {
                ArrayList<BlockPos> vulnerablePos = VulnerabilityUtil.getVulnerablePositions(target.getBlockPos());
                BlockPos bestPos = vulnerablePos.stream().min(Comparator.comparing(pos -> mc.player.getBlockPos().getSquaredDistance(pos))).orElse(null);
                if (bestPos != null) {
                    Direction bestFacing = getFacing(bestPos, strictDirection.getValue());
                    if (bestFacing != null) {

                        currentPos = bestPos;
                        currentFacing = bestFacing;
                        curBlockDamage = 0F;
                        stopped = false;
                        delayTimer.reset();

                        Vec3d hitVec = new Vec3d(currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5)
                                .add(new Vec3d(currentFacing.getUnitVector()).multiply(0.5));
                        float[] rots = LookCalculator.calculateAngle(hitVec);

                        if (instant.getValue() && currentPos.equals(prevPos)) {
                            curBlockDamage = 1F;
                            mineTimer.reset();
                            ActionManager.forceAdd(new Action(rots[0], rots[1], () -> {
                                if (limit.getValue()) {
                                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentPos, currentFacing.getOpposite()));
                                }
                                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentPos, currentFacing));
                                if (swing.getValue()) {
                                    mc.player.swingHand(Hand.MAIN_HAND);
                                }}, rotate.getValue(), false));
                            } else {
                            ActionManager.forceAdd(new Action(rots[0], rots[1], () -> {
                                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, currentPos, currentFacing));
                                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, currentPos, currentFacing));
                                if (swing.getValue()) {
                                    mc.player.swingHand(Hand.MAIN_HAND);
                                }}, rotate.getValue(), false));
                        }
                    }
                }
            }
        }
    }

    public void onDisable() {
        if (priorSlot != -1 && mc.player != null) {
            mc.player.inventory.selectedSlot = priorSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(priorSlot));
            priorSlot = -1;
        }
    }

    private PlayerEntity getNearestTarget() {
        return mc.world.getPlayers()
                .stream()
                .filter(e -> e != mc.player)
                .filter(e -> !e.isDead())
                .filter(e -> !Friends.isFriend(e.getEntityName()))
                .filter(e -> e.getHealth() > 0)
                .filter(e -> mc.player.distanceTo(e) <= range.getValue())
                .filter(VulnerabilityUtil::isVulnerable)
                .min(Comparator.comparing(e -> mc.player.distanceTo(e)))
                .orElse(null);
    }

    private Direction getFacing(BlockPos pos, boolean strictDirection) {
        List<Direction> validAxis = new ArrayList<>();
        Vec3d eyePos = LookCalculator.getEyesPos(mc.player);
        if (strictDirection) {
            Vec3d blockCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            BlockState blockState = mc.world.getBlockState(pos);
            boolean isFullBox = blockState.getBlock() == Blocks.AIR || blockState.isFullCube(mc.world, pos);
            validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, Direction.WEST, Direction.EAST, !isFullBox));
            validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, Direction.DOWN, Direction.UP, true));
            validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, Direction.NORTH, Direction.SOUTH, !isFullBox));
        } else {
            validAxis = Arrays.asList(Direction.values());
        }
        return validAxis.stream().min(Comparator.comparing(enumFacing -> new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                .add(new Vec3d(enumFacing.getUnitVector()).multiply(0.5)).distanceTo(eyePos))).orElse(null);
    }


    private int findBestTool(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        int bestSlot = -1;
        double bestSpeed = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStack(i);
            if (stack.isEmpty() || stack.getItem() == Items.AIR) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            int eff;
            if (speed > 1) {
                speed += ((eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack)) > 0 ? (Math.pow(eff, 2) + 1) : 0);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (currentPos != null && showMining.getValue()) {
            if (curBlockDamage >= 1F) {
                ModelRenderer.boxWithLines(ModelRenderer.NORMAL, ModelRenderer.LINES, currentPos, readyColor.getValue().getRenderColor(), readyLineColor.getValue().getRenderColor(), ShapeMode.Both, 0);
            } else {
                ModelRenderer.boxWithLines(ModelRenderer.NORMAL, ModelRenderer.LINES, currentPos, miningColor.getValue().getRenderColor(), miningLineColor.getValue().getRenderColor(), ShapeMode.Both, 0);
            }
        }
    }
}
