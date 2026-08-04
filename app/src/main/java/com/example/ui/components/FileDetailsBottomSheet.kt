package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onTextEditorClick: () -> Unit = {},
    onSelectMultiClick: () -> Unit = {},
    onMoveClick: () -> Unit = {},
    onCopyClick: () -> Unit = {}
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

            // Move / Copy Buttons for single item
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onMoveClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_single_move")
                ) {
                    Icon(Icons.Default.DriveFileMove, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mover")
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onCopyClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_single_copy")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    onDismiss()
                    onSelectMultiClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("action_select_multi")
            ) {
                Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seleccionar varios")
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
fun DetailRow(label: String, value: String) {
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
