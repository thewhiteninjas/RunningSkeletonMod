# RunningSkeleton

Client-only Fabric mod for **Minecraft Java 1.21.11**.

Author: TheWhiteNinja

## What it does

Every second, the mod has a configurable random chance (default: 1 in 2000)
of triggering a fullscreen animation made of 14 PNG frames. While playing,
the animation is scaled to always cover the entire game window. After the
animation finishes, there is a 5 second cooldown before it can trigger again.

## Verified toolchain versions

These versions were checked directly against the official FabricMC Maven
repository (https://maven.fabricmc.net) and Minecraft's official release
page before the project was generated.

| Component       | Version          |
|------------------|------------------|
| Minecraft        | 1.21.11          |
| Yarn Mappings    | 1.21.11+build.3  |
| Fabric Loader    | 0.18.5           |
| Fabric API       | 0.141.2+1.21.11  |
| Fabric Loom      | 1.14.10          |
| Gradle           | 9.0.0            |
| Java (JDK)       | 21               |

## Required: add the animation frames

Place the 14 PNG frames (1024x1024 each) in:

```
src/client/resources/assets/runningskeleton/textures/animation/
```

named exactly:

```
frame_01.png
frame_02.png
...
frame_14.png
```

A placeholder file (`PLACE_FRAMES_HERE.txt`) marks this folder; delete it
once your frames are in place (it is not loaded by the mod).

## Building

1. Make sure you have **JDK 21** installed and selected.
2. Open the project folder in IntelliJ IDEA as a Gradle project (or run
   from the command line).
3. From the project root:

   ```
   ./gradlew build
   ```

   On Windows:

   ```
   gradlew.bat build
   ```

   The first run will download Minecraft, mappings and dependencies; this
   requires an internet connection.
4. The compiled mod jar will be located at `build/libs/runningskeleton-1.0.0.jar`.

## Running in development

```
./gradlew runClient
```

This launches a development instance of Minecraft 1.21.11 with the mod loaded.

## Commands

- `/skeleton chance <number>` — sets the trigger chance denominator (e.g. `2000` = 1 in 2000).
- `/skeleton show` — immediately plays the animation, ignoring chance and cooldown.
- `/skeleton info` — shows the current chance and cooldown status.

## Notes

- This is a **client-side only** mod; it has no effect on servers and does
  not need to be installed server-side.
- If the IDE does not regenerate the Gradle wrapper jar automatically,
  run `gradle wrapper --gradle-version 9.0.0` once with a system-installed
  Gradle to (re)create `gradle/wrapper/gradle-wrapper.jar`.
