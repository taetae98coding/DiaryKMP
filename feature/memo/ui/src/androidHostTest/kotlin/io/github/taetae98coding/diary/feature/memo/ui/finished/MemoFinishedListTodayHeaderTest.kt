package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
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
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
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
class MemoFinishedListTodayHeaderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-d11d7fe5-0195-4aa7-b417-4b36e897b183 다른 앱에 다녀온 사이 날짜가 바뀌면 복귀 시 오늘 날짜 헤더가 옮겨진다`() {
        val clock = clockAt(date = PREVIOUS_TODAY)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setMemoFinishedList(clock = clock, lifecycleOwner = lifecycleOwner)
        awaitDateHeader(TODAY_HEADER)
        composeRule.onAllNodes(dateHeader(NEW_TODAY_HEADER)).assertCountEquals(1)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { every { clock.now() } returns noonOf(NEW_TODAY) }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        assertTodayHeaderMovedToNewToday()
    }

    @Test
    fun `TC-d11d7fe5-0195-4aa7-b417-4b36e897b183 다른 화면에 다녀온 사이 날짜가 바뀌면 복귀 시 오늘 날짜 헤더가 옮겨진다`() {
        val clock = clockAt(date = PREVIOUS_TODAY)
        val isShown = mutableStateOf(true)
        setMemoFinishedList(clock = clock, isShown = isShown)
        awaitDateHeader(TODAY_HEADER)
        composeRule.onAllNodes(dateHeader(NEW_TODAY_HEADER)).assertCountEquals(1)

        composeRule.runOnIdle { isShown.value = false }
        composeRule.runOnIdle { every { clock.now() } returns noonOf(NEW_TODAY) }
        composeRule.runOnIdle { isShown.value = true }

        assertTodayHeaderMovedToNewToday()
    }

    private fun assertTodayHeaderMovedToNewToday() {
        awaitDateHeader(PREVIOUS_TODAY_HEADER)

        composeRule.onAllNodes(dateHeader(TODAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodes(dateHeader(PREVIOUS_TODAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodes(dateHeader(NEW_TODAY_HEADER)).assertCountEquals(0)
        // 오늘 문구는 새 오늘의 메모 바로 위 헤더에 붙는다.
        val todayHeaderTop =
            composeRule
                .onNode(dateHeader(TODAY_HEADER))
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

    // 완료 메모 목록 화면이 만드는 목록 상태에 시계를 넣을 수 없으므로, 화면과 같은 방식으로 오늘 갱신과 목록을 직접 구성한다.
    private fun setMemoFinishedList(
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
                            MemoListItem.Content(memo = finishedMemo(date = PREVIOUS_TODAY, title = PREVIOUS_TODAY_TITLE)),
                            MemoListItem.DateHeader(date = NEW_TODAY),
                            MemoListItem.Content(memo = finishedMemo(date = NEW_TODAY, title = NEW_TODAY_TITLE)),
                        ),
                ),
            )

        composeRule.setContent {
            val memoListState = remember { MemoListState(clock = clock) }

            CompositionLocalProvider(LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current)) {
                DiaryTheme {
                    if (isShown?.value != false) {
                        UpdateMemoListTodayEffect(state = memoListState)
                        MemoFinishedListScaffold(
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

    private companion object {
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        const val TODAY_HEADER = "Today"
        const val PREVIOUS_TODAY_HEADER = "Jul 31, 2026"
        const val NEW_TODAY_HEADER = "Aug 1, 2026"
        const val PREVIOUS_TODAY_TITLE = "PreviousTodayFinishedMemoTitle"
        const val NEW_TODAY_TITLE = "NewTodayFinishedMemoTitle"
        val PREVIOUS_TODAY = LocalDate(year = 2026, month = 7, day = 31)
        val NEW_TODAY = LocalDate(year = 2026, month = 8, day = 1)
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun noonOf(date: LocalDate): Instant = LocalDateTime(date = date, time = LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())

        fun clockAt(date: LocalDate): Clock = mockk<Clock>().also { clock -> every { clock.now() } returns noonOf(date) }

        fun finishedMemo(
            date: LocalDate,
            title: String,
        ): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(
                    Memo::detail,
                    fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = MemoDateTime.AllDay(dateRange = date..date)),
                ).setExp(Memo::isFinished, true)
                .setExp(Memo::isDeleted, false)
                .setExp(Memo::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Memo::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        fun dateHeader(text: String): SemanticsMatcher = hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(text)
    }
}
