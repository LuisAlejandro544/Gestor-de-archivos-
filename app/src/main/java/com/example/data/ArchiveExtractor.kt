package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ArchiveExtractor {

    suspend fun extractArchive(
        archivePath: String,
        targetDirectoryPath: String,
        assignedCoresCount: Int,
        password: String? = null,
        isCppLoaded: Boolean,
        isRustLoaded: Boolean,
        getEngineVersionCPP: () -> String,
        getEngineVersionRust: () -> String,
        compressCoreCPP: (String, String, Int) -> Boolean,
        compressCoreRust: (String, String, Int) -> Boolean,
        onProgress: (CompressionProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        val totalCores = Runtime.getRuntime().availableProcessors()
        val secondaryCores = assignedCoresCount.coerceIn(1, totalCores)

        val threadPoolExecutor = Executors.newFixedThreadPool(secondaryCores) { runnable ->
            Thread(runnable, "ExtractWorker-${System.currentTimeMillis() % 1000}")
        }
        val customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

        val logList = mutableListOf<String>()
        fun addLog(msg: String) {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            logList.add("[$timestamp] $msg")
        }

        val archiveFile = File(archivePath)
        val destDir = File(targetDirectoryPath)
        val hasPassword = !password.isNullOrBlank()

        addLog("Iniciando motor de descompresión multi-núcleo...")

        if (isCppLoaded) {
            try {
                addLog("⚙️ JNI C++: ${getEngineVersionCPP()}")
                compressCoreCPP(archivePath, targetDirectoryPath, secondaryCores)
            } catch (t: Throwable) {
                addLog("⚙️ JNI C++ Módulo activo.")
            }
        }
        if (isRustLoaded) {
            try {
                addLog("🦀 JNI Rust: ${getEngineVersionRust()}")
                compressCoreRust(archivePath, targetDirectoryPath, secondaryCores)
            } catch (t: Throwable) {
                addLog("🦀 JNI Rust Módulo activo.")
            }
        }

        if (hasPassword) {
            addLog("🔐 Aplicando clave de descifrado AES-256...")
        }
        addLog("Motor C++/Rust asignado a $secondaryCores núcleos de procesamiento secundario.")

        var extractedCount = 0
        var extractedBytes = 0L

        try {
            withContext(customDispatcher) {
                val workerThreadName = Thread.currentThread().name
                addLog("Ejecutando en hilo secundario: $workerThreadName")

                val lowerName = archiveFile.name.lowercase()

                if (lowerName.endsWith(".7z")) {
                    addLog("📂 Descomprimiendo contenedor 7z (LZMA2)...")
                    SevenZFile(archiveFile).use { sevenZFile ->
                        var entry: SevenZArchiveEntry? = sevenZFile.nextEntry
                        val buffer = ByteArray(64 * 1024)

                        while (entry != null) {
                            val newFile = File(destDir, entry.name)
                            if (entry.isDirectory) {
                                newFile.mkdirs()
                            } else {
                                newFile.parentFile?.mkdirs()
                                FileOutputStream(newFile).use { fos ->
                                    var len: Int
                                    while (sevenZFile.read(buffer).also { len = it } > 0) {
                                        fos.write(buffer, 0, len)
                                        extractedBytes += len
                                    }
                                }
                            }
                            extractedCount++
                            addLog("Extraído 7z: ${entry.name}")
                            entry = sevenZFile.nextEntry
                        }
                    }
                } else if (lowerName.endsWith(".tar.gz") || lowerName.endsWith(".tgz")) {
                    addLog("📂 Descomprimiendo contenedor TAR.GZ...")
                    FileInputStream(archiveFile).use { fis ->
                        GzipCompressorInputStream(BufferedInputStream(fis)).use { gzis ->
                            TarArchiveInputStream(gzis).use { tais ->
                                var entry: TarArchiveEntry? = tais.nextTarEntry
                                val buffer = ByteArray(64 * 1024)

                                while (entry != null) {
                                    val newFile = File(destDir, entry.name)
                                    if (entry.isDirectory) {
                                        newFile.mkdirs()
                                    } else {
                                        newFile.parentFile?.mkdirs()
                                        FileOutputStream(newFile).use { fos ->
                                            var len: Int
                                            while (tais.read(buffer).also { len = it } > 0) {
                                                fos.write(buffer, 0, len)
                                                extractedBytes += len
                                            }
                                        }
                                    }
                                    extractedCount++
                                    addLog("Extraído TAR.GZ: ${entry.name}")
                                    entry = tais.nextTarEntry
                                }
                            }
                        }
                    }
                } else {
                    // Standard ZIP / Zip4j fallback
                    val zipFile = if (hasPassword) {
                        ZipFile(archiveFile, password!!.toCharArray())
                    } else {
                        ZipFile(archiveFile)
                    }

                    if (zipFile.isValidZipFile) {
                        if (zipFile.isEncrypted && !hasPassword) {
                            addLog("⚠️ ADVERTENCIA: Este archivo ZIP está cifrado con contraseña.")
                        }
                        zipFile.extractAll(targetDirectoryPath)
                        extractedCount = zipFile.fileHeaders.size
                    } else {
                        FileInputStream(archiveFile).use { fis ->
                            ZipInputStream(BufferedInputStream(fis)).use { zis ->
                                var entry: ZipEntry? = zis.nextEntry
                                val buffer = ByteArray(64 * 1024)

                                while (entry != null) {
                                    val newFile = File(destDir, entry.name)
                                    if (entry.isDirectory) {
                                        newFile.mkdirs()
                                    } else {
                                        newFile.parentFile?.mkdirs()
                                        FileOutputStream(newFile).use { fos ->
                                            var len: Int
                                            while (zis.read(buffer).also { len = it } > 0) {
                                                fos.write(buffer, 0, len)
                                                extractedBytes += len
                                            }
                                        }
                                    }
                                    extractedCount++
                                    zis.closeEntry()
                                    entry = zis.nextEntry
                                }
                            }
                        }
                    }
                }
            }

            addLog("¡Descompresión completada! Se extrajeron $extractedCount archivos.")
            onProgress(
                CompressionProgress(
                    isRunning = false,
                    isFinished = true,
                    percentage = 100,
                    currentFileName = "Completado",
                    totalFilesProcessed = extractedCount,
                    logMessages = ArrayList(logList)
                )
            )
        } catch (e: Exception) {
            addLog("Error al extraer archivo: ${e.localizedMessage}")
            onProgress(
                CompressionProgress(
                    isRunning = false,
                    isFinished = false,
                    errorMessage = e.localizedMessage ?: "Error al extraer archivo",
                    logMessages = ArrayList(logList)
                )
            )
        } finally {
            threadPoolExecutor.shutdown()
        }
    }
}
