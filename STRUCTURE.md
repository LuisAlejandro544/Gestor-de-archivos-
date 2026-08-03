# Estructura del Proyecto ArchivoX

Este documento describe la organización jerárquica de archivos y carpetas del repositorio **ArchivoX**, facilitando la navegación tanto para desarrolladores como para agentes de Inteligencia Artificial.

```
/
├── .github/
│   └── workflows/
│       ├── android.yml                # CI/CD GitHub Actions con firma en caliente y caché
│       └── unpack_zip.yml             # Auto Unpack ZIP & Overwrite Codebase
├── app/                               # Módulo principal de Android
│   ├── build.gradle.kts               # Configuración del módulo de aplicación (Zip4j, NDK, Compose)
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
│           │   │   ├── NativeArchiveEngine.kt # Motor JNI / Zip4j AES-256 / Coroutines C++ y Rust
│           │   │   └── ZipViewerEngine.kt     # Motor de inspección, árbol de directorios y lectura interna de ZIP/7z/Tar/APK
│           │   ├── ui/
│           │   │   ├── FileManagerViewModel.kt # ViewModel principal y UI State
│           │   │   ├── FileManagerScreen.kt    # Composable raíz de la interfaz ArchivoX
│           │   │   ├── components/
│           │   │   │   ├── ArchivoXTextViewerDialog.kt # Diálogo visor de archivos TXT y MD (Extensión ArchivoX Text)
│           │   │   │   ├── BreadcrumbBar.kt       # Navegación por rutas de archivos
│           │   │   │   ├── CompressFileDialog.kt  # Diálogo de compresión con opciones de formato, nivel y clave AES-256
│           │   │   │   ├── CompressionProgressDialog.kt # Diálogo modal con consola de logs de compresión en tiempo real
│           │   │   │   ├── ExtractArchiveDialog.kt # Diálogo de extracción de archivos comprimidos
│           │   │   │   ├── FileActionDialogs.kt   # Hoja de acciones (Renombrar, Eliminar, Detalle)
│           │   │   │   ├── FileManagerEmptyState.kt # Componente visual de estado de carpeta vacía
│           │   │   │   ├── FileManagerTopBar.kt   # Barra superior con búsqueda, ordenación y cambio de vista
│           │   │   │   ├── FileItemCard.kt        # Vista en Cuadrícula (Grid Card)
│           │   │   │   ├── FileItemRow.kt         # Vista en Lista
│           │   │   │   ├── PemViewerDialog.kt     # Visor Nativo de Claves y Certificados PEM
│           │   │   │   ├── SettingsSheet.kt       # Hoja de configuración de temas y paletas
│           │   │   │   ├── StorageHeader.kt       # Card de espacio de almacenamiento y filtros
│           │   │   │   ├── TextExtensionInstallDialog.kt # Diálogo modal de instalación del módulo de extensión ArchivoX Text
│           │   │   │   ├── TextRenderers.kt       # Renderizadores de texto plano con numeración de líneas y Markdown
│           │   │   │   └── ZipExplorerDialog.kt    # Diálogo modal de navegación y exploración interna de archivos .ZIP/7Z/APK
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
├── settings.gradle.kts                # Inclusión de proyectos y repositorios (rootProject.name = "ArchivoX")
├── metadata.json                      # Metadatos para la plataforma AI Studio
├── README.md                          # Descripción general del proyecto ArchivoX
├── STRUCTURE.md                       # Árbol y mapa de la arquitectura (Este archivo)
├── AI_CONTEXT.md                      # Contexto técnico para agentes de Inteligencia Artificial
├── ROADMAP.md                         # Estado actual y hoja de ruta de características
└── AGENTS.md                          # Reglas y directrices de desarrollo para agentes AI
```

---

## Aspectos Clave de la Arquitectura

1. **Separación de Responsabilidades (MVVM):**
   - El estado de la pantalla está centralizado en `FileManagerUiState` inmutable.
   - `FileManagerViewModel` coordina la carga de almacenamiento, selección de temas y despacho de tareas de compresión.

2. **Cifrado AES-256 & Multiprocesamiento C++ / Rust / Zip4j:**
   - Cifrado seguro de archivos ZIP mediante la integración con `Zip4j` en `NativeArchiveEngine.kt`.
   - Las operaciones de compresión crean un grupo de hilos dedicados (`Executors.newFixedThreadPool`) para delegar el trabajo a núcleos secundarios de la CPU sin bloquear el hilo principal.

