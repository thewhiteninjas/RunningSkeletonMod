# ElephantGreenScreenEffect (Fabric port - 1.21.1)

This is a Fabric port of the original Forge mod `ElephantGreenScreenEffect`,
updated to target Minecraft **1.21.1** (ported from the 1.20.1 Fabric build).
Functionality is unchanged: the same once-per-second probability roll, the
same 71-frame full-screen animation, the same sound, and the same
`/elephant chance|show|info` client command.

## Opening in IntelliJ IDEA

1. Make sure you have a **JDK 21** available (Minecraft 1.20.5+/1.21.x
   require Java 21; Loom will use it via the Gradle toolchain even if your
   IDE default is a different version).
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

## What changed vs. the 1.20.1 Fabric build

Only the changes actually required by the Minecraft/Fabric 1.21.1 API were
made; every gameplay behaviour is identical to the 1.20.1 version.

- **Minecraft / Java version**: `1.20.1` -> `1.21.1`. Minecraft 1.20.5+
  raised the minimum Java version to **21**, so the Gradle toolchain,
  `sourceCompatibility`/`targetCompatibility`, and `JavaCompile.release`
  were all bumped from 17 to 21.
- **`ResourceLocation` construction**: since 1.21, `ResourceLocation`'s
  constructor is `private` - `new ResourceLocation(namespace, path)` no
  longer compiles. Both call sites (`ModSounds.ELEPHANT_SOUND_ID` and the
  per-frame dynamic texture ids in `ElephantAnimationManager`) now use
  `ResourceLocation.fromNamespaceAndPath(namespace, path)`, which is the
  direct, behaviourally-identical replacement.
- **`HudRenderCallback` signature**: on 1.21.1, Fabric API's
  `HudRenderCallback.onHudRender` takes a `net.minecraft.client.DeltaTracker`
  as its second parameter instead of the `float tickDelta` used on 1.20.1.
  `ElephantAnimationManager.onRenderGui` was updated to take a
  `DeltaTracker` accordingly. This value isn't used by the animation logic
  either way - it's timed off wall-clock time (`System.currentTimeMillis()`),
  not partial ticks - so the change is purely a signature update, not a
  behaviour change.
- **Loom / Fabric Loader / Fabric API versions**: bumped to versions that
  target 1.21.1 (`fabric-loom 1.7-SNAPSHOT`, `fabric-loader 0.16.14`,
  `fabric-api 0.115.6+1.21.1`) - see `gradle.properties`.
- **Gradle wrapper**: bumped from `8.1.1` to `8.10` (required by the newer
  Loom version and to run comfortably on a Java 21 toolchain).
- **`fabric.mod.json`**: `minecraft` dependency bumped to `~1.21.1`, `java`
  dependency bumped to `>=21`.

### What did *not* need to change

These are worth calling out because they *did* change in later 1.21.x
releases, but not yet at 1.21.1, so the 1.20.1 code carries over as-is:

- `GuiGraphics.blit(ResourceLocation, x, y, u, v, width, height,
  textureWidth, textureHeight)` keeps its 1.20.1 signature through 1.21.1.
  It's only in **1.21.2** that `blit` starts requiring a
  `Function<ResourceLocation, RenderType>` first argument.
- `DynamicTexture(NativeImage)`'s constructor is unchanged.
- Fabric API's `ClientTickEvents.END_CLIENT_TICK`, `ClientCommandRegistrationCallback`
  and `FabricClientCommandSource` are unchanged.

Because of this, if you later port further (say, to 1.21.6+), expect to
also rework `GuiGraphics.blit`/HUD rendering, since those APIs were
significantly reworked in that range.

## Versions used

See `gradle.properties`: Fabric Loader `0.16.14`, Fabric API
`0.115.6+1.21.1`, Loom `1.7-SNAPSHOT`, targeting MC `1.21.1` / Java 21. If
IntelliJ's Gradle sync offers newer 1.21.1-compatible releases of
Loader/Fabric API, it's safe to bump them - just don't jump to a Fabric
API release built for 1.21.2+, since that's where `GuiGraphics.blit` and
`HudRenderCallback` start requiring code changes (see above).
