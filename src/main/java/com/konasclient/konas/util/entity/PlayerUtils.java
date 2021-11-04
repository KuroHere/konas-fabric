package com.konasclient.konas.util.entity;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static int countItem(Item item) {
        int count = 0;

        for (int i = 0; i < mc.player.inventory.size(); i++) {
            ItemStack itemStack = mc.player.inventory.getStack(i);

            if (itemStack.getItem() == item) {
                count += itemStack.getCount();
            }
        }

        return count;
    }

    public static int countSets() {
        int helmets = 0;
        int chestplates = 0;
        int leggings = 0;
        int boots = 0;

        for (int i = 0; i < mc.player.inventory.size(); i++) {
            ItemStack itemStack = mc.player.inventory.getStack(i);

            if (itemStack.getItem() instanceof ArmorItem) {
                ArmorItem item = (ArmorItem) itemStack.getItem();
                if (item.getSlotType() == EquipmentSlot.HEAD) {
                    helmets++;
                } else if (item.getSlotType() == EquipmentSlot.CHEST) {
                    chestplates++;
                } else if (item.getSlotType() == EquipmentSlot.LEGS) {
                    leggings++;
                } else if (item.getSlotType() == EquipmentSlot.FEET) {
                    boots++;
                }
            }
        }

        return Math.min(Math.min(helmets, chestplates), Math.min(leggings, boots));
    }

    public static Vec3d getDirectionalSpeed(double speed) {
        double x;
        double z;

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.yaw;

        if (forward == 0.0D && strafe == 0.0D) {
            x = 0D;
            z = 0D;
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (float)(forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (float)(forward > 0.0D ? 45 : -45);
                }

                strafe = 0.0D;

                if (forward > 0.0D) {
                    forward = 1.0D;
                } else if (forward < 0.0D) {
                    forward = -1.0D;
                }
            }

            x = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F));
            z = forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F));
        }

        return new Vec3d(x, 0, z);
    }

    public static double getBaseMotionSpeed() {
        double baseSpeed = 0.2873D;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

    public static boolean isPlayerMoving() {
        return mc.player.input.pressingForward || mc.player.input.pressingBack || mc.player.input.pressingRight || mc.player.input.pressingLeft;
    }

    public static boolean checkIfBlockInBB(Class<? extends Block> blockClass) {
        return checkIfBlockInBB(blockClass, (int) Math.floor(mc.player.getBoundingBox(mc.player.getPose()).minY));
    }

    public static boolean checkIfBlockInBB(Class<? extends Block> blockClass, int minY) {
        for(int iX = MathHelper.floor(mc.player.getBoundingBox(mc.player.getPose()).minX); iX < MathHelper.ceil(mc.player.getBoundingBox(mc.player.getPose()).maxX); iX++) {
            for(int iZ = MathHelper.floor(mc.player.getBoundingBox(mc.player.getPose()).minZ); iZ < MathHelper.ceil(mc.player.getBoundingBox(mc.player.getPose()).maxZ); iZ++) {
                BlockState state = mc.world.getBlockState(new BlockPos(iX, minY, iZ));
                if (state != null && blockClass.isInstance(state.getBlock())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void centerPlayerHorizontally() {
        double centerX = MathHelper.floor(mc.player.getX()) + 0.5;
        double centerZ = MathHelper.floor(mc.player.getZ()) + 0.5;
        mc.player.updatePosition(centerX, mc.player.getY(), centerZ);
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
    }

    public static boolean shouldSneakWhileClicking(Block block) {
        return block instanceof EnderChestBlock || block instanceof AnvilBlock || block instanceof AbstractButtonBlock || block instanceof AbstractPressurePlateBlock || block instanceof BlockWithEntity || block instanceof CraftingTableBlock || block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof NoteBlock || block instanceof TrapdoorBlock;
    }

    public static double movementYaw() {
        float yaw = mc.player.yaw;

        if (mc.player.forwardSpeed < 0f) yaw += 180f;

        float forward = 1f;

        if (mc.player.forwardSpeed < 0f) forward = -0.5f;
        else if (mc.player.forwardSpeed > 0f) forward = 0.5f;

        if (mc.player.sidewaysSpeed > 0f) yaw -= 90f * forward;
        if (mc.player.sidewaysSpeed < 0f) yaw += 90f * forward;

        return (double) yaw * 0.017453292;
    }

}
