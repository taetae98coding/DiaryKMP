package io.github.taetae98coding.diary.feature.memo.ui.form

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryColorInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDateTimeInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.toDiaryDateTimeInputValue
import io.github.taetae98coding.diary.feature.memo.ui.toMemoDateTime
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColorLong

@Stable
internal class MemoFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val colorState: DiaryColorInputState,
    val dateTimeState: DiaryDateTimeInputState,
    val hostState: SnackbarHostState,
    val tagPickerDialogState: DialogState,
    val webPickerDialogState: DialogState,
    val contactPickerDialogState: DialogState,
    val placePickerDialogState: DialogState,
) {
    val detail: MemoDetail
        get() =
            MemoDetail(
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
                color = colorState.color.toColorLong(),
                dateTime = dateTimeState.value.toMemoDateTime(),
            )
}

@Composable
internal fun rememberMemoAddFormState(initialDateTime: DiaryDateTimeInputValue? = null): MemoFormState =
    rememberMemoFormState(
        initialTitle = "",
        initialDescription = "",
        initialColor = randomColor(),
        initialDateTime = initialDateTime,
    )

@Composable
internal fun rememberMemoDetailFormState(initialDetail: MemoDetail): MemoFormState =
    rememberMemoFormState(
        initialTitle = initialDetail.title,
        initialDescription = initialDetail.description,
        initialColor = initialDetail.color.toColor(),
        initialDateTime = remember { initialDetail.dateTime.toDiaryDateTimeInputValue() },
    )

@Composable
private fun rememberMemoFormState(
    initialTitle: String,
    initialDescription: String,
    initialColor: Color,
    initialDateTime: DiaryDateTimeInputValue?,
): MemoFormState {
    val titleState = rememberDiaryTitleInputState(initialText = initialTitle)
    val descriptionState = rememberDiaryDescriptionInputState(initialText = initialDescription)
    val colorState = rememberDiaryColorInputState(initialColor = initialColor)
    val dateTimeState = rememberDiaryDateTimeInputState(initialValue = initialDateTime)
    val hostState = remember { SnackbarHostState() }
    val tagPickerDialogState = rememberDialogState()
    val webPickerDialogState = rememberDialogState()
    val contactPickerDialogState = rememberDialogState()
    val placePickerDialogState = rememberDialogState()

    return remember(
        titleState,
        descriptionState,
        colorState,
        dateTimeState,
        hostState,
        tagPickerDialogState,
        webPickerDialogState,
        contactPickerDialogState,
        placePickerDialogState,
    ) {
        MemoFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            colorState = colorState,
            dateTimeState = dateTimeState,
            hostState = hostState,
            tagPickerDialogState = tagPickerDialogState,
            webPickerDialogState = webPickerDialogState,
            contactPickerDialogState = contactPickerDialogState,
            placePickerDialogState = placePickerDialogState,
        )
    }
}
