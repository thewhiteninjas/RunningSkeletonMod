# RunningSkeleton

**Autor:** TheWhiteNinja

Mod de cliente (client-side only) para Minecraft Java Edition. Cada segundo existe una
probabilidad configurable (por defecto 1/2000) de que aparezca en pantalla completa una
animacion de 14 frames de un esqueleto corriendo. Tras reproducirse, entra en un cooldown
de 5 segundos durante el cual no puede volver a activarse automaticamente.

---

## 1. Aviso importante sobre la verificacion de este proyecto

Este proyecto se generó siguiendo el proceso pedido: se comprobaron en fuentes oficiales
(blog de Fabric, documentación de Fabric, wiki de Minecraft y el repositorio oficial
`FabricMC/fabric-example-mod`, rama `26.2`) las versiones exactas de Minecraft, Fabric
Loader, Fabric API, Fabric Loom, Gradle y Java, y el código se escribió usando las firmas
de API confirmadas en la documentación oficial de Fabric para 26.2 (rendering, comandos,
HUD, etc.), citada en la sección 5.

**Sin embargo, el entorno en el que se generó este proyecto no tiene acceso de red a
`maven.fabricmc.net`, a los repositorios de librerías de Mojang ni a los servidores de
distribución de Gradle.** Por esta razón **no ha sido posible ejecutar una compilación
real (`gradlew build`)** como paso final, algo que normalmente sí haría antes de entregar
el proyecto. Todo el código se ha revisado manualmente contra la documentación oficial
más reciente disponible, pero la primera compilación en tu máquina es la verificación
real y definitiva. Si `gradlew build` da algún error, casi con toda seguridad será un
`import` que IntelliJ resolverá automáticamente (ver sección 9).

Además, por la misma restricción de red, **no se incluye el binario `gradle-wrapper.jar`**
(no se pudo descargar). Ver sección 8 para generarlo en un solo paso.

---

## 2. Versiones exactas utilizadas (verificadas en fuentes oficiales)

| Componente | Versión | Fuente |
|---|---|---|
| Minecraft | 26.2 ("Chaos Cubed", 16 jun 2026) | fabricmc.net/2026/06/15/262.html |
| Fabric Loader | 0.19.3 | fabricmc.net/2026/06/15/262.html |
| Fabric API | 0.154.0+26.2 | github.com/FabricMC/fabric-example-mod (rama `26.2`) |
| Fabric Loom | 1.17-SNAPSHOT | github.com/FabricMC/fabric-example-mod (rama `26.2`) |
| Gradle | 9.5.1 | fabricmc.net/2026/06/15/262.html |
| Java / JDK | 25 (obligatorio desde 26.1, se mantiene en 26.2) | minecraft.wiki/w/Java_Edition_26.1, fabricmc.net/2026/03/14/261.html |

> Nota sobre Java: se pidió inicialmente Java 21, pero Minecraft 26.2 **requiere Java 25
> como mínimo** (confirmado por Mojang y por Fabric). Se acordó contigo usar Java 25 antes
> de generar el proyecto.

Fabric API 0.154.0+26.2 es la versión que el propio repositorio de ejemplo de Fabric
fija para la rama 26.2. En CurseForge existen builds más recientes de Fabric API para
26.2 (hasta 0.157.0+26.2 en el momento de escribir esto); si quieres la última, cambia
`fabric_api_version` en `gradle.properties`.

---

## 3. Requisitos

- JDK 25 instalado (por ejemplo, Eclipse Temurin 25 o Microsoft Build of OpenJDK 25).
- IntelliJ IDEA (Community o Ultimate) 2025.3 o superior — requerido por Fabric para que
  los mixins/loom funcionen correctamente en 26.x (este mod no usa mixins, pero se
  recomienda igualmente por compatibilidad general de Loom).
- Conexión a internet la primera vez que abras el proyecto (Gradle necesita descargar
  Minecraft, las librerías y Fabric API).
- Las 14 imágenes PNG de la animación (las añades tú, ver sección 6).

---

## 4. Estructura relevante del proyecto

```
RunningSkeleton/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
├── LICENSE
├── src/
│   ├── main/resources/
│   │   └── fabric.mod.json
│   └── client/
│       ├── java/com/thewhiteninja/runningskeleton/client/
│       │   ├── RunningSkeletonClient.java      (entrypoint del cliente)
│       │   ├── animation/AnimationManager.java (probabilidad, reproducción, cooldown)
│       │   ├── command/SkeletonCommand.java    (/skeleton chance|show|info)
│       │   ├── config/ModConfig.java           (persistencia JSON)
│       │   └── render/SkeletonHudRenderer.java (dibujado en pantalla completa)
│       └── resources/assets/runningskeleton/textures/gui/skeleton/
│           └── (aquí van tus 14 PNG)
```

