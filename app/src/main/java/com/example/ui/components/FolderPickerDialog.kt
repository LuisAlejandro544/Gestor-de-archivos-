package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.FileItem
import com.example.ui.FolderPickerAction
import com.example.ui.theme.FolderAmber
import java.io.File

@Composable
fun FolderPickerDialog(
    rootPath: String,
    currentPath: String,
    action: FolderPickerAction,
    itemCount: Int,
    onDismiss: () -> Unit,
    onConfirmLocation: (targetPath: String) -> Unit
) {
    var selectedPath by remember { mutableStateOf(currentPath) }
    var folderItems by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedPath) {
        isLoading = true
        val dir = File(selectedPath)
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
                        imageVector = if (action == FolderPickerAction.MOVE) Icons.Default.DriveFileMove else Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (action == FolderPickerAction.MOVE) "Mover $itemCount elemento(s)" else "Copiar $itemCount elemento(s)",
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
                            val parent = File(selectedPath).parent
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
                        overflow = TextOverflow.Ellipsis,
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
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
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
                                Icon(Icons.Default.Folder, contentDescription = null, tint = FolderAmber, modifier = Modifier.size(24.dp))
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
                Text(if (action == FolderPickerAction.MOVE) "Mover Aquí" else "Copiar Aquí", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
