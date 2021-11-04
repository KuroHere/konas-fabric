package com.konasclient.konas.gui.clickgui.frame;

import com.konasclient.konas.gui.clickgui.ClickGUI;
import com.konasclient.konas.module.modules.client.ClickGUIModule;
import com.konasclient.konas.util.render.font.FontRenderWrapper;
import com.konasclient.konas.util.render.gui.GuiRenderWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public class ButtonFrame extends Frame {

    public ButtonFrame() {
        super("HUD Editor", 20F, 100F, 100F, 16.0F);
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        Window window = MinecraftClient.getInstance().getWindow();
        setPosX(window.getFramebufferWidth() - getWidth() - 5F);
        setPosY(window.getFramebufferHeight() - getHeight() - 5F);
        int color = ClickGUIModule.color.getValue().getColor();
        if (ClickGUIModule.hover.getValue() && mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY(), getWidth(), getHeight()))
            color = ClickGUIModule.color.getValue().getColorObject().brighter().hashCode();
        GuiRenderWrapper.drawRect(getPosX() - 2F, getPosY() - 2F, getWidth() + 4, getHeight() + 4, ClickGUIModule.secondary.getValue().getColor());
        GuiRenderWrapper.drawRect(getPosX(), getPosY(), getWidth(), getHeight(), color);
        String s = ClickGUI.isHudEditor() ? "Modules" : "HUD Editor";
        FontRenderWrapper.drawString(s, (int) ((getPosX() + (getWidth() / 2F)) - (FontRenderWrapper.getStringWidth(s) / 2F)), (int) (getPosY() + (getHeight() / 2F) - (FontRenderWrapper.getFontHeight() / 2F)), 0xFFFFFF);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        if (mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY(), getWidth(), getHeight()) && mouseButton == 0) {
            ClickGUI.setHudEditor(!ClickGUI.isHudEditor());
            return true;
        }
        return false;
    }

}