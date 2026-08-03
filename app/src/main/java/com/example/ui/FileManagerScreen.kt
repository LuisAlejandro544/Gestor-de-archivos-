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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FileSortType
import com.example.data.ViewMode
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gestor de Archivos",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }
                },
                actions = {
                    // Search toggle
                    IconButton(
                        onClick = { isSearchActive = !isSearchActive },
                        modifier = Modifier.testTag("action_search")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }

                    // View Mode toggle (List vs Grid)
                    IconButton(
                        onClick = { viewModel.toggleViewMode() },
                        modifier = Modifier.testTag("action_view_mode")
                    ) {
                        Icon(
                            imageVector = if (uiState.viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                            contentDescription = "Cambiar vista"
                        )
                    }

                    // Sort menu
                    Box {
                        IconButton(
                            onClick = { viewModel.toggleSortMenu(true) },
                            modifier = Modifier.testTag("action_sort")
                        ) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = "Ordenar")
                        }

                        DropdownMenu(
                            expanded = uiState.showSortMenu,
                            onDismissRequest = { viewModel.toggleSortMenu(false) }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Opciones de ordenación", fontWeight = FontWeight.Bold) },
                                onClick = {},
                                enabled = false
                            )
                            HorizontalDivider()

                            FileSortType.values().forEach { sortType ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sortType.labelEs,
                                            fontWeight = if (uiState.sortType == sortType) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.sortType == sortType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = { viewModel.setSortType(sortType) },
                                    leadingIcon = {
                                        if (uiState.sortType == sortType) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (uiState.showHiddenFiles) "Ocultar archivos (.)" else "Mostrar ocultos (.)"
                                    )
                                },
                                onClick = {
                                    viewModel.toggleShowHiddenFiles()
                                    viewModel.toggleSortMenu(false)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (uiState.showHiddenFiles) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = { viewModel.setShowSettingsSheet(true) },
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowCreateFolderDialog(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_folder")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "Nueva carpeta")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Nueva Carpeta", fontWeight = FontWeight.Bold)
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

            // Main File List / Grid
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
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Esta carpeta está vacía",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Usa el botón '+' para crear una nueva carpeta o explora otros directorios.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    if (uiState.viewMode == ViewMode.LIST) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(uiState.items, key = { it.path }) { item ->
                                FileItemRow(
                                    item = item,
                                    onClick = {
                                        if (item.isDirectory) {
                                            viewModel.navigateTo(item.path)
                                        } else {
                                            viewModel.selectItemForAction(item)
                                        }
                                    },
                                    onOptionClick = {
                                        viewModel.selectItemForAction(item)
                                    }
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
                                    onClick = {
                                        if (item.isDirectory) {
                                            viewModel.navigateTo(item.path)
                                        } else {
                                            viewModel.selectItemForAction(item)
                                        }
                                    },
                                    onOptionClick = {
                                        viewModel.selectItemForAction(item)
                                    }
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
                onExtractClick = { viewModel.setShowExtractDialog(true) }
            )
        }
    }

    // Compress File Dialog
    if (uiState.showCompressDialog && uiState.selectedItem != null) {
        CompressFileDialog(
            defaultName = uiState.selectedItem!!.name.substringBeforeLast("."),
            onDismiss = { viewModel.setShowCompressDialog(false) },
            onCompress = { targetName, format, level, cores ->
                viewModel.compressSelectedItem(targetName, format, level, cores)
            }
        )
    }

    // Extract Archive Dialog
    if (uiState.showExtractDialog && uiState.selectedItem != null) {
        ExtractArchiveDialog(
            archiveName = uiState.selectedItem!!.name,
            onDismiss = { viewModel.setShowExtractDialog(false) },
            onConfirmExtract = { cores ->
                viewModel.extractSelectedItem(cores)
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
}
