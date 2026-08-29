package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class RoutineAddScreenRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ROUTINE-ADD-FEATURE-005 화면이 재생성되어도 입력 중이던 제목과 설명을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                RoutineAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                )
            }
        }
        composeRule.titleInput().performTextInput(TITLE)
        composeRule.descriptionInput().performTextInput(DESCRIPTION)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe TITLE
        composeRule.descriptionInput().editableText() shouldBe DESCRIPTION
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

        private const val TITLE = "RoutineAddRestoredTitle"
        private const val DESCRIPTION = "RoutineAddRestoredDescription"
    }
}
