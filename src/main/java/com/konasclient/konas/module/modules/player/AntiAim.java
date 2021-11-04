package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.action.Action;
import com.konasclient.konas.util.action.ActionManager;
import com.konasclient.konas.util.interaction.LookCalculator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;

public class AntiAim extends Module {
    private static final Setting<Mode> mode = new Setting<>("Mode", Mode.Spin);
    private static final Setting<PitchMode> pitchMode = new Setting<>("Pitch", PitchMode.Jitter);
    private static final Setting<Integer> speed = new Setting<>("Speed", 10, 55, 1, 1);
    private static final Setting<Integer> yawAdd = new Setting<>("YawAdd", 0, 180, -180, 10);

    private enum Mode {
        Spin, Jitter, Stare
    }

    private enum PitchMode {
        None, Jitter, Stare, Down
    }

    public AntiAim() {
        super("anti-aim", "Breaks motion prediction in bad clients", 0xFFE5D69F, Category.Player, "spin-bot", "stare");
    }

    private float currentYaw = 0F;
    private float currentPitch = 0F;

    @Override
    public String getMetadata() {
        return mode.getValue().toString();
    }

    @EventHandler(priority = 1)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (ActionManager.full()) {
            return;
        }
        
        if (mode.getValue() == Mode.Spin) {
            currentYaw += speed.getValue();
        } else if (mode.getValue() == Mode.Jitter) {
            if (Math.random() > 0.5) {
                currentYaw += speed.getValue() * Math.random();
            } else {
                currentYaw -= speed.getValue() * Math.random();
            }
        } else {
            PlayerEntity nearestEntity = getNearestEntity();
            if (nearestEntity != null) {
                currentYaw = LookCalculator.calculateAngle(LookCalculator.getEyesPos(nearestEntity))[0] - 180;
            } else {
                currentYaw = mc.player.yaw;
            }
        }

        currentYaw += yawAdd.getValue();

        currentYaw = MathHelper.wrapDegrees((int) currentYaw);

        if (pitchMode.getValue() == PitchMode.None) {
            currentPitch = mc.player.pitch;
        } else if (pitchMode.getValue() == PitchMode.Jitter) {
            if (Math.random() > 0.5) {
                currentPitch += speed.getValue() * Math.random();
            } else {
                currentPitch -= speed.getValue() * Math.random();
            }
        } else if (pitchMode.getValue() == PitchMode.Stare) {
            PlayerEntity nearestEntity = getNearestEntity();
            if (nearestEntity != null) {
                currentPitch = LookCalculator.calculateAngle(LookCalculator.getEyesPos(nearestEntity))[1];
            } else {
                currentPitch = mc.player.pitch;
            }
        } else {
            currentPitch = 90;
        }

        if (currentPitch > 89) {
            currentPitch = 89;
        } else if (currentPitch < -89) {
            currentPitch = -89;
        }

        ActionManager.add(new Action(currentYaw, currentPitch, null, true));
    }

    private PlayerEntity getNearestEntity() {
        return mc.world.getPlayers().stream()
                .filter(e -> e != mc.player)
                // .filter(e -> !Friends.isFriend(e.getName()))
                .filter(e -> e.distanceTo(mc.player) < 10)
                .min(Comparator.comparing(e -> mc.player.distanceTo(e)))
                .orElse(null);
    }
}
