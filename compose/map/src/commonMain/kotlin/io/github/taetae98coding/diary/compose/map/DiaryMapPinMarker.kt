package io.github.taetae98coding.diary.compose.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

internal const val PIN_MARKER_PATH_DATA: String =
    "M12,2" +
        "c-4.2,0,-8,3.22,-8,8.2" +
        "c0,3.18,2.45,6.92,7.34,11.23" +
        "c0.38,0.33,0.95,0.33,1.33,0" +
        "C17.55,17.12,20,13.38,20,10.2" +
        "C20,5.22,16.2,2,12,2" +
        "Z" +
        "M12,12" +
        "c-1.1,0,-2,-0.9,-2,-2" +
        "c0,-1.1,0.9,-2,2,-2" +
        "c1.1,0,2,0.9,2,2" +
        "C14,11.1,13.1,12,12,12" +
        "Z"

internal const val PIN_MARKER_VIEWPORT_SIZE: Float = 24F
internal const val PIN_MARKER_SIZE_DP: Float = 32F
internal const val PIN_MARKER_LABEL_HEIGHT_DP: Float = 16F

internal val PinMarkerImageVector: ImageVector by lazy {
    ImageVector
        .Builder(
            defaultWidth = PIN_MARKER_SIZE_DP.dp,
            defaultHeight = PIN_MARKER_SIZE_DP.dp,
            viewportWidth = PIN_MARKER_VIEWPORT_SIZE,
            viewportHeight = PIN_MARKER_VIEWPORT_SIZE,
        ).addPath(
            pathData = addPathNodes(PIN_MARKER_PATH_DATA),
            fill = SolidColor(Color.Black),
        ).build()
}
