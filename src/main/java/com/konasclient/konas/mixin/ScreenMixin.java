package com.konasclient.konas.mixin;

import com.konasclient.konas.module.ModuleManager;
import com.konasclient.konas.module.modules.render.Tooltips;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

import static com.konasclient.konas.module.modules.render.Tooltips.hasItems;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @ModifyArgs(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;II)V"))
    private void getList(Args args, MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
        Tooltips tooltips = (Tooltips) ModuleManager.get(Tooltips.class);

        if (hasItems(itemStack) && tooltips.previewStorage() || (itemStack.getItem() == Items.ENDER_CHEST && tooltips.previewEChest())) {
            List<Text> lines = args.get(1);

            int yChanged = y - 4;
            yChanged -= 10 * lines.size();

            args.set(3, yChanged);
        }
    }

}