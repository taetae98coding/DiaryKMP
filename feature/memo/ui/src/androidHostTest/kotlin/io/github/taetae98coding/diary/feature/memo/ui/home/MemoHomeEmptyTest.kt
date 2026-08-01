package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.rememberMemoListState
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoHomeEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-HOME-FEATURE-041 필터를 적용하지 않았는데 표시할 메모가 없으면 아직 메모가 없음을 알린다`() {
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-041 한국어 환경에서 빈 상태 안내는 아직 메모가 없습니다이다`() {
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-042 필터를 적용한 채로 표시할 메모가 없으면 조건에 맞는 메모가 없음을 알린다`() {
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(isApplied = true),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FILTERED_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-042 한국어 환경에서 필터 빈 상태 안내는 조건에 맞는 메모가 없습니다이다`() {
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(isApplied = true),
        )

        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_FILTERED_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-043 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setMemoHomeScaffold(MutableStateFlow(loadingMemoPagingData()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-045 빈 상태에서도 메모 추가와 필터 열기, 완료된 메모 확인을 실행할 수 있다`() {
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_BUTTON_LABEL).assert(hasClickAction())
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-048 빈 상태에서도 검색을 실행할 수 있다`() {
        setMemoHomeScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-048 필터를 적용해 두어도 검색을 실행할 수 있다`() {
        setMemoHomeScaffold(
            memoPagingDataFlow = MutableStateFlow(memoPagingDataOf(emptyList())),
            filterUiState = MemoHomeScaffoldFilterUiState(isApplied = true),
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
            filterUiState = MemoHomeScaffoldFilterUiState(isApplied = true),
        )

        assertEmptyBoxIsVerticallyCentered()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-046 빈 상태에서도 목록을 당겨 새로고침할 수 있다`() {
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
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
