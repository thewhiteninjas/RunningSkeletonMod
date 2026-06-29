# RunningSkeleton Mod — NeoForge 1.21.1

Un mod para Minecraft Java 1.21.1 (NeoForge) que muestra una animación GIF a pantalla completa con una probabilidad configurable.

---

## 📁 Estructura del proyecto

```
RunningSkeleton/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/main/
    ├── java/com/runningskeleton/mod/
    │   ├── RunningSkeleton.java          ← Clase principal del mod
    │   ├── client/overlay/
    │   │   └── SkeletonOverlay.java      ← Lógica de la animación y overlay
    │   └── command/
    │       └── SkeletonCommand.java      ← Comandos de chat
    └── resources/
        ├── META-INF/
        │   └── neoforge.mods.toml
        └── assets/runningskeleton/
            └── textures/overlay/
                ├── frame_01.png   ← ¡COLOCA AQUÍ TUS 14 FRAMES!
                ├── frame_02.png
                ├── ...
                └── frame_14.png
```

---

## 🖼️ Cómo añadir tus frames (PNG)

Copia tus archivos de imagen en la carpeta:
```
src/main/resources/assets/runningskeleton/textures/overlay/
```

Los archivos deben llamarse exactamente:
- `frame_01.png`
- `frame_02.png`
- ... hasta ...
- `frame_14.png`

> **Nota:** El mod estira cada frame al tamaño completo de la ventana. Puedes usar cualquier resolución, pero se recomienda que sean imágenes con fondo transparente (PNG con canal alpha) si quieres ver el juego detrás.

---

## 🛠️ Configuración en IntelliJ IDEA

### Prerrequisitos
- **JDK 21** (OpenJDK o Microsoft OpenJDK)
- **IntelliJ IDEA** (Community o Ultimate)
- **Git** (opcional)

### Pasos

1. **Abrir el proyecto:**
   - File → Open → selecciona la carpeta `RunningSkeleton`
   - IntelliJ detectará automáticamente el proyecto Gradle

2. **Importar el proyecto Gradle:**
   - Si no se importa solo, haz clic en el botón "Load Gradle Project" que aparece en la esquina
   - Espera a que descargue todas las dependencias (puede tardar varios minutos la primera vez)

3. **Generar configuraciones de ejecución:**
   Abre una terminal en IntelliJ y ejecuta:
   ```
   ./gradlew genIntellijRuns
   ```
   (En Windows: `gradlew.bat genIntellijRuns`)

4. **Ejecutar el cliente:**
   - Aparecerá una configuración llamada `runClient` en la esquina superior derecha
   - Pulsa el botón ▶ para lanzar Minecraft con el mod

---

## 🔨 Compilar el mod (JAR)

```bash
./gradlew build
```

El JAR compilado aparecerá en:
```
build/libs/runningskeleton-1.0.0.jar
```

Cópialo a la carpeta `mods/` de tu instalación de Minecraft con NeoForge 1.21.1.

---

## 💬 Comandos en el juego

> Requieren permiso de operador (nivel 2).

| Comando | Descripción |
|---|---|
| `/skeleton chance <número>` | Cambia la probabilidad. Ej: `/skeleton chance 500` → 1 de cada 500 segundos |
| `/skeleton show` | Fuerza que aparezca la animación ahora mismo |
| `/skeleton info` | Muestra la probabilidad actual |

### Ejemplos:
```
/skeleton chance 100    → 1% de probabilidad por segundo (muy frecuente)
/skeleton chance 2000   → 1/2000 por segundo (por defecto)
/skeleton chance 10000  → muy raro
/skeleton show          → activa la animación al instante
```

---

## ⚙️ Cómo funciona

- **Cada segundo** se hace una tirada aleatoria con probabilidad `1/chanceOneIn`
- Si la tirada sale favorecida, empieza la animación
- Los **14 frames** se muestran en secuencia a ~15 fps (71ms por frame)
- La animación cubre **toda la pantalla**
- Al terminar, hay un **cooldown de 5 segundos** antes de que pueda volver a salir
- No aparece si estás en un menú/inventario abierto

---

## 🐛 Solución de problemas

**"No encuentro la opción genIntellijRuns":**
- Asegúrate de usar JDK 21, no JRE
- Revisa que Gradle haya terminado de sincronizar sin errores

**Las texturas no aparecen (pantalla negra al activarse):**
- Verifica que los archivos estén en `src/main/resources/assets/runningskeleton/textures/overlay/`
- Confirma que se llamen exactamente `frame_01.png` ... `frame_14.png`

**El comando no funciona:**
- Necesitas ser OP. En singleplayer siempre eres OP.
- En multijugador: `/op TuNombre`
