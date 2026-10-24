package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail

@Stable
internal class FileDetailFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val nameState: TextFieldState,
    val nameFocusRequester: FocusRequester,
    val snackbarHostState: SnackbarHostState,
) {
    val detail: DiaryFileDetail
        get() =
            DiaryFileDetail(
                name = nameState.text.toString(),
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
            )

    fun requestNameFocus() {
        nameFocusRequester.requestFocus()
    }
}

@Composable
internal fun rememberFileDetailFormState(initialDetail: DiaryFileDetail = DiaryFileDetail(name = "", title = "", description = "")): FileDetailFormState {
    val titleState = rememberDiaryTitleInputState(initialText = initialDetail.title)
    val descriptionState = rememberDiaryDescriptionInputState(initialText = initialDetail.description)
    val nameState = rememberTextFieldState(initialText = initialDetail.name)
    val nameFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    return remember(titleState, descriptionState, nameState, nameFocusRequester, snackbarHostState) {
        FileDetailFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            nameState = nameState,
            nameFocusRequester = nameFocusRequester,
            snackbarHostState = snackbarHostState,
        )
    }
}
