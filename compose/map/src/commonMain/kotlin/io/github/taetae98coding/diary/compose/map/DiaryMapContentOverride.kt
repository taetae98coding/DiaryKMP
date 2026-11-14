package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import kotlin.uuid.Uuid

public interface DiaryMapContentOverride {
    @Composable
    public fun DiaryMapContentOverrideScope.MapContent()
}

public class DiaryMapContentOverrideScope internal constructor(
    public val state: DiaryMapState,
    public val modifier: Modifier,
    public val onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    public val onPinClick: ((Uuid) -> Unit)?,
) {
    // 지도 구현은 처음 그릴 때 보여 줄 위치와 확대 수준을 알아야 한다.
    public val zoom: Double? get() = state.camera?.zoom

    // 지도가 보고 있는 위치는 지도 구현만 알 수 있으므로, 지도가 움직일 때마다 그 결과를 상태에 알린다.
    public fun reportCamera(
        coordinate: DiaryMapCoordinate,
        zoom: Double,
        bounds: DiaryMapBounds? = null,
    ) {
        state.moveCamera(
            DiaryMapCamera(
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
                zoom = zoom,
                bounds = bounds,
            ),
        )
    }
}

public expect val PlatformDiaryMapContentOverride: DiaryMapContentOverride

internal object UnprovidedDiaryMapContentOverride : DiaryMapContentOverride {
    @Composable
    override fun DiaryMapContentOverrideScope.MapContent() {
        error("LocalDiaryMapContentOverride에 플랫폼 지도 구현을 제공해야 한다.")
    }
}

public val LocalDiaryMapContentOverride: ProvidableCompositionLocal<DiaryMapContentOverride> =
    staticCompositionLocalOf { PlatformDiaryMapContentOverride }
