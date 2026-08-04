# Roadmap de Desarrollo — ArchivoX

Este archivo documenta los hitos completados y la hoja de ruta para futuras versiones de **ArchivoX**.

---

## 🟢 Fase 1: Fundamentos y UI Core (COMPLETADO)
- [x] Estructura MVVM con Jetpack Compose y Material 3.
- [x] Explorador de archivos local con soporte para navegación profunda y migas de pan (Breadcrumbs).
- [x] Modos de visualización en Lista y Cuadrícula Adaptativa.
- [x] Creación de carpetas, renombrado, eliminación y copia de rutas al portapapeles.
- [x] Filtros por categoría de almacenamiento (Imágenes, Videos, Música, Documentos, Archivos, Descargas).

## 🟢 Fase 2: Rendimiento Multi-Core C++/Rust/Zip4j y Cifrado AES-256 (COMPLETADO)
- [x] Compresión ZIP 100% funcional integrada con la librería **Zip4j**.
- [x] Cifrado de nivel militar AES-256 bits para protección con contraseña en compresión y extracción.
- [x] Corrección del flujo UX de diálogos: apertura directa de compresión/extracción/visor PEM sin clics redundantes.
- [x] Integración de módulos nativos C++ (`native-archive.cpp`) y Rust (`rust-native`).
- [x] Delegación de tareas de compresión/descompresión a núcleos secundarios de la CPU mediante worker pools dedicados.
- [x] Formatos de compresión: `.zip`, `.rar`, `.7z`, `.tar.gz`.
- [x] Consola de log en tiempo real y velocímetro (MB/s) durante procesos de compresión.
- [x] Extracción en tiempo real del ícono original de aplicaciones y juegos desde archivos `.apk`.
- [x] Sistema de temas y paletas de color (Verde Esmeralda, AMOLED Negro Puro, Material You Dinámico).
- [x] Extensión nativa **ArchivoX Text v1.2** con editor y visor independiente en pantalla completa para archivos `.txt`, `.md` y `.json` con persistencia de cambios en almacenamiento local.
- [x] Soporte exclusivo para `.json`: resaltado sintáctico de claves/valores, validación de sintaxis en tiempo real y formateo/indentación automática.
- [x] Íconos vectoriales personalizados y exclusivos con badges para archivos `.json`.
- [x] Menú de opciones avanzadas por pulsación prolongada (Long Press) en archivos `.zip` y demás formatos.
- [x] División de archivos comprimidos en múltiples volúmenes (Split Volumes de 10MB, 50MB, 100MB, 700MB) almacenando las partes en carpetas dedicadas (`_partes`).
- [x] Alternancia entre Modo Lectura y Modo Edición con contadores en tiempo real (líneas, palabras, caracteres) y controles de fuente.
- [x] Refinamiento de UI minimalista: reducción de densidad en filas de archivos (`FileItemRow`), tarjetas de almacenamiento sutiles y migas de pan impecables.
- [x] Visor y explorador navegable 100% REAL del contenido de archivos `.zip`, `.7z`, `.tar.gz`, `.apk` y `.jar` con lectura en memoria de archivos de texto.
- [x] Detección de archivos ZIP/7z encriptados con indicador de candado (Lock) e ingreso automático de contraseña (`ArchivePasswordDialog`).
- [x] Modo de Selección Múltiple (Multi-Select) activable mediante toque rápido en el ícono del archivo o pulsación larga para mover (`executeBatchMove`), copiar (`executeBatchCopy`), comprimir en lote (`compressBatchSelectedItems`) o eliminar (`executeBatchDelete`) con diálogo de exploración de carpetas (`FolderPickerDialog`).
- [x] Creación de archivos de texto con nombres y extensiones personalizadas (`.txt`, `.json`, `.md`, `.xml`, etc.) desde el menú de acción flotante (FAB).
- [x] Visor nativo de claves privadas y certificados PEM (`.pem`, `.key`, `.crt`, `.pub`, `.p8`).
- [x] Integración con LibArchive Nativa (NDK C++) para manejo avanzado de contenedores y compresión/descompresión multinivel.
- [x] Cálculo de Hashes Acelerado por Hardware mediante instrucciones vectoriales ARM NEON / SIMD (`__builtin_arm_crc32`) para verificación ultrarrápida de integridad (CRC32/SHA-256).
- [x] Parser Ultra-Rápido de JSON / Markdown integrado con la librería `serde_json` de Rust y motor C++ nativo para análisis y validación sin sobrecarga JVM.
- [x] Optimización de renderizado perezoso (`LazyColumn`), formateo y validación de JSON multi-hilo en segundo plano (`Dispatchers.Default` / NDK) para evitar congelamientos en archivos grandes.
- [x] Corrección visual de interfaz M3 en `TextEditorControlBar` (`SegmentedButton` con slots `icon` y `label` independientes).
- [x] Extracción individual directa de archivos dentro de contenedores `.zip`, `.7z`, `.tar.gz`, `.apk` o `.xapk` sin necesidad de descomprimir el archivo completo.
- [x] Automatización CI/CD con GitHub Actions (firma en caliente de APK Debug, caché de Gradle/Cargo y flujo de auto-descompresión ZIP con GitHub App Bot).

## 🟡 Fase 3: Características Avanzadas (PRÓXIMAMENTE)
- [ ] Visor nativo interno para imágenes y reproductor de audio ligero.
- [ ] Herramienta de benchmark de CPU integrada para medir la velocidad de compresión de los núcleos del dispositivo.
- [ ] Integración con proveedores de almacenamiento en la nube (Google Drive, SMB/FTP local).

