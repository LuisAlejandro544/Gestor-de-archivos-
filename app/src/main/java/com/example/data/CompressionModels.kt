package com.example.data

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
