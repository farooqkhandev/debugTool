package com.quadlogixs.debugtool.ui.utils.qrutils

data class QRTlvEntry(
    val tag: String,
    val length: String,
    val value: String,
    val description: String,
    val children: List<QRTlvEntry> = emptyList(),
    val isError: Boolean = false,
)

data class QRTlvTableRow(
    val parentTag: String,
    val tag: String,
    val length: String,
    val value: String,
    val description: String,
    val depth: Int = 0,
    val isSubTag: Boolean = false,
)

data class QRTlvParseResult(
    val entries: List<QRTlvEntry>,
    val qrType: String,
    val initiationMethod: String?,
    val rawString: String,
    val errors: List<String> = emptyList(),
)

private class QrParseContext {
    var qrType: String = QRTagDescriptions.QR_TYPE_P2M
    var initiationMethod: String? = null
}

fun parseQRTlvDebug(raw: String): QRTlvParseResult {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) {
        return QRTlvParseResult(
            entries = listOf(
                QRTlvEntry(
                    tag = "ERR",
                    length = "00",
                    value = "",
                    description = "QR string is empty",
                    isError = true,
                )
            ),
            qrType = QRTagDescriptions.QR_TYPE_P2M,
            initiationMethod = null,
            rawString = trimmed,
            errors = listOf("QR string is empty"),
        )
    }

    val context = QrParseContext()
    val errors = mutableListOf<String>()
    val entries = parseTlvLevel(
        input = trimmed,
        parentTag = null,
        context = context,
        errors = errors,
    )

    return QRTlvParseResult(
        entries = entries,
        qrType = context.qrType,
        initiationMethod = context.initiationMethod,
        rawString = trimmed,
        errors = errors,
    )
}

private fun parseTlvLevel(
    input: String,
    parentTag: String?,
    context: QrParseContext,
    errors: MutableList<String>,
): List<QRTlvEntry> {
    val entries = mutableListOf<QRTlvEntry>()
    var offset = 0

    try {
        val mutable = input.asMutable()
        while (mutable.length > 0) {
            val startOffset = offset
            if (mutable.length < 4) {
                val msg = "Incomplete TLV at offset $startOffset (need tag + length)"
                errors.add(msg)
                entries.add(errorEntry("ERR", msg))
                break
            }

            val tag = mutable.pop(2)
            offset += 2
            val lengthStr = mutable.pop(2)
            offset += 2
            val length = lengthStr.toIntOrNull()
            if (length == null) {
                val msg = "Invalid length '$lengthStr' at offset ${startOffset + 2}"
                errors.add(msg)
                entries.add(errorEntry("ERR", msg))
                break
            }

            if (mutable.length < length) {
                val msg = "Incomplete value for tag $tag at offset $startOffset (expected $length chars)"
                errors.add(msg)
                entries.add(
                    QRTlvEntry(
                        tag = tag,
                        length = lengthStr,
                        value = mutable.value,
                        description = "Incomplete value",
                        isError = true,
                    )
                )
                break
            }

            val value = mutable.pop(length)
            offset += length

            if (parentTag == null) {
                when (tag) {
                    "00" -> context.qrType = QRTagDescriptions.detectQrType(value)
                    "01" -> context.initiationMethod = value
                }
            }

            val description = buildDescription(
                tag = tag,
                value = value,
                parentTag = parentTag,
                qrType = context.qrType,
            )

            val children = if (parentTag == null && QRTagDescriptions.shouldParseNested(tag)) {
                parseNestedChildren(tag, value, context, errors)
            } else {
                emptyList()
            }

            entries.add(
                QRTlvEntry(
                    tag = tag,
                    length = lengthStr,
                    value = value,
                    description = description,
                    children = children,
                )
            )
        }
    } catch (e: Exception) {
        val msg = "Parse error at offset $offset: ${e.message}"
        errors.add(msg)
        entries.add(errorEntry("ERR", msg))
    }

    return entries
}

