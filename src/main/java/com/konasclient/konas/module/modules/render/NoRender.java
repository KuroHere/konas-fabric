package com.konasclient.konas.module.modules.render;

import com.konasclient.konas.event.events.render.*;
import com.konasclient.konas.event.events.sound.SoundEvent;
import com.konasclient.konas.event.events.world.RecalculateSkylightEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Parent;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.ThreadUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.ElderGuardianAppearanceParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class NoRender extends Module {
    private final Setting<Boolean> explosions = new Setting<>("Explosions", true);

    private final Setting<Parent> armor = new Setting<>("Armor", new Parent(false));
    private final Setting<Boolean> head = new Setting<>("Head", false).withParent(armor);
    private final Setting<Boolean> chest = new Setting<>("Chest", false).withParent(armor);
    private final Setting<Boolean> legs = new Setting<>("Legs", false).withParent(armor);
    private final Setting<Boolean> feet = new Setting<>("Feet", false).withParent(armor);

    private final Setting<TotemMode> totems = new Setting<>("Totems", TotemMode.Effect);

    private final Setting<Boolean> bossBar = new Setting<>("BossBar", true);

    public static final Setting<Boolean> worldBorder = new Setting<>("WorldBorder", true);

    private final Setting<Boolean> hurt = new Setting<>("Hurt", true);

    private final Setting<Boolean> fire = new Setting<>("Fire", true);
    private final Setting<Boolean> liquid = new Setting<>("Liquid", true);
    private final Setting<Boolean> pumpkin = new Setting<>("Pumpkin", true);

    private final Setting<Boolean> blindness = new Setting<>("Blindness", true);
    private final Setting<Boolean> nausea = new Setting<>("Nausea", true);
    private final Setting<Boolean> elderGuardian = new Setting<>("ElderGuardian", false);

    private final Setting<Boolean> armorStands = new Setting<>("ArmorStands", false);
    private final Setting<Boolean> fireworks = new Setting<>("Fireworks", false);
    private final Setting<Boolean> gravity = new Setting<>("Gravity", false);
    private final Setting<Boolean> skyLight = new Setting<>("Skylight", false);

    private final Setting<Boolean> maps = new Setting<>("Maps", false);
    private final Setting<Boolean> signs = new Setting<>("Signs", false);

    private final Setting<Boolean> campFire = new Setting<>("CampFire", true);

    private enum TotemMode {
        None, Effect, Sound, Both
    }

    public NoRender() {
        super("no-render", "Prevent certain things from rendering", 0xFF4A67BB, Category.Render);
    }

    @EventHandler
    public void onDrawMap(DrawMapEvent event) {
        if (maps.getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onSoundEvent(SoundEvent event) {
        if (event.instance.getId().getPath().equals("item.totem.use") && (totems.getValue() == TotemMode.Both || totems.getValue() == TotemMode.Sound)) {
            event.cancel();
        } else if (event.instance.getId().getPath().equals("entity.elder_guardian.curse") && elderGuardian.getValue()) {
            event.cancel();
        }
    }

    private Text[] signText = new Text[4];

    @EventHandler
    public void onBlockEntityRender(BlockEntityRenderEvent.Pre event) {
        if (signs.getValue() && event.blockEntity instanceof SignBlockEntity) {
            for (int i = 0; i < 4; i++) {
                signText[i] = ((SignBlockEntity) event.blockEntity).getTextOnRow(i);
                ((SignBlockEntity) event.blockEntity).setTextOnRow(i, LiteralText.EMPTY);
            }
        }
    }

    @EventHandler
    public void onBlockEntityRender(BlockEntityRenderEvent.Post event) {
        if (signs.getValue() && event.blockEntity instanceof SignBlockEntity) {
            for (int i = 0; i < 4; i++) {
                if (signText[i] != null) {
                    ((SignBlockEntity) event.blockEntity).setTextOnRow(i, signText[i]);
                }
            }
        }
    }

    @EventHandler
    public void onRecalculateSkylight(RecalculateSkylightEvent event) {
        if (skyLight.getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onEntityRender(EntityRenderEvent.Pre event) {
        if ((event.entity instanceof ArmorStandEntity && armorStands.getValue()) || (event.entity instanceof FallingBlockEntity && gravity.getValue()) || (event.entity instanceof FireworkRocketEntity && fireworks.getValue())) {
            event.cancel();
        }
    }

    @EventHandler
    public void onRenderArmor(RenderArmorEvent event) {
        if ((event.equipmentSlot == EquipmentSlot.HEAD && head.getValue()) || (event.equipmentSlot == EquipmentSlot.CHEST && chest.getValue()) || (event.equipmentSlot == EquipmentSlot.LEGS && legs.getValue()) || (event.equipmentSlot == EquipmentSlot.FEET && feet.getValue())) {
            event.cancel();
        }
    }

    @EventHandler
    public void onRenderNauseaWobble(RenderNauseaWobbleEvent event) {
        if (nausea.getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onShowFloatingItem(ShowFloatingItemEvent event) {
        if (event.floatingItem.getItem() == Items.TOTEM_OF_UNDYING && (totems.getValue() == TotemMode.Both || totems.getValue() == TotemMode.Effect)) {
            event.cancel();
        }
    }

    @EventHandler
    public void onHurtBob(HurtBobEvent event) {
        if (hurt.getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onRenderOverlay(RenderOverlayEvent event) {
        if ((event.type == RenderOverlayEvent.Type.Fire && fire.getValue()) || (event.type == RenderOverlayEvent.Type.Pumpkin && pumpkin.getValue()) || (event.type == RenderOverlayEvent.Type.Liquid && liquid.getValue())) {
            event.cancel();
        }
    }

    @EventHandler
    public void onRenderBossBarEvent(RenderBossBarEvent event) {
        if (bossBar.getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onRenderStatusEffectBackground(RenderStatusEffectBackgroundEvent event) {
        if (event.effect == StatusEffects.BLINDNESS && blindness.getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onParticle(ParticleEvent.AddParticle event) {
        if (!ThreadUtils.canUpdate()) return;
        if (elderGuardian.getValue() && event.particle instanceof ElderGuardianAppearanceParticle) {
            event.cancel();
        } else if (explosions.getValue() && event.particle instanceof ExplosionLargeParticle) {
            event.cancel();
        } else if (campFire.getValue() && event.particle instanceof CampfireSmokeParticle) {
            event.cancel();
        } else if (fireworks.getValue() && (event.particle instanceof FireworksSparkParticle.FireworkParticle || event.particle instanceof FireworksSparkParticle.Flash)) {
            event.cancel();
        }
    }

    @EventHandler
    public void onParticle(ParticleEvent.AddEmmiter event) {
        if (!ThreadUtils.canUpdate()) return;
        if ((totems.getValue() == TotemMode.Both || totems.getValue() == TotemMode.Effect) && event.emmiter.getType() == ParticleTypes.TOTEM_OF_UNDYING) {
            event.cancel();
        }
    }
}
