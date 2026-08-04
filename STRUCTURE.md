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
│           │   │   ├── ArchiveCompressor.kt # Motor de compresión multi-formato (7z LZMA2, TarGz, Zip4j AES-256)
│           │   │   ├── ArchiveExtractor.kt  # Motor de descompresión y extracción multi-formato
│           │   │   ├── ArchiveSplitter.kt   # Utilidad de división de archivos en partes/volúmenes
│           │   │   ├── CompressionModels.kt # Enums y Data Class (CompressionFormat, CompressionLevel, CompressionProgress)
│           │   │   ├── FileItem.kt    # Modelo de datos para archivos y carpetas
│           │   │   ├── FileRepository.kt # Repositorio de lectura y operaciones I/O
│           │   │   ├── NativeArchiveEngine.kt # Fachada JNI (C++/Rust) y punto de entrada de compresión
│           │   │   └── ZipViewerEngine.kt     # Motor de inspección, árbol de directorios y lectura interna
│           │   ├── ui/
│           │   │   ├── FileManagerFilterAndSort.kt # Lógica modular de filtrado y ordenación
│           │   │   ├── FileManagerUiState.kt    # Estado de la UI inmutable (FileManagerUiState) y enums de acción
│           │   │   ├── FileManagerViewModel.kt  # ViewModel principal
│           │   │   ├── FileManagerScreen.kt     # Composable raíz de la interfaz ArchivoX
│           │   │   ├── components/
│           │   │   │   ├── ArchivoXTextViewerDialog.kt # Editor y visor en pantalla completa de archivos TXT y MD
│           │   │   │   ├── ArchivePasswordDialog.kt   # Diálogo para ingresar contraseña de archivos protegidos
│           │   │   │   ├── BreadcrumbBar.kt       # Navegación por rutas de archivos
│           │   │   │   ├── CompressFileDialog.kt  # Diálogo de compresión con opciones de formato, nivel y clave AES-256
│           │   │   │   ├── CompressionProgressDialog.kt # Diálogo modal con consola de logs de compresión en tiempo real
│           │   │   │   ├── ExtractArchiveDialog.kt # Diálogo de extracción de archivos comprimidos
│           │   │   │   ├── FileActionDialogs.kt   # Diálogos de creación de carpeta, renombrado, eliminación y nuevo archivo
│           │   │   │   ├── FileDetailsBottomSheet.kt # Hoja de detalles y acciones principales sobre un archivo
│           │   │   │   ├── FileManagerEmptyState.kt # Componente visual de estado de carpeta vacía
│           │   │   │   ├── FileManagerTopBar.kt   # Barra superior con búsqueda, ordenación y cambio de vista
│           │   │   │   ├── FileItemCard.kt        # Vista en Cuadrícula (Grid Card)
│           │   │   │   ├── FileItemRow.kt         # Vista en Lista
│           │   │   │   ├── FolderPickerDialog.kt   # Diálogo selector de destino legacy
│           │   │   │   ├── PasteBottomBar.kt       # Barra inferior estilo ZArchiver para navegación real de pegado
│           │   │   │   ├── PemViewerDialog.kt     # Visor Nativo de Claves y Certificados PEM
│           │   │   │   ├── SettingsSheet.kt       # Hoja de configuración de temas y paletas
│           │   │   │   ├── StorageHeader.kt       # Card de espacio de almacenamiento y filtros
│           │   │   │   ├── TextEditorControlBar.kt # Barra de controles y ajustes tipográficos para el visor/editor
│           │   │   │   ├── TextExtensionInstallDialog.kt # Diálogo modal de instalación del módulo de extensión ArchivoX Text
│           │   │   │   ├── TextMode.kt            # Enum de modo de lectura/edición (PREVIEW, EDIT)
│           │   │   │   ├── TextRenderers.kt       # Renderizadores de texto plano con numeración de líneas y Markdown
│           │   │   │   ├── ZipEntryRow.kt         # Fila e íconos para elementos dentro de archivos comprimidos
│           │   │   │   └── ZipExplorerDialog.kt    # Diálogo modal de navegación y exploración interna
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

1. **Separación de Responsabilidades (MVVM & Desarrollo Modular):**
   - El estado de la pantalla está centralizado en `FileManagerUiState` (`FileManagerUiState.kt`).
   - `FileManagerViewModel` coordina las operaciones, delegando el filtrado/ordenado a `FileManagerFilterAndSort.kt`.
   - Componentes UI de acción modularizados (`FileDetailsBottomSheet`, `FolderPickerDialog`, `ArchivePasswordDialog`, `FileActionDialogs`).

2. **Cifrado AES-256 & Multiprocesamiento C++ / Rust / Zip4j:**
   - Cifrado seguro de archivos ZIP mediante la integración con `Zip4j` en `ArchiveCompressor.kt`.
   - Descomprimidor y extractor delegados en `ArchiveExtractor.kt`.
   - Preservación de firmas JNI externas en `NativeArchiveEngine.kt` para interoperabilidad con C++ y Rust.
   - Las operaciones de compresión crean un grupo de hilos dedicados (`Executors.newFixedThreadPool`) para delegar el trabajo a núcleos secundarios de la CPU sin bloquear el hilo principal.
