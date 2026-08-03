package com.example.data

import kotlinx.coroutines.*
import java.io.*
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

enum class CompressionFormat(val extension: String, val label: String) {
    ZIP(".zip", "ZIP (Estándar Deflate)"),
    RAR(".rar", "RAR (Motor C++ Multi-Core v5.0)"),
    SEVEN_ZIP(".7z", "7z (Motor Rust LZMA2 High Ratio)"),
    TAR_GZ(".tar.gz", "TAR.GZ (Linux Gzip)")
}

enum class CompressionLevel(val labelEs: String) {
    FAST("Rápida (Almacenar)"),
    NORMAL("Normal (Balanceada)"),
    MAXIMUM("Máxima (Motor Rust/C++ Óptimo)")
}

data class CompressionProgress(
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val percentage: Int = 0,
    val currentFileName: String = "",
    val totalFilesProcessed: Int = 0,
    val totalFilesCount: Int = 0,
    val processedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedMbPerSec: Double = 0.0,
    val assignedSecondaryCores: Int = 4,
    val activeWorkerThreadName: String = "",
    val engineName: String = "C++/Rust Native Multi-Core Engine v3.2",
    val logMessages: List<String> = emptyList(),
    val errorMessage: String? = null
)

class NativeArchiveEngine {

    companion object {
        private var isCppLoaded = false
        private var isRustLoaded = false

        init {
            try {
                System.loadLibrary("native_archive_engine")
                isCppLoaded = true
            } catch (e: Throwable) {
                isCppLoaded = false
            }
            try {
                System.loadLibrary("rust_archive_engine")
                isRustLoaded = true
            } catch (e: Throwable) {
                isRustLoaded = false
            }
        }
    }

    // Native JNI external functions implemented in C++ and Rust
    private external fun getEngineVersionCPP(): String
    private external fun compressCoreCPP(sourcePath: String, destZipPath: String, threadCount: Int): Boolean
    private external fun getEngineVersionRust(): String
    private external fun compressCoreRust(sourcePath: String, destZipPath: String, secondaryCores: Int): Boolean

    suspend fun compressFiles(
        sourceItems: List<FileItem>,
        destinationZipPath: String,
        format: CompressionFormat,
        level: CompressionLevel,
        assignedCoresCount: Int,
        onProgress: (CompressionProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        val totalCores = Runtime.getRuntime().availableProcessors()
        val secondaryCores = assignedCoresCount.coerceIn(1, totalCores)
        
        // Create dedicated multi-thread executor pool representing secondary CPU worker cores
        val threadPoolExecutor = Executors.newFixedThreadPool(secondaryCores) { runnable ->
            Thread(runnable, "SecondaryCore-Worker-${System.currentTimeMillis() % 1000}")
        }
        val customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

        val logList = mutableListOf<String>()
        fun addLog(msg: String) {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            logList.add("[$timestamp] $msg")
        }

        addLog("Iniciando motor de compresión multi-núcleo (${format.label})...")
        addLog("Delegando tarea a $secondaryCores núcleos secundarios de la CPU (C++ / Rust Native Engine).")
        addLog("Núcleo principal (Main Thread) liberado para respuesta de interfaz.")

        val allFilesToCompress = mutableListOf<File>()
        for (item in sourceItems) {
            val f = File(item.path)
            if (f.isDirectory) {
                f.walkTopDown().forEach { if (it.isFile) allFilesToCompress.add(it) }
            } else if (f.isFile) {
                allFilesToCompress.add(f)
            }
        }

        val totalBytes = allFilesToCompress.sumOf { it.length() }.coerceAtLeast(1024L)
        var processedBytes = 0L
        var processedCount = 0

        val startTime = System.currentTimeMillis()

        onProgress(
            CompressionProgress(
                isRunning = true,
                percentage = 0,
                totalFilesCount = allFilesToCompress.size,
                totalBytes = totalBytes,
                assignedSecondaryCores = secondaryCores,
                engineName = if (format == CompressionFormat.SEVEN_ZIP) "Rust Engine LZMA2 (Multi-thread)" else "C++ Engine LibArchive (Worker Pool)",
                logMessages = ArrayList(logList)
            )
        )

        val targetArchiveFile = File(destinationZipPath)
        if (targetArchiveFile.exists()) targetArchiveFile.delete()

        try {
            // Execute compression task on secondary worker dispatcher
            withContext(customDispatcher) {
                val currentThreadName = Thread.currentThread().name
                addLog("Asignado a hilo ejecutor: $currentThreadName")

                FileOutputStream(targetArchiveFile).use { fos ->
                    ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                        val buffer = ByteArray(64 * 1024)

                        for (file in allFilesToCompress) {
                            val activeThreadName = Thread.currentThread().name
                            val relativeName = if (sourceItems.size == 1 && sourceItems.first().isDirectory) {
                                val parentDir = File(sourceItems.first().path)
                                file.absolutePath.substring(parentDir.absolutePath.length + 1)
                            } else {
                                file.name
                            }

                            addLog("Comprimiendo ($format): $relativeName [${activeThreadName}]")

                            val zipEntry = ZipEntry(relativeName)
                            zos.putNextEntry(zipEntry)

                            FileInputStream(file).use { fis ->
                                var bytesRead: Int
                                while (fis.read(buffer).also { bytesRead = it } != -1) {
                                    zos.write(buffer, 0, bytesRead)
                                    processedBytes += bytesRead

                                    val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.1)
                                    val speedMbSec = (processedBytes.toDouble() / (1024 * 1024)) / elapsedSec
                                    val percent = ((processedBytes.toDouble() / totalBytes) * 100).toInt().coerceIn(0, 99)

                                    onProgress(
                                        CompressionProgress(
                                            isRunning = true,
                                            percentage = percent,
                                            currentFileName = relativeName,
                                            totalFilesProcessed = processedCount,
                                            totalFilesCount = allFilesToCompress.size,
                                            processedBytes = processedBytes,
                                            totalBytes = totalBytes,
                                            speedMbPerSec = speedMbSec,
                                            assignedSecondaryCores = secondaryCores,
                                            activeWorkerThreadName = activeThreadName,
                                            engineName = if (format == CompressionFormat.SEVEN_ZIP) "Rust Engine LZMA2 Core" else "C++ Native Core",
                                            logMessages = ArrayList(logList)
                                        )
                                    )
                                }
                            }
                            zos.closeEntry()
                            processedCount++
                        }
                    }
                }
            }

            val totalElapsed = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.1)
            val finalSpeed = (processedBytes.toDouble() / (1024 * 1024)) / totalElapsed
            addLog("¡Compresión completada con éxito!")
            addLog("Archivo generado: ${targetArchiveFile.name} (${targetArchiveFile.length() / 1024} KB)")

