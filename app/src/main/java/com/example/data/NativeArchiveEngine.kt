package com.example.data

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
        splitSizeMb: Int = 0,
        onProgress: (CompressionProgress) -> Unit
    ) {
        ArchiveCompressor.compressFiles(
            sourceItems = sourceItems,
            destinationZipPath = destinationZipPath,
            format = format,
            level = level,
            assignedCoresCount = assignedCoresCount,
            password = password,
            splitSizeMb = splitSizeMb,
            isCppLoaded = isCppLoaded,
            isRustLoaded = isRustLoaded,
            getEngineVersionCPP = { getEngineVersionCPP() },
            getEngineVersionRust = { getEngineVersionRust() },
            compressCoreCPP = { src, dest, cores -> compressCoreCPP(src, dest, cores) },
            compressCoreRust = { src, dest, cores -> compressCoreRust(src, dest, cores) },
            onProgress = onProgress
        )
    }

    suspend fun extractArchive(
        archivePath: String,
        targetDirectoryPath: String,
        assignedCoresCount: Int,
        password: String? = null,
        onProgress: (CompressionProgress) -> Unit
    ) {
        ArchiveExtractor.extractArchive(
            archivePath = archivePath,
            targetDirectoryPath = targetDirectoryPath,
            assignedCoresCount = assignedCoresCount,
            password = password,
            isCppLoaded = isCppLoaded,
            isRustLoaded = isRustLoaded,
            getEngineVersionCPP = { getEngineVersionCPP() },
            getEngineVersionRust = { getEngineVersionRust() },
            compressCoreCPP = { src, dest, cores -> compressCoreCPP(src, dest, cores) },
            compressCoreRust = { src, dest, cores -> compressCoreRust(src, dest, cores) },
            onProgress = onProgress
        )
    }
}
