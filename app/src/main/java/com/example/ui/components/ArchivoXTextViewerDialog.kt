package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivoXTextViewerDialog(
    item: FileItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var savedContent by remember { mutableStateOf("") }
    var editableContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var currentMode by remember { mutableStateOf(TextMode.PREVIEW) }
    var isMarkdownRenderMode by remember { mutableStateOf(item.extension.lowercase() == "md") }
    var fontSizeSp by remember { mutableStateOf(14) }
    var isMonospace by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(savedContent, editableContent) {
        savedContent != editableContent
    }

    // Read File content initially
    LaunchedEffect(item.path) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                val f = File(item.path)
                val text = if (f.exists() && f.isFile) {
                    f.readText(Charsets.UTF_8)
                } else {
                    "El archivo no existe o no se puede leer."
                }
                savedContent = text
                editableContent = text
            } catch (e: Exception) {
                val err = "Error al leer el archivo: ${e.localizedMessage}"
                savedContent = err
                editableContent = err
            }
        }
        isLoading = false
    }

    // Format JSON function asynchronously off-thread
    fun formatJsonContent() {
        coroutineScope.launch {
            try {
                val formatted = withContext(Dispatchers.Default) {
                    val trimmed = editableContent.trim()
                    if (trimmed.startsWith("[")) {
                        org.json.JSONArray(trimmed).toString(2)
                    } else {
                        org.json.JSONObject(trimmed).toString(2)
                    }
                }
                editableContent = formatted
                Toast.makeText(context, "¡JSON Formateado!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al formatear JSON: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
    fun saveFile() {
        if (isSaving) return
        isSaving = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val f = File(item.path)
                f.writeText(editableContent, Charsets.UTF_8)
                withContext(Dispatchers.Main) {
                    savedContent = editableContent
                    Toast.makeText(context, "¡Archivo guardado correctamente!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al guardar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isSaving = false
                }
            }
        }
    }

    // Compute text statistics asynchronously on Dispatchers.Default to prevent UI freezes on large files
    val textStats by produceState(
        initialValue = Triple(0, 0, 0), // lineCount, wordCount, charCount
        key1 = editableContent
    ) {
        value = withContext(Dispatchers.Default) {
            if (editableContent.isBlank()) {
                Triple(0, 0, 0)
            } else {
                val lines = editableContent.lines().size
                val words = editableContent.trim().split(Regex("\\s+")).size
                val chars = editableContent.length
                Triple(lines, words, chars)
            }
        }
    }

    val (lineCount, wordCount, charCount) = textStats

    Dialog(
        onDismissRequest = {
            if (hasUnsavedChanges) {
                // Allows closing or saving
                onDismiss()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("archivox_text_editor_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (hasUnsavedChanges) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.error
                                        ) {
                                            Box(modifier = Modifier.size(8.dp))
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            text = "ArchivoX Text v1.1",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (hasUnsavedChanges) "Sin guardar" else "Guardado",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = if (hasUnsavedChanges) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver"
                                )
                            }
                        },
                        actions = {
                            // Save Action Button
                            IconButton(
                                onClick = { saveFile() },
                                enabled = hasUnsavedChanges && !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Guardar cambios",
                                        tint = if (hasUnsavedChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }

                            // Toggle Search Button
                            IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                                Icon(
                                    imageVector = if (isSearchVisible) Icons.Default.Clear else Icons.Default.Search,
                                    contentDescription = "Buscar"
                                )
                            }

                            // Copy All Button
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(editableContent))
                                    Toast.makeText(context, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar todo"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 3.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$lineCount lin  |  $wordCount pal  |  $charCount car",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            if (hasUnsavedChanges) {
                                Button(
                                    onClick = { saveFile() },
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = "UTF-8",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 12.dp)
                ) {
                    // Control Header Toolbar (Mode selector & font controls)
                    TextEditorControlBar(
                        currentMode = currentMode,
                        onModeChange = { currentMode = it },
                        fileExtension = item.extension,
                        isMarkdownRenderMode = isMarkdownRenderMode,
                        onMarkdownToggle = { isMarkdownRenderMode = !isMarkdownRenderMode },
                        isMonospace = isMonospace,
                        onMonospaceToggle = { isMonospace = !isMonospace },
                        fontSizeSp = fontSizeSp,
                        onDecreaseFontSize = { if (fontSizeSp > 10) fontSizeSp -= 2 },
                        onIncreaseFontSize = { if (fontSizeSp < 32) fontSizeSp += 2 },
                        onFormatJson = { formatJsonContent() }
                    )

                    // Optional Search Bar Input
                    AnimatedVisibility(visible = isSearchVisible) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar en el texto...") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Main Work Area (Editor vs Preview)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            when (currentMode) {
                                TextMode.EDIT -> {
                                    // Full-Screen Editable Text Field
                                    OutlinedTextField(
                                        value = editableContent,
                                        onValueChange = { editableContent = it },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .testTag("text_editor_input"),
                                        textStyle = TextStyle(
                                            fontSize = fontSizeSp.sp,
                                            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = (fontSizeSp + 6).sp
                                        ),
                                        placeholder = {
                                            Text("Escribe aquí el contenido del archivo...")
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            disabledBorderColor = Color.Transparent,
                                            errorBorderColor = Color.Transparent
                                        )
                                    )
                                }

                                TextMode.PREVIEW -> {
                                    // Formatted Render Preview (Uses LazyColumn internally for smooth multi-threaded scrolling)
                                    SelectionContainer(modifier = Modifier.fillMaxSize()) {
                                        if (item.extension.lowercase() == "json") {
                                            RenderJsonContent(
                                                content = editableContent,
                                                searchQuery = searchQuery,
                                                fontSizeSp = fontSizeSp,
                                                isMonospace = isMonospace
                                            )
                                        } else if (isMarkdownRenderMode && item.extension.lowercase() == "md") {
                                            RenderMarkdownContent(
                                                content = editableContent,
                                                searchQuery = searchQuery,
                                                fontSizeSp = fontSizeSp,
                                                isMonospace = isMonospace
                                            )
                                        } else {
                                            RenderPlainTextWithLineNumbers(
                                                content = editableContent,
                                                searchQuery = searchQuery,
                                                fontSizeSp = fontSizeSp,
                                                isMonospace = isMonospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
