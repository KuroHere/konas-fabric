package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.mixin.PlayerMoveC2SPacketAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module {

    public static Setting<Boolean> sprint = new Setting<>("Sprint", true);
    public static Setting<Boolean> noGround = new Setting<>("Ground", true);

    private boolean isOnGround = false;

    public AntiHunger() {
        super("anti-hunger", "Prevents hunger loss", 0xFFDE73CB, Category.Player, "NoHunger");
    }

    public void onEnable() {
        if (sprint.getValue() && mc.player != null) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }
    }

    public void onDisable() {
        if (sprint.getValue() && mc.player != null && mc.player.isSprinting()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof ClientCommandC2SPacket) {
            ClientCommandC2SPacket mode = (ClientCommandC2SPacket) event.packet;
            if (sprint.getValue() && (mode.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING || mode.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING)) {
                event.setCancelled(true);
            }
        }

        if (event.packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket player = (PlayerMoveC2SPacket) event.packet;
            boolean ground = mc.player.isOnGround();
            if (noGround.getValue() && isOnGround && ground && player.getY(0.0) == (!((PlayerMoveC2SPacketAccessor) player).isMoving() ? 0.0 : mc.player.getPos().y)) {
                ((PlayerMoveC2SPacketAccessor) player).setOnGround(false);
            }
            isOnGround = ground;
        }
    }
}
