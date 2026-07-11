package com.quadlogixs.debugtool.ui.utils.qrutils

internal class MutableQrBuffer(private val builder: StringBuilder) {
    val length: Int get() = builder.length
    val value: String get() = builder.toString()

    fun pop(count: Int): String {
        if (count <= 0 || builder.isEmpty()) return ""
        val end = count.coerceAtMost(builder.length)
        val result = builder.substring(0, end)
        builder.delete(0, end)
        return result
    }
}

internal fun String.asMutable(): MutableQrBuffer = MutableQrBuffer(StringBuilder(this))

internal fun getSchemeDetails(scheme: String): String = scheme
