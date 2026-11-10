package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.map.google.GoogleMap
import io.github.taetae98coding.diary.compose.map.naver.NaverMap
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider

public actual val PlatformDiaryMapContentOverride: DiaryMapContentOverride = WasmDiaryMapContentOverride

private object WasmDiaryMapContentOverride : DiaryMapContentOverride {
    @Composable
    override fun DiaryMapContentOverrideScope.MapContent() {
        when (state.provider) {
            DiaryMapProvider.NAVER ->
                NaverMap(
                    state = state,
                    modifier = modifier,
                    onSpotClick = onSpotClick,
                    onPinClick = onPinClick,
                )

            DiaryMapProvider.GOOGLE ->
                GoogleMap(
                    state = state,
                    modifier = modifier,
                    onSpotClick = onSpotClick,
                    onPinClick = onPinClick,
                )
        }
    }
}
