# RunningSkeleton

A Hytale server plugin that has a small, configurable chance every second of
playing a fullscreen animation of a skeleton running across the player's
screen.

- **Author:** TheWhiteNinja
- **Target SDK:** Hytale SDK 0.5.7
- **License:** MIT

## Description

Every second, the plugin rolls a random check for every online player. By
default there is a **1 in 2000** chance per second, per player, of the check
succeeding. When it does, a fullscreen, 14-frame skeleton animation plays for
that player over exactly 2 seconds and automatically disappears when
finished. After the animation ends, a 5 second cooldown starts for that
player, during which the event cannot trigger again for them, even if the
random check succeeds.

The animation is implemented as a persistent, non-interactive **HUD**
element (the same category of UI as the hotbar or health bar), not as a
dialog/page. This means it never captures mouse or keyboard input, never
pauses or interrupts the player, and stays visible on top of whatever the
player is doing, including menus like the inventory screen.

Rather than relying on the game's built-in sprite-sheet animation system, the
plugin displays your 14 frame images directly, one at a time, on a timer it
controls itself: it shows `frame_01.png`, waits `2000 / 14` milliseconds,
shows `frame_02.png`, and so on through `frame_14.png`, then hides the
overlay. This avoids any ambiguity about sprite-sheet grid layout or
built-in repeat/loop behavior, since each frame is just a plain image shown
for a fixed, exact slice of time, in order, once.

Both the chance and the cooldown are configurable at runtime through commands,
and are persisted to disk.

## Features

- Per-player, per-second random trigger with a configurable 1-in-N chance.
- Fullscreen 14-frame animation, shown frame by frame over an exact 2 second
  duration, that automatically scales to cover the player's screen at any
  resolution.
- Rendered as a non-interactive HUD overlay: it does not block input, does
  not close or change any menu the player has open, and keeps showing even
  while the player has their inventory or another screen open.
- Per-player cooldown that prevents the animation from re-triggering while
  active, even if the random check succeeds again.
- `/skeleton chance <number>` to change the probability at runtime.
- `/skeleton show` to preview the animation immediately, ignoring the random
  chance.
- `/skeleton info` to check the current probability, cooldown duration, and
  whether the cooldown is currently active.
- Configuration is saved to and loaded from a JSON file, so changes persist
  across restarts.

## Folder structure

```
RunningSkeleton/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew, gradlew.bat, gradle/
├── LICENSE
├── README.md
└── src/main/
    ├── java/com/thewhiteninja/runningskeleton/
    │   ├── RunningSkeletonPlugin.java
    │   ├── animation/
    │   │   └── SkeletonAnimationService.java
    │   ├── commands/
    │   │   ├── SkeletonCommand.java
    │   │   ├── ChanceSubCommand.java
    │   │   ├── ShowSubCommand.java
    │   │   └── InfoSubCommand.java
    │   ├── config/
    │   │   └── RunningSkeletonConfig.java
    │   └── ui/
    │       ├── SkeletonRunHud.java
    │       └── BlankSkeletonHud.java
    └── resources/
        ├── manifest.json
        └── Common/UI/Custom/Huds/
            ├── RunningSkeleton_Overlay.ui
            └── Frames/
                ├── frame_01.png   <- you provide these 14 files, see below
                ├── frame_02.png
                ├── ...
                └── frame_14.png
```

## Where to place your PNG frames

You provide 14 individual frames named `frame_01.png` through `frame_14.png`,
each **1024x1024 pixels**. No combining, resizing, or build step is needed:
copy them directly into the plugin's asset pack at

```
RunningSkeleton/src/main/resources/Common/UI/Custom/Huds/Frames/frame_01.png
RunningSkeleton/src/main/resources/Common/UI/Custom/Huds/Frames/frame_02.png
...
RunningSkeleton/src/main/resources/Common/UI/Custom/Huds/Frames/frame_14.png
```

They need to be in place **before** you build, since they are bundled into
the JAR as part of the plugin's asset pack.

## Requirements

