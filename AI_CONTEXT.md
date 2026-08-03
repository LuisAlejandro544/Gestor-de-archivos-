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
  - **C++ (NDK CMake):** `app/src/main/cpp/native-archive.cpp`
  - **Rust (Cargo):** `rust-native/src/lib.rs`
- **Decodificación de Recursos:** `PackageManager.getPackageArchiveInfo` + `produceState` para la extracción de logotipos en archivos `.apk`.

---

## 📐 Convenciones y Reglas del Proyecto

1. **Gestión de Permisos:**
   - La aplicación utiliza `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` en Android 11+ (API 30+) para garantizar el acceso a todos los archivos del usuario en `/storage/emulated/0`.

2. **Cifrado y Compresión (Zip4j + NDK):**
   - Soporte 100% funcional para archivos ZIP cifrados con contraseña mediante la especificación AES-256.
   - Flujo de interacción optimizado: las opciones de la hoja de acciones ejecutan los diálogos de compresión/extracción de forma fluida.

3. **Manejo de Operaciones de Compresión:**
   - La clase `NativeArchiveEngine` emite estados mediante callbacks de `CompressionProgress`.
   - La consola en tiempo real muestra velocidad en MB/s, hilo secundario activo asignado y logs estilo terminal.

4. **Visor Nativo PEM (`PemViewerDialog.kt`):**
   - Inspección sintáctica sin alterar la codificación original ni requerir renombrado a `.txt`.

