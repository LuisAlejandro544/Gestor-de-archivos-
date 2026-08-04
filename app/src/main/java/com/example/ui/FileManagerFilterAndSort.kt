package com.example.ui

import com.example.data.FileItem
import com.example.data.FileSortType
import com.example.data.FileType
import com.example.data.StorageCategory

object FileManagerFilterAndSort {

    fun filterAndSortItems(
        raw: List<FileItem>,
        query: String,
        category: StorageCategory,
        sortType: FileSortType
    ): List<FileItem> {
        var result = raw

        if (category != StorageCategory.ALL) {
            result = result.filter { item ->
                when (category) {
                    StorageCategory.FOLDERS -> item.isDirectory
                    StorageCategory.DOWNLOADS -> item.path.contains("Download", ignoreCase = true) || item.fileType == FileType.ARCHIVE
                    StorageCategory.DOCUMENTS -> item.fileType == FileType.DOCUMENT
                    StorageCategory.IMAGES -> item.fileType == FileType.IMAGE
                    StorageCategory.AUDIO -> item.fileType == FileType.AUDIO
                    StorageCategory.VIDEOS -> item.fileType == FileType.VIDEO
                    StorageCategory.ARCHIVES -> item.fileType == FileType.ARCHIVE
                    else -> true
                }
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { it.name.lowercase().contains(q) }
        }

        val folders = result.filter { it.isDirectory }
        val files = result.filter { !it.isDirectory }

        val sortedFolders = sortItemList(folders, sortType)
        val sortedFiles = sortItemList(files, sortType)

        return sortedFolders + sortedFiles
    }

    private fun sortItemList(list: List<FileItem>, sortType: FileSortType): List<FileItem> {
        return when (sortType) {
            FileSortType.NAME_ASC -> list.sortedBy { it.name.lowercase() }
            FileSortType.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
            FileSortType.SIZE_ASC -> list.sortedBy { it.sizeBytes }
            FileSortType.SIZE_DESC -> list.sortedByDescending { it.sizeBytes }
            FileSortType.DATE_ASC -> list.sortedBy { it.lastModified }
            FileSortType.DATE_DESC -> list.sortedByDescending { it.lastModified }
            FileSortType.TYPE_ASC -> list.sortedBy { it.fileType.name }
        }
    }
}
