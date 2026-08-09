package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.naver.maps.map.NaverMap as NaverMapClient

@Composable
internal fun NaverMapSelectSpotEffect(
    client: NaverMapClient? = null,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
) {
    DisposableEffect(client, onSpotClick) {
        if (client == null || onSpotClick == null) return@DisposableEffect onDispose {}

        client.setOnMapClickListener { _, latLng ->
            onSpotClick(DiaryMapCoordinate(latitude = latLng.latitude, longitude = latLng.longitude))
        }

        onDispose {
            client.onMapClickListener = null
        }
    }
}
