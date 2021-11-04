package com.konasclient.konas.mixin;

import com.konasclient.konas.Konas;
import com.konasclient.konas.command.CommandManager;
import com.konasclient.konas.event.events.player.ItemSlowdownEvent;
import com.konasclient.konas.event.events.player.PlayerMoveEvent;
import com.konasclient.konas.event.events.player.PushOutOfBlocksEvent;
import com.konasclient.konas.event.events.player.UpdateWalkingPlayerEvent;
import com.konasclient.konas.util.action.ActionManager;
import com.konasclient.konas.util.chat.Chat;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayerEntity.class, priority = Integer.MAX_VALUE)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double d, CallbackInfo ci) {
        PushOutOfBlocksEvent event = Konas.postEvent(PushOutOfBlocksEvent.get());
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    public void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        ci.cancel();

        PlayerMoveEvent event = Konas.postEvent(PlayerMoveEvent.get(type, movement));

        if (!event.isCancelled()) {
            super.move(event.type, event.movement);
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void onSendMovementPackets(CallbackInfo ci) {
        if (ActionManager.isRotating) return;

        UpdateWalkingPlayerEvent event = UpdateWalkingPlayerEvent.get();

        Konas.postEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable = true)
    private void onSendChatMessage(String msg, CallbackInfo info) {
        if (msg.startsWith(Konas.PREFIX)) {
            try {
                CommandManager.dispatch(msg.substring(Konas.PREFIX.length()));
            } catch (CommandSyntaxException e) {
                Chat.error(e.getMessage());
            }
            info.cancel();
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean onUsingItemCheck(ClientPlayerEntity player) {
        ItemSlowdownEvent event = Konas.postEvent(ItemSlowdownEvent.get());
        if (event.isCancelled()) return false;
        return player.isUsingItem();
    }

}
