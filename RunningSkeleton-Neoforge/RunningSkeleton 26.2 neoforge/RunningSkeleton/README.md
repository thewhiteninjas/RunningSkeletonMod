# RunningSkeleton

**Autor:** TheWhiteNinja

Mod de cliente (client-only) para Minecraft Java 26.2 / NeoForge. Mientras juegas, cada segundo
existe una probabilidad de que aparezca en pantalla completa una animación de un esqueleto
corriendo, de 14 fotogramas. Tras reproducirse, entra en un cooldown de 5 segundos durante el
cual no puede volver a activarse automáticamente. Incluye comandos para configurar la
probabilidad, forzar la animación y consultar el estado actual.

---

## 1. Versiones comprobadas (fuentes oficiales)

Todas las versiones se comprobaron contra la documentación y los repositorios oficiales de
NeoForged (neoforged.net, docs.neoforged.net, maven.neoforged.net y el generador oficial de
proyectos `NeoForgeMDKs/MDK-26.2-ModDevGradle`) el día de la generación de este proyecto.

| Componente | Versión | Notas |
|---|---|---|
| Minecraft | **26.2** | |
| NeoForge | **26.2.0.58** | Última versión estable publicada para MC 26.2 |
| Toolchain de Gradle | **ModDevGradle** (`net.neoforged.moddev` 2.0.143) | Toolchain oficialmente recomendada para un proyecto con una sola versión de Minecraft/mod. NeoGradle es la alternativa (también oficial), pensada para proyectos multi-versión; no se usa aquí |
| Gradle (wrapper) | **9.2.1** | Requerido por MC 26.2 (mínimo oficial: 9.1.0) |
| JDK | **25** | Minecraft 26.2 distribuye Java 25 a los usuarios finales; el toolchain de Gradle está fijado a `JavaLanguageVersion.of(25)` |
| Mappings | **Oficiales de Mojang** | Desde MC 26.1 el juego ya no está ofuscado y expone directamente los nombres oficiales de parámetros; no se usa Parchment |
| Mod loader | **NeoForge exclusivamente** | Sin Forge, sin `mods.toml` de Forge, sin `net.minecraftforge.*` |

> **Importante sobre Java:** este proyecto pediste inicialmente que funcionase con Java 21, pero
> la documentación oficial de NeoForged confirma que Minecraft 26.2 (igual que 26.1) requiere
> **Java 25**. Se usó Java 25 según lo confirmado en la conversación.

## 2. Limitación conocida: no se pudo ejecutar `gradlew build` en este entorno

El entorno en el que se generó este proyecto tiene una lista blanca de dominios de red que
**no incluye `maven.neoforged.net`**, que es el repositorio Maven desde donde ModDevGradle
descarga NeoForge y las fuentes de Minecraft. Por este motivo no fue posible ejecutar una
compilación real (`gradlew build`) para verificarla de extremo a extremo dentro de este entorno.

Se ha compensado esto verificando cuidadosamente contra la documentación y los javadocs
oficiales de NeoForged/26.1/26.2 cada API usada (eventos, `GuiGraphicsExtractor`, `Identifier`,
`ModConfigSpec`, comandos de cliente, etc.), y reutilizando literalmente el `gradlew`,
`gradle-wrapper.jar`, `build.gradle` base y `settings.gradle` del MDK oficial
(`NeoForgeMDKs/MDK-26.2-ModDevGradle`), adaptados solo en lo necesario.

**Por favor, la primera vez que abras el proyecto, ejecuta `gradlew build` (o `gradlew runClient`
directamente) y, si aparece cualquier error de compilación, avísame con el mensaje exacto para
corregirlo de inmediato.**

## 3. Requisitos

- JDK 25 (64 bits). Se recomienda la distribución de Microsoft Build of OpenJDK.
- IntelliJ IDEA (Community o Ultimate) con el plugin de Gradle habilitado (viene por defecto).
- Conexión a internet la primera vez que se importe el proyecto (Gradle descargará NeoForge,
  Minecraft y sus dependencias, y las descompilará; puede tardar bastante).
- No se necesita tener Gradle instalado en el sistema: el proyecto usa **Gradle Wrapper**
  (`gradlew` / `gradlew.bat`), que descarga automáticamente Gradle 9.2.1.

## 4. Estructura relevante del proyecto

