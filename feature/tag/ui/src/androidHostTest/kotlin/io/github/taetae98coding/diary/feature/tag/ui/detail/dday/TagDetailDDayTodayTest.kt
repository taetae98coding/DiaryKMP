package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.UpdateDDayListTodayEffect
import io.github.taetae98coding.diary.feature.tag.ui.tagDDay
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

// TagDetail은 디데이 탭의 오늘을 화면이 시작될 때마다 다시 정하는 Effect와 그 오늘로 카드 문구를 그리는 탭으로 구성한다.
// 기기 날짜를 바꿀 수 없으므로 같은 구성을 날짜를 정할 수 있는 시계로 만들어 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailDDayTodayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val clock = mockk<Clock>()
    private val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
    private var isTagDetailShown by mutableStateOf(true)

    @Test
    fun `TC-19cb0b18-9feb-4662-acf9-6a1f31b5c977 다른 화면에서 돌아오면 바뀐 오늘로 카드 문구를 다시 정한다`() {
        setTodayTab()
        assertCardText(EXAM_TITLE, BEFORE_TEXT)

        composeRule.runOnIdle { isTagDetailShown = false }
        composeRule.waitForIdle()
        setToday(MARCH_20)
        composeRule.runOnIdle { isTagDetailShown = true }
        composeRule.waitForIdle()

        assertCardText(EXAM_TITLE, TODAY_TEXT)
        assertOrder()
    }

    @Test
    fun `TC-19cb0b18-9feb-4662-acf9-6a1f31b5c977 다른 앱에서 돌아오면 바뀐 오늘로 카드 문구를 다시 정한다`() {
        setTodayTab()
        assertCardText(EXAM_TITLE, BEFORE_TEXT)

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        setToday(MARCH_20)
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        assertCardText(EXAM_TITLE, TODAY_TEXT)
        assertOrder()
    }

    @Test
    fun `TC-0b73733f-9181-40c4-80ac-79f226f415de 화면을 보는 동안 자정이 지나도 카드 문구를 다시 정하지 않는다`() {
        setTodayTab()
        assertCardText(EXAM_TITLE, BEFORE_TEXT)

        setToday(MARCH_20)
        composeRule.mainClock.advanceTimeBy(AFTER_MIDNIGHT_MILLIS)
        composeRule.waitForIdle()

        assertCardText(EXAM_TITLE, BEFORE_TEXT)
    }

    private fun setTodayTab() {
        setToday(MARCH_19)
        val state = DDayListState(initialToday = MARCH_19, clock = clock)
        val pagingDataFlow =
            MutableStateFlow(
                tagEntityPagingData(
                    itemList =
                        listOf(
                            tagDDay(title = FIRST_TITLE),
                            tagDDay(title = EXAM_TITLE, baseDate = MARCH_20, countsBaseDay = false),
                        ),
                ),
            )

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    if (isTagDetailShown) {
                        UpdateDDayListTodayEffect(state = state)
                        TagDetailDDayTab(
                            onEvent = {},
                            modifier = Modifier.fillMaxSize(),
                            listState = state,
                            ddayPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun setToday(date: LocalDate) {
        every { clock.now() } returns LocalDateTime(date = date, time = NOON).toInstant(TimeZone.currentSystemDefault())
    }

    private fun assertCardText(
        title: String,
        text: String,
    ) {
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(title) and hasText(text)).assertIsDisplayed()
    }

    private fun assertOrder() {
        val first = composeRule.onNodeWithText(FIRST_TITLE).fetchSemanticsNode().boundsInRoot
        val exam = composeRule.onNodeWithText(EXAM_TITLE).fetchSemanticsNode().boundsInRoot

        // 격자는 왼쪽에서 오른쪽, 위에서 아래 순서로 놓인다.
        (first.top < exam.top || (first.top == exam.top && first.left < exam.left)) shouldBe true
    }

    private companion object {
        const val FIRST_TITLE = "가"
        const val EXAM_TITLE = "수능"
        const val BEFORE_TEXT = "D-1"
        const val TODAY_TEXT = "D-Day"
        const val AFTER_MIDNIGHT_MILLIS = 60_000L
        val MARCH_19 = LocalDate(year = 2026, month = 3, day = 19)
        val MARCH_20 = LocalDate(year = 2026, month = 3, day = 20)
        val NOON = LocalTime(hour = 12, minute = 0)
    }
}
