package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.feature.setting.ui.gemini.form.rememberSettingGeminiFormState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelUiState
import io.github.taetae98coding.diary.feature.setting.ui.holiday.DEFAULT_NAVIGATE_UP_DESCRIPTION
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STORED_API_KEY = "storedApiKey"
private const val MODEL_ID = "models/gemini-flash"
private const val REMOVED_MODEL_ID = "models/removed"
private const val STORED_SYSTEM_PROMPT = "저장해 둔 지시문"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingGeminiScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 Gemini 설정이다`() {
        setScaffold()

        composeRule.onNodeWithText("Gemini 설정").assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 Gemini Settings다`() {
        setScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setScaffold()

        composeRule.onNodeWithContentDescription("뒤로가기").assert(hasClickAction())
    }

    @Test
    fun `기본 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-022 설정을 확인하지 못하면 입력과 저장 기능을 제공하지 않는다`() {
        setScaffold(uiState = SettingGeminiUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_API_KEY_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_MODEL_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SYSTEM_PROMPT_LABEL).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-001 저장된 설정을 입력된 상태로 표시한다`() {
        val setting =
            GeminiSetting(
                apiKey = STORED_API_KEY,
                model = MODEL_ID,
                systemPrompt = STORED_SYSTEM_PROMPT,
            )
        setScaffold(uiState = SettingGeminiUiState.Loaded(setting = setting), initialSetting = setting)

        composeRule.onNodeWithText(DEFAULT_API_KEY_LABEL).assertExists()
        composeRule.modelRow().assertContentDescriptionContains(MODEL_ID, substring = true)
        composeRule.onNodeWithText(STORED_SYSTEM_PROMPT).assertExists()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-002 저장된 설정이 없으면 비어 있는 상태로 표시한다`() {
        setScaffold(uiState = SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY))

        composeRule.modelRow().assertContentDescriptionContains(DEFAULT_MODEL_UNSELECTED, substring = true)
        composeRule.onNodeWithText(DEFAULT_SYSTEM_PROMPT_LABEL).assertExists()

        val systemPromptTexts =
            composeRule
                .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText))
                .fetchSemanticsNodes()
                .map { node -> node.config[SemanticsProperties.EditableText].text }

        systemPromptTexts.all { text -> text.isEmpty() } shouldBe true
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-013 목록을 받아 오기 전에는 목록에 없는 모델 안내를 표시하지 않는다`() {
        val setting = GeminiSetting.EMPTY.copy(model = MODEL_ID)
        setScaffold(uiState = SettingGeminiUiState.Loaded(setting = setting), initialSetting = setting)

        val description = composeRule.modelRowDescription()
        description.contains(MODEL_ID) shouldBe true
        description.contains(DEFAULT_MODEL_MISSING_MESSAGE) shouldBe false
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-012 목록에 없는 모델을 알리되 선택은 유지한다`() {
        val setting = GeminiSetting.EMPTY.copy(model = REMOVED_MODEL_ID)
        setScaffold(
            uiState = SettingGeminiUiState.Loaded(setting = setting),
            initialSetting = setting,
            modelUiState =
                SettingGeminiModelUiState(
                    isLoaded = true,
                    modelList = listOf(GeminiModel(id = MODEL_ID, displayName = "Gemini Flash", description = "")),
                ),
        )

        val description = composeRule.modelRowDescription()
        description.contains(REMOVED_MODEL_ID) shouldBe true
        description.contains(DEFAULT_MODEL_MISSING_MESSAGE) shouldBe true
    }

    @Test
    fun `목록에 있는 모델이면 목록에 없다는 안내를 표시하지 않는다`() {
        val setting = GeminiSetting.EMPTY.copy(model = MODEL_ID)
        setScaffold(
            uiState = SettingGeminiUiState.Loaded(setting = setting),
            initialSetting = setting,
            modelUiState =
                SettingGeminiModelUiState(
                    isLoaded = true,
                    modelList = listOf(GeminiModel(id = MODEL_ID, displayName = "Gemini Flash", description = "")),
                ),
        )

        composeRule.modelRowDescription().contains(DEFAULT_MODEL_MISSING_MESSAGE) shouldBe false
    }

    @Test
    fun `API 키 입력의 동작 키를 누르면 시스템 프롬프트 입력으로 초점을 옮긴다`() {
        setScaffold(uiState = SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY))

        val textFields = composeRule.onAllNodes(hasSetTextAction())
        textFields[0].performClick()
        composeRule.waitForIdle()
        textFields[0].performImeAction()
        composeRule.waitForIdle()

        textFields[1].assertIsFocused()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-016 저장하는 동안 진행 상태를 표시한다`() {
        setScaffold(
            uiState = SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY, isInProgress = true),
            initialSetting = GeminiSetting.EMPTY.copy(apiKey = STORED_API_KEY),
        )

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-024 편집 값이 저장된 설정과 같으면 저장 동작을 제공하지 않는다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = STORED_SYSTEM_PROMPT)
        setScaffold(uiState = SettingGeminiUiState.Loaded(setting = setting), initialSetting = setting)

        composeRule.onNodeWithText(DEFAULT_API_KEY_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-024 저장한 적이 없고 아무것도 입력하지 않으면 저장 동작을 제공하지 않는다`() {
        setScaffold(uiState = SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY), initialSetting = GeminiSetting.EMPTY)

        composeRule.onNodeWithText(DEFAULT_API_KEY_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    private fun ComposeContentTestRule.modelRow(): SemanticsNodeInteraction = onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true))

    private fun ComposeContentTestRule.modelRowDescription(): String =
        modelRow()
            .fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription]
            .joinToString(separator = " ")

    private fun setScaffold(
        uiState: SettingGeminiUiState = SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY),
        initialSetting: GeminiSetting = GeminiSetting.EMPTY,
        modelUiState: SettingGeminiModelUiState = SettingGeminiModelUiState(),
        onEvent: (SettingGeminiScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingGeminiScaffold(
                    onEvent = onEvent,
                    onModelDialogEvent = {},
                    state = rememberSettingGeminiFormState(initialSetting = initialSetting),
                    uiStateProvider = { uiState },
                    modelUiStateProvider = { modelUiState },
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_TITLE = "Gemini Settings"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_API_KEY_LABEL = "API key"
        private const val DEFAULT_MODEL_LABEL = "Model"
        private const val DEFAULT_MODEL_UNSELECTED = "Not selected"
        private const val DEFAULT_MODEL_MISSING_MESSAGE = "This model isn't in the loaded list"
        private const val DEFAULT_SYSTEM_PROMPT_LABEL = "System prompt"
        private const val DEFAULT_SAVE_DESCRIPTION = "Save"
    }
}
