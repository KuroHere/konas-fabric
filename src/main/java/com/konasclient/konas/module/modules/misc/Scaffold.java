package com.konasclient.konas.module.modules.misc;

import com.konasclient.konas.event.events.player.ClipAtLedgeEvent;
import com.konasclient.konas.event.events.player.PlayerMoveEvent;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.event.events.render.RenderEvent;
import com.konasclient.konas.interfaceaccessors.IVec3d;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.movement.Sprint;
import com.konasclient.konas.module.modules.render.Interactions;
import com.konasclient.konas.setting.BlockListSetting;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.setting.SubBind;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.entity.PlayerUtils;
import com.konasclient.konas.util.interaction.InteractionUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

public class Scaffold extends Module {

    private List<Block> invalid = Arrays.asList(Blocks.ANVIL, Blocks.AIR, Blocks.COBWEB, Blocks.WATER, Blocks.FIRE, Blocks.WATER, Blocks.LAVA, Blocks.CHEST, Blocks.ENCHANTING_TABLE, Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST, Blocks.GRAVEL, Blocks.LADDER, Blocks.VINE, Blocks.BEACON, Blocks.JUKEBOX, Blocks.ACACIA_DOOR, Blocks.BIRCH_DOOR, Blocks.DARK_OAK_DOOR, Blocks.IRON_DOOR,
            Blocks.JUNGLE_DOOR, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.IRON_TRAPDOOR, Blocks.ACACIA_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.CRIMSON_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.WARPED_TRAPDOOR, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX);

    private Timer timerMotion = new Timer();

    public static Setting<BlockListSetting> customBlocks = new Setting<>("CustomBlocks", new BlockListSetting());

    private static Setting<FilterMode> filterMode = new Setting<>("Filter", FilterMode.NONE);
    private static Setting<Double> expand = new Setting<>("Expand", 1D, 6D, 0.0D, 0.1D);
    private static Setting<Double> delay = new Setting<>("Delay", 3.5D, 10D, 1D, 0.5D);
    private static Setting<Boolean> Switch = new Setting<>("Switch", true);
    private static Setting<Boolean> tower = new Setting<>("Tower", true);
    private static Setting<Boolean> center = new Setting<>("Center", true);
    private static Setting<Boolean> safe = new Setting<>("Safe", true);
    private static Setting<Boolean> keepY = new Setting<>("KeepY", true);
    private static Setting<Boolean> sprint = new Setting<>("Sprint", true);
    private static Setting<Boolean> down = new Setting<>("Down", true);
    private static Setting<Boolean> swing = new Setting<>("Swing", false);
    public static Setting<SubBind> downBind = new Setting<>("DownBind", new SubBind(GLFW.GLFW_KEY_RIGHT_ALT)).withVisibility(down::getValue);

    private enum FilterMode {
        NONE, WHITELIST, BLACKLIST
    }

    private int lastY;

    private Timer lastTimer = new Timer();
    private float lastYaw;
    private float lastPitch;

    private BlockPos pos;

    private boolean teleported;

    public Scaffold() {
        super("scaffold", "Automatically places blocks below you", 0xFFAB933A, Category.Misc);
    }

    private boolean isValid(Block block) {
        if (invalid.contains(block)) return false;

        if (filterMode.getValue() == FilterMode.BLACKLIST) {
            return !customBlocks.getValue().getBlocks().contains(block);
        } else if (filterMode.getValue() == FilterMode.WHITELIST) {
            return customBlocks.getValue().getBlocks().contains(block);
        }

        return true;
    }

    public void onEnable() {
        if (mc.world != null) {
            this.timerMotion.reset();
            this.lastY = MathHelper.floor(mc.player.getY());
        }
    }

    /* uhhh
    @EventHandler(priority = 3)
    public void onUpdateLessPriorty(UpdateWalkingPlayerEvent event) {
        if (!lastTimer.hasPassed(100D * delay.getValue()) && InteractionUtil.canPlaceNormally()) {
            KonasGlobals.INSTANCE.rotationManager.setRotations((float) lastYaw, (float) lastPitch);
        }
    }*/

    @EventHandler
    public void onRender(RenderEvent event) {
        if (this.pos != null) {
            Interactions.renderPlace(pos);
        }
    }

    @EventHandler
    public void onClipAtLedge(ClipAtLedgeEvent event) {
        if (down.getValue() && InputUtil.isKeyPressed(mc.getWindow().getHandle(), downBind.getValue().getKeyCode()))
            return;

        if (safe.getValue()) event.cancel();
    }

