package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_DAILY
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineAddFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormAmountPerRecordInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDateButton
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDisplayedColorHex
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormEditableText
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormGoalAmountInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputtedUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectedCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.today
import io.github.taetae98coding.diary.feature.routine.ui.routineAddTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineAddViewModel
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class RoutineAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-edbcdc9b-03b0-4a1e-99d2-edba55ad2074 화면에 처음 진입하면 제목과 설명이 비어 있다`() {
        setRoutineAddScreen()

        composeRule.titleInput().editableText() shouldBe ""
        composeRule.descriptionInput().editableText() shouldBe ""
    }

    @Test
    fun `TC-edbcdc9b-03b0-4a1e-99d2-edba55ad2074 화면에 처음 진입하면 컬러가 제시되고 시작 날짜는 오늘, 나머지는 처음 값이다`() {
        setRoutineAddScreen()
        composeRule.waitForIdle()

        composeRule.routineFormDisplayedColorHex().length shouldBe COLOR_HEX_LENGTH
        composeRule.routineFormDateButton(today()).assertExists()
        composeRule.routineFormSelectedCycle() shouldBe ROUTINE_FORM_DEFAULT_CYCLE_DAILY
        composeRule.routineFormInputtedUnit() shouldBe ""
        composeRule.routineFormGoalAmountInput().routineFormEditableText() shouldBe ""
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe "1"
    }

    @Test
    fun `TC-aee23c66-2db7-41d8-9fec-cb549d0296ca 새 루틴의 입력은 매일, 빈 단위, 한 번에 기록할 양 1이고 목표량이 없다`() {
        var input: RoutineInput? = null

        composeRule.setContent {
            RoutineTestTheme {
                val state = rememberRoutineAddFormState()

                SideEffect { input = state.input }
            }
        }
        composeRule.waitForIdle()

        val newInput = input.shouldNotBeNull()
        newInput.cycle shouldBe RoutineCycle.DAILY
        newInput.unit shouldBe ""
        newInput.amountPerRecord shouldBe 1
        newInput.goalAmount shouldBe null
    }

    @Test
    fun `TC-6a87a917-4e61-4b0e-a4a1-df18f580ae6c 화면에 처음 진입하면 제목 입력에 초점이 있다`() {
        setRoutineAddScreen()

        composeRule.waitForIdle()

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-c184ab41-d30d-4136-8434-2a5d1b8fd176 입력한 제목과 설명을 화면에 유지한다`() {
        val title = TYPED_TITLE
        val description = TYPED_DESCRIPTION
        setRoutineAddScreen()

        composeRule.titleInput().performTextInput(title)
        composeRule.descriptionInput().performTextInput(description)
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe title
        composeRule.descriptionInput().editableText() shouldBe description
    }

    @Test
    fun `TC-f731c7b3-d20c-4718-ae02-b9287ed7d17a 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setRoutineAddScreen(navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-40815f60-f4ef-47e0-b6a5-8df2a5c18365 단독으로 표시되면 뒤로가기 버튼이 표시된다`() {
        setRoutineAddScreen(componentVisible = RoutineAddScaffoldComponentVisible(isNavigateUpButtonVisible = true))

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-b8284f0b-8b8d-418a-aab5-be224f5a025d 목록과 함께 표시되면 뒤로가기 버튼이 표시되지 않는다`() {
        setRoutineAddScreen(componentVisible = RoutineAddScaffoldComponentVisible(isNavigateUpButtonVisible = false))

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
    }

    private fun setRoutineAddScreen(
        navigateUp: () -> Unit = {},
        componentVisible: RoutineAddScaffoldComponentVisible = RoutineAddScaffoldComponentVisible(),
    ) {
        composeRule.setContent {
            RoutineTestTheme {
                RoutineAddScreen(
                    navigateUp = navigateUp,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = Uuid.NIL,
                    componentVisibleProvider = { componentVisible },
                    addViewModel = remember { routineAddViewModel() },
                    tagViewModel = remember { routineAddTagViewModel() },
                )
            }
        }
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

        private const val TYPED_TITLE = "Morning stretch"
        private const val TYPED_DESCRIPTION = "Ten minutes after waking up"

        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val COLOR_HEX_LENGTH = 7
    }
}
