package com.konasclient.konas.gui.clickgui.component;

import com.konasclient.konas.Konas;
import com.konasclient.konas.module.modules.client.ClickGUIModule;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.setting.SubBind;
import com.konasclient.konas.util.StringUtils;
import com.konasclient.konas.util.render.font.FontRenderWrapper;
import com.konasclient.konas.util.render.gui.GuiRenderWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class SubBindSettingComponent extends Component {
    private final Setting setting;

    private boolean binding;

    public SubBindSettingComponent(Setting setting, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(setting.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.setting = setting;
        Konas.EVENT_BUS.subscribe(this);
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        GuiRenderWrapper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), getHeight(), ClickGUIModule.color.getValue().getColor());
        int color = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight()) ? new Color(96, 96, 96, 100).hashCode() : ClickGUIModule.secondary.getValue().getColor();
        GuiRenderWrapper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);
        FontRenderWrapper.drawStringWithShadow(isBinding() ? "Press new bind..." : (getName() + ": " + StringUtils.getKeyName(((SubBind) setting.getValue()).getKeyCode())), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (FontRenderWrapper.getStringHeight(isBinding() ? "Press new bind..." : (getName() + ": " + StringUtils.getKeyName(((SubBind) setting.getValue()).getKeyCode()))) / 2) - 0.5F), 0xFFFFFFFF);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!binding) return;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) keyCode = -1;

        ((SubBind) setting.getValue()).setKeyCode(keyCode);
        setBinding(false);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (binding) {
            if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT && mouseButton != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                ((SubBind) setting.getValue()).setKeyCode(mouseButton);
            }
            setBinding(false);
            return true;
        } else if (mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight())) {
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                setBinding(true);
                return true;
            }

            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ((SubBind) setting.getValue()).setKeyCode(mouseButton);
                return true;
            }
        }
        return false;
    }

    public boolean isBinding() {
        return binding;
    }

    public void setBinding(boolean binding) {
        Konas.clickGUI.setBinding(this.binding = binding);
    }

    public Setting getSetting() {
        return setting;
    }
}
