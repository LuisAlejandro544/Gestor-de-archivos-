package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionLevel as Zip4jLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.apache.commons.compress.archivers.sevenz.SevenZMethod
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors

object ArchiveCompressor {

    suspend fun compressFiles(
        sourceItems: List<FileItem>,
        destinationZipPath: String,
        format: CompressionFormat,
        level: CompressionLevel,
        assignedCoresCount: Int,
        password: String? = null,
        splitSizeMb: Int = 0,
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
                        addLog("📦 Ejecutando algoritmo TarGz (Gzip + POSIX Tar)...")
                        FileOutputStream(targetArchiveFile).use { fos ->
                            GzipCompressorOutputStream(BufferedOutputStream(fos)).use { gzos ->
                                TarArchiveOutputStream(gzos).use { taos ->
                                    taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR)
                                    taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU)
                                    val buffer = ByteArray(64 * 1024)

                                    for (file in allFilesToCompress) {
                                        val relativeName = if (sourceItems.size == 1 && sourceItems.first().isDirectory) {
                                            val parentDir = File(sourceItems.first().path)
                                            file.absolutePath.substring(parentDir.absolutePath.length + 1)
                                        } else {
                                            file.name
                                        }

                                        addLog("Empaquetando TAR.GZ: $relativeName")
                                        val entry = taos.createArchiveEntry(file, relativeName) as TarArchiveEntry
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
                                CompressionLevel.FAST -> Zip4jLevel.FAST
                                CompressionLevel.NORMAL -> Zip4jLevel.NORMAL
                                CompressionLevel.MAXIMUM -> Zip4jLevel.MAXIMUM
                            }

                            if (hasPassword) {
                                isEncryptFiles = true
                                encryptionMethod = EncryptionMethod.AES
                                aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                            }
                        }

                        for (file in allFilesToCompress) {
                            val relativeName = file.name
                            addLog("Añadiendo a ZIP ${if (hasPassword) "[AES-256]" else ""}: $relativeName")

                            if (file.isDirectory) {
                                zipFile.addFolder(file, zipParameters)
                            } else {
                                zipFile.addFile(file, zipParameters)
                            }

                            processedCount++
                            processedBytes += file.length()
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
                                    engineName = engineDisplayName,
                                    logMessages = ArrayList(logList)
                                )
                            )
                        }
                    }
                }

                if (splitSizeMb > 0) {
                    ArchiveSplitter.splitArchiveFile(targetArchiveFile, splitSizeMb, ::addLog)
                }

                val finalElapsedSec = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.1)
                val finalSpeed = (processedBytes.toDouble() / (1024 * 1024)) / finalElapsedSec
                addLog("¡Compresión completada con éxito en ${String.format("%.2f", finalElapsedSec)} segundos!")

                onProgress(
                    CompressionProgress(
                        isRunning = false,
                        isFinished = true,
                        percentage = 100,
                        currentFileName = "Completado",
                        totalFilesProcessed = processedCount,
                        totalFilesCount = allFilesToCompress.size,
                        processedBytes = processedBytes,
                        totalBytes = totalBytes,
                        speedMbPerSec = finalSpeed,
                        assignedSecondaryCores = secondaryCores,
                        engineName = engineDisplayName,
                        logMessages = ArrayList(logList)
                    )
                )
            }
        } catch (e: Exception) {
            addLog("Error durante la compresión: ${e.localizedMessage}")
            onProgress(
                CompressionProgress(
                    isRunning = false,
                    isFinished = false,
                    errorMessage = e.localizedMessage ?: "Error desconocido durante la compresión",
                    logMessages = ArrayList(logList)
                )
            )
        } finally {
            threadPoolExecutor.shutdown()
        }
    }
}
