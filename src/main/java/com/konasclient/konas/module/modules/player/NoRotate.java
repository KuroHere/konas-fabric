package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.mixin.PlayerPositionLookS2CPacketAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class NoRotate extends Module {
    public static final Setting<Boolean> strict = new Setting<>("Strict", false);

    public NoRotate() {
        super("no-rotate", "Cancels server to client rotations", 0xFFD7995D, Category.Player);
    }

    @EventHandler
    public void onReceive(PacketEvent.Receive event) {
        if (strict.getValue()) return;
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            if (!(mc.currentScreen instanceof DownloadingTerrainScreen)) {
                PlayerPositionLookS2CPacket packet = (PlayerPositionLookS2CPacket) event.packet;
                ((PlayerPositionLookS2CPacketAccessor) packet).setYaw(mc.player.yaw);
                ((PlayerPositionLookS2CPacketAccessor) packet).setPitch(mc.player.pitch);
                packet.getFlags().remove(PlayerPositionLookS2CPacket.Flag.X_ROT);
                packet.getFlags().remove(PlayerPositionLookS2CPacket.Flag.Y_ROT);
            }
        }

    }

}
