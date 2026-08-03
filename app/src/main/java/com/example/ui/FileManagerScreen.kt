package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FileItem
import com.example.data.ViewMode
import com.example.ui.components.*

@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isSearchActive by remember { mutableStateOf(false) }

    // Intercept back press to navigate up directory hierarchy if not at root
    BackHandler(enabled = uiState.currentPath != uiState.rootPath) {
        viewModel.navigateUp()
    }

    // Show toast messages when ViewModel emits userMessage
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FileManagerTopBar(
                isSearchActive = isSearchActive,
                onToggleSearch = { isSearchActive = !isSearchActive },
                viewMode = uiState.viewMode,
                onToggleViewMode = { viewModel.toggleViewMode() },
                showSortMenu = uiState.showSortMenu,
                onToggleSortMenu = { viewModel.toggleSortMenu(it) },
                sortType = uiState.sortType,
                onSetSortType = { viewModel.setSortType(it) },
                showHiddenFiles = uiState.showHiddenFiles,
                onToggleShowHiddenFiles = { viewModel.toggleShowHiddenFiles() },
                onOpenSettings = { viewModel.setShowSettingsSheet(true) },
                isMultiSelectMode = uiState.isMultiSelectMode,
                selectedCount = uiState.selectedPaths.size,
                onClearSelection = { viewModel.clearSelection() },
                onSelectAll = { viewModel.selectAllItems() },
                onBatchMove = { viewModel.openFolderPicker(FolderPickerAction.MOVE) },
                onBatchCopy = { viewModel.openFolderPicker(FolderPickerAction.COPY) },
                onBatchDelete = { viewModel.executeBatchDelete() }
            )
        },
        floatingActionButton = {
            var showFabMenu by remember { mutableStateOf(false) }

            Box {
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_create_menu")
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Crear nuevo...",
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nueva Carpeta", fontWeight = FontWeight.SemiBold) },
                        onClick = {
                            showFabMenu = false
                            viewModel.setShowCreateFolderDialog(true)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Nuevo Archivo (.txt, .json, .md...)", fontWeight = FontWeight.SemiBold) },
                        onClick = {
                            showFabMenu = false
                            viewModel.setShowCreateFileDialog(true)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PostAdd, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Field
            AnimatedVisibility(visible = isSearchActive) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Buscar archivos o carpetas...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_input")
                )
            }

            // Storage Summary Card & Category Filter Chips
            StorageHeader(
                storageInfo = uiState.storageInfo,
                selectedCategory = uiState.selectedCategory,
                onCategorySelect = { viewModel.selectCategory(it) }
            )

            // Breadcrumb Navigation Bar
            BreadcrumbBar(
                breadcrumbs = uiState.breadcrumbs,
                onSegmentClick = { path -> viewModel.navigateTo(path) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Subheader: Count and Current Sort Mode Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.items.size} elementos",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = uiState.sortType.labelEs,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Main File List / Grid Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (uiState.items.isEmpty()) {
                    FileManagerEmptyState()
                } else {
                    val onItemClick: (FileItem) -> Unit = { item ->
                        if (uiState.isMultiSelectMode) {
                            viewModel.toggleSelectPath(item.path)
                        } else if (item.isDirectory) {
                            viewModel.navigateTo(item.path)
                        } else if (item.fileType == com.example.data.FileType.ARCHIVE || item.extension.lowercase() in listOf("zip", "7z", "tar", "gz", "tgz", "apk", "jar", "rar")) {
                            if (item.isEncrypted) {
                                viewModel.openPasswordPromptForArchive(item, ArchivePasswordAction.VIEW)
                            } else {
                                viewModel.openZipExplorer(item)
                            }
                        } else if (item.extension.lowercase() in listOf("pem", "key", "crt", "cer", "pub", "p8", "keytool")) {
                            viewModel.openPemViewer(item)
                        } else if (item.extension.lowercase() in listOf("txt", "md", "json", "xml", "kt", "java", "py", "sh", "html", "css", "js", "ts", "cpp", "c", "sql")) {
                            viewModel.openTextFileWithExtension(item)
                        } else {
                            viewModel.selectItemForAction(item)
                        }
                    }

                    val onItemOptionsClick: (FileItem) -> Unit = { item ->
                        if (uiState.isMultiSelectMode) {
                            viewModel.toggleSelectPath(item.path)
                        } else {
                            viewModel.selectItemForAction(item)
                        }
                    }

                    if (uiState.viewMode == ViewMode.LIST) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(uiState.items, key = { it.path }) { item ->
                                FileItemRow(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    onOptionClick = { onItemOptionsClick(item) },
                                    isMultiSelectMode = uiState.isMultiSelectMode,
                                    isSelected = uiState.selectedPaths.contains(item.path),
                                    onToggleSelect = { viewModel.toggleSelectPath(item.path) }
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 100.dp),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(uiState.items, key = { it.path }) { item ->
                                FileItemCard(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    onOptionClick = { onItemOptionsClick(item) },
                                    isMultiSelectMode = uiState.isMultiSelectMode,
                                    isSelected = uiState.selectedPaths.contains(item.path),
                                    onToggleSelect = { viewModel.toggleSelectPath(item.path) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Details BottomSheet
    uiState.selectedItem?.let { item ->
        if (!uiState.showRenameDialog && !uiState.showDeleteDialog && !uiState.showCompressDialog && !uiState.showExtractDialog) {
            FileDetailsBottomSheet(
                item = item,
                onDismiss = { viewModel.selectItemForAction(null) },
                onOpen = {
                    if (item.isDirectory) {
                        viewModel.navigateTo(item.path)
                    }
                },
                onRenameClick = { viewModel.setShowRenameDialog(true) },
                onDeleteClick = { viewModel.setShowDeleteDialog(true) },
                onCompressClick = { viewModel.setShowCompressDialog(true) },
                onExtractClick = { viewModel.setShowExtractDialog(true) },
                onExploreZipClick = { viewModel.openZipExplorer(item) },
                onPemViewerClick = { viewModel.openPemViewer(item) },
                onTextEditorClick = { viewModel.openTextFileWithExtension(item) }
            )
        }
    }

    // Zip Explorer Dialog
    if (uiState.showZipExplorerDialog && uiState.selectedZipItem != null) {
        ZipExplorerDialog(
            zipItem = uiState.selectedZipItem!!,
            currentDir = uiState.currentPath,
            onDismiss = { viewModel.closeZipExplorer() },
            onExtractAllClick = {
                viewModel.selectItemForAction(uiState.selectedZipItem)
                viewModel.closeZipExplorer()
                viewModel.setShowExtractDialog(true)
            }
        )
    }

    // Compress File Dialog
    if (uiState.showCompressDialog && uiState.selectedItem != null) {
        CompressFileDialog(
            defaultName = uiState.selectedItem!!.name.substringBeforeLast("."),
            onDismiss = { viewModel.setShowCompressDialog(false) },
            onCompress = { targetName, format, level, cores, password, splitSizeMb ->
                viewModel.compressSelectedItem(targetName, format, level, cores, password, splitSizeMb)
            }
        )
    }

    // Extract Archive Dialog
    if (uiState.showExtractDialog && uiState.selectedItem != null) {
        ExtractArchiveDialog(
            archiveName = uiState.selectedItem!!.name,
            onDismiss = { viewModel.setShowExtractDialog(false) },
            onConfirmExtract = { cores, password ->
                viewModel.extractSelectedItem(cores, password)
            }
        )
    }

    // Compression Progress Dialog
    if (uiState.showCompressionProgressDialog) {
        CompressionProgressDialog(
            progress = uiState.compressionProgress,
            onCancel = { viewModel.dismissCompressionProgress() }
        )
    }

    // Settings Bottom Sheet
    if (uiState.showSettingsSheet) {
        SettingsBottomSheet(
            currentPalette = uiState.appPalette,
            currentThemeMode = uiState.themeMode,
            showHiddenFiles = uiState.showHiddenFiles,
            onPaletteChange = { viewModel.setAppPalette(it) },
            onThemeModeChange = { viewModel.setThemeMode(it) },
            onToggleHiddenFiles = { viewModel.toggleShowHiddenFiles() },
            onDismiss = { viewModel.setShowSettingsSheet(false) }
        )
    }

    // Create Folder Dialog
    if (uiState.showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { viewModel.setShowCreateFolderDialog(false) },
            onCreate = { folderName -> viewModel.createFolder(folderName) }
        )
    }

    // Rename Dialog
    if (uiState.showRenameDialog && uiState.selectedItem != null) {
        RenameDialog(
            currentName = uiState.selectedItem!!.name,
            onDismiss = { viewModel.setShowRenameDialog(false) },
            onRename = { newName -> viewModel.renameSelectedItem(newName) }
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog && uiState.selectedItem != null) {
        DeleteConfirmDialog(
            itemName = uiState.selectedItem!!.name,
            onDismiss = { viewModel.setShowDeleteDialog(false) },
            onConfirmDelete = { viewModel.deleteSelectedItem() }
        )
    }

    // Native PEM Key / Certificate Viewer Dialog
    if (uiState.showPemViewerDialog && uiState.selectedPemItem != null) {
        PemViewerDialog(
            item = uiState.selectedPemItem!!,
            onDismiss = { viewModel.closePemViewer() }
        )
    }

    // ArchivoX Text Extension Installation Dialog
    if (uiState.showTextExtensionInstallDialog) {
        TextExtensionInstallDialog(
            progress = uiState.textExtensionProgress,
            statusText = uiState.textExtensionStatus,
            onCancel = { viewModel.cancelTextExtensionInstall() }
        )
    }

    // ArchivoX Text Native Viewer Dialog
    if (uiState.showTextViewerDialog && uiState.selectedTextItem != null) {
        ArchivoXTextViewerDialog(
            item = uiState.selectedTextItem!!,
            onDismiss = { viewModel.closeTextViewer() }
        )
    }

    // Create File Dialog
    if (uiState.showCreateFileDialog) {
        CreateFileDialog(
            onDismiss = { viewModel.setShowCreateFileDialog(false) },
            onCreate = { fileName, initialContent ->
                viewModel.createNewFile(fileName, initialContent)
            }
        )
    }

    // Folder Picker Dialog (For Move and Copy)
    if (uiState.showFolderPickerDialog) {
        FolderPickerDialog(
            rootPath = uiState.rootPath,
            currentPath = uiState.currentPath,
            action = uiState.folderPickerAction,
            itemCount = uiState.selectedPaths.size,
            onDismiss = { viewModel.closeFolderPicker() },
            onConfirmLocation = { destinationPath ->
                if (uiState.folderPickerAction == FolderPickerAction.MOVE) {
                    viewModel.executeBatchMove(destinationPath)
                } else {
                    viewModel.executeBatchCopy(destinationPath)
                }
            }
        )
    }

    // Archive Password Prompt Dialog
    if (uiState.showPasswordPromptDialog && uiState.passwordPromptItem != null) {
        ArchivePasswordDialog(
            archiveName = uiState.passwordPromptItem!!.name,
            action = uiState.passwordPromptAction,
            onDismiss = { viewModel.closePasswordPrompt() },
            onSubmitPassword = { password ->
                viewModel.submitArchivePassword(password)
            }
        )
    }
}
