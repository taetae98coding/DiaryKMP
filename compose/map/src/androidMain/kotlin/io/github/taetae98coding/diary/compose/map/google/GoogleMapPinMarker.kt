package io.github.taetae98coding.diary.compose.map.google

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberUpdatedMarkerState
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.PIN_MARKER_LABEL_HEIGHT_DP
import io.github.taetae98coding.diary.compose.map.PIN_MARKER_SIZE_DP
import io.github.taetae98coding.diary.compose.map.PinMarkerImageVector
import io.github.taetae98coding.diary.compose.map.provider.label
import kotlin.uuid.Uuid

private const val PIN_ANCHOR_X = 0.5F
private const val PIN_ICON_ONLY_ANCHOR_Y = 1F

@Composable
@GoogleMapComposable
internal fun GoogleMapPinMarker(
    pin: DiaryMapPin,
    onPinClick: ((Uuid) -> Unit)? = null,
) {
    MarkerComposable(
        pin,
        state =
            rememberUpdatedMarkerState(
                position = LatLng(pin.coordinate.latitude, pin.coordinate.longitude),
            ),
        anchor =
            if (pin.label.isEmpty()) {
                Offset(PIN_ANCHOR_X, PIN_ICON_ONLY_ANCHOR_Y)
            } else {
                Offset(PIN_ANCHOR_X, PIN_MARKER_SIZE_DP / (PIN_MARKER_SIZE_DP + PIN_MARKER_LABEL_HEIGHT_DP))
            },
        onClick =
            remember(pin.id, onPinClick) {
                { _ ->
                    onPinClick?.invoke(pin.id)
                    true
                }
            },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = PinMarkerImageVector,
                contentDescription = pin.label,
                modifier = Modifier.size(PIN_MARKER_SIZE_DP.dp),
                tint = pin.color,
            )
            if (pin.label.isNotEmpty()) {
                Text(
                    text = pin.label,
                    modifier =
                        Modifier
                            .height(PIN_MARKER_LABEL_HEIGHT_DP.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.8F),
                                shape = RoundedCornerShape(2.dp),
                            ).padding(horizontal = 2.dp),
                    color = Color.Black,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 1,
                )
            }
        }
    }
}
