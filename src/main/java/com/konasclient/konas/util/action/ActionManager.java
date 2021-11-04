package com.konasclient.konas.util.action;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.mixin.ClientPlayerEntityAccessor;
import com.konasclient.konas.util.interaction.StateStorage;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.LinkedList;

public class ActionManager {
    private final static int MAX_ACTIONS_PER_TICK = 5;
    private static int currentActions = 0;

    private final static MinecraftClient mc = MinecraftClient.getInstance();

    private final static LinkedList<Action> actions = new LinkedList<>();

    private static float[] trailingRotation = null;
    public static Runnable trailingBreakAction = null;
    public static Runnable trailingPlaceAction = null;

    public static boolean full() {
        return currentActions >= MAX_ACTIONS_PER_TICK;
    }

    public static void setTrailingRotation(float[] rotation) {
        trailingRotation = rotation;
    }

    public static boolean add(Action action) {
        if (currentActions >= MAX_ACTIONS_PER_TICK) return false;

        actions.add(action);
        currentActions++;
        return true;
    }

    public static void forceAdd(Action action) {
        actions.add(action);
        currentActions++;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUpdateWalkingPlayerHigh(UpdateWalkingPlayerEvent event) {
        Konas.prevYaw = ((ClientPlayerEntityAccessor) mc.player).getLastYaw();
        Konas.prevPitch = ((ClientPlayerEntityAccessor) mc.player).getLastPitch();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        handleActions(event);

        currentActions = 0;
    }

    private void handleActions(UpdateWalkingPlayerEvent event) {
        int startSlot = mc.player.inventory.selectedSlot;

        boolean ranAction = false;

        while (!actions.isEmpty()) {
            Action action = actions.pop();
            if (ranAction && action.isOptional()) continue;

            if (action.isRotate()) {
                event.cancel();
                rotate(action.getYaw(), action.getPitch());
            }

            Runnable runnable = action.getAction();
            if (runnable != null) runnable.run();
            ranAction = true;
        }

        if (trailingRotation != null) {
            event.cancel();
            rotate(trailingRotation[0], trailingRotation[1]);
            trailingRotation = null;
        }

        if (trailingBreakAction != null) {
            trailingBreakAction.run();
            trailingBreakAction = null;
        }

        if (trailingPlaceAction != null) {
            trailingPlaceAction.run();
            trailingPlaceAction = null;
        }

        if (mc.player.isSprinting() && StateStorage.syncSprinting) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }

        if (!mc.player.isSneaking() && StateStorage.syncSneaking) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }

        if (mc.player.inventory.selectedSlot != startSlot) {
            mc.player.inventory.selectedSlot = startSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(startSlot));
        }

        StateStorage.syncSneaking = false;
        StateStorage.syncSprinting = false;
    }

    public static boolean isRotating = false;

    private void rotate(float yaw, float pitch) {
        float prevYaw = mc.player.yaw;
        float prevPitch = mc.player.pitch;
        mc.player.yaw = yaw;
        mc.player.pitch = pitch;
        isRotating = true;
        ((ClientPlayerEntityAccessor) mc.player).iSendMovementPackets();
        isRotating = false;
        mc.player.yaw = prevYaw;
        mc.player.pitch = prevPitch;
    }
}
