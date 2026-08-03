package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FileItem
import com.example.data.FileType
import com.example.util.rememberApkIcon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemRow(
    item: FileItem,
    onClick: () -> Unit,
    onOptionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apkIcon = if (item.fileType == FileType.APK) rememberApkIcon(context, item.path) else null

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onOptionClick
            )
            .testTag("file_row_${item.name}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored File / Folder Icon Badge or real APK logo
            val iconInfo = getFileIconAndColor(item.fileType, item.isDirectory, item.extension)
            val extLower = item.extension.lowercase()
            val badgeColor = when (extLower) {
                "txt" -> Color(0xFF0288D1)
                "md" -> Color(0xFF673AB7)
                "json" -> Color(0xFFF59E0B)
                else -> null
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconInfo.second.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (apkIcon != null) {
                    Image(
                        bitmap = apkIcon,
                        contentDescription = "APK Logo",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = iconInfo.first,
                        contentDescription = item.fileType.name,
                        tint = iconInfo.second,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (badgeColor != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = extLower.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(1.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.formattedSize,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Text(
                        text = item.formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Options Button
            IconButton(
                onClick = onOptionClick,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("file_options_${item.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

fun getFileIconAndColor(fileType: FileType, isDirectory: Boolean, extension: String = ""): Pair<ImageVector, Color> {
    if (isDirectory) {
        return Pair(Icons.Default.Folder, fileType.color)
    }
    val ext = extension.lowercase()
    if (ext == "txt") {
        return Pair(Icons.Default.TextSnippet, Color(0xFF0288D1)) // Clean Cyan/Blue for TXT
    }
    if (ext == "md") {
        return Pair(Icons.Default.EditNote, Color(0xFF673AB7)) // Markdown Purple for MD
    }
    if (ext == "json" || fileType == FileType.JSON) {
        return Pair(Icons.Default.DataObject, Color(0xFFF59E0B)) // Exclusive DataObject vector for JSON
    }
    return when (fileType) {
        FileType.ARCHIVE -> Pair(Icons.Default.FolderZip, fileType.color)
        FileType.IMAGE -> Pair(Icons.Default.Image, fileType.color)
        FileType.VIDEO -> Pair(Icons.Default.Movie, fileType.color)
        FileType.AUDIO -> Pair(Icons.Default.MusicNote, fileType.color)
        FileType.DOCUMENT -> Pair(Icons.Default.Description, fileType.color)
        FileType.CODE -> Pair(Icons.Default.Code, fileType.color)
        FileType.APK -> Pair(Icons.Default.Android, fileType.color)
        FileType.JSON -> Pair(Icons.Default.DataObject, Color(0xFFF59E0B))
        FileType.UNKNOWN, FileType.FOLDER -> Pair(Icons.Default.InsertDriveFile, fileType.color)
    }
}
