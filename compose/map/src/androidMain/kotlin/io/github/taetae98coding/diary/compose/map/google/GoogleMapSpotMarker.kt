package io.github.taetae98coding.diary.compose.map.google

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate

@Composable
@GoogleMapComposable
internal fun GoogleMapSpotMarker(spot: DiaryMapCoordinate) {
    Marker(state = rememberUpdatedMarkerState(position = LatLng(spot.latitude, spot.longitude)))
}
