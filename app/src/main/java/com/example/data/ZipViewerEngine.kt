package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipFile as JavaZipFile
import net.lingala.zip4j.ZipFile as Zip4jFile
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

data class ZipEntryItem(
    val name: String,
    val fullPath: String,
    val isDirectory: Boolean,
    val uncompressedSize: Long = 0L,
    val compressedSize: Long = 0L,
    val crc: Long = 0L,
    val lastModified: Long = 0L,
    val extension: String = "",
    val fileType: FileType = FileType.UNKNOWN
) {
    val formattedSize: String
        get() {
            if (isDirectory) return "Carpeta"
            if (uncompressedSize <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            var digitGroups = (Math.log10(uncompressedSize.toDouble()) / Math.log10(1024.0)).toInt()
            if (digitGroups >= units.size) digitGroups = units.size - 1
            val value = uncompressedSize / Math.pow(1024.0, digitGroups.toDouble())
            return String.format("%.1f %s", value, units[digitGroups])
        }

    val compressionRatio: String
        get() {
            if (isDirectory || uncompressedSize <= 0) return "--"
            if (compressedSize <= 0) return "0%"
            val ratio = (1.0 - (compressedSize.toDouble() / uncompressedSize.toDouble())) * 100.0
            return String.format("%.0f%%", ratio.coerceIn(0.0, 99.0))
        }

    val formattedDate: String
        get() {
            if (lastModified <= 0L) return "--/--/----"
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(lastModified))
        }
}

data class ZipPathSegment(
    val name: String,
    val innerPath: String
)

data class ZipBrowseResult(
    val zipFilePath: String,
    val zipFileName: String,
    val currentInnerPath: String,
    val breadcrumbs: List<ZipPathSegment>,
    val items: List<ZipEntryItem>,
    val totalEntriesCount: Int,
    val totalUncompressedBytes: Long,
    val isEncrypted: Boolean = false,
    val errorMessage: String? = null
)

class ZipViewerEngine {

