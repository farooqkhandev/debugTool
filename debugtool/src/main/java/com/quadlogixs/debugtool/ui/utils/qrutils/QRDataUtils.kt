package com.quadlogixs.debugtool.ui.utils.qrutils

import android.graphics.Bitmap

enum class QrType { MY_QR, CUSTOM_QR }

data class ReceiveQRUiState(
    val selectedType: QrType = QrType.MY_QR,
    val amount: String = "",
    val qrBitmap: Bitmap? = null,
    val errorMessage: String? = null,
    val displayName: String = "",
    val maskedIban: String = "",
    val expiryMessage: String = "",
    val tillCode: String = "",
    val isQrValid: Boolean = true,
    val showExpiryMessage: Boolean = false
)