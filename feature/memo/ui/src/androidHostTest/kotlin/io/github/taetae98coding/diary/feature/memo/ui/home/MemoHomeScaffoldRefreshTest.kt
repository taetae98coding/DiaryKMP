package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.MemoListUiState
import io.github.taetae98coding.diary.compose.memo.rememberMemoListState
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoHomeScaffoldRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-001 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<MemoListEvent>()
        setMemoHomeScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))),
            onMemoListEvent = { event -> eventList += event },
        )

        composeRule.onNodeWithTag(MEMO_HOME_LIST_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoListEvent.Refresh)
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-002 진행 표시 상태이면 진행 표시가 나타난다`() {
        setMemoHomeScaffold(memoListUiStateProvider = { MemoListUiState(isRefreshing = true) })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w411dp-h891dp")
    fun `TC-SYNC-REFRESH-FEATURE-002 한국어 환경에서 진행 표시에 새로고침 중 이름을 제공한다`() {
        setMemoHomeScaffold(memoListUiStateProvider = { MemoListUiState(isRefreshing = true) })

        composeRule.onNodeWithContentDescription(KOREAN_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-004 진행 표시 상태가 아니면 진행 표시가 나타나지 않는다`() {
        setMemoHomeScaffold(memoListUiStateProvider = { MemoListUiState(isRefreshing = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-006 새로고침이 끝나면 진행 표시만 사라지고 목록은 유지된다`() {
        val isRefreshing = mutableStateOf(true)
        setMemoHomeScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))),
            memoListUiStateProvider = { MemoListUiState(isRefreshing = isRefreshing.value) },
        )
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-007 진행 표시 중에도 메모를 선택할 수 있다`() {
        val eventList = mutableListOf<MemoListEvent>()
        val memo = memo(title = FIRST_TITLE)
        setMemoHomeScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo)),
            memoListUiStateProvider = { MemoListUiState(isRefreshing = true) },
            onMemoListEvent = { event -> eventList += event },
        )

        composeRule.onNodeWithText(FIRST_TITLE).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoListEvent.ClickMemo(id = memo.id))
    }

    private fun setMemoHomeScaffold(
        itemList: List<MemoListItem> = emptyList(),
        memoListUiStateProvider: () -> MemoListUiState = { MemoListUiState() },
        onMemoListEvent: (MemoListEvent) -> Unit = {},
    ) {
        val memoPagingData = MutableStateFlow(memoPagingDataOf(itemList))

        composeRule.setContent {
            DiaryTheme {
                MemoHomeScaffold(
                    memoListState = rememberMemoListState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    memoPagingItems = memoPagingData.collectAsLazyPagingItems(),
                    onEvent = {},
                    onMemoListEvent = onMemoListEvent,
                    memoListUiStateProvider = memoListUiStateProvider,
                )
            }
        }

        itemList
            .filterIsInstance<MemoListItem.Content>()
            .firstOrNull()
            ?.memo
            ?.detail
            ?.title
            ?.let { title ->
                composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
                    composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
                }
            }
    }

    private fun memo(title: String): Memo =
        fixtureMonkey
            .giveMeKotlinBuilder<Memo>()
            .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = null))
            .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .sample()

    public companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val FIRST_TITLE = "FirstMemoTitle"
        private const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        private const val KOREAN_REFRESHING_DESCRIPTION = "새로고침 중"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
