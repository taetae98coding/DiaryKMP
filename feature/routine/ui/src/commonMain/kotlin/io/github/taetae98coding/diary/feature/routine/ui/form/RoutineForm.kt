package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun RoutineForm(
    modifier: Modifier = Modifier,
    state: RoutineFormState = rememberRoutineAddFormState(),
) {
    DiaryInputColumn(modifier = modifier) {
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ScreenPreview
@Composable
private fun RoutineFormPreview() {
    DiaryTheme {
        Surface {
            RoutineForm()
        }
    }
}
