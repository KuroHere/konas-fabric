package com.konasclient.konas.module.modules.movement;

import com.konasclient.konas.event.events.player.PlayerMoveEvent;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.interfaceaccessors.IVec3d;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.client.TimerManager;
import com.konasclient.konas.util.entity.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Speed extends Module {
    private Setting<Mode> mode = new Setting<>("Mode", Mode.Strafe);
    private Setting<Double> speed = new Setting<>("Speed", 1.0D, 10D, 0D, 0.1D).withVisibility(() -> mode.getValue() == Mode.Vanilla);
    private Setting<Boolean> antiLagback = new Setting<>("Strict", true);
    private Setting<Boolean> useTimer = new Setting<>("UseTimer", true);


    private Setting<Parent> whileIn = new Setting<>("While", new Parent(true));
    private Setting<Boolean> whileSneaking = new Setting<>("Sneaking", false).withParent(whileIn);
    private Setting<Boolean> whileInLiquid = new Setting<>("InLiquid", true).withParent(whileIn);
    private Setting<Boolean> whileInWeb = new Setting<>("InWeb", false).withParent(whileIn);

    private enum Mode {
        Strafe, Vanilla
    }

    private double currentMotion = 0D;
    private double prevMotion = 0D;
    private boolean odd = false;
    private int state = 4;

    private double aacSpeed = 0.2873D;
    private int aacCounter;
    private int aacState = 4;
    private int ticksPassed = 0;

    private Timer timer = new Timer();
    private Timer lagbackTimer = new Timer();

    public Speed() {
        super("speed", "Makes you go faster", 0xFFE58AB2, Category.Movement);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!whileSneaking.getValue() && mc.player.isSneaking()) return;
        if (!whileInLiquid.getValue() && PlayerUtils.checkIfBlockInBB(FluidBlock.class)) return;
        if (!whileInWeb.getValue() && PlayerUtils.checkIfBlockInBB(CobwebBlock.class)) return;
        if (!lagbackTimer.hasPassed(350)) return;

        double x = event.movement.x;
        double y = event.movement.y;
        double z = event.movement.z;

        switch (mode.getValue()) {
            case Strafe: {
                if (antiLagback.getValue()) {
                    aacCounter++;
                    aacCounter %= 5;

                    if (aacCounter != 0) {
                        TimerManager.resetTimer(this);
                    } else if (PlayerUtils.isPlayerMoving()) {
                        if (useTimer.getValue()) {
                            TimerManager.updateTimer(this, 10, 1.3F);
                        }
                        mc.player.setVelocity(mc.player.getVelocity().x * 1.0199999809265137D, mc.player.getVelocity().y, mc.player.getVelocity().z * 1.0199999809265137D);
                    }

                    if (mc.player.isOnGround() && PlayerUtils.isPlayerMoving()) {
                        aacState = 2;
                    }

                    if (round(mc.player.getY() - (int)mc.player.getY(), 3) == round(0.138D, 3)) {
                        mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y - 0.08D, mc.player.getVelocity().z);
                        y -= 0.09316090325960147D;
                        mc.player.setPos(mc.player.getX(), mc.player.getY() - 0.09316090325960147D, mc.player.getZ());
                    }

                    if (aacState == 1 && (mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F)) {
                        aacState = 2;
                        aacSpeed = 1.38D * PlayerUtils.getBaseMotionSpeed() - 0.01D;
                    } else if (aacState == 2) {
                        aacState = 3;
                        mc.player.setVelocity(mc.player.getVelocity().x, 0.399399995803833D, mc.player.getVelocity().z);
                        y = 0.399399995803833D;
                        aacSpeed *= 2.149D;
                    } else if (aacState == 3) {
                        aacState = 4;
                        double adjustedMotion = 0.66D * (prevMotion - PlayerUtils.getBaseMotionSpeed());
                        aacSpeed = prevMotion - adjustedMotion;
                    } else {
                        if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0)) || mc.player.verticalCollision)
                            aacState = 1;
                        aacSpeed = prevMotion - prevMotion / 159.0D;
                    }

                    aacSpeed = Math.max(aacSpeed, PlayerUtils.getBaseMotionSpeed());

                    aacSpeed = Math.min(aacSpeed, (ticksPassed > 25) ? 0.449D : 0.433D);

                    float forward = mc.player.input.movementForward;
                    float strafe = mc.player.input.movementSideways;
                    float yaw = mc.player.yaw;

                    ticksPassed++;

                    if (ticksPassed > 50)
                        ticksPassed = 0;
                    if (forward == 0.0F && strafe == 0.0F) {
                        x = 0D;
                        z = 0D;
                    } else if (forward != 0.0F) {
                        if (strafe >= 1.0F) {
                            yaw += ((forward > 0.0F) ? -45 : 45);
                            strafe = 0.0F;
                        } else if (strafe <= -1.0F) {
                            yaw += ((forward > 0.0F) ? 45 : -45);
                            strafe = 0.0F;
                        }
                        if (forward > 0.0F) {
                            forward = 1.0F;
                        } else if (forward < 0.0F) {
                            forward = -1.0F;
                        }
                    }

                    double cos = Math.cos(Math.toRadians((yaw + 90.0F)));
                    double sin = Math.sin(Math.toRadians((yaw + 90.0F)));

                    x = forward * aacSpeed * cos + strafe * aacSpeed * sin;
                    z = forward * aacSpeed * sin - strafe * aacSpeed * cos;

                    if (forward == 0.0F && strafe == 0.0F) {
                        x = 0.0D;
                        z = 0.0D;
                    }
                } else {
                    if (state != 1 || (mc.player.input.movementForward == 0.0f || mc.player.input.movementSideways == 0.0f)) {
                        if (state == 2 && (mc.player.input.movementForward != 0.0f || mc.player.input.movementSideways != 0.0f)) {
                            double jumpSpeed = 0.3999D;

                            if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                                jumpSpeed += (mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F;
                            }

                            mc.player.setVelocity(new Vec3d(mc.player.getVelocity().x, jumpSpeed, mc.player.getVelocity().z));
                            y = jumpSpeed;
                            currentMotion *= odd ? 1.6835D : 1.395D;
                        } else if (state == 3) {
                            double adjustedMotion = (antiLagback.getValue() ? 0.76D : 0.66D) * (prevMotion - PlayerUtils.getBaseMotionSpeed());
                            currentMotion = prevMotion - adjustedMotion;
                            odd = !odd;
                        } else {
                            if ((!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0)) || mc.player.verticalCollision) && state > 0) {
                                state = mc.player.input.movementForward == 0.0f && mc.player.input.movementSideways == 0.0f ? 0 : 1;
                            }
                            currentMotion = prevMotion - prevMotion / 159.0;
                        }
                    } else {
                        currentMotion = 1.35D * PlayerUtils.getBaseMotionSpeed() - 0.01D;
                    }

                    currentMotion = Math.max(currentMotion, PlayerUtils.getBaseMotionSpeed());

                    if (antiLagback.getValue()) {
                        if (lagbackTimer.hasPassed(2500L)) {
                            lagbackTimer.reset();
                        }

                        currentMotion = Math.min(currentMotion, lagbackTimer.hasPassed(1250L) ? 0.44D : 0.43D);
                    }


                    Vec3d directionalSpeed = PlayerUtils.getDirectionalSpeed(currentMotion);

                    x = directionalSpeed.x;
                    z = directionalSpeed.z;

                    if (mc.player.input.movementForward != 0.0f || mc.player.input.movementSideways != 0.0f) {
                        state++;
                    }
                }
                break;
            }
            case Vanilla: {
                if (!antiLagback.getValue() || !timer.hasPassed(190)) {
                    x *= speed.getValue();
                    z *= speed.getValue();
                } else {
                    timer.reset();
                }
                break;
            }
        }

        ((IVec3d) event.movement).setX(x);
        ((IVec3d) event.movement).setY(y);
        ((IVec3d) event.movement).setZ(z);
    }

    private double round(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @EventHandler
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (!PlayerUtils.isPlayerMoving()) {
            currentMotion = 0D;
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            TimerManager.resetTimer(this);
        } else if (useTimer.getValue() && !(mode.getValue() == Mode.Strafe && antiLagback.getValue())) {
            TimerManager.updateTimer(this, 69, 21.75F / 20F); // 22 packets/tick flags sometimes
        } else {
            TimerManager.resetTimer(this);
        }

        double dX = mc.player.getX() - mc.player.prevX;
        double dZ = mc.player.getZ() - mc.player.prevZ;
        prevMotion = Math.sqrt(dX * dX + dZ * dZ);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }
        state = 4;
        currentMotion = PlayerUtils.getBaseMotionSpeed();
        prevMotion = 0;
    }

    @Override
    public void onDisable() {
        TimerManager.resetTimer(this);
    }
}
