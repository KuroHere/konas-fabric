package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.command.CommandManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestor.class)
public abstract class CommandSuggestorMixin {

    @Shadow private ParseResults<CommandSource> parse;
    @Shadow @Final private TextFieldWidget textField;
    @Shadow private boolean completingSuggestions;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private CommandSuggestor.SuggestionWindow window;

    @Shadow protected abstract void show();

    @Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRefresh(CallbackInfo ci, String string, StringReader reader) {
        String prefix = Konas.PREFIX;
        int length = prefix.length();
        if (reader.canRead(length) && reader.getString().startsWith(prefix, reader.getCursor())) {
            reader.setCursor(reader.getCursor() + length);

            CommandDispatcher<CommandSource> commandDispatcher = CommandManager.getDispatcher();
            if (this.parse == null) this.parse = commandDispatcher.parse(reader, CommandManager.getCommandSource());

            int cursor = textField.getCursor();
            if (cursor >= 1 && (this.window == null || !this.completingSuggestions)) {
                this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.parse, cursor);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.show();
                    }
                });
            }
            ci.cancel();
        }
    }

}