El mod usa `splitEnvironmentSourceSets()` de Loom: todo el código vive en el source set
`client`, y `src/main/resources` solo contiene el descriptor `fabric.mod.json`. Esto,
junto con `"environment": "client"` en `fabric.mod.json`, garantiza que el mod es
exclusivamente de cliente y no se carga en un servidor dedicado.

---

## 5. Decisiones técnicas relevantes (APIs usadas, verificadas en docs.fabricmc.net/26.2)

- **Renderizado en pantalla completa:** `net.minecraft.client.gui.GuiGraphicsExtractor`
  (sustituye al antiguo `GuiGraphics`) junto con `net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED`,
  registrado a través de `HudElementRegistry.addLast(...)`
  (`net.fabricmc.fabric.api.client.rendering.v1.hud`), que sustituye al `HudRenderCallback`
  eliminado en 26.1. El frame se dibuja con `blit(...)` estirando la textura de origen
  (1024x1024) al tamaño actual de la ventana (`Window#getGuiScaledWidth/Height`), por lo
  que se adapta automáticamente a cualquier resolución o relación de aspecto.
- **Comandos de cliente:** `net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback`
  + `ClientCommands` (equivalente client-side de `Commands`), sobre Brigadier.
- **Sin OpenGL directo:** todo el dibujado pasa por la capa Blaze3D/GuiGraphicsExtractor,
  compatible tanto con el backend OpenGL como con el nuevo backend Vulkan experimental de
  26.2.
- **No bloqueante:** toda la lógica de temporización (probabilidad, frames, cooldown) se
  basa en `System.currentTimeMillis()` leído en cada tick de cliente
  (`ClientTickEvents.END_CLIENT_TICK`) y en cada frame de render; no hay hilos, `Thread.sleep`
  ni bucles bloqueantes, así que no puede provocar congelaciones.
- **Persistencia:** JSON simple vía Gson (ya incluido por Minecraft, sin dependencias
  extra) en la carpeta de configuración estándar de Fabric.

---

## 6. Ubicación exacta de las imágenes

Copia tus 14 archivos PNG (1024x1024 cada uno) exactamente aquí:

```
src/client/resources/assets/runningskeleton/textures/gui/skeleton/
```

Con estos nombres exactos:

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

Hay un archivo `PLACE_FRAMES_HERE.txt` en esa carpeta a modo de recordatorio; puedes
borrarlo una vez añadidas las imágenes (no afecta a la compilación si lo dejas).

Si una imagen falta, Minecraft mostrará la textura "missing" (el típico cuadriculado
morado/negro) en vez de fallar o cerrar el juego.

---

## 7. Funcionamiento

### Animación
- 14 frames, reproducidos en orden de `frame_01.png` a `frame_14.png`.
- Cada frame se muestra 80 ms (≈12.5 fps), duración total ≈1.12 s. Es un valor por
  defecto razonable ya que no se especificó uno; puedes cambiarlo editando la constante
  `FRAME_DURATION_MILLIS` en `AnimationManager.java`.
- Se dibuja estirada para ocupar el 100% del área jugable, recalculando el tamaño de
  ventana en cada frame (no hay que reiniciar el juego si cambias de resolución).

### Sistema de probabilidad
- Cada segundo (mientras no haya una animación en curso ni cooldown activo) se lanza un
  único sorteo: `1` entre `chance` (por defecto 2000).
- El sorteo solo se evalúa si hay un mundo/jugador cargado (no ocurre en el menú
  principal).

### Cooldown
- Al terminar la reproducción de los 14 frames, se activa un cooldown de 5 segundos
  durante el cual la probabilidad automática no se evalúa.
- `/skeleton show` ignora el cooldown y muestra la animación de inmediato (al terminar,
  el cooldown normal de 5 s se aplica igualmente).

---

## 8. Comandos

| Comando | Descripción |
|---|---|
| `/skeleton chance <numero>` | Cambia la probabilidad a 1/`numero`. `numero` debe ser un entero ≥ 1 (Brigadier rechaza automáticamente valores inválidos o no numéricos antes de ejecutar el comando). Se guarda inmediatamente en el archivo de configuración. |
| `/skeleton show` | Muestra la animación inmediatamente, ignorando el cooldown. |
| `/skeleton info` | Muestra la probabilidad configurada, si la animación está reproduciéndose y el cooldown restante (si aplica). |

