package com.konasclient.konas.util.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;

public class EntityUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static int getPing(PlayerEntity player) {
        if (mc.getNetworkHandler() == null) return 0;

        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (playerListEntry == null) {
            return 0;
        }
        return playerListEntry.getLatency();
    }

}
