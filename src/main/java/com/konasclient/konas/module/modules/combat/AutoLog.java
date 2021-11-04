package com.konasclient.konas.module.modules.combat;

import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.friend.Friends;
import com.konasclient.konas.util.math.DamageCalculator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class AutoLog extends Module {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Health);
    private final Setting<Float> health = new Setting<>("Health", 10f, 22f, 0f, 0.1f).withVisibility(() -> mode.getValue() == Mode.Health);
    private final Setting<Float> crystalRange = new Setting<>("CrystalRange", 10f, 15f, 1f, 1f).withVisibility(() -> mode.getValue() == Mode.Crystal);
    private final Setting<Boolean> totem = new Setting<>("IgnoreTotem", true).withVisibility(() -> mode.getValue() != Mode.Player);

    private enum Mode {
        Health, Player, Crystal
    }

    public AutoLog() {
        super("auto-log", "Automatically log out when at risk of dying", 0xFFC29C23, Category.Combat);
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Post event) {
        if (mc.world == null || mc.player == null) return;
        if (mode.getValue() == Mode.Health) {
            if (mc.player.getHealth() <= health.getValue()) {
                if (totem.getValue()) disconnect();
                else if (!hasTotems()) disconnect();
            }
        } else if (mode.getValue() == Mode.Player) {
            for (PlayerEntity e : mc.world.getPlayers()) {
                if (e != mc.player && !Friends.isFriend(e.getEntityName())) {
                    disconnect();
                    break;
                }
            }
        } else {
            if (!totem.getValue() && hasTotems()) return;
            float dmg = 0.0f;


            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof EndCrystalEntity && entity.distanceTo(mc.player) < crystalRange.getValue()) {
                    dmg += DamageCalculator.getExplosionDamage((EndCrystalEntity) entity, mc.player);
                }
            }

            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= dmg) disconnect();
        }
    }

    private boolean hasTotems() {
        for (int slot = 0; slot < 36; slot++) {
            if (mc.player.inventory.getStack(slot).getItem() == Items.TOTEM_OF_UNDYING) return true;
        }
        return false;
    }

    private void disconnect() {
        /*
        AutoReconnect module = (AutoReconnect) ModuleManager.getModuleByClass(AutoReconnect.class);
        if (module != null && module.isEnabled()) module.toggle();
         */
        toggle();
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(1000)); // AutoLog Bypass for L2 and 9B9T
    }
}
