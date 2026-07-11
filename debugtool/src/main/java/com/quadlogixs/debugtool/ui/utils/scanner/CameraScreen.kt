package com.quadlogixs.debugtool.ui.utils.scanner

import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

enum class AnalyzerType {
    QR,
    BARCODE,
}

private const val TAG = "CameraScreen"

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    analyzerType: AnalyzerType = AnalyzerType.QR,
    isFlashOn: Boolean = false,
    @Suppress("UNUSED_PARAMETER") onFlashToggle: (Boolean) -> Unit = {},
    onCameraReady: (CameraControl) -> Unit = {},
    onQrScanned: (String) -> Unit = {},
    onBarcodeScanned: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    val scanHandled = remember { AtomicBoolean(false) }

    val currentOnQrScanned by rememberUpdatedState(onQrScanned)
    val currentOnBarcodeScanned by rememberUpdatedState(onBarcodeScanned)
    val currentOnCameraReady by rememberUpdatedState(onCameraReady)
    val currentIsFlashOn by rememberUpdatedState(isFlashOn)

    LaunchedEffect(isFlashOn, cameraControl) {
        try {
            cameraControl?.enableTorch(isFlashOn)
        } catch (_: Exception) {
            // Torch may be unavailable on some devices.
        }
    }

    DisposableEffect(lifecycleOwner, analyzerType) {
        scanHandled.set(false)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val scanner = createBarcodeScanner(analyzerType)
        var bound = false

        val bindCamera = Runnable {
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { useCase ->
                    useCase.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { useCase ->
                        useCase.setAnalyzer(mainExecutor) { imageProxy ->
                            processFrame(
                                scanner = scanner,
                                imageProxy = imageProxy,
                                analyzerType = analyzerType,
                                scanHandled = scanHandled,
                                onQrScanned = currentOnQrScanned,
                                onBarcodeScanned = currentOnBarcodeScanned,
                            )
                        }
                    }

                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
                bound = true
                cameraControl = camera.cameraControl
                currentOnCameraReady(camera.cameraControl)
                try {
                    camera.cameraControl.enableTorch(currentIsFlashOn)
                } catch (_: Exception) {
                    // Torch may be unavailable on some devices.
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to bind camera", t)
            }
        }

        cameraProviderFuture.addListener(bindCamera, mainExecutor)

        onDispose {
            try {
                if (bound || cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to unbind camera", t)
            }
            scanner.close()
            cameraControl = null
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize(),
    )
}

private fun createBarcodeScanner(analyzerType: AnalyzerType): BarcodeScanner {
    val formats = when (analyzerType) {
        AnalyzerType.QR -> intArrayOf(Barcode.FORMAT_QR_CODE)
        AnalyzerType.BARCODE -> intArrayOf(
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_PDF417,
            Barcode.FORMAT_AZTEC,
        )
    }
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(formats.first(), *formats.drop(1).toIntArray())
        .build()
    return BarcodeScanning.getClient(options)
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processFrame(
    scanner: BarcodeScanner,
    imageProxy: ImageProxy,
    analyzerType: AnalyzerType,
    scanHandled: AtomicBoolean,
    onQrScanned: (String) -> Unit,
    onBarcodeScanned: (String) -> Unit,
) {
    if (scanHandled.get()) {
        imageProxy.close()
        return
    }

    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees,
    )

    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            if (scanHandled.get()) return@addOnSuccessListener
            val barcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() } ?: return@addOnSuccessListener
            val value = barcode.rawValue ?: return@addOnSuccessListener
            if (!scanHandled.compareAndSet(false, true)) return@addOnSuccessListener

            when (analyzerType) {
                AnalyzerType.QR -> onQrScanned(value)
                AnalyzerType.BARCODE -> {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        onQrScanned(value)
                    } else {
                        onBarcodeScanned(value)
                    }
                }
            }
        }
        .addOnFailureListener { error ->
            Log.w(TAG, "Barcode analysis failed", error)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
