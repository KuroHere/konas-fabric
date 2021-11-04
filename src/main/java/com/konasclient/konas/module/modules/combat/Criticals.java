package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.setting.Setting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {
    private final Setting<Boolean> strict = new Setting<>("NCPStrict", false);
    private final Setting<Boolean> onlyAura = new Setting<>("OnlyAura", false);

    public Criticals() {
        super("criticals", "Attempts to crit entities", 0xFFE6982A, Category.Combat);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerInteractEntityC2SPacket && ((PlayerInteractEntityC2SPacket) event.packet).getType() == PlayerInteractEntityC2SPacket.InteractionType.ATTACK) {
            if (mc.player.isOnGround() && !mc.player.isInLava() && !mc.player.isSubmergedInWater()) {
                if (onlyAura.getValue() && (!ModuleManager.get(Aura.class).isActive() || ((Aura) ModuleManager.get(Aura.class)).getTarget() == null || ((PlayerInteractEntityC2SPacket) event.packet).getEntity(mc.world) != ((Aura) ModuleManager.get(Aura.class)).getTarget())) return;
                if (strict.getValue() && mc.world.getBlockState(mc.player.getBlockPos()).getBlock() != Blocks.COBWEB) {
                   mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + 0.11D, mc.player.getZ(), false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + 0.1100013579D, mc.player.getZ(), false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + 0.0000013579D, mc.player.getZ(), false));
                } else {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() + 0.0625D, mc.player.getZ(), false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));

                }
            }
        }
    }
}
