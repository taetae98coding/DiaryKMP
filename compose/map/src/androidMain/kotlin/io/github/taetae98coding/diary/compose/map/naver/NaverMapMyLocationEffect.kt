package io.github.taetae98coding.diary.compose.map.naver

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.naver.maps.map.LocationSource
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.NaverMap as NaverMapClient

@Composable
internal fun NaverMapMyLocationEffect(
    client: NaverMapClient? = null,
    isLocationPermissionGranted: Boolean = false,
) {
    val activity = LocalActivity.current
    val locationSource: LocationSource? =
        remember(activity) {
            activity?.let { FusedLocationSource(it, LOCATION_PERMISSION_REQUEST_CODE) }
        }

    DisposableEffect(client, locationSource, isLocationPermissionGranted) {
        if (client == null) return@DisposableEffect onDispose { }

        // 위치 소스는 권한이 허용된 뒤에만 붙인다. 권한이 없는 상태로 붙이면 지도가 스스로 권한을 요청한다.
        if (isLocationPermissionGranted && locationSource != null) {
            client.locationSource = locationSource
            client.locationTrackingMode = LocationTrackingMode.NoFollow
            client.uiSettings.isLocationButtonEnabled = true
        } else {
            client.locationTrackingMode = LocationTrackingMode.None
            client.locationSource = null
            client.uiSettings.isLocationButtonEnabled = false
        }

        onDispose {
            client.locationTrackingMode = LocationTrackingMode.None
            client.locationSource = null
        }
    }
}

// FusedLocationSource가 권한 요청 결과를 구분할 때만 쓰는 값으로, 이 앱은 권한이 허용된 뒤에만 위치 소스를 붙여 요청을 만들지 않는다.
private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
