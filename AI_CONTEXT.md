# AI Context — Contexto del Sistema para Inteligencia Artificial

Este archivo proporciona contexto técnico profundo a modelos de lenguaje (LLMs) y agentes de IA que analicen o modifiquen este código.

---

## 🛠 Tech Stack de la Aplicación

- **Lenguaje:** Kotlin 2.x
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Concurrencia:** Kotlin Coroutines (`Dispatchers.IO`, `withContext`, `StateFlow`)
- **Arquitectura UI:** MVVM (Model-View-ViewModel) con `AndroidViewModel`
- **Módulos Nativos:**
  - **C++ (NDK CMake):** `app/src/main/cpp/native-archive.cpp`
  - **Rust (Cargo):** `rust-native/src/lib.rs`
- **Decodificación de Recursos:** `PackageManager.getPackageArchiveInfo` + `produceState` para la extracción de logotipos en archivos `.apk`.

---

## 📐 Convenciones y Reglas del Proyecto

1. **Gestión de Permisos:**
   - La aplicación utiliza `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION` en Android 11+ (API 30+) para garantizar el acceso a todos los archivos del usuario en `/storage/emulated/0`.
   - Mantiene compatibilidad con `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` para versiones anteriores.

2. **Integridad del Tema y Paletas:**
   - No se deben colocar valores hexadecimales quemados en Composables.
   - Usar siempre `MaterialTheme.colorScheme` o los valores centralizados en `Color.kt`.
   - Las tres variantes activas son:
     - `AppColorPalette.EMERALD`: Estilo ZArchiver clásico.
     - `AppColorPalette.AMOLED`: Fondo #000000 puro.
     - `AppColorPalette.MATERIAL_YOU`: Colores dinámicos del sistema Android 12+.

3. **Manejo de Operaciones de Compresión:**
   - La clase `NativeArchiveEngine` emite estados mediante callbacks de `CompressionProgress`.
   - La barra de progreso de compresión muestra velocidad en MB/s, hilo secundario activo asignado y logs detallados estilo consola.

4. **Visor Nativo de Claves y Certificados PEM (`PemViewerDialog.kt`):**
   - Manejo especializado de archivos `.pem`, `.key`, `.crt`, `.cer`, `.pub`, `.p8`.
   - Inspección sintáctica sin alterar la codificación original ni requerir renombrado a `.txt`.
   - Cálculo de huella digital SHA-256 en memoria, aislamiento de bloque Base64 y copia directa al portapapeles.

5. **Identificadores y TestTags:**
   - Se deben mantener las etiquetas `Modifier.testTag("...")` en los botones y campos interactivos clave para soportar pruebas automatizadas y localización de componentes por agentes de IA.
