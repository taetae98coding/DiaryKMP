package io.github.taetae98coding.diary.feature.web.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInput
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ScreenPreview
import io.github.taetae98coding.diary.feature.core.tag.EntityTagInputEvent

@Composable
internal fun WebForm(
    onEvent: (EntityTagInputEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebFormState = rememberWebAddFormState(),
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
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
        WebUrlInput(
            state = state.urlState,
            modifier = Modifier.fillMaxWidth(),
        )
        WebHeaderInput(
            state = state.headerState,
            modifier = Modifier.fillMaxWidth(),
        )
        EntityTagInput(
            onTagClick = { id -> onEvent(EntityTagInputEvent.ClickTag(id = id)) },
            onAddClick = { onEvent(EntityTagInputEvent.ClickAdd) },
            modifier = Modifier.fillMaxWidth(),
            uiStateProvider = tagUiStateProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun WebFormPreview() {
    DiaryTheme {
        Surface {
            WebForm(onEvent = {})
        }
    }
}
