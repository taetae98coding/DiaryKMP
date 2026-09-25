package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

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

    @Test
    fun `TC-ROUTINE-ADD-FEATURE-008 메모리 정리 뒤 복원해도 작성 중이던 제목과 설명을 모두 복원한다`() {
        val title = "title-${fixtureMonkey.giveMeOne<String>()}"
        val description = "description-${fixtureMonkey.giveMeOne<String>()}"
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 이 화면은 화면 상태를 들고 있는 별도 객체 없이 저장 가능한 입력 상태만 쓰므로, 복원하면 입력 상태도 저장된 값에서 새로 만들어진다.
            DiaryTheme {
                RoutineAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
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
