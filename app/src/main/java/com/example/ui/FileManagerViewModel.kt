package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.theme.AppColorPalette
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

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

    fun setHasStoragePermission(granted: Boolean) {
        _uiState.update { it.copy(hasStoragePermission = granted) }
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
                    items = FileManagerFilterAndSort.filterAndSortItems(files, state.searchQuery, state.selectedCategory, state.sortType)
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
        val parent = File(current).parent
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
                items = FileManagerFilterAndSort.filterAndSortItems(state.rawItems, query, state.selectedCategory, state.sortType)
            )
        }
    }

    fun selectCategory(category: StorageCategory) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                items = FileManagerFilterAndSort.filterAndSortItems(state.rawItems, state.searchQuery, category, state.sortType)
            )
        }
    }

    fun setSortType(sortType: FileSortType) {
        _uiState.update { state ->
            state.copy(
                sortType = sortType,
                showSortMenu = false,
                items = FileManagerFilterAndSort.filterAndSortItems(state.rawItems, state.searchQuery, state.selectedCategory, sortType)
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
            delay(600)
            _uiState.update {
                it.copy(
                    textExtensionProgress = 35,
                    textExtensionStatus = "Descomprimiendo motor nativo de lectura TXT & Markdown UTF-8..."
                )
            }
            delay(700)
            _uiState.update {
                it.copy(
                    textExtensionProgress = 75,
                    textExtensionStatus = "Registrando módulo de extensión 'ArchivoX Text'..."
                )
            }
            delay(500)
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
                val createdFile = File(current, trimmedName)
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

    fun startPasteMode(action: FolderPickerAction, paths: Set<String> = emptySet()) {
        val targetPaths = if (paths.isNotEmpty()) {
            paths
        } else if (_uiState.value.selectedPaths.isNotEmpty()) {
            _uiState.value.selectedPaths
        } else if (_uiState.value.selectedItem != null) {
            setOf(_uiState.value.selectedItem!!.path)
        } else {
            emptySet()
        }

        if (targetPaths.isEmpty()) {
            showMessage("No hay elementos seleccionados para operar")
            return
        }

        _uiState.update { state ->
            state.copy(
                isClipboardActive = true,
                clipboardAction = action,
                clipboardPaths = targetPaths,
                isMultiSelectMode = false,
                selectedPaths = emptySet(),
                selectedItem = null,
                showFolderPickerDialog = false
            )
        }

        val actionLabel = if (action == FolderPickerAction.MOVE) "Mover" else "Copiar"
        showMessage("Modo $actionLabel activado. Navega a la carpeta de destino y presiona 'Pegar aquí'")
    }

    fun cancelPasteMode() {
        _uiState.update { state ->
            state.copy(
                isClipboardActive = false,
                clipboardPaths = emptySet()
            )
        }
        showMessage("Operación cancelada")
    }

    fun executePaste() {
        val state = _uiState.value
        val paths = state.clipboardPaths.toList()
        val dest = state.currentPath
        val action = state.clipboardAction

        if (paths.isEmpty()) {
            cancelPasteMode()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val success = if (action == FolderPickerAction.MOVE) {
                repository.moveItems(paths, dest)
            } else {
                repository.copyItems(paths, dest)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isClipboardActive = false,
                    clipboardPaths = emptySet()
                )
            }

            if (success) {
                val actionLabel = if (action == FolderPickerAction.MOVE) "movido(s)" else "copiado(s)"
                val folderName = if (dest == state.rootPath || dest.endsWith("/")) "Almacenamiento Interno" else File(dest).name
                showMessage("¡${paths.size} elemento(s) $actionLabel en '$folderName'!")
                loadDirectory(dest)
                loadStorageInfo()
            } else {
                showMessage("Error al procesar la operación en la carpeta de destino")
            }
        }
    }

    fun openFolderPicker(action: FolderPickerAction) {
        startPasteMode(action)
    }

    fun closeFolderPicker() {
        _uiState.update { state ->
            state.copy(showFolderPickerDialog = false)
        }
    }

    fun executeBatchMove(destinationPath: String) {
        startPasteMode(FolderPickerAction.MOVE)
    }

    fun executeBatchCopy(destinationPath: String) {
        startPasteMode(FolderPickerAction.COPY)
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
            val partsDir = File(currentDir, partsFolderName)
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
            val partsDir = File(currentDir, partsFolderName)
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
}
