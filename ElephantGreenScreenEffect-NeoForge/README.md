# ElephantGreenScreenEffect (NeoForge 1.21.1)

Mod de cliente (client-side only) para **Minecraft Java 1.21.1 (NeoForge)**.
Puerto completo del mod original de Forge 1.20.1, con la misma funcionalidad.

Autor: **TheWhiteNinja**

## Versiones utilizadas (verificadas)

| Componente             | Versión                          |
|-------------------------|-----------------------------------|
| Minecraft               | 1.21.1                            |
| NeoForge                | 21.1.244                          |
| Plugin de Gradle        | `net.neoforged.moddev` 2.0.143 (ModDevGradle) |
| Parchment mappings      | 2024.11.17 (para 1.21.1)          |
| Gradle (wrapper)        | 9.2.1                             |
| Java (compilación y ejecución) | 21 (toolchain fijado en `build.gradle`) |

**Sobre Java 21:** a diferencia de la versión 1.20.1/Forge, aquí Java 21 **es**
la versión correcta y obligatoria tanto para compilar como para ejecutar el
juego, ya que Mojang distribuye Java 21 a los usuarios desde Minecraft
1.20.5 en adelante. `java.toolchain.languageVersion = JavaLanguageVersion.of(21)`
en `build.gradle` asegura que Gradle use un JDK 21 real.

## Diferencias clave respecto a la versión Forge 1.20.1

Este puerto no es un simple cambio de versión: NeoForge reorganizó paquetes y
APIs entre 1.20.1 y 1.21.1. Cambios aplicados:

- Paquetes `net.minecraftforge.*` → `net.neoforged.*` / `net.neoforged.neoforge.*`.
- `ForgeConfigSpec` → `net.neoforged.neoforge.common.ModConfigSpec`.
- El registro de configuración ya no usa `ModLoadingContext.get()`, sino el
  `ModContainer` que NeoForge inyecta automáticamente en el constructor del
  mod: `modContainer.registerConfig(...)`.
- El constructor de la clase `@Mod` ahora recibe `(IEventBus modEventBus, ModContainer modContainer)`
  directamente, sin necesidad de `FMLJavaModLoadingContext.get()`.
- `@Mod.EventBusSubscriber` (anidada) → `@net.neoforged.fml.common.EventBusSubscriber`
  (anotación independiente), y **ya no se especifica el parámetro `bus`**:
  NeoForge detecta automáticamente el bus correcto según el tipo de evento.
- `TickEvent.ClientTickEvent` con `Phase.START/END` →
  `net.neoforged.neoforge.client.event.ClientTickEvent.Post`, que se dispara
  una única vez por tick (ya no hay que comprobar la fase).
- `RenderGuiEvent.Post` se mantiene, pero ahora vive en
  `net.neoforged.neoforge.client.event.RenderGuiEvent`.
- `new ResourceLocation(namespace, path)` (constructor público) →
  `ResourceLocation.fromNamespaceAndPath(namespace, path)`, ya que el
  constructor se hizo privado a partir de 1.21.
- `ForgeRegistries.SOUND_EVENTS` → `net.minecraft.core.registries.Registries.SOUND_EVENT`
  (NeoForge usa las claves de registro vanilla directamente).
- `RegistryObject<T>` → `DeferredHolder<T, T>`.
- El fichero de metadatos ya no es `META-INF/mods.toml` sino
  `META-INF/neoforge.mods.toml`, y además ya **no vive directamente en
  `resources`**: la plantilla está en `src/main/templates/META-INF/neoforge.mods.toml`
  y Gradle la procesa (sustituyendo los `${...}`) hacia
  `build/generated/sources/modMetadata` automáticamente en cada sync.
- `pack_format` de `pack.mcmeta` pasa de `15` (1.20.1) a `34` (1.21–1.21.1).

El resto de la lógica (probabilidad 1/N cada segundo, animación de 48 frames
en pantalla completa durante 1 segundo exacto por tiempo real, sonido,
cooldown de 5 segundos y comandos `/elephant`) es idéntica en comportamiento
al mod de Forge 1.20.1.

## Cómo abrir el proyecto

1. Abre IntelliJ IDEA.
2. `File -> Open...` y selecciona la carpeta raíz del proyecto (la que
   contiene `build.gradle`).
3. Deja que IntelliJ importe el proyecto como Gradle (usa el plugin
   `net.neoforged.moddev`, que sustituye a NeoGradle/ForgeGradle). La
   primera sincronización descargará Minecraft, NeoForge, Parchment y las
   librerías necesarias.
4. Cuando termine, usa la configuración de ejecución `client` generada
   automáticamente (o la tarea Gradle `runClient`) para lanzar Minecraft con
   el mod cargado.
5. Para generar el `.jar` final: tarea Gradle `build`. El archivo aparecerá
   en `build/libs/`.

Con el plugin `net.neoforged.moddev` no hace falta ejecutar ningún paso
adicional del estilo "genIntellijRuns"; todo se configura automáticamente al
sincronizar Gradle.

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

Igual que en la versión de Forge 1.20.1: los paréntesis no son válidos en un
`ResourceLocation`, así que el mod las lee directamente desde el classpath
con su nombre de archivo original (sin pasar por el sistema de recursos de
Minecraft), por lo que debes mantener el nombre exacto `frame_(N).png`.

### Sonido

Coloca el archivo de sonido aquí, con este nombre exacto:

```
src/main/resources/assets/elephantgreenscreeneffect/sounds/elephant.ogg
```

El fichero `sounds.json` ya está creado y registrado para apuntar a este
archivo (`elephantgreenscreeneffect:elephant`).

## Funcionamiento

- Cada segundo (cada 20 ticks de cliente) se realiza una tirada aleatoria
  con probabilidad `1 / chance` (valor configurable, por defecto `2000`).
- Si la tirada tiene éxito y no hay una animación en curso ni en cooldown,
  comienza la animación:
  - Se reproduce el sonido `elephant.ogg`.
  - Se muestran los 48 frames en orden, estirados a pantalla completa (sin
    mantener relación de aspecto). La animación completa dura exactamente
    **1 segundo**, calculado por tiempo real (no por ticks).
- Al terminar el frame 48, comienza un cooldown de 5 segundos durante el
  cual no puede volver a activarse la animación de forma automática.

## Comandos

- `/elephant chance <número>` — Cambia la probabilidad. El valor se guarda
  de forma persistente en `config/elephantgreenscreeneffect-client.toml`.
- `/elephant show` — Reproduce inmediatamente la animación y el sonido; si
  falla (por ejemplo, por faltar algún archivo), lo indica explícitamente en
  el chat en lugar de fallar en silencio.
- `/elephant info` — Muestra la configuración actual (probabilidad).

Son comandos **de cliente** (`RegisterClientCommandsEvent`), por lo que
funcionan tanto en un mundo en solitario como conectado a un servidor sin
necesidad de que el servidor tenga el mod instalado.

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
    ├── templates/META-INF/
    │   └── neoforge.mods.toml               (plantilla, se procesa a build/)
    └── resources/
        ├── pack.mcmeta
        └── assets/elephantgreenscreeneffect/
            ├── sounds.json
            ├── sounds/elephant.ogg          (añadir manualmente)
            └── textures/elephant/
                └── frame_(1..48).png        (añadir manualmente)
```

## Notas de compatibilidad

- El mod está marcado como `side="CLIENT"` en `neoforge.mods.toml`.
- No se usan dependencias adicionales aparte de NeoForge, tal como se pidió.
- Todo el código está en inglés en cuanto a comentarios.