```
RunningSkeleton/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── src/main/java/com/thewhiteninja/runningskeleton/
│   ├── RunningSkeletonMod.java          -> punto de entrada del mod (@Mod, dist=CLIENT)
│   ├── RunningSkeletonConfig.java       -> configuración persistente (probabilidad)
│   ├── SkeletonAnimationState.java      -> máquina de estados: idle / playing / cooldown
│   ├── SkeletonOverlayRenderer.java     -> renderizado a pantalla completa del frame actual
│   ├── SkeletonCommand.java             -> comandos /skeleton chance|show|info
│   └── SkeletonTextures.java            -> resuelve los Identifier de los 14 frames
├── src/main/templates/META-INF/
│   └── neoforge.mods.toml               -> descriptor de NeoForge (NO es un mods.toml de Forge)
└── src/main/resources/assets/runningskeleton/textures/animation/
    └── (aquí van las 14 imágenes, ver sección 5)
```

## 5. Ubicación exacta de las imágenes

Copia las 14 imágenes PNG (1024x1024 px cada una) dentro de esta carpeta exacta del proyecto:

```
src/main/resources/assets/runningskeleton/textures/animation/
```

Con estos nombres de archivo exactos (minúsculas, guion bajo, dos dígitos):

```
frame_01.png
frame_02.png
frame_03.png
frame_04.png
frame_05.png
frame_06.png
frame_07.png
frame_08.png
frame_09.png
frame_10.png
frame_11.png
frame_12.png
frame_13.png
frame_14.png
```

Dentro de esa carpeta encontrarás un archivo `PLACE_FRAMES_HERE.txt` a modo de recordatorio;
puedes borrarlo una vez hayas colocado los 14 frames (no lo lee el mod).

## 6. Funcionamiento de la animación

- Cada segundo de juego (mientras el estado es "idle", ni reproduciendo ni en cooldown), el mod
  hace una tirada aleatoria con probabilidad `1 / chance` de iniciar la animación.
- Al iniciarse, se reproducen los 14 frames en orden (`frame_01.png` → `frame_14.png`) a un ritmo
  fijo de ~14 fotogramas por segundo (~70 ms por frame), con una duración total aproximada de 1
  segundo.
- La imagen se dibuja ocupando el 100% de la pantalla, recalculando el tamaño de destino en cada
  frame a partir de la resolución actual de la ventana de Minecraft (`Window#getGuiScaledWidth`/
  `getGuiScaledHeight`), por lo que se adapta automáticamente a cualquier resolución o relación de
  aspecto, incluso si se redimensiona la ventana durante la partida.
- El renderizado se realiza mediante el evento `RenderGuiEvent.Post` de NeoForge y la API vigente
  `GuiGraphicsExtractor#blit(...)`; no se realiza ninguna carga de archivos ni operación bloqueante
  en el hilo de renderizado, por lo que no introduce congelaciones ni tirones.

## 7. Sistema de probabilidad

- Configurable mediante `/skeleton chance <numero>`, donde `<numero>` es el denominador: un valor
  de `2000` significa una probabilidad de `1/2000` cada segundo.
- El valor mínimo permitido es `1` (probabilidad del 100% cada segundo). Brigadier valida el
  argumento como entero ≥ 1 automáticamente; cualquier valor no numérico o menor que 1 es
  rechazado por el propio parser de comandos antes de llegar al código del mod.
- Valor por defecto si no existe configuración previa: **2000**.

## 8. Cooldown

- Al terminar la reproducción de los 14 frames, el mod entra en estado de cooldown durante
  **5 segundos**.
- Durante el cooldown no se realiza ninguna tirada de probabilidad ni puede iniciarse la
  animación de forma automática.
- El comando `/skeleton show` **sí** puede mostrar la animación de inmediato en cualquier
  momento (incluso durante un cooldown activo), ya que es una activación manual explícita del
  usuario y no pasa por la lógica de probabilidad.

## 9. Comandos

| Comando | Descripción |
|---|---|
| `/skeleton chance <numero>` | Cambia la probabilidad a `1/<numero>` por segundo. Se valida como entero ≥ 1 y se persiste automáticamente en el archivo de configuración. |
| `/skeleton show` | Muestra la animación inmediatamente, sin esperar a la tirada de probabilidad y sin verse bloqueada por el cooldown. |
| `/skeleton info` | Muestra la probabilidad configurada actualmente y el estado de la animación (`idle`, `playing` o `cooldown` con los milisegundos restantes). |

