@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.app.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
internal actual fun rememberNotificationPermissionRequester(): NotificationPermissionRequester = remember { WasmNotificationPermissionRequester() }

internal class WasmNotificationPermissionRequester : NotificationPermissionRequester {
    override suspend fun request(): NotificationPermissionRequestResult =
        when (notificationPermissionState()) {
            GRANTED_STATE -> NotificationPermissionRequestResult.GRANTED
            DEFAULT_STATE -> requestPermission()
            else -> NotificationPermissionRequestResult.DENIED
        }

    private suspend fun requestPermission(): NotificationPermissionRequestResult {
        val state =
            suspendCancellableCoroutine { continuation ->
                requestNotificationPermission(
                    onState = { state -> continuation.resume(state) },
                    onUnavailable = { continuation.resume(UNAVAILABLE_STATE) },
                )
            }

        return if (state == GRANTED_STATE) {
            NotificationPermissionRequestResult.GRANTED
        } else {
            NotificationPermissionRequestResult.DENIED
        }
    }

    companion object {
        private const val GRANTED_STATE = "granted"
        private const val DEFAULT_STATE = "default"
        private const val UNAVAILABLE_STATE = ""
    }
}

private fun notificationPermissionState(): String = js("(typeof Notification === 'undefined' ? '' : Notification.permission)")

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
