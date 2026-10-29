package com.scann.thekami.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.scann.thekami.camera.BarcodeAnalyzer
import com.scann.thekami.model.ScanResult
import com.scann.thekami.ui.components.BottomNavHeight
import com.scann.thekami.util.GalleryScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Camera UI is always dark, whatever the app theme, so overlays use a fixed scrim
private val ScanScrim = Color.Black.copy(alpha = 0.5f)
private val ScanFrameSize = 260.dp

@Composable
fun ScannerScreen(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onBarcodeScanned: (ScanResult) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val reducedMotion = rememberReducedMotion()
    val coroutineScope = rememberCoroutineScope()
    val isActiveState by rememberUpdatedState(isActive)

    fun checkCameraPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    var hasCameraPermission by remember { mutableStateOf(checkCameraPermission()) }
    var cameraPermanentlyDenied by remember { mutableStateOf(false) }

    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var hasScanned by remember { mutableStateOf(false) }
    var showSuccessFlash by remember { mutableStateOf(false) }
    var isProcessingGalleryImage by remember { mutableStateOf(false) }
    var galleryScanFailed by remember { mutableStateOf(false) }

    // Reset scan state whenever this screen becomes active again (e.g. user backs out of the result screen)
    LaunchedEffect(isActive) {
        if (isActive) {
            hasScanned = false
            showSuccessFlash = false
        }
    }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        cameraPermanentlyDenied = !granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
    }

    // Fires the success flash, waits briefly so the user sees it, then navigates to the result.
    fun confirmScan(result: ScanResult) {
        hasScanned = true
        showSuccessFlash = true
        coroutineScope.launch {
            delay(if (reducedMotion) 120 else 450)
            onBarcodeScanned(result)
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && !hasScanned) {
            isProcessingGalleryImage = true
            galleryScanFailed = false
            coroutineScope.launch {
                val result = GalleryScanner.scanImage(context, uri)
                isProcessingGalleryImage = false
                if (result != null) {
                    confirmScan(result)
                } else {
                    galleryScanFailed = true
                    delay(2200)
                    galleryScanFailed = false
                }
            }
        }
    }

    fun openAppSettings() {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
        )
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Pick the permission up again if the user granted it from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = checkCameraPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Box(modifier = modifier.background(Color.Black)) {
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
                                    if (isActiveState && !hasScanned) {
                                        confirmScan(result)
                                    }
                                })
                            }

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
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

            // The preview runs behind the bottom nav; controls and the frame are laid out
            // in the area above it so everything is centred in what the user can actually see.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(bottom = BottomNavHeight)
            ) {
                ScanFrame(
                    success = showSuccessFlash,
                    reducedMotion = reducedMotion,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(ScanFrameSize)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScanHint()

                    AnimatedVisibility(
                        visible = galleryScanFailed,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "No QR code or barcode found in this image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                CircleIconButton(
                    icon = Icons.Filled.PhotoLibrary,
                    contentDescription = "Scan from gallery",
                    loading = isProcessingGalleryImage,
                    onClick = {
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 24.dp)
                )

                CircleIconButton(
                    icon = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = if (isFlashOn) "Turn flash off" else "Turn flash on",
                    active = isFlashOn,
                    onClick = {
                        isFlashOn = !isFlashOn
                        camera?.cameraControl?.enableTorch(isFlashOn)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 24.dp)
                )
            }
        } else {
            CameraPermissionPrompt(
                permanentlyDenied = cameraPermanentlyDenied,
                onAction = {
                    if (cameraPermanentlyDenied) {
                        openAppSettings()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        }
    }
}

@Composable
private fun ScanHint() {
    Surface(
        shape = CircleShape,
        color = ScanScrim,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Point at a QR code or barcode",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ScanFrame(
    success: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        ScanFrameCorners(success = success)

        AnimatedVisibility(
            visible = success,
            enter = if (reducedMotion) fadeIn() else scaleIn() + fadeIn(),
            exit = if (reducedMotion) fadeOut() else scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Scan successful",
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanFrameCorners(success: Boolean) {
    val color by animateColorAsState(
        targetValue = if (success) MaterialTheme.colorScheme.tertiary else Color.White,
        label = "scanFrameColor"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 4.dp.toPx()
        val arm = 36.dp.toPx()
        val radius = 20.dp.toPx()
        val left = strokeWidth / 2
        val top = strokeWidth / 2
        val right = size.width - strokeWidth / 2
        val bottom = size.height - strokeWidth / 2

        val corners = Path().apply {
            // Top-left
            moveTo(left, top + arm)
            lineTo(left, top + radius)
            arcTo(Rect(left, top, left + 2 * radius, top + 2 * radius), 180f, 90f, false)
            lineTo(left + arm, top)
            // Top-right
            moveTo(right - arm, top)
            lineTo(right - radius, top)
            arcTo(Rect(right - 2 * radius, top, right, top + 2 * radius), 270f, 90f, false)
            lineTo(right, top + arm)
            // Bottom-right
            moveTo(right, bottom - arm)
            lineTo(right, bottom - radius)
            arcTo(Rect(right - 2 * radius, bottom - 2 * radius, right, bottom), 0f, 90f, false)
            lineTo(right - arm, bottom)
            // Bottom-left
            moveTo(left + arm, bottom)
            lineTo(left + radius, bottom)
            arcTo(Rect(left, bottom - 2 * radius, left + 2 * radius, bottom), 90f, 90f, false)
            lineTo(left, bottom - arm)
        }

        drawPath(
            path = corners,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    active: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (active) colorScheme.primaryContainer else ScanScrim
    val contentColor = if (active) colorScheme.onPrimaryContainer else Color.White

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
            .size(56.dp)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionPrompt(
    permanentlyDenied: Boolean,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = BottomNavHeight)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Camera access needed",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Scann needs the camera to scan QR codes and barcodes.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAction,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (permanentlyDenied) "Open settings" else "Grant permission")
        }
    }
}

@Composable
private fun rememberReducedMotion(): Boolean {
    val view = LocalView.current
    return remember(view) {
        Settings.Global.getFloat(
            view.context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}
