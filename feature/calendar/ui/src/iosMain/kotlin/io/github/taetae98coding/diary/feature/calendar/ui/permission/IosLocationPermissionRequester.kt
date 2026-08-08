package io.github.taetae98coding.diary.feature.calendar.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject
import kotlin.coroutines.resume

@Composable
internal actual fun rememberLocationPermissionRequester(): LocationPermissionRequester = remember { IosLocationPermissionRequester() }

private class IosLocationPermissionRequester : LocationPermissionRequester {
    // CLLocationManager는 델리게이트 콜백을 생성한 스레드의 런 루프로 전달하므로 메인 스레드에서 사용한다.
    override suspend fun request(): LocationPermissionRequestResult =
        withContext(Dispatchers.Main) {
            val locationManager = CLLocationManager()

            when (locationManager.authorizationStatus) {
                in AUTHORIZED_STATUS_LIST -> LocationPermissionRequestResult.ALREADY_GRANTED

                kCLAuthorizationStatusNotDetermined ->
                    suspendCancellableCoroutine { continuation ->
                        val delegate = AuthorizationDelegate(locationManager = locationManager, continuation = continuation)

                        delegate.request()
                        continuation.invokeOnCancellation { delegate.cancel() }
                    }

                else -> LocationPermissionRequestResult.DENIED
            }
        }

    private class AuthorizationDelegate(
        private val locationManager: CLLocationManager,
        private val continuation: CancellableContinuation<LocationPermissionRequestResult>,
    ) : NSObject(),
        CLLocationManagerDelegateProtocol {
        fun request() {
            locationManager.delegate = this
            locationManager.requestWhenInUseAuthorization()
        }

        fun cancel() {
            locationManager.delegate = null
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            if (manager.authorizationStatus == kCLAuthorizationStatusNotDetermined) return

            locationManager.delegate = null

            if (continuation.isActive) {
                val result =
                    if (manager.authorizationStatus in AUTHORIZED_STATUS_LIST) {
                        LocationPermissionRequestResult.GRANTED
                    } else {
                        LocationPermissionRequestResult.DENIED
                    }

                continuation.resume(result)
            }
        }
    }

    companion object {
        private val AUTHORIZED_STATUS_LIST =
            listOf(
                kCLAuthorizationStatusAuthorizedWhenInUse,
                kCLAuthorizationStatusAuthorizedAlways,
            )
    }
}
