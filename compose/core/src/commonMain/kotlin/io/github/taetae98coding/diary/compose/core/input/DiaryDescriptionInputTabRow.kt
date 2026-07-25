package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.gestures.animateTo
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.coroutines.launch

@Composable
internal fun DiaryDescriptionInputTabRow(
    modifier: Modifier = Modifier,
    state: DiaryDescriptionInputState = rememberDiaryDescriptionInputState(),
) {
    val scope = rememberCoroutineScope()

    PrimaryTabRow(
        selectedTabIndex = diaryDescriptionInputPageList.indexOf(state.swipeState.targetValue),
        modifier = modifier,
        containerColor = Color.Transparent,
    ) {
        diaryDescriptionInputPageList.forEach { page ->
            Tab(
                selected = state.swipeState.targetValue == page,
                onClick = { scope.launch { state.swipeState.animateTo(page) } },
                icon = { DiaryDescriptionInputPageIcon(page = page) },
            )
        }
    }
}

@ComponentPreview
@Composable
private fun DiaryDescriptionInputTabRowPreview() {
    DiaryTheme {
        Surface {
            DiaryDescriptionInputTabRow()
        }
    }
}
