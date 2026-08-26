# ElephantGreenScreenEffect (Fabric port)

This is a Fabric port of the original Forge mod `ElephantGreenScreenEffect`,
targeting Minecraft **1.20.1**. Functionality is unchanged: the same
once-per-second probability roll, the same 71-frame full-screen animation,
the same sound, and the same `/elephant chance|show|info` client command.

## Opening in IntelliJ IDEA

1. Make sure you have a **JDK 17** available (Loom will use it via the
   Gradle toolchain even if your IDE default is newer).
2. `File -> Open...` and select this folder (the one containing
   `build.gradle`).
3. Let IntelliJ import it as a Gradle project and let Loom finish its first
   sync (it will download Minecraft, the official Mojang mappings, Fabric
   Loader and Fabric API - this needs an internet connection the first time).
4. Once sync finishes, a `Minecraft Client` run configuration is generated
   automatically by Loom (or run the Gradle task `runClient`).

## Building a jar

```
./gradlew build
```

The output jar is written to `build/libs/`.

## What changed vs. the Forge version

- **Loader / build system**: Forge + ForgeGradle -> Fabric Loader + Fabric
  API + Fabric Loom.
- **Mappings**: kept on the **official Mojang mappings** channel (Loom's
  `officialMojangMappings()`), the same channel the Forge project used, so
  all vanilla class/method names (`ResourceLocation`, `GuiGraphics`,
  `DynamicTexture`, `NativeImage`, `SimpleSoundInstance`, `Minecraft`, ...)
  are identical to the original source - no renaming was needed for vanilla
  API usage.
- **Mod entrypoint**: the Forge `@Mod` constructor became a
  `ClientModInitializer#onInitializeClient()` (`ElephantGreenScreenEffectClient`).
  The mod is declared `"environment": "client"` in `fabric.mod.json`,
  matching the Forge `side="CLIENT"` dependency declarations.
- **Metadata**: `META-INF/mods.toml` -> `fabric.mod.json`.
- **Sound registration**: Forge's `DeferredRegister`/`RegistryObject` ->
  a plain `Registry.register(BuiltInRegistries.SOUND_EVENT, ...)` call in
  `ModSounds.register()`, called from the client entrypoint.
- **Tick handling**: `TickEvent.ClientTickEvent` (Forge event bus) ->
  Fabric API's `ClientTickEvents.END_CLIENT_TICK`.
- **HUD rendering**: `RenderGuiEvent.Post` (Forge) -> Fabric API's
  `HudRenderCallback`. Same `GuiGraphics.blit(...)` call, same stretched
  full-screen draw.
- **Client command**: `RegisterClientCommandsEvent` (Forge) -> Fabric API's
  `ClientCommandRegistrationCallback` (client-command v2 module), using
  `FabricClientCommandSource` instead of `CommandSourceStack`. The command
  tree, argument name (`number`), and all feedback/error strings are
  unchanged.
- **Config**: Forge's `ForgeConfigSpec` isn't available on Fabric, so
  `ClientConfig` now reads/writes
  `config/elephantgreenscreeneffect-client.toml` by hand. Same file name,
  same `[general]` section, same `chance` key, same default (`2000`) and
  same lower bound (`1`).
- **Resources** (`assets/elephantgreenscreeneffect/textures/elephant/*.png`,
  `sounds/elephant.ogg`, `sounds.json`, `pack.mcmeta`) were copied over
  unmodified.

## Versions used

See `gradle.properties`: Fabric Loader `0.15.11`, Fabric API
`0.92.2+1.20.1`, Loom `1.6-SNAPSHOT`. If IntelliJ's Gradle sync offers
newer 1.20.1-compatible releases of Loader/Fabric API, it's safe to bump
them - this mod only relies on the long-stable `ClientTickEvents`,
`HudRenderCallback` and client-command v2 APIs.
