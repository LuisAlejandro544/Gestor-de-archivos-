# Roadmap de Desarrollo — ZArchiver Native

Este archivo documenta los hitos completados y la hoja de ruta para futuras versiones del gestor de archivos ZArchiver Native.

---

## 🟢 Fase 1: Fundamentos y UI Core (COMPLETADO)
- [x] Estructura MVVM con Jetpack Compose y Material 3.
- [x] Explorador de archivos local con soporte para navegación profunda y migas de pan (Breadcrumbs).
- [x] Modos de visualización en Lista y Cuadrícula Adaptativa.
- [x] Creación de carpetas, renombrado, eliminación y copia de rutas al portapapeles.
- [x] Filtros por categoría de almacenamiento (Imágenes, Videos, Música, Documentos, Archivos, Descargas).

## 🟢 Fase 2: Rendimiento Multi-Core C++/Rust y Apariencia (COMPLETADO)
- [x] Integración de módulos nativos C++ (`native-archive.cpp`) y Rust (`rust-native`).
- [x] Delegación de tareas de compresión/descompresión a núcleos secundarios de la CPU mediante worker pools dedicados.
- [x] Formatos iniciales de compresión: `.zip`, `.rar`, `.7z`, `.tar.gz`.
- [x] Consola de log en tiempo real y velocímetro (MB/s) durante procesos de compresión.
- [x] Extracción en tiempo real del ícono original de aplicaciones y juegos desde archivos `.apk`.
- [x] Sistema de temas y paletas de color (Verde Esmeralda ZArchiver, AMOLED Negro Puro, Material You Dinámico).
- [x] Automatización CI/CD con GitHub Actions (firma en caliente de APK Debug y caché).

## 🟡 Fase 3: Características Avanzadas de Archivo (PRÓXIMAMENTE)
- [ ] Compresión protegida con contraseña mediante cifrado AES-256 en motor Rust.
- [ ] División de archivos comprimidos en múltiples volúmenes (ej. `archive.7z.001`, `archive.7z.002`).
- [ ] Visor nativo interno para imágenes, código fuente y reproductor de audio ligero.
- [ ] Herramienta de benchmark de CPU integrada para medir la velocidad de compresión de los núcleos del dispositivo.
- [ ] Integración con proveedores de almacenamiento en la nube (Google Drive, SMB/FTP local).
