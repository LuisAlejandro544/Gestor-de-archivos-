package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date

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
                LazyRow(
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
                            "json" -> "{\n  \"nombre\": \"Nuevo Archivo\",\n  \"fecha\": \"${SimpleDateFormat("yyyy-MM-dd").format(Date())}\"\n}"
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
