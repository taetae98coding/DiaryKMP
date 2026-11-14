package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.routine.toCardText
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 입력과 기록 내역이 한 화면에 모두 놓이도록 세로로 긴 창에서 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h2400dp")
class RoutineDetailFinishScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val tagA: Tag = tagOf(title = TAG_A_TITLE)
    private val tagB: Tag = tagOf(title = TAG_B_TITLE)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        routineRecordSetToday(TODAY)
    }

    @After
    fun tearDown() {
        routineRecordReleaseToday()
    }

    @Test
    fun `TC-294fd7f4-c40e-4b3f-92d5-835d240f2ffc 완료되지 않은 루틴에는 완료만 제공한다`() {
        setScreen(RoutineRecordTestEnvironment(isFinished = false))

        assertFinishActionOnly(isFinished = false)
    }

    @Test
    fun `TC-294fd7f4-c40e-4b3f-92d5-835d240f2ffc 완료된 루틴에는 다시 시작만 제공한다`() {
        setScreen(RoutineRecordTestEnvironment(isFinished = true))

        assertFinishActionOnly(isFinished = true)
    }

    @Test
    fun `TC-84ca3874-025d-41cf-85a7-18d7564695ff 완료하면 화면과 입력을 유지한 채 다시 시작을 제공한다`() {
        assertToggled(isFinished = false)
    }

    @Test
    fun `TC-10797a39-0249-4871-98fe-43b7ecd75bb8 다시 시작하면 화면과 입력을 유지한 채 완료를 제공한다`() {
        assertToggled(isFinished = true)
    }

    private fun assertToggled(isFinished: Boolean) {
        val environment = RoutineRecordTestEnvironment(isFinished = isFinished)
        environment.storeOnFinish()
        setScreen(environment)
        editInput()

        clickFinishAction(isFinished = isFinished)

        composeRule.onNode(isDialog()).assertDoesNotExist()
        verify(exactly = if (isFinished) 0 else 1) { environment.routineViewModel.finish() }
        verify(exactly = if (isFinished) 1 else 0) { environment.routineViewModel.restart() }
        environment.navigateUpCount shouldBe 0
        assertInputKept()
        assertFinishActionOnly(isFinished = !isFinished)
        assertNoFeedback()
    }

    @Test
    fun `TC-6911145f-75ae-4699-9127-8fb4783224b2 완료를 처리하는 동안 진행 중인 상태를 표시한다`() {
        assertFinishProgress(isFinished = false)
    }

    @Test
    fun `TC-6911145f-75ae-4699-9127-8fb4783224b2 다시 시작을 처리하는 동안 진행 중인 상태를 표시한다`() {
        assertFinishProgress(isFinished = true)
    }

    private fun assertFinishProgress(isFinished: Boolean) {
        val environment = RoutineRecordTestEnvironment(isFinished = isFinished)
        // 요청에 답이 올 때까지 처리 중인 상태가 이어진다.
        every { environment.routineViewModel.finish() } answers { environment.setFinishInProgress(isInProgress = true) }
        every { environment.routineViewModel.restart() } answers { environment.setFinishInProgress(isInProgress = true) }
        setScreen(environment)
        composeRule.onNode(finishAction(isFinished = isFinished) and routineRecordProgress(), useUnmergedTree = true).assertDoesNotExist()

        clickFinishAction(isFinished = isFinished)

        composeRule.onNode(finishAction(isFinished = isFinished) and routineRecordProgress(), useUnmergedTree = true).assertExists()

        composeRule.runOnIdle {
            environment.setFinishInProgress(isInProgress = false)
            environment.setFinished(isFinished = !isFinished)
        }
        composeRule.waitForIdle()

        composeRule.onNode(routineRecordProgress(), useUnmergedTree = true).assertDoesNotExist()
        assertFinishActionOnly(isFinished = !isFinished)
    }

    @Test
    fun `TC-7d6fdf91-b086-45d5-bba7-92302d3550a2 완료 저장에 실패하면 안내 없이 화면과 입력, 완료 동작이 그대로다`() {
        assertFinishFailed(isFinished = false)
    }

    @Test
    fun `TC-7d6fdf91-b086-45d5-bba7-92302d3550a2 다시 시작 저장에 실패하면 안내 없이 화면과 입력, 다시 시작 동작이 그대로다`() {
        assertFinishFailed(isFinished = true)
    }

    private fun assertFinishFailed(isFinished: Boolean) {
        val environment = RoutineRecordTestEnvironment(isFinished = isFinished)
        // 저장에 실패한 요청은 진행 중인 상태만 거쳤다가 저장된 완료 여부를 바꾸지 않고 끝난다.
        val failed: () -> Unit = {
            environment.setFinishInProgress(isInProgress = true)
            environment.setFinishInProgress(isInProgress = false)
        }
        every { environment.routineViewModel.finish() } answers { failed() }
        every { environment.routineViewModel.restart() } answers { failed() }
        setScreen(environment)
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.waitForIdle()

        clickFinishAction(isFinished = isFinished)

        environment.navigateUpCount shouldBe 0
        composeRule.routineRecordTitleInput().routineRecordEditableText() shouldBe ROUTINE_RECORD_TITLE_EDITED
        composeRule.onNode(routineRecordProgress(), useUnmergedTree = true).assertDoesNotExist()
        assertFinishActionOnly(isFinished = isFinished)
        assertNoFeedback()
        composeRule.onAllNodesWithText(RETRY, substring = true, ignoreCase = true).assertCountEquals(0)

        clickFinishAction(isFinished = isFinished)

        verify(exactly = if (isFinished) 0 else 2) { environment.routineViewModel.finish() }
        verify(exactly = if (isFinished) 2 else 0) { environment.routineViewModel.restart() }
    }

    @Test
    fun `TC-1730ef1e-2b33-4a6e-b8cf-3bf13a1ca3ec 완료된 루틴에서 제목을 고쳐 수정을 반영하면 화면 제목이 바뀌고 다시 시작이 그대로다`() {
        val environment = finishedEnvironment()
        environment.storeOnUpdate()
        setScreen(environment)
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.waitForIdle()

        composeRule.routineRecordClickUpdate()

        verify(exactly = 1) { environment.routineViewModel.update(input = any()) }
        environment.currentDetail.title shouldBe ROUTINE_RECORD_TITLE_EDITED
        composeRule.onNode(routineDetailTopBarTitle(ROUTINE_RECORD_TITLE_EDITED), useUnmergedTree = true).assertExists()
        assertFinishActionOnly(isFinished = true)
    }

    @Test
    fun `TC-1730ef1e-2b33-4a6e-b8cf-3bf13a1ca3ec 완료된 루틴에서 기록을 넣으면 기록 내역에 나타나고 다시 시작이 그대로다`() {
        val environment = finishedEnvironment()
        environment.storeOnAdd()
        setScreen(environment)
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordInputAmount("500")

        composeRule.routineRecordClickAdd()

        composeRule.routineRecordRowNameList() shouldBe
            listOf(
                routineRecordRowName(date = MARCH_10, amountText = "500ml"),
                routineRecordRowName(date = MARCH_5, amountText = "300ml"),
            )
        assertFinishActionOnly(isFinished = true)
    }

    @Test
    fun `TC-1730ef1e-2b33-4a6e-b8cf-3bf13a1ca3ec 완료된 루틴에서 기록을 빼면 기록 내역에서 사라지고 다시 시작이 그대로다`() {
        val environment = finishedEnvironment()
        environment.storeOnDelete()
        setScreen(environment)

        composeRule.routineRecordClickRemove(date = MARCH_5, amountText = "300ml")

        composeRule.routineRecordRowNameList() shouldBe emptyList()
        assertFinishActionOnly(isFinished = true)
    }

    @Test
    fun `TC-1730ef1e-2b33-4a6e-b8cf-3bf13a1ca3ec 완료된 루틴에서 태그를 연결하고 해제하면 태그 입력에 반영되고 다시 시작이 그대로다`() {
        val environment = finishedEnvironment()
        val tagList = listOf(tagA, tagB)
        every { environment.tagViewModel.add(tagId = any()) } answers {
            val tagId = firstArg<Uuid>()
            environment.tagUiState.update { state -> state.copy(tagList = state.tagList + tagList.filter { tag -> tag.id == tagId }) }
        }
        every { environment.tagViewModel.remove(tagId = any()) } answers {
            val tagId = firstArg<Uuid>()
            environment.tagUiState.update { state -> state.copy(tagList = state.tagList.filterNot { tag -> tag.id == tagId }) }
        }
        setScreen(environment)

        composeRule.routineRecordOpenTagPicker()
        composeRule.routineRecordAwaitText(text = TAG_B_TITLE)
        composeRule.onNodeWithText(TAG_B_TITLE).performClick()
        composeRule.waitForIdle()
        composeRule.routineRecordOpenTagPicker()
        composeRule.routineRecordAwaitText(text = TAG_A_TITLE, count = 2)
        composeRule.onAllNodesWithText(TAG_A_TITLE).onLast().performClick()
        composeRule.waitForIdle()

        environment.tagUiState.value shouldBe EntityTagInputUiState(tagList = listOf(tagB))
        composeRule.onNode(hasText(TAG_B_TITLE) and !hasAnyAncestor(isDialog())).assertExists()
        composeRule.onNode(hasText(TAG_A_TITLE) and !hasAnyAncestor(isDialog())).assertDoesNotExist()
        assertFinishActionOnly(isFinished = true)
    }

    @Test
    fun `TC-1730ef1e-2b33-4a6e-b8cf-3bf13a1ca3ec 완료된 루틴에서 삭제를 선택하면 한 번 요청하고 진입 전 화면으로 돌아간다`() {
        val environment = finishedEnvironment()
        every { environment.routineViewModel.delete() } answers { environment.effect.trySend(RoutineDetailEffect.DeleteSucceeded) }
        setScreen(environment)

        composeRule.routineRecordClickDelete()

        verify(exactly = 1) { environment.routineViewModel.delete() }
        environment.navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-f64538e2-c9cf-4809-827e-7e83242d23e9 다른 경로로 완료되면 고친 제목을 둔 채 다시 시작만 제공한다`() {
        assertExternalChange(isFinished = false)
    }

    @Test
    fun `TC-f64538e2-c9cf-4809-827e-7e83242d23e9 다른 경로로 다시 시작되면 고친 제목을 둔 채 완료만 제공한다`() {
        assertExternalChange(isFinished = true)
    }

    private fun assertExternalChange(isFinished: Boolean) {
        val environment = RoutineRecordTestEnvironment(isFinished = isFinished)
        setScreen(environment)
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.waitForIdle()

        // 목록의 카드, 검색 결과, AI 도구와 다른 기기의 변경은 모두 기기에 저장된 완료 여부를 바꾼다.
        composeRule.runOnIdle { environment.setFinished(isFinished = !isFinished) }
        composeRule.waitForIdle()

        assertFinishActionOnly(isFinished = !isFinished)
        composeRule.routineRecordTitleInput().routineRecordEditableText() shouldBe ROUTINE_RECORD_TITLE_EDITED
        verify(exactly = 0) { environment.routineViewModel.finish() }
        verify(exactly = 0) { environment.routineViewModel.restart() }
    }

    @Test
    fun `TC-dad09e17-46c3-4f4e-b96b-07660db2c031 완료한 뒤 화면이 재생성되어도 다시 시작만 제공한다`() {
        assertFinishActionAfter(boundary = Boundary.RECREATE)
    }

    @Test
    fun `TC-dad09e17-46c3-4f4e-b96b-07660db2c031 완료한 뒤 백그라운드에 다녀와도 다시 시작만 제공한다`() {
        assertFinishActionAfter(boundary = Boundary.BACKGROUND)
    }

    @Test
    fun `TC-dad09e17-46c3-4f4e-b96b-07660db2c031 완료한 뒤 메모리 정리 뒤 복원해도 다시 시작만 제공한다`() {
        assertFinishActionAfter(boundary = Boundary.PROCESS_DEATH)
    }

    private fun assertFinishActionAfter(boundary: Boundary) {
        val environment = RoutineRecordTestEnvironment(isFinished = false)
        environment.storeOnFinish()
        val restorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        restorationTester.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                // 메모리 정리 뒤에는 ViewModel도 새로 만들어지므로, 복원할 때마다 새 대역을 만든다.
                if (boundary == Boundary.PROCESS_DEATH) {
                    RoutineRecordTestScreen(
                        environment = environment,
                        routineViewModel = remember { environment.newRoutineViewModel() },
                        recordViewModel = remember { environment.newRecordViewModel() },
                    )
                } else {
                    RoutineRecordTestScreen(environment = environment)
                }
            }
        }
        composeRule.waitForIdle()
        clickFinishAction(isFinished = false)
        assertFinishActionOnly(isFinished = true)

        when (boundary) {
            Boundary.BACKGROUND -> {
                composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
                composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
            }

            Boundary.RECREATE, Boundary.PROCESS_DEATH -> {
                restorationTester.emulateSavedInstanceStateRestore()
            }
        }
        composeRule.waitForIdle()

        assertFinishActionOnly(isFinished = true)
    }

    @Test
    fun `TC-84a50df2-a720-4cb8-a237-2e8cf05ac891 완료되지 않은 루틴의 내용을 수정해도 완료 여부는 그대로다`() {
        assertFinishedKeptAfterUpdate(isFinished = false)
    }

    @Test
    fun `TC-84a50df2-a720-4cb8-a237-2e8cf05ac891 완료된 루틴의 내용을 수정해도 완료 여부는 그대로다`() {
        assertFinishedKeptAfterUpdate(isFinished = true)
    }

    private fun assertFinishedKeptAfterUpdate(isFinished: Boolean) {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1), goalAmount = 2000), isFinished = isFinished)
        environment.storeOnUpdate()
        setScreen(environment)
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.routineRecordGoalInput().performTextReplacement("3000")
        composeRule.waitForIdle()
        composeRule.routineRecordPickFormDate(from = LocalDate(2026, 3, 1), to = MARCH_10)

        composeRule.routineRecordClickUpdate()

        environment.currentDetail.title shouldBe ROUTINE_RECORD_TITLE_EDITED
        environment.currentDetail.startDate shouldBe MARCH_10
        environment.currentDetail.goalAmount shouldBe 3000
        verify(exactly = 0) { environment.routineViewModel.finish() }
        verify(exactly = 0) { environment.routineViewModel.restart() }
        assertFinishActionOnly(isFinished = isFinished)
    }

    @Test
    fun `TC-da594be8-f952-4150-933f-cf3906b3f43f 고치지 않은 채 이 화면에서 완료해도 수정 동작이 없다`() {
        assertUpdateActionAfterFinishChange(isEdited = false, isFinished = false, isExternal = false)
    }

    @Test
    fun `TC-da594be8-f952-4150-933f-cf3906b3f43f 고치지 않은 채 이 화면에서 다시 시작해도 수정 동작이 없다`() {
        assertUpdateActionAfterFinishChange(isEdited = false, isFinished = true, isExternal = false)
    }

    @Test
    fun `TC-da594be8-f952-4150-933f-cf3906b3f43f 고치지 않은 채 다른 경로에서 완료되어도 수정 동작이 없다`() {
        assertUpdateActionAfterFinishChange(isEdited = false, isFinished = false, isExternal = true)
    }

    @Test
    fun `TC-da594be8-f952-4150-933f-cf3906b3f43f 제목을 고친 채 이 화면에서 완료해도 수정 동작이 있다`() {
        assertUpdateActionAfterFinishChange(isEdited = true, isFinished = false, isExternal = false)
    }

    @Test
    fun `TC-da594be8-f952-4150-933f-cf3906b3f43f 제목을 고친 채 이 화면에서 다시 시작해도 수정 동작이 있다`() {
        assertUpdateActionAfterFinishChange(isEdited = true, isFinished = true, isExternal = false)
    }

    private fun assertUpdateActionAfterFinishChange(
        isEdited: Boolean,
        isFinished: Boolean,
        isExternal: Boolean,
    ) {
        val environment = RoutineRecordTestEnvironment(isFinished = isFinished)
        environment.storeOnFinish()
        setScreen(environment)
        if (isEdited) {
            composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
            composeRule.waitForIdle()
        }

        if (isExternal) {
            composeRule.runOnIdle { environment.setFinished(isFinished = !isFinished) }
            composeRule.waitForIdle()
        } else {
            clickFinishAction(isFinished = isFinished)
        }

        assertFinishActionOnly(isFinished = !isFinished)
        if (isEdited) {
            composeRule.onNode(routineRecordUpdateButton()).assertExists()
        } else {
            composeRule.onNode(routineRecordUpdateButton()).assertDoesNotExist()
        }
    }

    @Test
    @Config(sdk = [36], qualifiers = "ko-w480dp-h2400dp")
    fun `TC-c1e19923-3022-4512-8d2a-25bb8636c352 완료된 루틴은 기록을 넣거나 빼도 카드 문구가 완료 그대로다`() {
        val routine =
            Arb
                .routine(
                    isDeleted = false,
                    detail =
                        routineRecordDetailOf(
                            startDate = LocalDate(2026, 3, 1),
                            cycle = RoutineCycle.DAILY,
                            unit = "ml",
                            goalAmount = 2000,
                        ),
                    isFinished = true,
                ).next()
        // 오늘 날짜의 기록 1000에 250을 넣은 뒤와 그 기록 1000을 뺀 뒤의 오늘 합계다.
        val cycleAmountList = listOf(1000L, 1250L, 0L)
        var cardTextList: List<Triple<String, Int?, String?>> = emptyList()

        composeRule.setContent {
            cardTextList =
                cycleAmountList.map { cycleAmount ->
                    val cardText = RoutineSummary(routine = routine, cycleAmount = cycleAmount).toCardText(today = TODAY)

                    Triple(cardText.text, cardText.achievementRate, cardText.achievementRateText)
                }
        }
        composeRule.waitForIdle()

        cardTextList shouldBe cycleAmountList.map { Triple("완료", null, null) }
    }

    private fun finishedEnvironment(): RoutineRecordTestEnvironment {
        val id = Arb.kotlinUuid().next()

        return RoutineRecordTestEnvironment(
            id = id,
            recordList = listOf(routineRecordOf(date = MARCH_5, amount = 300, routineId = id)),
            tagList = listOf(tagA),
            selectableTagList = listOf(tagA, tagB),
            isFinished = true,
        )
    }

    // 제목을 고치고, 기록할 날짜를 고르고 기록의 양을 입력한다.
    private fun editInput() {
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.waitForIdle()
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordInputAmount("500")
    }

    private fun assertInputKept() {
        composeRule.routineRecordTitleInput().routineRecordEditableText() shouldBe ROUTINE_RECORD_TITLE_EDITED
        composeRule.onNode(routineRecordDateButton(MARCH_10)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "500"
    }

    private fun assertNoFeedback() {
        composeRule.onNode(routineRecordSnackbar()).assertDoesNotExist()
        composeRule.onAllNodesWithText(UNDO, substring = true, ignoreCase = true).assertCountEquals(0)
    }

    private fun finishAction(isFinished: Boolean) = if (isFinished) routineDetailRestartButton() else routineDetailFinishButton()

    private fun clickFinishAction(isFinished: Boolean) {
        composeRule.onNode(finishAction(isFinished = isFinished)).performClick()
        composeRule.waitForIdle()
    }

    private fun assertFinishActionOnly(isFinished: Boolean) {
        composeRule.onNode(finishAction(isFinished = isFinished)).assertExists()
        composeRule.onNode(finishAction(isFinished = !isFinished)).assertDoesNotExist()
    }

    private fun setScreen(environment: RoutineRecordTestEnvironment) {
        composeRule.setContent { RoutineRecordTestScreen(environment = environment) }
        composeRule.waitForIdle()
    }

    private enum class Boundary {
        RECREATE,
        BACKGROUND,
        PROCESS_DEATH,
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 3, 20)
        val MARCH_5: LocalDate = LocalDate(2026, 3, 5)
        val MARCH_10: LocalDate = LocalDate(2026, 3, 10)
        const val UNDO = "undo"
        const val RETRY = "retry"
        const val TAG_A_TITLE = "RoutineDetailFinishTagA"
        const val TAG_B_TITLE = "RoutineDetailFinishTagB"

        fun tagOf(title: String): Tag =
            Arb.tag().next().let { tag ->
                tag.copy(detail = tag.detail.copy(emoji = "", title = title), isFinished = false, isDeleted = false)
            }
    }
}
