package com.example.ui

import com.example.data.*
import com.example.ui.theme.AppColorPalette
import com.example.ui.theme.ThemeMode

enum class FolderPickerAction { MOVE, COPY }
enum class ArchivePasswordAction { VIEW, EXTRACT }

data class FileManagerUiState(
    val rootPath: String = "",
    val currentPath: String = "",
    val breadcrumbs: List<PathSegment> = emptyList(),
    val rawItems: List<FileItem> = emptyList(),
    val items: List<FileItem> = emptyList(),
    val storageInfo: StorageInfo = StorageInfo(),
    val searchQuery: String = "",
    val selectedCategory: StorageCategory = StorageCategory.ALL,
    val sortType: FileSortType = FileSortType.NAME_ASC,
    val viewMode: ViewMode = ViewMode.LIST,
    val showHiddenFiles: Boolean = false,
    val isLoading: Boolean = false,
    val selectedItem: FileItem? = null,
    val showCreateFolderDialog: Boolean = false,
    val showCreateFileDialog: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showCompressDialog: Boolean = false,
    val showExtractDialog: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val showSortMenu: Boolean = false,
    val showPemViewerDialog: Boolean = false,
    val selectedPemItem: FileItem? = null,
    val appPalette: AppColorPalette = AppColorPalette.EMERALD,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val compressionProgress: CompressionProgress = CompressionProgress(),
    val showCompressionProgressDialog: Boolean = false,
    val isArchivoXTextInstalled: Boolean = false,
    val showTextExtensionInstallDialog: Boolean = false,
    val textExtensionProgress: Int = 0,
    val textExtensionStatus: String = "",
    val showTextViewerDialog: Boolean = false,
    val selectedTextItem: FileItem? = null,
    val showZipExplorerDialog: Boolean = false,
    val selectedZipItem: FileItem? = null,
    val userMessage: String? = null,
    // Multi-selection state
    val isMultiSelectMode: Boolean = false,
    val selectedPaths: Set<String> = emptySet(),
    val showFolderPickerDialog: Boolean = false,
    val folderPickerAction: FolderPickerAction = FolderPickerAction.MOVE,
    // Password Prompt State
    val showPasswordPromptDialog: Boolean = false,
    val passwordPromptItem: FileItem? = null,
    val passwordPromptAction: ArchivePasswordAction = ArchivePasswordAction.VIEW
)
