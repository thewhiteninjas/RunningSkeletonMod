# ElephantGreenScreenEffect (Fabric port - 1.21.11)

This is a Fabric port of the original Forge mod `ElephantGreenScreenEffect`,
updated to target Minecraft **1.21.11** (ported from the 1.21.1 Fabric
build). Functionality is unchanged: the same once-per-second probability
roll, the same 71-frame full-screen animation, the same sound, and the same
`/elephant chance|show|info` client command.

This jump (1.21.1 -> 1.21.11) crosses several real rendering-API rewrites
inside Minecraft itself (1.21.2, 1.21.5, 1.21.6, 1.21.8), so - unlike the
1.20.1 -> 1.21.1 -> 1.21.11 version bumps before it - this is not a purely
mechanical port. Every change below was verified against Mojang's official
mapping for 1.21.11 and Fabric's current API docs/javadocs rather than
assumed from the 1.21.1 source.

## Opening in IntelliJ IDEA

1. Make sure you have a **JDK 21** available (unchanged requirement since
   1.20.5; Loom will use it via the Gradle toolchain even if your IDE
   default is a different version).
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

## What changed vs. the 1.21.1 Fabric build

- **`ResourceLocation` renamed to `Identifier`**: as of 1.21.11, Mojang's own
  official mapping renamed the class historically called `ResourceLocation`
  to `Identifier` (`net.minecraft.resources.Identifier`). Every use in this
  mod (`ModSounds`, `ElephantAnimationManager`) was updated accordingly. The
  API shape is otherwise the same - the constructor is still private, and
  `Identifier.fromNamespaceAndPath(namespace, path)` is still the way to
  build one.
- **`GuiGraphics#blit` signature**: `blit` now takes an explicit render
  pipeline as its first argument (`RenderPipelines.GUI_TEXTURED` for a plain
  textured quad, from `net.minecraft.client.renderer.RenderPipelines`), and
  separates the on-screen size from the source-region size:
  `blit(pipeline, texture, x, y, u, v, width, height, regionWidth,
  regionHeight, textureWidth, textureHeight)`, instead of 1.21.1's
  `blit(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth,
  textureHeight)`. `ElephantAnimationManager` was updated to the new
  argument order and pipeline.
- **`DynamicTexture` constructor**: the old `DynamicTexture(NativeImage)`
  constructor no longer exists - Mojang added GPU debug labelling, so every
  constructor now needs a name/label. `ElephantAnimationManager` now uses
  `new DynamicTexture(() -> debugLabel, nativeImage)`.
- **HUD rendering hook**: Fabric API's `HudRenderCallback` is deprecated
  (superseded by `HudElementRegistry` when Minecraft's HUD rendering was
  rewritten in 1.21.6) and isn't relied on here anymore. The client
  entrypoint now registers the animation's render callback via
  `HudElementRegistry.addLast(Identifier, HudElement)`
  (`net.fabricmc.fabric.api.client.rendering.v1.hud`). The callback itself
  still has the shape `(GuiGraphics guiGraphics, DeltaTracker deltaTracker)`,
  same as on 1.21.1, so `ElephantAnimationManager.onRenderGui` didn't need
  its signature changed - only how it's registered changed.
- **Loom / Fabric Loader / Fabric API / Gradle**: bumped to versions that
  target 1.21.11 - see `gradle.properties`. 1.21.11 is (at the time of
  writing) still an obfuscated Minecraft version. Loom 1.14 introduced a
  dedicated `net.fabricmc.fabric-loom-remap` plugin id specifically for
  obfuscated versions, but only alpha builds are published under that id so
  far - so `build.gradle` uses the classic, stable `fabric-loom` plugin id
  instead, which the Loom team explicitly kept publishing for backwards
  compatibility with obfuscated versions such as 1.21.11. The Gradle wrapper
  was bumped to `9.7.0` - Loom 1.14's published build specifically requires
  Gradle 9.2 or newer (its runtime variant declares
  `org.gradle.plugin.api-version = 9.2.0`), so Gradle 8.x will fail to
  resolve it even though Loom's own docs mention 8.14 support in general.
- **`fabric.mod.json`**: `minecraft` dependency bumped to `~1.21.11`; `java`
  dependency stays `>=21` (Minecraft has required Java 21 since 1.20.5, so
  this didn't change from the 1.21.1 build).

### What did *not* need to change

- `ClientTickEvents.END_CLIENT_TICK`, `ClientCommandRegistrationCallback`
  and `FabricClientCommandSource` are unchanged.
- The Java toolchain stays on Java 21.
- `ElephantClientCommand` needed no changes at all - it never touched
  `ResourceLocation`/`Identifier`, `blit`, or HUD registration.
- `ClientConfig`'s hand-rolled TOML-ish config file reader/writer is
  unaffected - it only uses `FabricLoader` and plain file I/O.

### If you port further

1.21.11 is expected to be the **last obfuscated** Minecraft version - the
version after it (26.1) removes obfuscation entirely, which changes how
Loom itself needs to be configured (a different plugin id/setup beyond just
bumping `minecraft_version`). Don't just bump the version number in
`gradle.properties` past 1.21.11 with this `build.gradle`.

## Versions used

See `gradle.properties`: Fabric Loader `0.16.14`, Fabric API
`0.141.6+1.21.11`, Loom `1.14-SNAPSHOT` (via the classic `fabric-loom`
plugin id), targeting MC `1.21.11` / Java 21, with Gradle `9.7.0`. If
IntelliJ's Gradle sync offers a newer 1.21.11-targeted Fabric API release,
that's safe to take. If, at some later date, `net.fabricmc.fabric-loom-remap`
gets a stable (non-alpha) release, switching to it is also an option, but
isn't required - the classic id works fine for 1.21.11.
