package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 목록과 상세를 함께 쓰는 배치에서 보기 범위가 목록을 좁혀도 상세 영역이 유지되는지를 제품 routineEntry로 구성한 NavDisplay 위에서 검증한다.
// 보기 범위와 오늘마다 조회가 돌려줄 목록은 테스트가 정해 둔다. 저장된 루틴을 좁히는 조회 자체는 RoutineHomeViewScopeDaoTest가 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class RoutineListDetailViewScopeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineListDetailTestEnvironment(today = WEDNESDAY)
    private val robot = RoutineListDetailRobot(composeRule = composeRule, environment = environment)
    private val backStack: NavBackStack<ScreenNavKey>
        get() = environment.backStack

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineListDetailToday(WEDNESDAY)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseRoutineListDetailToday()
    }

    @Test
    fun `TC-35b8101c-8176-4c8a-9fa9-bdab67779ec0 시작 날짜가 오늘보다 뒤인 루틴을 추가하면 오늘 할 루틴 목록에 나타나지 않는다`() {
        assertAddedRoutineHidden { robot.pickDate(currentText = WEDNESDAY.toRoutineListDetailText(), date = THURSDAY) }

        environment.storedRoutineList
            .single { routine -> routine.detail.title == ADDED_TITLE }
            .detail.startDate shouldBe THURSDAY
    }

    @Test
    fun `TC-35b8101c-8176-4c8a-9fa9-bdab67779ec0 주기가 주말만인 루틴을 추가하면 수요일의 오늘 할 루틴 목록에 나타나지 않는다`() {
        assertAddedRoutineHidden { robot.selectCycle(option = RoutineListDetailRobot.CYCLE_WEEKENDS) }

        environment.storedRoutineList
            .single { routine -> routine.detail.title == ADDED_TITLE }
            .detail.cycle shouldBe RoutineCycle.WEEKENDS
    }

    @Test
    fun `TC-35b8101c-8176-4c8a-9fa9-bdab67779ec0 태그를 고르지 않은 루틴을 추가하면 태그 필터를 적용한 모든 루틴 목록에 나타나지 않는다`() {
        val tagA = Arb.tag(title = ROUTINE_LIST_DETAIL_TAG_TITLE).next().copy(isFinished = false, isDeleted = false)
        val existing = routine(title = "가")
        environment.tagList = listOf(tagA)
        environment.routineTagMap = mapOf(existing.id to setOf(tagA.id))
        environment.setViewScope(viewScope = RoutineViewScope.ALL)
        robot.setNavDisplay(routineList = listOf(existing))
        robot.toggleFilterTag(tagA)
        robot.closeFilter()

        addRoutine {}

        composeRule.onNode(robot.card(ADDED_TITLE)).assertDoesNotExist()
        composeRule.onNode(robot.card("가")).assertExists()
        robot.assertAddOnDetailPane()
        robot.assertAddInitial(today = WEDNESDAY)
    }

    private fun assertAddedRoutineHidden(input: () -> Unit) {
        environment.setHomeTitleList(viewScope = RoutineViewScope.TODAY, today = WEDNESDAY, titleList = listOf("가"))
        robot.setNavDisplay(routineList = listOf(routine(title = "가")))
        environment.storedViewScope shouldBe RoutineViewScope.TODAY

        addRoutine(input)

        environment.storedRoutineList.map { routine -> routine.detail.title } shouldBe listOf("가", ADDED_TITLE)
        robot.cardTitleList(TITLE_LIST) shouldBe listOf("가")
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertAddOnDetailPane()
        robot.assertAddInitial(today = WEDNESDAY)
    }

    private fun addRoutine(input: () -> Unit) {
        robot.titleInput().performTextInput(ADDED_TITLE)
        composeRule.waitForIdle()
        input()
        robot.clickDetailAdd()
        composeRule.waitForIdle()
    }

    @Test
    fun `TC-d72a2c69-279b-4098-a736-b9c752de003d 상세 영역에서 다시 시작한 주말만 루틴은 수요일의 오늘 할 루틴 목록에 나타나지 않는다`() {
        assertRestartedRoutine(finished = routine(title = "나", cycle = RoutineCycle.WEEKENDS, isFinished = true), viewScope = RoutineViewScope.TODAY, expectedTitleList = listOf("가"))
    }

    @Test
    fun `TC-d72a2c69-279b-4098-a736-b9c752de003d 상세 영역에서 다시 시작한 시작 전 루틴은 오늘 할 루틴 목록에 나타나지 않는다`() {
        assertRestartedRoutine(finished = routine(title = "나", startDate = THURSDAY, isFinished = true), viewScope = RoutineViewScope.TODAY, expectedTitleList = listOf("가"))
    }

    @Test
    fun `TC-d72a2c69-279b-4098-a736-b9c752de003d 상세 영역에서 다시 시작한 주말만 루틴은 모든 루틴 목록에 나타난다`() {
        assertRestartedRoutine(finished = routine(title = "나", cycle = RoutineCycle.WEEKENDS, isFinished = true), viewScope = RoutineViewScope.ALL, expectedTitleList = listOf("가", "나"))
    }

    private fun assertRestartedRoutine(
        finished: Routine,
        viewScope: RoutineViewScope,
        expectedTitleList: List<String>,
    ) {
        val recordDate = maxOf(finished.detail.startDate, WEDNESDAY)
        environment.setViewScope(viewScope = viewScope)
        environment.setHomeTitleList(viewScope = viewScope, today = WEDNESDAY, titleList = expectedTitleList)
        robot.setNavDisplay(
            routineList = listOf(routine(title = "가"), finished),
            recordList = listOf(routineListDetailRecord(routine = finished, date = recordDate, amount = 1)),
        )
        robot.showCalendar()
        robot.selectCalendarRecord(title = "나", amountText = "1회")
        robot.showList()
        robot.assertDetailOnDetailPane(finished)
        robot.assertDetailOffersRestart()
        robot.cardTitleList(TITLE_LIST) shouldBe listOf("가")

        robot.clickDetailAction(description = RoutineListDetailRobot.RESTART_DESCRIPTION)

        expectedTitleList.forEach { title -> robot.waitUntilExists(robot.card(title)) }
        robot.cardTitleList(TITLE_LIST) shouldBe expectedTitleList
        backStack.last() shouldBe RoutineDetailNavKey(id = finished.id)
        robot.assertDetailOnDetailPane(finished)
        robot.assertDetailOffersFinish()
    }

    @Test
    fun `TC-ed1582d3-5930-4eb2-986a-a5f3e3fc9186 상세 영역에 루틴 추가가 놓여 있어도 보기 범위를 바꿀 수 있고 적어 둔 제목은 그대로다`() {
        setTodayAndAllPage()
        robot.setNavDisplay(routineList = listOf(routine(title = "가"), routine(title = "나", startDate = THURSDAY)))
        robot.titleInput().performTextInput(ADDED_TITLE)
        composeRule.waitForIdle()

        assertViewScopeChangedOnList()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertAddOnDetailPane()
        robot.titleInput().routineListDetailEditableText() shouldBe ADDED_TITLE
    }

    @Test
    fun `TC-ed1582d3-5930-4eb2-986a-a5f3e3fc9186 상세 영역에 루틴 상세가 놓여 있어도 보기 범위를 바꿀 수 있고 그 상세가 그대로다`() {
        val ga = routine(title = "가")
        setTodayAndAllPage()
        robot.setNavDisplay(routineList = listOf(ga, routine(title = "나", startDate = THURSDAY)))
        robot.selectRoutine(ga)
        robot.assertDetailOnDetailPane(ga)

        assertViewScopeChangedOnList()

        backStack.last() shouldBe RoutineDetailNavKey(id = ga.id)
        robot.assertDetailOnDetailPane(ga)
    }

    private fun setTodayAndAllPage() {
        environment.setHomeTitleList(viewScope = RoutineViewScope.TODAY, today = WEDNESDAY, titleList = listOf("가"))
        environment.setHomeTitleList(viewScope = RoutineViewScope.ALL, today = WEDNESDAY, titleList = listOf("가", "나"))
    }

    private fun assertViewScopeChangedOnList() {
        robot.cardTitleList(TITLE_LIST) shouldBe listOf("가")

        robot.selectViewScope(label = RoutineListDetailRobot.VIEW_SCOPE_ALL)

        robot.assertViewScopeSelected(label = RoutineListDetailRobot.VIEW_SCOPE_ALL)
        robot.waitUntilExists(robot.card("나"))
        robot.cardTitleList(TITLE_LIST) shouldBe listOf("가", "나")

        robot.selectViewScope(label = RoutineListDetailRobot.VIEW_SCOPE_TODAY)

        robot.assertViewScopeSelected(label = RoutineListDetailRobot.VIEW_SCOPE_TODAY)
        robot.waitUntilGone(robot.card("나"))
        robot.cardTitleList(TITLE_LIST) shouldBe listOf("가")
        robot.closeFilter()
    }

    @Test
    fun `TC-90e65a38-d184-4725-83b0-c7e4549998c9 보기 범위를 오늘 할 루틴으로 골라 목록에서 사라져도 상세 영역에는 그 상세가 남는다`() {
        val routineX = routine(title = X_TITLE, startDate = THURSDAY)
        environment.setViewScope(viewScope = RoutineViewScope.ALL)
        environment.setHomeTitleList(viewScope = RoutineViewScope.ALL, today = WEDNESDAY, titleList = listOf(X_TITLE))
        environment.setHomeTitleList(viewScope = RoutineViewScope.TODAY, today = WEDNESDAY, titleList = emptyList())
        robot.setNavDisplay(routineList = listOf(routineX))
        robot.selectRoutine(routineX)

        robot.selectViewScope(label = RoutineListDetailRobot.VIEW_SCOPE_TODAY)
        robot.closeFilter()

        assertDetailKeptWithoutCard(routineX)
    }

    @Test
    fun `TC-90e65a38-d184-4725-83b0-c7e4549998c9 상세 영역에서 시작 날짜를 뒤로 고쳐 목록에서 사라져도 그 상세가 남는다`() {
        val routineX = routine(title = X_TITLE)
        robot.setNavDisplay(routineList = listOf(routineX))
        robot.selectRoutine(routineX)

        robot.pickDate(currentText = routineX.detail.startDate.toRoutineListDetailText(), date = THURSDAY)
        robot.clickDetailUpdate()
        publishEmptyTodayPage()

        assertDetailKeptWithoutCard(routineX)
        environment.storedRoutineList
            .single()
            .detail.startDate shouldBe THURSDAY
    }

    @Test
    fun `TC-90e65a38-d184-4725-83b0-c7e4549998c9 상세 영역에서 주기를 주말만으로 고쳐 목록에서 사라져도 그 상세가 남는다`() {
        val routineX = routine(title = X_TITLE)
        robot.setNavDisplay(routineList = listOf(routineX))
        robot.selectRoutine(routineX)

        robot.selectCycle(option = RoutineListDetailRobot.CYCLE_WEEKENDS)
        robot.clickDetailUpdate()
        publishEmptyTodayPage()

        assertDetailKeptWithoutCard(routineX)
        environment.storedRoutineList
            .single()
            .detail.cycle shouldBe RoutineCycle.WEEKENDS
    }

    @Test
    fun `TC-90e65a38-d184-4725-83b0-c7e4549998c9 오늘을 다시 정해 평일만 루틴이 목록에서 사라져도 상세 영역에는 그 상세가 남는다`() {
        val friday = LocalDate(year = 2026, month = 7, day = 10)
        val saturday = LocalDate(year = 2026, month = 7, day = 11)
        setRoutineListDetailToday(friday)
        val routineX = routine(title = X_TITLE, cycle = RoutineCycle.WEEKDAYS)
        environment.setHomeTitleList(viewScope = RoutineViewScope.TODAY, today = friday, titleList = listOf(X_TITLE))
        environment.setHomeTitleList(viewScope = RoutineViewScope.TODAY, today = saturday, titleList = emptyList())
        robot.setNavDisplay(routineList = listOf(routineX))
        robot.selectRoutine(routineX)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        setRoutineListDetailToday(saturday)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertDetailKeptWithoutCard(routineX)
    }

    // 수정이 저장되면 열어 둔 오늘 할 루틴 조회가 그 루틴을 뺀 결과를 이어서 돌려준다.
    private fun publishEmptyTodayPage() {
        composeRule.runOnIdle { environment.setHomeTitleList(viewScope = RoutineViewScope.TODAY, today = WEDNESDAY, titleList = emptyList()) }
        composeRule.waitForIdle()
    }

    private fun assertDetailKeptWithoutCard(routine: Routine) {
        robot.waitUntilGone(robot.card(routine.detail.title))
        backStack.last() shouldBe RoutineDetailNavKey(id = routine.id)
        robot.assertDetailOnDetailPane(routine)
    }

    // 따로 정하지 않으면 2026-07-15에 진행 중인 완료되지 않은 매일 루틴이다.
    private fun routine(
        title: String,
        cycle: RoutineCycle = RoutineCycle.DAILY,
        startDate: LocalDate = START_DATE,
        isFinished: Boolean = false,
    ): Routine = routineListDetailRoutine(title = title, today = WEDNESDAY, cycle = cycle, startDate = startDate, isFinished = isFinished)

    private companion object {
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        val WEDNESDAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val THURSDAY: LocalDate = LocalDate(year = 2026, month = 7, day = 16)
        const val ADDED_TITLE = "추가한 루틴"
        const val X_TITLE = "루틴 X"
        val TITLE_LIST = listOf("가", "나", ADDED_TITLE, X_TITLE)
    }
}
