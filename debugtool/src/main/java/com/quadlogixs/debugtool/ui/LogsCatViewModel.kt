package com.quadlogixs.debugtool.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quadlogixs.debugtool.core.JankLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject



@HiltViewModel
class LogcatViewModel @Inject constructor(
    private val logRepository: JankLogRepository,
    @ApplicationContext val context: Context,
) : ViewModel() {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    // search & filter
    var query by mutableStateOf("")
    var useRegex by mutableStateOf(false)
    var onlyShowAppTags by mutableStateOf(false)
    var selectedLevels by mutableStateOf(setOf<String>())

    private var process: Process? = null

    init {
        startLogcatStreaming()
    }

    override fun onCleared() {
        super.onCleared()
        process?.destroy()
    }

    // NOTE: replace this with a Timber buffer or file reader in production.
    private fun startLogcatStreaming() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Only for debug/dev. `-v time` for timestamped logs, `*:S` to silence everything then include tags as needed
                val cmd = arrayOf("logcat", "-v", "time")
                val pb = ProcessBuilder(*cmd)
                pb.redirectErrorStream(true)
                process = pb.start()
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String?
                val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
                val buffer = mutableListOf<LogEntry>()
                while (reader.readLine().also { line = it } != null) {
                    line?.let { raw ->
                        // crude parsing: expected format: MM-DD HH:MM:SS.mmm PID-TID/TagLevel: message
                        // but actual formats vary. We'll do best-effort parsing.
                        val entry = parseLogLine(raw, dateFormat)
                        entry?.let {
                            buffer.add(it)
                            if (buffer.size > 2000) buffer.removeAt(0)
                            _logs.value = buffer.toList()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private fun parseLogLine(line: String, dateFormat: SimpleDateFormat): LogEntry? {
        // Many devices use different formats; attempt to extract timestamp, level, pid, tag and message.
        // Example: "10-30 11:06:12.345  1234  1234 I MyTag: This is a message"
        try {
            val parts = line.trim().split(Regex("\\s+"), limit = 6)
            if (parts.size >= 6) {
                val dateStr = parts[0] + " " + parts[1]
                val maybePid = parts[2]
                val maybeTid = parts[3]
                val level = parts[4]
                // parts[5] contains Tag: message
                val tagAndMsg = parts[5]
                val colonIndex = tagAndMsg.indexOf(":")
                val tag = if (colonIndex > 0) tagAndMsg.substring(0, colonIndex).trim() else ""
                val msg = if (colonIndex > 0) tagAndMsg.substring(colonIndex + 1).trim() else tagAndMsg
                val timestamp = try {
                    // Android logcat has no year; we use current year
                    val sdf = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
                    val date = sdf.parse(dateStr)
                    date?.time ?: System.currentTimeMillis()
                } catch (t: Exception) {
                    System.currentTimeMillis()
                }
                return LogEntry(timestamp, level, maybePid, maybeTid, tag, msg)
            } else {
                // fallback: entire line as message
                return LogEntry(System.currentTimeMillis(), "?", "?", "?", "", line)
            }
        } catch (e: Exception) {
            return LogEntry(System.currentTimeMillis(), "?", "?", "?", "", line)
        }
    }

    // Filtering performed on UI thread for simplicity
    fun filteredLogs(): List<LogEntry> {
        val base = _logs.value
        if (query.isBlank() && selectedLevels.isEmpty() && !onlyShowAppTags) return base.reversed()

        val pattern: Pattern? = try {
            if (query.isBlank()) null
            else if (useRegex) Pattern.compile(query, Pattern.CASE_INSENSITIVE)
            else Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE)
        } catch (e: Exception) {
            null
        }

        return base.filter { entry ->
            var ok = true
            if (selectedLevels.isNotEmpty()) ok = selectedLevels.contains(entry.level)
            if (ok && onlyShowAppTags) ok = entry.tag.startsWith(context.packageName.substringAfterLast('.'), ignoreCase = true) || entry.tag.contains("App", ignoreCase = true)
            if (ok && pattern != null) {
                ok = pattern.matcher(entry.tag).find() || pattern.matcher(entry.message).find() || pattern.matcher(entry.pid).find()
            }
            ok
        }.reversed()
    }
}