package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.routineNavKeys
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.CYCLE_WEEKLY
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.UNIT_MILLILITER
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 상세 선택과 상세 영역 루틴 추가의 작성 내용이 유지되고 초기화되는 경계를 제품 routineEntry로 구성한 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class RoutineListDetailRetentionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineListDetailTestEnvironment(today = TODAY)
    private val robot = RoutineListDetailRobot(composeRule = composeRule, environment = environment)
    private val backStack: NavBackStack<ScreenNavKey>
        get() = environment.backStack

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
    fun `TC-07274212-453a-4d61-9936-d0f1012cf33c 다른 주요 목적지에 다녀오면 상세 선택이 초기화되고 뒤로가면 기본 목적지로 돌아간다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.selectRoutine(routine)
        robot.assertDetailOnDetailPane(routine)

        robot.navigateToOtherTopLevel()
        robot.navigateToRoutineTopLevel()

        robot.waitUntilCard(routine)
        robot.assertAddOnDetailPane()

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey)
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_DEFAULT_CONTENT).assertExists()
    }

    @Test
    fun `TC-0a891a10-77b6-4c18-a2da-f35c15958d10 다른 주요 목적지에 다녀오면 상세 영역의 루틴 추가는 처음 상태로 시작한다`() {
        robot.setNavDisplay(routineList = listOf(routine(title = FIRST_TITLE)))
        robot.pickDate(currentText = TODAY_TEXT, date = PLACEHOLDER_START_DATE)
        fillPlaceholder(withTitle = false)

        robot.navigateToOtherTopLevel()
        robot.navigateToRoutineTopLevel()

        robot.assertAddOnDetailPane()
        robot.assertAddInitial(today = TODAY)
        environment.addTagViewModelList shouldHaveSize 2
    }

    @Test
    fun `TC-ad14b913-2822-4211-9d3e-6a6d3c523c89 목록에서 고른 루틴 상세를 보고 돌아와도 루틴 추가의 작성 내용이 남는다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        fillPlaceholder(withTitle = true)

        robot.selectRoutine(routine)
        robot.assertDetailOnDetailPane(routine)
        robot.pressBack()

        assertPlaceholderRetained(withTitle = true)
    }

    @Test
    fun `TC-ad14b913-2822-4211-9d3e-6a6d3c523c89 달력에서 고른 루틴 상세를 보고 돌아와도 루틴 추가의 작성 내용이 남는다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(
            routineList = listOf(routine),
            recordList = listOf(routineListDetailRecord(routine = routine, date = TODAY, amount = RECORD_AMOUNT)),
        )
        fillPlaceholder(withTitle = true)

        robot.showCalendar()
        robot.selectCalendarRecord(title = FIRST_TITLE, amountText = RECORD_AMOUNT_TEXT)
        robot.assertDetailOnDetailPane(routine)
        robot.pressBack()

        robot.assertCalendarView()
        assertPlaceholderRetained(withTitle = true)
    }

    @Test
    fun `TC-304f05ac-ca03-405f-a7ef-687f34a31ecb 목록에서 연 루틴 추가를 뒤로가기로 닫아도 선택 전 루틴 추가의 작성 내용이 남는다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        fillTitleAndGoalAmount()
        openAddOnDetail(routine)
        robot.assertAddInitial(today = TODAY)

        robot.pressBack()
        robot.assertDetailOnDetailPane(routine)
        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        assertTitleAndGoalAmountRetained()
    }

    @Test
    fun `TC-304f05ac-ca03-405f-a7ef-687f34a31ecb 목록에서 연 루틴 추가를 다른 루틴을 골라 걷어내도 선택 전 루틴 추가의 작성 내용이 남는다`() {
        val first = routine(title = FIRST_TITLE)
        val second = routine(title = SECOND_TITLE)
        robot.setNavDisplay(routineList = listOf(first, second))
        fillTitleAndGoalAmount()
        openAddOnDetail(first)
        robot.assertAddInitial(today = TODAY)

        robot.selectRoutine(second)
        robot.assertDetailOnDetailPane(second)
        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        assertTitleAndGoalAmountRetained()
    }

    @Test
    fun `TC-d86ad921-51e7-48c3-bba4-7688b7da4eb0 화면이 재생성되어도 상세 선택을 유지한다`() {
        val routine = routine(title = FIRST_TITLE)
        environment.setRoutineList(routineList = listOf(routine))
        val restorationTester = StateRestorationTester(composeRule)
        val viewModelStoreOwner = routineListDetailViewModelStoreOwner()
        lateinit var restoredBackStack: NavBackStack<ScreenNavKey>
        restorationTester.setContent {
            val restorableBackStack =
                rememberSerializable(
                    configuration = routineSavedStateConfiguration,
                    serializer = NavBackStackSerializer(PolymorphicSerializer(ScreenNavKey::class)),
                ) { NavBackStack<ScreenNavKey>(RoutineHomeNavKey) }
            restoredBackStack = restorableBackStack
            environment.Content(navBackStack = restorableBackStack, viewModelStoreOwner = viewModelStoreOwner)
        }
        robot.selectRoutine(routine)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        restoredBackStack.toList() shouldBe listOf(RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))
        robot.waitUntilCard(routine)
        robot.assertDetailOnDetailPane(routine)
        robot.pressBack()
        restoredBackStack.toList() shouldBe listOf(RoutineHomeNavKey)
        robot.assertAddOnDetailPane()
    }

    @Test
    fun `TC-d86ad921-51e7-48c3-bba4-7688b7da4eb0 앱이 백그라운드에 다녀와도 상세 선택을 유지한다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.selectRoutine(routine)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))
        composeRule.onNode(robot.card(routine.detail.title)).assertExists()
        robot.assertDetailOnDetailPane(routine)
        robot.pressBack()
        robot.assertAddOnDetailPane()
    }

    // 선택한 루틴이 없을 때의 루틴 추가에 주기, 단위, 목표량, 한 번에 기록할 양과 태그를 채운다.
    private fun fillPlaceholder(withTitle: Boolean) {
        val tag = Arb.tag(title = ROUTINE_LIST_DETAIL_TAG_TITLE).next()
        environment.tagList = listOf(tag)
        if (withTitle) {
            robot.titleInput().performTextInput(PLACEHOLDER_TITLE)
        }
        robot.selectCycle(option = CYCLE_WEEKLY)
        robot.inputUnit(unit = UNIT_MILLILITER)
        robot.goalAmountInput().performTextInput(PLACEHOLDER_GOAL_AMOUNT)
        robot.amountPerRecordInput().performTextReplacement(PLACEHOLDER_AMOUNT_PER_RECORD)
        composeRule.runOnIdle { environment.addTagViewModelList.last().add(tagId = tag.id) }
        composeRule.waitForIdle()
        assertPlaceholderRetained(withTitle = withTitle)
    }

    private fun assertPlaceholderRetained(withTitle: Boolean) {
        robot.assertAddOnDetailPane()
        if (withTitle) {
            robot.titleInput().routineListDetailEditableText() shouldBe PLACEHOLDER_TITLE
        }
        robot.assertCycleSelected(option = CYCLE_WEEKLY)
        robot.assertUnit(unit = UNIT_MILLILITER)
        robot.goalAmountInput().routineListDetailEditableText() shouldBe PLACEHOLDER_GOAL_AMOUNT
        robot.amountPerRecordInput().routineListDetailEditableText() shouldBe PLACEHOLDER_AMOUNT_PER_RECORD
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_TAG_TITLE).assertExists()
        environment.addTagViewModelList shouldHaveSize 1
    }

    private fun fillTitleAndGoalAmount() {
        robot.titleInput().performTextInput(PLACEHOLDER_TITLE)
        robot.goalAmountInput().performTextInput(PLACEHOLDER_GOAL_AMOUNT)
        composeRule.waitForIdle()
        assertTitleAndGoalAmountRetained()
    }

    private fun assertTitleAndGoalAmountRetained() {
        robot.assertAddOnDetailPane()
        robot.titleInput().routineListDetailEditableText() shouldBe PLACEHOLDER_TITLE
        robot.goalAmountInput().routineListDetailEditableText() shouldBe PLACEHOLDER_GOAL_AMOUNT
    }

    private fun openAddOnDetail(routine: Routine) {
        robot.selectRoutine(routine)
        robot.assertDetailOnDetailPane(routine)
        robot.clickHomeAdd()
        backStack.last() shouldBe RoutineAddNavKey()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val PLACEHOLDER_START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        const val TODAY_TEXT = "Jul 15, 2026"
        const val PLACEHOLDER_TITLE = "물 마시기"
        const val PLACEHOLDER_GOAL_AMOUNT = "2000"
        const val PLACEHOLDER_AMOUNT_PER_RECORD = "250"
        const val FIRST_TITLE = "A-list-routine"
        const val SECOND_TITLE = "B-list-routine"
        const val RECORD_AMOUNT = 20
        const val RECORD_AMOUNT_TEXT = "20회"

        val routineSavedStateConfiguration: SavedStateConfiguration =
            SavedStateConfiguration {
                serializersModule =
                    SerializersModule {
                        polymorphic(ScreenNavKey::class) { routineNavKeys() }
                    }
            }

        fun routine(title: String): Routine = routineListDetailRoutine(title = title, today = TODAY)
    }
}
