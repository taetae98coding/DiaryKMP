@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.compose.map.naver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMyPositionDisabled
import swiftPMImport.DiaryKmp.compose.compose.map.NMFMyPositionNormal
import swiftPMImport.DiaryKmp.compose.compose.map.NMFNaverMapView

@Composable
internal fun NaverMapMyLocationEffect(
    view: NMFNaverMapView? = null,
    isLocationPermissionGranted: Boolean = false,
) {
    LaunchedEffect(view, isLocationPermissionGranted) {
        if (view == null) return@LaunchedEffect

        // 위치 추적 모드를 켜야 현 위치 점이 표시되고, 현 위치 버튼도 그 뒤부터 지도를 옮길 수 있다.
        view.showLocationButton = isLocationPermissionGranted
        view.mapView.positionMode = if (isLocationPermissionGranted) NMFMyPositionNormal else NMFMyPositionDisabled
    }
}
