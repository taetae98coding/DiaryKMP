package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInputUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInputtedUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineSelectCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineSelectedCycle
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 입력과 기록 내역이 한 화면에 모두 놓이도록 세로로 긴 창에서 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h2400dp")
class RoutineDetailRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

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
    fun `TC-d222c046-e8d8-4f29-8c63-35f2e913fc91 화면이 재생성되어도 입력 중이던 내용과 기록 넣기의 날짜와 양을 유지한다`() {
        assertInputKept(boundary = Boundary.RECREATE)
    }

    @Test
    fun `TC-d222c046-e8d8-4f29-8c63-35f2e913fc91 백그라운드에 다녀와도 입력 중이던 내용과 기록 넣기의 날짜와 양을 유지한다`() {
        assertInputKept(boundary = Boundary.BACKGROUND)
    }

    @Test
    fun `TC-d222c046-e8d8-4f29-8c63-35f2e913fc91 메모리 정리 뒤 복원해도 입력 중이던 내용과 기록 넣기의 날짜와 양을 유지한다`() {
        assertInputKept(boundary = Boundary.PROCESS_DEATH)
    }

    private fun assertInputKept(boundary: Boundary) {
        val colorA = routineRecordColor()
        val colorB = routineRecordOtherColor(colorA)
        val environment =
            RoutineRecordTestEnvironment(
                detail = routineRecordDetailOf(color = colorA, startDate = LocalDate(2026, 3, 1)),
                recordList = listOf(routineRecordOf(date = LocalDate(2026, 3, 5), amount = 300)),
            )
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
        editEveryInput(colorA = colorA, colorB = colorB)
        val routineViewModelCount = environment.routineViewModelList.size
        val recordViewModelCount = environment.recordViewModelList.size

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

        if (boundary == Boundary.PROCESS_DEATH) {
            environment.routineViewModelList.size shouldBe routineViewModelCount + 1
            environment.recordViewModelList.size shouldBe recordViewModelCount + 1
        }
        assertEveryInputKept(colorB = colorB)
    }

    private fun editEveryInput(
        colorA: Long,
        colorB: Long,
    ) {
        composeRule.waitForIdle()
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.routineRecordDescriptionInput().performTextReplacement(EDITED_DESCRIPTION)
        composeRule.waitForIdle()
        composeRule.routineRecordChangeColor(fromHex = colorA.routineRecordColorHex(), toHex = colorB.routineRecordColorHex())
        composeRule.routineRecordPickFormDate(from = LocalDate(2026, 3, 1), to = EDITED_START_DATE)
        composeRule.routineSelectCycle(option = ROUTINE_RECORD_DEFAULT_CYCLE_WEEKLY)
        composeRule.routineInputUnit(unit = ROUTINE_RECORD_UNIT_METER)
        composeRule.routineRecordGoalInput().performTextReplacement(EDITED_GOAL)
        composeRule.routineRecordAmountPerRecordInput().performTextClearance()
        composeRule.waitForIdle()
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordInputAmount("500")
    }

    private fun assertEveryInputKept(colorB: Long) {
        composeRule.routineRecordTitleInput().routineRecordEditableText() shouldBe ROUTINE_RECORD_TITLE_EDITED
        composeRule.routineRecordDescriptionInput().routineRecordEditableText() shouldBe EDITED_DESCRIPTION
        composeRule.routineRecordDisplayedColorHex() shouldBe colorB.routineRecordColorHex()
        composeRule.routineRecordFormDateButton(EDITED_START_DATE).assertExists()
        composeRule.routineSelectedCycle() shouldBe ROUTINE_RECORD_DEFAULT_CYCLE_WEEKLY
        composeRule.routineInputtedUnit() shouldBe ROUTINE_RECORD_UNIT_METER
        composeRule.routineRecordGoalInput().routineRecordEditableText() shouldBe EDITED_GOAL
        composeRule.routineRecordAmountPerRecordInput().routineRecordEditableText() shouldBe ""
        composeRule.onNode(routineRecordDateButton(MARCH_10)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "500"
        composeRule.onNode(routineRecordUpdateButton()).assertExists()
        // 기록은 저장된 단위로 읽히므로, 반영하지 않은 단위는 기록의 양의 문구를 바꾸지 않는다.
        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = LocalDate(2026, 3, 5), amountText = "300ml"))
    }

    @Test
    fun `TC-13eb8f87-59d1-49c6-844d-18113fe37bb6 시작 날짜를 고르는 중에 화면이 재생성되어도 고르기가 열린 채 남는다`() {
        assertPickerKept(picker = Picker.START_DATE, isRecreate = true)
    }

    @Test
    fun `TC-13eb8f87-59d1-49c6-844d-18113fe37bb6 시작 날짜를 고르는 중에 백그라운드에 다녀와도 고르기가 열린 채 남는다`() {
        assertPickerKept(picker = Picker.START_DATE, isRecreate = false)
    }

    @Test
    fun `TC-13eb8f87-59d1-49c6-844d-18113fe37bb6 기록 날짜를 고르는 중에 화면이 재생성되어도 고르기가 열린 채 남는다`() {
        assertPickerKept(picker = Picker.RECORD_DATE, isRecreate = true)
    }

    @Test
    fun `TC-13eb8f87-59d1-49c6-844d-18113fe37bb6 기록 날짜를 고르는 중에 백그라운드에 다녀와도 고르기가 열린 채 남는다`() {
        assertPickerKept(picker = Picker.RECORD_DATE, isRecreate = false)
    }

    private fun assertPickerKept(
        picker: Picker,
        isRecreate: Boolean,
    ) {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1)))
        val restorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        restorationTester.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                RoutineRecordTestScreen(environment = environment)
            }
        }
        composeRule.waitForIdle()
        val date =
            when (picker) {
                Picker.START_DATE -> LocalDate(2026, 3, 1)
                Picker.RECORD_DATE -> TODAY
            }
        when (picker) {
            Picker.START_DATE -> composeRule.routineRecordOpenFormDatePicker(date)
            Picker.RECORD_DATE -> composeRule.routineRecordOpenDatePicker()
        }
        composeRule.routineRecordPickerSelection(date).assertExists()

        if (isRecreate) {
            restorationTester.emulateSavedInstanceStateRestore()
        } else {
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
            composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ROUTINE_RECORD_DEFAULT_DATE_PICKER_CONFIRM).assertExists()
        composeRule.routineRecordPickerSelection(date).assertExists()
    }

    @Test
    fun `TC-69c4534a-d570-401c-a313-1c818fb5cbd8 화면이 재생성되면 수정 성공 피드백을 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = RoutineDetailEffect.UpdateSucceeded, message = ROUTINE_RECORD_DEFAULT_UPDATE_SUCCEEDED)
    }

    @Test
    fun `TC-69c4534a-d570-401c-a313-1c818fb5cbd8 화면이 재생성되면 목표량 미입력 안내를 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = RoutineDetailEffect.GoalAmountInvalid, message = ROUTINE_RECORD_DEFAULT_GOAL_INVALID)
    }

    @Test
    fun `TC-69c4534a-d570-401c-a313-1c818fb5cbd8 화면이 재생성되면 한 번에 기록할 양 미입력 안내를 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = RoutineDetailEffect.AmountPerRecordInvalid, message = ROUTINE_RECORD_DEFAULT_AMOUNT_PER_RECORD_INVALID)
    }

    private fun assertFeedbackNotRestored(
        effect: RoutineDetailEffect,
        message: String,
    ) {
        val environment = RoutineRecordTestEnvironment()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { RoutineRecordTestScreen(environment = environment) }
        composeRule.waitForIdle()
        environment.effect.trySend(effect)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(message).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(message).assertDoesNotExist()
        composeRule.onNode(routineRecordSnackbar()).assertDoesNotExist()
    }

    @Test
    fun `TC-018c8b0c-91e9-49b4-b6f8-c6c52e0def8d 앱을 다시 실행해 진입하면 저장된 내용과 기록 넣기의 처음 값으로 다시 시작한다`() {
        val environment = RoutineRecordTestEnvironment(detail = routineRecordDetailOf(startDate = LocalDate(2026, 3, 1), goalAmount = 2000, amountPerRecord = 250))
        var launchCount by mutableIntStateOf(0)
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없고 ViewModel도 새로 만들어지므로, 새 키로 저장 상태 없이 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                RoutineRecordTestScreen(
                    environment = environment,
                    routineViewModel = remember { environment.newRoutineViewModel() },
                    recordViewModel = remember { environment.newRecordViewModel() },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.routineRecordGoalInput().performTextReplacement(EDITED_GOAL)
        composeRule.waitForIdle()
        composeRule.routineRecordPickDate(MARCH_10)
        composeRule.routineRecordInputAmount("500")
        composeRule.onNode(routineRecordUpdateButton()).assertExists()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.routineRecordTitleInput().routineRecordEditableText() shouldBe ROUTINE_RECORD_TITLE
        composeRule.routineRecordDescriptionInput().routineRecordEditableText() shouldBe ROUTINE_RECORD_DESCRIPTION
        composeRule.routineRecordFormDateButton(LocalDate(2026, 3, 1)).assertExists()
        composeRule.routineRecordGoalInput().routineRecordEditableText() shouldBe "2000"
        composeRule.routineRecordAmountPerRecordInput().routineRecordEditableText() shouldBe "250"
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "250"
        composeRule.onNode(routineRecordUpdateButton()).assertDoesNotExist()
    }

    private enum class Boundary {
        RECREATE,
        BACKGROUND,
        PROCESS_DEATH,
    }

    private enum class Picker {
        START_DATE,
        RECORD_DATE,
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 3, 20)
        val MARCH_10: LocalDate = LocalDate(2026, 3, 10)
        val EDITED_START_DATE: LocalDate = LocalDate(2026, 2, 1)
        const val EDITED_DESCRIPTION = "매일"
        const val EDITED_GOAL = "03000"
    }
}