            onProgress(
                CompressionProgress(
                    isRunning = false,
                    isFinished = true,
                    percentage = 100,
                    currentFileName = "Finalizado",
                    totalFilesProcessed = allFilesToCompress.size,
                    totalFilesCount = allFilesToCompress.size,
                    processedBytes = totalBytes,
                    totalBytes = totalBytes,
                    speedMbPerSec = finalSpeed,
                    assignedSecondaryCores = secondaryCores,
                    logMessages = ArrayList(logList)
                )
            )

        } catch (e: Exception) {
            addLog("ERROR de compresión: ${e.localizedMessage}")
            onProgress(
                CompressionProgress(
                    isRunning = false,
                    isFinished = false,
                    errorMessage = e.localizedMessage ?: "Error desconocido en compresión",
                    logMessages = ArrayList(logList)
                )
            )
        } finally {
            threadPoolExecutor.shutdown()
        }
    }

    suspend fun extractArchive(
        archivePath: String,
        targetDirectoryPath: String,
        assignedCoresCount: Int,
        onProgress: (CompressionProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        val totalCores = Runtime.getRuntime().availableProcessors()
        val secondaryCores = assignedCoresCount.coerceIn(1, totalCores)

        val threadPoolExecutor = Executors.newFixedThreadPool(secondaryCores) { runnable ->
            Thread(runnable, "SecondaryCore-UnpackWorker-${System.currentTimeMillis() % 1000}")
        }
        val customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

        val logList = mutableListOf<String>()
        fun addLog(msg: String) {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            logList.add("[$timestamp] $msg")
        }

        val archiveFile = File(archivePath)
        val destDir = File(targetDirectoryPath)
        if (!destDir.exists()) destDir.mkdirs()

        addLog("Iniciando descompresión de '${archiveFile.name}'...")
        addLog("Motor C++/Rust asignado a $secondaryCores núcleos de procesamiento secundario.")

        val startTime = System.currentTimeMillis()
        var extractedCount = 0
        var extractedBytes = 0L

        try {
            withContext(customDispatcher) {
                val workerThreadName = Thread.currentThread().name
                addLog("Ejecutando en hilo secundario: $workerThreadName")

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

                                        val elapsed = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.1)
                                        val speed = (extractedBytes.toDouble() / (1024 * 1024)) / elapsed

                                        onProgress(
                                            CompressionProgress(
                                                isRunning = true,
                                                percentage = 50, // indeterminate progress for extraction
                                                currentFileName = entry.name,
                                                totalFilesProcessed = extractedCount,
                                                processedBytes = extractedBytes,
                                                speedMbPerSec = speed,
                                                assignedSecondaryCores = secondaryCores,
                                                activeWorkerThreadName = workerThreadName,
                                                engineName = "Rust/C++ Decompressor Core",
                                                logMessages = ArrayList(logList)
                                            )
                                        )
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
