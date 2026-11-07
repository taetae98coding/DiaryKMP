package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isOff
import androidx.compose.ui.test.isOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingMcpScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 MCP 설정이다`() {
        setSettingMcpScaffold()

        composeRule.onNodeWithText("MCP 설정").assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 MCP Settings다`() {
        setSettingMcpScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `기본 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setSettingMcpScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-babc55c7-b2bc-4c88-a5b6-7752ff29f25d 확인 전에는 안내만 표시하고 켜고 끄기와 연결 상태를 두지 않는다`() {
        setSettingMcpScaffold(uiStateProvider = { SettingMcpUiState.Loading })

        displayedTexts() shouldContainExactlyInAnyOrder
            listOf(DEFAULT_TITLE, DEFAULT_CONNECTION_LABEL, DEFAULT_CONNECTION_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_ENABLED_LABEL).assertDoesNotExist()
        composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).assertDoesNotExist()
        stateLabelList.forEach { label -> composeRule.onNodeWithText(label).assertDoesNotExist() }
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-babc55c7-b2bc-4c88-a5b6-7752ff29f25d 한국어 환경에서 확인 전에도 안내를 표시한다`() {
        setSettingMcpScaffold(uiStateProvider = { SettingMcpUiState.Loading })

        composeRule.onNodeWithText("AI 도구 연결").assertExists()
        composeRule
            .onNodeWithText("켜면 이 Mac의 AI 도구가 확인 없이 현재 계정의 메모와 항목을 조회하고 추가, 수정, 삭제할 수 있습니다.")
            .assertExists()
        composeRule.onNodeWithText("MCP 서버 사용").assertDoesNotExist()
    }

    @Test
    fun `TC-30ea3b2b-ac06-4fc0-a7b3-9f87fa6aecf0 저장된 연결 사용 설정을 표시한다`() {
        val uiState = mutableStateOf(content(isEnabled = false, serverState = McpServerState.Off))
        setSettingMcpScaffold(uiStateProvider = { uiState.value })

        composeRule.onNodeWithText(DEFAULT_ENABLED_LABEL).assert(isOff())

        composeRule.runOnIdle { uiState.value = content(isEnabled = true, serverState = McpServerState.Off) }

        composeRule.onNodeWithText(DEFAULT_ENABLED_LABEL).assert(isOn())
    }

    @Test
    fun `TC-89b9a0f5-4c46-412e-b067-1fd08f8dd10c 연결 사용을 누르면 반대 값으로 바꾸기를 요청한다`() {
        val eventList = mutableListOf<SettingMcpScaffoldEvent>()
        val uiState = mutableStateOf(content(isEnabled = false, serverState = McpServerState.Off))
        setSettingMcpScaffold(uiStateProvider = { uiState.value }, onEvent = { event -> eventList += event })

        listOf(false, true).forEach { isEnabled ->
            composeRule.runOnIdle {
                uiState.value = content(isEnabled = isEnabled, serverState = McpServerState.Off)
                eventList.clear()
            }

            composeRule.onNodeWithText(DEFAULT_ENABLED_LABEL).performClick()
            composeRule.waitForIdle()

            eventList shouldBe listOf(SettingMcpScaffoldEvent.ChangeEnabled(isEnabled = !isEnabled))
        }
    }

    @Test
    fun `TC-09ab3adb-4b9b-45c4-ad6c-6c1d35a64473 연결 상태에 맞는 안내와 주소를 표시한다`() {
        val uiState = mutableStateOf(content(isEnabled = false, serverState = McpServerState.Off))
        setSettingMcpScaffold(uiStateProvider = { uiState.value })

        stateCaseList().forEach { (serverState, label, address) ->
            composeRule.runOnIdle { uiState.value = content(isEnabled = true, serverState = serverState) }

            composeRule.onNodeWithText(label).assertExists()
            (stateLabelList - label).forEach { otherLabel -> composeRule.onNodeWithText(otherLabel).assertDoesNotExist() }
            if (address == null) {
                composeRule.onNodeWithText(ADDRESS).assertDoesNotExist()
            } else {
                composeRule.onNodeWithText(address).assertExists()
            }
        }
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-09ab3adb-4b9b-45c4-ad6c-6c1d35a64473 한국어 환경에서 연결 상태 안내를 표시한다`() {
        val uiState = mutableStateOf(content(isEnabled = false, serverState = McpServerState.Off))
        setSettingMcpScaffold(uiStateProvider = { uiState.value })

        listOf(
            McpServerState.Off to "꺼져 있음",
            McpServerState.Running(address = ADDRESS) to "동작 중",
            McpServerState.AddressInUse(address = ADDRESS) to "다른 프로그램이 주소를 쓰고 있어 시작하지 못했습니다",
            McpServerState.StartFailed to "연결을 시작하지 못했습니다",
        ).forEach { (serverState, label) ->
            composeRule.runOnIdle { uiState.value = content(isEnabled = true, serverState = serverState) }

            composeRule.onNodeWithText(label).assertExists()
        }
    }

    @Test
    fun `TC-78bc132a-5ecc-4a32-80a3-c4433b22fd2d 연결 상태가 바뀌면 바뀐 안내와 주소를 표시한다`() {
        val uiState = mutableStateOf(content(isEnabled = true, serverState = McpServerState.Off))
        setSettingMcpScaffold(uiStateProvider = { uiState.value })
        composeRule.onNodeWithText(DEFAULT_STATE_OFF).assertExists()

        composeRule.runOnIdle { uiState.value = content(isEnabled = true, serverState = McpServerState.Running(address = ADDRESS)) }

        composeRule.onNodeWithText(DEFAULT_STATE_RUNNING).assertExists()
        composeRule.onNodeWithText(ADDRESS).assertExists()
        composeRule.onNodeWithText(DEFAULT_STATE_OFF).assertDoesNotExist()
    }

    @Test
    fun `TC-96afd6dd-77e2-4fe8-8ded-56b705adb68d 동작 중이면 상태 줄을 눌러 주소 복사를 요청한다`() {
        val eventList = mutableListOf<SettingMcpScaffoldEvent>()
        setSettingMcpScaffold(
            uiStateProvider = { content(isEnabled = true, serverState = McpServerState.Running(address = ADDRESS)) },
            onEvent = { event -> eventList += event },
        )

        composeRule.onNodeWithText(DEFAULT_STATE_RUNNING).assert(hasClickAction()).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(SettingMcpScaffoldEvent.ClickCopyAddress(address = ADDRESS))
    }

    @Test
    fun `TC-229a7f57-7608-45fe-92ee-7d8958cb7bc2 동작 중이 아니면 주소를 복사할 수 없다`() {
        val eventList = mutableListOf<SettingMcpScaffoldEvent>()
        val uiState = mutableStateOf(content(isEnabled = false, serverState = McpServerState.Off))
        setSettingMcpScaffold(uiStateProvider = { uiState.value }, onEvent = { event -> eventList += event })

        listOf(
            McpServerState.Off to DEFAULT_STATE_OFF,
            McpServerState.AddressInUse(address = ADDRESS) to DEFAULT_STATE_ADDRESS_IN_USE,
            McpServerState.StartFailed to DEFAULT_STATE_START_FAILED,
        ).forEach { (serverState, label) ->
            composeRule.runOnIdle { uiState.value = content(isEnabled = true, serverState = serverState) }

            composeRule.onNodeWithText(label).assertIsNotEnabled().performClick()
            composeRule.waitForIdle()

            eventList shouldBe emptyList()
        }
    }

    private fun setSettingMcpScaffold(
        uiStateProvider: () -> SettingMcpUiState = { content(isEnabled = false, serverState = McpServerState.Off) },
        onEvent: (SettingMcpScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingMcpScaffold(
                    onEvent = onEvent,
                    uiStateProvider = uiStateProvider,
                )
            }
        }
    }

    private fun displayedTexts(): List<String> =
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text] }
            .map { text -> text.text }

    private fun content(
        isEnabled: Boolean,
        serverState: McpServerState,
    ): SettingMcpUiState.Content = SettingMcpUiState.Content(isEnabled = isEnabled, serverState = serverState)

    private fun stateCaseList(): List<Triple<McpServerState, String, String?>> =
        listOf(
            Triple(McpServerState.Off, DEFAULT_STATE_OFF, null),
            Triple(McpServerState.Running(address = ADDRESS), DEFAULT_STATE_RUNNING, ADDRESS),
            Triple(McpServerState.AddressInUse(address = ADDRESS), DEFAULT_STATE_ADDRESS_IN_USE, ADDRESS),
            Triple(McpServerState.StartFailed, DEFAULT_STATE_START_FAILED, null),
        )

    public companion object {
        private const val ADDRESS = "http://127.0.0.1:38450/mcp"
        private const val DEFAULT_TITLE = "MCP Settings"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_CONNECTION_LABEL = "AI tool connection"
        private const val DEFAULT_CONNECTION_DESCRIPTION =
            "When on, AI tools on this Mac can view, add, edit, and delete your current account's memos and items without asking."
        private const val DEFAULT_ENABLED_LABEL = "Use MCP server"
        private const val DEFAULT_STATE_OFF = "Off"
        private const val DEFAULT_STATE_RUNNING = "Running"
        private const val DEFAULT_STATE_ADDRESS_IN_USE = "Couldn't start because another program is using the address"
        private const val DEFAULT_STATE_START_FAILED = "Couldn't start the connection"
        private val stateLabelList =
            listOf(DEFAULT_STATE_OFF, DEFAULT_STATE_RUNNING, DEFAULT_STATE_ADDRESS_IN_USE, DEFAULT_STATE_START_FAILED)
    }
}
