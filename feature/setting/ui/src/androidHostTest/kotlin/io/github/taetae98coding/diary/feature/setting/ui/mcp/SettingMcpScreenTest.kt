package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.feature.setting.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingMcpScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-b6f6c100-f235-4fe7-9e1a-646fd628695e 뒤로가면 이전 화면으로 돌아가고 설정은 바뀌지 않는다`() {
        var navigateUpCount = 0
        val viewModel = screenTestViewModel(SettingMcpUiState.Content(isEnabled = true, serverState = McpServerState.Off))
        setSettingMcpScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.changeEnabled(any()) }
    }

    @Test
    fun `TC-89b9a0f5-4c46-412e-b067-1fd08f8dd10c 연결 사용을 누르면 반대 값으로 저장을 요청한다`() {
        var navigateUpCount = 0
        val viewModel = screenTestViewModel(SettingMcpUiState.Content(isEnabled = false, serverState = McpServerState.Off))
        every { viewModel.changeEnabled(any()) } returns Unit
        setSettingMcpScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithText(DEFAULT_ENABLED_LABEL).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.changeEnabled(isEnabled = true) }
        navigateUpCount shouldBe 0
    }

    @Test
    fun `TC-96afd6dd-77e2-4fe8-8ded-56b705adb68d 동작 중일 때 주소를 복사하고 안내를 한 번 표시한다`() {
        val clipboard = mockk<Clipboard>()
        val clipEntry = slot<ClipEntry>()
        coEvery { clipboard.setClipEntry(capture(clipEntry)) } just runs
        val viewModel =
            screenTestViewModel(SettingMcpUiState.Content(isEnabled = true, serverState = McpServerState.Running(address = ADDRESS)))
        setSettingMcpScreen(viewModel = viewModel, clipboard = clipboard)

        composeRule.onNodeWithText(DEFAULT_STATE_RUNNING).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { clipboard.setClipEntry(any()) }
        clipEntry.captured.clipData
            .getItemAt(0)
            .text
            .toString() shouldBe ADDRESS
        composeRule.onAllNodesWithText(DEFAULT_ADDRESS_COPIED_MESSAGE).assertCountEquals(1)
        verify(exactly = 0) { viewModel.changeEnabled(any()) }
    }

    private fun setSettingMcpScreen(
        viewModel: SettingMcpViewModel,
        navigateUp: () -> Unit = {},
        clipboard: Clipboard = mockk(relaxed = true),
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalClipboard provides clipboard) {
                DiaryTheme {
                    SettingMcpScreen(
                        navigateUp = navigateUp,
                        componentVisibleProvider = { SettingMcpScaffoldComponentVisible() },
                        viewModel = viewModel,
                    )
                }
            }
        }
    }

    public companion object {
        private const val ADDRESS = "http://127.0.0.1:38450/mcp"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ENABLED_LABEL = "Use MCP server"
        private const val DEFAULT_STATE_RUNNING = "Running"
        private const val DEFAULT_ADDRESS_COPIED_MESSAGE = "Address copied."

        private fun screenTestViewModel(uiState: SettingMcpUiState): SettingMcpViewModel {
            val viewModel = mockk<SettingMcpViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(uiState)
            return viewModel
        }
    }
}
