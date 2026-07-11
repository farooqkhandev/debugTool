package com.quadlogixs.debugtool.ui

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

fun Bitmap.toBase64(format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 90): String {
    val stream = ByteArrayOutputStream()
    compress(format, quality, stream)
    val prefix = when (format) {
        Bitmap.CompressFormat.JPEG -> "data:image/jpeg;base64,"
        Bitmap.CompressFormat.PNG -> "data:image/png;base64,"
        else -> "data:image/png;base64,"
    }
    return prefix + Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
}
