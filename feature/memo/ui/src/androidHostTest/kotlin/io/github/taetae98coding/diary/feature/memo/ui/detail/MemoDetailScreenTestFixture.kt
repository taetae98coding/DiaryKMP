package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

internal const val MEMO_TITLE = "MemoDetailTitle"
internal const val EDIT_SUFFIX = "Edited"
internal const val DEFAULT_UPDATE_BUTTON_DESCRIPTION = "Update memo"
internal const val DEFAULT_FINISH_BUTTON_DESCRIPTION = "Finish memo"
internal const val DEFAULT_RESTART_BUTTON_DESCRIPTION = "Restart memo"
internal const val DEFAULT_COPY_BUTTON_DESCRIPTION = "Copy memo"
internal const val DEFAULT_DELETE_BUTTON_DESCRIPTION = "Delete memo"
internal const val DEFAULT_COPY_SUCCEEDED_MESSAGE = "Memo copied."
internal const val ALL_DAY_START_DATE_TEXT = "Jul 19, 2026"
internal val FIRST_MEMO_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

internal fun memoDetail(title: String): MemoDetail = MemoDetail.EMPTY.copy(title = title)

internal fun memoDetailUiState(
    id: Uuid = FIRST_MEMO_ID,
    detail: MemoDetail = MemoDetail.EMPTY,
    isFinished: Boolean = false,
    isInProgress: Boolean = false,
    isFinishInProgress: Boolean = false,
    isCopyInProgress: Boolean = false,
    isDeleteInProgress: Boolean = false,
): MemoDetailUiState.Content =
    MemoDetailUiState.Content(
        id = id,
        detail = detail,
        isFinished = isFinished,
        isInProgress = isInProgress,
        isFinishInProgress = isFinishInProgress,
        isCopyInProgress = isCopyInProgress,
        isDeleteInProgress = isDeleteInProgress,
    )

internal fun screenTestTagViewModel(
    uiState: StateFlow<MemoTagInputUiState> = MutableStateFlow(MemoTagInputUiState()),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingDataOf(emptyList())),
): MemoTagViewModel {
    val viewModel = mockk<MemoTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns uiState
    every { viewModel.tagPagingData } returns tagPagingDataFlow
    every { viewModel.selectableTagPagingData } returns tagPagingDataFlow
    return viewModel
}

internal fun screenTestViewModel(
    uiState: StateFlow<MemoDetailUiState> = MutableStateFlow(MemoDetailUiState.Loading),
    effect: Flow<MemoDetailEffect> = emptyFlow(),
): MemoDetailViewModel {
    val viewModel = mockk<MemoDetailViewModel>()
    every { viewModel.uiState } returns uiState
    every { viewModel.effect } returns effect
    return viewModel
}

/**
 * MemoDetail 화면은 결과 이벤트 버스를 [LocalResultEventBus]에서 읽으므로, 화면을 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun MemoDetailScreenTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}
