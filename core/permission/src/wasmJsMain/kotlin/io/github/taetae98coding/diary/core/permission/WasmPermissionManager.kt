@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.permission

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

public class WasmPermissionManager : PermissionManager() {
    override suspend fun readIsGranted(permission: Permission): Boolean = permissionState(permission) == GRANTED_STATE

    override suspend fun requestPermission(permission: Permission): PermissionResult {
        if (permissionState(permission) != PROMPT_STATE) return PermissionResult.DENIED

        return when (permission) {
            Permission.NOTIFICATION -> requestNotificationPermission()
            Permission.LOCATION -> requestLocationPermission()
        }
    }

    private suspend fun permissionState(permission: Permission): String =
        when (permission) {
            Permission.NOTIFICATION -> notificationPermissionState()
            Permission.LOCATION -> queryLocationPermissionState()
        }

    private suspend fun requestNotificationPermission(): PermissionResult {
        val state =
            suspendCancellableCoroutine { continuation ->
                requestNotificationPermission(
                    onState = { state -> continuation.resume(state) },
                    onUnavailable = { continuation.resume(UNAVAILABLE_STATE) },
                )
            }

        return if (state == GRANTED_STATE) PermissionResult.GRANTED else PermissionResult.DENIED
    }

    // 브라우저는 위치 권한을 요청하는 API를 따로 제공하지 않으므로 위치 조회로 요청을 대신한다.
    private suspend fun requestLocationPermission(): PermissionResult {
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
        // 브라우저는 알림 권한의 미결정 상태를 default로, 위치 권한의 미결정 상태를 prompt로 알려 주므로 알림 쪽을 prompt로 맞춰 읽는다.
        private const val GRANTED_STATE = "granted"
        private const val PROMPT_STATE = "prompt"
        private const val UNAVAILABLE_STATE = ""
    }
}

private fun notificationPermissionState(): String = js("(typeof Notification === 'undefined' ? '' : Notification.permission === 'default' ? 'prompt' : Notification.permission)")

@Suppress("UnusedParameter")
private fun requestNotificationPermission(
    onState: (String) -> Unit,
    onUnavailable: () -> Unit,
): Unit =
    js(
        """
        (() => {
            if (typeof Notification === 'undefined' || !Notification.requestPermission) {
                onUnavailable();
                return;
            }
            const request = Notification.requestPermission();
            if (!request || typeof request.then !== 'function') {
                onUnavailable();
                return;
            }
            request.then(
                function (permission) { onState(permission); },
                function () { onUnavailable(); }
            );
        })()
        """,
    )

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
