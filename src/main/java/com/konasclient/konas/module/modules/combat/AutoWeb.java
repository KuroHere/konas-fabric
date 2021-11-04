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
import com.konasclient.konas.util.interaction.InteractionRules;
import com.konasclient.konas.util.interaction.InteractionUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

public class AutoWeb extends Module {
    private static Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private static Setting<Boolean> swing = new Setting<>("Swing", true);

    private static Setting<Integer> actionShift = new Setting<>("ActionShift", 2, 2, 1, 1);
    private static Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 10, 0, 1);

    private static final Setting<Float> placeRange = new Setting<>("TargetRange", 3.5F, 6F, 1F, 0.1F);

    private static Setting<Boolean> head = new Setting<>("Head", true);

    private static final Setting<SubBind> self = new Setting<>("Self", new SubBind(GLFW.GLFW_KEY_LEFT_ALT));
    private static Setting<Boolean> toggelable = new Setting<>("DisableWhenDone", false);

    public AutoWeb() {
        super("auto-web", "Places webs at enemies' feet", 0xFFE97070, Category.Combat);
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

        BlockPos feetPos = new BlockPos(nearestTarget.getX() + (nearestTarget.getX() - nearestTarget.prevX), nearestTarget.getY(), nearestTarget.getZ() + (nearestTarget.getZ() - nearestTarget.prevZ));

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            InteractionRules.ignoreEntities = true;
            BlockPos nextPos = InteractionUtil.canPlaceBlock(feetPos) ? feetPos : head.getValue() ? InteractionUtil.canPlaceBlock(feetPos.up()) ? feetPos.up() : null : null;
            if (nextPos != null) {
                if (InteractionUtil.place(Blocks.COBWEB, nextPos, rotate.getValue(), swing.getValue(), false) != null) {
                    blocksPlaced++;
                    InteractionUtil.ghostBlocks.put(nextPos, System.currentTimeMillis());
                    renderPoses.put(nextPos, System.currentTimeMillis());
                    tickCounter = 0;
                } else {
                    InteractionRules.ignoreEntities = false;
                    return;
                }
            } else {
                if (toggelable.getValue()) {
                    toggle();
                }
                InteractionRules.ignoreEntities = false;
                return;
            }
        }
        InteractionRules.ignoreEntities = false;
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
