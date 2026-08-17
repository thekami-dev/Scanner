package com.scann.thekami.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.scann.thekami.model.BarcodeParser
import com.scann.thekami.model.ScanResult

class BarcodeAnalyzer(
    private val onResult: (ScanResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @Volatile
    private var isProcessing = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage == null || isProcessing) {
            imageProxy.close()
            return
        }

        isProcessing = true
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val firstValid = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                if (firstValid != null) {
                    onResult(BarcodeParser.parse(firstValid))
                }
            }
            .addOnFailureListener {
                // Silently ignore — next frame will retry
            }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }
}
