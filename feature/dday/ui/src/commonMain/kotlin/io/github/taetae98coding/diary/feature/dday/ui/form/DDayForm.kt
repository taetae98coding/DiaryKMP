package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInput
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInput
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState

@Composable
internal fun DDayForm(
    onEvent: (DDayFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: DDayFormState = rememberDDayAddFormState(),
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
    ) {
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
        DDayBaseDateCard(
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )
        DDayRepeatRuleCard(
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )
        DiaryColorInput(
            modifier = Modifier.fillMaxWidth(),
            state = state.colorState,
        )
        DDayRepeatCard(
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )
        EntityTagInput(
            onTagClick = { id -> onEvent(DDayFormEvent.ClickTag(id = id)) },
            onAddClick = { onEvent(DDayFormEvent.ClickTagAdd) },
            modifier = Modifier.fillMaxWidth(),
            uiStateProvider = tagUiStateProvider,
        )
    }
}

@ComponentPreview
@Composable
private fun DDayFormPreview() {
    DiaryTheme {
        Surface {
            DDayForm(onEvent = {})
        }
    }
}
