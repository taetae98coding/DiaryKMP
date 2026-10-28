package io.github.taetae98coding.diary.feature.tag.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInput
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryEmojiInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun TagForm(
    modifier: Modifier = Modifier,
    state: TagFormState = rememberTagAddFormState(),
    content: @Composable ColumnScope.() -> Unit = {},
) {
    DiaryInputColumn(modifier = modifier) {
        TitleRow(state = state, modifier = Modifier.fillMaxWidth())
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryColorInput(
            state = state.colorState,
            modifier = Modifier.fillMaxWidth(),
        )
        content()
    }
}

@Composable
private fun TitleRow(
    modifier: Modifier = Modifier,
    state: TagFormState = rememberTagAddFormState(),
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
    ) {
        DiaryEmojiInput(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(ratio = 1F, matchHeightConstraintsFirst = true),
            state = state.emojiState,
        )
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.weight(1F),
        )
    }
}

@ScreenPreview
@Composable
private fun TagFormPreview() {
    DiaryTheme {
        Surface {
            TagForm()
        }
    }
}
