package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoHomeEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-48ab2da7-8834-4aed-8d71-edecd7a2abe3 필터를 적용하지 않았는데 표시할 메모가 없으면 아직 메모가 없음을 알린다`() {
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-48ab2da7-8834-4aed-8d71-edecd7a2abe3 선택한 태그가 모두 판정에서 무시되고 있으면 아직 메모가 없음을 알린다`() {
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(storedTagIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>())),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-48ab2da7-8834-4aed-8d71-edecd7a2abe3 한국어 환경에서 빈 상태 안내는 아직 메모가 없습니다이다`() {
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-81c9e74a-735a-4d32-ad00-4a760300e14d 필터를 적용한 채로 표시할 메모가 없으면 조건에 맞는 메모가 없음을 알린다`() {
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST)),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-81c9e74a-735a-4d32-ad00-4a760300e14d 한국어 환경에서 필터 빈 상태 안내는 조건에 맞는 메모가 없습니다이다`() {
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST)),
        )

        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-81c9e74a-735a-4d32-ad00-4a760300e14d 유무 필터의 한 축이 없음인 채로 표시할 메모가 없으면 조건에 맞는 메모가 없음을 알린다`() {
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(place = MemoFilterExistence.NOT_EXIST)),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-81c9e74a-735a-4d32-ad00-4a760300e14d 태그를 하나 이상 선택한 채로 표시할 메모가 없으면 조건에 맞는 메모가 없음을 알린다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-e6e2ae08-6e73-4a51-a80c-edc4a8222c77 처음 불러오기에 실패하면 오류 안내 없이 빈 상태 안내를 표시한다`() {
        setMemoHomeScaffold(
            MutableStateFlow(
                memoPagingDataWithLoadStates(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                ),
            ),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-e6e2ae08-6e73-4a51-a80c-edc4a8222c77 이어서 불러오기에 실패해도 앞서 불러온 메모를 그대로 표시한다`() {
        setMemoHomeScaffold(
            MutableStateFlow(
                memoPagingDataWithLoadStates(
                    itemList = listOf(MemoListItem.Content(memo = memo(title = MEMO_TITLE))),
                    append = LoadState.Error(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                ),
            ),
        )
        waitUntilTextDisplayed(MEMO_TITLE)

        composeRule.onNodeWithText(MEMO_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(MEMO_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-c5c17316-ff60-45f8-8d73-0487e9937cd0 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setMemoHomeScaffold(MutableStateFlow(loadingMemoPagingData()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-fe0ce52b-a27a-4183-b061-0ccfbc8635f2 빈 상태에서도 메모 추가와 필터 열기, 완료된 메모 확인을 실행할 수 있다`() {
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_BUTTON_LABEL).assert(hasClickAction())
    }

    @Test
    fun `TC-06c46da4-a520-44eb-aa94-8b95d6eaea02 빈 상태에서도 검색을 실행할 수 있다`() {
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-06c46da4-a520-44eb-aa94-8b95d6eaea02 필터를 적용해 두어도 검색을 실행할 수 있다`() {
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST)),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `빈 상태 안내는 목록 영역의 세로 가운데에 놓인다`() {
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        assertEmptyBoxIsVerticallyCentered()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `필터를 적용한 빈 상태 안내도 목록 영역의 세로 가운데에 놓인다`() {
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST)),
        )

        assertEmptyBoxIsVerticallyCentered()
    }

    @Test
    fun `TC-12aefeb2-393e-4408-afc9-94e251e7d209 빈 상태에서도 목록을 당겨 새로고침할 수 있다`() {
        val eventList = mutableListOf<MemoListEvent>()
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())), onMemoListEvent = eventList::add)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoListEvent.Refresh)
    }

    private fun setMemoHomeScaffold(
        memoPagingDataFlow: MutableStateFlow<PagingData<MemoListItem>>,
        filterUiState: MemoHomeScaffoldFilterUiState = MemoHomeScaffoldFilterUiState(),
        onMemoListEvent: (MemoListEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoHomeScaffold(
                    memoListState = rememberMemoListState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                    onEvent = {},
                    onMemoListEvent = onMemoListEvent,
                )
            }
        }
    }

    private fun memoPagingDataWithLoadStates(
        itemList: List<MemoListItem>,
        refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
        append: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
    ): PagingData<MemoListItem> =
        PagingData.from(
            data = itemList,
            sourceLoadStates =
                LoadStates(
                    refresh = refresh,
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = append,
                ),
        )

    private fun assertEmptyBoxIsVerticallyCentered() {
        val listBounds = composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).getUnclippedBoundsInRoot()
        val emptyBounds = composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).getUnclippedBoundsInRoot()

        val listCenter = (listBounds.top + listBounds.bottom) / 2
        val emptyCenter = (emptyBounds.top + emptyBounds.bottom) / 2

        abs((emptyCenter - listCenter).value) shouldBeLessThan CENTER_TOLERANCE.value
    }

    private fun waitUntilTextDisplayed(text: String) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        private const val DEFAULT_EMPTY_TITLE = "No memos yet"
        private const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a memo."
        private const val DEFAULT_FILTERED_EMPTY_TITLE = "No memos match the filter"
        private const val DEFAULT_FILTERED_EMPTY_DESCRIPTION = "Change the filter to see other memos."
        private const val KOREAN_EMPTY_TITLE = "아직 메모가 없습니다"
        private const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 메모를 만들 수 있습니다"
        private const val KOREAN_FILTERED_EMPTY_TITLE = "조건에 맞는 메모가 없습니다"
        private const val KOREAN_FILTERED_EMPTY_DESCRIPTION = "필터를 바꾸면 다른 메모를 볼 수 있습니다"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter"
        private const val DEFAULT_FINISHED_LIST_BUTTON_LABEL = "Finished memos"
        private const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search"
        private const val DEFAULT_RETRY_TEXT = "Retry"
        private const val MEMO_TITLE = "EmptyStateMemoTitle"
        private const val TIMEOUT_MILLIS = 5_000L
        private val CENTER_TOLERANCE: Dp = 2.dp

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(title: String): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = null))
                .setExp(Memo::isFinished, false)
                .setExp(Memo::isDeleted, false)
                .setExp(Memo::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Memo::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
