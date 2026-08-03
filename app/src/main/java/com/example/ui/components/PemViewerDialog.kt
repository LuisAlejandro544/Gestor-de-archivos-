package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FileItem
import java.io.File
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PemViewerDialog(
    item: FileItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    var fileContent by remember { mutableStateOf("") }
    var pemHeaderType by remember { mutableStateOf("Clave PEM Desconocida") }
    var isEncrypted by remember { mutableStateOf(false) }
    var lineCount by remember { mutableStateOf(0) }
    var charCount by remember { mutableStateOf(0) }
    var sha256Fingerprint by remember { mutableStateOf("") }
    var rawBase64Only by remember { mutableStateOf("") }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item.path) {
        try {
            val file = File(item.path)
            if (file.exists() && file.canRead()) {
                val content = file.readText(Charsets.UTF_8)
                fileContent = content
                charCount = content.length
                val lines = content.lines()
                lineCount = lines.size

                // Analyze PEM headers
                pemHeaderType = detectPemType(content)
                isEncrypted = content.contains("ENCRYPTED") || content.contains("Proc-Type: 4,ENCRYPTED")

                // Extract pure Base64 without headers/footers
                rawBase64Only = content
                    .lines()
                    .filter { !it.startsWith("-----") && !it.contains(":") }
                    .joinToString("")
                    .trim()

                // Calculate SHA-256 fingerprint
                if (content.isNotBlank()) {
                    val md = MessageDigest.getInstance("SHA-256")
                    val digest = md.digest(content.toByteArray(Charsets.UTF_8))
                    sha256Fingerprint = digest.joinToString(":") { String.format("%02X", it) }
                }
            } else {
                loadError = "No se pudo leer el archivo .pem (Permisos o archivo no encontrado)"
            }
        } catch (e: Exception) {
            loadError = "Error al abrir archivo: ${e.localizedMessage}"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(8.dp)
                .testTag("pem_viewer_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Bar with Lock Icon & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "PEM Key Viewer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Visor Nativo de Claves y Certificados PEM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Badges (Key Type, Encryption, Lines & Size)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(pemHeaderType, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    if (isEncrypted) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("🔒 Cifrada", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }

                    SuggestionChip(
                        onClick = {},
                        label = { Text("$lineCount líneas • $charCount B", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (sha256Fingerprint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHA-256: ${sha256Fingerprint.take(24)}...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content View / Monospace Editor View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(12.dp)
                ) {
                    if (loadError != null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = loadError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            fileContent.lines().forEachIndexed { index, line ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    Text(
                                        text = String.format("%3d  ", index + 1),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    )

                                    val lineColor = when {
                                        line.startsWith("-----BEGIN") || line.startsWith("-----END") -> MaterialTheme.colorScheme.primary
                                        line.contains(":") -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    val lineFontWeight = if (line.startsWith("-----")) FontWeight.Bold else FontWeight.Normal

                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = lineFontWeight,
                                            color = lineColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Copy Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Clave Base64 PEM", rawBase64Only)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Base64 copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_raw_base64")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar Base64", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Archivo PEM Completo", fileContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "PEM completo copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_full_pem"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar PEM", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun detectPemType(content: String): String {
    return when {
        content.contains("-----BEGIN RSA PRIVATE KEY-----") -> "🔑 Clave Privada RSA (PKCS#1)"
        content.contains("-----BEGIN PRIVATE KEY-----") -> "🔑 Clave Privada (PKCS#8)"
        content.contains("-----BEGIN EC PRIVATE KEY-----") -> "🔑 Clave Privada EC (Curva Elíptica)"
        content.contains("-----BEGIN ENCRYPTED PRIVATE KEY-----") -> "🔐 Clave Privada Cifrada"
        content.contains("-----BEGIN CERTIFICATE-----") -> "📜 Certificado X.509"
        content.contains("-----BEGIN PUBLIC KEY-----") || content.contains("-----BEGIN RSA PUBLIC KEY-----") -> "🔓 Clave Pública RSA"
        content.contains("-----BEGIN OPENSSH PRIVATE KEY-----") -> "🔑 Clave Privada OpenSSH"
        content.contains("ssh-rsa") || content.contains("ssh-ed25519") -> "🔓 Clave Pública SSH"
        else -> "📄 Clave/Certificado PEM"
    }
}
