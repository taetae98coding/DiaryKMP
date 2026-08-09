package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.darwin.NSObject

@Composable
internal fun rememberIsLocationPermissionGranted(): Boolean {
    // CLLocationManager를 만드는 것만으로는 권한을 요청하지 않으므로 현재 권한을 읽는 용도로만 쓴다.
    val locationManager = remember { CLLocationManager() }
    var isGranted by remember(locationManager) { mutableStateOf(locationManager.isLocationAuthorized()) }

    DisposableEffect(locationManager) {
        val delegate = LocationAuthorizationDelegate { manager -> isGranted = manager.isLocationAuthorized() }

        locationManager.delegate = delegate

        onDispose { locationManager.delegate = null }
    }

    return isGranted
}

private class LocationAuthorizationDelegate(
    private val onChange: (CLLocationManager) -> Unit,
) : NSObject(),
    CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onChange(manager)
    }
}

private fun CLLocationManager.isLocationAuthorized(): Boolean = authorizationStatus in AUTHORIZED_STATUS_LIST

private val AUTHORIZED_STATUS_LIST =
    listOf(
        kCLAuthorizationStatusAuthorizedWhenInUse,
        kCLAuthorizationStatusAuthorizedAlways,
    )
