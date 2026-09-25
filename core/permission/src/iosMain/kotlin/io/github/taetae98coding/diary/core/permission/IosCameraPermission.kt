package io.github.taetae98coding.diary.core.permission

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import kotlin.coroutines.resume

internal fun isCameraPermissionGranted(): Boolean = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized

// 이미 결정된 뒤에는 시스템이 요청을 표시하지 않으므로, 결정되지 않았을 때만 요청한다.
internal suspend fun requestCameraPermission(): PermissionResult {
    if (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) != AVAuthorizationStatusNotDetermined) {
        return PermissionResult.DENIED
    }

    val isGranted =
        suspendCancellableCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { isGranted ->
                if (continuation.isActive) continuation.resume(isGranted)
            }
        }

    return if (isGranted) PermissionResult.GRANTED else PermissionResult.DENIED
}
