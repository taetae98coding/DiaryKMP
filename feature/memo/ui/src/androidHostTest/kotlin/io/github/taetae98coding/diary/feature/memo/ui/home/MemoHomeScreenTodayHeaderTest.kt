package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListState
import io.github.taetae98coding.diary.compose.memo.list.UpdateMemoListTodayEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoHomeScreenTodayHeaderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-5a9a4657-63ac-4e59-aa07-aeb42f4f6d7f 시작 날짜가 오늘인 메모의 날짜 헤더에는 오늘 문구가 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setMemoHomeScreen(
            today = today,
            expectedHeader = DEFAULT_TODAY_HEADER,
        )

        composeRule.onNode(dateHeader(DEFAULT_TODAY_HEADER)).assertIsDisplayed()
        composeRule.onAllNodes(dateHeader(today.toDefaultDisplayText())).assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-5a9a4657-63ac-4e59-aa07-aeb42f4f6d7f 한국어 환경에서 오늘 문구를 표시한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setMemoHomeScreen(
            today = today,
            expectedHeader = KOREAN_TODAY_HEADER,
        )

        composeRule.onNode(dateHeader(KOREAN_TODAY_HEADER)).assertIsDisplayed()
        composeRule.onAllNodes(dateHeader(today.toKoreanDisplayText())).assertCountEquals(0)
    }

    @Test
    fun `TC-9b146ec4-d100-4de8-ac7b-64e87092f3f2 시작 날짜가 오늘이 아닌 메모의 날짜 헤더에는 날짜가 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val other = LocalDate(year = today.year - 1, month = 1, day = 2)

        setMemoHomeScreen(
            today = other,
            expectedHeader = other.toDefaultDisplayText(),
        )

        composeRule.onNode(dateHeader(other.toDefaultDisplayText())).assertIsDisplayed()
        composeRule.onAllNodes(dateHeader(DEFAULT_TODAY_HEADER)).assertCountEquals(0)
        composeRule.onAllNodes(dateHeader(today.toDefaultDisplayText())).assertCountEquals(0)
    }

    @Test
    fun `TC-f5de21cd-ccdc-4a33-834d-24019af0ec27 다른 앱에 다녀온 사이 날짜가 바뀌면 복귀 시 오늘 날짜 헤더가 옮겨진다`() {
        val clock = clockAt(date = PREVIOUS_TODAY)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setMemoHomeList(clock = clock, lifecycleOwner = lifecycleOwner)
        awaitDateHeader(DEFAULT_TODAY_HEADER)
        composeRule.onAllNodes(dateHeader(NEW_TODAY.toDefaultDisplayText())).assertCountEquals(1)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { every { clock.now() } returns noonOf(NEW_TODAY) }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        assertTodayHeaderMovedToNewToday()
    }

    @Test
    fun `TC-f5de21cd-ccdc-4a33-834d-24019af0ec27 다른 화면에 다녀온 사이 날짜가 바뀌면 복귀 시 오늘 날짜 헤더가 옮겨진다`() {
        val clock = clockAt(date = PREVIOUS_TODAY)
        val isShown = mutableStateOf(true)
        setMemoHomeList(clock = clock, isShown = isShown)
        awaitDateHeader(DEFAULT_TODAY_HEADER)
        composeRule.onAllNodes(dateHeader(NEW_TODAY.toDefaultDisplayText())).assertCountEquals(1)

        composeRule.runOnIdle { isShown.value = false }
        composeRule.runOnIdle { every { clock.now() } returns noonOf(NEW_TODAY) }
        composeRule.runOnIdle { isShown.value = true }

        assertTodayHeaderMovedToNewToday()
    }

    private fun assertTodayHeaderMovedToNewToday() {
        awaitDateHeader(PREVIOUS_TODAY.toDefaultDisplayText())

        composeRule.onAllNodes(dateHeader(DEFAULT_TODAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodes(dateHeader(PREVIOUS_TODAY.toDefaultDisplayText())).assertCountEquals(1)
        composeRule.onAllNodes(dateHeader(NEW_TODAY.toDefaultDisplayText())).assertCountEquals(0)
        // 오늘 문구는 새 오늘의 메모 바로 위 헤더에 붙는다.
        val todayHeaderTop =
            composeRule
                .onNode(dateHeader(DEFAULT_TODAY_HEADER))
                .fetchSemanticsNode()
                .boundsInRoot.top
        val newTodayMemoTop =
            composeRule
                .onNodeWithText(NEW_TODAY_TITLE)
                .fetchSemanticsNode()
                .boundsInRoot.top
        val previousTodayMemoTop =
            composeRule
                .onNodeWithText(PREVIOUS_TODAY_TITLE)
                .fetchSemanticsNode()
                .boundsInRoot.top
        (todayHeaderTop < newTodayMemoTop && todayHeaderTop > previousTodayMemoTop) shouldBe true
    }

    // 메모 목록 화면이 만드는 목록 상태에 시계를 넣을 수 없으므로, 화면과 같은 방식으로 오늘 갱신과 목록을 직접 구성한다.
    private fun setMemoHomeList(
        clock: Clock,
        lifecycleOwner: TestLifecycleOwner? = null,
        isShown: State<Boolean>? = null,
    ) {
        val memoPagingData =
            MutableStateFlow(
                memoPagingDataOf(
                    itemList =
                        listOf(
                            MemoListItem.DateHeader(date = PREVIOUS_TODAY),
                            MemoListItem.Content(memo = memo(date = PREVIOUS_TODAY, title = PREVIOUS_TODAY_TITLE)),
                            MemoListItem.DateHeader(date = NEW_TODAY),
                            MemoListItem.Content(memo = memo(date = NEW_TODAY, title = NEW_TODAY_TITLE)),
                        ),
                ),
            )

        composeRule.setContent {
            val memoListState = remember { MemoListState(clock = clock) }

            CompositionLocalProvider(LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current)) {
                DiaryTheme {
                    if (isShown?.value != false) {
                        UpdateMemoListTodayEffect(state = memoListState)
                        MemoHomeScaffold(
                            onEvent = {},
                            onMemoListEvent = {},
                            memoListState = memoListState,
                            memoPagingItems = memoPagingData.collectAsLazyPagingItems(),
                        )
                    }
                }
            }
        }
    }

    private fun awaitDateHeader(text: String) {
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(dateHeader(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setMemoHomeScreen(
        today: LocalDate,
        expectedHeader: String,
    ) {
        val memo = memo(date = today)
        val viewModel = mockk<MemoHomeViewModel>()
        every { viewModel.sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
        every { viewModel.memoPagingData } returns
            MutableStateFlow(
                memoPagingDataOf(
                    itemList =
                        listOf(
                            MemoListItem.DateHeader(date = today),
                            MemoListItem.Content(memo = memo),
                        ),
                ),
            )
        every { viewModel.filterUiState } returns MutableStateFlow(MemoHomeScaffoldFilterUiState())
        every { viewModel.effect } returns emptyFlow()

        composeRule.setContent {
            DiaryTheme {
                MemoHomeScreen(
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = {},
                    navigateToFinishedList = {},
                    navigateToSearch = {},
                    listState = rememberLazyListState(),
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                    componentVisibleProvider = { MemoHomeScaffoldComponentVisible() },
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(dateHeader(expectedHeader)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    public companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val DEFAULT_TODAY_HEADER = "Today"
        private const val KOREAN_TODAY_HEADER = "오늘"
        private val DEFAULT_MONTH_NAMES =
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private const val PREVIOUS_TODAY_TITLE = "PreviousTodayMemoTitle"
        private const val NEW_TODAY_TITLE = "NewTodayMemoTitle"
        private val PREVIOUS_TODAY = LocalDate(year = 2026, month = 7, day = 31)
        private val NEW_TODAY = LocalDate(year = 2026, month = 8, day = 1)

        private fun LocalDate.toDefaultDisplayText(): String = "${DEFAULT_MONTH_NAMES[month.number - 1]} $day, $year"

        private fun LocalDate.toKoreanDisplayText(): String = "$year. ${month.number}. $day."

        private fun noonOf(date: LocalDate): Instant = LocalDateTime(date = date, time = LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())

        private fun clockAt(date: LocalDate): Clock = mockk<Clock>().also { clock -> every { clock.now() } returns noonOf(date) }

        private fun memo(
            date: LocalDate,
            title: String? = null,
        ): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(
                    Memo::detail,
                    fixtureMonkey
                        .giveMeOne<MemoDetail>()
                        .let { detail -> if (title == null) detail else detail.copy(title = title) }
                        .copy(dateTime = MemoDateTime.AllDay(dateRange = date..date)),
                ).setExp(Memo::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Memo::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun dateHeader(text: String): SemanticsMatcher = hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(text)
    }
}
