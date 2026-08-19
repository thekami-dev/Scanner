package com.scann.thekami.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.scann.thekami.camera.BarcodeAnalyzer
import com.scann.thekami.model.ScanResult
import com.scann.thekami.util.GalleryScanner
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    onBarcodeScanned: (ScanResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var hasScanned by remember { mutableStateOf(false) }
    var isProcessingGalleryImage by remember { mutableStateOf(false) }
    val galleryPickerLauncherTrigger = remember { mutableIntStateOf(0) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val galleryPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && !hasScanned) {
            isProcessingGalleryImage = true
            coroutineScope.launch {
                val result = GalleryScanner.scanImage(context, uri)
                isProcessingGalleryImage = false
                if (result != null) {
                    hasScanned = true
                    onBarcodeScanned(result)
                }
            }
        }
    }

    val galleryPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            galleryPickerLauncherTrigger.intValue += 1
        }
    }

    LaunchedEffect(galleryPickerLauncherTrigger.intValue) {
        if (galleryPickerLauncherTrigger.intValue > 0) {
            galleryPickerLauncher.launch("image/*")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    fun onGalleryClick() {
        val granted = ContextCompat.checkSelfPermission(context, galleryPermission) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            galleryPickerLauncher.launch("image/*")
        } else {
            galleryPermissionLauncher.launch(galleryPermission)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setTargetResolution(android.util.Size(1280, 720))
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { result ->
                                    if (!hasScanned) {
                                        hasScanned = true
                                        onBarcodeScanned(result)
                                    }
                                })
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            // Camera bind failed
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scan frame overlay — subtle corner brackets for a premium feel
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(260.dp)
            ) {
                ScanFrameCorners()
            }

            // Top label
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Scann",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }

            // Bottom-left: Gallery, Bottom-right: Flash — positioned above the nav bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 140.dp)
            ) {
                CircleIconButton(
                    icon = Icons.Filled.PhotoLibrary,
                    contentDescription = "Scan from gallery",
                    loading = isProcessingGalleryImage,
                    onClick = { onGalleryClick() }
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 140.dp)
            ) {
                CircleIconButton(
                    icon = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = "Toggle flash",
                    active = isFlashOn,
                    onClick = {
                        isFlashOn = !isFlashOn
                        camera?.cameraControl?.enableTorch(isFlashOn)
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera permission is required to scan QR codes and barcodes.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    loading: Boolean = false,
    active: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (active) Color.White else Color.Black.copy(alpha = 0.45f)
    val tintColor = if (active) Color.Black else Color.White

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bgColor,
        modifier = Modifier.size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = tintColor,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tintColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun ScanFrameCorners() {
    val cornerColor = Color.White.copy(alpha = 0.9f)
    val strokeWidth = 4.dp
    val cornerLength = 32.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(width = cornerLength, height = strokeWidth)
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(width = strokeWidth, height = cornerLength)
                .background(cornerColor)
        )
        // Top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(width = cornerLength, height = strokeWidth)
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(width = strokeWidth, height = cornerLength)
                .background(cornerColor)
        )
        // Bottom-left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(width = cornerLength, height = strokeWidth)
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(width = strokeWidth, height = cornerLength)
                .background(cornerColor)
        )
        // Bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(width = cornerLength, height = strokeWidth)
                .background(cornerColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(width = strokeWidth, height = cornerLength)
                .background(cornerColor)
        )
    }
}
