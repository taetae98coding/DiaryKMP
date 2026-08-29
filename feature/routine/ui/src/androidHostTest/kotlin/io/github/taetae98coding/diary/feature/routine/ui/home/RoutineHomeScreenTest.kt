package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ROUTINE-HOME-FEATURE-002 루틴 추가 버튼을 선택하면 루틴 추가 화면으로 이동한다`() {
        var navigateToAddCount = 0
        setRoutineHomeScreen(navigateToAdd = { navigateToAddCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateToAddCount shouldBe 1
    }

    @Test
    fun `TC-ROUTINE-HOME-FEATURE-003 Cmd A 단축키를 입력하면 루틴 추가 화면으로 한 번 이동한다`() {
        var navigateToAddCount = 0
        setRoutineHomeScreen(navigateToAdd = { navigateToAddCount += 1 })

        performAddShortcut()

        navigateToAddCount shouldBe 1
    }

    @Test
    fun `TC-ROUTINE-LIST-DETAIL-FEATURE-003 추가 버튼을 표시하지 않으면 단축키로도 이동하지 않는다`() {
        var navigateToAddCount = 0
        setRoutineHomeScreen(
            navigateToAdd = { navigateToAddCount += 1 },
            componentVisible = RoutineHomeScaffoldComponentVisible(isAddButtonVisible = false),
        )

        performAddShortcut()

        navigateToAddCount shouldBe 0
    }

    private fun performAddShortcut() {
        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()
    }

    private fun setRoutineHomeScreen(
        navigateToAdd: () -> Unit = {},
        componentVisible: RoutineHomeScaffoldComponentVisible = RoutineHomeScaffoldComponentVisible(),
    ) {
        val viewModel = mockk<RoutineHomeViewModel>()
        every { viewModel.uiState } returns MutableStateFlow(RoutineHomeUiState())

        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScreen(
                    navigateToAdd = navigateToAdd,
                    componentVisibleProvider = { componentVisible },
                    viewModel = viewModel,
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add routine"
    }
}
