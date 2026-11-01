package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.feature.file.ui.previewDiaryFile

@Composable
internal fun FileDetailForm(
    file: DiaryFile,
    modifier: Modifier = Modifier,
    state: FileDetailFormState = rememberFileDetailFormState(),
    isUpdateButtonVisibleProvider: () -> Boolean = { false },
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
        FileDetailFileCard(
            file = file,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isUpdateButtonVisibleProvider()) DiaryTheme.dimens.floatingActionButtonClearance - DiaryTheme.dimens.screenVerticalPadding else 0.dp),
            nameState = state.nameState,
            nameFocusRequester = state.nameFocusRequester,
        )
    }
}

@ScreenPreview
@Composable
private fun FileDetailFormPreview() {
    DiaryTheme {
        Surface {
            FileDetailForm(file = previewDiaryFile(title = "분기 보고서", name = "보고서.pdf", size = 24_536_679))
        }
    }
}