private fun parseNestedChildren(
    parentTag: String,
    value: String,
    context: QrParseContext,
    errors: MutableList<String>,
): List<QRTlvEntry> {
    if (!looksLikeNestedTlv(value)) return emptyList()
    return parseTlvLevel(value, parentTag, context, errors)
}

private fun looksLikeNestedTlv(value: String): Boolean {
    if (value.length < 4) return false
    val length = value.substring(2, 4).toIntOrNull() ?: return false
    return length <= value.length - 4
}

private fun buildDescription(
    tag: String,
    value: String,
    parentTag: String?,
    qrType: String,
): String {
    val baseDescription = if (parentTag != null) {
        QRTagDescriptions.getNestedDescription(parentTag, tag)
    } else {
        QRTagDescriptions.getRootDescription(tag, qrType)
    }

    val enriched = QRTagDescriptions.enrichValueDescription(tag, value, qrType)
    return when {
        parentTag == null && tag in listOf("00", "01", "02", "53", "58", "55") && enriched != value -> {
            "$baseDescription ($enriched)"
        }
        parentTag != null -> baseDescription
        else -> baseDescription
    }
}

private fun errorEntry(tag: String, message: String): QRTlvEntry {
    return QRTlvEntry(
        tag = tag,
        length = "00",
        value = message,
        description = message,
        isError = true,
    )
}

fun formatEntry(entry: QRTlvEntry, depth: Int = 0, parentTag: String = ""): String {
    val indent = "  ".repeat(depth)
    val displayValue = tableDisplayValue(entry)
    val tagLabel = if (parentTag.isNotEmpty()) "$parentTag → ${entry.tag}" else entry.tag
    return "${indent}$tagLabel: ${entry.length}: $displayValue - ${entry.description}"
}

fun flattenEntriesToTableRows(entries: List<QRTlvEntry>, parentTag: String = "", depth: Int = 0): List<QRTlvTableRow> {
    val rows = mutableListOf<QRTlvTableRow>()
    entries.forEach { entry ->
        rows.add(
            QRTlvTableRow(
                parentTag = parentTag,
                tag = entry.tag,
                length = entry.length,
                value = tableDisplayValue(entry),
                description = entry.description,
                depth = depth,
                isSubTag = parentTag.isNotEmpty(),
            )
        )
        if (entry.children.isNotEmpty()) {
            rows.addAll(flattenEntriesToTableRows(entry.children, parentTag = entry.tag, depth = depth + 1))
        }
    }
    return rows
}

private fun tableDisplayValue(entry: QRTlvEntry): String {
    if (entry.children.isNotEmpty()) {
        return "(${entry.children.size} sub-tag${if (entry.children.size == 1) "" else "s"})"
    }
    return entry.value
}

fun formatAllEntries(entries: List<QRTlvEntry>): List<String> {
    val lines = mutableListOf<String>()
    fun walk(entry: QRTlvEntry, depth: Int, parentTag: String) {
        lines.add(formatEntry(entry, depth, parentTag))
        entry.children.forEach { walk(it, depth + 1, entry.tag) }
    }
    entries.forEach { walk(it, 0, "") }
    return lines
}

fun formatTableForClipboard(rows: List<QRTlvTableRow>): String {
    return buildString {
        appendLine("Parent\tTag\tLength\tValue\tDescription")
        rows.forEach { row ->
            appendLine("${row.parentTag}\t${row.tag}\t${row.length}\t${row.value}\t${row.description}")
        }
    }
}

fun formatParseResultSummary(result: QRTlvParseResult, crcValid: Boolean): String {
    val initiation = when (result.initiationMethod) {
        "11" -> "Static"
        "12" -> "Dynamic"
        else -> result.initiationMethod ?: "Unknown"
    }
    val tableRows = flattenEntriesToTableRows(result.entries)
    return buildString {
        appendLine("QR Type: ${result.qrType}")
        appendLine("Initiation: $initiation")
        appendLine("CRC: ${if (crcValid) "Valid" else "Invalid"}")
        appendLine("---")
        append(formatTableForClipboard(tableRows))
    }
}
