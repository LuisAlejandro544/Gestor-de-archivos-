package com.example.data

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ArchiveSplitter {

    fun splitArchiveFile(file: File, splitSizeMb: Int, addLog: (String) -> Unit): List<File> {
        if (!file.exists() || splitSizeMb <= 0) return listOf(file)
        val chunkSize = splitSizeMb.toLong() * 1024L * 1024L
        val parts = mutableListOf<File>()
        val buffer = ByteArray(1024 * 1024)
        val parentDir = file.parentFile ?: return listOf(file)
        val baseName = file.name

        addLog("✂️ Dividiendo archivo en partes de $splitSizeMb MB en carpeta '${parentDir.name}'...")

        var partNumber = 1
        var bytesWrittenCurrentPart = 0L
        var currentOutputStream: FileOutputStream? = null

        try {
            FileInputStream(file).use { fis ->
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    if (currentOutputStream == null || bytesWrittenCurrentPart >= chunkSize) {
                        currentOutputStream?.close()
                        val partName = "$baseName.part${String.format("%03d", partNumber)}"
                        val partFile = File(parentDir, partName)
                        parts.add(partFile)
                        currentOutputStream = FileOutputStream(partFile)
                        addLog("📦 Creando parte ${partNumber}: $partName")
                        partNumber++
                        bytesWrittenCurrentPart = 0L
                    }
                    currentOutputStream?.write(buffer, 0, bytesRead)
                    bytesWrittenCurrentPart += bytesRead
                }
                currentOutputStream?.close()
            }

            if (parts.isNotEmpty()) {
                file.delete()
                addLog("✅ Se crearon exitosamente ${parts.size} partes en '${parentDir.name}'")
            }
        } catch (e: Exception) {
            addLog("⚠️ Error al dividir partes: ${e.localizedMessage}")
        }
        return parts
    }
}
