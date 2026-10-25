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
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListState
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
    fun `TC-ee7d7010-984c-49c6-937d-304f78fa4943 목록에 완료된 메모 제목이 카드로 표시된다`() {
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
    fun `TC-5e6fdbd1-bad5-4bd1-989e-8e17a2fe5856 기간이 있는 메모에는 기간이 함께 표시된다`() {
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE, dateTime = allDay(day = 19)))),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_ALL_DAY_PERIOD).assertExists()
    }

    @Test
    fun `TC-011800e2-b60f-4a6e-b17f-39788a066432 기간이 없는 메모에는 기간이 표시되지 않는다`() {
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_ALL_DAY_PERIOD).assertDoesNotExist()
    }

    @Test
    fun `TC-7783a8b4-a3f6-4c2e-84f3-a3930e0e07ab 이 화면에는 태그 필터 버튼을 표시하지 않는다`() {
        setMemoFinishedListScaffold(itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-787c7488-3765-4e41-bcaa-131a1020fb7b 이 화면에는 메모 추가 버튼을 표시하지 않는다`() {
        setMemoFinishedListScaffold(itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-3699fead-e7af-4daf-b0a4-98de4ba4e490 기간이 있는 메모의 카드 위에 시작 날짜 헤더가 표시된다`() {
        setMemoFinishedListScaffold(itemList = dateHeaderMemoItemList())

        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_ALL_DAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_NEXT_DAY_HEADER)).assertCountEquals(1)
    }

    @Test
    fun `TC-8068ed89-cbd3-49ee-bcc2-d877c3d6c82f 시작 날짜가 같은 메모들에는 날짜 헤더가 하나만 표시된다`() {
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
    fun `TC-0ce1eee3-98c3-4f35-8247-b4401e1027ae 기간이 없는 메모에는 날짜 헤더가 표시되지 않는다`() {
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo(title = FIRST_TITLE))),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onAllNodesWithTag(MEMO_DATE_HEADER_TEST_TAG).assertCountEquals(0)
    }

    @Test
    fun `TC-09809dd0-333f-465a-8f4e-98c53aedb5f5 시작 날짜가 오늘인 메모의 날짜 헤더에는 오늘 문구가 표시된다`() {
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
    fun `TC-09809dd0-333f-465a-8f4e-98c53aedb5f5 한국어 환경에서 오늘 문구를 표시한다`() {
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
    fun `TC-698aba55-0311-40af-ba80-ac00ecfeb0ce 메모 카드를 선택하면 해당 메모의 상세 화면 전환 행동을 한 번 전달한다`() {
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
    fun `TC-d65fbf7b-d699-42bf-8449-22d55dd61b1f 뒤로가기 버튼을 선택하면 뒤로가기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<MemoFinishedListScaffoldEvent>()
        setMemoFinishedListScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(MemoFinishedListScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `TC-1ef63eda-de50-40a0-a6a6-11efc71536e3 좌에서 우로 스와이프하면 다시 시작 행동을 한 번 전달한다`() {
        val memo = memo(title = FIRST_TITLE)
        val eventList = mutableListOf<MemoListEvent>()
        setMemoFinishedListScaffold(
            itemList = listOf(MemoListItem.Content(memo = memo)),
            onMemoListEvent = eventList::add,
        )

        composeRule.onNodeWithText(FIRST_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoListEvent.SwipeRestart(memo.id))
    }

    @Test
    fun `TC-72adc9f7-a269-41ef-a86c-ba35e313d143 우에서 좌로 스와이프하면 삭제 행동을 한 번 전달한다`() {
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
                .setExp(Memo::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Memo::createdAt, fixtureMonkey.giveMeOne<Instant>())
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
