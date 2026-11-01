package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.feature.setting.ui.gemini.form.SettingGeminiFormState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.form.rememberSettingGeminiFormState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelUiState
import io.github.taetae98coding.diary.feature.setting.ui.holiday.DEFAULT_NAVIGATE_UP_DESCRIPTION
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.absoluteValue

private const val STORED_API_KEY = "storedApiKey"
private const val MODEL_ID = "models/gemini-flash"
private const val MODEL_DISPLAY_NAME = "Gemini Flash"
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
    fun `TC-8cb37358-a8e6-4600-ac68-aeb09bf93d5e 설정을 확인하지 못하면 입력과 저장 기능을 제공하지 않는다`() {
        setScaffold(uiState = SettingGeminiUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_API_KEY_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_MODEL_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SYSTEM_PROMPT_LABEL).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    fun `TC-b7f4e2f9-6e46-467f-a334-663f7169f362 저장된 설정을 입력된 상태로 표시한다`() {
        val setting =
            GeminiSetting(
                apiKey = STORED_API_KEY,
                model = MODEL_ID,
                systemPrompt = STORED_SYSTEM_PROMPT,
            )
        setScaffold(uiState = SettingGeminiUiState.Content(setting = setting), initialSetting = setting)

        composeRule.onNodeWithText(DEFAULT_API_KEY_LABEL).assertExists()
        composeRule.modelRow().assertContentDescriptionContains(MODEL_ID, substring = true)
        composeRule.onNodeWithText(STORED_SYSTEM_PROMPT).assertExists()
    }

    @Test
    fun `TC-8449e39b-5c2c-4c7c-9b9b-7f95beb2cd6c 저장된 설정이 없으면 비어 있는 상태로 표시한다`() {
        setScaffold(uiState = SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY))

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
    fun `TC-24cfa575-fea7-46cf-b140-9c3993db8233 목록을 받아 오기 전에는 목록에 없는 모델 안내를 표시하지 않는다`() {
        val setting = GeminiSetting.EMPTY.copy(model = MODEL_ID)
        setScaffold(uiState = SettingGeminiUiState.Content(setting = setting), initialSetting = setting)

        val description = composeRule.modelRowDescription()
        description.contains(MODEL_ID) shouldBe true
        description.contains(DEFAULT_MODEL_MISSING_MESSAGE) shouldBe false
    }

    @Test
    fun `TC-962ee82a-4fd2-475d-85eb-19a1c2f2fc10 목록에 없는 모델을 알리되 선택은 유지한다`() {
        val setting = GeminiSetting.EMPTY.copy(model = REMOVED_MODEL_ID)
        setScaffold(
            uiState = SettingGeminiUiState.Content(setting = setting),
            initialSetting = setting,
            modelUiState =
                SettingGeminiModelUiState(
                    isLoaded = true,
                    modelList = listOf(GeminiModel(id = MODEL_ID, displayName = MODEL_DISPLAY_NAME, description = "")),
                ),
            isModelDialogVisible = true,
        )

        val description = composeRule.modelRowDescription()
        description.contains(REMOVED_MODEL_ID) shouldBe true
        description.contains(DEFAULT_MODEL_MISSING_MESSAGE) shouldBe true
    }

    @Test
    fun `TC-a115aec1-6731-4230-a136-241ada88ee23 모델 선택을 닫으면 목록에 없는 모델 안내를 남기지 않는다`() {
        val setting = GeminiSetting.EMPTY.copy(model = REMOVED_MODEL_ID)
        val state =
            setScaffold(
                uiState = SettingGeminiUiState.Content(setting = setting),
                initialSetting = setting,
                modelUiState =
                    SettingGeminiModelUiState(
                        isLoaded = true,
                        modelList = listOf(GeminiModel(id = MODEL_ID, displayName = MODEL_DISPLAY_NAME, description = "")),
                    ),
                isModelDialogVisible = true,
            )
        composeRule.modelRowDescription().contains(DEFAULT_MODEL_MISSING_MESSAGE) shouldBe true

        composeRule.runOnIdle { state.modelDialogState.hide() }
        composeRule.waitForIdle()

        val description = composeRule.modelRowDescription()
        description.contains(REMOVED_MODEL_ID) shouldBe true
        description.contains(DEFAULT_MODEL_MISSING_MESSAGE) shouldBe false
    }

    @Test
    fun `목록에 있는 모델이면 목록에 없다는 안내를 표시하지 않는다`() {
        val setting = GeminiSetting.EMPTY.copy(model = MODEL_ID)
        setScaffold(
            uiState = SettingGeminiUiState.Content(setting = setting),
            initialSetting = setting,
            modelUiState =
                SettingGeminiModelUiState(
                    isLoaded = true,
                    modelList = listOf(GeminiModel(id = MODEL_ID, displayName = MODEL_DISPLAY_NAME, description = "")),
                ),
            isModelDialogVisible = true,
        )

        composeRule.modelRowDescription().contains(DEFAULT_MODEL_MISSING_MESSAGE) shouldBe false
    }

    @Test
    fun `API 키 입력의 동작 키를 누르면 시스템 프롬프트 입력으로 초점을 옮긴다`() {
        setScaffold(uiState = SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY))

        val textFields = composeRule.onAllNodes(hasSetTextAction())
        textFields[0].performClick()
        composeRule.waitForIdle()
        textFields[0].performImeAction()
        composeRule.waitForIdle()

        textFields[1].assertIsFocused()
    }

    @Test
    fun `TC-17c7f47a-b67b-4041-a191-1d5970ccc131 저장하는 동안 진행 상태를 표시한다`() {
        setScaffold(
            uiState = SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY, isInProgress = true),
            initialSetting = GeminiSetting.EMPTY.copy(apiKey = STORED_API_KEY),
        )

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun `TC-e84391f2-0ded-4490-9087-57dc56b22d96 편집 값이 저장된 설정과 같으면 저장 동작을 제공하지 않는다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = STORED_SYSTEM_PROMPT)
        setScaffold(uiState = SettingGeminiUiState.Content(setting = setting), initialSetting = setting)

        composeRule.onNodeWithText(DEFAULT_API_KEY_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-e84391f2-0ded-4490-9087-57dc56b22d96 저장한 적이 없고 아무것도 입력하지 않으면 저장 동작을 제공하지 않는다`() {
        setScaffold(uiState = SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY), initialSetting = GeminiSetting.EMPTY)

        composeRule.onNodeWithText(DEFAULT_API_KEY_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp")
    fun `시스템 프롬프트 입력은 본문의 남은 높이를 채운다`() {
        setScaffold()

        val rootBottom = composeRule.onRoot().getBoundsInRoot().bottom
        val inputBottom = composeRule.onNode(hasSetTextAction() and hasText(DEFAULT_SYSTEM_PROMPT_LABEL)).getBoundsInRoot().bottom

        ((rootBottom - inputBottom).value.absoluteValue < FILL_TOLERANCE.value) shouldBe true
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp")
    fun `시스템 프롬프트가 길어도 입력은 본문 아래로 늘어나지 않는다`() {
        val longPrompt = List(size = LONG_PROMPT_LINE_COUNT) { index -> "줄 $index" }.joinToString(separator = "\n")
        val setting = GeminiSetting.EMPTY.copy(systemPrompt = longPrompt)
        setScaffold(uiState = SettingGeminiUiState.Content(setting = setting), initialSetting = setting)

        val rootBottom = composeRule.onRoot().getBoundsInRoot().bottom
        val inputBottom = composeRule.onNode(hasSetTextAction() and hasText(DEFAULT_SYSTEM_PROMPT_LABEL)).getBoundsInRoot().bottom

        (inputBottom <= rootBottom) shouldBe true
        (rootBottom - inputBottom < FILL_TOLERANCE) shouldBe true
    }

    private fun ComposeContentTestRule.modelRow(): SemanticsNodeInteraction = onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true))

    private fun ComposeContentTestRule.modelRowDescription(): String =
        modelRow()
            .fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription]
            .joinToString(separator = " ")

    private fun setScaffold(
        uiState: SettingGeminiUiState = SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY),
        initialSetting: GeminiSetting = GeminiSetting.EMPTY,
        modelUiState: SettingGeminiModelUiState = SettingGeminiModelUiState(),
        isModelDialogVisible: Boolean = false,
        onEvent: (SettingGeminiScaffoldEvent) -> Unit = {},
    ): SettingGeminiFormState {
        lateinit var formState: SettingGeminiFormState
        composeRule.setContent {
            DiaryTheme {
                formState = rememberSettingGeminiFormState(initialSetting = initialSetting)
                if (isModelDialogVisible) {
                    LaunchedEffect(formState) { formState.modelDialogState.show() }
                }

                SettingGeminiScaffold(
                    onEvent = onEvent,
                    onModelDialogEvent = {},
                    state = formState,
                    uiStateProvider = { uiState },
                    modelUiStateProvider = { modelUiState },
                )
            }
        }
        composeRule.waitForIdle()
        return formState
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
        private val FILL_TOLERANCE = 48.dp
        private const val LONG_PROMPT_LINE_COUNT = 100
    }
}
