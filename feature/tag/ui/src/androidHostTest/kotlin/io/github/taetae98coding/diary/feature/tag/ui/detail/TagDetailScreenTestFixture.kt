package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DIARY_EMOJI_INPUT_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.tag.ui.detail.form.TagDetailFormTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TagDetailMemoTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.web.TagDetailWebTab
import io.github.taetae98coding.diary.feature.tag.ui.form.TagFormState
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

internal const val TAG_TITLE = "TagDetailTitle"
internal const val DEFAULT_UPDATE_BUTTON_DESCRIPTION = "Update tag"
internal const val DEFAULT_FINISH_BUTTON_DESCRIPTION = "Finish tag"
internal const val DEFAULT_RESTART_BUTTON_DESCRIPTION = "Restart tag"
internal const val DEFAULT_DELETE_BUTTON_DESCRIPTION = "Delete tag"
internal const val DEFAULT_DETAIL_TAB_DESCRIPTION = "Tag detail"
internal const val DEFAULT_MEMO_TAB_DESCRIPTION = "Memo"
internal const val KOREAN_DETAIL_TAB_DESCRIPTION = "태그 디테일"
internal const val KOREAN_MEMO_TAB_DESCRIPTION = "메모"
internal const val DEFAULT_WEB_TAB_DESCRIPTION = "Web"
internal const val DEFAULT_PLACE_TAB_DESCRIPTION = "Place"
internal const val KOREAN_WEB_TAB_DESCRIPTION = "웹"
internal const val KOREAN_PLACE_TAB_DESCRIPTION = "장소"
internal const val EDIT_SUFFIX = "Edited"
internal val FIRST_TAG_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

internal fun tagDetail(
    title: String,
    emoji: String = "",
): TagDetail = TagDetail.EMPTY.copy(emoji = emoji, title = title)

internal fun tagDetailUiState(
    id: Uuid = FIRST_TAG_ID,
    detail: TagDetail = TagDetail.EMPTY,
    isFinished: Boolean = false,
    isInProgress: Boolean = false,
    isFinishInProgress: Boolean = false,
    isDeleteInProgress: Boolean = false,
): TagDetailUiState.Content =
    TagDetailUiState.Content(
        id = id,
        detail = detail,
        isFinished = isFinished,
        isInProgress = isInProgress,
        isFinishInProgress = isFinishInProgress,
        isDeleteInProgress = isDeleteInProgress,
    )

internal fun screenTestViewModel(
    uiState: StateFlow<TagDetailUiState> = MutableStateFlow(TagDetailUiState.Loading),
    effect: Flow<TagDetailEffect> = emptyFlow(),
): TagDetailViewModel {
    val viewModel = mockk<TagDetailViewModel>()
    every { viewModel.uiState } returns uiState
    every { viewModel.effect } returns effect
    return viewModel
}

internal fun ComposeContentTestRule.emojiInput(): SemanticsNodeInteraction = onNodeWithTag(DIARY_EMOJI_INPUT_TEST_TAG)

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[0]

internal fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[1]

@Composable
internal fun TagDetailTestTabContent(
    tab: TagDetailTab,
    uiStateProvider: () -> TagDetailUiState = { TagDetailUiState.Loading },
    state: TagFormState = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY),
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
) {
    when (tab) {
        TagDetailTab.DETAIL ->
            TagDetailFormTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                uiStateProvider = uiStateProvider,
                state = state,
            )

        TagDetailTab.MEMO ->
            TagDetailMemoTab(
                onEvent = {},
                onMemoListEvent = {},
                modifier = Modifier.fillMaxSize(),
                memoPagingItems = memoPagingItems,
            )

        TagDetailTab.WEB ->
            TagDetailWebTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                webPagingItems = webPagingItems,
            )

        TagDetailTab.PLACE ->
            TagDetailPlaceTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                placePagingItems = placePagingItems,
            )
    }
}
