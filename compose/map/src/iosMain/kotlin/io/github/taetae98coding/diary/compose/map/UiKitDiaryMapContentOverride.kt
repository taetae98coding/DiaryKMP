package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.map.google.GoogleMap
import io.github.taetae98coding.diary.compose.map.naver.NaverMap
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import kotlinx.coroutines.CoroutineDispatcher

public actual val PlatformDiaryMapContentOverride: DiaryMapContentOverride = UnprovidedDiaryMapContentOverride

public class UiKitDiaryMapContentOverride(
    dispatcher: CoroutineDispatcher,
) : DiaryMapContentOverride {
    private val pinMarkerImageFactory = PinMarkerImageFactory(dispatcher = dispatcher)

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
                    pinMarkerImageFactory = pinMarkerImageFactory,
                    state = state,
                    modifier = modifier,
                    onSpotClick = onSpotClick,
                    onPinClick = onPinClick,
                )
        }
    }
}
