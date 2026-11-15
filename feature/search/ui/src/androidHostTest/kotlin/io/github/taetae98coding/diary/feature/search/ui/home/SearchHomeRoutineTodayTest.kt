package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.resetAndroidUiDispatcher
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
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

// 카드 문구와 순서의 오늘은 기기의 현재 시각에서 정해지므로, 시스템 시계를 대신해 날짜가 바뀌는 순간을 만든다.
// 순서는 화면이 넘긴 오늘로 조회한 결과가 정하므로, 화면이 바뀐 오늘을 넘기면 그 오늘의 조회 결과를 돌려주게 한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class SearchHomeRoutineTodayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var today = MARCH_19
    private val started = routineResult(title = STARTED_TITLE, startDate = LocalDate(year = 2026, month = 3, day = 1))
    private val starting = routineResult(title = STARTING_TITLE, startDate = MARCH_20)

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
    fun `TC-fb0e7fba-331b-4e48-a5b3-b0797711bf95 결과로 연 상세 화면에서 돌아오면 바뀐 오늘로 루틴 카드 문구와 순서를 다시 정한다`() {
        val isShown = mutableStateOf(true)
        composeRule.setSearchHomeScreen(
            routineList = listOf(started, starting),
            initialType = SearchHomeType.ROUTINE,
            isShownProvider = { isShown.value },
        )
        stubNextDayResult()
        assertBeforeReturn()

        composeRule.runOnIdle { isShown.value = false }
        composeRule.waitForIdle()
        today = MARCH_20
        composeRule.runOnIdle { isShown.value = true }
        composeRule.waitForIdle()

        assertAfterReturn()
    }

    @Test
    fun `TC-fb0e7fba-331b-4e48-a5b3-b0797711bf95 다른 앱에서 돌아오면 바뀐 오늘로 루틴 카드 문구와 순서를 다시 정한다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setSearchHomeScreen(
            routineList = listOf(started, starting),
            initialType = SearchHomeType.ROUTINE,
            lifecycleOwner = lifecycleOwner,
        )
        stubNextDayResult()
        assertBeforeReturn()

        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP) }
        today = MARCH_20
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START) }
        composeRule.waitForIdle()

        assertAfterReturn()
    }

    @Test
    fun `TC-d0b9a5ea-0346-4f67-8d8d-0febfbf57a92 화면을 보는 동안 자정이 지나도 루틴 카드 문구와 순서를 다시 정하지 않는다`() {
        composeRule.setSearchHomeScreen(
            routineList = listOf(started, starting),
            initialType = SearchHomeType.ROUTINE,
        )
        stubNextDayResult()
        assertBeforeReturn()

        today = MARCH_20
        composeRule.mainClock.advanceTimeBy(DATE_CHANGE_WAIT_MILLIS)
        composeRule.waitForIdle()

        assertBeforeReturn()
        verify(exactly = 0) { viewModel().updateToday(today = MARCH_20) }
    }

    @Test
    fun `TC-f73cb2dd-5072-4061-98bb-e2a7b6767d4b 오늘이 바뀌어 진행 상태가 달라진 완료된 루틴은 카드 문구가 완료 그대로이고 자리만 바뀐다`() {
        val finishedStarting = starting.copy(routine = starting.routine.copy(isFinished = true))
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setSearchHomeScreen(
            routineList = listOf(started, finishedStarting),
            initialType = SearchHomeType.ROUTINE,
            lifecycleOwner = lifecycleOwner,
        )
        every { viewModel().updateToday(today = MARCH_20) } answers { updateSearchRoutineResult(listOf(finishedStarting, started)) }
        assertCard(title = STARTING_TITLE, text = FINISHED_TEXT)
        cardTitleList() shouldBe listOf(STARTED_TITLE, STARTING_TITLE)

        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP) }
        today = MARCH_20
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START) }
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel().updateToday(today = MARCH_20) }
        assertCard(title = STARTING_TITLE, text = FINISHED_TEXT)
        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(STARTING_TITLE) and hasText(IN_PROGRESS_TEXT)).assertDoesNotExist()
        cardTitleList() shouldBe listOf(STARTING_TITLE, STARTED_TITLE)
    }

    @Test
    fun `TC-fca70df8-6764-4a01-88ad-698663679928 루틴의 기록이 바뀌면 바뀐 카드 문구가 바로 반영된다`() {
        val water =
            routineResult(title = WATER_TITLE, startDate = LocalDate(year = 2026, month = 3, day = 1), unit = "ml", goalAmount = 2000, cycleAmount = 1500)
        composeRule.setSearchHomeScreen(
            routineList = listOf(water),
            initialType = SearchHomeType.ROUTINE,
        )
        assertCard(title = WATER_TITLE, text = "오늘 1500 / 2000ml")
        assertCard(title = WATER_TITLE, text = "75%")

        composeRule.runOnIdle { updateSearchRoutineResult(listOf(water.copy(cycleAmount = 1750))) }
        composeRule.waitForIdle()

        assertCard(title = WATER_TITLE, text = "오늘 1750 / 2000ml")
        assertCard(title = WATER_TITLE, text = "87%")
    }

    private fun stubNextDayResult() {
        every { viewModel().updateToday(today = MARCH_20) } answers { updateSearchRoutineResult(listOf(starting, started)) }
    }

    private fun assertBeforeReturn() {
        verify(exactly = 1) { viewModel().updateToday(today = MARCH_19) }
        assertCard(title = STARTED_TITLE, text = IN_PROGRESS_TEXT)
        assertCard(title = STARTING_TITLE, text = NOT_STARTED_TEXT)
        cardTitleList() shouldBe listOf(STARTED_TITLE, STARTING_TITLE)
    }

    private fun assertAfterReturn() {
        verify(exactly = 1) { viewModel().updateToday(today = MARCH_20) }
        assertCard(title = STARTING_TITLE, text = IN_PROGRESS_TEXT)
        assertCard(title = STARTED_TITLE, text = IN_PROGRESS_TEXT)
        cardTitleList() shouldBe listOf(STARTING_TITLE, STARTED_TITLE)
    }

    private fun viewModel() = searchRoutineViewModelRef.shouldNotBeNull()

    private fun routineResult(
        title: String,
        startDate: LocalDate,
        unit: String = "회",
        goalAmount: Int? = null,
        cycleAmount: Long = 0,
    ): RoutineSummary =
        resultRoutine(title = title).let { summary ->
            summary.copy(
                routine =
                    summary.routine.copy(
                        detail =
                            summary.routine.detail.copy(
                                startDate = startDate,
                                cycle = RoutineCycle.DAILY,
                                unit = unit,
                                goalAmount = goalAmount,
                            ),
                    ),
                cycleAmount = cycleAmount,
            )
        }

    private fun assertCard(
        title: String,
        text: String,
    ) {
        composeRule
            .onNode(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title))
            .assert(hasText(text))
    }

    private fun cardTitleList(): List<String> =
        composeRule
            .onAllNodesWithTag(ROUTINE_CARD_TEST_TAG)
            .fetchSemanticsNodes()
            .sortedBy { node -> node.boundsInRoot.top }
            .map { node -> listOf(STARTED_TITLE, STARTING_TITLE).single { title -> hasText(title).matches(node) } }

    private companion object {
        val MARCH_19 = LocalDate(year = 2026, month = 3, day = 19)
        val MARCH_20 = LocalDate(year = 2026, month = 3, day = 20)
        val NOON_OFFSET = 12.hours
        const val STARTED_TITLE = "운동 나"
        const val STARTING_TITLE = "운동 가"
        const val WATER_TITLE = "물 마시기"
        const val IN_PROGRESS_TEXT = "오늘 0회"
        const val NOT_STARTED_TEXT = "시작 전"
        const val FINISHED_TEXT = "완료"
        const val DATE_CHANGE_WAIT_MILLIS = 120_000L
    }
}
