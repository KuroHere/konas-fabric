package com.konasclient.konas.module.modules.player;

import baritone.api.BaritoneAPI;
import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.movement.Speed;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.client.TimerManager;
import com.konasclient.konas.util.entity.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.stream.Collectors;

public class Step extends Module {

    private static Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL);

    private static Setting<Float> stepHeight = new Setting<>("StepHeight", 1f, 7f, 0.5f, 0.5f);

    private static Setting<Boolean> upwards = new Setting<>("Upwards", true);

    private static Setting<Boolean> reverse = new Setting<>("Reverse", true);

    private static Setting<Boolean> useTimer = new Setting<>("UseTimer", false);

    private static Setting<Boolean> speedDisable = new Setting<>("SpeedDisable", true);

    private static Setting<Boolean> autoDisable = new Setting<>("AutoDisable", false);

    private enum Mode {
        VANILLA, NORMAL, OLD
    }

    private boolean prevOnGround = false;

    private int curStep = 0;

    private final Timer timer = new Timer();
    private final Timer newTimer = new Timer();

    public final double[] oneBlockNCP = {0.42, 0.753};
    public final double[] tallOneBlockNCP = {0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
    public final double[] twoBlockNCP = {0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
    public final double[] tallTwoBlockNCP = {0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};

    public final double[] oneBlockNormal = {0.42D, 0.75D};
    public final double[] tallOneBlockNormal = {0.42D, 0.75D, 1.0D, 1.16D, 1.23D, 1.2D};
    public final double[] twoBlockNormal = {0.42D, 0.7800000000000002D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D};

    public Step() {
        super("step", "Instantly steps up blocks", 0xFFC1D96C, Category.Player);
    }

    @EventHandler
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        Module speed = ModuleManager.get("Speed");

        if (speed != null) {
            if (speed.isActive() && speedDisable.getValue()) {
                toggle();
            }
        }

        if (reverse.getValue() /*&& !ModuleManager.getModuleByClass(RubberFill.class).isEnabled()*/ && prevOnGround && !mc.player.isOnGround() && mc.player.getVelocity().y <= 0.0 && mc.player.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, -3.01, 0.0)).findAny().isPresent() && !mc.player.isSubmergedInWater() && timer.hasPassed(1000L)) {
            mc.player.setVelocity(mc.player.getVelocity().x, -3.0, mc.player.getVelocity().z);
        }

        prevOnGround = mc.player.isOnGround();

        if (mode.getValue() == Mode.NORMAL && newTimer.hasPassed(250)) {
            TimerManager.resetTimer(this);
        }

        if (upwards.getValue() && !mc.player.isSubmergedInWater() && mc.player.isOnGround() && !mc.player.isHoldingOntoLadder() && !mc.player.input.jumping && mc.player.verticalCollision && (double) mc.player.fallDistance < 0.1) {
            if (mode.getValue() == Mode.VANILLA) {
                mc.player.stepHeight = stepHeight.getValue();
            } else if (mode.getValue() == Mode.NORMAL) {
                if (!timer.hasPassed(320)) {
                    return;
                }

                double currentStepHeight = getCurrentStepHeight();

                if (currentStepHeight == 0.0D) {
                    return;
                }

                if (currentStepHeight <= 1.0D) {
                    for (double v : oneBlockNormal) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + v, mc.player.getZ(), mc.player.isOnGround()));
                    }
                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 1.0D, mc.player.getZ());
                    return;
                }

                if (currentStepHeight <= stepHeight.getValue() && currentStepHeight <= 1.5D) {
                    event.setCancelled(true);
                    for (double v : tallOneBlockNormal) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + v, mc.player.getZ(), mc.player.isOnGround()));
                    }
                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() + currentStepHeight, mc.player.getZ());
                    return;
                }

                if (currentStepHeight <= stepHeight.getValue()) {
                    event.setCancelled(true);
                    for (double v : twoBlockNormal) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + v, mc.player.getZ(), mc.player.isOnGround()));
                    }
                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() + currentStepHeight, mc.player.getZ());
                }

                if (useTimer.getValue()){
                    TimerManager.updateTimer(this, 15, 0.6F);
                    newTimer.reset();
                }
            } else if (mode.getValue() == Mode.OLD) {
                Vec3d dir = PlayerUtils.getDirectionalSpeed(0.1);

                if (!mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(dir.x, 1.0, dir.z)).findAny().isPresent() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(dir.x, 0.6, dir.z)).findAny().isPresent() && stepHeight.getValue() >= 1.0){
                    for (double v : oneBlockNCP) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + v, mc.player.getZ(), mc.player.isOnGround()));
                    }

                    if (useTimer.getValue()){
                        TimerManager.updateTimer(this, 15, 0.6F);
                    }

                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ());
                    curStep = 1;
                }
                if (!mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(dir.x, 1.6, dir.z)).findAny().isPresent() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(dir.x, 1.4, dir.z)).findAny().isPresent() && stepHeight.getValue() >= 1.5){
                    for (double v : tallOneBlockNCP) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + v, mc.player.getZ(), mc.player.isOnGround()));
                    }

                    if (useTimer.getValue()){
                        TimerManager.updateTimer(this, 15, 0.35F);
                    }

                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 1.5, mc.player.getZ());
                    curStep = 1;
                }
                if (!mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(dir.x, 2.1, dir.z)).findAny().isPresent() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(dir.x, 1.9, dir.z)).findAny().isPresent() && stepHeight.getValue() >= 2.0){
                    for (double v : twoBlockNCP) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + v, mc.player.getZ(), mc.player.isOnGround()));
                    }

                    if (useTimer.getValue()){
                        TimerManager.updateTimer(this, 15, 0.25F);
                    }

                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 2.0, mc.player.getZ());
                    curStep = 2;
                }
                if (!mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(dir.x, 2.6, dir.z)).findAny().isPresent() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(dir.x, 2.4, dir.z)).findAny().isPresent() && stepHeight.getValue() >= 2.5){
                    for (double v : tallTwoBlockNCP) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + v, mc.player.getZ(), mc.player.isOnGround()));
                    }

                    if (useTimer.getValue()){
                        TimerManager.updateTimer(this, 15, 0.15F);
                    }

                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 2.5, mc.player.getZ());
                    curStep = 2;
                }
            }
        } else if (mode.getValue() == Mode.VANILLA) {
            mc.player.stepHeight = 0.5F;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            timer.reset();
            if (autoDisable.getValue()) {
                toggle();
            }
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Pre event) {
        Module speed = ModuleManager.get(Speed.class);

        if (stepHeight.getValue() > 0.5) {
            BaritoneAPI.getSettings().assumeStep.value = true;
        } else {
            BaritoneAPI.getSettings().assumeStep.value = false;
        }

        if (speed != null) {
            if (speed.isActive() && speedDisable.getValue()) {
                toggle();
            }
        }

        if (mc.player.getVehicle() != null) {
            timer.reset();
        }

        if (useTimer.getValue()){
            if (curStep == 0){
                TimerManager.resetTimer(this);
            } else{
                curStep--;
            }
        } else {
            TimerManager.resetTimer(this);
        }
    }

    @Override
    public void onDisable() {
        mc.player.stepHeight = 0.5F;
        TimerManager.resetTimer(this);
        if (BaritoneAPI.getSettings().assumeStep != null) {
            BaritoneAPI.getSettings().assumeStep.value = false;
        }
    }

    @Override
    public void onEnable() {
        prevOnGround = false;
    }

    private boolean canStep() {
        float rotationYaw = mc.player.yaw;
        if (mc.player.forwardSpeed < 0.0F)
            rotationYaw += 180.0F;
        float forward = 1.0F;
        if (mc.player.forwardSpeed < 0.0F) {
            forward = -0.5F;
        } else if (mc.player.forwardSpeed > 0.0F) {
            forward = 0.5F;
        }
        if (mc.player.sidewaysSpeed > 0.0F)
            rotationYaw -= 90.0F * forward;
        if (mc.player.sidewaysSpeed < 0.0F)
            rotationYaw += 90.0F * forward;

        float yaw = (float) Math.toRadians(rotationYaw);

        double x = -MathHelper.sin(yaw) * 0.4D;
        double z = MathHelper.cos(yaw) * 0.4D;
        return !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(x, 1.001335979112147D, z)).findAny().isPresent();
    }

    private double getCurrentStepHeight() {
        boolean collided = (mc.player.isOnGround() && mc.player.horizontalCollision);

        if (!collided) {
            return 0.0D;
        }

        double maximumY = -1.0D;

        float rotationYaw = mc.player.yaw;
        if (mc.player.forwardSpeed < 0.0F)
            rotationYaw += 180.0F;
        float forward = 1.0F;
        if (mc.player.forwardSpeed < 0.0F) {
            forward = -0.5F;
        } else if (mc.player.forwardSpeed > 0.0F) {
            forward = 0.5F;
        }
        if (mc.player.sidewaysSpeed > 0.0F)
            rotationYaw -= 90.0F * forward;
        if (mc.player.sidewaysSpeed < 0.0F)
            rotationYaw += 90.0F * forward;

        float yaw = (float) Math.toRadians(rotationYaw);

        double x = -MathHelper.sin(yaw) * 0.4D;
        double z = MathHelper.cos(yaw) * 0.4D;

        Box expandedBB = mc.player.getBoundingBox().offset(0.0D, 0.05D, 0.0D).expand(0.05D);
        expandedBB = new Box(expandedBB.minX, expandedBB.minY, expandedBB.minZ, expandedBB.maxX, expandedBB.maxY + stepHeight.getValue(), expandedBB.maxZ);

        for (VoxelShape axisAlignedBB : mc.world.getBlockCollisions(mc.player, expandedBB).collect(Collectors.toList())) {
            if (axisAlignedBB.getBoundingBox().maxY > maximumY)
                maximumY = axisAlignedBB.getBoundingBox().maxY;
        }

        maximumY -= mc.player.getY();
        return (maximumY > 0.0D && maximumY <= stepHeight.getValue()) ? maximumY : 0.0D;
    }
}
