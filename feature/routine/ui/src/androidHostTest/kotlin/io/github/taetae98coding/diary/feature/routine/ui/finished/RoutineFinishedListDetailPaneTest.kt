package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.activity.ComponentActivity
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.ROUTINE_PROGRESS_BAR_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.routineNavKeys
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.ADD_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.ADD_TITLE
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.CYCLE_WEEKLY
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.DELETE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.FINISHED_TEXT
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.FINISH_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.NAVIGATE_UP_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.PLACEHOLDER_MESSAGE
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.RECORD_ADD_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.RESTART_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.UNIT_COUNT
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListRobot.Companion.UPDATE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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

// 제품 entry의 배치 정보를 그대로 쓰도록, 두 영역을 함께 표시하는 너비에서 제품 routineEntry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class RoutineFinishedListDetailPaneTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineFinishedListTestEnvironment(RoutineFinishedListDefaultNavKey, RoutineHomeNavKey, RoutineFinishedListNavKey)
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
    fun `TC-773e10fe-a17f-4f5b-a9ad-c52430350dc2 목록과 상세를 함께 쓰는 환경에서는 두 영역을 함께 표시한다`() {
        backStack.removeLastOrNull()
        robot.setNavDisplay(routineList = threeRoutineList())

        robot.enterFinishedList()

        robot.assertFinishedListShown()
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나", "다"))
        robot.assertPlaceholder()
    }

    @Test
    fun `TC-8c75f8ca-07a1-4ac7-875d-0161ffcb8748 선택 전 상세에는 루틴을 선택하라는 안내만 표시한다`() {
        assertPlaceholderOnly(message = PLACEHOLDER_MESSAGE, navigateUpDescription = NAVIGATE_UP_DESCRIPTION)
    }

    @Test
    @Config(qualifiers = "ko-w1280dp-h1600dp")
    fun `TC-8c75f8ca-07a1-4ac7-875d-0161ffcb8748 한국어 환경에서 선택 전 상세에는 루틴을 선택하라는 안내만 표시한다`() {
        assertPlaceholderOnly(message = KOREAN_PLACEHOLDER_MESSAGE, navigateUpDescription = KOREAN_NAVIGATE_UP_DESCRIPTION)
    }

    @Test
    fun `TC-fad61a2f-3cb3-488e-af27-eb7ed5f434fe 루틴을 선택하면 상세 영역에 그 루틴의 상세 화면이 표시된다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)

        robot.selectRoutine("나")

        backStack.toList() shouldBe FINISHED_LIST_BACK_STACK + RoutineDetailNavKey(id = routineList[1].id)
        robot.assertDetail(title = "나")
        robot.assertFinishedListShown()
        composeRule.routineFinishedListCardTitleList() shouldBe listOf("가", "나", "다")
    }

    @Test
    fun `TC-77e6ff56-f13f-4323-b7f0-9d43de132efc 다른 루틴을 선택하면 현재 상세를 교체한다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        robot.selectRoutine("가")
        robot.assertDetail(title = "가")

        robot.selectRoutine("나")

        backStack.toList() shouldBe FINISHED_LIST_BACK_STACK + RoutineDetailNavKey(id = routineList[1].id)
        robot.assertDetail(title = "나")
    }

    @Test
    fun `TC-9f940bec-3e41-48e1-987d-a86d800dc698 완료된 루틴 목록의 상세 영역에는 루틴 추가가 나타나지 않는다`() {
        robot.setNavDisplay(routineList = threeRoutineList())

        robot.assertPlaceholder()
        composeRule.onAllNodesWithContentDescription(ADD_DESCRIPTION).assertCountEquals(0)
        composeRule.onAllNodesWithText(ADD_TITLE).assertCountEquals(0)
    }

    @Test
    fun `TC-13bc6f6c-6544-4310-9435-df87e9ba5d04 상세 영역에서 루틴을 다시 시작하면 목록에서 사라지고 상세는 그대로 남는다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        robot.clickDetailAction(RESTART_DESCRIPTION)

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))
        backStack.toList() shouldBe FINISHED_LIST_BACK_STACK + RoutineDetailNavKey(id = routineList[1].id)
        robot.assertDetail(title = "나")
        robot.detailActionCount(FINISH_DESCRIPTION) shouldBe 1
        robot.detailActionCount(RESTART_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-f12c1f04-b713-4fd7-a7d3-9a2798bb1693 다시 시작한 상세에서 다시 완료하면 그 루틴이 목록에 다시 나타난다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")
        robot.clickDetailAction(RESTART_DESCRIPTION)
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))

        robot.clickDetailAction(FINISH_DESCRIPTION)

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나", "다"))
        composeRule.routineFinishedListCardTextList()[1] shouldBe listOf("나", FINISHED_TEXT)
        robot.assertDetail(title = "나")
        robot.detailActionCount(RESTART_DESCRIPTION) shouldBe 1
        robot.detailActionCount(FINISH_DESCRIPTION) shouldBe 0
    }

    @Test
    fun `TC-3c247c1b-cefc-4a60-b46d-99a0c8a4e105 상세 영역에서 기록을 바꾸거나 루틴을 수정해도 목록의 카드 문구는 완료 그대로다`() {
        val routine = routineFinishedListRoutine(title = WATER_TITLE, startDate = LocalDate(2026, 7, 1), unit = "ml", goalAmount = 2000, amountPerRecord = 250)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.selectRoutine(WATER_TITLE)
        robot.assertDetail(title = WATER_TITLE)

        robot.clickDetailAction(RECORD_ADD_DESCRIPTION)
        environment.storedRecordList.map { record -> record.date to record.amount } shouldBe listOf(TODAY to 250)
        assertCardFinishedOnly()

        composeRule.waitUntilRoutineFinishedListExists(hasContentDescription(RECORD_REMOVE_DESCRIPTION))
        composeRule.onNode(hasContentDescription(RECORD_REMOVE_DESCRIPTION) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        environment.storedRecordList.shouldBeEmpty()
        assertCardFinishedOnly()

        robot.goalAmountInput().performTextReplacement("3000")
        robot.clickDetailAction(UPDATE_DESCRIPTION)
        environment.stored(routine).detail.goalAmount shouldBe 3000
        assertCardFinishedOnly()

        robot.selectCycle(option = CYCLE_WEEKLY)
        robot.clickDetailAction(UPDATE_DESCRIPTION)
        environment.stored(routine).detail.cycle shouldBe RoutineCycle.WEEKLY
        assertCardFinishedOnly()

        robot.inputUnit(unit = UNIT_COUNT)
        robot.clickDetailAction(UPDATE_DESCRIPTION)
        environment.stored(routine).detail.unit shouldBe "회"
        assertCardFinishedOnly()
    }

    @Test
    fun `TC-4838bdd4-b003-44f2-9e89-45355d132632 상세 영역에서 제목을 고치면 목록에 놓인 그 루틴의 제목과 자리가 같은 화면에서 바뀐다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        robot.titleInput().performTextReplacement("하")
        robot.clickDetailAction(UPDATE_DESCRIPTION)

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다", "하"))
        robot.assertDetail(title = "하")
    }

    @Test
    fun `TC-4838bdd4-b003-44f2-9e89-45355d132632 상세 영역에서 시작 날짜를 고치면 목록에 놓인 그 루틴의 자리가 같은 화면에서 바뀐다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        robot.pickDate(currentText = START_DATE_TEXT, date = LocalDate(2026, 7, 16))
        robot.clickDetailAction(UPDATE_DESCRIPTION)

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다", "나"))
        composeRule.routineFinishedListCardTextList()[2] shouldBe listOf("나", FINISHED_TEXT)
        robot.assertDetail(title = "나")
    }

    @Test
    fun `TC-72dc941a-6eda-4b9e-9f93-b92520d76441 상세 영역의 루틴에서 연 태그 상세는 단독으로 표시하고 돌아오면 떠나기 전의 루틴 상세가 놓인다`() {
        val tag = Arb.tag(title = TAG_TITLE).next().copy(isDeleted = false)
        val routineList = threeRoutineList()
        environment.tagList = listOf(tag)
        robot.setNavDisplay(routineList = routineList)
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        composeRule.onNode(hasText(TAG_TITLE) and !hasAnyAncestor(isDialog())).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        backStack.toList() shouldBe FINISHED_LIST_BACK_STACK + RoutineDetailNavKey(id = routineList[1].id) + TagDetailNavKey(id = tag.id)
        composeRule.onNodeWithText(ROUTINE_FINISHED_LIST_TAG_DETAIL_CONTENT).assertExists()
        robot.assertFinishedListNotShown()

        robot.pressBack()

        assertListWithDetail(routine = routineList[1])
    }

    @Test
    fun `TC-72dc941a-6eda-4b9e-9f93-b92520d76441 상세 영역의 루틴에서 연 태그 추가는 단독으로 표시하고 돌아오면 떠나기 전의 루틴 상세가 놓인다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        // 고를 수 있는 태그가 하나도 없으면 태그 선택은 곧바로 태그 추가로 이동한다.
        composeRule.onNodeWithText(TAG_SELECT_LABEL).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        backStack.last().shouldBeInstanceOf<TagAddNavKey>()
        composeRule.onNodeWithText(ROUTINE_FINISHED_LIST_TAG_ADD_CONTENT).assertExists()
        robot.assertFinishedListNotShown()

        robot.pressBack()

        assertListWithDetail(routine = routineList[1])
    }

    @Test
    fun `TC-597ef9c7-472d-4565-8c7b-769806f96f95 상세 영역에서 루틴을 삭제하면 선택 전 상태로 돌아간다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        robot.clickDetailAction(DELETE_DESCRIPTION)

        assertDeletedFromDetailPane(deleted = routineList[1], remainingTitleList = listOf("가", "다"))
    }

    @Test
    fun `TC-597ef9c7-472d-4565-8c7b-769806f96f95 이어 고른 다른 루틴을 상세 영역에서 삭제해도 선택 전 상태로 돌아간다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        robot.selectRoutine("가")
        robot.assertDetail(title = "가")
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        robot.clickDetailAction(DELETE_DESCRIPTION)

        assertDeletedFromDetailPane(deleted = routineList[1], remainingTitleList = listOf("가", "다"))
    }

    @Test
    fun `TC-017f0931-e11a-4243-b5d4-7e8e45bd37e6 루틴 상세가 선택된 상태에서 뒤로가면 목록을 유지하고 상세만 선택 전 상태로 되돌린다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        robot.pressBack()

        assertListWithPlaceholder(titleList = listOf("가", "나", "다"))
    }

    @Test
    fun `TC-017f0931-e11a-4243-b5d4-7e8e45bd37e6 다른 루틴 상세로 교체한 상태에서 뒤로가도 선택 전 상태로 되돌린다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.selectRoutine("가")
        robot.assertDetail(title = "가")
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        robot.pressBack()

        assertListWithPlaceholder(titleList = listOf("가", "나", "다"))
    }

    @Test
    fun `TC-017f0931-e11a-4243-b5d4-7e8e45bd37e6 다시 시작해 목록에서 사라진 루틴의 상세에서 뒤로가도 선택 전 상태로 되돌린다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")
        robot.clickDetailAction(RESTART_DESCRIPTION)
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))

        robot.pressBack()

        assertListWithPlaceholder(titleList = listOf("가", "다"))
    }

    @Test
    fun `TC-c9f4ab3f-e8e1-440d-8d31-2e7d29c01b9b 선택 전 상태에서 목록 화면의 뒤로가기 동작을 쓰면 RoutineHome의 목록 보기로 돌아간다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.assertPlaceholder()

        robot.clickNavigateUp()

        backStack.toList() shouldBe listOf(RoutineFinishedListDefaultNavKey, RoutineHomeNavKey)
        robot.assertHomeListView()
    }

    @Test
    fun `TC-c9f4ab3f-e8e1-440d-8d31-2e7d29c01b9b 루틴 상세가 놓인 상태에서 목록 화면의 뒤로가기 동작을 쓰면 RoutineHome의 목록 보기로 돌아간다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        robot.clickNavigateUp()

        backStack.toList() shouldBe listOf(RoutineFinishedListDefaultNavKey, RoutineHomeNavKey)
        robot.assertHomeListView()
    }

    @Test
    fun `TC-1f76123e-1d38-42a1-9af9-572696b36c0a 상세가 선택 전 상태일 때 뒤로가면 목록 화면 전체를 떠나 RoutineHome의 목록 보기로 돌아간다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.assertPlaceholder()

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineFinishedListDefaultNavKey, RoutineHomeNavKey)
        robot.assertHomeListView()
    }

    @Test
    fun `TC-525daa1d-b52e-4123-8093-991577432329 루틴 상세를 연 채 목록 화면을 떠났다가 다시 진입하면 상세가 선택 전 상태로 돌아간다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        assertReenteredWithPlaceholder()
    }

    @Test
    fun `TC-525daa1d-b52e-4123-8093-991577432329 다른 루틴 상세로 교체한 채 목록 화면을 떠났다가 다시 진입해도 상세가 선택 전 상태로 돌아간다`() {
        robot.setNavDisplay(routineList = threeRoutineList())
        robot.selectRoutine("가")
        robot.assertDetail(title = "가")
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        assertReenteredWithPlaceholder()
    }

    @Test
    fun `TC-1ab834a3-e029-4c76-a3d3-c2f95e170f81 화면이 재생성되어도 상세 선택이 그대로다`() {
        val routineList = threeRoutineList()
        environment.setRoutineList(routineList = routineList)
        val restorationTester = StateRestorationTester(composeRule)
        val viewModelStoreOwner = routineFinishedListViewModelStoreOwner()
        lateinit var restoredBackStack: NavBackStack<ScreenNavKey>
        restorationTester.setContent {
            val restorableBackStack =
                rememberSerializable(
                    configuration = routineSavedStateConfiguration,
                    serializer = NavBackStackSerializer(PolymorphicSerializer(ScreenNavKey::class)),
                ) { NavBackStack<ScreenNavKey>(RoutineHomeNavKey, RoutineFinishedListNavKey) }
            restoredBackStack = restorableBackStack
            environment.Content(navBackStack = restorableBackStack, viewModelStoreOwner = viewModelStoreOwner)
        }
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        restoredBackStack.toList() shouldBe listOf(RoutineHomeNavKey, RoutineFinishedListNavKey, RoutineDetailNavKey(id = routineList[1].id))
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나", "다"))
        robot.assertDetail(title = "나")
        robot.pressBack()
        restoredBackStack.toList() shouldBe listOf(RoutineHomeNavKey, RoutineFinishedListNavKey)
        assertListWithPlaceholder(titleList = listOf("가", "나", "다"))
    }

    @Test
    fun `TC-1ab834a3-e029-4c76-a3d3-c2f95e170f81 앱이 백그라운드에 다녀와도 상세 선택이 그대로다`() {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        robot.selectRoutine("나")
        robot.assertDetail(title = "나")

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertListWithDetail(routine = routineList[1])
        robot.pressBack()
        backStack.toList() shouldBe FINISHED_LIST_BACK_STACK
        assertListWithPlaceholder(titleList = listOf("가", "나", "다"))
    }

    private fun assertPlaceholderOnly(
        message: String,
        navigateUpDescription: String,
    ) {
        val routineList = threeRoutineList()
        robot.setNavDisplay(routineList = routineList)
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나", "다"))

        composeRule.onNodeWithText(message).assertExists()
        // 누를 수 있는 것은 목록 영역의 뒤로가기와 루틴 카드뿐이다.
        composeRule.onNodeWithContentDescription(navigateUpDescription).assertExists()
        composeRule.onAllNodes(hasClickAction()).assertCountEquals(routineList.size + 1)
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    private fun assertCardFinishedOnly() {
        composeRule.waitForIdle()
        composeRule.routineFinishedListCardTextList() shouldBe listOf(listOf(WATER_TITLE, FINISHED_TEXT))
        composeRule.onAllNodes(hasTestTag(ROUTINE_PROGRESS_BAR_TEST_TAG), useUnmergedTree = true).assertCountEquals(0)
    }

    private fun assertListWithDetail(routine: Routine) {
        backStack.toList() shouldBe FINISHED_LIST_BACK_STACK + RoutineDetailNavKey(id = routine.id)
        robot.assertFinishedListShown()
        composeRule.onNode(routineFinishedListCard(routine.detail.title)).assertExists()
        robot.assertDetail(title = routine.detail.title)
    }

    private fun assertListWithPlaceholder(titleList: List<String>) {
        robot.assertFinishedListShown()
        composeRule.waitUntilRoutineFinishedListTitles(titleList)
        robot.assertPlaceholder()
    }

    private fun assertDeletedFromDetailPane(
        deleted: Routine,
        remainingTitleList: List<String>,
    ) {
        backStack.toList() shouldBe FINISHED_LIST_BACK_STACK
        environment.stored(deleted).isDeleted shouldBe true
        assertListWithPlaceholder(titleList = remainingTitleList)
    }

    private fun assertReenteredWithPlaceholder() {
        robot.clickNavigateUp()
        robot.assertHomeListView()
        robot.enterFinishedList()

        backStack.toList() shouldBe FINISHED_LIST_BACK_STACK
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).fetchSemanticsNodes().size shouldBe 3
        assertListWithPlaceholder(titleList = listOf("가", "나", "다"))
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 7, 15)
        val FINISHED_LIST_BACK_STACK: List<ScreenNavKey> = listOf(RoutineFinishedListDefaultNavKey, RoutineHomeNavKey, RoutineFinishedListNavKey)
        const val WATER_TITLE = "물 마시기"
        const val TAG_TITLE = "RoutineFinishedListTagA"
        const val TAG_SELECT_LABEL = "Select tag"
        const val START_DATE_TEXT = "Jul 1, 2026"
        const val RECORD_REMOVE_DESCRIPTION = "Remove record, Jul 15, 2026 250ml"
        const val KOREAN_PLACEHOLDER_MESSAGE = "루틴을 선택하세요"
        const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"

        val routineSavedStateConfiguration: SavedStateConfiguration =
            SavedStateConfiguration {
                serializersModule =
                    SerializersModule {
                        polymorphic(ScreenNavKey::class) { routineNavKeys() }
                    }
            }

        fun threeRoutineList(): List<Routine> = listOf("가", "나", "다").map { title -> routineFinishedListRoutine(title = title, startDate = LocalDate(2026, 7, 1)) }
    }
}
