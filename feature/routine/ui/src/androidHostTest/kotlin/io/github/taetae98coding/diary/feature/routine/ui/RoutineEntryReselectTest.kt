package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 루틴 목적지를 다시 선택한 신호가 제품 routineEntry를 거쳐 RoutineHome에 전달되는지를, 루틴 화면만 표시하는 너비에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineEntryReselectTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineListDetailTestEnvironment(today = TODAY)
    private val robot = RoutineListDetailRobot(composeRule = composeRule, environment = environment)

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineListDetailToday(TODAY)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseRoutineListDetailToday()
    }

    @Test
    fun `TC-6d6ea720-76ed-46dc-bc47-d20dcc847db7 목록 보기의 루틴 목적지를 다시 선택하면 목록의 첫 항목으로 돌아간다`() {
        val routineList = List(ROUTINE_COUNT) { index -> routine(title = "routine-${index.toString().padStart(2, '0')}") }
        robot.setNavDisplay(routineList = routineList)
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(routineList.lastIndex)
        composeRule.waitForIdle()
        composeRule.onNode(robot.card(routineList.first().detail.title)).assertIsNotDisplayed()

        environment.reselectEvent.tryEmit(Unit) shouldBe true
        composeRule.waitForIdle()

        composeRule.onNode(robot.card(routineList.first().detail.title)).assertIsDisplayed()
        robot.assertListView()
        environment.backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
    }

    @Test
    fun `TC-55e9fe36-6c0d-443b-a1d9-e2ce4bc980df 달력 보기의 루틴을 다시 선택하면 보기는 그대로 두고 오늘이 속한 달로 돌아간다`() {
        robot.setNavDisplay(routineList = listOf(routine(title = "routine")))
        robot.showCalendar()
        composeRule.onNodeWithText(TODAY_MONTH_TITLE).assertIsDisplayed()
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(NEXT_MONTH_TITLE).assertIsDisplayed()

        environment.reselectEvent.tryEmit(Unit) shouldBe true
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TODAY_MONTH_TITLE).assertIsDisplayed()
        robot.assertCalendarView()
        environment.backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
    }

    @Test
    fun `TC-e8c22e00-ba72-44a3-af1b-6bae10c5d492 첫 항목이 보이는 목록 보기의 루틴을 다시 선택하면 보던 자리가 그대로다`() {
        val routineList = List(ROUTINE_COUNT) { index -> routine(title = "routine-${index.toString().padStart(2, '0')}") }
        robot.setNavDisplay(routineList = routineList)
        composeRule.onNode(robot.card(routineList.first().detail.title)).assertIsDisplayed()

        environment.reselectEvent.tryEmit(Unit) shouldBe true
        composeRule.waitForIdle()

        composeRule.onNode(robot.card(routineList.first().detail.title)).assertIsDisplayed()
        robot.assertListView()
        environment.backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
    }

    @Test
    fun `TC-e8c22e00-ba72-44a3-af1b-6bae10c5d492 오늘이 속한 달을 보는 달력 보기의 루틴을 다시 선택하면 보던 달이 그대로다`() {
        robot.setNavDisplay(routineList = listOf(routine(title = "routine")))
        robot.showCalendar()
        composeRule.onNodeWithText(TODAY_MONTH_TITLE).assertIsDisplayed()

        environment.reselectEvent.tryEmit(Unit) shouldBe true
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TODAY_MONTH_TITLE).assertIsDisplayed()
        robot.assertCalendarView()
        environment.backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        const val TODAY_MONTH_TITLE = "July 2026"
        const val NEXT_MONTH_TITLE = "August 2026"
        const val ROUTINE_COUNT = 30

        fun routine(title: String): Routine = routineListDetailRoutine(title = title, today = TODAY)
    }
}
