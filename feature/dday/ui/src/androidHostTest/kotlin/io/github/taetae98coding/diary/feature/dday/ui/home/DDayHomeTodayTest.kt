package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.UpdateDDayListTodayEffect
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class DDayHomeTodayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    // DDayHomeScreen과 같게 오늘 상태를 화면 시작 시점에 다시 정하는 효과와 목록을 함께 구성하고, 기기의 날짜만 테스트가 바꾼다.
    @Test
    fun `TC-af4d3520-a726-4597-af1c-000416eba2c8 화면을 보는 동안 자정이 지나도 카드 문구는 바뀌지 않는다`() {
        val now = mutableStateOf(BEFORE_MIDNIGHT.atStartOfDayIn(TimeZone.currentSystemDefault()))
        val clock = mockk<Clock>()
        every { clock.now() } answers { now.value }
        val listState = DDayListState(initialToday = BEFORE_MIDNIGHT, clock = clock)
        val dday = homeDDayOf(title = DDAY_TITLE) { detail -> detail.copy(baseDate = AFTER_MIDNIGHT) }
        val pagingDataFlow = MutableStateFlow(homeDDayPagingDataOf(listOf(dday)))
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    UpdateDDayListTodayEffect(state = listState)
                    DDayHomeScaffold(
                        onEvent = {},
                        onDDayListEvent = {},
                        listState = listState,
                        ddayPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(DDAY_TITLE) and hasText("D-1")).assertExists()

        composeRule.runOnIdle { now.value = nextDayInstant() }
        composeRule.mainClock.advanceTimeBy(ONE_MINUTE_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(DDAY_TITLE) and hasText("D-1")).assertExists()
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(TODAY_CARD_TEXT)).assertDoesNotExist()
    }

    @Test
    fun `화면이 다시 시작되면 그때의 기기 날짜로 카드 문구를 다시 정한다`() {
        val now = mutableStateOf(BEFORE_MIDNIGHT.atStartOfDayIn(TimeZone.currentSystemDefault()))
        val clock = mockk<Clock>()
        every { clock.now() } answers { now.value }
        val listState = DDayListState(initialToday = BEFORE_MIDNIGHT, clock = clock)
        val dday = homeDDayOf(title = DDAY_TITLE) { detail -> detail.copy(baseDate = AFTER_MIDNIGHT) }
        val pagingDataFlow = MutableStateFlow(homeDDayPagingDataOf(listOf(dday)))
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    UpdateDDayListTodayEffect(state = listState)
                    DDayHomeScaffold(
                        onEvent = {},
                        onDDayListEvent = {},
                        listState = listState,
                        ddayPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    )
                }
            }
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            lifecycleOwner.currentState = Lifecycle.State.CREATED
            now.value = nextDayInstant()
            lifecycleOwner.currentState = Lifecycle.State.RESUMED
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(DDAY_TITLE) and hasText(TODAY_CARD_TEXT)).assertExists()
    }

    private fun nextDayInstant(): Instant = AFTER_MIDNIGHT.atStartOfDayIn(TimeZone.currentSystemDefault())

    private companion object {
        val BEFORE_MIDNIGHT: LocalDate = LocalDate(year = 2026, month = 1, day = 1)
        val AFTER_MIDNIGHT: LocalDate = LocalDate(year = 2026, month = 1, day = 2)
        const val DDAY_TITLE = "Anniversary"
        const val TODAY_CARD_TEXT = "D-Day"
        const val ONE_MINUTE_MILLIS = 60_000L
    }
}
