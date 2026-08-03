# ZArchiver Native — Gestor de Archivos Multi-Core (Kotlin, C++, Rust)

Un gestor de archivos y motor de compresión de alto rendimiento diseñado para Android inspirándose en la velocidad, simplicidad y potencia de **ZArchiver**, repensado con una arquitectura moderna utilizando **Jetpack Compose**, **Kotlin Coroutines**, **C++ NDK LibArchive** y **Rust Rayon/LZMA2**.

---

## 🌟 Características Principales

- **Delegación a Núcleos Secundarios de CPU:**
  - La compresión y extracción no congelan la interfaz de usuario. Las operaciones pesadas se delegan automáticamente a hilos de procesamiento en núcleos secundarios de la CPU.
- **Motores Nativos Multi-Lenguaje (C++ / Rust / Kotlin):**
  - **C++ (NDK LibArchive / Pthreads):** Soporte multi-hilo para formatos `.zip`, `.rar`, `.tar.gz`.
  - **Rust (LZMA2 / Rayon Threadpool):** Algoritmos de alta densidad para formato `.7z`.
  - **Kotlin Engine:** Manejo nativo resiliente de fallback `ZipOutputStream`/`ZipInputStream`.
- **Extracción de Logos e Íconos de APKs Real:**
  - Decodificación y carga dinámica en tiempo real del ícono original de aplicaciones y juegos Android contenidas dentro de archivos `.apk`.
- **Visor Nativo de Claves y Certificados PEM:**
  - Visor especializado en tiempo real para archivos de seguridad `.pem`, `.key`, `.crt`, `.cer`, `.pub`, `.p8`.
  - Detección automática de encabezados cryptographic (RSA PKCS#1, PKCS#8, EC, OpenSSH, Certificados X.509).
  - Indicador visual de estado de cifrado (🔒 Cifrada / Libre), número de líneas, tamaño en bytes y huella digital SHA-256.
  - Copia rápida en un toque de bloques Base64 puros o del archivo PEM completo.
- **Personalización de Apariencia y Temas:**
  - **Verde Esmeralda (Clásico ZArchiver):** Tonalidades esmeralda con acentos amarillos para carpetas.
  - **Negro Puro (AMOLED):** Fondo totalmente negro (#000000) ideal para pantallas OLED y ahorro energético.
  - **Material You (Dinámico):** Integración nativa con la paleta de colores del sistema Android 12+.
  - **Modo Claro / Modo Oscuro:** Transición fluida con Material Design 3.
- **Visualización Flexible y Gestión Completa:**
  - Modos de vista en Lista y Cuadrícula Adaptativa.
  - Barra de navegación tipo migas de pan (Breadcrumbs) interactiva.
  - Creación de carpetas, renombrado, eliminación y copia de rutas.
  - Filtros por categoría (Carpetas, Descargas, Documentos, Imágenes, Audio, Video, Archivos).
  - Interruptor para mostrar u ocultar archivos del sistema que inician con punto (`.`).

---

## 🚀 Cómo Compilar el Proyecto

### Requisitos Previos
- Android Studio Ladybug / Jellyfish o Gradle 8.x
- JDK 17
- Android NDK (para módulo C++)
- Rust Toolchain (`cargo` + `aarch64-linux-android` target)

### Compilación Local
```bash
# Compilar la aplicación Debug
gradle :app:assembleDebug

# Ejecutar tests unitarios
gradle :app:testDebugUnitTest
```

### Compilación Automatizada y Automatización CI/CD (GitHub Actions)
- **Compilación de APK Debug (`.github/workflows/android.yml`):** Activación manual (`workflow_dispatch`), caché de Gradle/Cargo y generación de llave en caliente (`keytool`) para firmar automáticamente la APK.
- **Reemplazo Automático de Código mediante ZIP (`.github/workflows/unpack_zip.yml`):** Acción activada al subir archivos `.zip` al repositorio o mediante `workflow_dispatch`. Extrae el código, sobreescribe el repositorio y realiza commit/push autenticado con una GitHub App (Bot personalizado con avatar y nombre configurable).

---

## 📄 Licencia

Este proyecto se distribuye bajo la Licencia MIT.
