package io.github.taetae98coding.diary.feature.web.ui.form

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader

@Stable
internal class WebFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val urlState: WebUrlInputState,
    val headerState: WebHeaderInputState,
    val hostState: SnackbarHostState,
    val tagPickerDialogState: DialogState,
) {
    val detail: WebDetail
        get() =
            WebDetail(
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
                url = urlState.text.toString(),
                headerList = headerState.rowList.map { row -> WebHeader(name = row.name, value = row.value) },
            )
}

@Composable
internal fun rememberWebAddFormState(): WebFormState = rememberWebFormState(initialDetail = WebDetail.EMPTY)

@Composable
internal fun rememberWebDetailFormState(initialDetail: WebDetail = WebDetail.EMPTY): WebFormState = rememberWebFormState(initialDetail = initialDetail)

@Composable
private fun rememberWebFormState(initialDetail: WebDetail): WebFormState {
    val titleState = rememberDiaryTitleInputState(initialText = initialDetail.title)
    val descriptionState = rememberDiaryDescriptionInputState(initialText = initialDetail.description)
    val urlState = rememberWebUrlInputState(initialText = initialDetail.url)
    val headerState =
        rememberWebHeaderInputState(
            initialRowList = initialDetail.headerList.map { header -> WebHeaderRowState(initialName = header.name, initialValue = header.value) },
        )
    val hostState = remember { SnackbarHostState() }
    val tagPickerDialogState = rememberDialogState()

    return remember(titleState, descriptionState, urlState, headerState, hostState, tagPickerDialogState) {
        WebFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            urlState = urlState,
            headerState = headerState,
            hostState = hostState,
            tagPickerDialogState = tagPickerDialogState,
        )
    }
}
