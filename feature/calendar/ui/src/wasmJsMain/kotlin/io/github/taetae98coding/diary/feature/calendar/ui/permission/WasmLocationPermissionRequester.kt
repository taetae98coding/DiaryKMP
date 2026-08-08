@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.feature.calendar.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
internal actual fun rememberLocationPermissionRequester(): LocationPermissionRequester = remember { WasmLocationPermissionRequester() }

internal class WasmLocationPermissionRequester : LocationPermissionRequester {
    override suspend fun request(): LocationPermissionRequestResult =
        when (permissionState()) {
            GRANTED_STATE -> LocationPermissionRequestResult.ALREADY_GRANTED
            PROMPT_STATE -> requestPermission()
            else -> LocationPermissionRequestResult.DENIED
        }

    private suspend fun requestPermission(): LocationPermissionRequestResult {
        val isPositionReceived =
            suspendCancellableCoroutine { continuation ->
                requestPosition(
                    onSuccess = { continuation.resume(true) },
                    onFailure = { continuation.resume(false) },
                )
            }

        return when {
            isPositionReceived -> LocationPermissionRequestResult.GRANTED
            permissionState() == GRANTED_STATE -> LocationPermissionRequestResult.GRANTED
            else -> LocationPermissionRequestResult.DENIED
        }
    }

    private suspend fun permissionState(): String =
        suspendCancellableCoroutine { continuation ->
            queryPermissionState(
                onState = { state -> continuation.resume(state) },
                onUnavailable = { continuation.resume(UNAVAILABLE_STATE) },
            )
        }

    companion object {
        private const val GRANTED_STATE = "granted"
        private const val PROMPT_STATE = "prompt"
        private const val UNAVAILABLE_STATE = ""
    }
}

@Suppress("UnusedParameter")
private fun queryPermissionState(
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