    /*@EventHandler(priority = 3)
    public void onUpdateLessPriorty(UpdateEvent.Pre event) {
        if (!lastTimer.hasPassed(100D * delay.getValue()) && InteractionUtil.canPlaceNormally()) {
            KonasGlobals.INSTANCE.rotationManager.setRotations((float) lastYaw, (float) lastPitch);
        }
    }*/

    @EventHandler(priority = 20)
    public void onUpdate(UpdateWalkingPlayerEvent event) {
        int downDistance;
        if (!ModuleManager.get(Sprint.class).isActive() && ((
                down.getValue() && InputUtil.isKeyPressed(mc.getWindow().getHandle(), downBind.getValue().getKeyCode())) || !sprint.getValue()))
            mc.player.setSprinting(false);

        boolean doDown = down.getValue() && InputUtil.isKeyPressed(mc.getWindow().getHandle(), downBind.getValue().getKeyCode());
        downDistance = doDown ? 2 : 1;

        if (keepY.getValue()) {
            if ((!PlayerUtils.isPlayerMoving() && mc.options.keyJump.isPressed()) || mc.player.verticalCollision || mc.player.isOnGround())
                this.lastY = MathHelper.floor(mc.player.getY());
        } else {
            this.lastY = MathHelper.floor(mc.player.getY());
        }
        this.pos = null;
        double x = mc.player.getX();
        double z = mc.player.getZ();
        double y = keepY.getValue() ? this.lastY : mc.player.getY();
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.yaw;
        if (!mc.player.horizontalCollision && expand.getValue() > 0D) {
            double[] coords = getExpandCoords(x, z, forward, strafe, yaw);
            x = coords[0];
            z = coords[1];
        }
        if (canPlace(mc.world.getBlockState(new BlockPos(mc.player.getX(), mc.player.getY() - downDistance, mc.player.getZ())).getBlock())) {
            x = mc.player.getX();
            z = mc.player.getZ();
        }
        BlockPos blockBelow = new BlockPos(x, Math.floor(y) - downDistance, z);
        if (mc.world.getBlockState(blockBelow).getMaterial().isReplaceable()) {
            this.pos = blockBelow;
        }
        if (this.pos != null) {
            int slot = -1;
            final ItemStack mainhandStack = mc.player.getMainHandStack();
            if (mainhandStack != ItemStack.EMPTY && mainhandStack.getItem() instanceof BlockItem) {
                final Block blockFromMainhandItem = ((BlockItem) mainhandStack.getItem()).getBlock();
                if (isValid(blockFromMainhandItem)) {
                    slot = mc.player.inventory.selectedSlot;
                }
            }

            final ItemStack offhandStack = mc.player.getOffHandStack();
            if (offhandStack != ItemStack.EMPTY && offhandStack.getItem() instanceof BlockItem) {
                final Block blockFromOffhandItem = ((BlockItem) offhandStack.getItem()).getBlock();
                if (isValid(blockFromOffhandItem)) {
                    slot = -2;
                }
            }

            if (Switch.getValue()) {
                if (slot == -1) {
                    for (int i = 0; i < 9; i++) {
                        final ItemStack stack = mc.player.inventory.getStack(i);
                        if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem) {
                            final Block blockFromItem = ((BlockItem) stack.getItem()).getBlock();
                            if (isValid(blockFromItem)) {
                                slot = i;
                                break;
                            }
                        }
                    }
                }
            }

            if (slot != -1) {
                if (tower.getValue()) {
                    if (mc.options.keyJump.isPressed() && mc.player.forwardSpeed == 0.0F && mc.player.sidewaysSpeed == 0.0F && tower.getValue() && !mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                        if (!this.teleported && center.getValue()) {
                            this.teleported = true;
                            BlockPos pos = new BlockPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                            mc.player.updatePosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                        }
                        if (center.getValue() && !this.teleported)
                            return;
                        mc.player.setVelocity(0.0D, 0.41999998688697815D, 0.0D);
                        if (this.timerMotion.hasPassed(1500L)) {
                            timerMotion.reset();
                            mc.player.setVelocity(mc.player.getVelocity().x, -0.28D, mc.player.getVelocity().z);
                        }
                    } else {
                        this.timerMotion.reset();
                        if (this.teleported && center.getValue())
                            this.teleported = false;
                    }
                }
                if (InteractionUtil.place(this.pos, true, swing.getValue(), false, slot == -2 ? Hand.OFF_HAND : Hand.MAIN_HAND, slot) != null) {

                }
            }
        }
    }

    private int getBlockCount() {
        int blockCount = 0;
        for (int i = 0; i < 45; i++) {
            if (mc.player.inventory.getStack(i).isStackable()) {
                ItemStack is = mc.player.inventory.getStack(i);
                Item item = is.getItem();
                if (is.getItem() instanceof BlockItem &&
                        !this.invalid.contains(((BlockItem) item).getBlock()))
                    blockCount += is.getCount();
            }
        }
        return blockCount;
    }

    public double[] getExpandCoords(double x, double z, double forward, double strafe, float yaw) {
        BlockPos underPos = new BlockPos(x, mc.player.getY() - ((InputUtil.isKeyPressed(mc.getWindow().getHandle(), downBind.getValue().getKeyCode()) && down.getValue()) ? 2 : 1), z);
        Block underBlock = mc.world.getBlockState(underPos).getBlock();
        double xCalc = -999.0D, zCalc = -999.0D;
        double dist = 0.0D;
        double expandDist = expand.getValue() * 2.0D;
        while (!canPlace(underBlock)) {
            xCalc = x;
            zCalc = z;
            dist++;
            if (dist > expandDist)
                dist = expandDist;
            xCalc += (forward * 0.45D * Math.cos(Math.toRadians((yaw + 90.0F))) + strafe * 0.45D * Math.sin(Math.toRadians((yaw + 90.0F)))) * dist;
            zCalc += (forward * 0.45D * Math.sin(Math.toRadians((yaw + 90.0F))) - strafe * 0.45D * Math.cos(Math.toRadians((yaw + 90.0F)))) * dist;
            if (dist == expandDist)
                break;
            underPos = new BlockPos(xCalc, mc.player.getY() - ((InputUtil.isKeyPressed(mc.getWindow().getHandle(), downBind.getValue().getKeyCode()) && down.getValue()) ? 2 : 1), zCalc);
            underBlock = mc.world.getBlockState(underPos).getBlock();
        }
        return new double[]{xCalc, zCalc};
    }

    public boolean canPlace(Block block) {
        //think
        return ((block instanceof AirBlock || block instanceof FluidBlock) && mc.world != null && mc.player != null && this.pos != null && !mc.world.intersectsEntities(mc.player, VoxelShapes.cuboid(new Box(this.pos))));
    }

    private int getBlockCountHotbar() {
        int blockCount = 0;
        for (int i = 36; i < 45; i++) {
            if (mc.player.inventory.getStack(i).isStackable()) {
                ItemStack is = mc.player.inventory.getStack(i);
                Item item = is.getItem();
                if (is.getItem() instanceof BlockItem &&
                        !this.invalid.contains(((BlockItem) item).getBlock()))
                    blockCount += is.getCount();
            }
        }
        return blockCount;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        double x = event.movement.getX();
        double z = event.movement.getZ();

        if (mc.player.isOnGround() && !mc.player.noClip && safe.getValue() && !InputUtil.isKeyPressed(mc.getWindow().getHandle(), downBind.getValue().getKeyCode())) {
            double i;

            for (i = 0.05D; x != 0.0D && !mc.world.intersectsEntities(mc.player, VoxelShapes.cuboid(mc.player.getBoundingBox().offset(x, -1.0f, 0.0D))); ) {
                if (x < i && x >= -i) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= i;
                } else {
                    x += i;
                }
            }

            while (z != 0.0D && !mc.world.intersectsEntities(mc.player, VoxelShapes.cuboid(mc.player.getBoundingBox().offset(0.0D, -1.0f, z)))) {
                if (z < i && z >= -i) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= i;
                } else {
                    z += i;
                }
            }

            while (x != 0.0D && z != 0.0D && !mc.world.intersectsEntities(mc.player, VoxelShapes.cuboid(mc.player.getBoundingBox().offset(x, -1.0f, z)))) {
                if (x < i && x >= -i) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= i;
                } else {
                    x += i;
                }
                if (z < i && z >= -i) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= i;
                } else {
                    z += i;
                }
            }
        }

        ((IVec3d) event.movement).setX(x);
        ((IVec3d) event.movement).setZ(z);
    }
}

