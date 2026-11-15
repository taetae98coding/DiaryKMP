package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.VIEW_SCOPE_ALL
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.VIEW_SCOPE_TODAY
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

// 필터에서 고른 보기 범위가 목록에 적용되고 경계를 지나도 이어지는지를, 제품 routineEntry로 띄운 목록과 필터 화면에서 확인한다.
// 보기 범위와 오늘마다 조회가 돌려줄 목록은 테스트가 정해 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeViewScopeFilterTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineListDetailTestEnvironment(today = TODAY)
    private val robot = RoutineListDetailRobot(composeRule = composeRule, environment = environment)
    private val routineList: List<Routine> =
        listOf(
            routineListDetailRoutine(title = IN_PROGRESS_TITLE, today = TODAY),
            routineListDetailRoutine(title = NOT_STARTED_TITLE, today = TODAY, startDate = TOMORROW),
        )
    private var launchCount by mutableIntStateOf(0)

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineListDetailToday(TODAY)
        environment.setHomeTitleList(viewScope = RoutineViewScope.TODAY, today = TODAY, titleList = listOf(IN_PROGRESS_TITLE))
        environment.setHomeTitleList(viewScope = RoutineViewScope.ALL, today = TODAY, titleList = listOf(IN_PROGRESS_TITLE, NOT_STARTED_TITLE))
        environment.setRoutineList(routineList = routineList)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseRoutineListDetailToday()
    }

    @Test
    fun `TC-829e32df-b32a-4101-812c-58ee288ec037 목록 보기와 달력 보기에서 필터를 열면 태그 필터와 함께 오늘 할 루틴이 골라진 보기 범위가 있다`() {
        setContent()

        assertInitialFilter()
        robot.closeFilter()
        robot.showCalendar()

        assertInitialFilter()
    }

    private fun assertInitialFilter() {
        robot.assertViewScopeSelected(label = VIEW_SCOPE_TODAY)
        composeRule.onNodeWithText(TAG_FILTER_TITLE).assertExists()
        composeRule.onNodeWithText(TAG_ADD_LABEL).assertExists()
        environment.storedViewScope shouldBe RoutineViewScope.TODAY
    }

    @Test
    fun `TC-5d746b2b-5de4-4886-b84a-fcee31664391 달력 보기에서 바꾼 보기 범위는 목록 보기에 적용되어 있고 보기를 바꿔도 그대로다`() {
        setContent()
        robot.showCalendar()

        robot.selectViewScope(label = VIEW_SCOPE_ALL)
        robot.closeFilter()
        robot.showList()

        assertAllScopeKept()

        robot.showCalendar()
        robot.showList()

        assertAllScopeKept()
    }

    @Test
    fun `TC-0bff665b-1451-463f-886f-9e8bd7d230ec 필터를 닫았다가 다시 열어도 보기 범위 선택이 이어진다`() {
        setContent()

        selectAllScope()

        assertAllScopeKept()
    }

    @Test
    fun `TC-0bff665b-1451-463f-886f-9e8bd7d230ec 화면이 재생성되어도 보기 범위 선택이 이어진다`() {
        val viewModelStoreOwner = routineListDetailViewModelStoreOwner()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { environment.Content(viewModelStoreOwner = viewModelStoreOwner) }
        selectAllScope()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertAllScopeKept()
    }

    @Test
    fun `TC-0bff665b-1451-463f-886f-9e8bd7d230ec 앱이 백그라운드에 다녀와도 보기 범위 선택이 이어진다`() {
        setContent()
        selectAllScope()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertAllScopeKept()
    }

    @Test
    fun `TC-0bff665b-1451-463f-886f-9e8bd7d230ec 루틴 상세에 다녀와도 보기 범위 선택이 이어진다`() {
        setContent()
        selectAllScope()

        robot.selectRoutine(routineList.first())
        robot.waitUntilGone(robot.card(NOT_STARTED_TITLE))
        robot.pressBack()

        assertAllScopeKept()
    }

    @Test
    fun `TC-0bff665b-1451-463f-886f-9e8bd7d230ec 다른 주요 목적지에 다녀와도 보기 범위 선택이 이어진다`() {
        setContent()
        selectAllScope()

        robot.navigateToOtherTopLevel()
        robot.navigateToRoutineTopLevel()

        assertAllScopeKept()
    }

    @Test
    fun `TC-0bff665b-1451-463f-886f-9e8bd7d230ec 앱을 다시 실행해도 저장된 보기 범위 선택이 이어진다`() {
        // 앱을 다시 실행하면 화면 상태와 ViewModel이 새로 만들어지고, 기기에 저장된 선택만 남는다.
        composeRule.setContent {
            key(launchCount) {
                environment.Content(viewModelStoreOwner = remember { routineListDetailViewModelStoreOwner() })
            }
        }
        selectAllScope()

        composeRule.runOnIdle { launchCount += 1 }
        robot.navigateToRoutineTopLevel()

        assertAllScopeKept()
        environment.homeViewModelList.size shouldBe 2
    }

    private fun setContent() {
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
    }

    // 필터에서 `모든 루틴`을 고르고 필터를 닫는다.
    private fun selectAllScope() {
        robot.waitUntilExists(robot.card(IN_PROGRESS_TITLE))
        robot.cardTitleList(TITLE_LIST) shouldBe listOf(IN_PROGRESS_TITLE)
        robot.selectViewScope(label = VIEW_SCOPE_ALL)
        robot.closeFilter()
        robot.waitUntilExists(robot.card(NOT_STARTED_TITLE))
    }

    // 목록에 두 루틴이 표시되고, 필터를 다시 열면 `모든 루틴`이 골라져 있다.
    private fun assertAllScopeKept() {
        robot.waitUntilExists(robot.card(NOT_STARTED_TITLE))
        robot.cardTitleList(TITLE_LIST) shouldBe listOf(IN_PROGRESS_TITLE, NOT_STARTED_TITLE)
        robot.assertViewScopeSelected(label = VIEW_SCOPE_ALL)
        robot.closeFilter()
        environment.storedViewScope shouldBe RoutineViewScope.ALL
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val TOMORROW: LocalDate = LocalDate(year = 2026, month = 7, day = 16)
        const val IN_PROGRESS_TITLE = "가"
        const val NOT_STARTED_TITLE = "나"
        const val TAG_FILTER_TITLE = "Tag Filter"
        const val TAG_ADD_LABEL = "Add tag"
        val TITLE_LIST = listOf(IN_PROGRESS_TITLE, NOT_STARTED_TITLE)
    }
}
