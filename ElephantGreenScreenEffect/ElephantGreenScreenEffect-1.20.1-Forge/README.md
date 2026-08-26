# ElephantGreenScreenEffect

Mod de cliente (client-side only) para **Minecraft Java 1.20.1 (Forge)**.

Autor: **TheWhiteNinja**

## Versiones utilizadas (verificadas)

| Componente        | Versión                     |
|--------------------|------------------------------|
| Minecraft          | 1.20.1                       |
| Forge              | 47.4.10 (recomendada oficial para 1.20.1) |
| ForgeGradle        | 6.0.x (`[6.0,6.2)`)          |
| Mappings           | `official` 1.20.1            |
| Gradle (wrapper)   | 8.1.1                        |
| Java (compilación) | 17 (toolchain fijado en `build.gradle`) |

**Importante sobre Java 21 en IntelliJ:** el `build.gradle` fija
`java.toolchain.languageVersion = JavaLanguageVersion.of(17)`. Esto significa
que aunque el SDK del proyecto/IntelliJ esté configurado con Java 21, Gradle
usará (o descargará automáticamente, si tienes la resolución automática de
toolchains activada) un JDK 17 real para compilar el código. Minecraft
1.20.1 y Forge 47.x **requieren** Java 17 para compilar y ejecutarse; Java 21
no es compatible como JDK de ejecución para esta versión concreta de
Minecraft. Si Gradle no encuentra un JDK 17 y no puede descargarlo
automáticamente, instala un JDK 17 (por ejemplo Temurin 17) y dispondrás de
él en tu sistema; Gradle lo detectará solo.

## Cómo abrir el proyecto

1. Abre IntelliJ IDEA.
2. `File -> Open...` y selecciona la carpeta raíz del proyecto
   (`ElephantGreenScreenEffect`), la que contiene `build.gradle`.
3. Deja que IntelliJ importe el proyecto como Gradle. La primera
   sincronización descargará Minecraft, Forge y las librerías necesarias
   (puede tardar varios minutos).
4. Cuando termine, en el panel de Gradle (lateral derecho) ejecuta la tarea
   `runClient` (o usa la configuración de ejecución `client` que Forge/FG
   genera automáticamente) para lanzar Minecraft con el mod cargado.
5. Para generar el `.jar` final: tarea Gradle `build`. El archivo aparecerá
   en `build/libs/`.

No es necesario ejecutar ningún paso manual adicional de "genIntellijRuns":
ForgeGradle 6 configura las Run Configurations automáticamente al importar
el proyecto en IntelliJ.

## Archivos que debes añadir manualmente

### Imágenes de la animación (48 frames)

Coloca las 48 imágenes PNG (398×281 px cada una) exactamente con estos
nombres, en esta carpeta:

```
src/main/resources/assets/elephantgreenscreeneffect/textures/elephant/
├── frame_(1).png
├── frame_(2).png
├── ...
└── frame_(48).png
```

Nota técnica: los paréntesis en el nombre de archivo no son válidos como
`ResourceLocation` de Minecraft, así que el mod **no** las carga a través
del sistema de recursos estándar de Minecraft. En su lugar, las lee
directamente desde el classpath/jar con su nombre de archivo original, por
lo que puedes (y debes) mantener el nombre exacto `frame_(N).png`.

### Sonido

Coloca el archivo de sonido aquí, con este nombre exacto:

```
src/main/resources/assets/elephantgreenscreeneffect/sounds/elephant.ogg
```

El fichero `sounds.json` ya está creado y registrado para apuntar a este
archivo (`elephantgreenscreeneffect:elephant`).

Tras añadir las imágenes y el sonido, si estabas con Gradle/IntelliJ ya
sincronizado, simplemente vuelve a ejecutar `runClient` (no hace falta
recompilar Java, son solo recursos).

## Funcionamiento

- Cada segundo (cada 20 ticks de cliente) se realiza una tirada aleatoria
  con probabilidad `1 / chance` (valor configurable, por defecto `2000`).
- Si la tirada tiene éxito y no hay una animación en curso ni en cooldown,
  comienza la animación:
  - Se reproduce el sonido `elephant.ogg`.
  - Se muestran los 48 frames en orden, estirados a pantalla completa (sin
    mantener relación de aspecto). La animación completa dura exactamente
    **1 segundo**, calculado por tiempo real (no por ticks), ya que un tick
    de Minecraft equivale a 50 ms (20 ticks/segundo) y no permite suficiente
    resolución para repartir 48 frames en 1 segundo.
- Al terminar el frame 48, comienza un cooldown de 5 segundos durante el
  cual no puede volver a activarse la animación (ni por probabilidad ni
  manualmente mediante lógica del propio disparo automático; el comando
  `/elephant show` sí puede forzarla en cualquier momento, incluso durante
  el cooldown del disparo aleatorio, tal como se pide en el enunciado: es
  una reproducción inmediata explícita).

## Comandos

- `/elephant chance <número>` — Cambia la probabilidad. Por ejemplo,
  `/elephant chance 500` establece 1 entre 500. El valor se guarda de forma
  persistente en `config/elephantgreenscreeneffect-client.toml` y se
  mantiene tras reiniciar Minecraft.
- `/elephant show` — Reproduce inmediatamente la animación y el sonido.
- `/elephant info` — Muestra la configuración actual (probabilidad).

Estos son comandos **de cliente** (registrados mediante
`RegisterClientCommandsEvent` de Forge), por lo que funcionan tanto en un
mundo en solitario como conectado a un servidor, sin necesidad de que el
servidor tenga el mod instalado.

## Estructura del proyecto

```
ElephantGreenScreenEffect/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── README.md
└── src/main/
    ├── java/com/thewhiteninja/elephantgreenscreeneffect/
    │   ├── ElephantGreenScreenEffect.java   (clase @Mod principal)
    │   ├── ModSounds.java                   (registro del SoundEvent)
    │   ├── ClientConfig.java                (configuración persistente)
    │   ├── ElephantAnimationManager.java    (lógica, tick y render)
    │   └── ElephantClientCommand.java       (comandos /elephant)
    └── resources/
        ├── META-INF/mods.toml
        ├── pack.mcmeta
        └── assets/elephantgreenscreeneffect/
            ├── sounds.json
            ├── sounds/elephant.ogg          (añadir manualmente)
            └── textures/elephant/
                └── frame_(1..48).png        (añadir manualmente)
```

## Notas de compatibilidad

- El mod está marcado como `side="CLIENT"` en `mods.toml`.
- No se usan dependencias adicionales aparte de Forge (Minecraft/Forge API
  únicamente), tal como se pidió.
- Todo el código está en inglés en cuanto a comentarios; los mensajes de
  chat generados por los comandos también están en inglés (identificados
  con el prefijo `[ElephantGreenScreenEffect]`) para mantener consistencia
  con el resto de mods del ecosistema Forge; puedes traducirlos si lo
  deseas editando `ElephantClientCommand.java`.
