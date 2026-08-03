# AGENTS.md — Reglas y Directrices para Agentes de Código AI

Este documento contiene las normas operativas indispensables que cualquier agente de IA o desarrollador debe respetar al realizar modificaciones en este código.

---

## 🤖 Reglas Generales de Comportamiento

1. **Respeto Estricto de la Intención del Usuario:**
   - Construir únicamente lo que se solicita. Mantener el enfoque en la calidad visual, rendimiento y estabilidad sin añadir funciones innecesarias o sobreingeniería no requerida.

2. **Verificación de Compilación Obligatoria:**
   - Antes de dar por concluida cualquier modificación de código, ejecutar la herramienta `compile_applet` para certificar que el proyecto construye correctamente y no introduce regresiones de sintaxis ni de dependencias.

3. **Arquitectura y Limpieza de Código:**
   - Mantener los archivos Kotlin modularizados (máximo 400-500 líneas por archivo).
   - Utilizar `StateFlow` e inmutabilidad en el `UiState`.
   - Garantizar descripciones de contenido (`contentDescription`) en todos los íconos de Compose.
   - Preservar las firmas JNI en `NativeArchiveEngine.kt` para la interoperabilidad con los módulos C++ y Rust.

4. **Nombres de Recursos y Metadatos:**
   - Mantener sincronizado el nombre de la app en `res/values/strings.xml` (`app_name`) con el campo `name` en `metadata.json`.
   - No modificar nunca las claves ni credenciales del sistema a través de `local.properties`. Utilizar siempre la configuración de `BuildConfig` o variables de entorno.
