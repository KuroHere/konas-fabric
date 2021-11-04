package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.modules.render.Interactions;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.ThreadUtils;
import com.konasclient.konas.util.friend.Friends;
import com.konasclient.konas.util.interaction.InteractionUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

public class HoleFill extends Module {
    private static Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private static Setting<Boolean> swing = new Setting<>("Swing", true);
    private static Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);
    private static Setting<Double> rangeXZ = new Setting<>("Range", 5D, 6D, 1D, 0.1D);
    private static Setting<Integer> actionShift = new Setting<>("ActionShift", 1, 3, 1, 1);
    private static Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 5, 0, 1);
    private static Setting<Boolean> jumpDisable = new Setting<>("JumpDisable", false);
    private static Setting<Boolean> onlyWebs = new Setting<>("OnlyWebs", false);
    private static Setting<SmartMode> smartMode = new Setting<>("Smart", SmartMode.Always);
    private static Setting<Double> targetRange = new Setting<>("EnemyRange", 10D, 15D, 1D, 0.5D);
    private static Setting<Boolean> disableWhenNone = new Setting<>("DisableWhenNone", false);

    private enum SmartMode {
        None, Always, Target
    }

    public HoleFill() {
        super("hole-fill", "Automatically fill holes", 0xFFEE57B2, Category.Combat);
    }

    private Map<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    private int tickCounter = 0;

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
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (jumpDisable.getValue() && mc.player.prevY < mc.player.getY()) {
            toggle();
        }

        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
        }

        if (tickCounter < actionInterval.getValue()) {
            return;
        }

        int slot = getBlockSlot();

        if (slot == -1) {
            return;
        }

        List<BlockPos> holes = findHoles();

        if (smartMode.getValue() == SmartMode.Target && getNearestTarget() == null) return;

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos pos = StreamSupport.stream(holes.spliterator(), false)
                    .filter(this::isHole)
                    .filter(p -> mc.player.getPos().distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5)) <= rangeXZ.getValue())
                    .filter(p -> InteractionUtil.canPlaceBlock(p, strictDirection.getValue()))
                    .min(Comparator.comparing(p -> mc.player.getPos().distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5))))
                    .orElse(null);

            if (pos != null) {
                if (InteractionUtil.place(pos, rotate.getValue(), swing.getValue(), strictDirection.getValue(), Hand.MAIN_HAND, slot) != null) {
                    blocksPlaced++;
                    renderPoses.put(pos, System.currentTimeMillis());
                    InteractionUtil.ghostBlocks.put(pos, System.currentTimeMillis());
                    tickCounter = 0;
                    if (!mc.player.isOnGround()) return;
                } else {
                    return;
                }
            } else {
                if (disableWhenNone.getValue()) {
                    toggle();
                }
                return;
            }
        }
    }

    private List<BlockPos> findHoles() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos centerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(rangeXZ.getValue()) + 1;
        int h = rangeXZ.getValue().intValue();
        for (int i = centerPos.getX() - r; i < centerPos.getX() + r; i++) {
            for (int j = centerPos.getY() - h; j < centerPos.getY() + h; j++) {
                for (int k = centerPos.getZ() - r; k < centerPos.getZ() + r; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (isHole(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }

    private int getBlockSlot() {
        ItemStack stack = mc.player.getMainHandStack();

        if (!stack.isEmpty() && isValidItem(stack.getItem())) {
            return mc.player.inventory.selectedSlot;
        } else {
            for (int i = 0; i < 9; ++i) {
                stack = mc.player.inventory.getStack(i);
                if (!stack.isEmpty() && isValidItem(stack.getItem())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isValidItem(Item item) {
        if (item instanceof BlockItem) {
            if (onlyWebs.getValue()) {
                return ((BlockItem) item).getBlock() == Blocks.COBWEB;
            }
            return true;
        }
        return false;
    }

    private PlayerEntity getNearestTarget() {
        return mc.world.getPlayers().stream()
                .filter(e -> e != mc.player)
                .filter(e -> !Friends.isFriend(e.getEntityName()))
                .filter(e -> mc.player.distanceTo(e) < targetRange.getValue())
                .min(Comparator.comparing(e -> mc.player.distanceTo(e)))
                .orElse(null);
    }

    public boolean validObi(BlockPos pos) {
        return !validBedrock(pos)
                && (mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK)
                && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR;
    }

    public boolean validBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR;
    }

    public BlockPos validTwoBlockObiXZ(BlockPos pos) {
        if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.east()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up(2)).getMaterial() == Material.AIR
        ) {
            return validTwoBlockBedrockXZ(pos) == null ? new BlockPos(1, 0, 0) : null;
        } else if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.south().down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south(2)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south().east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south().west()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.south()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up(2)).getMaterial() == Material.AIR
        ) {
            return validTwoBlockBedrockXZ(pos) == null ? new BlockPos(0, 0, 1) : null;
        }
        return null;
    }

    public BlockPos validTwoBlockBedrockXZ(BlockPos pos) {
        if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.east()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up(2)).getMaterial() == Material.AIR
        ) {
            return new BlockPos(1, 0, 0);
        } else if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.south().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().west()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.south()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up(2)).getMaterial() == Material.AIR
        ) {
            return new BlockPos(0, 0, 1);
        }
        return null;
    }

    public boolean isHole(BlockPos pos) {
        return validObi(pos) || validBedrock(pos);
    }
}
