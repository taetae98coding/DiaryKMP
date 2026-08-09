package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
@GoogleMapComposable
internal fun GoogleMapSpotMarker(spot: DiaryMapCoordinate) {
    Marker(state = rememberUpdatedMarkerState(position = LatLng(spot.latitude, spot.longitude)))
}
