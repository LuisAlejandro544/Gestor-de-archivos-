package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.theme.AppColorPalette
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application.applicationContext)
    private val archiveEngine = NativeArchiveEngine()

    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    init {
        val root = repository.getDefaultStoragePath()
        _uiState.update { it.copy(rootPath = root, currentPath = root) }
        loadStorageInfo()
        loadDirectory(root)
    }

    private fun loadStorageInfo() {
        viewModelScope.launch {
            val info = repository.getStorageInfo()
            _uiState.update { it.copy(storageInfo = info) }
        }
    }

    fun loadDirectory(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentPath = path) }
            val breadcrumbs = repository.buildBreadcrumbs(path, _uiState.value.rootPath)
            val files = repository.getFilesInDirectory(path, _uiState.value.showHiddenFiles)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    currentPath = path,
                    breadcrumbs = breadcrumbs,
                    rawItems = files,
                    items = filterAndSortItems(files, state.searchQuery, state.selectedCategory, state.sortType)
                )
            }
        }
    }

    fun navigateTo(path: String) {
        loadDirectory(path)
    }

    fun navigateUp(): Boolean {
        val current = _uiState.value.currentPath
        val root = _uiState.value.rootPath
        if (current == root || current.length <= root.length) {
            return false
        }
        val parent = java.io.File(current).parent
        if (parent != null && parent.length >= root.length) {
            loadDirectory(parent)
            return true
        }
        return false
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                items = filterAndSortItems(state.rawItems, query, state.selectedCategory, state.sortType)
            )
        }
    }

    fun selectCategory(category: StorageCategory) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                items = filterAndSortItems(state.rawItems, state.searchQuery, category, state.sortType)
            )
        }
    }

    fun setSortType(sortType: FileSortType) {
        _uiState.update { state ->
            state.copy(
                sortType = sortType,
                showSortMenu = false,
                items = filterAndSortItems(state.rawItems, state.searchQuery, state.selectedCategory, sortType)
            )
        }
    }

    fun toggleViewMode() {
        _uiState.update { state ->
            val nextMode = if (state.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
            state.copy(viewMode = nextMode)
        }
    }

    fun toggleShowHiddenFiles() {
        _uiState.update { state ->
            val nextHidden = !state.showHiddenFiles
            state.copy(showHiddenFiles = nextHidden)
        }
        loadDirectory(_uiState.value.currentPath)
    }

    fun setAppPalette(palette: AppColorPalette) {
        _uiState.update { it.copy(appPalette = palette) }
        showMessage("Tema cambiado a ${palette.labelEs}")
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleSortMenu(show: Boolean? = null) {
        _uiState.update { state ->
            state.copy(showSortMenu = show ?: !state.showSortMenu)
        }
    }

    fun selectItemForAction(item: FileItem?) {
        _uiState.update { it.copy(selectedItem = item) }
    }

    fun setShowCreateFolderDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateFolderDialog = show) }
    }

    fun setShowRenameDialog(show: Boolean) {
        _uiState.update { it.copy(showRenameDialog = show) }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun setShowCompressDialog(show: Boolean) {
        _uiState.update { it.copy(showCompressDialog = show) }
    }

    fun setShowExtractDialog(show: Boolean) {
        _uiState.update { it.copy(showExtractDialog = show) }
    }

    fun setShowSettingsSheet(show: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = show) }
    }

    fun openPemViewer(item: FileItem) {
        _uiState.update { it.copy(selectedPemItem = item, showPemViewerDialog = true) }
    }

    fun closePemViewer() {
        _uiState.update { it.copy(selectedPemItem = null, showPemViewerDialog = false) }
    }

    fun openZipExplorer(item: FileItem) {
        _uiState.update { it.copy(selectedZipItem = item, showZipExplorerDialog = true) }
    }

    fun closeZipExplorer() {
        _uiState.update { it.copy(selectedZipItem = null, showZipExplorerDialog = false) }
    }

    fun dismissCompressionProgress() {
        _uiState.update { it.copy(showCompressionProgressDialog = false) }
    }

    fun openTextFileWithExtension(item: FileItem) {
        if (_uiState.value.isArchivoXTextInstalled) {
            _uiState.update { it.copy(selectedTextItem = item, showTextViewerDialog = true) }
        } else {
            installTextExtensionAndOpen(item)
        }
    }

    private fun installTextExtensionAndOpen(item: FileItem) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showTextExtensionInstallDialog = true,
                    textExtensionProgress = 0,
                    textExtensionStatus = "Descargando paquete de extensión ArchivoX Text v1.0 (1.2 MB)..."
                )
            }
            kotlinx.coroutines.delay(600)
            _uiState.update {
                it.copy(
                    textExtensionProgress = 35,
                    textExtensionStatus = "Descomprimiendo motor nativo de lectura TXT & Markdown UTF-8..."
                )
            }
            kotlinx.coroutines.delay(700)
            _uiState.update {
                it.copy(
                    textExtensionProgress = 75,
                    textExtensionStatus = "Registrando módulo de extensión 'ArchivoX Text'..."
                )
            }
            kotlinx.coroutines.delay(500)
            _uiState.update {
                it.copy(
                    textExtensionProgress = 100,
                    textExtensionStatus = "¡Instalación completada!",
                    isArchivoXTextInstalled = true,
                    showTextExtensionInstallDialog = false,
                    selectedTextItem = item,
                    showTextViewerDialog = true
                )
            }
        }
    }

    fun closeTextViewer() {
        _uiState.update { it.copy(selectedTextItem = null, showTextViewerDialog = false) }
    }

    fun cancelTextExtensionInstall() {
        _uiState.update { it.copy(showTextExtensionInstallDialog = false) }
    }

    fun setShowCreateFileDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateFileDialog = show) }
    }

    fun createNewFile(fileName: String, initialContent: String = "") {
        viewModelScope.launch {
            val current = _uiState.value.currentPath
            val trimmedName = fileName.trim()
            if (trimmedName.isEmpty()) return@launch
            val success = repository.createNewFile(current, trimmedName, initialContent)
            if (success) {
                showMessage("Archivo '$trimmedName' creado correctamente")
                setShowCreateFileDialog(false)
                loadDirectory(current)
                val createdFile = java.io.File(current, trimmedName)
                val ext = createdFile.extension.lowercase()
                if (ext in listOf("txt", "md", "json", "xml", "kt", "java", "py", "sh", "html", "css", "js", "ts", "cpp", "c", "sql")) {
                    val fileItem = FileItem(
                        name = trimmedName,
                        path = createdFile.absolutePath,
                        isDirectory = false,
                        sizeBytes = createdFile.length(),
                        lastModified = createdFile.lastModified(),
                        extension = ext,
                        fileType = when (ext) {
                            "json" -> FileType.JSON
                            "txt", "md" -> FileType.DOCUMENT
                            else -> FileType.CODE
                        }
                    )
                    openTextFileWithExtension(fileItem)
                }
            } else {
                showMessage("Error: ya existe un archivo con ese nombre o no se pudo crear")
            }
        }
    }

    fun toggleMultiSelectMode(enabled: Boolean? = null) {
        _uiState.update { state ->
            val next = enabled ?: !state.isMultiSelectMode
            state.copy(
                isMultiSelectMode = next,
                selectedPaths = if (!next) emptySet() else state.selectedPaths
            )
        }
    }

    fun toggleSelectPath(path: String) {
        _uiState.update { state ->
            val set = state.selectedPaths.toMutableSet()
            if (set.contains(path)) set.remove(path) else set.add(path)
            state.copy(
                selectedPaths = set,
                isMultiSelectMode = true
            )
        }
    }

    fun selectAllItems() {
        _uiState.update { state ->
            val allPaths = state.items.map { it.path }.toSet()
            state.copy(selectedPaths = allPaths, isMultiSelectMode = true)
        }
    }

    fun clearSelection() {
        _uiState.update { state ->
            state.copy(selectedPaths = emptySet(), isMultiSelectMode = false)
        }
    }

    fun openFolderPicker(action: FolderPickerAction) {
        _uiState.update { state ->
            state.copy(showFolderPickerDialog = true, folderPickerAction = action)
        }
    }

    fun closeFolderPicker() {
        _uiState.update { state ->
            state.copy(showFolderPickerDialog = false)
        }
    }

    fun executeBatchMove(destinationPath: String) {
        val selected = _uiState.value.selectedPaths.toList()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            closeFolderPicker()
            _uiState.update { it.copy(isLoading = true) }
            val success = repository.moveItems(selected, destinationPath)
            if (success) {
                showMessage("${selected.size} elemento(s) movido(s) con éxito")
                clearSelection()
                loadDirectory(_uiState.value.currentPath)
                loadStorageInfo()
            } else {
                showMessage("Error al mover algunos elementos")
            }
        }
    }

    fun executeBatchCopy(destinationPath: String) {
        val selected = _uiState.value.selectedPaths.toList()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            closeFolderPicker()
            _uiState.update { it.copy(isLoading = true) }
            val success = repository.copyItems(selected, destinationPath)
            if (success) {
                showMessage("${selected.size} elemento(s) copiado(s) con éxito")
                clearSelection()
                loadDirectory(_uiState.value.currentPath)
                loadStorageInfo()
            } else {
                showMessage("Error al copiar algunos elementos")
            }
        }
    }

    fun executeBatchDelete() {
        val selected = _uiState.value.selectedPaths.toList()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            var deletedCount = 0
            for (path in selected) {
                if (repository.deleteFileOrDirectory(path)) deletedCount++
            }
            showMessage("Se eliminaron $deletedCount elemento(s)")
            clearSelection()
            loadDirectory(_uiState.value.currentPath)
            loadStorageInfo()
        }
    }

    fun openPasswordPromptForArchive(item: FileItem, action: ArchivePasswordAction) {
        _uiState.update {
            it.copy(
                showPasswordPromptDialog = true,
                passwordPromptItem = item,
                passwordPromptAction = action
            )
        }
    }

    fun closePasswordPrompt() {
        _uiState.update {
            it.copy(
                showPasswordPromptDialog = false,
                passwordPromptItem = null
            )
        }
    }

    fun submitArchivePassword(password: String) {
        val item = _uiState.value.passwordPromptItem ?: return
        val action = _uiState.value.passwordPromptAction
        closePasswordPrompt()

        if (action == ArchivePasswordAction.VIEW) {
            _uiState.update { it.copy(selectedZipItem = item, showZipExplorerDialog = true) }
        } else if (action == ArchivePasswordAction.EXTRACT) {
            _uiState.update { it.copy(selectedItem = item) }
            extractSelectedItem(assignedCores = Runtime.getRuntime().availableProcessors(), password = password)
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val current = _uiState.value.currentPath
            val success = repository.createNewDirectory(current, name.trim())
            if (success) {
                showMessage("Carpeta '$name' creada con éxito")
                setShowCreateFolderDialog(false)
                loadDirectory(current)
            } else {
                showMessage("Error al crear la carpeta")
            }
        }
    }

    fun deleteSelectedItem() {
        val item = _uiState.value.selectedItem ?: return
        viewModelScope.launch {
            val success = repository.deleteFileOrDirectory(item.path)
            if (success) {
                showMessage("Se eliminó '${item.name}'")
                selectItemForAction(null)
                setShowDeleteDialog(false)
                loadDirectory(_uiState.value.currentPath)
                loadStorageInfo()
            } else {
                showMessage("Error al eliminar el elemento")
            }
        }
    }

    fun renameSelectedItem(newName: String) {
        val item = _uiState.value.selectedItem ?: return
        viewModelScope.launch {
            val success = repository.renameFileOrDirectory(item.path, newName.trim())
            if (success) {
                showMessage("Renombrado a '$newName'")
                selectItemForAction(null)
                setShowRenameDialog(false)
                loadDirectory(_uiState.value.currentPath)
            } else {
                showMessage("Error al renombrar el elemento")
            }
        }
    }

    fun compressBatchSelectedItems(
        targetArchiveName: String,
        format: CompressionFormat,
        level: CompressionLevel,
        assignedCores: Int,
        password: String? = null,
        splitSizeMb: Int = 0
    ) {
        val selectedPaths = _uiState.value.selectedPaths.toList()
        if (selectedPaths.isEmpty()) return
        val selectedItems = _uiState.value.items.filter { it.path in selectedPaths }
        if (selectedItems.isEmpty()) return

        val currentDir = _uiState.value.currentPath
        val destPath = if (splitSizeMb > 0) {
            val partsFolderName = targetArchiveName.substringBeforeLast(".") + "_partes"
            val partsDir = java.io.File(currentDir, partsFolderName)
            if (!partsDir.exists()) partsDir.mkdirs()
            "${partsDir.absolutePath}/$targetArchiveName"
        } else {
            "$currentDir/$targetArchiveName"
        }

        setShowCompressDialog(false)
        _uiState.update { it.copy(showCompressionProgressDialog = true) }

        viewModelScope.launch {
            archiveEngine.compressFiles(
                sourceItems = selectedItems,
                destinationZipPath = destPath,
                format = format,
                level = level,
                assignedCoresCount = assignedCores,
                password = password,
                splitSizeMb = splitSizeMb,
                onProgress = { progress ->
                    _uiState.update { state -> state.copy(compressionProgress = progress) }
                }
            )
            clearSelection()
            loadDirectory(_uiState.value.currentPath)
            loadStorageInfo()
        }
    }

    fun compressSelectedItem(
        targetArchiveName: String,
        format: CompressionFormat,
        level: CompressionLevel,
        assignedCores: Int,
        password: String? = null,
        splitSizeMb: Int = 0
    ) {
        val item = _uiState.value.selectedItem ?: return
        val currentDir = _uiState.value.currentPath
        val destPath = if (splitSizeMb > 0) {
            val partsFolderName = targetArchiveName.substringBeforeLast(".") + "_partes"
            val partsDir = java.io.File(currentDir, partsFolderName)
            if (!partsDir.exists()) partsDir.mkdirs()
            "${partsDir.absolutePath}/$targetArchiveName"
        } else {
            "$currentDir/$targetArchiveName"
        }

        setShowCompressDialog(false)
        _uiState.update { it.copy(showCompressionProgressDialog = true) }

        viewModelScope.launch {
            archiveEngine.compressFiles(
                sourceItems = listOf(item),
                destinationZipPath = destPath,
                format = format,
                level = level,
                assignedCoresCount = assignedCores,
                password = password,
                splitSizeMb = splitSizeMb,
                onProgress = { progress ->
                    _uiState.update { state -> state.copy(compressionProgress = progress) }
                }
            )
            loadDirectory(_uiState.value.currentPath)
            loadStorageInfo()
        }
    }

    fun extractSelectedItem(assignedCores: Int, password: String? = null) {
        val item = _uiState.value.selectedItem ?: return
        val currentDir = _uiState.value.currentPath
        setShowExtractDialog(false)
        _uiState.update { it.copy(showCompressionProgressDialog = true) }

        viewModelScope.launch {
            archiveEngine.extractArchive(
                archivePath = item.path,
                targetDirectoryPath = currentDir,
                assignedCoresCount = assignedCores,
                password = password,
                onProgress = { progress ->
                    _uiState.update { state -> state.copy(compressionProgress = progress) }
                }
            )
            loadDirectory(_uiState.value.currentPath)
            loadStorageInfo()
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun showMessage(msg: String) {
        _uiState.update { it.copy(userMessage = msg) }
    }

    private fun filterAndSortItems(
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
