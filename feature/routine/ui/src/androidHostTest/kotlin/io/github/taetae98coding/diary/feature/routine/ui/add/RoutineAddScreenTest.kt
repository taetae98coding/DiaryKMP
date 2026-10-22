package io.github.taetae98coding.diary.feature.routine.ui.add

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

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

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
    fun `TC-6a87a917-4e61-4b0e-a4a1-df18f580ae6c 화면에 처음 진입하면 제목 입력에 초점이 있다`() {
        setRoutineAddScreen()

        composeRule.waitForIdle()

        composeRule.titleInput().assertIsFocused()
    }

    @Test
    fun `TC-c184ab41-d30d-4136-8434-2a5d1b8fd176 입력한 제목과 설명을 화면에 유지한다`() {
        val title = fixtureMonkey.giveMeOne<String>()
        val description = fixtureMonkey.giveMeOne<String>()
        setRoutineAddScreen()

        composeRule.titleInput().performTextInput(title)
        composeRule.descriptionInput().performTextInput(description)
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe title
        composeRule.descriptionInput().editableText() shouldBe description
    }

    @Test
    fun `TC-8a81e75d-a18f-4b36-8f91-81c9c9ef26bc 추가를 실행해도 입력 내용과 화면 상태가 바뀌지 않는다`() {
        val title = fixtureMonkey.giveMeOne<String>()
        val description = fixtureMonkey.giveMeOne<String>()
        var navigateUpCount = 0
        setRoutineAddScreen(navigateUp = { navigateUpCount += 1 })
        composeRule.titleInput().performTextInput(title)
        composeRule.descriptionInput().performTextInput(description)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.titleInput().editableText() shouldBe title
        composeRule.descriptionInput().editableText() shouldBe description
        navigateUpCount shouldBe 0
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
            DiaryTheme {
                RoutineAddScreen(
                    navigateUp = navigateUp,
                    componentVisibleProvider = { componentVisible },
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

        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add routine"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
    }
}
