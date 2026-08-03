package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RenderPlainTextWithLineNumbers(
    content: String,
    searchQuery: String,
    fontSizeSp: Int,
    isMonospace: Boolean
) {
    val lines = remember(content) { content.lines() }
    val fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
    val highlightBg = MaterialTheme.colorScheme.primaryContainer
    val highlightFg = MaterialTheme.colorScheme.onPrimaryContainer

    lines.forEachIndexed { index, line ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
        ) {
            // Line Number
            Text(
                text = "${index + 1}".padStart(4, ' '),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (fontSizeSp - 2).sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.width(36.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Line Text with Search Highlight
            val annotatedText = remember(line, searchQuery) {
                buildAnnotatedString {
                    if (searchQuery.isNotBlank() && line.contains(searchQuery, ignoreCase = true)) {
                        var startIndex = 0
                        val lowerLine = line.lowercase()
                        val lowerQuery = searchQuery.lowercase()
                        while (startIndex < line.length) {
                            val matchIndex = lowerLine.indexOf(lowerQuery, startIndex)
                            if (matchIndex == -1) {
                                append(line.substring(startIndex))
                                break
                            } else {
                                append(line.substring(startIndex, matchIndex))
                                withStyle(
                                    style = SpanStyle(
                                        background = highlightBg,
                                        color = highlightFg,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(line.substring(matchIndex, matchIndex + searchQuery.length))
                                }
                                startIndex = matchIndex + searchQuery.length
                            }
                        }
                    } else {
                        append(line)
                    }
                }
            }

            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = fontSizeSp.sp,
                    fontFamily = fontFamily
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RenderMarkdownContent(
    content: String,
    searchQuery: String,
    fontSizeSp: Int,
    isMonospace: Boolean
) {
    val lines = remember(content) { content.lines() }
    val defaultFontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default

    lines.forEach { line ->
        val trimmed = line.trim()
        when {
            trimmed.startsWith("# ") -> {
                Text(
                    text = trimmed.removePrefix("# "),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSizeSp + 8).sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            trimmed.startsWith("## ") -> {
                Text(
                    text = trimmed.removePrefix("## "),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSizeSp + 5).sp,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            trimmed.startsWith("### ") -> {
                Text(
                    text = trimmed.removePrefix("### "),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (fontSizeSp + 2).sp,
                        color = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                Row(modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)) {
                    Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = fontSizeSp.sp)
                    Text(
                        text = trimmed.substring(2),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = fontSizeSp.sp,
                            fontFamily = defaultFontFamily
                        )
                    )
                }
            }
            trimmed.startsWith("> ") -> {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trimmed.removePrefix("> "),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = fontSizeSp.sp,
                                fontStyle = FontStyle.Italic
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            trimmed.startsWith("```") -> {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = trimmed.removePrefix("```"),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = (fontSizeSp - 1).sp
                        ),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
            else -> {
                if (trimmed.isNotEmpty()) {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = fontSizeSp.sp,
                            fontFamily = defaultFontFamily
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
