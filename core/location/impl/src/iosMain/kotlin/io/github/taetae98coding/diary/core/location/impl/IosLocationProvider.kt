@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.core.location.impl

import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyKilometer
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

@Factory
internal class IosLocationProvider : LocationProvider {
    // CLLocationManager는 델리게이트 콜백을 생성한 스레드의 런 루프로 전달하므로 메인 스레드에서 사용한다.
    override suspend fun getCurrentLocation(): Location? =
        withContext(Dispatchers.Main) {
            val locationManager = CLLocationManager()

            locationManager.location?.let { lastLocation ->
                return@withContext lastLocation.coordinate.useContents { Location(latitude = latitude, longitude = longitude) }
            }

            suspendCancellableCoroutine<Location?> { continuation ->
                val delegate = LocationDelegate(locationManager = locationManager, continuation = continuation)

                delegate.request()
                continuation.invokeOnCancellation { delegate.cancel() }
            }
        }

    private class LocationDelegate(
        private val locationManager: CLLocationManager,
        private val continuation: CancellableContinuation<Location?>,
    ) : NSObject(),
        CLLocationManagerDelegateProtocol {
        fun request() {
            locationManager.delegate = this
            locationManager.desiredAccuracy = kCLLocationAccuracyKilometer
            locationManager.requestLocation()
        }

        fun cancel() {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
        }

        override fun locationManager(
            manager: CLLocationManager,
            didUpdateLocations: List<*>,
        ) {
            val location =
                (didUpdateLocations.lastOrNull() as? CLLocation)
                    ?.coordinate
                    ?.useContents { Location(latitude = latitude, longitude = longitude) }

            resume(location)
        }

        override fun locationManager(
            manager: CLLocationManager,
            didFailWithError: NSError,
        ) {
            resume(null)
        }

        private fun resume(location: Location?) {
            locationManager.delegate = null

            if (continuation.isActive) {
                continuation.resume(location)
            }
        }
    }
}