## 10. Archivo de configuración

- Tipo: configuración de **cliente** de NeoForge (`ModConfig.Type.CLIENT`), basada en
  `ModConfigSpec`.
- Ubicación tras ejecutar el juego: `config/runningskeleton-client.toml` (dentro de la carpeta de
  ejecución de Minecraft, por ejemplo `run/config/runningskeleton-client.toml` en el entorno de
  desarrollo, o `.minecraft/config/runningskeleton-client.toml` en un cliente instalado).
- Si el archivo no existe al arrancar, NeoForge lo crea automáticamente con el valor por defecto
  `chance = 2000`.
- Cualquier cambio realizado con `/skeleton chance <numero>` se guarda automáticamente en ese
  archivo y se conserva entre reinicios del juego.

## 11. Cómo importar el proyecto en IntelliJ IDEA

1. Descomprime el ZIP del proyecto.
2. Coloca las 14 imágenes PNG en la carpeta indicada en la sección 5 (puedes hacerlo también más
   tarde, antes de ejecutar `runClient`).
3. En IntelliJ IDEA: **File → Open...** y selecciona la carpeta raíz del proyecto (la que
   contiene `build.gradle`).
4. IntelliJ detectará automáticamente que es un proyecto Gradle y comenzará la importación.
5. Asegúrate de que IntelliJ usa **JDK 25** como SDK del proyecto (**File → Project Structure →
   Project SDK**). Si no tienes el JDK 25 instalado, IntelliJ puede descargarlo automáticamente
   gracias al plugin `foojay-resolver` incluido en `settings.gradle`.
6. Espera a que termine la sincronización de Gradle (la primera vez puede tardar bastante, ya que
   descarga y descompila Minecraft y NeoForge).

## 12. Cómo ejecutar el cliente de desarrollo

- Desde IntelliJ: en el panel de Gradle, ejecuta la tarea `runClient` (dentro de
  `Tasks → neoforge`), o usa la configuración de ejecución "client" que NeoForge genera
  automáticamente tras la sincronización.
- Desde terminal:
  ```
  ./gradlew runClient        # Linux / macOS
  gradlew.bat runClient      # Windows
  ```

## 13. Cómo compilar el mod

```
./gradlew build        # Linux / macOS
gradlew.bat build       # Windows
```

El JAR resultante se genera en `build/libs/runningskeleton-1.0.0.jar`.

## 14. Cómo instalar el mod

1. Instala NeoForge **26.2.0.58** (o compatible) para Minecraft 26.2 en tu launcher, siguiendo el
   instalador oficial desde `https://neoforged.net/`.
2. Copia el archivo `build/libs/runningskeleton-1.0.0.jar` a la carpeta `mods` de tu instalación
   de Minecraft con NeoForge.
3. Inicia Minecraft con el perfil de NeoForge 26.2. Al ser un mod de cliente, no es necesario
   instalarlo en servidores dedicados; si se instala allí, simplemente no se carga (la clase
   principal está anotada con `dist = Dist.CLIENT`).

## 15. Notas técnicas relevantes

- No hay ninguna referencia a Forge en el proyecto: ni imports `net.minecraftforge.*`, ni
  `mods.toml` de Forge, ni dependencias ni plugins de Gradle de Forge/ForgeGradle. El único
  descriptor de mod presente es `src/main/templates/META-INF/neoforge.mods.toml`, procesado por
  ModDevGradle hacia `META-INF/neoforge.mods.toml` en el JAR final.
- El mod es 100% cliente: la única clase `@Mod` está anotada con `dist = Dist.CLIENT` y no
  registra bloques, items, ni ninguna lógica que se ejecute en el lado servidor.
- La renderización usa la API vigente en 26.2: `GuiGraphicsExtractor` (renombrado desde
  `GuiGraphics` en Minecraft 26.1) junto con `RenderPipelines.GUI_TEXTURED` e `Identifier`
  (renombrado desde `ResourceLocation`).
- La licencia del mod, en `gradle.properties` (`mod_license`), está configurada como
  "All Rights Reserved" por defecto; cámbiala si lo deseas.
