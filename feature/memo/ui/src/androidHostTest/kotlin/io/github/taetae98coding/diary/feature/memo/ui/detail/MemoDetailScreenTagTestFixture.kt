package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.placePagingData
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

internal const val DEFAULT_UPDATE_SUCCEEDED_MESSAGE = "Memo updated."
internal const val DEFAULT_TAG_PICKER_TAG_ADD = "Add tag"
internal const val DEFAULT_PRIMARY_SET_DESCRIPTION = "Set as primary tag"
internal const val DEFAULT_PRIMARY_UNSET_DESCRIPTION = "Unset primary tag"

internal fun ComposeContentTestRule.setMemoDetailScreenWithTag(
    uiState: StateFlow<MemoDetailUiState> = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE))),
    tagUiState: StateFlow<MemoTagInputUiState> = MutableStateFlow(MemoTagInputUiState()),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingDataOf(emptyList())),
    tagViewModel: MemoTagViewModel = screenTestTagViewModel(uiState = tagUiState, tagPagingDataFlow = tagPagingDataFlow),
    placeUiState: StateFlow<MemoPlaceInputUiState> = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true)),
    placePagingData: Flow<PagingData<Place>> = placePagingData(),
    navigateToTagAdd: () -> Unit = {},
    navigateToTagDetail: (Uuid) -> Unit = {},
    navigateToPlaceAdd: (Coordinate?) -> Unit = {},
    navigateToPlaceDetail: (Uuid) -> Unit = {},
) {
    setContent {
        MemoDetailScreenTestTheme {
            MemoDetailScreen(
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                detailViewModel = screenTestViewModel(uiState = uiState),
                tagViewModel = tagViewModel,
                webViewModel = screenTestWebViewModel(),
                placeViewModel = screenTestPlaceViewModel(uiState = placeUiState, placePagingData = placePagingData),
                placeMapViewModel = screenTestPlaceMapViewModel(),
                navigateUp = {},
                navigateToCopiedMemo = {},
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
                navigateToWebAdd = {},
                navigateToWebDetail = {},
                navigateToPlaceAdd = navigateToPlaceAdd,
                navigateToPlaceDetail = navigateToPlaceDetail,
                componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                isStandalone = true,
            )
        }
    }
}

internal fun ComposeContentTestRule.openMemoDetailTagPicker() {
    onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
}

internal fun ComposeContentTestRule.setMemoDetailTagScreen(
    tagList: List<Tag> = emptyList(),
    tagViewModel: MemoTagViewModel = memoDetailTagViewModel(tagList = tagList),
    navigateToTagAdd: () -> Unit = {},
    navigateToTagDetail: (Uuid) -> Unit = {},
    navigateToPlaceAdd: (Coordinate?) -> Unit = {},
    navigateToPlaceDetail: (Uuid) -> Unit = {},
) {
    setMemoDetailScreenWithTag(
        tagViewModel = tagViewModel,
        navigateToTagAdd = navigateToTagAdd,
        navigateToTagDetail = navigateToTagDetail,
        navigateToPlaceAdd = navigateToPlaceAdd,
        navigateToPlaceDetail = navigateToPlaceDetail,
    )
}

internal fun memoDetailTagViewModel(
    tagList: List<Tag>,
    selectedTagList: List<Tag> = emptyList(),
    primaryTagId: Uuid? = null,
): MemoTagViewModel =
    screenTestTagViewModel(
        uiState = MutableStateFlow(MemoTagInputUiState(selectedTagList = selectedTagList, primaryTagId = primaryTagId)),
        tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(tagList)),
    )
