package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorControlBar(
    currentMode: TextMode,
    onModeChange: (TextMode) -> Unit,
    fileExtension: String,
    isMarkdownRenderMode: Boolean,
    onMarkdownToggle: () -> Unit,
    isMonospace: Boolean,
    onMonospaceToggle: () -> Unit,
    fontSizeSp: Int,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onFormatJson: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode Switcher (Lectura / Editor)
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = currentMode == TextMode.PREVIEW,
                onClick = { onModeChange(TextMode.PREVIEW) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = {
                    Icon(
                        imageVector = if (currentMode == TextMode.PREVIEW) Icons.Default.Check else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = {
                    Text("Lectura", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            )

            SegmentedButton(
                selected = currentMode == TextMode.EDIT,
                onClick = { onModeChange(TextMode.EDIT) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = {
                    Icon(
                        imageVector = if (currentMode == TextMode.EDIT) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = {
                    Text("Editor", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            )
        }

        // Right-side controls (Markdown/JSON toggle & font adjustments)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (fileExtension.lowercase() == "json" && onFormatJson != null) {
                FilterChip(
                    selected = false,
                    onClick = onFormatJson,
                    label = {
                        Text(
                            text = "Formatear",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Formatear JSON",
                            modifier = Modifier.size(12.dp)
                        )
                    }
                )
            } else if (fileExtension.lowercase() == "md" && currentMode == TextMode.PREVIEW) {
                FilterChip(
                    selected = isMarkdownRenderMode,
                    onClick = onMarkdownToggle,
                    label = {
                        Text(
                            text = if (isMarkdownRenderMode) "Markdown" else "Plano",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isMarkdownRenderMode) Icons.Default.AutoAwesome else Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                )
            }

            IconButton(
                onClick = onMonospaceToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isMonospace) Icons.Default.FontDownload else Icons.Default.TextFields,
                    contentDescription = "Fuente Mono/Sans",
                    tint = if (isMonospace) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onDecreaseFontSize,
                modifier = Modifier.size(28.dp)
            ) {
                Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Text(
                text = "${fontSizeSp}sp",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )

            IconButton(
                onClick = onIncreaseFontSize,
                modifier = Modifier.size(28.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
