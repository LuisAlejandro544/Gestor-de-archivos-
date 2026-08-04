# AI Context — ArchivoX System Context

Este archivo proporciona contexto técnico profundo a modelos de lenguaje (LLMs) y agentes de IA que analicen o modifiquen el código de **ArchivoX**.

---

## 🛠 Tech Stack de la Aplicación

- **Nombre de la App:** ArchivoX
- **Lenguaje:** Kotlin 2.x
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Concurrencia:** Kotlin Coroutines (`Dispatchers.IO`, `withContext`, `StateFlow`)
- **Arquitectura UI:** MVVM (Model-View-ViewModel) con `AndroidViewModel`
- **Librería de Cifrado ZIP:** `Zip4j` (`net.lingala.zip4j`) con cifrado militar AES-256 bits y algoritmos Deflate/Fastest/Ultra.
- **Módulos Nativos:**
  - **C++ (NDK CMake):** `app/src/main/cpp/native-archive.cpp` — Motor C++ LibArchive nativo con paralelismo Pthreads, aceleración vectorial ARM NEON SIMD (`__builtin_arm_crc32`) para cálculo de hashes de ultra alta velocidad y validación de sintaxis nativa.
  - **Rust (Cargo):** `rust-native/src/lib.rs` — Módulo nativo Rust con `serde_json` para análisis ultra-rápido de JSON/Markdown y hilos paralelos Rayon/LZMA2.
- **Decodificación de Recursos:** `PackageManager.getPackageArchiveInfo` + `produceState` para la extracción de logotipos en archivos `.apk`.

---

## 📐 Convenciones y Reglas del Proyecto

1. **Gestión de Permisos:**
   - La aplicación utiliza `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` en Android 11+ (API 30+) para garantizar el acceso a todos los archivos del usuario en `/storage/emulated/0`.

2. **Cifrado y Compresión (Zip4j + NDK):**
   - Soporte 100% funcional para archivos ZIP cifrados con contraseña mediante la especificación AES-256.
   - Flujo de interacción optimizado: las opciones de la hoja de acciones ejecutan los diálogos de compresión/extracción de forma fluida.

3. **Manejo de Operaciones de Compresión y Exploración ZIP:**
   - La clase `NativeArchiveEngine` emite estados mediante callbacks de `CompressionProgress` y realiza la partición en volúmenes (split archive) de 10MB, 50MB, 100MB o 700MB guardando las partes en una carpeta dedicada `_partes`.
   - La clase `ZipViewerEngine` genera la jerarquía virtual de carpetas y archivos dentro de `.zip`, `.7z`, `.tar.gz`, `.apk` y `.jar`.
   - `ZipExplorerDialog` permite navegar por subcarpetas dentro del ZIP con breadcrumbs, buscar elementos y previsualizar archivos de texto en RAM sin extraer en disco.
   - La consola en tiempo real muestra velocidad en MB/s, hilo secundario activo asignado y logs estilo terminal.

5. **Modulo ArchivoX Text v1.2 (`ArchivoXTextViewerDialog.kt`):**
   - Pantalla completa independiente (`DialogProperties(usePlatformDefaultWidth = false)`).
   - Alternancia entre `TextMode.PREVIEW` (lectura con numeración de líneas, sintaxis Markdown o JSON resaltado con badge de validación sintáctica) y `TextMode.EDIT` (edición interactiva mediante `OutlinedTextField`).
   - Botón de formateo e indentación de JSON automático (`formatJsonContent()`).
   - Persistencia de cambios asíncrona mediante `Dispatchers.IO` escribiendo en `File(item.path)`.
   - Cálculo en tiempo real de estadísticas de lectura (líneas, palabras, caracteres).

