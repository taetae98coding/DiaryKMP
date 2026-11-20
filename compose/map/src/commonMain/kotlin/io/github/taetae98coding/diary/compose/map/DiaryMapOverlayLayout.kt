package io.github.taetae98coding.diary.compose.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderSelector
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
internal fun DiaryMapOverlayLayout(
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    map: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) { map() }

        DiaryMapProviderSelector(
            state = state,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(DiaryTheme.dimens.componentSpacing),
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryMapOverlayLayoutPreview() {
    DiaryTheme {
        DiaryMapOverlayLayout(
            map = {},
        )
    }
}
