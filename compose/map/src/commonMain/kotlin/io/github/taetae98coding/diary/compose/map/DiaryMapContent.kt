package io.github.taetae98coding.diary.compose.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.google.GoogleMap
import io.github.taetae98coding.diary.compose.map.naver.NaverMap
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import kotlin.uuid.Uuid

@Composable
internal fun DiaryMapContent(
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
    onPinClick: ((Uuid) -> Unit)? = null,
) {
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

@ScreenPreview
@Composable
private fun DiaryMapContentPreview() {
    DiaryTheme {
        DiaryMapContent(modifier = Modifier.fillMaxSize())
    }
}
