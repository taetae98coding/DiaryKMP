package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.ui.home.record.ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG
import io.kotest.matchers.floats.shouldBeLessThan
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

// 목록과 상세를 함께 쓰는 배치에서는 상세 영역의 화면이 바뀌어도 목록이 화면에 남아 있으므로, 제품 routineEntry로 구성한 배치에서 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class RoutineHomeTodayListDetailTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineListDetailTestEnvironment(today = BEFORE_MIDNIGHT)
    private val robot = RoutineListDetailRobot(composeRule = composeRule, environment = environment)

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineListDetailToday(BEFORE_MIDNIGHT)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseRoutineListDetailToday()
    }

    @Test
    fun `TC-e8d9b607-73fa-4e8d-8271-0224de6b1a02 목록과 상세를 함께 쓰는 동안 상세 영역의 화면이 바뀌어도 오늘을 다시 정하지 않는다`() {
        val starting = startingRoutine()
        val other = routineListDetailRoutine(title = OTHER_TITLE, today = BEFORE_MIDNIGHT)
        environment.setViewScope(viewScope = RoutineViewScope.ALL)
        robot.setNavDisplay(routineList = listOf(starting, other))
        robot.assertAddOnDetailPane()
        composeRule.onNode(robot.card(title = STARTING_TITLE, cardText = NOT_STARTED_TEXT)).assertExists()

        setRoutineListDetailToday(AFTER_MIDNIGHT)
        robot.selectRoutine(other)
        environment.backStack.last() shouldBe RoutineDetailNavKey(id = other.id)
        robot.assertDetailOnDetailPane(other)
        robot.pressBack()
        robot.assertAddOnDetailPane()

        composeRule.onNode(robot.card(title = STARTING_TITLE, cardText = NOT_STARTED_TEXT)).assertExists()
        composeRule.onNode(robot.card(title = STARTING_TITLE, cardText = TODAY_ZERO_TEXT)).assertDoesNotExist()
    }

    // 필터의 태그 추가로 TagAdd에 다녀오는 경로는 다른 화면이 목록을 가리는 한 영역 환경에서 지난다.
    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h891dp")
    fun `TC-115d314d-eb03-4b9a-85ed-643f736e6751 필터의 태그 추가로 TagAdd에 다녀오면 오늘을 다시 정한다`() {
        val started = routineListDetailRoutine(title = STARTED_TITLE, today = BEFORE_MIDNIGHT).let { routine -> routine.copy(detail = routine.detail.copy(startDate = STARTED_START_DATE)) }
        val starting = startingRoutine()
        environment.setViewScope(viewScope = RoutineViewScope.ALL)
        robot.setNavDisplay(
            routineList = listOf(started, starting),
            recordList = listOf(routineListDetailRecord(routine = started, date = BEFORE_MIDNIGHT, amount = 1)),
        )
        composeRule.onNode(robot.card(title = STARTED_TITLE, cardText = TODAY_ONE_TEXT)).assertExists()
        composeRule.onNode(robot.card(title = STARTING_TITLE, cardText = NOT_STARTED_TEXT)).assertExists()
        composeRule.onNode(quickRecordButton(STARTED_TITLE)).assertExists()
        composeRule.onNode(quickRecordButton(STARTING_TITLE)).assertDoesNotExist()
        cardTop(STARTED_TITLE) shouldBeLessThan cardTop(STARTING_TITLE)

        composeRule.onNodeWithContentDescription(FILTER_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        robot.waitUntilExists(hasText(TAG_FILTER_ADD_LABEL))
        composeRule.onNodeWithText(TAG_FILTER_ADD_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_TAG_ADD_CONTENT).assertExists()
        setRoutineListDetailToday(AFTER_MIDNIGHT)
        robot.pressBack()

        robot.waitUntilExists(robot.card(title = STARTING_TITLE, cardText = TODAY_ZERO_TEXT))
        composeRule.onNode(robot.card(title = STARTED_TITLE, cardText = TODAY_ZERO_TEXT)).assertExists()
        composeRule.onNode(quickRecordButton(STARTING_TITLE)).assertExists()
        composeRule.onNode(quickRecordButton(STARTED_TITLE)).assertExists()
        cardTop(STARTING_TITLE) shouldBeLessThan cardTop(STARTED_TITLE)
    }

    private fun quickRecordButton(title: String): SemanticsMatcher = hasTestTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG) and hasAnyAncestor(robot.card(title))

    private fun cardTop(title: String): Float =
        composeRule
            .onNode(robot.card(title))
            .fetchSemanticsNode()
            .boundsInRoot.top

    private fun startingRoutine(): Routine {
        val routine = routineListDetailRoutine(title = STARTING_TITLE, today = BEFORE_MIDNIGHT)

        return routine.copy(detail = routine.detail.copy(startDate = AFTER_MIDNIGHT))
    }

    private companion object {
        val BEFORE_MIDNIGHT: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val AFTER_MIDNIGHT: LocalDate = LocalDate(year = 2026, month = 7, day = 16)
        val STARTED_START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        const val STARTING_TITLE = "A-starting-routine"
        const val STARTED_TITLE = "B-started-routine"
        const val OTHER_TITLE = "C-other-routine"
        const val NOT_STARTED_TEXT = "Not started"
        const val FILTER_DESCRIPTION = "Filter"
        const val TAG_FILTER_ADD_LABEL = "Add tag"
        const val TODAY_ZERO_TEXT = "Today 0회"
        const val TODAY_ONE_TEXT = "Today 1회"
    }
}
