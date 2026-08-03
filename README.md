# ArchivoX — Gestor de Archivos Multi-Core y Cifrado AES-256 (Kotlin, C++, Rust)

Un gestor de archivos y motor de compresión de alto rendimiento diseñado para Android inspirándose en la velocidad, simplicidad y potencia de **ZArchiver**, denominado **ArchivoX** y construido sobre una arquitectura moderna con **Jetpack Compose**, **Kotlin Coroutines**, **C++ NDK**, **Rust Rayon/LZMA2** y **Zip4j Crypto Engine**.

---

## 🌟 Características Principales

- **Navegación y Exploración Interna 100% REAL de Archivos ZIP / 7Z / TAR / APK:**
  - Inspección directa de la estructura jerárquica interna de contenedores comprimidos sin necesidad de extraer previamente en disco.
  - Navegación multinivel por carpetas y subdirectorios internos dentro del archivo comprimido con barra de migas de pan (Breadcrumbs) interactiva.
  - Previsualización en tiempo real de archivos de texto (`.txt`, `.json`, `.kt`, `.java`, `.md`, `.xml`, `.gradle`, `.sql`) leídos directamente desde la memoria RAM del archivo ZIP.
  - Búsqueda interna filtrada de elementos dentro del archivo comprimido.
  - Compatible con formatos `.zip`, `.7z`, `.tar.gz`, `.apk`, `.jar`, `.xapk` y archivos cifrados con contraseña AES-256.
- **Extensión Nativa ArchivoX Text (.txt / .md):**
  - Módulo de extensión ligero de instalación dinámica con pantalla de carga: *"Descargando y descomprimiendo archivos esenciales"*.
  - Lectura completa de archivos `.txt` (con numeración de líneas, ajuste de tipografía sp y monoespaciada) y archivos `.md` (con renderizado de títulos Markdown, listas, bloques de código y citas).
  - Búsqueda interna con resaltado de coincidencias en tiempo real y contador de palabras/caracteres.
- **Cifrado Militar AES-256 en Archivos ZIP:**
  - Compresión de archivos ZIP 100% funcional con protección opcional mediante clave segura y cifrado de 256 bits AES.
  - Extracción transparente de archivos comprimidos cifrados con verificación de contraseña.
- **Flujo de Navegación y Diálogos Fluidos:**
  - Solucionado el flujo de acciones de archivo para que los diálogos de compresión y extracción se abran directamente sin requerir clics adicionales.
- **Delegación a Núcleos Secundarios de CPU:**
  - Las tareas de compresión y descompresión pesadas no congelan la interfaz de usuario, ejecutándose en un pool de hilos asignados a núcleos secundarios de la CPU.
- **Motores Nativos Multi-Lenguaje (C++ / Rust / Zip4j):**
  - **Zip4j AES Engine:** Algoritmo Deflate puro con cifrado de alto nivel AES-256 para `.zip`.
  - **C++ (NDK LibArchive / Pthreads):** Soporte multi-hilo para formatos `.rar`, `.tar.gz`.
  - **Rust (LZMA2 / Rayon Threadpool):** Algoritmos de alta densidad para formato `.7z`.
- **Extracción de Logos e Íconos de APKs Real:**
  - Decodificación y carga dinámica en tiempo real del ícono original de aplicaciones y juegos Android contenidas dentro de archivos `.apk`.
- **Visor Nativo de Claves y Certificados PEM:**
  - Visor especializado en tiempo real para archivos de seguridad `.pem`, `.key`, `.crt`, `.cer`, `.pub`, `.p8` con inspector de huellas SHA-256 y copia de Base64.
- **Personalización de Apariencia y Temas:**
  - **Verde Esmeralda (Clásico ZArchiver)**
  - **Negro Puro (AMOLED)**
  - **Material You (Dinámico)**
- **Visualización Flexible y Gestión Completa:**
  - Modos de vista en Lista y Cuadrícula Adaptativa.
  - Barra de navegación tipo migas de pan (Breadcrumbs) interactiva.
  - Creación de carpetas, renombrado, eliminación y copia de rutas.
  - Filtros por categoría (Carpetas, Descargas, Documentos, Imágenes, Audio, Video, Archivos).

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

### Compilación Automatizada y CI/CD (GitHub Actions)
- **Compilación de APK Debug (`.github/workflows/android.yml`):** Activación manual (`workflow_dispatch`), caché de Gradle/Cargo y generación de llave en caliente (`keytool`) para firmar automáticamente la APK.
- **Reemplazo Automático de Código mediante ZIP (`.github/workflows/unpack_zip.yml`):** Acción activada automáticamente al subir un archivo `.zip` dentro de la carpeta `/zip`. Extrae el código contenido en el `.zip`, sobreescribe la raíz del proyecto y realiza commit/push autenticado con tu GitHub App (Bot personalizado con avatar y nombre asignado).

---

## 📄 Licencia

Este proyecto se distribuye bajo la Licencia MIT.

