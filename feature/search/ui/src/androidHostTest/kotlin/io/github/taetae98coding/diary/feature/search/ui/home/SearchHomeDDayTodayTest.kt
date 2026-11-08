package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

// 카드 문구의 오늘은 기기의 현재 시각에서 정해지므로, 시스템 시계를 대신해 날짜가 바뀌는 순간을 만든다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SearchHomeDDayTodayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var today = MARCH_19

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        stopKoin()
        mockkObject(Clock.System)
        every { Clock.System.now() } answers { today.atStartOfDayIn(TimeZone.currentSystemDefault()) + NOON_OFFSET }
    }

    @After
    fun tearDown() {
        unmockkObject(Clock.System)
        stopKoin()
    }

    @Test
    fun `TC-1f86cbf3-de97-4f4c-b791-6ad14d94585e 결과로 연 상세 화면에서 돌아오면 바뀐 오늘로 카드 문구를 다시 정한다`() {
        val isShown = mutableStateOf(true)
        composeRule.setSearchHomeScreen(
            ddayList = examResultList(),
            initialType = SearchHomeType.DDAY,
            isShownProvider = { isShown.value },
        )
        assertCard(title = EXAM_TITLE, countText = "D-1")

        composeRule.runOnIdle { isShown.value = false }
        composeRule.waitForIdle()
        today = MARCH_20
        composeRule.runOnIdle { isShown.value = true }
        composeRule.waitForIdle()

        assertCard(title = EXAM_TITLE, countText = "D-Day")
        cardTitleList() shouldBe listOf(FIRST_TITLE, EXAM_TITLE)
    }

    @Test
    fun `TC-1f86cbf3-de97-4f4c-b791-6ad14d94585e 다른 앱에서 돌아오면 바뀐 오늘로 카드 문구를 다시 정한다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setSearchHomeScreen(
            ddayList = examResultList(),
            initialType = SearchHomeType.DDAY,
            lifecycleOwner = lifecycleOwner,
        )
        assertCard(title = EXAM_TITLE, countText = "D-1")

        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP) }
        today = MARCH_20
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START) }
        composeRule.waitForIdle()

        assertCard(title = EXAM_TITLE, countText = "D-Day")
        cardTitleList() shouldBe listOf(FIRST_TITLE, EXAM_TITLE)
    }

    @Test
    fun `TC-2db6a4aa-ed38-48de-844c-93b767d78231 화면을 보는 동안 자정이 지나도 디데이 카드 문구를 다시 정하지 않는다`() {
        composeRule.setSearchHomeScreen(
            ddayList = examResultList(),
            initialType = SearchHomeType.DDAY,
        )
        assertCard(title = EXAM_TITLE, countText = "D-1")

        today = MARCH_20
        composeRule.mainClock.advanceTimeBy(DATE_CHANGE_WAIT_MILLIS)
        composeRule.waitForIdle()

        assertCard(title = EXAM_TITLE, countText = "D-1")
    }

    @Test
    fun `TC-d80c53cc-1252-47d8-aa6f-c2c794d61614 디데이의 카드 문구를 정하는 값이 바뀌면 바뀐 카드 문구가 바로 반영된다`() {
        val exam = ddayResult(title = EXAM_TITLE, baseDate = MARCH_20)
        composeRule.setSearchHomeScreen(
            ddayList = listOf(exam),
            initialType = SearchHomeType.DDAY,
        )
        assertCard(title = EXAM_TITLE, countText = "D-1")

        composeRule.runOnIdle { updateSearchDDayResult(listOf(exam.copy(detail = exam.detail.copy(baseDate = LocalDate(year = 2026, month = 3, day = 21))))) }
        composeRule.waitForIdle()

        assertCard(title = EXAM_TITLE, countText = "D-2")
    }

    private fun examResultList(): List<DDay> =
        listOf(
            ddayResult(title = FIRST_TITLE, baseDate = LocalDate(year = 2026, month = 2, day = 1)),
            ddayResult(title = EXAM_TITLE, baseDate = MARCH_20),
        )

    private fun ddayResult(
        title: String,
        baseDate: LocalDate,
    ): DDay =
        Arb
            .dday(
                isDeleted = false,
                detail =
                    Arb.ddayDetail().next().copy(
                        title = title,
                        baseDate = baseDate,
                        countsBaseDay = false,
                        repeats = emptySet(),
                        endDate = null,
                    ),
            ).next()

    private fun assertCard(
        title: String,
        countText: String,
    ) {
        composeRule
            .onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(title))
            .assert(hasText(countText))
    }

    private fun cardTitleList(): List<String> =
        composeRule
            .onAllNodesWithTag(DDAY_CARD_TEST_TAG)
            .fetchSemanticsNodes()
            .sortedBy { node -> node.boundsInRoot.top }
            .map { node -> listOf(FIRST_TITLE, EXAM_TITLE).single { title -> hasText(title).matches(node) } }

    private companion object {
        val MARCH_19 = LocalDate(year = 2026, month = 3, day = 19)
        val MARCH_20 = LocalDate(year = 2026, month = 3, day = 20)
        val NOON_OFFSET = 12.hours
        const val FIRST_TITLE = "시험 가"
        const val EXAM_TITLE = "시험 수능"
        const val DATE_CHANGE_WAIT_MILLIS = 120_000L
    }
}
