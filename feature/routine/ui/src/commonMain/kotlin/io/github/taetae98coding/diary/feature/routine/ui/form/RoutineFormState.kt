package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState

@Stable
internal class RoutineFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
)

@Composable
internal fun rememberRoutineAddFormState(): RoutineFormState {
    val titleState = rememberDiaryTitleInputState()
    val descriptionState = rememberDiaryDescriptionInputState()

    return remember(titleState, descriptionState) {
        RoutineFormState(
            titleState = titleState,
            descriptionState = descriptionState,
        )
    }
}
