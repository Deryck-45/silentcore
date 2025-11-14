package com.example.silentcore.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Helper to send server->client settings for SilentCore.
 * Use NetworkHandler.sendToggleState(player, enabled) from server code (e.g. when a player toggles or on join).
 */
public final class NetworkHandler {
    public static final Identifier SETTINGS_CHANNEL = new Identifier("silentcore", "settings");

    private NetworkHandler() {}

    public static void sendToggleState(ServerPlayerEntity player, boolean enabled) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(enabled);
        ServerPlayNetworking.send(player, SETTINGS_CHANNEL, buf);
    }
}