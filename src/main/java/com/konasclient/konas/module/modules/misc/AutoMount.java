package com.konasclient.konas.module.modules.misc;

import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.action.Action;
import com.konasclient.konas.util.action.ActionManager;
import com.konasclient.konas.util.client.Timer;
import com.konasclient.konas.util.interaction.LookCalculator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoMount extends Module {
    private static final Setting<Integer> range = new Setting<>("Range", 4, 10, 1, 1);
    private static final Setting<Float> delay = new Setting<>("Delay", 1F, 10F, 0F, 0.1F);

    private static final Setting<Boolean> boats = new Setting<>("Boats", false);
    private static final Setting<Boolean> horses = new Setting<>("Horses", false);
    private static final Setting<Boolean> skeletonHorses = new Setting<>("SkeletonHorses", false);
    private static final Setting<Boolean> donkeys = new Setting<>("Donkeys", true);
    private static final Setting<Boolean> pigs = new Setting<>("Pigs", false);
    private static final Setting<Boolean> llamas = new Setting<>("Llamas", false);

    private Timer timer = new Timer();

    public AutoMount() {
        super("AutoMount", "Automatically mounts entities", 0xFFA16DD0, Category.Misc);
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Pre event) {
        if (mc.player == null) return;
        if (mc.player.isRiding() || mc.player.getVehicle() != null || mc.currentScreen instanceof HorseScreen) {
            timer.reset();
        }
    }

    @EventHandler
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (mc.player.isRiding() || mc.player.getVehicle() != null || mc.currentScreen instanceof HorseScreen) {
            timer.reset();
            return;
        }

        if (!timer.hasPassed(delay.getValue() * 1000)) {
            return;
        }

        timer.reset();

        List<Entity> entities = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (isValidEntity(entity)) entities.add(entity);
        }

        Entity selectedEntity = entities.stream()
                .min(Comparator.comparing(entity -> mc.player.distanceTo(entity)))
                .orElse(null);

        if (selectedEntity != null) {
            float[] rotations = LookCalculator.calculateAngle(selectedEntity.getPos());
            ActionManager.add(new Action(rotations[0], rotations[1], () -> {
                mc.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(selectedEntity, Hand.MAIN_HAND, false));
            }));
        }
    }

    private boolean isValidEntity(Entity entity) {
        if (entity.distanceTo(mc.player) > range.getValue()) {
            return false;
        }

        if (entity instanceof HorseEntity && horses.getValue()) {
           return true;
        }

        if (entity instanceof BoatEntity && boats.getValue()) {
            return true;
        }

        if (entity instanceof SkeletonHorseEntity && skeletonHorses.getValue()) {
            return true;
        }

        if (entity instanceof DonkeyEntity && donkeys.getValue()) {
            return true;
        }

        if (entity instanceof PigEntity && pigs.getValue()) {
            PigEntity pig = (PigEntity) entity;

            return pig.isSaddled();
        }

        if (entity instanceof LlamaEntity && llamas.getValue()) {
            return true;
        }

        return false;
    }
}
