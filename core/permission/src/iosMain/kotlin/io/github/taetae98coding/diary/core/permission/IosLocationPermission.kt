package io.github.taetae98coding.diary.core.permission

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

// CLLocationManager는 델리게이트 콜백을 생성한 스레드의 런 루프로 전달하므로 메인 스레드에서 사용한다.
internal suspend fun isLocationPermissionGranted(): Boolean = withContext(Dispatchers.Main) { CLLocationManager().isLocationAuthorized() }

internal suspend fun requestLocationPermission(): PermissionResult =
    withContext(Dispatchers.Main) {
        val locationManager = CLLocationManager()

        // 이미 결정된 뒤에는 requestWhenInUseAuthorization이 아무 동작도 하지 않고 델리게이트도 호출되지 않는다.
        if (locationManager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
            suspendCancellableCoroutine { continuation ->
                val delegate = AuthorizationDelegate(locationManager = locationManager, continuation = continuation)

                delegate.request()
                continuation.invokeOnCancellation { delegate.cancel() }
            }
        } else {
            PermissionResult.DENIED
        }
    }

internal fun CLLocationManager.isLocationAuthorized(): Boolean = authorizationStatus in AUTHORIZED_LOCATION_STATUS_LIST

private class AuthorizationDelegate(
    private val locationManager: CLLocationManager,
    private val continuation: CancellableContinuation<PermissionResult>,
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
            val result = if (manager.isLocationAuthorized()) PermissionResult.GRANTED else PermissionResult.DENIED

            continuation.resume(result)
        }
    }
}

private val AUTHORIZED_LOCATION_STATUS_LIST =
    listOf(
        kCLAuthorizationStatusAuthorizedWhenInUse,
        kCLAuthorizationStatusAuthorizedAlways,
    )
