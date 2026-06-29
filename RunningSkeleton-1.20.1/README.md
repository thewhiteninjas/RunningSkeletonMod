# RunningSkeleton Mod — Minecraft Forge 1.20.1

## Requisitos
- Java 17 (JDK)
- IntelliJ IDEA
- Minecraft Forge 1.20.1 — versión 47.4.10

## Configuración del proyecto en IntelliJ

1. Abre IntelliJ IDEA → **File > Open** → selecciona la carpeta `RunningSkeleton`
2. IntelliJ detectará el proyecto Gradle automáticamente. Deja que cargue.
3. En la pestaña **Gradle** (derecha), ejecuta la tarea:
   ```
   Tasks > forgegradle runs > genIntellijRuns
   ```
4. Recarga el proyecto Gradle (**Reload All Gradle Projects**).
5. Usa la configuración de run **runClient** para lanzar Minecraft con el mod.

## Añadir tus imágenes (IMPORTANTE)

Copia tus 14 frames dentro de:
```
src/main/resources/assets/runningskeleton/textures/frames/
```
Con estos nombres exactos:
```
frame_01.png
frame_02.png
...
frame_14.png
```
Las imágenes se escalarán automáticamente para ocupar toda la pantalla.

## Compilar el mod

```bash
./gradlew build
```
El `.jar` resultante estará en `build/libs/`.

## Comandos en el juego (chat, solo cliente)

| Comando | Descripción |
|---|---|
| `/skeleton info` | Muestra la configuración actual |
| `/skeleton show` | Fuerza mostrar la animación ahora |
| `/skeleton chance 2000` | Establece probabilidad a 1 en 2000 por segundo |

## Comportamiento
- Cada segundo hay una probabilidad de **1 en 2000** (configurable) de que aparezca la animación.
- La animación muestra los 14 frames a 100ms por frame (~1.4 segundos en total).
- Al terminar hay un **cooldown de 5 segundos** antes de que pueda volver a aparecer.
- Las imágenes ocupan **toda la pantalla** y se adaptan al tamaño de la ventana.
- Los comandos `/skeleton` son **solo cliente** y no se envían al servidor.

## Notas
- El mod es **solo cliente** (client-side only). No necesita instalarse en el servidor.
- Si quieres cambiar la velocidad de los frames, edita `FRAME_DURATION_MS` en `SkeletonConfig.java`.
- Si quieres cambiar el cooldown, edita `COOLDOWN_MS` en `SkeletonConfig.java`.
