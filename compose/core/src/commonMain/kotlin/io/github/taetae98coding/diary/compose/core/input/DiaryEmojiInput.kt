package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.diary_emoji_input_label
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource

public const val DIARY_EMOJI_INPUT_TEST_TAG: String = "DiaryEmojiInput"

@Composable
public fun DiaryEmojiInput(
    modifier: Modifier = Modifier,
    state: DiaryEmojiInputState = rememberDiaryEmojiInputState(),
) {
    val dialogState = rememberDialogState()

    Card(
        onClick = dialogState::show,
        modifier = modifier.testTag(DIARY_EMOJI_INPUT_TEST_TAG),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (state.text.isEmpty()) {
                Text(
                    text = stringResource(Res.string.diary_emoji_input_label),
                    textAlign = TextAlign.Center,
                    style = DiaryTheme.typography.labelMedium,
                )
            } else {
                Text(
                    text = state.text,
                    textAlign = TextAlign.Center,
                    style = DiaryTheme.typography.headlineSmall,
                )
            }
        }
    }

    DiaryEmojiInputDialogHost(
        dialogState = dialogState,
        state = state,
    )
}

@ComponentPreview
@Composable
private fun DiaryEmojiInputPreview(
    @PreviewParameter(BooleanPreviewParameter::class) hasEmoji: Boolean,
) {
    DiaryTheme {
        Surface {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                DiaryEmojiInput(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .aspectRatio(ratio = 1F, matchHeightConstraintsFirst = true),
                    state = rememberDiaryEmojiInputState(initialText = if (hasEmoji) "🏃" else ""),
                )
                DiaryTitleInput()
            }
        }
    }
}
