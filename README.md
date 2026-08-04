# ArchivoX — Gestor de Archivos Multi-Core y Cifrado AES-256 (Kotlin, C++, Rust)

Un gestor de archivos y motor de compresión de alto rendimiento diseñado para Android inspirándose en la velocidad, simplicidad y potencia de **ZArchiver**, denominado **ArchivoX** y construido sobre una arquitectura moderna con **Jetpack Compose**, **Kotlin Coroutines**, **C++ NDK**, **Rust Rayon/LZMA2** y **Zip4j Crypto Engine**.

---

## 🌟 Características Principales

- **Selección Múltiple Intuitiva y Acciones en Lote (Mover, Copiar, Comprimir, Eliminar):**
  - **Activación Ultrarrápida por Toque de Ícono:** Al presionar directamente el ícono izquierdo de cualquier archivo o carpeta (o mediante clic largo), se activa de inmediato el modo de selección múltiple.
  - **Barra Superior e Inferior de Selección:** Muestra en tiempo real la cantidad de elementos marcados, botón para "Seleccionar todo" / "Desmarcar todo" y menú de acciones masivas.
  - **Compresión Masiva en Lote:** Permite comprimir múltiples archivos y carpetas seleccionados simultáneamente a formatos `.zip`, `.7z` o `.tar.gz` con clave opcional AES-256 y particionado.
  - **Selector de Carpeta de Destino (`FolderPickerDialog`):** Diálogo interactivo para navegar por el almacenamiento y confirmar la ruta exacta al mover o copiar lotes de archivos.
- **Soporte y Detección de Archivos Comprimidos Cifrados (Protegidos con Contraseña):**
  - Detección automática de archivos ZIP/7z cifrados con insignia visual de candado.
  - Solicitud de contraseña mediante diálogo dedicado (`ArchivePasswordDialog`) para explorar o descomprimir archivos protegidos.
- **Creación Personalizada de Nuevos Archivos de Texto (.txt, .json, .md, .xml, .kt...):**
  - Menú desplegable desde el botón FAB (+) con opciones para crear tanto "Nueva Carpeta" como "Nuevo Archivo" con extensión y contenido inicial personalizado.
- **Navegación y Exploración Interna 100% REAL de Archivos ZIP / 7Z / TAR / APK:**
  - Inspección directa de la estructura jerárquica interna de contenedores comprimidos sin necesidad de extraer previamente en disco.
  - **Extracción Directa de Archivo Individual:** Permite extraer o copiar un archivo específico desde el interior de contenedores `.zip`, `.7z`, `.tar.gz`, `.apk` o `.xapk` hacia el directorio actual con un solo toque, sin necesidad de descomprimir el contenedor completo.
  - Navegación multinivel por carpetas y subdirectorios internos dentro del archivo comprimido con barra de migas de pan (Breadcrumbs) interactiva.
  - Previsualización en tiempo real de archivos de texto (`.txt`, `.json`, `.kt`, `.java`, `.md`, `.xml`, `.gradle`, `.sql`) leídos directamente desde la memoria RAM del archivo ZIP.
  - Búsqueda interna filtrada de elementos dentro del archivo comprimido.
  - Compatible con formatos `.zip`, `.7z`, `.tar.gz`, `.apk`, `.jar`, `.xapk` y archivos cifrados con contraseña AES-256.
