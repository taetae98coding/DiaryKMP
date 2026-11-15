package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.map.google.GoogleMap
import io.github.taetae98coding.diary.compose.map.naver.NaverMap
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.map.web.MapHttpServer

public actual val PlatformDiaryMapContentOverride: DiaryMapContentOverride = UnprovidedDiaryMapContentOverride

public class JvmDiaryMapContentOverride(
    private val httpServer: MapHttpServer,
) : DiaryMapContentOverride {
    @Composable
    override fun DiaryMapContentOverrideScope.MapContent() {
        when (state.provider) {
            DiaryMapProvider.NAVER ->
                NaverMap(
                    httpServer = httpServer,
                    state = state,
                    modifier = modifier,
                    onSpotClick = onSpotClick,
                    onPinClick = onPinClick,
                )

            DiaryMapProvider.GOOGLE ->
                GoogleMap(
                    httpServer = httpServer,
                    state = state,
                    modifier = modifier,
                    onSpotClick = onSpotClick,
                    onPinClick = onPinClick,
                )
        }
    }
}
