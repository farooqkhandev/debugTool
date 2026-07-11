package com.quadlogixs.debugtool.ui.utils.scanner

import androidx.camera.core.CameraControl
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

enum class AnalyzerType {
    QR,
    BARCODE,
}

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    analyzerType: AnalyzerType = AnalyzerType.QR,
    isFlashOn: Boolean = false,
    onFlashToggle: (Boolean) -> Unit = {},
    onCameraReady: (CameraControl) -> Unit = {},
    onQrScanned: (String) -> Unit = {},
    onBarcodeScanned: (String) -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Camera preview stub — paste QR text manually")
    }
}