- **Editor y Visor en Pantalla Completa ArchivoX Text v1.2 (.txt / .md / .json):**
  - Editor y visor independiente en pantalla completa (independiente de diálogos flotantes) para lectura y edición en tiempo real de archivos `.txt`, `.md` y `.json`.
  - Soporte exclusivo para archivos `.json` con renderizado resaltado sintáctico (claves en dorado, cadenas en verde, números y booleanos en violeta), validación asíncrona mediante hilo secundario / motor NDK (`✓ JSON Válido`) y botón de formateo automático (Indent 2 espacios) ejecutado en segundo plano.
  - Renderizado mediante listas perezosas (`LazyColumn`) para desplazamiento suave e instantáneo incluso en archivos con miles de líneas.
  - Modo Edición con guardado directo en almacenamiento local, indicador visual de cambios no guardados y prevención de pérdidas.
  - Alternancia fluida entre Modo Lectura (con renderizado de Markdown enriquecido, JSON estructurado o texto plano con numeración de líneas) y Modo Editor con interfaz adaptada de botones segmentados M3 (`SegmentedButton`).
  - Barra de estado inferior con contadores en tiempo real de líneas, palabras, caracteres y tipo de codificación (UTF-8).
  - Herramientas de ajuste tipográfico (tamaño de fuente en sp, fuente monoespaciada/sans), búsqueda con resaltado de coincidencias y copiado al portapapeles.
- **Compresión con Partición en Partes (Split Volumes) y Opciones Avanzadas:**
  - Menú de opciones avanzadas mediante clic mantenido (Long Press) en archivos `.zip` y demás formatos.
  - Opción de dividir compresión en múltiples volumenes (10MB, 50MB, 100MB, 700MB) almacenando las partes (`.zip.part001`, `.part002`) en una carpeta especial creada automáticamente (`_partes`).
- **Íconos Vectoriales Exclusivos:**
  - Íconos y badges de color personalizados y exclusivos para archivos de datos `.json`.
- **Diseño de Interfaz Minimalista y Ultraligero:**
  - Rediseño refinado de la lista de archivos (`FileItemRow`) con menor densidad visual, filas compactas e íconos estilizados de 38dp.
  - Tarjeta de almacenamiento (`StorageHeader`) y barra de migas de pan (`BreadcrumbBar`) integradas con acabado limpio y plano.
  - Botón de Acción Flotante (FAB) estilizado en formato circular sutil.
- **Cifrado Militar AES-256 en Archivos ZIP:**
  - Compresión de archivos ZIP 100% funcional con protección opcional mediante clave segura y cifrado de 256 bits AES.
  - Extracción transparente de archivos comprimidos cifrados con verificación de contraseña.
- **Flujo de Navegación y Diálogos Fluidos:**
  - Solucionado el flujo de acciones de archivo para que los diálogos de compresión y extracción se abran directamente sin requerir clics adicionales.
- **Delegación a Núcleos Secundarios de CPU:**
  - Las tareas de compresión y descompresión pesadas no congelan la interfaz de usuario, ejecutándose en un pool de hilos asignados a núcleos secundarios de la CPU.
- **Motores Nativos Multi-Lenguaje (C++ / Rust / Zip4j):**
  - **Zip4j AES Engine:** Algoritmo Deflate puro con cifrado de alto nivel AES-256 para `.zip`.
  - **Integración con LibArchive Nativa (C++ NDK):** Manejo directo de contenedores multinivel y compresión/descompresión optimizada en C++ con hilos Pthreads para formatos `.rar`, `.tar.gz`, `.tar.bz2`, `.iso`, `.7z`.
  - **Cálculo de Hashes Acelerado por Hardware (ARM NEON / SIMD):** Rutinas nativas en C++ y Rust utilizando instrucciones de hardware vectoriales ARMv8 NEON (`__builtin_arm_crc32`) y SIMD para verificación ultrarrápida de integridad (CRC32, SHA-256) sin sobrecargar la CPU.
  - **Parser Ultra-Rápido de JSON / Markdown (`serde_json` Rust Engine):** Integración nativa de la librería `serde_json` en Rust y parsing nativo en C++ para validación, formateo y renderizado de sintaxis JSON y Markdown a velocidad nativa sin sobrecarga en la JVM.
  - **Rust (LZMA2 / Rayon Threadpool):** Algoritmos de alta densidad para formato `.7z` con hilos paralelos Rayon.
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

