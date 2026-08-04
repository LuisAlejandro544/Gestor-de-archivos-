package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZipExplorerDialog(
    zipItem: FileItem,
    currentDir: String,
    onDismiss: OnDismiss,
    onExtractAllClick: () -> Unit,
    onExtractSingleEntry: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { ZipViewerEngine() }

    var innerPath by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPasswordInput by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var browseResult by remember { mutableStateOf<ZipBrowseResult?>(null) }

    // State for viewing entry text preview
    var previewEntryName by remember { mutableStateOf<String?>(null) }
    var previewTextContent by remember { mutableStateOf<String?>(null) }
    var isPreviewLoading by remember { mutableStateOf(false) }

    fun refreshEntries() {
        isLoading = true
        kotlinx.coroutines.GlobalScope.run {
            // Note: Launching coroutine cleanly with LaunchedEffect below
        }
    }

    LaunchedEffect(zipItem.path, innerPath, password, searchQuery) {
        isLoading = true
        val result = engine.browseZip(
            zipFilePath = zipItem.path,
            targetInnerPath = innerPath,
            password = if (password.isBlank()) null else password,
            searchQuery = searchQuery
        )
        browseResult = result
        if (result.isEncrypted && password.isBlank()) {
            showPasswordInput = true
        }
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("zip_explorer_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = FileType.ARCHIVE.color.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FolderZip,
                                    contentDescription = "Zip",
                                    tint = FileType.ARCHIVE.color,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = zipItem.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            browseResult?.let { res ->
                                Text(
                                    text = "${res.totalEntriesCount} elementos • ${formatBytes(res.totalUncompressedBytes)} total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar en ZIP")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Optional Search Input Field
                AnimatedVisibility(visible = isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar dentro del archivo ZIP...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                // Password Prompt if Encrypted
                if (showPasswordInput || (browseResult?.isEncrypted == true && browseResult?.items.isNullOrEmpty())) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Cifrado", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Contraseña AES-256") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Inner Breadcrumb Bar
                browseResult?.let { res ->
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(res.breadcrumbs) { seg ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { innerPath = seg.innerPath }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = seg.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (seg.innerPath == res.currentInnerPath) FontWeight.Bold else FontWeight.Normal,
                                        color = if (seg.innerPath == res.currentInnerPath) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Error Message if any
                browseResult?.errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Main Content List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        val items = browseResult?.items ?: emptyList()
                        if (items.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Carpeta vacía o sin archivos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(items, key = { it.fullPath }) { entry ->
                                    ZipEntryRow(
                                        entry = entry,
                                        onClick = {
                                            if (entry.isDirectory) {
                                                innerPath = entry.fullPath
                                            } else if (isTextFileExtension(entry.extension)) {
                                                previewEntryName = entry.name
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Elemento: ${entry.name} (${entry.formattedSize})",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        onExtractItem = {
                                            coroutineScope.launch {
                                                Toast.makeText(
                                                    context,
                                                    "Copiando '${entry.name}' sin descomprimir todo...",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                val success = engine.extractZipEntry(
                                                    zipFilePath = zipItem.path,
                                                    entryFullPath = entry.fullPath,
                                                    destDirectoryPath = currentDir,
                                                    password = if (password.isBlank()) null else password
                                                )
                                                if (success) {
                                                    Toast.makeText(
                                                        context,
                                                        "¡'${entry.name}' extraído con éxito en el directorio actual!",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    onExtractSingleEntry()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Error al extraer '${entry.name}'",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (innerPath.isNotEmpty()) {
                                // Navigate up one level in ZIP
                                val trimmed = innerPath.trim('/')
                                val lastSlash = trimmed.lastIndexOf('/')
                                innerPath = if (lastSlash != -1) {
                                    trimmed.substring(0, lastSlash + 1)
                                } else ""
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (innerPath.isNotEmpty()) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (innerPath.isNotEmpty()) "Atrás en ZIP" else "Cerrar")
                    }

                    Button(
                        onClick = onExtractAllClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Unarchive,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Extraer Todo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Text File Inline Preview Sub-dialog
    previewEntryName?.let { entryName ->
        LaunchedEffect(entryName) {
            isPreviewLoading = true
            val text = engine.readZipEntryText(
                zipFilePath = zipItem.path,
                entryFullPath = browseResult?.items?.firstOrNull { it.name == entryName }?.fullPath ?: entryName,
                password = if (password.isBlank()) null else password
            )
            previewTextContent = text
            isPreviewLoading = false
        }

        AlertDialog(
            onDismissRequest = {
                previewEntryName = null
                previewTextContent = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = entryName, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    if (isPreviewLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = previewTextContent ?: "Sin contenido",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        previewEntryName = null
                        previewTextContent = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
