package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class FileType {
    FOLDER,
    ARCHIVE,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    CODE,
    APK,
    JSON,
    UNKNOWN;

    val color: Color
        get() = when (this) {
            FOLDER -> FolderAmber
            ARCHIVE -> FileColorArchive
            IMAGE -> FileColorImage
            VIDEO -> FileColorVideo
            AUDIO -> FileColorAudio
            DOCUMENT -> FileColorDocument
            CODE -> FileColorCode
            APK -> FileColorApk
            JSON -> FileColorJson
            UNKNOWN -> FileColorUnknown
        }
}

enum class FileSortType(val labelEs: String) {
    NAME_ASC("Nombre (A-Z)"),
    NAME_DESC("Nombre (Z-A)"),
    SIZE_ASC("Tamaño (Menor)"),
    SIZE_DESC("Tamaño (Mayor)"),
    DATE_ASC("Fecha (Antiguos)"),
    DATE_DESC("Fecha (Recientes)"),
    TYPE_ASC("Tipo (Categoría)")
}

enum class ViewMode {
    LIST,
    GRID
}

enum class StorageCategory(val labelEs: String) {
    ALL("Todos"),
    FOLDERS("Carpetas"),
    DOWNLOADS("Descargas"),
    DOCUMENTS("Documentos"),
    IMAGES("Imágenes"),
    AUDIO("Audio"),
    VIDEOS("Videos"),
    ARCHIVES("Archivos Compresos")
}

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0L,
    val lastModified: Long = 0L,
    val itemCount: Int? = null,
    val extension: String = "",
    val mimeType: String = "",
    val fileType: FileType = FileType.UNKNOWN,
    val isHidden: Boolean = false,
    val canRead: Boolean = true,
    val canWrite: Boolean = true,
    val isEncrypted: Boolean = false
) {
    val formattedSize: String
        get() {
            if (isDirectory) return itemCount?.let { "$it elementos" } ?: "Carpeta"
            if (sizeBytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var digitGroups = (Math.log10(sizeBytes.toDouble()) / Math.log10(1024.0)).toInt()
            if (digitGroups >= units.size) digitGroups = units.size - 1
            val value = sizeBytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format("%.1f %s", value, units[digitGroups])
        }

    val formattedDate: String
        get() {
            if (lastModified == 0L) return "--/--/----"
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(lastModified))
        }
}

data class PathSegment(
    val name: String,
    val path: String
)

data class StorageInfo(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val storagePath: String = ""
) {
    val formattedUsed: String
        get() = formatBytes(usedBytes)

    val formattedTotal: String
        get() = formatBytes(totalBytes)

    val usagePercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) else 0f

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 GB"
        val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
        return String.format("%.1f GB", gb)
    }
}
