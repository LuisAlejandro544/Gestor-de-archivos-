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
- [x] Extensión nativa **ArchivoX Text** para lectura de archivos `.txt` y `.md` con descompresión e instalación de recursos dinámicos.
- [x] Visor nativo de claves privadas y certificados PEM (`.pem`, `.key`, `.crt`, `.pub`, `.p8`).
- [x] Automatización CI/CD con GitHub Actions (firma en caliente de APK Debug, caché de Gradle/Cargo y flujo de auto-descompresión ZIP con GitHub App Bot).

## 🟡 Fase 3: Características Avanzadas (PRÓXIMAMENTE)
- [ ] División de archivos comprimidos en múltiples volúmenes (ej. `archive.7z.001`, `archive.7z.002`).
- [ ] Visor nativo interno para imágenes, código fuente y reproductor de audio ligero.
- [ ] Herramienta de benchmark de CPU integrada para medir la velocidad de compresión de los núcleos del dispositivo.
- [ ] Integración con proveedores de almacenamiento en la nube (Google Drive, SMB/FTP local).

