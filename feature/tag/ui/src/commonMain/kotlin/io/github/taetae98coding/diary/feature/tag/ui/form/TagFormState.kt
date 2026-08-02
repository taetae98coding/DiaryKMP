package io.github.taetae98coding.diary.feature.tag.ui.form

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryEmojiInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryColorInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryEmojiInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColorLong

@Stable
internal class TagFormState(
    val emojiState: DiaryEmojiInputState,
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val colorState: DiaryColorInputState,
    val hostState: SnackbarHostState,
    val linkPickerDialogState: DialogState,
) {
    val detail: TagDetail
        get() =
            TagDetail(
                emoji = emojiState.text,
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
                color = colorState.color.toColorLong(),
            )
}

@Composable
internal fun rememberTagAddFormState(initialColor: Color = randomColor()): TagFormState =
    rememberTagFormState(
        initialEmoji = "",
        initialTitle = "",
        initialDescription = "",
        initialColor = initialColor,
    )

@Composable
internal fun rememberTagDetailFormState(initialDetail: TagDetail): TagFormState =
    rememberTagFormState(
        initialEmoji = initialDetail.emoji,
        initialTitle = initialDetail.title,
        initialDescription = initialDetail.description,
        initialColor = initialDetail.color.toColor(),
    )

@Composable
private fun rememberTagFormState(
    initialEmoji: String,
    initialTitle: String,
    initialDescription: String,
    initialColor: Color,
): TagFormState {
    val emojiState = rememberDiaryEmojiInputState(initialText = initialEmoji)
    val titleState = rememberDiaryTitleInputState(initialText = initialTitle)
    val descriptionState = rememberDiaryDescriptionInputState(initialText = initialDescription)
    val colorState = rememberDiaryColorInputState(initialColor = initialColor)
    val hostState = remember { SnackbarHostState() }
    val linkPickerDialogState = rememberDialogState()

    return remember(emojiState, titleState, descriptionState, colorState, hostState, linkPickerDialogState) {
        TagFormState(
            emojiState = emojiState,
            titleState = titleState,
            descriptionState = descriptionState,
            colorState = colorState,
            hostState = hostState,
            linkPickerDialogState = linkPickerDialogState,
        )
    }
}
