package dev.fabik.bluetoothhid.ui

import android.content.pm.PackageManager
import android.view.MotionEvent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fabik.bluetoothhid.ui.model.CameraViewModel
import dev.fabik.bluetoothhid.utils.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun CameraPreview(
    viewModel: CameraViewModel = viewModel(),
    onCameraReady: (Camera) -> Unit,
    onBarCodeReady: (String) -> Unit
) = with(viewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val hasFrontCamera = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }

    val cameraResolution by rememberPreferenceNull(PrefKeys.SCAN_RESOLUTION)
    val frontCamera by rememberPreferenceDefault(PrefKeys.FRONT_CAMERA)
    val useRawValue by rememberPreferenceDefault(PrefKeys.RAW_VALUE)
    val fullyInside by rememberPreferenceDefault(PrefKeys.FULL_INSIDE)

    val scanFrequency by remember {
        context.getPreference(PrefKeys.SCAN_FREQUENCY).map {
            when (it) {
                0 -> 0
                1 -> 100
                3 -> 1000
                else -> 500
            }
        }
    }.collectAsState(500)

    val scanFormats by remember {
        context.getPreference(PrefKeys.CODE_TYPES).map {
            it.map { v -> 1 shl v.toInt() }.toIntArray()
        }
    }.collectAsState(intArrayOf(0))

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder()
                        .build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val barcodeAnalyser = BarCodeAnalyser(
                        scanDelay = scanFrequency,
                        formats = scanFormats,
                        onNothing = { currentBarCode = null }
                    ) { barcodes, source ->
                        if (sourceRes != source) {
                            updateScale(
                                source.width.toFloat(),
                                source.height.toFloat(),
                                previewView.width.toFloat(),
                                previewView.height.toFloat()
                            )
                            sourceRes = source
                        }

                        val filtered = barcodes.filter {
                            it.cornerPoints?.map { p ->
                                Offset(p.x * scale - transX, p.y * scale - transY)
                            }?.forEach { o ->
                                if (fullyInside) {
                                    if (!scanRect.contains(o)) {
                                        return@filter false
                                    }
                                } else {
                                    if (scanRect.contains(o)) {
                                        return@filter true
                                    }
                                }
                            }
                            fullyInside
                        }

                        filtered.firstOrNull().let { barcode ->
                            val value = if (useRawValue) {
                                barcode?.rawValue
                            } else {
                                barcode?.displayValue
                            }
                            value?.let { barcodeValue ->
                                if (lastBarCodeValue != barcodeValue) {
                                    onBarCodeReady(barcodeValue)
                                    lastBarCodeValue = barcodeValue
                                }
                            }
                            currentBarCode = barcode
                        }
                    }

                    val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder().setTargetResolution(
                        when (cameraResolution) {
                            2 -> CameraViewModel.FHD_1080P
                            1 -> CameraViewModel.HD_720P
                            else -> CameraViewModel.SD_480P
                        }
                    ).setOutputImageRotationEnabled(true)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                        .also {
                            it.setAnalyzer(executor, barcodeAnalyser)
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(
                            when {
                                frontCamera && hasFrontCamera -> CameraSelector.LENS_FACING_FRONT
                                else -> CameraSelector.LENS_FACING_BACK
                            }
                        )
                        .build()

                    val useCaseGroup = UseCaseGroup.Builder()
                        .setViewPort(previewView.viewPort!!)
                        .addUseCase(preview)
                        .addUseCase(imageAnalysis)
                        .build()

                    cameraProvider.unbindAll()

                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, useCaseGroup
                    )

                    onCameraReady(camera)

                    previewView.setOnTouchListener { view, event ->
                        return@setOnTouchListener when (event.action) {
                            MotionEvent.ACTION_DOWN -> true
                            MotionEvent.ACTION_UP -> {
                                if (focusTouchPoint == null) {
                                    focusTouchPoint = Offset(event.x, event.y)
                                    scope.launch {
                                        focusCircleAlpha.animateTo(1f, tween(100))
                                    }
                                    scope.launch {
                                        focusCircleRadius.snapTo(100f)
                                        focusCircleRadius.animateTo(
                                            80f, spring(Spring.DampingRatioMediumBouncy)
                                        )
                                    }
                                    val factory = DisplayOrientedMeteringPointFactory(
                                        previewView.display,
                                        camera.cameraInfo,
                                        previewView.width.toFloat(),
                                        previewView.height.toFloat()
                                    )
                                    val focusPoint = factory.createPoint(event.x, event.y)
                                    camera.cameraControl.startFocusAndMetering(
                                        FocusMeteringAction.Builder(
                                            focusPoint, FocusMeteringAction.FLAG_AF
                                        ).apply {
                                            disableAutoCancel()
                                        }.build()
                                    ).addListener({
                                        scope.launch {
                                            focusCircleAlpha.animateTo(0f, tween(100))
                                            focusTouchPoint = null
                                        }
                                    }, executor)
                                }
                                view.performClick()
                            }
                            else -> false
                        }
                    }
                }, executor
            )
            previewView
        },
        Modifier.fillMaxSize()
    )

    OverlayCanvas()
}

@Composable
fun CameraViewModel.OverlayCanvas() {
    val overlayType by rememberPreferenceNull(PrefKeys.OVERLAY_TYPE)
    val restrictArea by rememberPreferenceNull(PrefKeys.RESTRICT_AREA)

    Canvas(Modifier.fillMaxSize()) {
        val x = this.size.width / 2
        val y = this.size.height / 2
        val length = (x * 1.5f).coerceAtMost(y * 1.5f)
        val radius = 30f

        if (restrictArea == true) {
            scanRect = when (overlayType) {
                1 -> Rect(Offset(x - length / 2, y - length / 4), Size(length, length / 2))
                else -> Rect(Offset(x - length / 2, y - length / 2), Size(length, length))
            }

            val markerPath = Path().apply {
                addRoundRect(RoundRect(scanRect, CornerRadius(radius)))
            }

            clipPath(markerPath, clipOp = ClipOp.Difference) {
                drawRect(color = Color.Black, topLeft = Offset.Zero, size = size, alpha = 0.5f)
            }

            drawPath(markerPath, color = Color.White, style = Stroke(5f))
        } else {
            scanRect = Rect(Offset(0f, 0f), size)
        }

        currentBarCode?.let {
            it.cornerPoints?.forEach { p ->
                drawCircle(
                    color = Color.Red,
                    radius = 8f,
                    center = Offset(p.x * scale - transX, p.y * scale - transY)
                )
            }
        }

        focusTouchPoint?.let {
            drawCircle(
                color = Color.White,
                radius = focusCircleRadius.value,
                center = it,
                style = Stroke(5f),
                alpha = focusCircleAlpha.value
            )
        }
    }
}
