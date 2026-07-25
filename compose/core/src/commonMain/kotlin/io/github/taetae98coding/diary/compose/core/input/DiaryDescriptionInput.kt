package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryDescriptionInput(
    modifier: Modifier = Modifier,
    state: DiaryDescriptionInputState = rememberDiaryDescriptionInputState(),
) {
    Card(modifier = modifier) {
        DiaryDescriptionInputTabRow(state = state)
        DiaryDescriptionInputPageLayout(state = state)
    }
}

@ComponentPreview
@Composable
private fun DiaryDescriptionInputPreview() {
    DiaryTheme {
        DiaryDescriptionInput()
    }
}
