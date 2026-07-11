package com.quadlogixs.debugtool.core

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppCrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val log = buildCrashLog(thread, throwable)
        saveCrashLog(context, log)

        // You can still let the default handler do its job (for crash reporting, etc.)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun buildCrashLog(thread: Thread, throwable: Throwable): String {
        val dateFormatter = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.ENGLISH)
        val formattedDateTime = dateFormatter.format(Date())
        return """${formattedDateTime}|||Thread: ${thread.name} Message: ${throwable.message}
Stack Trace:
${Log.getStackTraceString(throwable)}
""".trimIndent()
    }
}


fun saveCrashLog(context: Context, log: String) {
    val file = File(context.filesDir, "crash_logs.txt")
    val existingLogs = if (file.exists()) file.readText() else ""
    val newLog = buildString {
        appendLine(log.trim())
        appendLine("\nCRASH_ENDS\n")
        append(existingLogs)
    }
    file.writeText(newLog)
}


fun getCrashLogs(context: Context): String {
    val file = File(context.filesDir, "crash_logs.txt")
    return if (file.exists()) file.readText() else "No crash logs found."
}

fun clearCrashLogs(context: Context) {
    val file = File(context.filesDir, "crash_logs.txt")
    if (file.exists()) file.delete()
}

fun getCrashCount(context: Context): Int {
    val file = File(context.filesDir, "crash_logs.txt")
    if (!file.exists()) return 0
    return file.readText()
        .split("CRASH_ENDS")
        .count { it.trim().isNotEmpty() }
}

