package com.scann.thekami.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.scann.thekami.model.BarcodeParser
import com.scann.thekami.model.ScanResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object GalleryScanner {

    suspend fun scanImage(context: Context, uri: Uri): ScanResult? =
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()

                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val firstValid = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                        val result = firstValid?.let { BarcodeParser.parse(it) }
                        if (continuation.isActive) continuation.resume(result)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume(null)
            }
        }
}
