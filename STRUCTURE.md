# Estructura del Proyecto ZArchiver Native

Este documento describe la organización jerárquica de archivos y carpetas del repositorio, facilitando la navegación tanto para desarrolladores como para agentes de Inteligencia Artificial.

```
/
├── .github/
│   └── workflows/
│       └── android.yml                # CI/CD GitHub Actions con firma en caliente y caché
├── app/                               # Módulo principal de Android
│   ├── build.gradle.kts               # Configuración del módulo de aplicación
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml    # Permisos de almacenamiento y componentes
│           ├── cpp/                   # Código Nativo C++
│           │   ├── CMakeLists.txt     # Script de construcción CMake para NDK
│           │   └── native-archive.cpp # Implementación JNI en C++ (Multi-Core Pthreads)
│           ├── java/com/example/
│           │   ├── MainActivity.kt    # Entry Point con permisos runtime Android R+
│           │   ├── data/
│           │   │   ├── FileItem.kt    # Modelo de datos para archivos y carpetas
│           │   │   ├── FileRepository.kt # Repositorio de lectura y operaciones I/O
│           │   │   └── NativeArchiveEngine.kt # Motor JNI / Coroutines con C++, Rust y Kotlin
│           │   ├── ui/
│           │   │   ├── FileManagerViewModel.kt # ViewModel principal y UI State
│           │   │   ├── FileManagerScreen.kt    # Composable raíz de la interfaz
│           │   │   ├── components/
│           │   │   │   ├── BreadcrumbBar.kt       # Navegación por rutas de archivos
│           │   │   │   ├── CompressionDialogs.kt  # Diálogos de compresión y progreso
│           │   │   │   ├── FileActionDialogs.kt   # Hoja de acciones (Renombrar, Eliminar, Detalle)
│           │   │   │   ├── FileItemCard.kt        # Vista en Cuadrícula (Grid Card)
│           │   │   │   ├── FileItemRow.kt         # Vista en Lista
│           │   │   │   ├── SettingsSheet.kt       # Hoja de configuración de temas y paletas
│           │   │   │   └── StorageHeader.kt       # Card de espacio de almacenamiento y filtros
│           │   │   └── theme/
│           │   │       ├── Color.kt               # Paletas Verde Esmeralda, AMOLED y Light
│           │   │       ├── Theme.kt               # Composable de tema dinámico M3
│           │   │       └── ThemeSettings.kt       # Enums de paletas de color y modo claro/oscuro
│           │   └── util/
│           │       └── ApkIconUtils.kt            # Decodificador de íconos nativos para archivos .apk
│           └── res/                   # Recursos visuales, valores e íconos adaptativos
├── rust-native/                       # Módulo Nativo en Rust
│   ├── Cargo.toml                     # Configuración de librerías Cargo (JNI, Rayon, Flate2)
│   └── src/
│       └── lib.rs                     # Implementación JNI en Rust con paralelismo Rayon
├── build.gradle.kts                   # Build gradle raíz del proyecto
├── settings.gradle.kts                # Inclusión de proyectos y repositorios
├── metadata.json                      # Metadatos para la plataforma AI Studio
├── README.md                          # Descripción general del proyecto
├── STRUCTURE.md                       # Arbol y mapa de la arquitectura (Este archivo)
├── AI_CONTEXT.md                      # Contexto técnico para agentes de Inteligencia Artificial
├── ROADMAP.md                         # Estado actual y hoja de ruta de características
└── AGENTS.md                          # Reglas y directrices de desarrollo para agentes AI
```

---

## Aspectos Clave de la Arquitectura

1. **Separación de Responsabilidades (MVVM):**
   - El estado de la pantalla está centralizado en `FileManagerUiState` inmutable.
   - `FileManagerViewModel` coordina la carga de almacenamiento, selección de temas y despacho de tareas de compresión.

2. **Multiprocesamiento C++ / Rust / Kotlin:**
   - La capa nativa interactúa mediante las firmas JNI expuestas en `NativeArchiveEngine.kt`.
   - Las operaciones de compresión crean un grupo de hilos dedicados (`Executors.newFixedThreadPool`) para delegar el trabajo a núcleos secundarios de la CPU sin bloquear el hilo principal.
