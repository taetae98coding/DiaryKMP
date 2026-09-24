@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.permission

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// 웹은 알림을 보내는 기능이 없으므로 알림 권한은 요청하지 않고 거부로 다룬다.
public class WasmPermissionManager : PermissionManager() {
    override suspend fun readIsGranted(permission: Permission): Boolean =
        when (permission) {
            Permission.NOTIFICATION -> false
            Permission.LOCATION -> queryLocationPermissionState() == GRANTED_STATE
        }

    override suspend fun requestPermission(permission: Permission): PermissionResult =
        when (permission) {
            Permission.NOTIFICATION -> PermissionResult.DENIED
            Permission.LOCATION -> requestLocationPermission()
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
