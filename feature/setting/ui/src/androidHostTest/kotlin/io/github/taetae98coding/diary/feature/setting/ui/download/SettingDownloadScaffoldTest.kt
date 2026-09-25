package io.github.taetae98coding.diary.feature.setting.ui.download

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.feature.setting.ui.download.form.SettingDownloadFormState
import io.github.taetae98coding.diary.feature.setting.ui.download.form.rememberSettingDownloadFormState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ADDRESS = "http://192.168.0.10:27180"
private const val OTHER_ADDRESS = "http://10.0.0.5:27180"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingDownloadScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 다운로드 설정이다`() {
        setScaffold()

        composeRule.onNodeWithText("다운로드 설정").assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 Download Settings다`() {
        setScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `기본 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-001 프록시가 제공 중이면 주소 후보를 모두 표시하고 입력은 두지 않는다`() {
        setScaffold(uiState = SettingDownloadUiState.Serving(addressList = listOf(ADDRESS, OTHER_ADDRESS)))

        composeRule.onNodeWithText(ADDRESS).assertExists()
        composeRule.onNodeWithText(OTHER_ADDRESS).assertExists()
        composeRule.onNodeWithText(DEFAULT_THIS_DEVICE_LABEL).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size shouldBe 0
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-DOWNLOAD-FEATURE-002 한국어 환경에서 닿을 주소가 없으면 그 사실을 알린다`() {
        setScaffold(uiState = SettingDownloadUiState.Serving(addressList = emptyList()))

        composeRule.onNodeWithText("연결된 네트워크가 없어 다른 기기가 닿을 주소가 없습니다").assertExists()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-002 닿을 주소가 없으면 그 사실을 알리고 어떤 주소도 표시하지 않는다`() {
        setScaffold(uiState = SettingDownloadUiState.Serving(addressList = emptyList()))

        composeRule.onNodeWithText(DEFAULT_NO_ADDRESS_MESSAGE).assertExists()
        composeRule.onNodeWithText(ADDRESS).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-003 프록시를 제공하지 못하면 시작하지 못했음을 알리고 다시 시도 수단을 두지 않는다`() {
        setScaffold(uiState = SettingDownloadUiState.Unavailable)

        composeRule.onNodeWithText(DEFAULT_UNAVAILABLE_MESSAGE).assertExists()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-DOWNLOAD-FEATURE-003 한국어 환경에서 시작하지 못했음을 알린다`() {
        setScaffold(uiState = SettingDownloadUiState.Unavailable)

        composeRule.onNodeWithText("프록시를 시작하지 못했습니다. 앱을 다시 실행해 주세요").assertExists()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-004 상태를 확인하기 전에는 본문을 제공하지 않는다`() {
        setScaffold(uiState = SettingDownloadUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_THIS_DEVICE_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADDRESS_LABEL).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-005 저장된 주소가 입력된 상태로 표시하고 저장 동작은 두지 않는다`() {
        val setting = MusicDownloadProxySetting(address = ADDRESS)
        setScaffold(uiState = SettingDownloadUiState.Consumer(setting = setting), initialSetting = setting)

        composeRule.onNodeWithText(DEFAULT_ADDRESS_LABEL).assertExists()
        editableTexts() shouldBe listOf(ADDRESS)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-006 저장된 주소가 없으면 비어 있는 입력으로 표시하고 저장 동작은 두지 않는다`() {
        setScaffold(uiState = SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY))

        editableTexts() shouldBe listOf("")
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-007 TC-SETTING-DOWNLOAD-DOMAIN-001 입력을 저장된 주소와 다르게 바꾸면 저장 동작을 제공한다`() {
        val setting = MusicDownloadProxySetting(address = ADDRESS)
        lateinit var state: SettingDownloadFormState
        setScaffold(uiState = SettingDownloadUiState.Consumer(setting = setting), initialSetting = setting, onState = { state = it })

        listOf(OTHER_ADDRESS, "$ADDRESS ", ADDRESS.uppercase()).forEach { edited ->
            composeRule.runOnIdle { state.addressState.setTextAndPlaceCursorAtEnd(edited) }
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assert(hasClickAction())

            composeRule.runOnIdle { state.addressState.setTextAndPlaceCursorAtEnd(ADDRESS) }
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-008 바꾼 값을 저장된 주소로 되돌리면 저장 동작을 다시 제공하지 않는다`() {
        val setting = MusicDownloadProxySetting(address = ADDRESS)
        lateinit var state: SettingDownloadFormState
        setScaffold(uiState = SettingDownloadUiState.Consumer(setting = setting), initialSetting = setting, onState = { state = it })

        composeRule.runOnIdle { state.addressState.setTextAndPlaceCursorAtEnd(OTHER_ADDRESS) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { state.addressState.setTextAndPlaceCursorAtEnd(ADDRESS) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-009 저장 동작을 실행하면 저장 이벤트를 전달한다`() {
        val eventList = mutableListOf<SettingDownloadScaffoldEvent>()
        setScaffold(
            uiState = SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY),
            initialSetting = MusicDownloadProxySetting(address = ADDRESS),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(SettingDownloadScaffoldEvent.ClickSave)
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-010 저장하는 동안 진행 상태를 표시한다`() {
        setScaffold(
            uiState = SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY, isInProgress = true),
            initialSetting = MusicDownloadProxySetting(address = ADDRESS),
        )

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-DOMAIN-004 저장하는 동안에도 저장된 주소와 같아지면 저장 동작이 사라진다`() {
        val uiState = mutableStateOf<SettingDownloadUiState>(SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY, isInProgress = true))
        composeRule.setContent {
            DiaryTheme {
                SettingDownloadScaffold(
                    onEvent = {},
                    state = rememberSettingDownloadFormState(initialSetting = MusicDownloadProxySetting(address = ADDRESS)),
                    uiStateProvider = { uiState.value },
                )
            }
        }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()

        composeRule.runOnIdle { uiState.value = SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS), isInProgress = true) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-DOMAIN-005 저장하는 동안 입력을 저장된 주소로 되돌리면 저장 동작과 진행 상태가 사라진다`() {
        val setting = MusicDownloadProxySetting(address = ADDRESS)
        lateinit var state: SettingDownloadFormState
        setScaffold(
            uiState = SettingDownloadUiState.Consumer(setting = setting, isInProgress = true),
            initialSetting = MusicDownloadProxySetting(address = OTHER_ADDRESS),
            onState = { state = it },
        )
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()

        composeRule.runOnIdle { state.addressState.setTextAndPlaceCursorAtEnd(ADDRESS) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-019 화면을 떠났다가 다시 들어오면 저장된 주소로 시작한다`() {
        val setting = MusicDownloadProxySetting(address = ADDRESS)
        val isShown = mutableStateOf(true)
        lateinit var state: SettingDownloadFormState
        composeRule.setContent {
            DiaryTheme {
                if (isShown.value) {
                    state = rememberSettingDownloadFormState(initialSetting = setting)

                    SettingDownloadScaffold(
                        onEvent = {},
                        state = state,
                        uiStateProvider = { SettingDownloadUiState.Consumer(setting = setting) },
                    )
                }
            }
        }
        composeRule.runOnIdle { state.addressState.setTextAndPlaceCursorAtEnd(OTHER_ADDRESS) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isShown.value = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown.value = true }
        composeRule.waitForIdle()

        editableTexts() shouldBe listOf(ADDRESS)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-014 뒤로가기 동작을 선택하면 뒤로가기 이벤트를 전달한다`() {
        val eventList = mutableListOf<SettingDownloadScaffoldEvent>()
        setScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(SettingDownloadScaffoldEvent.ClickNavigateUp)
    }

    private fun editableTexts(): List<String> =
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText))
            .fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.EditableText].text }

    private fun setScaffold(
        uiState: SettingDownloadUiState = SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY),
        initialSetting: MusicDownloadProxySetting = MusicDownloadProxySetting.EMPTY,
        onEvent: (SettingDownloadScaffoldEvent) -> Unit = {},
        onState: (SettingDownloadFormState) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                val state = rememberSettingDownloadFormState(initialSetting = initialSetting)
                onState(state)

                SettingDownloadScaffold(
                    onEvent = onEvent,
                    state = state,
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_TITLE = "Download Settings"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_THIS_DEVICE_LABEL = "This device's proxy address"
        private const val DEFAULT_NO_ADDRESS_MESSAGE = "No network connection, so other devices can't reach this device"
        private const val DEFAULT_UNAVAILABLE_MESSAGE = "Couldn't start the proxy. Restart the app."
        private const val DEFAULT_ADDRESS_LABEL = "Proxy address"
        private const val DEFAULT_SAVE_DESCRIPTION = "Save"
    }
}
