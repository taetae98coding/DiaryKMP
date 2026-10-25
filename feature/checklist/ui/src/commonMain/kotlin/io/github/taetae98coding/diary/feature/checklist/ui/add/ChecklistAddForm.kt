package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun ChecklistAddForm(
    onEvent: (ChecklistAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ChecklistAddFormState = rememberChecklistAddFormState(),
) {
    Column(
        modifier =
            modifier
                .verticalScroll(state.scrollState)
                .padding(
                    start = DiaryTheme.dimens.screenHorizontalPadding,
                    top = DiaryTheme.dimens.screenVerticalPadding,
                    end = DiaryTheme.dimens.screenHorizontalPadding,
                    bottom = DiaryTheme.dimens.floatingActionButtonClearance,
                ),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.componentSpacing),
    ) {
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.fillMaxWidth(),
            nextFocusProvider = { state.descriptionState.focusTarget },
        )
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
        ChecklistAddItemCard(
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )
    }
}

@ScreenPreview
@Composable
private fun ChecklistAddFormPreview() {
    DiaryTheme {
        Surface {
            ChecklistAddForm(onEvent = {})
        }
    }
}
