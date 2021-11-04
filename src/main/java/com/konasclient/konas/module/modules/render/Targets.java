package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.entity.TotemPopEvent;
import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.util.friend.Friends;
import com.konasclient.konas.util.math.InterpolationUtil;
import com.konasclient.konas.util.render.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Targets {
    private static Map<Entity, Long> targets = new ConcurrentHashMap<>();

    public static void addTarget(Entity target) {
        targets.put(target, System.currentTimeMillis());
    }

    public static void removeTarget(Entity target) {
        targets.remove(target);
    }

    public static Set<Entity> getTargets() {
        return targets.keySet();
    }

    public static boolean isTarget(Entity suspect) {
        if (targets.containsKey(suspect)) {
            return true;
        }
        return false;
    }

    public static long getTargetTime(String playerName) {
        for (Entity target : getTargets()) {
            if (target.getName().getString().equalsIgnoreCase(playerName)) return targets.get(target);
        }
        return -1L;
    }

    private static final String[] devs = new String[]{"Speaking", "Konas", "antiflame", "Darkii", "LittleDraily", "TBM_", "blockparole", "seasnail8192"};
    private static final String[] coolKids = new String[]{"spartan4200", "25__", "Soulbond", "0851_", "samtheclam7000", "R41F", "Not_Daisy"};

    public static HashMap<String, Integer> popList = new HashMap<>();

    public static int mixWithTargetColor(String playerName, float red, float green, float blue) {
        long targetTime = getTargetTime(playerName);
        if (targetTime > -1L) {
            float diff = (System.currentTimeMillis() - targetTime) / 30000F;
            if (diff > 1F) diff = 1F; else if (diff < 0F) diff = 0F;
            float newRed = InterpolationUtil.lerp(1F, red, diff);
            float newGreen = InterpolationUtil.lerp(0F, green, diff);
            float newBlue = InterpolationUtil.lerp(0F, blue, diff);
            return Color.fromRGBA((int) (newRed * 255), (int) (newGreen * 255), (int) (newBlue * 255), 255);
        }
        return Color.fromRGBA((int) (red * 255), (int) (green * 255), (int) (blue * 255), 255);
    }

    public static int getTargetFontColor(String playerName) {
        if (Friends.isFriend(playerName)) {
            return 0xFF27CC00;
        }
        for (String dev : devs) {
            if (dev.equals(playerName)) return 0xFF8200C8;
        }
        for (String coolKid : coolKids) {
            if (coolKid.equals(playerName)) return 0xFFE700FF;
        }
        long targetTime = getTargetTime(playerName);
        if (targetTime > -1L) {
            float diff = (System.currentTimeMillis() - targetTime) / 30000F;
            if (diff > 1F) diff = 1F; else if (diff < 0F) diff = 0F;
            float value = InterpolationUtil.lerp(0F, 1F, diff);
            return Color.fromRGBA(255, (int) (value * 255), (int) (value * 255), 255);
        }
        return 0xFFFFFFFF;
    }

    @EventHandler
    public void onPlayerUpdate(UpdateEvent.Pre event) {
        refreshTargets();
    }

    public static void refreshTargets() {
        targets.forEach((entity, time) -> {
            if (System.currentTimeMillis() - time > TimeUnit.SECONDS.toMillis(30L)) {
                targets.remove(entity);
            }
        });
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) return;

        if (event.packet instanceof EntityStatusS2CPacket) {
            EntityStatusS2CPacket packet = (EntityStatusS2CPacket) event.packet;
            if (packet.getStatus() == 35) {
                Entity entity = packet.getEntity(MinecraftClient.getInstance().world);
                if (popList == null) {
                    popList = new HashMap<>();
                }

                if (popList.get(entity.getEntityName()) == null) {
                    popList.put(entity.getEntityName(), 1);
                } else if (popList.get(entity.getEntityName()) != null) {
                    popList.put(entity.getEntityName(),  popList.get(entity.getEntityName()) + 1);
                }

                // Has to be instance
                TotemPopEvent totemPopEvent = new TotemPopEvent(entity, popList.get(entity.getEntityName()));
                Konas.EVENT_BUS.post(totemPopEvent);
            }
        }

    }

    @EventHandler
    public void onUpdate(UpdateEvent.Pre event) {
        for (PlayerEntity player : MinecraftClient.getInstance().world.getPlayers()) {
            if (player.getHealth() <= 0
                    && popList.containsKey(player.getName())) {
                popList.remove(player.getName(), popList.get(player.getName()));
            }
        }
    }
}
