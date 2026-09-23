package io.github.taetae98coding.diary.compose.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTabRow

@Composable
internal fun DiaryMapCardLayout(
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    map: @Composable () -> Unit,
) {
    Card(modifier = modifier) {
        DiaryMapProviderTabRow(state = state)

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
        ) {
            map()
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryMapCardLayoutPreview() {
    DiaryTheme {
        DiaryMapCardLayout(
            map = {},
        )
    }
}
