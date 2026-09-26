@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.permission

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// 웹은 알림을 보내는 기능이 없으므로 알림 권한은 요청하지 않고 거부로 다룬다.
public class WasmPermissionManager : PermissionManager() {
    override suspend fun readIsGranted(permission: Permission): Boolean =
        when (permission) {
            Permission.NOTIFICATION -> false
            Permission.CAMERA -> queryCameraPermissionState() == GRANTED_STATE
            Permission.LOCATION -> queryLocationPermissionState() == GRANTED_STATE
        }

    override suspend fun requestPermission(permission: Permission): PermissionResult =
        when (permission) {
            Permission.NOTIFICATION -> PermissionResult.DENIED
            Permission.CAMERA -> requestCameraPermission()
            Permission.LOCATION -> requestLocationPermission()
        }

    // 브라우저는 카메라 권한을 요청하는 API를 따로 제공하지 않으므로 카메라를 잠깐 열었다 닫아 요청을 대신한다.
    // 권한 상태를 조회하지 못하는 브라우저가 있어 거부가 확인된 경우에만 요청을 건너뛴다.
    private suspend fun requestCameraPermission(): PermissionResult {
        if (queryCameraPermissionState() == DENIED_STATE) return PermissionResult.DENIED

        val isCameraOpened =
            suspendCancellableCoroutine { continuation ->
                openAndCloseCamera(
                    onSuccess = { continuation.resume(true) },
                    onFailure = { continuation.resume(false) },
                )
            }

        return if (isCameraOpened) PermissionResult.GRANTED else PermissionResult.DENIED
    }

    private suspend fun queryCameraPermissionState(): String =
        suspendCancellableCoroutine { continuation ->
            queryCameraPermissionState(
                onState = { state -> continuation.resume(state) },
                onUnavailable = { continuation.resume(UNAVAILABLE_STATE) },
            )
        }

    // 브라우저는 위치 권한을 요청하는 API를 따로 제공하지 않으므로 위치 조회로 요청을 대신한다.
    private suspend fun requestLocationPermission(): PermissionResult {
        if (queryLocationPermissionState() != PROMPT_STATE) return PermissionResult.DENIED

        val isPositionReceived =
            suspendCancellableCoroutine { continuation ->
                requestPosition(
                    onSuccess = { continuation.resume(true) },
                    onFailure = { continuation.resume(false) },
                )
            }

        return when {
            isPositionReceived -> PermissionResult.GRANTED
            queryLocationPermissionState() == GRANTED_STATE -> PermissionResult.GRANTED
            else -> PermissionResult.DENIED
        }
    }

    private suspend fun queryLocationPermissionState(): String =
        suspendCancellableCoroutine { continuation ->
            queryLocationPermissionState(
                onState = { state -> continuation.resume(state) },
                onUnavailable = { continuation.resume(UNAVAILABLE_STATE) },
            )
        }

    private companion object {
        private const val GRANTED_STATE = "granted"
        private const val PROMPT_STATE = "prompt"
        private const val DENIED_STATE = "denied"
        private const val UNAVAILABLE_STATE = ""
    }
}

@Suppress("UnusedParameter")
private fun queryLocationPermissionState(
    onState: (String) -> Unit,
    onUnavailable: () -> Unit,
): Unit =
    js(
        """
        (() => {
            if (!navigator.geolocation || !navigator.permissions || !navigator.permissions.query) {
                onUnavailable();
                return;
            }
            navigator.permissions.query({ name: 'geolocation' }).then(
                function (status) { onState(status.state); },
                function () { onUnavailable(); }
            );
        })()
        """,
    )

@Suppress("UnusedParameter")
private fun requestPosition(
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
): Unit =
    js(
        """
        (() => {
            navigator.geolocation.getCurrentPosition(
                function () { onSuccess(); },
                function () { onFailure(); },
                { enableHighAccuracy: false, maximumAge: Infinity }
            );
        })()
        """,
    )

@Suppress("UnusedParameter")
private fun queryCameraPermissionState(
    onState: (String) -> Unit,
    onUnavailable: () -> Unit,
): Unit =
    js(
        """
        (() => {
            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia || !navigator.permissions || !navigator.permissions.query) {
                onUnavailable();
                return;
            }
            navigator.permissions.query({ name: 'camera' }).then(
                function (status) { onState(status.state); },
                function () { onUnavailable(); }
            );
        })()
        """,
    )

@Suppress("UnusedParameter")
private fun openAndCloseCamera(
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
): Unit =
    js(
        """
        (() => {
            if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                onFailure();
                return;
            }
            navigator.mediaDevices.getUserMedia({ video: true }).then(
                function (stream) {
                    stream.getTracks().forEach(function (track) { track.stop(); });
                    onSuccess();
                },
                function () { onFailure(); }
            );
        })()
        """,
    )
