package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
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
    fun `TC-1a4c0e5b-a267-4f3a-98fe-4a6ea261e3c4 화면이 재생성되어도 입력 중이던 제목과 설명을 유지한다`() {
        val title = typedTitle()
        val description = typedDescription()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
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

    @Test
    fun `TC-84d2562f-324f-4d75-ad50-6c3362f8c240 메모리 정리 뒤 복원해도 작성 중이던 제목과 설명을 모두 복원한다`() {
        val title = typedTitle()
        val description = typedDescription()
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

    @Test
    fun `TC-b910633a-ad71-4a5e-828a-c292a0f41cbc 앱을 다시 실행해 진입하면 새 입력 상태로 시작한다`() {
        val title = typedTitle()
        val description = typedDescription()
        var launchCount by mutableIntStateOf(0)
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                DiaryTheme {
                    RoutineAddScreen(
                        navigateUp = {},
                        componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                    )
                }
            }
        }
        composeRule.titleInput().performTextInput(title)
        composeRule.descriptionInput().performTextInput(description)
        composeRule.waitForIdle()
        composeRule.titleInput().editableText() shouldBe title

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe ""
        composeRule.descriptionInput().editableText() shouldBe ""
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

private fun typedTitle(): String = "title-${fixtureMonkey.giveMeOne<String>()}"

private fun typedDescription(): String = "description-${fixtureMonkey.giveMeOne<String>()}"
