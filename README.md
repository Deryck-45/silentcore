# SilentCore

SilentCore is a player-only notifier mod for Minecraft 1.21.8 (Fabric). It sends action-bar notifications to individual players when certain events happen, designed to avoid triggering anti-cheat systems.

What it notifies you about
- Potion effect pre-timeout (client-side action bar) — warns ~5 seconds before a potion effect expires.
- Health low (client-side action bar) — warns when health drops to ≤25% of max.
- Drowning start/stop (client-side action bar) — alerts when you start drowning or recover.
- Player nearby (client-side action bar) — announces when another player enters your visual range (~48 blocks).

Key features
- Client-side notifications to avoid server-side anticheat flags.
- Per-player toggle — use /silentcore toggle to enable/disable notifications (server persists and syncs setting to client).
- Ready to integrate with Mod Menu / Auto Config + Cloth Config.

Development
- Java 17
- Fabric Loader + Fabric API
- Use the provided Gradle build (Fabric Loom) to build and run:
  - ./gradlew build
  - ./gradlew runClient

Notes
- The server-side SilentCoreMod class handles the /silentcore toggle command and sends the per-player setting to clients via the "silentcore:settings" channel.
- The client-side implementation (com.example.silentcore.client.SilentCoreClient) receives the toggle and performs all notifications locally using action-bar messages.

License: MIT
