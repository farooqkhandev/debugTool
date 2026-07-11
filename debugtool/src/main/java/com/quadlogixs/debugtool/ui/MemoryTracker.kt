package com.quadlogixs.debugtool.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MemoryTracker {

    data class MemoryLog(val screen: String, val usedMemory: Long, val totalMemory: Long, val dateTime: String)

    private val _logs = mutableListOf<MemoryLog>()
    val logs: List<MemoryLog> get() = _logs.reversed() // Show latest first

    fun logMemory(screen: String) {
        val runtime = Runtime.getRuntime()
        val usedMem = runtime.totalMemory() - runtime.freeMemory()
        val totalMem = runtime.totalMemory()
        if (_logs.size > 100) {
            _logs.clear()
        }
        val dateFormatter = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.ENGLISH)
        val formattedDateTime = dateFormatter.format(Date())
        _logs.add(
            MemoryLog(
                screen = screen,
                usedMemory = usedMem,
                totalMemory = totalMem,
                dateTime = formattedDateTime
            )
        )

    }
}

