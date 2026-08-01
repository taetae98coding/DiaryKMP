package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.MemoListState
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoFinishedListScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-002 목록에 완료된 메모 제목이 카드로 표시된다`() {
        val itemList =
            listOf(
                MemoListItem.Content(memo = memo(title = FIRST_TITLE)),
                MemoListItem.Content(memo = memo(title = SECOND_TITLE)),
            )

        setMemoFinishedListScaffold(itemList = itemList)

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-003 기간이 있는 메모에는 기간이 함께 표시된다`() {
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE, dateTime = allDay(day = 19)))),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_ALL_DAY_PERIOD).assertExists()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-004 기간이 없는 메모에는 기간이 표시되지 않는다`() {
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_ALL_DAY_PERIOD).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-009 이 화면에는 태그 필터 버튼을 표시하지 않는다`() {
        setMemoFinishedListScaffold(itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-021 이 화면에는 메모 추가 버튼을 표시하지 않는다`() {
        setMemoFinishedListScaffold(itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-010 기간이 있는 메모의 카드 위에 시작 날짜 헤더가 표시된다`() {
        setMemoFinishedListScaffold(itemList = dateHeaderMemoItemList())

        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_ALL_DAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_NEXT_DAY_HEADER)).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-011 시작 날짜가 같은 메모들에는 날짜 헤더가 하나만 표시된다`() {
        val itemList =
            listOf(
                MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 19)),
                MemoListItem.Content(memo = memo(title = FIRST_TITLE, dateTime = allDay(day = 19))),
                MemoListItem.Content(memo = memo(title = SECOND_TITLE, dateTime = dateTime(day = 19, hour = 13))),
            )

        setMemoFinishedListScaffold(itemList = itemList)

        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_ALL_DAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodesWithTag(MEMO_DATE_HEADER_TEST_TAG).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-012 기간이 없는 메모에는 날짜 헤더가 표시되지 않는다`() {
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onAllNodesWithTag(MEMO_DATE_HEADER_TEST_TAG).assertCountEquals(0)
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-013 시작 날짜가 오늘인 메모의 날짜 헤더에는 오늘 문구가 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val itemList =
            listOf(
                MemoListItem.DateHeader(date = today),
                MemoListItem.Content(
                    memo = memo(title = FIRST_TITLE, dateTime = MemoDateTime.AllDay(dateRange = today..today)),
                ),
            )

        setMemoFinishedListScaffold(itemList = itemList, isTodayUpdated = true)

        composeRule.onNode(dateHeaderMatcher(DEFAULT_TODAY_HEADER)).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-FINISHED-LIST-FEATURE-013 한국어 환경에서 오늘 문구를 표시한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val itemList =
            listOf(
                MemoListItem.DateHeader(date = today),
                MemoListItem.Content(
                    memo = memo(title = FIRST_TITLE, dateTime = MemoDateTime.AllDay(dateRange = today..today)),
                ),
            )

        setMemoFinishedListScaffold(itemList = itemList, isTodayUpdated = true)

        composeRule.onNode(dateHeaderMatcher(KOREAN_TODAY_HEADER)).assertExists()
    }

    @Test
    fun `기본 환경에서 상단 바 제목은 Finished Memos이다`() {
        setMemoFinishedListScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 상단 바 제목은 완료된 메모이다`() {
        setMemoFinishedListScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-019 메모 카드를 선택하면 해당 메모의 상세 화면 전환 행동을 한 번 전달한다`() {
        val memo = memo(title = FIRST_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo)),
            onMemoListEvent = eventList::add,
        )

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(MemoListEvent.ClickMemo(memo.id))
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-020 뒤로가기 버튼을 선택하면 뒤로가기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<MemoFinishedListScaffoldEvent>()
        setMemoFinishedListScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(MemoFinishedListScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-015 좌에서 우로 스와이프하면 다시 시작 행동을 한 번 전달한다`() {
        val memo = memo(title = FIRST_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo)),
            onMemoListEvent = eventList::add,
        )

        composeRule.onNodeWithText(FIRST_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoListEvent.SwipeFinish(memo.id))
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-017 우에서 좌로 스와이프하면 삭제 행동을 한 번 전달한다`() {
        val memo = memo(title = FIRST_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo)),
            onMemoListEvent = eventList::add,
        )

        composeRule.onNodeWithText(FIRST_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoListEvent.SwipeDelete(memo.id))
    }

    private fun setMemoFinishedListScaffold(
        itemList: List<MemoListItem> = emptyList(),
        onEvent: (MemoFinishedListScaffoldEvent) -> Unit = {},
        onMemoListEvent: (MemoListEvent) -> Unit = {},
        isTodayUpdated: Boolean = false,
    ) {
        val memoPagingData = MutableStateFlow(memoPagingDataOf(itemList))

        composeRule.setContent {
            DiaryTheme {
                MemoFinishedListScaffold(
                    memoListState = remember { MemoListState().apply { if (isTodayUpdated) updateToday() } },
                    snackbarHostState = remember { SnackbarHostState() },
                    memoPagingItems = memoPagingData.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                    onMemoListEvent = onMemoListEvent,
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

    public companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val FIRST_TITLE = "FirstFinishedMemoTitle"
        private const val SECOND_TITLE = "SecondFinishedMemoTitle"
        private const val DEFAULT_TITLE = "Finished Memos"
        private const val KOREAN_TITLE = "완료된 메모"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter"
        private const val DEFAULT_ALL_DAY_HEADER = "Jul 19, 2026"
        private const val DEFAULT_NEXT_DAY_HEADER = "Jul 21, 2026"
        private const val DEFAULT_ALL_DAY_PERIOD = "Jul 19, 2026"
        private const val DEFAULT_TODAY_HEADER = "Today"
        private const val KOREAN_TODAY_HEADER = "오늘"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(
            title: String,
            dateTime: MemoDateTime? = null,
        ): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = dateTime))
                .setExp(Memo::isFinished, true)
                .setExp(Memo::isDeleted, false)
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun dateHeaderMemoItemList(): List<MemoListItem> =
            listOf(
                MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 19)),
                MemoListItem.Content(memo = memo(title = FIRST_TITLE, dateTime = allDay(day = 19))),
                MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 21)),
                MemoListItem.Content(memo = memo(title = SECOND_TITLE, dateTime = dateTime(day = 21, hour = 13))),
            )

        private fun allDay(day: Int): MemoDateTime.AllDay = MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = day)..LocalDate(year = 2026, month = 7, day = day))

        private fun dateTime(
            day: Int,
            hour: Int,
        ): MemoDateTime.DateTime =
            MemoDateTime.DateTime(
                start = LocalDateTime(year = 2026, month = 7, day = day, hour = hour, minute = 30),
                endInclusive = LocalDateTime(year = 2026, month = 7, day = day + 1, hour = hour, minute = 30),
            )
    }
}

private fun dateHeaderMatcher(text: String): SemanticsMatcher = hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(text)
