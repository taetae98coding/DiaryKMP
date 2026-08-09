package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.toArgb
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.MarkerIcons
import kotlin.uuid.Uuid
import com.naver.maps.map.NaverMap as NaverMapClient

@Composable
internal fun NaverMapPinMarkersEffect(
    client: NaverMapClient? = null,
    state: DiaryMapState = rememberDiaryMapState(),
    onPinClick: ((Uuid) -> Unit)? = null,
) {
    val markers = remember { mutableListOf<Marker>() }

    DisposableEffect(markers) {
        onDispose {
            markers.forEach { marker -> marker.map = null }
        }
    }

    LaunchedEffect(client, markers, state, onPinClick) {
        snapshotFlow { state.pins }
            .collect { pins ->
                markers.forEach { marker -> marker.map = null }
                markers.clear()

                if (client == null) return@collect

                pins
                    .filter { pin -> pin.isFinite }
                    .forEach { pin ->
                        markers +=
                            Marker().apply {
                                position = LatLng(pin.coordinate.latitude, pin.coordinate.longitude)
                                icon = MarkerIcons.BLACK
                                iconTintColor = pin.color.toArgb()
                                captionText = pin.label
                                if (onPinClick != null) {
                                    onClickListener =
                                        Overlay.OnClickListener {
                                            onPinClick(pin.id)
                                            true
                                        }
                                }
                                map = client
                            }
                    }
            }
    }
}
