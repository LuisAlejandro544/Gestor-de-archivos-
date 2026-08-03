package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.FileType
import com.example.data.ZipEntryItem

typealias OnDismiss = () -> Unit

@Composable
fun ZipEntryRow(
    entry: ZipEntryItem,
    onClick: () -> Unit,
    onExtractItem: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = entry.fileType.color.copy(alpha = 0.2f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (entry.isDirectory) Icons.Default.Folder else getItemIcon(entry.fileType),
                        contentDescription = entry.name,
                        tint = entry.fileType.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!entry.isDirectory && entry.uncompressedSize > 0) {
                        Text(
                            text = " • Ratio: ${entry.compressionRatio}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (entry.isDirectory) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Abrir carpeta",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isTextFileExtension(entry.extension)) Icons.Default.Visibility else Icons.Default.Info,
                        contentDescription = "Ver",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

fun getItemIcon(fileType: FileType) = when (fileType) {
    FileType.FOLDER -> Icons.Default.Folder
    FileType.ARCHIVE -> Icons.Default.FolderZip
    FileType.IMAGE -> Icons.Default.Image
    FileType.VIDEO -> Icons.Default.Movie
    FileType.AUDIO -> Icons.Default.MusicNote
    FileType.DOCUMENT -> Icons.Default.Description
    FileType.CODE -> Icons.Default.Code
    FileType.APK -> Icons.Default.Android
    FileType.JSON -> Icons.Default.DataObject
    FileType.UNKNOWN -> Icons.AutoMirrored.Filled.InsertDriveFile
}

fun isTextFileExtension(ext: String): Boolean {
    return ext.lowercase() in listOf(
        "txt", "md", "json", "xml", "kt", "java", "py", "js", "ts",
        "html", "css", "sh", "c", "cpp", "gradle", "sql", "pem", "log", "conf", "properties"
    )
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    if (digitGroups >= units.size) digitGroups = units.size - 1
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format("%.1f %s", value, units[digitGroups])
}
