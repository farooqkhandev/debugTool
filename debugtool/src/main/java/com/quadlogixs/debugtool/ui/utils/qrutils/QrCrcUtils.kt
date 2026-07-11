package com.quadlogixs.debugtool.ui.utils.qrutils

fun generateCRC(args: String): String {
    var crc = 0xFFFF
    val polynomial = 0x1021
    val bytes = args.toByteArray()
    for (b in bytes) {
        for (i in 0..7) {
            val bit = b.toInt() shr 7 - i and 1 == 1
            val c15 = crc shr 15 and 1 == 1
            crc = crc shl 1
            if (c15 xor bit) crc = crc xor polynomial
        }
    }
    crc = crc and 0xffff
    val result = Integer.toHexString(crc).uppercase()
    return if (result.length < 4) "0".repeat(4 - result.length) + result else result
}

fun checkCRCValidity(qrString: String): Boolean {
    if (qrString.length < 4) return false
    return try {
        val providedCRC = qrString.takeLast(4)
        val generatedCRC1 = generateCRC(qrString.dropLast(4))
        val generatedCRC2 = generateCRC(qrString.dropLast(8))
        providedCRC.equals(generatedCRC1, ignoreCase = true) ||
            providedCRC.equals(generatedCRC2, ignoreCase = true)
    } catch (_: Exception) {
        false
    }
}