Ejemplo: `/skeleton chance 2000` → probabilidad de 1 entre 2000 por segundo.

---

## 9. Configuración persistente

- Archivo: `.minecraft/config/runningskeleton.json` (la carpeta exacta depende de tu
  instancia/launcher, pero siempre es la subcarpeta `config` de tu carpeta `.minecraft`).
- Si el archivo no existe al iniciar, se crea automáticamente con el valor por defecto
  `2000`.
- Si el archivo existe pero está corrupto o tiene un valor inválido (≤ 0), se restaura el
  valor por defecto y se sobrescribe el archivo, sin que el juego falle.

---

## 10. Cómo importar el proyecto en IntelliJ IDEA

1. Abre IntelliJ IDEA → `File > Open...` → selecciona la carpeta `RunningSkeleton`.
2. IntelliJ detectará el `build.gradle` y ofrecerá importar el proyecto Gradle; acepta.
3. **Antes de la primera importación**, genera el wrapper de Gradle (ver sección 12 —
   esto es necesario porque `gradle-wrapper.jar` no se pudo incluir en este entorno).
4. Asegúrate de que el **Gradle JVM** configurado en
   `File > Settings > Build, Execution, Deployment > Build Tools > Gradle` apunta a un
   JDK 25.
5. Espera a que Gradle sincronice (primera vez puede tardar varios minutos: descarga
   Minecraft, mappings/fuentes y Fabric API).
6. Ejecuta la tarea `genSources` si quieres código fuente legible de Minecraft en el IDE
   (opcional): panel Gradle → `runningskeleton > Tasks > fabric > genSources`.

---

## 11. Cómo ejecutar el cliente de desarrollo

Desde el panel de Gradle en IntelliJ: `runningskeleton > Tasks > fabric > runClient`.

O por terminal, una vez tengas el wrapper generado:

```
./gradlew runClient
```

Esto abrirá una instancia de Minecraft 26.2 con el mod cargado.

---

## 12. Cómo compilar el mod

```
./gradlew build
```

El `.jar` resultante aparece en `build/libs/runningskeleton-1.0.0.jar`.

### Generar el wrapper de Gradle (paso previo obligatorio en este entorno)

Este proyecto no incluye `gradlew`, `gradlew.bat` ni `gradle/wrapper/gradle-wrapper.jar`
porque el entorno en el que se generó no tiene acceso a los servidores de Gradle. Para
generarlos, con cualquier Gradle instalado localmente (o el que trae IntelliJ), ejecuta
una sola vez desde la raíz del proyecto:

```
gradle wrapper --gradle-version 9.5.1
```

Esto creará `gradlew`, `gradlew.bat` y `gradle/wrapper/gradle-wrapper.jar` apuntando a la
versión correcta (9.5.1), coherente con `gradle/wrapper/gradle-wrapper.properties` que sí
está incluido. Alternativamente, IntelliJ puede regenerarlos automáticamente al importar
si tienes la opción "Use Gradle from: 'wrapper task in Gradle build script'" y un Gradle
local disponible como fallback.

---

## 13. Cómo instalar el mod (uso normal, no desarrollo)

1. Instala Fabric Loader 0.19.3 (o superior) para Minecraft 26.2 usando el instalador
   oficial de Fabric.
2. Descarga el jar de **Fabric API 0.154.0+26.2** (o una versión más reciente compatible
   con 26.2) y colócalo en la carpeta `mods` de tu instancia.
3. Copia `build/libs/runningskeleton-1.0.0.jar` (generado en el paso anterior) también a
   la carpeta `mods`.
4. Lanza el perfil de Fabric Loader 26.2 desde el launcher de Minecraft.

---

## 14. Advertencias

- Si `/skeleton chance` recibe un valor no numérico o menor que 1, Brigadier lo rechaza
  antes de llegar a tu código (no puede provocar una excepción en tiempo de ejecución).
- El mod no hace nada en un servidor dedicado ni en la lógica de servidor: es puramente
  de cliente (`"environment": "client"` en `fabric.mod.json` y todo el código vive en el
  source set `client`).
- Si alguna imagen PNG no tiene exactamente 1024x1024, Minecraft la estirará igualmente
  para cubrir la pantalla (el `blit` siempre destina el tamaño completo de la ventana),
  así que el resultado visual dependerá de que respetes esa resolución.
