package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
            .padding(vertical = 3.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("file_row_${item.name}"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored File / Folder Icon Badge or real APK logo
            val iconInfo = getFileIconAndColor(item.fileType, item.isDirectory)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconInfo.second.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (apkIcon != null) {
                    Image(
                        bitmap = apkIcon,
                        contentDescription = "APK Logo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = iconInfo.first,
                        contentDescription = item.fileType.name,
                        tint = iconInfo.second,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.formattedSize,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = item.formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Options Button
            IconButton(
                onClick = onOptionClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("file_options_${item.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun getFileIconAndColor(fileType: FileType, isDirectory: Boolean): Pair<ImageVector, Color> {
    if (isDirectory) {
        return Pair(Icons.Default.Folder, fileType.color)
    }
    return when (fileType) {
        FileType.ARCHIVE -> Pair(Icons.Default.FolderZip, fileType.color)
        FileType.IMAGE -> Pair(Icons.Default.Image, fileType.color)
        FileType.VIDEO -> Pair(Icons.Default.Movie, fileType.color)
        FileType.AUDIO -> Pair(Icons.Default.MusicNote, fileType.color)
        FileType.DOCUMENT -> Pair(Icons.Default.Description, fileType.color)
        FileType.CODE -> Pair(Icons.Default.Code, fileType.color)
        FileType.APK -> Pair(Icons.Default.Android, fileType.color)
        FileType.UNKNOWN, FileType.FOLDER -> Pair(Icons.Default.InsertDriveFile, fileType.color)
    }
}
