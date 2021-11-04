package com.konasclient.konas.gui.clickgui.component;

import com.konasclient.konas.Konas;
import com.konasclient.konas.module.Module;
import com.konasclient.konas.module.modules.client.ClickGUIModule;
import com.konasclient.konas.util.StringUtils;
import com.konasclient.konas.util.render.font.FontRenderWrapper;
import com.konasclient.konas.util.render.gui.GuiRenderWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class BindSettingComponent extends Component {

    private final Module module;
    private boolean binding;

    public BindSettingComponent(Module module, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(module.getTitle(), parentX, parentY, offsetX, offsetY, width, height);
        this.module = module;
        Konas.EVENT_BUS.subscribe(this);
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        GuiRenderWrapper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), getHeight(), ClickGUIModule.color.getValue().getColor());
        int color = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight()) ? new Color(96, 96, 96, 100).hashCode() : ClickGUIModule.secondary.getValue().getColor();
        GuiRenderWrapper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);
        FontRenderWrapper.drawStringWithShadow(binding ? "Press new bind..." : ((module.isHold() ? "Hold: " : "Bind: ") + StringUtils.getKeyName(module.getKeybind())), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (FontRenderWrapper.getStringHeight(binding ? "Press new bind..." : ("Bind: " + StringUtils.getKeyName(module.getKeybind()))) / 2) - 0.5F), 0xFFFFFFFF);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!binding) return;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) keyCode = -1;

        module.setKeybind(keyCode);
        setBinding(false);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (binding) {
            if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT && mouseButton != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                module.setKeybind(mouseButton);
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
                module.setHold(!module.isHold());
                return true;
            }
        }
        return false;
    }

    public void setBinding(boolean binding) {
        Konas.clickGUI.setBinding(this.binding = binding);
    }

}