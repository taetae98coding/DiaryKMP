package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.InitializationException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.awaitCancellation

@Composable
internal actual fun QrScanCameraPreview(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

    // 카메라를 생명주기에 묶으면 화면이 멈출 때 카메라도 멈추고 다시 앞에 나오면 이어서 보여 준다.
    LaunchedEffect(context, lifecycleOwner) {
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider { request -> surfaceRequest = request }

        val cameraProvider =
            try {
                ProcessCameraProvider.awaitInstance(context)
            } catch (_: InitializationException) {
                return@LaunchedEffect
            }

        try {
            if (!cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) return@LaunchedEffect

            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            awaitCancellation()
        } catch (_: CameraInfoUnavailableException) {
        } catch (_: IllegalStateException) {
        } catch (_: IllegalArgumentException) {
        } finally {
            cameraProvider.unbind(preview)
        }
    }

    Box(modifier = modifier.background(color = QrScanCameraPreviewDefaults.EmptyColor)) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