    suspend fun browseZip(
        zipFilePath: String,
        targetInnerPath: String = "",
        password: String? = null,
        searchQuery: String = ""
    ): ZipBrowseResult = withContext(Dispatchers.IO) {
        val archiveFile = File(zipFilePath)
        if (!archiveFile.exists() || !archiveFile.canRead()) {
            return@withContext ZipBrowseResult(
                zipFilePath = zipFilePath,
                zipFileName = archiveFile.name,
                currentInnerPath = "",
                breadcrumbs = listOf(ZipPathSegment("📦 Raíz", "")),
                items = emptyList(),
                totalEntriesCount = 0,
                totalUncompressedBytes = 0L,
                errorMessage = "El archivo '${archiveFile.name}' no existe o no se puede leer."
            )
        }

        val normalizedInnerPath = targetInnerPath.trim('/').let { if (it.isEmpty()) "" else "$it/" }
        val rawEntries = mutableListOf<ZipEntryItem>()
        var totalCount = 0
        var totalBytes = 0L
        var isEncrypted = false

        try {
            val lowerName = archiveFile.name.lowercase()

            if (lowerName.endsWith(".7z")) {
                SevenZFile(archiveFile).use { sevenZ ->
                    var entry: SevenZArchiveEntry? = sevenZ.nextEntry
                    while (entry != null) {
                        val path = entry.name.replace('\\', '/').trim('/')
                        val isDir = entry.isDirectory
                        val normPath = if (isDir) "$path/" else path
                        val size = entry.size
                        totalCount++
                        totalBytes += size

                        val ext = if (!isDir && path.contains(".")) path.substringAfterLast(".").lowercase() else ""
                        rawEntries.add(
                            ZipEntryItem(
                                name = path.substringAfterLast('/'),
                                fullPath = normPath,
                                isDirectory = isDir,
                                uncompressedSize = size,
                                compressedSize = size, // 7z entries aggregate stream
                                crc = try { entry.crcValue } catch (t: Throwable) { 0L },
                                lastModified = entry.lastModifiedDate?.time ?: 0L,
                                extension = ext,
                                fileType = if (isDir) FileType.FOLDER else determineFileType(ext)
                            )
                        )
                        entry = sevenZ.nextEntry
                    }
                }
            } else if (lowerName.endsWith(".tar.gz") || lowerName.endsWith(".tgz")) {
                FileInputStream(archiveFile).use { fis ->
                    GzipCompressorInputStream(BufferedInputStream(fis)).use { gzis ->
                        TarArchiveInputStream(gzis).use { tais ->
                            var entry: TarArchiveEntry? = tais.nextTarEntry
                            while (entry != null) {
                                val path = entry.name.replace('\\', '/').trim('/')
                                val isDir = entry.isDirectory
                                val normPath = if (isDir) "$path/" else path
                                val size = entry.size
                                totalCount++
                                totalBytes += size

                                val ext = if (!isDir && path.contains(".")) path.substringAfterLast(".").lowercase() else ""
                                rawEntries.add(
                                    ZipEntryItem(
                                        name = path.substringAfterLast('/'),
                                        fullPath = normPath,
                                        isDirectory = isDir,
                                        uncompressedSize = size,
                                        compressedSize = size,
                                        crc = 0L,
                                        lastModified = entry.modTime.time,
                                        extension = ext,
                                        fileType = if (isDir) FileType.FOLDER else determineFileType(ext)
                                    )
                                )
                                entry = tais.nextTarEntry
                            }
                        }
                    }
                }
            } else {
                // Standard ZIP / Zip4j / APK / JAR / XAPK
                val zip4j = if (!password.isNullOrBlank()) {
                    Zip4jFile(archiveFile, password.toCharArray())
                } else {
                    Zip4jFile(archiveFile)
                }

                if (zip4j.isValidZipFile) {
                    isEncrypted = zip4j.isEncrypted
                    val headers = zip4j.fileHeaders
                    totalCount = headers.size

                    for (header in headers) {
                        val path = header.fileName.replace('\\', '/').trim('/')
                        val isDir = header.isDirectory
                        val normPath = if (isDir) "$path/" else path
                        val size = header.uncompressedSize
                        totalBytes += size

                        val ext = if (!isDir && path.contains(".")) path.substringAfterLast(".").lowercase() else ""
                        rawEntries.add(
                            ZipEntryItem(
                                name = path.substringAfterLast('/'),
                                fullPath = normPath,
                                isDirectory = isDir,
                                uncompressedSize = size,
                                compressedSize = header.compressedSize,
                                crc = header.crc,
                                lastModified = header.lastModifiedTime,
                                extension = ext,
                                fileType = if (isDir) FileType.FOLDER else determineFileType(ext)
                            )
                        )
                    }
                } else {
                    // Fallback to java.util.zip.ZipFile
                    JavaZipFile(archiveFile).use { zipFile ->
                        val entries = zipFile.entries()
                        while (entries.hasMoreElements()) {
                            val entry = entries.nextElement()
                            val path = entry.name.replace('\\', '/').trim('/')
                            val isDir = entry.isDirectory
                            val normPath = if (isDir) "$path/" else path
                            val size = entry.size.coerceAtLeast(0L)
                            totalCount++
                            totalBytes += size

                            val ext = if (!isDir && path.contains(".")) path.substringAfterLast(".").lowercase() else ""
                            rawEntries.add(
                                ZipEntryItem(
                                    name = path.substringAfterLast('/'),
                                    fullPath = normPath,
                                    isDirectory = isDir,
                                    uncompressedSize = size,
                                    compressedSize = entry.compressedSize.coerceAtLeast(0L),
                                    crc = entry.crc,
                                    lastModified = entry.time,
                                    extension = ext,
                                    fileType = if (isDir) FileType.FOLDER else determineFileType(ext)
                                )
                            )
                        }
                    }
                }
            }

            // Filter items for current inner folder or search query
            val itemsInCurrentPath = if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                rawEntries.filter { it.name.lowercase().contains(q) || it.fullPath.lowercase().contains(q) }
            } else {
                extractDirectChildren(rawEntries, normalizedInnerPath)
            }

            // Build breadcrumbs
            val breadcrumbs = mutableListOf(ZipPathSegment("📦 ${archiveFile.name}", ""))
            if (normalizedInnerPath.isNotEmpty()) {
                val parts = normalizedInnerPath.trim('/').split('/')
                var accumulated = ""
                for (part in parts) {
                    if (part.isNotEmpty()) {
                        accumulated = "$accumulated$part/"
                        breadcrumbs.add(ZipPathSegment(part, accumulated))
                    }
                }
            }

            ZipBrowseResult(
                zipFilePath = zipFilePath,
                zipFileName = archiveFile.name,
                currentInnerPath = normalizedInnerPath,
                breadcrumbs = breadcrumbs,
                items = itemsInCurrentPath,
                totalEntriesCount = totalCount,
                totalUncompressedBytes = totalBytes,
                isEncrypted = isEncrypted
            )
        } catch (e: Exception) {
            ZipBrowseResult(
                zipFilePath = zipFilePath,
                zipFileName = archiveFile.name,
                currentInnerPath = normalizedInnerPath,
                breadcrumbs = listOf(ZipPathSegment("📦 ${archiveFile.name}", "")),
                items = emptyList(),
                totalEntriesCount = 0,
                totalUncompressedBytes = 0L,
                isEncrypted = isEncrypted,
                errorMessage = e.localizedMessage ?: "Error al abrir o leer el archivo ZIP"
            )
        }
    }

    private fun extractDirectChildren(
        allEntries: List<ZipEntryItem>,
        currentPath: String
    ): List<ZipEntryItem> {
        val resultList = mutableListOf<ZipEntryItem>()
        val folderNamesAdded = mutableSetOf<String>()

        for (entry in allEntries) {
            val entryPath = entry.fullPath
            if (entryPath == currentPath) continue

            if (entryPath.startsWith(currentPath)) {
                val rel = entryPath.substring(currentPath.length)
                if (rel.isEmpty()) continue

                val slashIndex = rel.indexOf('/')
                if (slashIndex != -1 && slashIndex < rel.length - 1) {
                    // It's inside a subfolder
                    val subFolderName = rel.substring(0, slashIndex)
                    val subFolderPath = "$currentPath$subFolderName/"

                    if (!folderNamesAdded.contains(subFolderName)) {
                        folderNamesAdded.add(subFolderName)
                        resultList.add(
                            ZipEntryItem(
                                name = subFolderName,
                                fullPath = subFolderPath,
                                isDirectory = true,
                                fileType = FileType.FOLDER
                            )
                        )
                    }
                } else {
                    // Direct child file or direct folder
                    val itemName = if (entry.isDirectory) rel.trim('/') else rel
                    if (itemName.isNotEmpty()) {
                        resultList.add(entry.copy(name = itemName))
                    }
                }
            }
        }

        // Sort folders first, then files
        val folders = resultList.filter { it.isDirectory }.distinctBy { it.fullPath }.sortedBy { it.name.lowercase() }
        val files = resultList.filter { !it.isDirectory }.distinctBy { it.fullPath }.sortedBy { it.name.lowercase() }

        return folders + files
    }

    suspend fun readZipEntryText(
        zipFilePath: String,
        entryFullPath: String,
        password: String? = null
    ): String = withContext(Dispatchers.IO) {
        val archiveFile = File(zipFilePath)
        try {
            val zip4j = if (!password.isNullOrBlank()) {
                Zip4jFile(archiveFile, password.toCharArray())
            } else {
                Zip4jFile(archiveFile)
            }

            if (zip4j.isValidZipFile) {
                val header = zip4j.fileHeaders.firstOrNull {
                    it.fileName.replace('\\', '/').trim('/') == entryFullPath.trim('/')
                }
                if (header != null) {
                    zip4j.getInputStream(header).use { inputStream ->
                        return@withContext inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    }
                }
            }

            // Fallback java.util.zip.ZipFile
            JavaZipFile(archiveFile).use { zipFile ->
                val entry = zipFile.getEntry(entryFullPath) ?: zipFile.getEntry(entryFullPath.trim('/'))
                if (entry != null) {
                    zipFile.getInputStream(entry).use { inputStream ->
                        return@withContext inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    }
                }
            }

            "No se pudo encontrar o leer la entrada '$entryFullPath' dentro del archivo."
        } catch (e: Exception) {
            "Error al leer el archivo desde el ZIP: ${e.localizedMessage}"
        }
    }

    suspend fun extractZipEntry(
        zipFilePath: String,
        entryFullPath: String,
        destDirectoryPath: String,
        password: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val archiveFile = File(zipFilePath)
        val destDir = File(destDirectoryPath)
        if (!destDir.exists()) destDir.mkdirs()

        try {
            val zip4j = if (!password.isNullOrBlank()) {
                Zip4jFile(archiveFile, password.toCharArray())
            } else {
                Zip4jFile(archiveFile)
            }

            if (zip4j.isValidZipFile) {
                val header = zip4j.fileHeaders.firstOrNull {
                    it.fileName.replace('\\', '/').trim('/') == entryFullPath.trim('/')
                }
                if (header != null) {
                    if (header.isDirectory) {
                        zip4j.extractFile(header, destDirectoryPath)
                    } else {
                        val outFile = File(destDir, header.fileName.substringAfterLast('/'))
                        zip4j.getInputStream(header).use { input ->
                            FileOutputStream(outFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    return@withContext true
                }
            }

            // Fallback Java ZipFile
            JavaZipFile(archiveFile).use { zipFile ->
                val entry = zipFile.getEntry(entryFullPath) ?: zipFile.getEntry(entryFullPath.trim('/'))
                if (entry != null) {
                    val outFile = File(destDir, entry.name.substringAfterLast('/'))
                    zipFile.getInputStream(entry).use { input ->
                        FileOutputStream(outFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    return@withContext true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun determineFileType(ext: String): FileType {
        return when (ext) {
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso", "tgz", "z", "apk", "jar" -> FileType.ARCHIVE
            "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp", "heic" -> FileType.IMAGE
            "mp4", "mkv", "avi", "mov", "webm", "3gp", "flv" -> FileType.VIDEO
            "mp3", "flac", "wav", "aac", "m4a", "ogg", "opus" -> FileType.AUDIO
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "epub", "csv" -> FileType.DOCUMENT
            "kt", "java", "py", "js", "ts", "html", "css", "json", "xml", "sh", "c", "cpp", "gradle", "sql" -> FileType.CODE
            "apk", "xapk", "apks" -> FileType.APK
            else -> FileType.UNKNOWN
        }
    }
}
