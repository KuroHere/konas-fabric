package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.event.events.player.PlayerPushEvent;
import com.konasclient.konas.event.events.player.PushOutOfBlocksEvent;
import com.konasclient.konas.mixin.EntityVelocityUpdateS2CPacketAccessor;
import com.konasclient.konas.mixin.ExplosionS2CPacketAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.ThreadUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

public class Velocity extends Module {
    private static Setting<Float> horizontal = new Setting<>("Horizontal", 0f, 2f, 0f, 1f);
    private static Setting<Float> vertical = new Setting<>("Vertical", 0f, 2f, 0f, 1f);

    private static Setting<Boolean> knockback = new Setting<>("Knockback", true);
    private static Setting<Boolean> entities = new Setting<>("Entities", true);
    private static Setting<Boolean> explosions = new Setting<>("Explosions", true);
    private static Setting<Boolean> blocks = new Setting<>("Blocks", true);

    public Velocity() {
        super("velocity", "Cancels unwanted velocity", 0xFFA7DE2F, Category.Player);
    }

    @EventHandler
    public void onPushOutOfBlocks(PushOutOfBlocksEvent event) {
        if (!ThreadUtils.canUpdate()) return;
        if (mc.player.age < 5) return;
        if (blocks.getValue()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPush(PlayerPushEvent event) {
        if (!ThreadUtils.canUpdate()) return;
        if (mc.player.age < 5) return;
        if (entities.getValue()) {
            event.movement = new Vec3d(event.movement.x * horizontal.getValue(), event.movement.y * vertical.getValue(), event.movement.z * horizontal.getValue());
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!ThreadUtils.canUpdate()) return;
        if (mc.player.age < 5) return;
        if (event.packet instanceof EntityVelocityUpdateS2CPacket && knockback.getValue()) {
            EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket) event.packet;
            if (packet.getId() == mc.player.getEntityId()) {
                EntityVelocityUpdateS2CPacketAccessor vPacket = (EntityVelocityUpdateS2CPacketAccessor) event.packet;
                vPacket.setVelocityX((int) Math.floor(vPacket.getVelocityX() * horizontal.getValue()));
                vPacket.setVelocityY((int) Math.floor(vPacket.getVelocityY() * vertical.getValue()));
                vPacket.setVelocityZ((int) Math.floor(vPacket.getVelocityZ() * horizontal.getValue()));
            }
        } else if (event.packet instanceof ExplosionS2CPacket && explosions.getValue()) {
            ExplosionS2CPacketAccessor packet = (ExplosionS2CPacketAccessor) event.packet;

            packet.setPlayerVelocityX(packet.getPlayerVelocityX() * horizontal.getValue());
            packet.setPlayerVelocityY(packet.getPlayerVelocityY() * vertical.getValue());
            packet.setPlayerVelocityZ(packet.getPlayerVelocityZ() * horizontal.getValue());
        }
    }
}
