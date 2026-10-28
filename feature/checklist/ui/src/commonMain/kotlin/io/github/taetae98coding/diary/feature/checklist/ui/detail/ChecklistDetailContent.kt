package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.ChecklistIcon
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryChecklistItemSortBarHost
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_empty_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_empty_title
import io.github.taetae98coding.diary.feature.checklist.ui.item.ChecklistItemInputRow
import io.github.taetae98coding.diary.feature.checklist.ui.previewChecklistItem
import org.jetbrains.compose.resources.stringResource

private const val TITLE_KEY = "title"
private const val DESCRIPTION_KEY = "description"
private const val SORT_KEY = "sort"
private const val EMPTY_KEY = "empty"
private const val INPUT_KEY = "input"

@Composable
internal fun ChecklistDetailContent(
    onEvent: (ChecklistDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ChecklistDetailFormState = rememberChecklistDetailFormState(),
    itemUiStateProvider: () -> ChecklistDetailItemUiState = { ChecklistDetailItemUiState() },
) {
    val itemUiState = itemUiStateProvider()
    val horizontalPadding = DiaryTheme.dimens.screenHorizontalPadding
    val componentSpacing = DiaryTheme.dimens.componentSpacing

    LazyColumn(
        modifier = modifier,
        state = state.listState,
        contentPadding =
            PaddingValues(
                top = DiaryTheme.dimens.screenVerticalPadding,
                bottom = DiaryTheme.dimens.floatingActionButtonClearance,
            ),
    ) {
        formInputs(
            state = state,
            horizontalPadding = horizontalPadding,
            componentSpacing = componentSpacing,
        )
        checklistItems(
            onEvent = onEvent,
            itemUiState = itemUiState,
            itemUiStateProvider = itemUiStateProvider,
            componentSpacing = componentSpacing,
        )
        item(key = INPUT_KEY) {
            ChecklistItemInputRow(
                onAdd = { onEvent(ChecklistDetailScaffoldEvent.ClickItemAdd) },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                state = state.itemInputState,
                isInProgressProvider = { itemUiStateProvider().isAddInProgress },
            )
        }
    }
}

private fun LazyListScope.formInputs(
    state: ChecklistDetailFormState,
    horizontalPadding: Dp,
    componentSpacing: Dp,
) {
    item(key = TITLE_KEY) {
        DiaryTitleInput(
            state = state.titleState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
        )
    }
    item(key = DESCRIPTION_KEY) {
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = componentSpacing),
        )
    }
}

private fun LazyListScope.checklistItems(
    onEvent: (ChecklistDetailScaffoldEvent) -> Unit,
    itemUiState: ChecklistDetailItemUiState,
    itemUiStateProvider: () -> ChecklistDetailItemUiState,
    componentSpacing: Dp,
) {
    if (itemUiState.itemList.isNotEmpty()) {
        stickyHeader(key = SORT_KEY) {
            Surface(modifier = Modifier.fillMaxWidth().padding(top = componentSpacing)) {
                DiaryChecklistItemSortBarHost(
                    onClick = { onEvent(ChecklistDetailScaffoldEvent.ClickSort) },
                    modifier = Modifier.fillMaxWidth(),
                    sortProvider = { itemUiStateProvider().sort },
                )
            }
        }
    }
    items(
        items = itemUiState.itemList,
        key = { item -> item.id.toString() },
    ) { item ->
        ChecklistItemRow(
            onClick = { onEvent(ChecklistDetailScaffoldEvent.ClickItem(id = item.id, isChecked = item.isChecked)) },
            onRemove = { onEvent(ChecklistDetailScaffoldEvent.ClickItemRemove(id = item.id)) },
            item = item,
            modifier =
                Modifier
                    .animateItem()
                    .fillMaxWidth(),
        )
    }
    if (itemUiState.isEmpty) {
        item(key = EMPTY_KEY) {
            DiaryEmptyBox(
                title = stringResource(Res.string.checklist_detail_empty_title),
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(vertical = DiaryTheme.dimens.screenVerticalPadding),
                description = stringResource(Res.string.checklist_detail_empty_description),
                icon = { ChecklistIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        }
    }
}

private class ChecklistDetailItemUiStatePreviewParameter : PreviewParameterProvider<ChecklistDetailItemUiState> {
    override val values: Sequence<ChecklistDetailItemUiState> =
        sequenceOf(
            ChecklistDetailItemUiState(
                itemList =
                    listOf(
                        previewChecklistItem(content = "여권"),
                        previewChecklistItem(content = "충전기", isChecked = true),
                    ),
                isLoaded = true,
            ),
            ChecklistDetailItemUiState(isLoaded = true),
        )
}

@ScreenPreview
@Composable
private fun ChecklistDetailContentPreview(
    @PreviewParameter(ChecklistDetailItemUiStatePreviewParameter::class) itemUiState: ChecklistDetailItemUiState,
) {
    DiaryTheme {
        Surface {
            ChecklistDetailContent(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                itemUiStateProvider = { itemUiState },
            )
        }
    }
}
