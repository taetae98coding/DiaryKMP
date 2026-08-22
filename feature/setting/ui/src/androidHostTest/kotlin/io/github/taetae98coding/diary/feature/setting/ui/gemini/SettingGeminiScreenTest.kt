package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val EDITING_API_KEY = "editingApiKey"
private const val EDITING_SUFFIX = "editing"
private const val API_KEY_INDEX = 0
private const val SYSTEM_PROMPT_INDEX = 1
private const val STORED_API_KEY = "storedApiKey"
private const val MODEL_ID = "models/gemini-flash"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingGeminiScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-020 뒤로가기 동작을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setScreen(navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-021 저장하지 않은 내용이 있어도 곧바로 돌아간다`() {
        var navigateUpCount = 0
        val settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY))
        setScreen(settingViewModel = settingViewModel, navigateUp = { navigateUpCount += 1 })

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput(EDITING_API_KEY)
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { settingViewModel.save(any()) }
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-004 인증 정보가 비어 있으면 다이얼로그를 열지 않고 입력 필요를 알린다`() {
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY)),
            modelViewModel = modelViewModel,
        )

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_API_KEY_BLANK_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertDoesNotExist()
        verify(exactly = 0) { modelViewModel.fetch(any()) }
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-005 다이얼로그를 열면 입력란의 인증 정보로 조회한다`() {
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY.copy(apiKey = STORED_API_KEY))),
            modelViewModel = modelViewModel,
        )

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("editing")
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertExists()
        verify(exactly = 1) { modelViewModel.fetch(apiKey = "storedApiKeyediting") }
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-003 화면에 들어오는 것만으로는 모델을 받아 오지 않는다`() {
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY.copy(apiKey = STORED_API_KEY))),
            modelViewModel = modelViewModel,
        )

        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertDoesNotExist()
        verify(exactly = 0) { modelViewModel.fetch(any()) }
    }

    @Test
    fun `이미 받아 둔 목록이 있으면 다이얼로그를 열어도 다시 조회하지 않는다`() {
        val modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = MODEL_LIST))
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY.copy(apiKey = STORED_API_KEY))),
            modelViewModel = modelViewModel,
        )

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertExists()
        verify(exactly = 0) { modelViewModel.fetch(any()) }
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-014 모델을 골라도 저장하지 않는다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_LIST[0].id, systemPrompt = "")
        val settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = setting))
        val modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = MODEL_LIST))
        setScreen(settingViewModel = settingViewModel, modelViewModel = modelViewModel)

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MODEL_LIST[1].id).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNode(hasContentDescription(MODEL_LIST[1].id, substring = true)).assertExists()
        verify(exactly = 0) { settingViewModel.save(any()) }
    }

    @Test
    fun `TC-SETTING-GEMINI-DATA-007 모델을 조회해도 인증 정보를 저장하지 않는다`() {
        val settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY))
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())
        setScreen(settingViewModel = settingViewModel, modelViewModel = modelViewModel)

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput(EDITING_API_KEY)
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { modelViewModel.fetch(apiKey = EDITING_API_KEY) }
        verify(exactly = 0) { settingViewModel.save(any()) }
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-015 저장 동작을 선택하면 입력한 세 값을 저장한다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        val settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = setting))
        setScreen(settingViewModel = settingViewModel)

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(EDITING_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { settingViewModel.save(setting = setting.copy(systemPrompt = setting.systemPrompt + EDITING_SUFFIX)) }
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-023 화면이 재생성되어도 입력 중이던 값과 저장 동작을 유지한다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        val restorationTester = StateRestorationTester(composeRule)
        val settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = setting))

        restorationTester.setContent {
            DiaryTheme {
                SettingGeminiScreen(
                    navigateUp = {},
                    componentVisibleProvider = { SettingGeminiScaffoldComponentVisible() },
                    settingViewModel = settingViewModel,
                    modelViewModel = modelViewModel(SettingGeminiModelUiState()),
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(EDITING_SUFFIX)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(setting.systemPrompt + EDITING_SUFFIX).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-024 저장된 설정을 채운 직후에는 저장 동작을 제공하지 않는다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = setting)))

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-025 인증 정보만 바꿔도 저장 동작을 제공한다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextInput(EDITING_SUFFIX)

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-025 시스템 프롬프트만 바꿔도 저장 동작을 제공한다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(EDITING_SUFFIX)

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-025 인증 정보에 공백만 덧붙여도 저장 동작을 제공한다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextInput(" ")

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-025 모델만 바꿔도 저장 동작을 제공한다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_LIST[0].id, systemPrompt = "지시문")
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = setting)),
            modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = MODEL_LIST)),
        )

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MODEL_LIST[1].id).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-026 바꾼 값을 저장된 값으로 되돌리면 저장 동작을 다시 제공하지 않는다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextInput(EDITING_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextReplacement(STORED_API_KEY)

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-027 저장에 성공하면 저장 동작을 더 제공하지 않는다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        val uiStateFlow = MutableStateFlow<SettingGeminiUiState>(SettingGeminiUiState.Loaded(setting = setting))
        setScreen(settingViewModel = settingViewModel(uiStateFlow))

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(EDITING_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        uiStateFlow.value = SettingGeminiUiState.Loaded(setting = setting.copy(systemPrompt = setting.systemPrompt + EDITING_SUFFIX))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(setting.systemPrompt + EDITING_SUFFIX).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-028 저장에 실패하면 저장 동작을 계속 제공한다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        val uiStateFlow = MutableStateFlow<SettingGeminiUiState>(SettingGeminiUiState.Loaded(setting = setting))
        setScreen(settingViewModel = settingViewModel(uiStateFlow))

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(EDITING_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        uiStateFlow.value = SettingGeminiUiState.Loaded(setting = setting)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-029 저장을 처리하는 동안 값을 되돌리면 진행 표시까지 사라진다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = MODEL_ID, systemPrompt = "지시문")
        val uiStateFlow = MutableStateFlow<SettingGeminiUiState>(SettingGeminiUiState.Loaded(setting = setting))
        setScreen(settingViewModel = settingViewModel(uiStateFlow))

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextInput(EDITING_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        uiStateFlow.value = SettingGeminiUiState.Loaded(setting = setting, isInProgress = true)
        composeRule.waitForIdle()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextReplacement(STORED_API_KEY)

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    private fun setScreen(
        settingViewModel: SettingGeminiViewModel = settingViewModel(SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY)),
        modelViewModel: SettingGeminiModelViewModel = modelViewModel(SettingGeminiModelUiState()),
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingGeminiScreen(
                    navigateUp = navigateUp,
                    componentVisibleProvider = { SettingGeminiScaffoldComponentVisible() },
                    settingViewModel = settingViewModel,
                    modelViewModel = modelViewModel,
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_SAVE_DESCRIPTION = "Save"
        private const val DEFAULT_MODEL_LABEL = "Model"
        private const val DEFAULT_MODEL_PICKER_TITLE = "Select model"
        private const val DEFAULT_API_KEY_BLANK_MESSAGE = "Please enter an API key."

        private val MODEL_LIST =
            listOf(
                GeminiModel(id = "models/gemini-flash", displayName = "Gemini Flash", description = "빠른 범용 모델"),
                GeminiModel(id = "models/gemini-pro", displayName = "Gemini Pro", description = "정확한 범용 모델"),
            )

        private fun settingViewModel(uiState: SettingGeminiUiState): SettingGeminiViewModel = settingViewModel(MutableStateFlow(uiState))

        private fun settingViewModel(uiStateFlow: MutableStateFlow<SettingGeminiUiState>): SettingGeminiViewModel =
            mockk<SettingGeminiViewModel>(relaxed = true) {
                every { uiState } returns uiStateFlow
                every { effect } returns emptyFlow()
            }

        private fun modelViewModel(uiState: SettingGeminiModelUiState): SettingGeminiModelViewModel =
            mockk<SettingGeminiModelViewModel>(relaxed = true) {
                every { this@mockk.uiState } returns MutableStateFlow(uiState)
                every { effect } returns emptyFlow()
            }
    }
}
