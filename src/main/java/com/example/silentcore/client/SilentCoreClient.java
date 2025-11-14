package com.example.silentcore.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Client-side notification handler for SilentCore.
 * - Action-bar notifications for potion pre-timeout, low health, drowning start/stop, and nearby players.
 * - Receives per-player toggle from the server via "silentcore:settings".
 *
 * Notes:
 * - Wire the PREWARN_SECONDS and thresholds into your Auto Config + Mod Menu if you add that dependency.
 * - Messages use translation keys defined in assets/silentcore/lang/en_us.json.
 */
public class SilentCoreClient implements ClientModInitializer {
    public static final Identifier SETTINGS_CHANNEL = new Identifier("silentcore", "settings");

    // Config defaults (can be replaced by AutoConfig-backed values)
    private static int PREWARN_SECONDS = 5; // warn ~5 seconds before potion expires
    private static double HEALTH_THRESHOLD = 0.25; // 25% of max health
    private static double NEARBY_RANGE = 48.0; // blocks
    private static final int TICK_INTERVAL = 10; // check every 10 ticks (~0.5s)

    private boolean notificationsEnabled = true;

    // Simple state to avoid spam
    private int tickCounter = 0;
    private final Map<String, Integer> lastPotionWarnTick = new HashMap<>();
    private int lastLowHealthTick = 0;
    private boolean wasDrowning = false;
    private final Set<String> visiblePlayers = new HashSet<>();
    private int lastNearbyAnnounceTick = 0;

    @Override
    public void onInitializeClient() {
        // Register packet receiver for toggle state
        ClientPlayNetworking.registerGlobalReceiver(SETTINGS_CHANNEL, (client, handler, buf, responseSender) -> {
            boolean enabled = buf.readBoolean();
            client.execute(() -> this.notificationsEnabled = enabled);
        });

        // Client tick listener
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;
            tickCounter++;
            if (tickCounter % TICK_INTERVAL != 0) return;
            if (!notificationsEnabled) return;

            ClientPlayerEntity player = client.player;
            int now = tickCounter;

            // Potion pre-timeout: warn PREWARN_SECONDS before expiration (client-side only)
            int prewarnTicks = PREWARN_SECONDS * 20;
            for (StatusEffectInstance effect : player.getStatusEffects()) {
                String key = effect.getEffectType().getName().getString() + ":" + effect.hashCode();
                int duration = effect.getDuration();
                if (duration <= prewarnTicks) {
                    if (!lastPotionWarnTick.containsKey(key) || now - lastPotionWarnTick.get(key) > 20 * 10) {
                        player.sendMessage(Text.translatable("silentcore.potion.expiring", effect.getEffectType().getName().getString(), PREWARN_SECONDS), true);
                        lastPotionWarnTick.put(key, now);
                    }
                }
            }

            // Low health (<= HEALTH_THRESHOLD)
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            if (maxHealth > 0) {
                if (health / maxHealth <= HEALTH_THRESHOLD) {
                    if (now - lastLowHealthTick > 20 * 5) { // announce at most every 5s
                        player.sendMessage(Text.translatable("silentcore.health.low", Math.round(health), Math.round(maxHealth)), true);
                        lastLowHealthTick = now;
                    }
                }
            }

            // Drowning start/stop: monitor air (client-side)
            int air = player.getAir();
            boolean isDrowning = air < 10; // threshold: air < 10 ticks means drowning soon/in progress
            if (isDrowning && !wasDrowning) {
                player.sendMessage(Text.translatable("silentcore.drowning.start"), true);
            } else if (!isDrowning && wasDrowning) {
                player.sendMessage(Text.translatable("silentcore.drowning.stop"), true);
            }
            wasDrowning = isDrowning;

            // Nearby players: announce when a new player enters visual range (~NEARBY_RANGE)
            Set<String> nowVisible = new HashSet<>();
            client.world.getPlayers().forEach(p -> {
                if (p.getUuid().equals(player.getUuid())) return;
                if (!p.getWorld().getRegistryKey().equals(player.getWorld().getRegistryKey())) return;
                double dx = p.getX() - player.getX();
                double dy = p.getY() - player.getY();
                double dz = p.getZ() - player.getZ();
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq <= NEARBY_RANGE * NEARBY_RANGE) {
                    nowVisible.add(p.getEntityName());
                }
            });

            for (String name : nowVisible) {
                if (!visiblePlayers.contains(name) && (tickCounter - lastNearbyAnnounceTick) > 20 * 3) { // at most every 3s
                    player.sendMessage(Text.translatable("silentcore.player.nearby", name), true);
                    lastNearbyAnnounceTick = tickCounter;
                }
            }
            visiblePlayers.clear();
            visiblePlayers.addAll(nowVisible);
        });
    }
}