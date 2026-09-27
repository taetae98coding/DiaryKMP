package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState

@Stable
internal class FileAddFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val hostState: SnackbarHostState,
) {
    val title: String
        get() = titleState.text.toString()

    val description: String
        get() = descriptionState.text.toString()

    suspend fun clear() {
        titleState.clearText()
        descriptionState.reset()
    }
}

@Composable
internal fun rememberFileAddFormState(): FileAddFormState {
    val titleState = rememberDiaryTitleInputState()
    val descriptionState = rememberDiaryDescriptionInputState()
    val hostState = remember { SnackbarHostState() }

    return remember(titleState, descriptionState, hostState) {
        FileAddFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            hostState = hostState,
        )
    }
}
