package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.dialog.DiaryDatePickerDialogHost
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.RoutineIcon
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.systemscroll.systemScroll
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.previewRoutineDetail
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineRecordInputRow
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineRecordRow
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineForm
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineFormEvent
import io.github.taetae98coding.diary.feature.routine.ui.previewRoutineRecord
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_record_empty_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_record_empty_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_detail_record_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

private const val FORM_KEY = "form"
private const val RECORD_TITLE_KEY = "recordTitle"
private const val RECORD_INPUT_KEY = "recordInput"
private const val RECORD_EMPTY_KEY = "recordEmpty"

@Composable
internal fun RoutineDetailContent(
    onEvent: (RoutineDetailScaffoldEvent) -> Unit,
    onFormEvent: (RoutineFormEvent) -> Unit,
    detail: RoutineDetail,
    modifier: Modifier = Modifier,
    state: RoutineDetailScaffoldState = rememberRoutineDetailScaffoldState(),
    recordPagingItems: LazyPagingItems<RoutineRecord> = remember { flowOf(PagingData.empty<RoutineRecord>()) }.collectAsLazyPagingItems(),
    recordUiStateProvider: () -> RoutineDetailRecordUiState = { RoutineDetailRecordUiState() },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    val horizontalPadding = DiaryTheme.dimens.screenHorizontalPadding
    val componentSpacing = DiaryTheme.dimens.componentSpacing
    val itemSpacing = DiaryTheme.dimens.itemSpacing

    LazyColumn(
        modifier = modifier.systemScroll(state.listState) { state.listState.animateScrollToItem(0) },
        state = state.listState,
        contentPadding =
            PaddingValues(
                top = DiaryTheme.dimens.screenVerticalPadding,
                bottom = DiaryTheme.dimens.floatingActionButtonClearance,
            ),
    ) {
        item(key = FORM_KEY) {
            RoutineForm(
                onEvent = onFormEvent,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding),
                state = state.formState,
                tagUiStateProvider = tagUiStateProvider,
            )
        }
        recordInputs(
            onEvent = onEvent,
            detail = detail,
            state = state,
            recordUiStateProvider = recordUiStateProvider,
            horizontalPadding = horizontalPadding,
            componentSpacing = componentSpacing,
            itemSpacing = itemSpacing,
        )
        records(
            onEvent = onEvent,
            detail = detail,
            recordPagingItems = recordPagingItems,
        )
    }

    DiaryDatePickerDialogHost(
        initialDateProvider = { state.recordInputState.date },
        onConfirm = state.recordInputState.dateState::select,
        dialogState = state.recordInputState.datePickerDialogState,
        minDateProvider = { detail.startDate },
    )
}

private fun LazyListScope.recordInputs(
    onEvent: (RoutineDetailScaffoldEvent) -> Unit,
    detail: RoutineDetail,
    state: RoutineDetailScaffoldState,
    recordUiStateProvider: () -> RoutineDetailRecordUiState,
    horizontalPadding: Dp,
    componentSpacing: Dp,
    itemSpacing: Dp,
) {
    item(key = RECORD_TITLE_KEY) {
        Text(
            text = stringResource(Res.string.routine_detail_record_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = componentSpacing)
                    .semantics { heading() },
            maxLines = 1,
            style = DiaryTheme.typography.titleSmall,
        )
    }
    item(key = RECORD_INPUT_KEY) {
        RoutineRecordInputRow(
            onAdd = { onEvent(RoutineDetailScaffoldEvent.ClickRecordAdd) },
            unit = detail.unit,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = itemSpacing),
            state = state.recordInputState,
            isInProgressProvider = { recordUiStateProvider().isAddInProgress },
        )
    }
}

private fun LazyListScope.records(
    onEvent: (RoutineDetailScaffoldEvent) -> Unit,
    detail: RoutineDetail,
    recordPagingItems: LazyPagingItems<RoutineRecord>,
) {
    items(
        count = recordPagingItems.itemCount,
        key = recordPagingItems.itemKey { record -> record.id.toString() },
    ) { index ->
        val record = recordPagingItems[index]

        RoutineRecordRow(
            onRemove = { record?.let { value -> onEvent(RoutineDetailScaffoldEvent.ClickRecordRemove(id = value.id)) } },
            unit = detail.unit,
            modifier =
                Modifier
                    .animateItem()
                    .fillMaxWidth(),
            record = record,
        )
    }
    if (recordPagingItems.isLoadedEmpty()) {
        item(key = RECORD_EMPTY_KEY) {
            DiaryEmptyBox(
                title = stringResource(Res.string.routine_detail_record_empty_title),
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(vertical = DiaryTheme.dimens.screenVerticalPadding),
                description = stringResource(Res.string.routine_detail_record_empty_description),
                icon = { RoutineIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        }
    }
}

@ScreenPreview
@Composable
private fun RoutineDetailContentPreview(
    @PreviewParameter(BooleanPreviewParameter::class) hasRecord: Boolean,
) {
    val detail = remember { previewRoutineDetail(title = "물 마시기") }
    val pagingData =
        remember(hasRecord) {
            flowOf(if (hasRecord) PagingData.from(listOf(previewRoutineRecord(), previewRoutineRecord(amount = 500))) else PagingData.empty())
        }

    DiaryTheme {
        Surface {
            RoutineDetailContent(
                onEvent = {},
                onFormEvent = {},
                detail = detail,
                modifier = Modifier.fillMaxSize(),
                state = rememberRoutineDetailScaffoldState(initialDetail = detail),
                recordPagingItems = pagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
