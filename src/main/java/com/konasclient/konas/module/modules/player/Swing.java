package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.mixin.HandSwingC2SPacketAccessor;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

public class Swing extends Module {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.CANCEL);
    private final Setting<Boolean> strict = new Setting<>("Strict", false);
    private final Setting<Boolean> shuffle = new Setting<>("Shuffle", false);

    private enum Mode {
        CANCEL, OFFHAND, MAINHAND, OPPOSITE, NONE
    }

    public Swing() {
        super("swing", 0xFFD04EC3, Category.Player);
    }

    @EventHandler
    public void onPacketSent(PacketEvent.Send event) {
        if(mc.player == null || mc.world == null) return;
        if(shuffle.getValue() && Math.random() > 0.5) {
            return;
        }
        if(event.packet instanceof HandSwingC2SPacket) {
            if (mode.getValue() == Mode.CANCEL) {
                if (!strict.getValue() || mc.interactionManager.isBreakingBlock()) {
                    event.cancel();
                }
            } else if (mode.getValue() == Mode.OFFHAND) {
                HandSwingC2SPacket packet = (HandSwingC2SPacket) event.packet;
                ((HandSwingC2SPacketAccessor) packet).setHand(Hand.OFF_HAND);
            } else if (mode.getValue() == Mode.MAINHAND) {
                HandSwingC2SPacket packet = (HandSwingC2SPacket) event.packet;
                ((HandSwingC2SPacketAccessor) packet).setHand(Hand.MAIN_HAND);
            } else if (mode.getValue() == Mode.OPPOSITE) {
                HandSwingC2SPacket packet = (HandSwingC2SPacket) event.packet;
                ((HandSwingC2SPacketAccessor) packet).setHand(packet.getHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
            }
        }
    }

}
