package com.konasclient.konas.module.modules.player;

import com.konasclient.konas.event.events.network.PacketEvent;
import com.konasclient.konas.event.events.player.ItemSlowdownEvent;
import com.konasclient.konas.event.events.player.PlayerMoveEvent;
import com.konasclient.konas.event.events.render.LastPassEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.ThreadUtils;
import com.konasclient.konas.util.client.TimerManager;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class NoSlow extends Module {
    private final Setting<Boolean> strict = new Setting<>("Strict", false);

    private final Setting<Boolean> items = new Setting<>("Items", true);
    private final Setting<Boolean> soulSand = new Setting<>("SoulSand", false);
    private final Setting<Boolean> webs = new Setting<>("Webs", false);
    private final Setting<Boolean> slime = new Setting<>("Slime", false);
    private final Setting<Boolean> ladders = new Setting<>("Ladders", false);
    private final Setting<Boolean> invMove = new Setting<>("Inventories", true);
    private final Setting<Boolean> sneak = new Setting<>("Sneak", true).withVisibility(invMove::getValue);
    private final Setting<Boolean> rotate = new Setting<>("Rotate", true).withVisibility(invMove::getValue);

    public NoSlow() {
        super("no-slow", "Prevents you from slowing down", 0xFFDE73CB, Category.Player);
    }

    private long lastPartialTick = 0L;
    private boolean sneaking = false;

    public void onDisable() {
        TimerManager.resetTimer(this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!ThreadUtils.canUpdate()) return;
        if (soulSand.getValue() && mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.SOUL_SAND) {
            mc.player.setVelocity(mc.player.getVelocity().multiply(2.5, 1, 2.5));
        }

        if (mc.player.isHoldingOntoLadder() && mc.player.getVelocity().y > 0 && ladders.getValue()) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.169, mc.player.getVelocity().z);
        }

        if (webs.getValue() && mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB) {
            float speed = (mc.player.age % 3 == 0) ? 5F : 10F;
            TimerManager.updateTimer(this, 30, speed);
        } else {
            TimerManager.resetTimer(this);
        }

        if (slime.getValue() && mc.world.getBlockState(new BlockPos(mc.player.getPos().add(0, -0.01, 0))).getBlock() == Blocks.SLIME_BLOCK && mc.player.isOnGround()) {
            double dY = Math.abs(mc.player.getVelocity().y);
            if (dY < 0.1D && !mc.player.bypassesSteppingEffects()) {
                double exY = 1 / (0.4D + dY * 0.2D);
                mc.player.setVelocity(mc.player.getVelocity().multiply(exY, 1.0D, exY));
            }
        }
    }

    @EventHandler
    public void onUpdatePre(UpdateEvent.Pre event) {
        if (!ThreadUtils.canUpdate()) return;

        if (!strict.getValue()) return;

        if (mc.player.isUsingItem()) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            sneaking = true;
        } else if (sneaking && !mc.player.isUsingItem()) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            sneaking = false;
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (!ThreadUtils.canUpdate()) return;
        if (strict.getValue() && event.packet instanceof ClickSlotC2SPacket && !sneaking) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            sneaking = true;
        }
    }

    @EventHandler
    public void onItemSlowdown(ItemSlowdownEvent event) {
        if (!ThreadUtils.canUpdate()) return;
        if (items.getValue()) {
            event.cancel();
        }
    }

    @EventHandler
    public void onLastPass(LastPassEvent event) {
        if (!ThreadUtils.canUpdate()) return;
        // Maintain constant rotation speed at different framerates
        float amount = (System.currentTimeMillis() - lastPartialTick) / 5F;
        lastPartialTick = System.currentTimeMillis();

        if (invMove.getValue() && rotate.getValue() && shouldMoveInScreen(mc.currentScreen)) {

            float yaw = 0f;
            float pitch = 0f;

            mc.keyboard.setRepeatEvents(true);

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT))
                yaw -= amount;
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT))
                yaw += amount;
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_UP))
                pitch -= amount;
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN))
                pitch += amount;

            mc.player.yaw += yaw;

            mc.player.pitch = MathHelper.clamp(mc.player.pitch + pitch, -90f, 90f);
        }

        if (invMove.getValue() && shouldMoveInScreen(mc.currentScreen)) {
            for (KeyBinding k : new KeyBinding[] { mc.options.keyForward, mc.options.keyBack, mc.options.keyLeft, mc.options.keyRight, mc.options.keyJump, mc.options.keySprint }) {
                k.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
            }

            if (sneak.getValue()) {
                mc.options.keySneak.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.keySneak.getBoundKeyTranslationKey()).getCode()));
            }
        }
    }

    private boolean shouldMoveInScreen(Screen screen) {
        if (screen == null) {
            return false;
        }

        return !(screen instanceof ChatScreen
                || screen instanceof BookEditScreen
                || screen instanceof SignEditScreen
                || screen instanceof CommandBlockScreen // 2b backdoor?
                || screen instanceof AnvilScreen);
    }
}
