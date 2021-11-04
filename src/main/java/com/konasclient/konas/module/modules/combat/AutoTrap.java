package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.modules.render.Interactions;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.setting.SubBind;
import com.konasclient.konas.util.client.ThreadUtils;
import com.konasclient.konas.util.friend.Friends;
import com.konasclient.konas.util.interaction.InteractionUtil;
import com.konasclient.konas.util.interaction.LookCalculator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

public class AutoTrap extends Module {
    private static Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private static Setting<Boolean> swing = new Setting<>("Swing", true);

    private static Setting<Integer> actionShift = new Setting<>("ActionShift", 3, 5, 1, 1);
    private static Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 10, 0, 1);

    private static final Setting<Float> placeRange = new Setting<>("TargetRange", 3.5F, 6F, 1F, 0.1F);

    private static final Setting<Boolean> top = new Setting<>("Top", true);
    private static final Setting<SubBind> self = new Setting<>("Self", new SubBind(GLFW.GLFW_KEY_LEFT_ALT));
    private static Setting<Boolean> toggelable = new Setting<>("DisableWhenDone", false);
    // private static Setting<Boolean> logoutSpots = new Setting<>("LogoutSpots", false);

    public AutoTrap() {
        super("auto-trap", "Automatically traps players", 0xFFF36711, Category.Combat);
    }

    private int tickCounter = 0;

    private ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

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
        if (!mc.player.isOnGround()) return;

        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
        }

        PlayerEntity nearestTarget = getNearestTarget();

        if (nearestTarget == null || tickCounter < actionInterval.getValue()) {
            return;
        }

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            BlockPos nextPos = getNextPos(nearestTarget.getBlockPos());
            if (nextPos != null) {
                if (InteractionUtil.place(Blocks.OBSIDIAN, nextPos, rotate.getValue(), swing.getValue(), true) != null) {
                    blocksPlaced++;
                    InteractionUtil.ghostBlocks.put(nextPos, System.currentTimeMillis());
                    renderPoses.put(nextPos, System.currentTimeMillis());
                    tickCounter = 0;
                } else {
                    return;
                }
            } else {
                if (toggelable.getValue()) {
                    toggle();
                    return;
                }
            }
        }
    }

    private BlockPos getNextPos(BlockPos playerPos) {
        for (Direction enumFacing : Direction.values()) {
            if (enumFacing == Direction.DOWN || enumFacing == Direction.UP) continue;
            BlockPos furthestBlock = null;
            double furthestDistance = 0D;
            if (InteractionUtil.canPlaceBlock(playerPos.offset(enumFacing).down(), true)) {
                BlockPos tempBlock = playerPos.offset(enumFacing).down();
                double tempDistance = LookCalculator.getEyesPos(mc.player).distanceTo(new Vec3d(tempBlock.getX() + 0.5, tempBlock.getY() + 0.5, tempBlock.getZ() + 0.5));
                if (tempDistance >= furthestDistance) {
                    furthestBlock = tempBlock;
                    furthestDistance = tempDistance;
                }
            }
            if (furthestBlock != null) return furthestBlock;
        }

        for (Direction enumFacing : Direction.values()) {
            if (enumFacing == Direction.DOWN || enumFacing == Direction.UP) continue;
            BlockPos furthestBlock = null;
            double furthestDistance = 0D;
            if (InteractionUtil.canPlaceBlock(playerPos.offset(enumFacing), true)) {
                BlockPos tempBlock = playerPos.offset(enumFacing);
                double tempDistance = LookCalculator.getEyesPos(mc.player).distanceTo(new Vec3d(tempBlock.getX() + 0.5, tempBlock.getY() + 0.5, tempBlock.getZ() + 0.5));
                if (tempDistance >= furthestDistance) {
                    furthestBlock = tempBlock;
                    furthestDistance = tempDistance;
                }
            }
            if (furthestBlock != null) return furthestBlock;
        }

        for (Direction enumFacing : Direction.values()) {
            if (enumFacing == Direction.DOWN || enumFacing == Direction.UP) continue;
            BlockPos furthestBlock = null;
            double furthestDistance = 0D;
            if (InteractionUtil.canPlaceBlock(playerPos.up().offset(enumFacing), true)) {
                BlockPos tempBlock = playerPos.up().offset(enumFacing);;
                double tempDistance = LookCalculator.getEyesPos(mc.player).distanceTo(new Vec3d(tempBlock.getX() + 0.5, tempBlock.getY() + 0.5, tempBlock.getZ() + 0.5));
                if (tempDistance >= furthestDistance) {
                    furthestBlock = tempBlock;
                    furthestDistance = tempDistance;
                }
            }
            if (furthestBlock != null) return furthestBlock;
        }

        if (top.getValue()) {
            Block baseBlock = mc.world.getBlockState(playerPos.up().up()).getBlock();
            if (baseBlock instanceof AirBlock || baseBlock instanceof FluidBlock) {
                if (InteractionUtil.canPlaceBlock(playerPos.up().up(), true)) {
                    return playerPos.up().up();
                } else {
                    BlockPos offsetPos = playerPos.up().up().offset(Direction.fromHorizontal(MathHelper.floor((double) (mc.player.yaw * 4.0F / 360.0F) + 0.5D) & 3));
                    if (InteractionUtil.canPlaceBlock(offsetPos, false)) {
                        return offsetPos;
                    }
                }
            }
        }

        return null;
    }

    private PlayerEntity getNearestTarget() {
        return mc.world.getPlayers()
                .stream()
                .filter(e -> e != mc.player)
                .filter(e -> !e.isDead())
                .filter(e -> !Friends.isFriend(e.getEntityName()))
                .filter(e -> e.getHealth() > 0)
                .filter(e -> mc.player.distanceTo(e) <= placeRange.getValue())
                .filter(this::isValidBase)
                .min(Comparator.comparing(e -> mc.player.distanceTo(e)))
                .orElse(InputUtil.isKeyPressed(mc.getWindow().getHandle(), self.getValue().getKeyCode()) ? mc.player : null);
    }

    private boolean isValidBase(PlayerEntity player) {
        BlockPos basePos = new BlockPos(player.getX(), player.getY(), player.getZ()).down();

        Block baseBlock = mc.world.getBlockState(basePos).getBlock();

        return !(baseBlock instanceof AirBlock) && !(baseBlock instanceof FluidBlock);
    }
}
