package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.DELETE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.FINISHED_TEXT
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.RESTART_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 제품 routineEntry로 NavDisplay를 구성해, 한 영역만 쓰는 너비에서 RoutineHome과 완료된 루틴 목록, 루틴 상세 사이의 이동을 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineFinishedListNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineFinishedListTestEnvironment()
    private val robot = RoutineFinishedListRobot(composeRule = composeRule, environment = environment)
    private val backStack: NavBackStack<ScreenNavKey>
        get() = environment.backStack

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineFinishedListToday(TODAY)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseRoutineFinishedListToday()
    }

    @Test
    fun `TC-51c12f84-27d2-43eb-b401-5767a18d2f72 완료된 루틴이 없어도 화면에 진입할 수 있다`() {
        robot.setNavDisplay(routineList = listOf(routine(title = "가", isFinished = false)))

        robot.enterFinishedList()

        backStack.toList() shouldBe listOf(RoutineFinishedListDefaultNavKey, RoutineHomeNavKey, RoutineFinishedListNavKey)
        robot.assertFinishedListShown()
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
    }

    @Test
    fun `TC-75b611f6-faff-4e18-9818-13d8ab87a248 뒤로가기를 사용하면 RoutineHome의 목록 보기로 돌아간다`() {
        robot.setNavDisplay(routineList = listOf(routine(title = "가")))
        robot.enterFinishedList()

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineFinishedListDefaultNavKey, RoutineHomeNavKey)
        robot.assertHomeListView()
    }

    @Test
    fun `TC-75b611f6-faff-4e18-9818-13d8ab87a248 목록 화면의 뒤로가기 동작을 사용하면 RoutineHome의 목록 보기로 돌아간다`() {
        robot.setNavDisplay(routineList = listOf(routine(title = "가")))
        robot.enterFinishedList()

        robot.clickNavigateUp()

        backStack.toList() shouldBe listOf(RoutineFinishedListDefaultNavKey, RoutineHomeNavKey)
        robot.assertHomeListView()
    }

    @Test
    fun `TC-1db6e580-10af-4804-9158-c09f1ed76ca4 루틴을 선택하면 그 루틴의 상세로 이동한다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        robot.enterFinishedList()

        robot.selectRoutine("나")

        backStack.last() shouldBe RoutineDetailNavKey(id = routineList[1].id)
        robot.assertDetail(title = "나")
    }

    @Test
    fun `TC-c82dae3d-3259-4b1d-8e4e-f0896c43293a 한 영역만 쓰는 환경에서는 현재 영역만 표시한다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.enterFinishedList()

        robot.assertFinishedListShown()
        composeRule.onAllNodesWithText(RoutineFinishedListRobot.PLACEHOLDER_MESSAGE).assertCountEquals(0)

        robot.selectRoutine("나")

        robot.assertDetail(title = "나")
        robot.assertFinishedListNotShown()
        composeRule.onNodeWithContentDescription(RoutineFinishedListRobot.NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-2bb70e52-5dcc-4323-b213-dff3a9f9cc3e 루틴 상세에서 돌아오면 목록에서 보던 자리가 그대로다`() {
        robot.setNavDisplay(routineList = manyRoutineList())
        robot.enterFinishedList()
        scrollToLaterRoutine()

        robot.selectRoutine(LATER_TITLE)
        robot.assertDetail(title = LATER_TITLE)
        robot.pressBack()

        assertLaterRoutineShown()
    }

    @Test
    fun `TC-60ab68b1-ca46-45ad-a05b-ade53b6f1e36 상세에서 다시 시작한 뒤 돌아오면 그 루틴이 목록에서 사라진다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        robot.enterFinishedList()
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        composeRule.onNodeWithContentDescription(RESTART_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        robot.pressBack()

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))
        environment.stored(routineList[1]).isFinished shouldBe false
    }

    @Test
    fun `TC-60ab68b1-ca46-45ad-a05b-ade53b6f1e36 상세에서 삭제한 뒤 돌아오면 그 루틴이 목록에서 사라진다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        robot.enterFinishedList()
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        composeRule.onNodeWithContentDescription(DELETE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.last() shouldBe RoutineFinishedListNavKey
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))
        environment.stored(routineList[1]).isDeleted shouldBe true
    }

    @Test
    fun `TC-2d0aeab1-94fe-4682-bdd7-46b387432e0f 화면에 들어오거나 다른 화면에서 돌아오는 것만으로는 새로고침하지 않는다`() {
        robot.setNavDisplay(routineList = threeRoutineList())

        robot.enterFinishedList()
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")
        robot.pressBack()
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나", "다"))

        verify(exactly = 0) { environment.syncViewModel.refresh() }
    }

    @Test
    fun `TC-4a1aab63-ddc5-4917-b0e2-5c8ae89c075e 다시 시작한 루틴은 RoutineHome의 루틴 목록에 목록 순서에 맞는 자리로 나타난다`() {
        val restarted = routine(title = "나")
        robot.setNavDisplay(routineList = listOf(routine(title = "가", isFinished = false), restarted, routine(title = "다", isFinished = false)))
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))
        robot.enterFinishedList()

        composeRule.onNode(routineFinishedListCard("나")).performTouchInput { swipeRight() }
        composeRule.waitUntilRoutineFinishedListGone(routineFinishedListCard("나"))
        robot.pressBack()

        robot.assertHomeListView()
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나", "다"))
        environment.stored(restarted).isFinished shouldBe false
    }

    @Test
    fun `TC-e1a92649-5a7e-4ca4-856f-c0ed093bd43d 루틴 상세에 다녀온 사이 자정이 지나면 진행 상태가 달라진 루틴을 바뀐 자리로 옮긴다`() {
        robot.setNavDisplay(routineList = todayOrderRoutineList())
        robot.enterFinishedList()
        composeRule.waitUntilRoutineFinishedListTitles(listOf("나", "가"))

        robot.selectRoutine("나")
        robot.assertDetail(title = "나")
        composeRule.runOnIdle { setRoutineFinishedListToday(NEXT_DAY) }
        robot.pressBack()

        assertOrderAfterMidnight()
    }

    @Test
    fun `TC-e1a92649-5a7e-4ca4-856f-c0ed093bd43d RoutineHome에 돌아갔다가 다시 진입한 사이 자정이 지나면 진행 상태가 달라진 루틴을 바뀐 자리로 옮긴다`() {
        robot.setNavDisplay(routineList = todayOrderRoutineList())
        robot.enterFinishedList()
        composeRule.waitUntilRoutineFinishedListTitles(listOf("나", "가"))

        robot.pressBack()
        robot.assertHomeListView()
        composeRule.runOnIdle { setRoutineFinishedListToday(NEXT_DAY) }
        robot.enterFinishedList()

        assertOrderAfterMidnight()
    }

    @Test
    fun `TC-660b35df-20bb-4b05-9cfb-215a205f1328 RoutineHome의 목록 보기로 돌아갔다가 다시 진입하면 목록이 맨 위부터 보인다`() {
        robot.setNavDisplay(routineList = manyRoutineList())
        robot.enterFinishedList()
        scrollToLaterRoutine()

        robot.pressBack()
        robot.assertHomeListView()
        robot.enterFinishedList()

        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(FIRST_OF_MANY_TITLE))
        composeRule.onAllNodes(routineFinishedListCard(LATER_TITLE)).assertCountEquals(0)
    }

    private fun assertOrderAfterMidnight() {
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나"))
        composeRule.routineFinishedListCardTextList() shouldBe listOf(listOf("가", FINISHED_TEXT), listOf("나", FINISHED_TEXT))
    }

    private fun scrollToLaterRoutine() {
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(FIRST_OF_MANY_TITLE))
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(LATER_INDEX)
        composeRule.waitForIdle()
        assertLaterRoutineShown()
    }

    private fun assertLaterRoutineShown() {
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(LATER_TITLE))
        composeRule.onAllNodes(routineFinishedListCard(FIRST_OF_MANY_TITLE)).assertCountEquals(0)
        composeRule.onAllNodes(hasText(RoutineFinishedListRobot.FINISHED_LIST_TITLE)).assertCountEquals(1)
        composeRule.onAllNodes(hasContentDescription(RoutineFinishedListRobot.SHOW_CALENDAR_DESCRIPTION)).assertCountEquals(0)
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 7, 15)
        val NEXT_DAY: LocalDate = LocalDate(2026, 7, 16)
        const val MANY_COUNT = 40
        const val LATER_INDEX = 30
        const val FIRST_OF_MANY_TITLE = "Routine 01"
        const val LATER_TITLE = "Routine 31"

        fun routine(
            title: String,
            startDate: LocalDate = LocalDate(2026, 7, 1),
            isFinished: Boolean = true,
        ): Routine = routineFinishedListRoutine(title = title, startDate = startDate, isFinished = isFinished)

        fun threeRoutineList(): List<Routine> = listOf("가", "나", "다").map { title -> routine(title = title) }

        // 2026-07-15에는 `나`가 진행 중, `가`가 시작 전이고, 2026-07-16에는 둘 다 진행 중이다.
        fun todayOrderRoutineList(): List<Routine> = listOf(routine(title = "나"), routine(title = "가", startDate = NEXT_DAY))

        fun manyRoutineList(): List<Routine> = List(MANY_COUNT) { index -> routine(title = "Routine ${(index + 1).toString().padStart(2, '0')}") }
    }
}
