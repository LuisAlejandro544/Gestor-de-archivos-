package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FileItem
import com.example.data.FileType
import com.example.util.rememberApkIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailsBottomSheet(
    item: FileItem,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompressClick: () -> Unit,
    onExtractClick: () -> Unit,
    onExploreZipClick: () -> Unit = {},
    onPemViewerClick: () -> Unit = {},
    onTextEditorClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val iconInfo = getFileIconAndColor(item.fileType, item.isDirectory, item.extension)
    val apkIcon = if (item.fileType == FileType.APK) rememberApkIcon(context, item.path) else null
    val isPemOrKey = item.extension.lowercase() in listOf("pem", "key", "crt", "cer", "pub", "p8", "keytool")
    val isTextOrMd = item.extension.lowercase() in listOf("txt", "md") || item.fileType == FileType.DOCUMENT

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("file_details_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header with Icon and Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(iconInfo.second.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (apkIcon != null) {
                        Image(
                            bitmap = apkIcon,
                            contentDescription = "APK Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = iconInfo.first,
                            contentDescription = null,
                            tint = iconInfo.second,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (item.isDirectory) "Carpeta de archivos" else "Archivo ${item.extension.uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // File Details List
            DetailRow(label = "Ruta", value = item.path)
            DetailRow(label = "Tamaño", value = item.formattedSize)
            DetailRow(label = "Modificado", value = item.formattedDate)
            DetailRow(label = "Lectura / Escritura", value = if (item.canRead && item.canWrite) "Sí / Sí" else "Restringido")

            Spacer(modifier = Modifier.height(16.dp))

            // PEM Key / Certificate Viewer Button
            if (isPemOrKey) {
                Button(
                    onClick = {
                        onPemViewerClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_view_pem"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Clave / Certificado PEM", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // ArchivoX Text Extension Viewer Button
            if (isTextOrMd) {
                Button(
                    onClick = {
                        onDismiss()
                        onTextEditorClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_view_text"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Default.Article, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abrir con ArchivoX Text", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Compression & Extraction Actions (Multi-Core Native Engine C++/Rust)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (item.fileType == FileType.ARCHIVE) {
                    Button(
                        onClick = {
                            onDismiss()
                            onExploreZipClick()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_explore_zip"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explorar ZIP")
                    }

                    Button(
                        onClick = {
                            onExtractClick()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_extract"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Unarchive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Extraer")
                    }
                } else {
                    Button(
                        onClick = {
                            onCompressClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("action_compress"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Comprimir (.zip/.rar)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Standard Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (item.isDirectory) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpen()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_open_folder")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Abrir")
                    }
                }

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Ruta de archivo", item.path)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Ruta copiada al portapapeles", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Ruta")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onRenameClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_rename")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Renombrar")
                }

                Button(
                    onClick = {
                        onDeleteClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_delete"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.65f)
        )
    }
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Carpeta", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Introduce el nombre para la nueva carpeta:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Ej. Mi_Documentos") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_folder_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onCreate(text)
                    }
                },
                enabled = text.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_folder")
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renombrar", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Introduce el nuevo nombre:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank() && text != currentName) {
                        onRename(text)
                    }
                },
                enabled = text.isNotBlank() && text != currentName,
                modifier = Modifier.testTag("confirm_rename")
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar elemento?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar '$itemName'? Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete")
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFileDialog(
    onDismiss: () -> Unit,
    onCreate: (fileName: String, initialContent: String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var selectedExt by remember { mutableStateOf("txt") }
    val extensionOptions = listOf("txt", "json", "md", "xml", "kt", "py", "html", "css", "js")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PostAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Archivo", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Ingresa el nombre del archivo y selecciona la extensión:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    placeholder = { Text("Ej: nuevo_documento") },
                    singleLine = true,
                    suffix = { Text(".$selectedExt", fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_file_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Extensión rápida:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(extensionOptions.size) { idx ->
                        val ext = extensionOptions[idx]
                        FilterChip(
                            selected = (selectedExt == ext),
                            onClick = {
                                selectedExt = ext
                                if (fileName.contains(".")) {
                                    fileName = fileName.substringBeforeLast(".")
                                }
                            },
                            label = { Text(".$ext") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (fileName.contains(".")) fileName else "$fileName.$selectedExt"
                    if (finalName.isNotBlank()) {
                        val defaultContent = when (selectedExt) {
                            "json" -> "{\n  \"nombre\": \"Nuevo Archivo\",\n  \"fecha\": \"${java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())}\"\n}"
                            "md" -> "# Título\n\nEscribe tu nota aquí..."
                            "xml" -> "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n</resources>"
                            else -> ""
                        }
                        onCreate(finalName, defaultContent)
                    }
                },
                enabled = fileName.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_file")
            ) {
                Text("Crear Archivo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun FolderPickerDialog(
    rootPath: String,
    currentPath: String,
    action: com.example.ui.FolderPickerAction,
    itemCount: Int,
    onDismiss: () -> Unit,
    onConfirmLocation: (targetPath: String) -> Unit
) {
    var selectedPath by remember { mutableStateOf(currentPath) }
    var folderItems by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedPath) {
        isLoading = true
        val dir = java.io.File(selectedPath)
        val files = dir.listFiles()?.filter { it.isDirectory }?.map { sub ->
            FileItem(
                name = sub.name,
                path = sub.absolutePath,
                isDirectory = true,
                itemCount = sub.listFiles()?.size ?: 0
            )
        }?.sortedBy { it.name.lowercase() } ?: emptyList()
        folderItems = files
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (action == com.example.ui.FolderPickerAction.MOVE) Icons.Default.DriveFileMove else Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (action == com.example.ui.FolderPickerAction.MOVE) "Mover $itemCount elemento(s)" else "Copiar $itemCount elemento(s)",
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Selecciona la carpeta de destino:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.heightIn(max = 350.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val parent = java.io.File(selectedPath).parent
                            if (parent != null && parent.length >= rootPath.length) {
                                selectedPath = parent
                            }
                        },
                        enabled = selectedPath.length > rootPath.length
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Subir nivel")
                    }
                    Text(
                        text = selectedPath.substringAfterLast('/').ifEmpty { "Raíz" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (folderItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No hay subcarpetas aquí", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(folderItems.size) { idx ->
                            val folder = folderItems[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedPath = folder.path }
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = com.example.ui.theme.FolderAmber, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(folder.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmLocation(selectedPath) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (action == com.example.ui.FolderPickerAction.MOVE) "Mover Aquí" else "Copiar Aquí", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ArchivePasswordDialog(
    archiveName: String,
    action: com.example.ui.ArchivePasswordAction,
    onDismiss: () -> Unit,
    onSubmitPassword: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ZIP Protegido con Clave", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "El archivo '$archiveName' está protegido con contraseña. Por favor ingrésala para ${if (action == com.example.ui.ArchivePasswordAction.VIEW) "explorar su contenido" else "extraerlo"}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar contraseña"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("archive_password_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.isNotBlank()) onSubmitPassword(password)
                },
                enabled = password.isNotBlank(),
                modifier = Modifier.testTag("confirm_archive_password")
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
