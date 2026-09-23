package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
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
class MemoHomeScreenTodayHeaderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-HOME-FEATURE-015 시작 날짜가 오늘인 메모의 날짜 헤더에는 오늘 문구가 표시된다`() {
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
    fun `TC-MEMO-HOME-FEATURE-015 한국어 환경에서 오늘 문구를 표시한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setMemoHomeScreen(
            today = today,
            expectedHeader = KOREAN_TODAY_HEADER,
        )

        composeRule.onNode(dateHeader(KOREAN_TODAY_HEADER)).assertIsDisplayed()
        composeRule.onAllNodes(dateHeader(today.toKoreanDisplayText())).assertCountEquals(0)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-013 시작 날짜가 오늘이 아닌 메모의 날짜 헤더에는 날짜가 표시된다`() {
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

    private fun setMemoHomeScreen(
        today: LocalDate,
        expectedHeader: String,
    ) {
        val memo = memo(date = today)
        val viewModel = mockk<MemoHomeViewModel>()
        every { viewModel.sort } returns MutableStateFlow(ListSort.DEFAULT)
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

        private fun LocalDate.toDefaultDisplayText(): String = "${DEFAULT_MONTH_NAMES[month.number - 1]} $day, $year"

        private fun LocalDate.toKoreanDisplayText(): String = "$year. ${month.number}. $day."

        private fun memo(date: LocalDate): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(
                    Memo::detail,
                    fixtureMonkey
                        .giveMeOne<MemoDetail>()
                        .copy(dateTime = MemoDateTime.AllDay(dateRange = date..date)),
                ).setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun dateHeader(text: String): SemanticsMatcher = hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(text)
    }
}
