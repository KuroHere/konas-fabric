package com.konasclient.konas.gui.clickgui.component;

import com.konasclient.konas.module.modules.client.ClickGUIModule;
import com.konasclient.konas.setting.Setting;
import com.konasclient.konas.util.render.font.FontRenderWrapper;
import com.konasclient.konas.util.render.gui.GuiRenderWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.awt.*;

public class EnumSettingComponent extends Component {

    private final Setting<Enum<?>> enumSetting;

    public EnumSettingComponent(Setting<Enum<?>> enumSetting, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(enumSetting.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.enumSetting = enumSetting;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        int color = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight()) ? new Color(96, 96, 96, 100).hashCode() : ClickGUIModule.secondary.getValue().getColor();
        GuiRenderWrapper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);
        FontRenderWrapper.drawStringWithShadow(getName(), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (FontRenderWrapper.getStringHeight(getName()) / 2)), ClickGUIModule.font.getValue().getColor());
        GuiRenderWrapper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), getHeight(), ClickGUIModule.color.getValue().getColor());
        FontRenderWrapper.drawStringWithShadow(" " + getEnumSetting().getValue().name(), (int) (getAbsoluteX() + 5.0F + FontRenderWrapper.getStringWidth(getName())), (int) (getAbsoluteY() + getHeight() / 2.0F - (FontRenderWrapper.getStringHeight(getEnumSetting().getValue().toString()) / 2)), ClickGUIModule.color.getValue().getColor());
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        boolean withinBounds = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

        boolean forward = mouseX > getAbsoluteX() + getWidth() / 2;

        if (withinBounds && mouseButton == 0) {
            int i = getEnumSetting().getEnum(getEnumSetting().getValue().toString());

            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (forward) {
                if (isIndexInBounds(i + 1)) {
                    getEnumSetting().setValue(getEnumSetting().getValue().getClass().getEnumConstants()[i + 1]);
                } else {
                    getEnumSetting().setValue(getEnumSetting().getValue().getClass().getEnumConstants()[0]);
                }
            } else {
                if (isIndexInBounds(i - 1)) {
                    getEnumSetting().setValue(getEnumSetting().getValue().getClass().getEnumConstants()[i - 1]);
                } else {
                    getEnumSetting().setValue(getEnumSetting().getValue().getClass().getEnumConstants()[getEnumSetting().getValue().getClass().getEnumConstants().length - 1]);
                }
            }
            return true;
        }
        return false;
    }

    private boolean isIndexInBounds(int index) {
        return index <= getEnumSetting().getValue().getClass().getEnumConstants().length - 1 && index >= 0;
    }

    public Setting<Enum<?>> getEnumSetting() {
        return enumSetting;
    }
}
