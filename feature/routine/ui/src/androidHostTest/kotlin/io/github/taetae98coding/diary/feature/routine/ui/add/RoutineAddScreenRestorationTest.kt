package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_DAILY
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_WEEKLY
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_UNIT_MILLILITER
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormAmountPerRecordInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDateButton
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormEditableText
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormGoalAmountInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputAmountPerRecord
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputGoalAmount
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputtedUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormPickDate
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectedCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.today
import io.github.taetae98coding.diary.feature.routine.ui.routineAddTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineAddViewModel
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class RoutineAddScreenRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-1a4c0e5b-a267-4f3a-98fe-4a6ea261e3c4 화면이 재생성되어도 입력 중이던 제목과 설명을 유지한다`() {
        val title = typedTitle()
        val description = typedDescription()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            RoutineTestTheme {
                RoutineAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = Uuid.NIL,
                    componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                    addViewModel = remember { routineAddViewModel() },
                    tagViewModel = remember { routineAddTagViewModel() },
                )
            }
        }
        composeRule.titleInput().performTextInput(title)
        composeRule.descriptionInput().performTextInput(description)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe title
        composeRule.descriptionInput().editableText() shouldBe description
    }

    @Test
    fun `TC-84d2562f-324f-4d75-ad50-6c3362f8c240 메모리 정리 뒤 복원해도 작성 중이던 제목과 설명을 모두 복원한다`() {
        val title = typedTitle()
        val description = typedDescription()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 이 화면은 화면 상태를 들고 있는 별도 객체 없이 저장 가능한 입력 상태만 쓰므로, 복원하면 입력 상태도 저장된 값에서 새로 만들어진다.
            RoutineTestTheme {
                RoutineAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = Uuid.NIL,
                    componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                    addViewModel = remember { routineAddViewModel() },
                    tagViewModel = remember { routineAddTagViewModel() },
                )
            }
        }
        composeRule.titleInput().performTextInput(title)
        composeRule.descriptionInput().performTextInput(description)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe title
        composeRule.descriptionInput().editableText() shouldBe description
    }

    @Test
    fun `TC-b910633a-ad71-4a5e-828a-c292a0f41cbc 앱을 다시 실행해 진입하면 새 입력 상태로 시작한다`() {
        val title = typedTitle()
        val description = typedDescription()
        var launchCount by mutableIntStateOf(0)
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                RoutineTestTheme {
                    RoutineAddScreen(
                        navigateUp = {},
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        tagAddRequestKey = Uuid.NIL,
                        componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                        addViewModel = remember { routineAddViewModel() },
                        tagViewModel = remember { routineAddTagViewModel() },
                    )
                }
            }
        }
        composeRule.titleInput().performTextInput(title)
        composeRule.descriptionInput().performTextInput(description)
        composeRule.routineFormPickDate(from = today(), to = LocalDate(2026, 3, 1))
        composeRule.routineFormSelectCycle(ROUTINE_FORM_DEFAULT_CYCLE_WEEKLY)
        composeRule.routineFormInputUnit(ROUTINE_FORM_UNIT_MILLILITER)
        composeRule.routineFormInputGoalAmount("2000")
        composeRule.routineFormInputAmountPerRecord("250")
        composeRule.waitForIdle()
        composeRule.titleInput().editableText() shouldBe title
        composeRule.routineFormInputtedUnit() shouldBe ROUTINE_FORM_UNIT_MILLILITER

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe ""
        composeRule.descriptionInput().editableText() shouldBe ""
        composeRule.routineFormDateButton(today()).assertExists()
        composeRule.routineFormSelectedCycle() shouldBe ROUTINE_FORM_DEFAULT_CYCLE_DAILY
        composeRule.routineFormInputtedUnit() shouldBe ""
        composeRule.routineFormGoalAmountInput().routineFormEditableText() shouldBe ""
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe "1"
    }

    private fun SemanticsNodeInteraction.editableText(): String =
        fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.EditableText)
            ?.text
            .orEmpty()

    private fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

    private fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

    private companion object {
        private const val TITLE_INPUT_INDEX = 0
        private const val DESCRIPTION_INPUT_INDEX = 1
    }
}

private fun typedTitle(): String = "title-${Arb.string().next()}"

private fun typedDescription(): String = "description-${Arb.string().next()}"
