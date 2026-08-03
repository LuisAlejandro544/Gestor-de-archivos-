package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FileSortType
import com.example.data.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerTopBar(
    isSearchActive: Boolean,
    onToggleSearch: () -> Unit,
    viewMode: ViewMode,
    onToggleViewMode: () -> Unit,
    showSortMenu: Boolean,
    onToggleSortMenu: (Boolean) -> Unit,
    sortType: FileSortType,
    onSetSortType: (FileSortType) -> Unit,
    showHiddenFiles: Boolean,
    onToggleShowHiddenFiles: () -> Unit,
    onOpenSettings: () -> Unit,
    isMultiSelectMode: Boolean = false,
    selectedCount: Int = 0,
    onClearSelection: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onBatchMove: () -> Unit = {},
    onBatchCopy: () -> Unit = {},
    onBatchDelete: () -> Unit = {}
) {
    if (isMultiSelectMode) {
        TopAppBar(
            title = {
                Text(
                    text = "$selectedCount seleccionado(s)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(onClick = onClearSelection) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancelar selección")
                }
            },
            actions = {
                IconButton(onClick = onSelectAll) {
                    Icon(imageVector = Icons.Default.SelectAll, contentDescription = "Seleccionar todo")
                }
                IconButton(onClick = onBatchMove, enabled = selectedCount > 0) {
                    Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = "Mover seleccionados")
                }
                IconButton(onClick = onBatchCopy, enabled = selectedCount > 0) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copiar seleccionados")
                }
                IconButton(onClick = onBatchDelete, enabled = selectedCount > 0) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar seleccionados", tint = MaterialTheme.colorScheme.error)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    } else {
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
                        text = "ArchivoX",
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
                    onClick = onToggleSearch,
                    modifier = Modifier.testTag("action_search")
                ) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                }

                // View Mode toggle (List vs Grid)
                IconButton(
                    onClick = onToggleViewMode,
                    modifier = Modifier.testTag("action_view_mode")
                ) {
                    Icon(
                        imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                        contentDescription = "Cambiar vista"
                    )
                }

                // Sort menu
                Box {
                    IconButton(
                        onClick = { onToggleSortMenu(true) },
                        modifier = Modifier.testTag("action_sort")
                    ) {
                        Icon(imageVector = Icons.Default.Sort, contentDescription = "Ordenar")
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { onToggleSortMenu(false) }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Opciones de ordenación", fontWeight = FontWeight.Bold) },
                            onClick = {},
                            enabled = false
                        )
                        HorizontalDivider()

                        FileSortType.values().forEach { sort ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sort.labelEs,
                                        fontWeight = if (sortType == sort) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sortType == sort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { onSetSortType(sort) },
                                leadingIcon = {
                                    if (sortType == sort) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                        }

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (showHiddenFiles) "Ocultar archivos (.)" else "Mostrar ocultos (.)"
                                )
                            },
                            onClick = {
                                onToggleShowHiddenFiles()
                                onToggleSortMenu(false)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (showHiddenFiles) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                // Settings Button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("action_settings")
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Configuración")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
