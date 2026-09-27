package io.github.taetae98coding.diary.feature.file.ui.add

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.FileUploadSource

@Composable
internal fun FileAddForm(
    onChooseFileClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: FileAddFormState = rememberFileAddFormState(),
    selectedFileProvider: () -> FileUploadSource? = { null },
) {
    DiaryInputColumn(modifier = modifier) {
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.fillMaxWidth(),
            nextFocusProvider = { state.descriptionState.focusTarget },
        )
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
        FileAddFileCard(
            onChooseClick = onChooseFileClick,
            modifier = Modifier.fillMaxWidth(),
            selectedFileProvider = selectedFileProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun FileAddFormPreview() {
    DiaryTheme {
        Surface {
            FileAddForm(onChooseFileClick = {})
        }
    }
}
