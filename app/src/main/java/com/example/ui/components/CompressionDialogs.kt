package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CompressionFormat
import com.example.data.CompressionLevel
import com.example.data.CompressionProgress

@Composable
fun CompressFileDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onCompress: (String, CompressionFormat, CompressionLevel, Int) -> Unit
) {
    var fileName by remember { mutableStateOf(defaultName) }
    var selectedFormat by remember { mutableStateOf(CompressionFormat.ZIP) }
    var selectedLevel by remember { mutableStateOf(CompressionLevel.NORMAL) }
    var secondaryCores by remember { mutableStateOf(4) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Comprimir en Núcleos Secundarios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "El proceso se ejecutará en segundo plano delegando la carga a núcleos secundarios (Motor C++ / Rust).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nombre del archivo") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("compress_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Formato de compresión:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Column {
                    CompressionFormat.values().forEach { format ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedFormat == format,
                                onClick = { selectedFormat = format }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(format.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Nivel de compresión:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CompressionLevel.values().forEach { level ->
                        FilterChip(
                            selected = selectedLevel == level,
                            onClick = { selectedLevel = level },
                            label = { Text(level.labelEs, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Núcleos de CPU secundarios:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2 to "2 Cores", 4 to "4 Cores", 8 to "Máximo (Multi-Core)").forEach { (cores, label) ->
                        FilterChip(
                            selected = secondaryCores == cores,
                            onClick = { secondaryCores = cores },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) {
                        val extension = selectedFormat.extension
                        val finalName = if (fileName.endsWith(extension)) fileName else "$fileName$extension"
                        onCompress(finalName, selectedFormat, selectedLevel, secondaryCores)
                    }
                },
                modifier = Modifier.testTag("confirm_compress_btn")
            ) {
                Text("Comprimir")
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
fun ExtractArchiveDialog(
    archiveName: String,
    onDismiss: () -> Unit,
    onConfirmExtract: (Int) -> Unit
) {
    var secondaryCores by remember { mutableStateOf(4) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Extraer '$archiveName'", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "La descompresión será procesada en núcleos secundarios para máxima velocidad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Asignación de núcleos secundarios:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2 to "2 Hilos", 4 to "4 Hilos", 8 to "Máximo C++").forEach { (cores, label) ->
                        FilterChip(
                            selected = secondaryCores == cores,
                            onClick = { secondaryCores = cores },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmExtract(secondaryCores) },
                modifier = Modifier.testTag("confirm_extract_btn")
            ) {
                Text("Extraer Aquí")
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
fun CompressionProgressDialog(
    progress: CompressionProgress,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismiss during active operation */ },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (progress.isFinished) "¡Proceso Completado!" else "Procesando en Núcleos Secundarios",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Motor: ${progress.engineName}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Hilo activo: ${progress.activeWorkerThreadName.ifEmpty { "Worker-Core-Delegated" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (progress.percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${progress.percentage}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f MB/s", progress.speedMbPerSec),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Console output log
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    val scroll = rememberScrollState()
                    LaunchedEffect(progress.logMessages.size) {
                        scroll.animateScrollTo(scroll.maxValue)
                    }
                    Column(modifier = Modifier.verticalScroll(scroll)) {
                        progress.logMessages.forEach { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (progress.isFinished) {
                Button(onClick = onCancel) {
                    Text("Cerrar")
                }
            }
        }
    )
}
