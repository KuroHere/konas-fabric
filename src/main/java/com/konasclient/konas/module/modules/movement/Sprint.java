package com.konasclient.konas.module.modules.movement;


import com.konasclient.konas.event.events.player.JumpEvent;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.client.ThreadUtils;
import com.konasclient.konas.util.entity.PlayerUtils;
import meteordevelopment.orbit.EventHandler;

public class Sprint extends Module {

    private static final Setting<Mode> mode = new Setting<>("Mode", Mode.LEGIT);
    //private static final Setting<Boolean> jump = new Setting<>("Jump", false).withVisibility(() -> mode.getValue() == Mode.RAGE);
    private static final Setting<Boolean> whenStatic = new Setting<>("Static", false);
    public Sprint() {
        super("sprint", "Makes you Sprint!", 0xFFE0C39B, Category.Movement);
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Pre event) {
        if (!ThreadUtils.canUpdate()) return;
        if (whenStatic.getValue()) {
            mc.player.setSprinting(true);
            return;
        }

        if (mc.player.isSneaking() || mc.player.horizontalCollision) return;

        switch (mode.getValue()) {
            case LEGIT:
                mc.player.setSprinting(mc.player.input.movementForward > 0);
                break;
            case RAGE:
                mc.player.setSprinting(mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0);
                break;
        }
    }

    /*@EventHandler
    public void onJump(JumpEvent event) {
        if (mc.player.isSneaking() || mc.player.horizontalCollision) return;

        if (mode.getValue() == Mode.RAGE && jump.getValue() && (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0)) {
            mc.player.setSprinting(true);
            event.yaw = (float) Math.toDegrees(PlayerUtils.movementYaw());
            System.out.println("lol " + event.yaw);
        }
    }*/

    private enum Mode {
        LEGIT,
        RAGE
    }
}