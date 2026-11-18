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
import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagInput
import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagInputUiState
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview
import io.github.taetae98coding.diary.feature.core.tag.EntityTagInputEvent

@Composable
internal fun DDayForm(
    onEvent: (EntityTagInputEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: DDayFormState = rememberDDayAddFormState(),
    tagUiStateProvider: () -> PrimaryTagInputUiState = { PrimaryTagInputUiState() },
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
        PrimaryTagInput(
            onTagClick = { id -> onEvent(EntityTagInputEvent.ClickTag(id = id)) },
            onAddClick = { onEvent(EntityTagInputEvent.ClickAdd) },
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
