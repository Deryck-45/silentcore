-Silent Core — Minimal Fabric Mod

Overview
- Minimal Fabric mod scaffold that logs a message on initialization.
- Java 17, Fabric Loader, Fabric API. Adjust versions in `build.gradle`/`gradle.properties` as needed. This scaffold targets Minecraft `1.21.8`.

Setup (Windows / PowerShell)
1. Install JDK 17 and set `JAVA_HOME`.
2. Install Gradle or generate a wrapper:

gradle wrapper
```powershell
# from project root
gradle wrapper
```

3. Build and run in the Minecraft dev environment:

```powershell
# generate IDE runs & run client (using wrapper if created)
./gradlew genSources
./gradlew runClient

# or build JAR
./gradlew build
# artifact at build/libs/silentcore-1.0.0.jar
```

Notes & next steps
- Change Minecraft/loader/Fabric-API versions in `build.gradle` and `gradle.properties` to match your target.
- Add content: items, blocks, commands, or event listeners in `src/main/java`.
- Want Forge instead of Fabric, or a specific feature (item, block, command)? Tell me which and I'll add it.
