package io.github.taetae98coding.diary.compose.map.naver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.isFinite
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import com.naver.maps.map.NaverMap as NaverMapClient

@Composable
internal fun NaverMapSpotMarkerEffect(
    client: NaverMapClient? = null,
    state: DiaryMapState = rememberDiaryMapState(),
) {
    val marker = remember { Marker() }

    DisposableEffect(marker) {
        onDispose {
            marker.map = null
        }
    }

    LaunchedEffect(client, marker, state) {
        snapshotFlow { state.spot }
            .collect { spot ->
                if (client == null || spot == null || !spot.isFinite) {
                    marker.map = null
                    return@collect
                }

                marker.position = LatLng(spot.latitude, spot.longitude)
                marker.map = client
            }
    }
}
