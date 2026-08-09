@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.core.location.impl

import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Factory
import kotlin.coroutines.resume

@Factory
internal class WasmLocationProvider : LocationProvider {
    override suspend fun getCurrentLocation(): Location? {
        if (!isPermissionGranted()) return null

        return suspendCancellableCoroutine { continuation ->
            requestCurrentPosition(
                onSuccess = { latitude, longitude -> continuation.resume(Location(latitude = latitude, longitude = longitude)) },
                onFailure = { continuation.resume(null) },
            )
        }
    }

    private suspend fun isPermissionGranted(): Boolean =
        suspendCancellableCoroutine { continuation ->
            queryPermissionState(
                onState = { state -> continuation.resume(state == GRANTED_STATE) },
                onUnavailable = { continuation.resume(false) },
            )
        }

    companion object {
        private const val GRANTED_STATE = "granted"
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
private fun requestCurrentPosition(
    onSuccess: (Double, Double) -> Unit,
    onFailure: () -> Unit,
): Unit =
    js(
        """
        (() => {
            navigator.geolocation.getCurrentPosition(
                function (position) { onSuccess(position.coords.latitude, position.coords.longitude); },
                function () { onFailure(); },
                { enableHighAccuracy: false, maximumAge: Infinity, timeout: 5000 }
            );
        })()
        """,
    )
