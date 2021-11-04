package com.konasclient.konas.module.modules.client;

import com.konasclient.konas.Konas;
import com.konasclient.konas.event.events.world.UpdateEvent;
import com.konasclient.konas.module.Category;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.setting.ColorSetting;
import com.konasclient.konas.setting.IRunnable;
import com.konasclient.konas.setting.ListenableSettingDecorator;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.render.font.FontRenderWrapper;
import com.konasclient.konas.util.render.font.CustomFontRenderer;
import com.konasclient.konas.util.render.font.DefaultFontRenderer;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

public class ClickGUIModule extends Module {
    public static Setting<Boolean> binds = new Setting<>("Binds", false);

    public static Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0xFFBA15BA));
    public static Setting<ColorSetting> font = new Setting<>("Font", new ColorSetting(0xFFFFFFFF));
    public static Setting<ColorSetting> secondary = new Setting<>("Secondary", new ColorSetting(0xFF000000));
    public static Setting<ColorSetting> header = new Setting<>("Header", new ColorSetting(0xDD000000));
    public static Setting<ColorSetting> background = new Setting<>("Background", new ColorSetting(0xDD000000));

    public static Setting<ColorSetting> overlayTop = new Setting<>("OverlayTop", new ColorSetting(-0x73EFEFF0));
    public static Setting<ColorSetting> overlay = new Setting<>("Overlay", new ColorSetting(-0x73EFEFF0));

    public static Setting<Boolean> hover = new Setting<>("Hover", true);
    public static Setting<Boolean> animate = new Setting<>("Animate", true);
    public static Setting<Boolean> outline = new Setting<>("Outline", false).withVisibility(() -> !animate.getValue());
    public static Setting<Integer> thickness = new Setting<>("Thickness", 1, 5, 1, 1).withVisibility(() -> !animate.getValue() && outline.getValue());
    public static Setting<Integer> animationSpeed = new Setting<>("AnimationSpeed", 10, 20, 1, 1).withVisibility(animate::getValue);
    public static CustomFontRenderer customFontRenderer;

    public static int clipBoard = -1;

    public ClickGUIModule() {
        super("click-gui", 0xFFC15DE9, Category.Client);
        setKeybind(GLFW.GLFW_KEY_Y);
    }

    @EventHandler
    public void onUpdate(UpdateEvent.Pre event) {
        if (mc.currentScreen == null) {
            Konas.EVENT_BUS.subscribe(Konas.clickGUI);
            mc.openScreen(Konas.clickGUI);
        }
        toggle();
    }

    @Override
    protected boolean shouldSendToggleMessage() {
        return false;
    }
}