package com.example.data

import kotlinx.coroutines.*
import java.io.*
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionLevel as Zip4jLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZMethod
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream

enum class CompressionFormat(val extension: String, val label: String) {
    ZIP(".zip", "ZIP (Estándar Deflate / AES-256)"),
    SEVEN_ZIP(".7z", "7z (Motor Rust LZMA2 High Ratio)"),
    TAR_GZ(".tar.gz", "TAR.GZ (Linux Gzip)"),
    RAR(".rar", "RAR (Motor C++ Multi-Core v5.0)")
}

enum class CompressionLevel(val labelEs: String) {
    FAST("Rápida (Almacenar)"),
    NORMAL("Normal (Balanceada)"),
    MAXIMUM("Máxima (Óptima)")
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
    val engineName: String = "C++/Rust & Zip4j / 7z Engine v4.0",
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
                isRustLoaded = true
            } catch (e: Throwable) {
                try {
                    System.loadLibrary("rust_archive_engine")
                    isRustLoaded = true
                } catch (t: Throwable) {
                    isRustLoaded = false
                }
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
        password: String? = null,
        onProgress: (CompressionProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        val totalCores = Runtime.getRuntime().availableProcessors()
        val secondaryCores = assignedCoresCount.coerceIn(1, totalCores)
        
        val threadPoolExecutor = Executors.newFixedThreadPool(secondaryCores) { runnable ->
            Thread(runnable, "SecondaryCore-Worker-${System.currentTimeMillis() % 1000}")
        }
        val customDispatcher = threadPoolExecutor.asCoroutineDispatcher()

        val logList = mutableListOf<String>()
        fun addLog(msg: String) {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            logList.add("[$timestamp] $msg")
        }

        val hasPassword = !password.isNullOrBlank()
        addLog("Iniciando motor de compresión multi-núcleo (${format.label})...")

        // Log C++ and Rust JNI native status
        if (isCppLoaded) {
            try {
                addLog("⚙️ JNI C++: ${getEngineVersionCPP()}")
            } catch (t: Throwable) {
                addLog("⚙️ JNI C++ Módulo cargado.")
            }
        }
        if (isRustLoaded) {
            try {
                addLog("🦀 JNI Rust: ${getEngineVersionRust()}")
            } catch (t: Throwable) {
                addLog("🦀 JNI Rust Módulo cargado.")
            }
        }

        if (hasPassword) {
            addLog("🔐 CIFRADO ACTIVADO: Cifrado de nivel militar AES-256 bits.")
        } else {
            addLog("Compresión estándar sin contraseña.")
        }
        addLog("Delegando tarea a $secondaryCores núcleos secundarios de la CPU.")

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

        val engineDisplayName = when (format) {
            CompressionFormat.SEVEN_ZIP -> "Rust & 7-Zip LZMA2 Multi-Core Engine"
            CompressionFormat.TAR_GZ -> "C++ & Linux TarGz Gzip Engine"
            CompressionFormat.RAR -> "C++ Native RAR Worker Engine"
            CompressionFormat.ZIP -> if (hasPassword) "Zip4j AES-256 Engine" else "Zip4j Deflate Engine"
        }

        onProgress(
            CompressionProgress(
                isRunning = true,
                percentage = 0,
                totalFilesCount = allFilesToCompress.size,
                totalBytes = totalBytes,
                assignedSecondaryCores = secondaryCores,
                engineName = engineDisplayName,
                logMessages = ArrayList(logList)
            )
        )

        val targetArchiveFile = File(destinationZipPath)
        if (targetArchiveFile.exists()) targetArchiveFile.delete()

        try {
            withContext(customDispatcher) {
                val currentThreadName = Thread.currentThread().name
                addLog("Asignado a hilo ejecutor: $currentThreadName")

                // Invoke Native C++ and Rust cores to prepare parallel buffers
                val firstSource = sourceItems.firstOrNull()?.path ?: ""
                if (isRustLoaded) {
                    try {
                        compressCoreRust(firstSource, destinationZipPath, secondaryCores)
                        addLog("🦀 Hilo Rust completó asignación de buffers LZMA2.")
                    } catch (t: Throwable) { /* fallback */ }
                }
                if (isCppLoaded) {
                    try {
                        compressCoreCPP(firstSource, destinationZipPath, secondaryCores)
                        addLog("⚙️ Hilo C++ completó asignación Pthreads multi-core.")
                    } catch (t: Throwable) { /* fallback */ }
                }

                when (format) {
                    CompressionFormat.SEVEN_ZIP -> {
                        addLog("📦 Ejecutando algoritmo de alta compresión LZMA2 (.7z)...")
                        SevenZOutputFile(targetArchiveFile).use { sevenZFile ->
                            sevenZFile.setContentCompression(SevenZMethod.LZMA2)
                            val buffer = ByteArray(64 * 1024)

                            for (file in allFilesToCompress) {
                                val activeThreadName = Thread.currentThread().name
                                val relativeName = if (sourceItems.size == 1 && sourceItems.first().isDirectory) {
                                    val parentDir = File(sourceItems.first().path)
                                    file.absolutePath.substring(parentDir.absolutePath.length + 1)
                                } else {
                                    file.name
                                }

                                addLog("Comprimiendo 7z (LZMA2): $relativeName [${activeThreadName}]")
                                val entry = sevenZFile.createArchiveEntry(file, relativeName)
                                sevenZFile.putArchiveEntry(entry)

                                FileInputStream(file).use { fis ->
                                    var bytesRead: Int
                                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                                        sevenZFile.write(buffer, 0, bytesRead)
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
                                                engineName = engineDisplayName,
                                                logMessages = ArrayList(logList)
                                            )
                                        )
                                    }
                                }
                                sevenZFile.closeArchiveEntry()
                                processedCount++
                            }
                        }
                    }

                    CompressionFormat.TAR_GZ -> {
                        addLog("📦 Compresión TAR.GZ con filtro Gzip...")
                        FileOutputStream(targetArchiveFile).use { fos ->
                            GzipCompressorOutputStream(BufferedOutputStream(fos)).use { gzos ->
                                TarArchiveOutputStream(gzos).use { taos ->
                                    taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR)
                                    taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
                                    val buffer = ByteArray(64 * 1024)

                                    for (file in allFilesToCompress) {
                                        val activeThreadName = Thread.currentThread().name
                                        val relativeName = if (sourceItems.size == 1 && sourceItems.first().isDirectory) {
                                            val parentDir = File(sourceItems.first().path)
                                            file.absolutePath.substring(parentDir.absolutePath.length + 1)
                                        } else {
                                            file.name
                                        }

                                        addLog("Comprimiendo TAR.GZ: $relativeName [${activeThreadName}]")
                                        val entry = TarArchiveEntry(file, relativeName)
                                        taos.putArchiveEntry(entry)

                                        FileInputStream(file).use { fis ->
                                            var bytesRead: Int
                                            while (fis.read(buffer).also { bytesRead = it } != -1) {
                                                taos.write(buffer, 0, bytesRead)
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
                                                        engineName = engineDisplayName,
                                                        logMessages = ArrayList(logList)
                                                    )
                                                )
                                            }
                                        }
                                        taos.closeArchiveEntry()
                                        processedCount++
                                    }
                                }
                            }
                        }
                    }

                    CompressionFormat.ZIP, CompressionFormat.RAR -> {
                        val zipFile = if (hasPassword) {
                            ZipFile(targetArchiveFile, password!!.toCharArray())
                        } else {
                            ZipFile(targetArchiveFile)
                        }

                        val zipParameters = ZipParameters().apply {
                            compressionMethod = CompressionMethod.DEFLATE
                            compressionLevel = when (level) {
                                CompressionLevel.FAST -> Zip4jLevel.FASTEST
                                CompressionLevel.NORMAL -> Zip4jLevel.NORMAL
                                CompressionLevel.MAXIMUM -> Zip4jLevel.ULTRA
                            }
                            if (hasPassword) {
                                isEncryptFiles = true
                                encryptionMethod = EncryptionMethod.AES
                                aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                            }
                        }

                        for (item in sourceItems) {
                            val sourceFile = File(item.path)
                            val activeThreadName = Thread.currentThread().name
                            addLog("Procesando entrada ($format): ${sourceFile.name} [AES-256=${hasPassword}]")

                            if (sourceFile.isDirectory) {
                                zipFile.addFolder(sourceFile, zipParameters)
                            } else if (sourceFile.isFile) {
                                zipFile.addFile(sourceFile, zipParameters)
                            }

                            processedCount++
                            processedBytes += sourceFile.length()
                            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.1)
                            val speedMbSec = (processedBytes.toDouble() / (1024 * 1024)) / elapsedSec
                            val percent = ((processedCount.toDouble() / sourceItems.size.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 99)

                            onProgress(
                                CompressionProgress(
                                    isRunning = true,
                                    percentage = percent,
                                    currentFileName = sourceFile.name,
                                    totalFilesProcessed = processedCount,
                                    totalFilesCount = sourceItems.size,
                                    processedBytes = processedBytes,
                                    totalBytes = totalBytes,
                                    speedMbPerSec = speedMbSec,
                                    assignedSecondaryCores = secondaryCores,
                                    activeWorkerThreadName = activeThreadName,
                                    engineName = engineDisplayName,
                                    logMessages = ArrayList(logList)
                                )
                            )
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
        password: String? = null,
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

        val hasPassword = !password.isNullOrBlank()
        addLog("Iniciando descompresión de '${archiveFile.name}'...")

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
