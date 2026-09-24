package io.github.taetae98coding.diary.feature.setting.ui.browser

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingBrowserScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 브라우저 설정이다`() {
        setSettingBrowserScaffold()

        composeRule.onNodeWithText("브라우저 설정").assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 Browser Settings다`() {
        setSettingBrowserScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setSettingBrowserScaffold()

        composeRule.onNodeWithContentDescription("뒤로가기").assert(hasClickAction())
    }

    @Test
    fun `기본 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setSettingBrowserScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-BROWSER-FEATURE-002 한국어 환경에서 설정 이름과 보조 문구, 선택 안 함과 프로필을 제공한다`() {
        setSettingBrowserScaffold()

        composeRule.onNodeWithText("Chrome 로그인 이어받기").assertExists()
        composeRule.onNodeWithText("고른 Chrome 프로필에서 로그인한 사이트를 앱 안에서도 로그인된 상태로 엽니다").assertExists()
        composeRule.onNodeWithText("선택 안 함").assert(hasClickAction())
        composeRule.onNodeWithText(PROFILE_A.name).assert(hasClickAction())
        composeRule.onNodeWithText(PROFILE_B.name).assert(hasClickAction())
    }

    @Test
    fun `TC-SETTING-BROWSER-FEATURE-002 기본 환경에서 설정 이름과 보조 문구, 선택 안 함과 프로필을 순서대로 제공한다`() {
        setSettingBrowserScaffold()

        composeRule.onNodeWithText(DEFAULT_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_DESCRIPTION).assertExists()
        selectableItemLabels() shouldBe listOf(DEFAULT_NONE_LABEL, PROFILE_A.name, PROFILE_B.name)
    }

    @Test
    fun `TC-SETTING-BROWSER-FEATURE-003 저장된 선택만 선택된 상태로 표시한다`() {
        val cases =
            mapOf(
                "" to DEFAULT_NONE_LABEL,
                PROFILE_A.directory to PROFILE_A.name,
                PROFILE_B.directory to PROFILE_B.name,
            )

        val selectedDirectory = mutableStateOf("")
        setSettingBrowserScaffold(uiStateProvider = { loaded(selectedProfileDirectory = selectedDirectory.value) })

        cases.forEach { (directory, selectedLabel) ->
            composeRule.runOnIdle { selectedDirectory.value = directory }

            listOf(DEFAULT_NONE_LABEL, PROFILE_A.name, PROFILE_B.name).forEach { label ->
                if (label == selectedLabel) {
                    composeRule.onNodeWithText(label).assertIsSelected()
                } else {
                    composeRule.onNodeWithText(label).assertIsNotSelected()
                }
            }
        }
    }

    @Test
    fun `TC-SETTING-BROWSER-FEATURE-004 항목을 고르면 그 선택을 요청한다`() {
        val eventList = mutableListOf<SettingBrowserScaffoldEvent>()
        setSettingBrowserScaffold(uiState = loaded(selectedProfileDirectory = ""), onEvent = eventList::add)

        composeRule.onNodeWithText(PROFILE_A.name).performClick()
        composeRule.onNodeWithText(DEFAULT_NONE_LABEL).performClick()
        composeRule.waitForIdle()

        eventList shouldBe
            listOf(
                SettingBrowserScaffoldEvent.SelectProfile(directory = PROFILE_A.directory),
                SettingBrowserScaffoldEvent.SelectProfile(directory = ""),
            )
    }

    @Test
    fun `TC-SETTING-BROWSER-FEATURE-007 선택을 확인하기 전에는 설정 영역과 진행 표시를 두지 않는다`() {
        setSettingBrowserScaffold(uiState = SettingBrowserUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_NONE_LABEL).assertDoesNotExist()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `TC-SETTING-BROWSER-FEATURE-008 Chrome 프로필이 없으면 선택 안 함만 선택된 상태로 제공한다`() {
        setSettingBrowserScaffold(uiState = SettingBrowserUiState.Loaded(profileList = emptyList(), selectedProfileDirectory = ""))

        composeRule.onNodeWithText(DEFAULT_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_NONE_LABEL).assertIsSelected()
        selectableItemLabels() shouldBe listOf(DEFAULT_NONE_LABEL)
    }

    @Test
    fun `TC-SETTING-BROWSER-FEATURE-009 프로필 목록을 읽지 못하면 선택 안 함과 조회 실패 안내를 제공한다`() {
        val eventList = mutableListOf<SettingBrowserScaffoldEvent>()
        setSettingBrowserScaffold(
            uiState = SettingBrowserUiState.Loaded(profileList = emptyList(), selectedProfileDirectory = "", isProfileListUnavailable = true),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(DEFAULT_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNAVAILABLE_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_NONE_LABEL).assertIsSelected()
        selectableItemLabels() shouldBe listOf(DEFAULT_NONE_LABEL)

        composeRule.onNodeWithText(DEFAULT_NONE_LABEL).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(SettingBrowserScaffoldEvent.SelectProfile(directory = ""))
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-BROWSER-FEATURE-009 한국어 환경에서 조회 실패 안내를 표시한다`() {
        setSettingBrowserScaffold(
            uiState = SettingBrowserUiState.Loaded(profileList = emptyList(), selectedProfileDirectory = "", isProfileListUnavailable = true),
        )

        composeRule.onNodeWithText("Chrome 프로필을 조회할 수 없습니다").assertExists()
    }

    @Test
    fun `프로필 목록을 읽었으면 조회 실패 안내를 두지 않는다`() {
        setSettingBrowserScaffold()

        composeRule.onNodeWithText(DEFAULT_UNAVAILABLE_MESSAGE).assertDoesNotExist()
    }

    private fun setSettingBrowserScaffold(
        uiState: SettingBrowserUiState = loaded(selectedProfileDirectory = ""),
        onEvent: (SettingBrowserScaffoldEvent) -> Unit = {},
        uiStateProvider: () -> SettingBrowserUiState = { uiState },
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingBrowserScaffold(
                    onEvent = onEvent,
                    uiStateProvider = uiStateProvider,
                )
            }
        }
    }

    private fun selectableItemLabels(): List<String> =
        composeRule
            .onAllNodes(hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected))
            .fetchSemanticsNodes()
            .flatMap { node -> node.config.getOrElse(SemanticsProperties.Text) { emptyList() } }
            .map { text -> text.text }

    public companion object {
        private const val DEFAULT_TITLE = "Browser Settings"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_LABEL = "Use Chrome logins"
        private const val DEFAULT_DESCRIPTION = "Opens sites you're signed in to in the chosen Chrome profile as signed in inside the app"
        private const val DEFAULT_NONE_LABEL = "None"
        private const val DEFAULT_UNAVAILABLE_MESSAGE = "Couldn't load Chrome profiles"

        private val PROFILE_A = ChromeProfile(directory = "Default", name = "TaeJong")
        private val PROFILE_B = ChromeProfile(directory = "Profile 1", name = "Work")

        private fun loaded(selectedProfileDirectory: String): SettingBrowserUiState.Loaded =
            SettingBrowserUiState.Loaded(
                profileList = listOf(PROFILE_A, PROFILE_B),
                selectedProfileDirectory = selectedProfileDirectory,
            )
    }
}
