package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 완료된 루틴 목록 화면을 제품 ViewModel과 함께 배치해, 다시 시작과 삭제, 안내와 실행 취소가 저장된 루틴에 반영되는 결과를 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineFinishedListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val environment = RoutineFinishedListTestEnvironment()
    private var isShown by mutableStateOf(true)
    private val navigatedIdList = mutableListOf<Uuid>()
    private var navigateUpCount = 0

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        setRoutineFinishedListToday(TODAY)
    }

    @After
    fun tearDown() {
        releaseRoutineFinishedListToday()
    }

    @Test
    fun `TC-8a130cbb-7635-4bfc-830f-8f9d116e441f 루틴을 다시 시작하면 목록에서 사라지고 안내와 실행 취소를 제공한다`() {
        assertRestartFeedback(message = DEFAULT_RESTARTED_MESSAGE, undoAction = DEFAULT_UNDO_ACTION)
    }

    @Test
    @Config(qualifiers = "ko-w411dp-h891dp")
    fun `TC-8a130cbb-7635-4bfc-830f-8f9d116e441f 한국어 환경에서 다시 시작 안내와 실행 취소를 제공한다`() {
        assertRestartFeedback(message = KOREAN_RESTARTED_MESSAGE, undoAction = KOREAN_UNDO_ACTION)
    }

    @Test
    fun `TC-ea599f9a-8586-4971-bf46-80f51a8676a8 루틴을 삭제하면 목록에서 사라지고 안내와 실행 취소를 제공한다`() {
        assertDeleteFeedback(message = DEFAULT_DELETED_MESSAGE, undoAction = DEFAULT_UNDO_ACTION)
    }

    @Test
    @Config(qualifiers = "ko-w411dp-h891dp")
    fun `TC-ea599f9a-8586-4971-bf46-80f51a8676a8 한국어 환경에서 삭제 안내와 실행 취소를 제공한다`() {
        assertDeleteFeedback(message = KOREAN_DELETED_MESSAGE, undoAction = KOREAN_UNDO_ACTION)
    }

    @Test
    fun `TC-22a7c048-68cf-4e88-affc-8e6700cd2b36 다시 시작을 실행 취소하면 루틴이 목록 순서에 맞는 자리에 다시 나타난다`() {
        val routineList = threeRoutineList()
        setScreen(routineList = routineList)
        swipe(title = "나") { swipeRight() }
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))

        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나", "다"))
        composeRule.routineFinishedListCardTextList()[1] shouldBe listOf("나", DEFAULT_FINISHED_TEXT)
        environment.stored(routineList[1]).isFinished shouldBe true
    }

    @Test
    fun `TC-e59bd1c7-9f62-4fcf-bdb0-be5a48d29e53 삭제를 실행 취소하면 루틴이 목록 순서에 맞는 자리에 다시 나타난다`() {
        val routineList = threeRoutineList()
        setScreen(routineList = routineList)
        swipe(title = "나") { swipeLeft() }
        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))

        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나", "다"))
        environment.stored(routineList[1]).isDeleted shouldBe false
        environment.stored(routineList[1]).isFinished shouldBe true
    }

    @Test
    fun `TC-106e3683-e62f-4bb5-b691-3be21fe3bf30 마지막 루틴을 다시 시작하면 빈 상태 안내가 나타난다`() {
        setScreen(routineList = listOf(routine(title = "가")))

        swipe(title = "가") { swipeRight() }

        composeRule.waitUntilRoutineFinishedListExists(hasText(DEFAULT_EMPTY_TITLE))
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
    }

    @Test
    fun `TC-33517bec-35fc-4f9e-8d28-aa1d65290fed 안내가 보이는 동안 이어서 동작하면 마지막 동작의 안내만 남는다`() {
        val routineA = routine(title = "가")
        val routineB = routine(title = "나")
        setScreen(routineList = listOf(routineA, routineB))
        swipe(title = "가") { swipeRight() }
        composeRule.waitUntilRoutineFinishedListExists(hasText(DEFAULT_RESTARTED_MESSAGE))

        swipe(title = "나") { swipeLeft() }

        composeRule.waitUntilRoutineFinishedListExists(hasText(DEFAULT_DELETED_MESSAGE))
        composeRule.onAllNodesWithText(DEFAULT_RESTARTED_MESSAGE).assertCountEquals(0)

        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()

        composeRule.waitUntilRoutineFinishedListTitles(listOf("나"))
        environment.stored(routineB).isDeleted shouldBe false
        environment.stored(routineA).isFinished shouldBe false
        coVerify(exactly = 0) { environment.finishRoutineUseCase(parameter = any()) }
    }

    @Test
    fun `TC-5699aba0-889f-4377-a9e0-cc3cdc7be03b 다시 시작이 저장되지 못하면 루틴이 목록에 남고 안내를 표시하지 않는다`() {
        val routine = routine(title = "가")
        environment.isRestartFailing = true
        setScreen(routineList = listOf(routine))

        swipe(title = "가") { swipeRight() }

        coVerify(exactly = 1) { environment.restartRoutineUseCase(parameter = routine.id) }
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard("가"))
        environment.stored(routine).isFinished shouldBe true
        composeRule.onAllNodesWithText(DEFAULT_RESTARTED_MESSAGE).assertCountEquals(0)
        composeRule.onAllNodesWithText(DEFAULT_UNDO_ACTION).assertCountEquals(0)
    }

    @Test
    fun `TC-5699aba0-889f-4377-a9e0-cc3cdc7be03b 삭제가 저장되지 못하면 루틴이 목록에 남고 안내를 표시하지 않는다`() {
        val routine = routine(title = "가")
        environment.isDeleteFailing = true
        setScreen(routineList = listOf(routine))

        swipe(title = "가") { swipeLeft() }

        coVerify(exactly = 1) { environment.deleteRoutineUseCase(parameter = routine.id) }
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard("가"))
        environment.stored(routine).isDeleted shouldBe false
        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).assertCountEquals(0)
        composeRule.onAllNodesWithText(DEFAULT_UNDO_ACTION).assertCountEquals(0)
    }

    @Test
    fun `TC-10555626-c539-49c6-b0d0-3115ae3e328f 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val routine = routine(title = "가")
        setScreen(routineList = listOf(routine))
        swipe(title = "가") { swipeRight() }
        composeRule.waitUntilRoutineFinishedListExists(hasText(DEFAULT_RESTARTED_MESSAGE))

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(DEFAULT_RESTARTED_MESSAGE).assertCountEquals(0)
        composeRule.onAllNodesWithText(DEFAULT_UNDO_ACTION).assertCountEquals(0)
        environment.stored(routine).isFinished shouldBe false
        coVerify(exactly = 0) { environment.finishRoutineUseCase(parameter = any()) }
    }

    @Test
    fun `TC-917282d9-ab53-4474-8626-84392c57dba4 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        val routine = routine(title = "가")
        setScreen(routineList = listOf(routine, routine(title = "나")))
        swipe(title = "가") { swipeRight() }
        composeRule.waitUntilRoutineFinishedListExists(hasText(DEFAULT_RESTARTED_MESSAGE))

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard("나"))

        composeRule.onAllNodesWithText(DEFAULT_RESTARTED_MESSAGE).assertCountEquals(0)
        composeRule.onAllNodesWithText(DEFAULT_UNDO_ACTION).assertCountEquals(0)
        environment.stored(routine).isFinished shouldBe false
        coVerify(exactly = 0) { environment.finishRoutineUseCase(parameter = any()) }
    }

    @Test
    fun `TC-1db6e580-10af-4804-9158-c09f1ed76ca4 루틴을 선택하면 그 루틴의 상세 이동을 요청한다`() {
        val routineList = threeRoutineList()
        setScreen(routineList = routineList)

        composeRule.onNode(routineFinishedListCard("나")).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(routineList[1].id)
    }

    @Test
    fun `TC-75b611f6-faff-4e18-9818-13d8ab87a248 뒤로가기 버튼을 선택하면 뒤로가기를 요청한다`() {
        setScreen(routineList = listOf(routine(title = "가")))

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-2d0aeab1-94fe-4682-bdd7-46b387432e0f 화면에 들어오거나 다시 보이는 것만으로는 새로고침하지 않는다`() {
        setScreen(routineList = listOf(routine(title = "가")))

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard("가"))

        verify(exactly = 0) { environment.syncViewModel.refresh() }
    }

    @Test
    fun `TC-e1a92649-5a7e-4ca4-856f-c0ed093bd43d 백그라운드에 다녀온 사이 자정이 지나면 진행 상태가 달라진 루틴을 바뀐 자리로 옮긴다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setScreen(routineList = todayOrderRoutineList(), lifecycleOwner = lifecycleOwner)
        composeRule.waitUntilRoutineFinishedListTitles(listOf("나", "가"))

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { setRoutineFinishedListToday(NEXT_DAY) }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "나"))
        composeRule.routineFinishedListCardTextList() shouldBe listOf(listOf("가", DEFAULT_FINISHED_TEXT), listOf("나", DEFAULT_FINISHED_TEXT))
    }

    @Test
    fun `TC-cc8a66ab-bcbc-4a2a-8e07-b3f7294b9e2b 화면을 보는 동안 자정이 지나도 목록 순서는 바뀌지 않는다`() {
        setScreen(routineList = todayOrderRoutineList())
        composeRule.waitUntilRoutineFinishedListTitles(listOf("나", "가"))

        composeRule.runOnIdle { setRoutineFinishedListToday(NEXT_DAY) }
        composeRule.mainClock.advanceTimeBy(MIDNIGHT_WAIT_MILLIS)
        composeRule.waitForIdle()

        composeRule.routineFinishedListCardTextList() shouldBe listOf(listOf("나", DEFAULT_FINISHED_TEXT), listOf("가", DEFAULT_FINISHED_TEXT))
    }

    @Test
    fun `TC-1a3481be-1051-4bac-9e6d-767a0a201edd 화면이 재생성되어도 보던 자리가 그대로다`() {
        val restorationTester = StateRestorationTester(composeRule)
        environment.setRoutineList(routineList = manyRoutineList())
        val viewModel = environment.finishedViewModel()
        restorationTester.setContent { Screen(viewModel = viewModel) }
        scrollToLaterRoutine()

        restorationTester.emulateSavedInstanceStateRestore()

        assertLaterRoutineShown()
    }

    @Test
    fun `TC-1a3481be-1051-4bac-9e6d-767a0a201edd 앱이 백그라운드에 다녀와도 보던 자리가 그대로다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setScreen(routineList = manyRoutineList(), lifecycleOwner = lifecycleOwner)
        scrollToLaterRoutine()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        assertLaterRoutineShown()
    }

    private fun assertRestartFeedback(
        message: String,
        undoAction: String,
    ) {
        val routineList = threeRoutineList()
        setScreen(routineList = routineList)

        swipe(title = "나") { swipeRight() }

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))
        coVerify(exactly = 1) { environment.restartRoutineUseCase(parameter = routineList[1].id) }
        environment.stored(routineList[1]).isFinished shouldBe false
        composeRule.onNodeWithText(message).assertExists()
        composeRule.onNodeWithText(undoAction).assertExists()
    }

    private fun assertDeleteFeedback(
        message: String,
        undoAction: String,
    ) {
        val routineList = threeRoutineList()
        setScreen(routineList = routineList)

        swipe(title = "나") { swipeLeft() }

        composeRule.waitUntilRoutineFinishedListTitles(listOf("가", "다"))
        coVerify(exactly = 1) { environment.deleteRoutineUseCase(parameter = routineList[1].id) }
        environment.stored(routineList[1]).isDeleted shouldBe true
        composeRule.onNodeWithText(message).assertExists()
        composeRule.onNodeWithText(undoAction).assertExists()
    }

    private fun swipe(
        title: String,
        gesture: TouchInjectionScope.() -> Unit,
    ) {
        composeRule.onNode(routineFinishedListCard(title)).performTouchInput(gesture)
        composeRule.waitForIdle()
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
    }

    private fun setScreen(
        routineList: List<Routine>,
        lifecycleOwner: TestLifecycleOwner? = null,
    ) {
        environment.setRoutineList(routineList = routineList)
        val viewModel = environment.finishedViewModel()
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current)) {
                Screen(viewModel = viewModel)
            }
        }
        routineList.firstOrNull()?.let { routine -> composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(routine.detail.title)) }
    }

    @Composable
    private fun Screen(viewModel: RoutineFinishedListViewModel) {
        DiaryTheme {
            if (isShown) {
                RoutineFinishedListScreen(
                    navigateUp = { navigateUpCount += 1 },
                    navigateToDetail = navigatedIdList::add,
                    routineViewModel = viewModel,
                    syncViewModel = environment.syncViewModel,
                )
            }
        }
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 7, 15)
        val NEXT_DAY: LocalDate = LocalDate(2026, 7, 16)
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val MIDNIGHT_WAIT_MILLIS = 5_000L
        const val MANY_COUNT = 40
        const val LATER_INDEX = 30
        const val FIRST_OF_MANY_TITLE = "Routine 01"
        const val LATER_TITLE = "Routine 31"
        const val DEFAULT_FINISHED_TEXT = "Finished"
        const val DEFAULT_EMPTY_TITLE = "No finished routines"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_RESTARTED_MESSAGE = "Routine restarted."
        const val KOREAN_RESTARTED_MESSAGE = "루틴을 다시 시작했습니다."
        const val DEFAULT_DELETED_MESSAGE = "Routine deleted."
        const val KOREAN_DELETED_MESSAGE = "루틴이 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"

        fun routine(
            title: String,
            startDate: LocalDate = LocalDate(2026, 7, 1),
        ): Routine = routineFinishedListRoutine(title = title, startDate = startDate)

        fun threeRoutineList(): List<Routine> = listOf("가", "나", "다").map { title -> routine(title = title) }

        // 2026-07-15에는 `나`가 진행 중, `가`가 시작 전이고, 2026-07-16에는 둘 다 진행 중이다.
        fun todayOrderRoutineList(): List<Routine> = listOf(routine(title = "나"), routine(title = "가", startDate = NEXT_DAY))

        fun manyRoutineList(): List<Routine> = List(MANY_COUNT) { index -> routine(title = "Routine ${(index + 1).toString().padStart(2, '0')}") }
    }
}
