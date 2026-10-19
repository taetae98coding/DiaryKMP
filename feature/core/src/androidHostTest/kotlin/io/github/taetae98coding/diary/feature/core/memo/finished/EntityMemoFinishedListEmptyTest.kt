package io.github.taetae98coding.diary.feature.core.memo.finished

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EntityMemoFinishedListEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-013 완료된 메모가 없으면 화면이 정한 빈 상태 안내를 표시한다`() {
        setEntityMemoFinishedListScaffold(pagingData = loadedEmptyPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-013 빈 상태에 메모 추가를 권하는 안내를 두지 않는다`() {
        setEntityMemoFinishedListScaffold(pagingData = loadedEmptyPagingData())

        composeRule.onNodeWithText(DEFAULT_ADD_EMPTY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-014 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setEntityMemoFinishedListScaffold(pagingData = loadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-015 대상 항목을 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setEntityMemoFinishedListScaffold(
            pagingData = loadedEmptyPagingData(),
            uiState = EntityMemoFinishedListUiState(),
        )

        composeRule.onNodeWithText(EMPTY_TITLE).assertExists()
    }

    private fun setEntityMemoFinishedListScaffold(
        pagingData: PagingData<MemoListItem>,
        uiState: EntityMemoFinishedListUiState = EntityMemoFinishedListUiState(title = ENTITY_NAME),
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                EntityMemoFinishedListScaffold(
                    memoListState = rememberMemoListState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = {},
                    onMemoListEvent = {},
                    emptyTitle = EMPTY_TITLE,
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    private companion object {
        const val EMPTY_TITLE = "EntityEmptyTitle"
        private const val DEFAULT_ADD_EMPTY_DESCRIPTION = "Use the add button to create a memo."
        private const val ENTITY_NAME = "EntityName"

        private fun loadedEmptyPagingData(): PagingData<MemoListItem> =
            PagingData.from(
                data = emptyList(),
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
            )

        private fun loadingPagingData(): PagingData<MemoListItem> =
            PagingData.from(
                data = emptyList(),
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.Loading,
                        prepend = LoadState.NotLoading(endOfPaginationReached = false),
                        append = LoadState.NotLoading(endOfPaginationReached = false),
                    ),
            )
    }
}