- **Hytale SDK / server version:** `>=0.5.7 <0.6.0` (targets 0.5.7).
- **Java:** 25 (matches the toolchain used by the official
  `HytaleModding/plugin-template` and the `com.azuredoom.hytale-tools` Gradle
  plugin). A JetBrains Runtime JDK is recommended for hot-reload/debugging.
- **Gradle:** not required to be installed separately, the Gradle wrapper
  (`gradlew` / `gradlew.bat`) is included.

## Dependencies

- `Hytale:AssetModule` (declared in `manifest.json`, required because the
  plugin ships a Custom UI asset pack alongside the compiled code).
- The [Hytale Gradle Plugin](https://github.com/AzureDoom/Hytale-Gradle-Plugin)
  (`com.azuredoom.hytale-tools`), resolved automatically from the AzureDoom
  Maven repository configured in `settings.gradle.kts`.

## Installation

1. Build the plugin (see below) to produce a JAR under `build/libs/`.
2. Copy the resulting JAR into your Hytale server's plugin/mods directory.
3. Start (or restart) the server. On first run, a `running_skeleton_config`
   configuration file is created in the plugin's data directory with the
   default chance (`2000`) and cooldown (`5` seconds).
4. Make sure your 14 frame PNGs (see above) were in place **before** you ran
   the build, since they are bundled into the JAR as part of the plugin's
   asset pack.

## How to compile / build

```
# Clone or copy this project, then from the project root:
./gradlew setupHytaleDev   # first-time only: prepares the local Hytale dev environment
./gradlew build            # compiles the plugin and produces the JAR
```

On Windows use `gradlew.bat` instead of `./gradlew`.

If dependency resolution fails, try:

```
./gradlew build --refresh-dependencies
```

## How to run it locally for testing

```
./gradlew runServer
```

This starts a local Hytale development server with the plugin already loaded,
using the Hytale Gradle Plugin's dev tooling. For hot-reload/debugging:

```
./gradlew runServer -Ddebug=true -Dhotswap=true
```

You can sanity-check your JVM/hot-swap setup with:

```
./gradlew hytaleJvmDoctor
```

## How to test

1. Start the local dev server with `./gradlew runServer` and join with a game
   client.
2. Run `/skeleton show` to confirm the fullscreen animation plays all 14
   frames in order, over about 2 seconds, and disappears automatically.
3. While the animation is not on cooldown, open your inventory and run
   `/skeleton show` again (or wait for a random trigger) to confirm the
   animation still appears on top of the inventory screen and does not close
   it or block your mouse/keyboard input.
4. Run `/skeleton chance 1` and wait up to a second to see the animation
   trigger essentially every tick (useful for visually confirming the
   animation and cooldown behavior quickly). Set it back to a sensible value
   afterward, for example `/skeleton chance 2000`.
5. Run `/skeleton info` right after an animation plays to confirm the
   cooldown is reported as active, then again after 5+ seconds to confirm it
   has cleared.

## How the random probability system works

Once per second, a scheduled task rolls one random integer in the range
`[0, chance)` for every player currently in a world. If the result is `0`, the
check "succeeds" and the animation plays for that player. With the default
`chance` of `2000`, this gives each online player an independent 1-in-2000
probability of triggering the event on any given second.

The chance is stored as the config's `Chance` field and can be changed at
runtime with `/skeleton chance <number>`, where `<number>` is the denominator
(for example, `/skeleton chance 2000` means "1 chance out of 2000 every
second"). Changes are saved immediately and take effect on the next tick.

## How the cooldown works

When the animation plays for a player (whether triggered randomly or via
`/skeleton show`), that player is placed on a cooldown that lasts for the
configured `CooldownSeconds` (5 seconds by default). While a player is on
cooldown, the per-second random check is skipped entirely for them, so the
event cannot trigger again until the cooldown expires, even if the random
roll would otherwise have succeeded. Cooldowns are tracked independently per
player.

## How the animation itself works

`RunningSkeleton_Overlay.ui` statically declares all 14 frames up front, each
as its own fullscreen `Group` with `Background: (TexturePath:
"Frames/frame_NN.png")`, stacked directly on top of each other. Only frame 1
is `Visible: true` by default; the rest start `Visible: false`.

`SkeletonAnimationService` then drives the animation directly, without using
the game's built-in sprite-sheet/repeat system:

1. It shows frame 1 (`SkeletonRunHud` with frame number 1; the file's default
   visibility state already shows frame 1 and hides the rest).
2. It schedules a task `2000 / 14` milliseconds later (about 143 ms) that
   shows frame 2 by setting `#Frame01.Visible` to `false` and
   `#Frame02.Visible` to `true`, and so on through frame 14.
3. Once frame 14 has been shown for its slice of time, it swaps in
   `BlankSkeletonHud`, an empty HUD, which clears the overlay entirely.

Because every frame's texture is declared directly in the `.ui` file itself
(not injected as a dynamic markup string at runtime) and only a plain
boolean `Visible` property is toggled per step, this avoids the main sources
of the "glitchy sprite sheet" and "placeholder texture" problems this
approach replaced.

## How the non-interrupting overlay works

Hytale's Custom UI system has two distinct kinds of custom screens:

- **Custom Pages** (`CustomUIPage` / `InteractiveCustomUIPage`): dialog-style
  screens that take over player input and unlock the mouse, similar to the
  crafting menu or pause menu.
- **Custom HUDs** (`CustomUIHud`): persistent, display-only overlays that sit
  alongside the player's normal HUD (hotbar, health bar, etc.) and never
  capture input or interrupt anything.

This plugin uses a `CustomUIHud` (`SkeletonRunHud`) for the animation, which
is why it does not interrupt the player, close their inventory, or otherwise
change what menu they have open. Frame switching and clearing are both done
through `HudManager#addCustomHud(PlayerRef, CustomUIHud)`, replacing the
previous frame's HUD with the next one (or with `BlankSkeletonHud` at the
end), since both HUD instances share the same HUD id (`"RunningSkeleton"`).

## Commands

All commands are subcommands of `/skeleton` and are open to any player or
console user — no permission node is required for any of them.

### `/skeleton chance <number>`

Sets the probability denominator.

```
/skeleton chance 2000
```

Sets the chance to 1 in 2000 per second.

### `/skeleton show`

Immediately plays the fullscreen animation for the executing player,
completely ignoring the random chance (the cooldown is still applied
afterward).

```
/skeleton show
```

### `/skeleton info`

Displays the current probability, the cooldown duration, and whether the
cooldown is currently active for the executing player (or, when run from the
console, how many players are currently on cooldown).

```
/skeleton info
```

Example output:

```
RunningSkeleton status
Chance: 1 in 2000 per second
Cooldown: 5 second(s)
Your cooldown is currently not active.
```

## Expected output

- Under normal play, most seconds produce no visible effect at all.
- Occasionally (by default, roughly once every 2000 seconds per player on
  average), a skeleton animation runs across the entire screen for exactly 2
  seconds (14 frames, in order) and then disappears on its own, without
  pausing gameplay or closing any open menu.
- For 5 seconds after that, no further animation can trigger for that player.
- `/skeleton show` always plays the animation on demand, useful for
  demonstrating the effect or testing configuration changes.

## Troubleshooting

- **`/skeleton show` runs with no errors, but nothing appears on screen.**
  Confirm that all 14 files exist at
  `src/main/resources/Common/UI/Custom/Huds/Frames/frame_01.png` through
  `frame_14.png` and were present when you ran `./gradlew build`. Also check
  the client-side log/console for any UI markup or texture-loading errors,
  and try enabling Diagnostic Mode under the Hytale client's General
  settings for more detailed error messages.
- **The animation plays but frames look skipped, frozen, or out of order.**
  Since each frame is shown individually on a fixed timer rather than through
  a sprite sheet, this is most likely a naming issue: double check every
  file is named exactly `frame_01.png` through `frame_14.png` (two-digit,
  zero-padded) with no gaps, and that each is exactly 1024x1024 pixels.
- **The animation still seems to interrupt the player or block input.**
  Double-check you're on the version of the plugin that uses `CustomUIHud`
  (`SkeletonRunHud`/`BlankSkeletonHud`) rather than a `CustomUIPage`-based
  implementation; only the HUD system is guaranteed not to capture input.
- **Gradle sync fails in IntelliJ.**
  Check that Java 25 is installed and configured under
  **File → Project Structure → SDKs**.
- **The Hytale Gradle Plugin does not resolve.**
  Make sure `settings.gradle.kts` includes the AzureDoom Maven repository at
  `https://maven.azuredoom.com/mods`.
- **Build fails with missing dependencies.**
  Run `./gradlew build --refresh-dependencies` and make sure you have
  internet access.
- **Permission denied on `./gradlew`.**
  Run `chmod +x gradlew` on macOS/Linux.
- **`/skeleton` says "Unknown command".**
  This means the plugin never finished loading, or the jar you installed is
  stale. Rebuild with `./gradlew build`, reinstall the fresh jar, and check
  the server console at startup for any error mentioning RunningSkeleton.
- **Config changes do not seem to apply.**
  `/skeleton chance` saves immediately, but if you edit
  `running_skeleton_config.json` by hand while the server is running, restart
  the server (or add your own `/skeleton reload`-style command) so the change
  is picked up, since the in-memory config is not automatically reloaded from
  disk.

## A note on verifying exact SDK symbols

This project was built by cross-referencing the publicly available
[HytaleModding](https://github.com/HytaleModding) organization, including its
`plugin-template` and `Hyssentials` repositories and its `site` documentation
repository, against Hytale SDK 0.5.7, and by iterating directly against real
compiler output from that exact SDK version. The plugin lifecycle, command,
config, and Custom UI APIs used here (`JavaPlugin`, `AbstractCommand`/
`AbstractCommandCollection`/`AbstractPlayerCommand`, `Config`/`BuilderCodec`,
`CustomUIHud`, `UICommandBuilder`) are all drawn directly from that real,
compiling reference material, refined against actual build errors along the
way:

- Player session tracking uses `PlayerReadyEvent` (which hands back a live
  `Ref<EntityStore>`) rather than the `Holder<EntityStore>` returned by
  world-transfer events, since a `Holder` is a pre-entity component blueprint
  and not a stable long-lived handle.
- `CustomUIHud` lives in
  `com.hypixel.hytale.server.core.entity.entities.player.hud`, obtained via
  `Player#getHudManager()`, and its constructor takes `(PlayerRef, String)`,
  where the `String` is a HUD id (`"RunningSkeleton"` here) — both confirmed
  directly from compiler output against the real SDK.
- `HudManager#addCustomHud(PlayerRef, CustomUIHud)` is the confirmed method
  used to both show and replace the HUD; calling it again with the same HUD
  id swaps the previous content, which is how frame-to-frame swapping and
  the final clear (via the empty-build `BlankSkeletonHud`) are both
  implemented, without needing `HudManager`'s `remove`/`clear` methods.
- All 14 frame textures are declared statically in
  `RunningSkeleton_Overlay.ui` itself, each as its own `Group` with
  `Background: (TexturePath: "Frames/frame_NN.png")`; only the currently
  active frame's `Group` has `Visible: true`. Frame switching is done with
  `UICommandBuilder#set("#FrameNN.Visible", true/false)`, a plain boolean
  toggle. This replaced two earlier approaches that were tried and
  disproven during development: an `AssetImage`/`AssetPath` element (never
  observed to render anything, silently showing a placeholder texture with
  no log output at all), and a single dynamically-injected `Sprite`/`Group`
  element rebuilt via `UICommandBuilder#clear` + `#appendInline` on every
  frame step (which did render, but only ever showed the first frame — the
  official docs confirm UI paths resolve relative to the `.ui` file where
  they are textually declared, and an inline markup string passed from Java
  has no such file, which is the most likely reason that path never
  updated). Declaring every texture path directly in the actual `.ui` file
  and only ever toggling a plain boolean at runtime avoids that whole class
  of problem.
- The exact name of the integer `Codec`/`ArgTypes` constant used for the
  `/skeleton chance <number>` argument was not directly visible in the
  published examples and was inferred from the SDK's established naming
  conventions.

If you hit any compile errors or runtime exceptions, paste them back and the
code can be adjusted to match your exact SDK build.
