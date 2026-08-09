package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.naver.maps.map.NaverMap as NaverMapClient

@Composable
internal fun NaverMapShareCameraEffect(
    client: NaverMapClient? = null,
    state: DiaryMapState = rememberDiaryMapState(),
) {
    DisposableEffect(client, state) {
        if (client == null) return@DisposableEffect onDispose {}

        val listener =
            NaverMapClient.OnCameraIdleListener {
                val position = client.cameraPosition
                val contentBounds = client.contentBounds

                state.moveCamera(
                    DiaryMapCamera(
                        latitude = position.target.latitude,
                        longitude = position.target.longitude,
                        zoom = position.zoom,
                        bounds =
                            DiaryMapBounds(
                                south = contentBounds.southLatitude,
                                north = contentBounds.northLatitude,
                                west = contentBounds.westLongitude,
                                east = contentBounds.eastLongitude,
                            ),
                    ),
                )
            }

        client.addOnCameraIdleListener(listener)

        onDispose {
            client.removeOnCameraIdleListener(listener)
        }
    }
}
