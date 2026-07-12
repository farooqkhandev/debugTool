package com.quadlogixs.debugtool.ui

/*
Compose LogCat Viewer
- Single-file example showing an Android Jetpack Compose UI that:
  - Streams logcat output (dev mode only)
  - Filters/searches by free-text, tag, level, and PID/thread
  - Highlights matches and supports regex search
  - Supports copy / share / clear actions per log row

Notes:
- Reading system-wide logcat from an app is restricted on modern Android (READ_LOGS permission is signature-protected).
  For development/debug builds use one of these approaches:
   1) Use a custom logger (Timber) and keep an in-memory buffer or file that this viewer reads.
   2) Spawn `logcat` process: Runtime.getRuntime().exec("logcat -v time -d") — works on emulators or rooted devices or when running via ADB shell.
   3) Use `ProcessBuilder` and stream `logcat` output; include checks to only run in debug builds.

This file uses a Process-based reader but the `startLogcatStreaming()` function can be replaced with a file or Observable source.
*/


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.quadlogixs.debugtool.ui.components.BodySmallText
import com.quadlogixs.debugtool.ui.components.LabelMediumText
import com.quadlogixs.debugtool.ui.components.LabelSmallText
import com.quadlogixs.debugtool.ui.components.ResourceImage
import com.quadlogixs.debugtool.ui.components.safeClickable
import com.quadlogixs.debugtool.ui.components.sdp
import com.quadlogixs.debugtool.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

// --- Data models ---
sealed class LogLevel(val short: String) {
    object V : LogLevel("V")
    object D : LogLevel("D")
    object I : LogLevel("I")
    object W : LogLevel("W")
    object E : LogLevel("E")
}

data class LogEntry(
    val timestamp: Long,
    val level: String,
    val pid: String,
    val thread: String,
    val tag: String,
    val message: String
)



// --- UI ---

@Composable
fun LogcatScreen(viewModel: LogcatViewModel) {
    val logs by viewModel.logs.collectAsState()
    val filtered = remember(logs, viewModel.query, viewModel.selectedLevels, viewModel.onlyShowAppTags, viewModel.useRegex) {
        viewModel.filteredLogs()
    }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
      /*  TopBar(
            query = viewModel.query,
            onQueryChange = { viewModel.query = it },
            onClear = { viewModel.query = "" },
            onClearAll = { viewModel.clearLogs() },
            useRegex = viewModel.useRegex,
            onToggleRegex = { viewModel.useRegex = it }
        )*/

        FilterRow(
            levelsSelected = viewModel.selectedLevels,
            onLevelToggle = { level ->
                viewModel.selectedLevels = if (viewModel.selectedLevels.contains(level)) viewModel.selectedLevels - level else viewModel.selectedLevels + level
            },
            onlyApp = viewModel.onlyShowAppTags,
            onOnlyAppToggle = { viewModel.onlyShowAppTags = it }
        )

        Divider()

        BodySmallText(
            text = "Showing ${filtered.size} entries",
            modifier = Modifier.padding(4.dp),
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered) { entry ->
                LogRow(entry = entry, highlight = viewModel.query, useRegex = viewModel.useRegex,clipboardManager=clipboardManager)
            }
        }
    }
}

@Composable
private fun TopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onClearAll: () -> Unit,
    useRegex: Boolean,
    onToggleRegex: (Boolean) -> Unit
) {

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            leadingIcon = { ResourceImage(image = com.quadlogixs.debugtool.R.drawable.ic_search, modifier = Modifier.size(15.sdp))},
            modifier = Modifier.weight(1f).padding(4.dp),
            placeholder = { LabelMediumText(text = "Search tag/message/pid... (regex optional)") }
        )
        ResourceImage(modifier = Modifier.size(18.sdp).safeClickable{
            onClear()
        }, image = R.drawable.ic_clear)
        ResourceImage(modifier = Modifier.size(18.sdp).safeClickable{
            onClearAll()
        }, image = R.drawable.ic_clear)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
            LabelMediumText(text = "Regex")
            Switch(checked = useRegex, onCheckedChange = onToggleRegex)
        }
    }
}

@Composable
private fun FilterRow(levelsSelected: Set<String>, onLevelToggle: (String) -> Unit, onlyApp: Boolean, onOnlyAppToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val levels = listOf("V","D","I","W","E")
        levels.forEach { level ->
            FilterChip(text = level, selected = levelsSelected.contains(level)) { onLevelToggle(level) }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.safeClickable{
            onOnlyAppToggle(!onlyApp)
        }, verticalAlignment = Alignment.CenterVertically) {
            LabelMediumText(text = "Only Tags")
            val color =if(onlyApp){
                MaterialTheme.colorScheme.tertiary } else {Color.LightGray }
            ResourceImage(image = R.drawable.ic_toggle_radio_off, colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(color) )
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.wrapContentSize().clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        shape = MaterialTheme.shapes.small
    ) {
        LabelMediumText(text = text, modifier = Modifier.padding(6.dp))
    }
}

@Composable
private fun LogRow(entry: LogEntry, highlight: String, useRegex: Boolean,clipboardManager:ClipboardManager) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).animateContentSize()) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LabelSmallText(
                    text = formatTimestamp(entry.timestamp),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(140.dp),
                )
                LabelSmallText(
                    text = entry.level,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
                LabelSmallText(
                    text = entry.tag,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 6.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                 ResourceImage(modifier = Modifier.size(18.sdp).safeClickable{
                     clipboardManager.setText(
                         AnnotatedString(
                            "${entry.timestamp} : ${entry.tag}\n${entry.message}\n\n ${entry.thread}"
                         )
                     )
                 },image = R.drawable.ic_copy)
            }

            Spacer(modifier = Modifier.height(4.dp))
            val annotated = highlightAnnotated(entry.message, highlight, useRegex)
            Text(
                text = annotated,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (expanded) {
                LabelSmallText(text = "PID: ${entry.pid}  TID: ${entry.thread}")
            }
            LabelMediumText(
                text = if (!expanded) "Show details" else "Hide details",
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 4.dp),
            )
        }
    }
}

private fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return sdf.format(Date(ts))
}

@Composable
private fun highlightAnnotated(message: String, query: String, useRegex: Boolean) =
    buildAnnotatedString {
        if (query.isBlank()) {
            append(message)
            return@buildAnnotatedString
        }
        try {
            val pattern = if (useRegex) Pattern.compile(query, Pattern.CASE_INSENSITIVE) else Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(message)
            var last = 0
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                if (start > last) append(message.substring(last, start))
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(message.substring(start, end))
                }
                last = end
            }
            if (last < message.length) append(message.substring(last))
        } catch (e: Exception) {
            append(message)
        }
    }
