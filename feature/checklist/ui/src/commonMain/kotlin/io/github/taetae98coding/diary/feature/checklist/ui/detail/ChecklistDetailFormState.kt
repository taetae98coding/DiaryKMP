package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.withFrameNanos
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.feature.checklist.ui.item.ChecklistItemInputState
import io.github.taetae98coding.diary.feature.checklist.ui.item.rememberChecklistItemInputState

@Stable
internal class ChecklistDetailFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val itemInputState: ChecklistItemInputState,
    val listState: LazyListState,
    val snackbarHostState: SnackbarHostState,
    val sortSheetState: DialogState,
    val resetDialogState: DialogState,
) {
    val detail: ChecklistDetail
        get() =
            ChecklistDetail(
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
            )

    suspend fun scrollToItemList() {
        listState.scrollToItem(index = ITEM_LIST_INDEX)
    }

    suspend fun bringItemInputIntoView() {
        // 새 행이 배치된 뒤에야 밀려난 항목 입력의 자리를 알 수 있다.
        withFrameNanos { }
        val layoutInfo = listState.layoutInfo
        val lastIndex = layoutInfo.totalItemsCount - 1
        val inputInfo = layoutInfo.visibleItemsInfo.find { info -> info.index == lastIndex }
        if (inputInfo == null) {
            listState.scrollToItem(index = lastIndex)
            return
        }

        val overflow = inputInfo.offset + inputInfo.size - (layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding)
        if (overflow > 0) {
            listState.scrollBy(value = overflow.toFloat())
        }
    }

    private companion object {
        // 제목과 설명 입력 뒤에 정렬 줄과 체크 항목이 놓인다.
        const val ITEM_LIST_INDEX = 2
    }
}

@Composable
internal fun rememberChecklistDetailFormState(initialDetail: ChecklistDetail = ChecklistDetail.EMPTY): ChecklistDetailFormState {
    val titleState = rememberDiaryTitleInputState(initialText = initialDetail.title)
    val descriptionState = rememberDiaryDescriptionInputState(initialText = initialDetail.description)
    val itemInputState = rememberChecklistItemInputState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sortSheetState = rememberDialogState()
    // rememberSaveable과 달리 retain은 프로세스가 정리되면 값을 버린다.
    val resetDialogState = retain { DialogState() }

    return remember(titleState, descriptionState, itemInputState, listState, snackbarHostState, sortSheetState, resetDialogState) {
        ChecklistDetailFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            itemInputState = itemInputState,
            listState = listState,
            snackbarHostState = snackbarHostState,
            sortSheetState = sortSheetState,
            resetDialogState = resetDialogState,
        )
    }
}
