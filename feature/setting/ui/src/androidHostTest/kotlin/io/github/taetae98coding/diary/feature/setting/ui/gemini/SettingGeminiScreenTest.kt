package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelEffect
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelFailure
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelUiState
import io.github.taetae98coding.diary.feature.setting.ui.gemini.model.SettingGeminiModelViewModel
import io.github.taetae98coding.diary.feature.setting.ui.holiday.DEFAULT_NAVIGATE_UP_DESCRIPTION
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private const val API_KEY_INDEX = 0
private const val SYSTEM_PROMPT_INDEX = 1

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingGeminiScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-f9e571da-19df-480b-b330-4acb5f36d403 뒤로가기 동작을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setScreen(navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-71dab48c-17fc-4d8c-a2ea-7fa8ba9de2e0 저장하지 않은 내용이 있어도 곧바로 돌아간다`() {
        val editingApiKey = editingApiKey()
        var navigateUpCount = 0
        val settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY))
        setScreen(settingViewModel = settingViewModel, navigateUp = { navigateUpCount += 1 })

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput(editingApiKey)
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { settingViewModel.save(any()) }
    }

    @Test
    fun `TC-bf792eb2-1157-4aa7-9dc1-849d7f0e871b 인증 정보가 비어 있으면 다이얼로그를 열지 않고 입력 필요를 알린다`() {
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY)),
            modelViewModel = modelViewModel,
        )

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_API_KEY_BLANK_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertDoesNotExist()
        verify(exactly = 0) { modelViewModel.fetch(any()) }
    }

    @Test
    fun `TC-bf792eb2-1157-4aa7-9dc1-849d7f0e871b 인증 정보에 공백만 있으면 다이얼로그를 열지 않고 입력 필요를 알린다`() {
        val systemPrompt = "prompt" + fixtureMonkey.giveMeOne<Int>()
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY.copy(apiKey = "   ", systemPrompt = systemPrompt))),
            modelViewModel = modelViewModel,
        )

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_API_KEY_BLANK_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(systemPrompt).assertExists()
        verify(exactly = 0) { modelViewModel.fetch(any()) }
    }

    @Test
    fun `TC-b51630a9-a005-409d-851f-ea6ce36850e0 모델은 목록에서 고를 뿐 직접 적을 수 없다`() {
        setScreen()

        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(2)
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).assert(hasClickAction())
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).assert(hasSetTextAction().not())
    }

    @Test
    fun `TC-54bf33da-7b16-4728-9208-c8b422ef52f7 다이얼로그를 열면 입력란의 인증 정보로 조회한다`() {
        val storedApiKey = storedApiKey()
        val editingSuffix = editingSuffix()
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY.copy(apiKey = storedApiKey))),
            modelViewModel = modelViewModel,
        )

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput(editingSuffix)
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertExists()
        verify(exactly = 1) { modelViewModel.fetch(apiKey = storedApiKey + editingSuffix) }
    }

    @Test
    fun `TC-4f82e618-8baf-419e-ae3a-ccb498f4e91d 화면에 들어오는 것만으로는 모델을 받아 오지 않는다`() {
        val storedApiKey = storedApiKey()
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY.copy(apiKey = storedApiKey))),
            modelViewModel = modelViewModel,
        )

        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertDoesNotExist()
        verify(exactly = 0) { modelViewModel.fetch(any()) }
    }

    @Test
    fun `TC-a0cb58d2-3f32-4fb7-a667-be0f915543a4 이미 받아 둔 목록이 있으면 다이얼로그를 열어도 다시 조회하지 않는다`() {
        val modelList = modelList()
        val storedApiKey = storedApiKey()
        val modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = modelList))
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY.copy(apiKey = storedApiKey))),
            modelViewModel = modelViewModel,
        )

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertExists()
        verify(exactly = 0) { modelViewModel.fetch(any()) }
    }

    @Test
    fun `TC-b127c219-d53c-4442-b0aa-125385d14c94 받아 둔 목록이 있으면 인증 정보가 비어 있어도 모델 선택을 열어 고를 수 있다`() {
        assertLoadedModelSelectableWithBlankApiKey(apiKey = "")
    }

    @Test
    fun `TC-b127c219-d53c-4442-b0aa-125385d14c94 받아 둔 목록이 있으면 인증 정보가 공백뿐이어도 모델 선택을 열어 고를 수 있다`() {
        assertLoadedModelSelectableWithBlankApiKey(apiKey = "   ")
    }

    private fun assertLoadedModelSelectableWithBlankApiKey(apiKey: String) {
        val modelList = modelList()
        val modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = modelList))
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY.copy(apiKey = apiKey))),
            modelViewModel = modelViewModel,
        )

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertExists()
        composeRule.onNodeWithText(modelList[1].id).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasContentDescription(modelList[1].id, substring = true)).assertExists()
        composeRule.onNodeWithText(DEFAULT_API_KEY_BLANK_MESSAGE).assertDoesNotExist()
        verify(exactly = 0) { modelViewModel.fetch(any()) }
    }

    @Test
    fun `TC-802a6bb0-5e64-45f3-bdd8-0038973d964d 받아 둔 목록이 있어도 인증 정보가 비어 있으면 다시 조회하지 않고 입력 필요를 알린다`() {
        assertReloadRejectedWithBlankApiKey(apiKey = "")
    }

    @Test
    fun `TC-802a6bb0-5e64-45f3-bdd8-0038973d964d 받아 둔 목록이 있어도 인증 정보가 공백뿐이면 다시 조회하지 않고 입력 필요를 알린다`() {
        assertReloadRejectedWithBlankApiKey(apiKey = "   ")
    }

    private fun assertReloadRejectedWithBlankApiKey(apiKey: String) {
        val modelList = modelList()
        val modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = modelList))
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY.copy(apiKey = apiKey))),
            modelViewModel = modelViewModel,
        )

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_RELOAD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_API_KEY_BLANK_MESSAGE).assertExists()
        composeRule.onNodeWithText(modelList[0].id).assertExists()
        composeRule.onNodeWithText(modelList[1].id).assertExists()
        verify(exactly = 0) { modelViewModel.fetch(any()) }
    }

    @Test
    fun `TC-3dc69964-ee78-415d-9564-54f8648d5eed 모델을 골라도 저장하지 않는다`() {
        val modelList = modelList()
        val storedApiKey = storedApiKey()
        val setting = GeminiSetting(apiKey = storedApiKey, model = modelList[0].id, systemPrompt = "")
        val settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting))
        val modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = modelList))
        setScreen(settingViewModel = settingViewModel, modelViewModel = modelViewModel)

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(modelList[1].id).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNode(hasContentDescription(modelList[1].id, substring = true)).assertExists()
        verify(exactly = 0) { settingViewModel.save(any()) }
    }

    @Test
    fun `TC-528e876a-2071-48db-97e0-140265dbfba0 모델을 조회해도 인증 정보를 저장하지 않는다`() {
        val editingApiKey = editingApiKey()
        val settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY))
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())
        setScreen(settingViewModel = settingViewModel, modelViewModel = modelViewModel)

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput(editingApiKey)
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { modelViewModel.fetch(apiKey = editingApiKey) }
        verify(exactly = 0) { settingViewModel.save(any()) }
    }

    @Test
    fun `TC-5305f042-c198-4a29-9d9b-7b490355f139 저장 동작을 선택하면 입력한 세 값을 저장한다`() {
        val editingSuffix = editingSuffix()
        val setting = geminiSetting()
        val settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting))
        setScreen(settingViewModel = settingViewModel)

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(editingSuffix)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { settingViewModel.save(setting = setting.copy(systemPrompt = setting.systemPrompt + editingSuffix)) }
    }

    @Test
    fun `TC-1aa3bc50-802d-4ef8-b2e2-9fe319706954 화면이 재생성되어도 입력 중이던 값과 저장 동작을 유지한다`() {
        val editingSuffix = editingSuffix()
        val setting = geminiSetting()
        val restorationTester = StateRestorationTester(composeRule)
        val settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting))

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

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(editingSuffix)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(setting.systemPrompt + editingSuffix).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-e84391f2-0ded-4490-9087-57dc56b22d96 저장된 설정을 채운 직후에는 저장 동작을 제공하지 않는다`() {
        val setting = geminiSetting()
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting)))

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-abc5a5f7-d3f2-4b36-bbad-f592dd185ec1 인증 정보만 바꿔도 저장 동작을 제공한다`() {
        val editingSuffix = editingSuffix()
        val setting = geminiSetting()
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextInput(editingSuffix)

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-abc5a5f7-d3f2-4b36-bbad-f592dd185ec1 시스템 프롬프트만 바꿔도 저장 동작을 제공한다`() {
        val editingSuffix = editingSuffix()
        val setting = geminiSetting()
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(editingSuffix)

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-abc5a5f7-d3f2-4b36-bbad-f592dd185ec1 인증 정보에 공백만 덧붙여도 저장 동작을 제공한다`() {
        val setting = geminiSetting()
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextInput(" ")

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-4c96577b-90ce-4721-9710-b2b44125dbb8 인증 정보의 영문 대소문자만 바꿔도 저장 동작을 제공한다`() {
        val setting = geminiSetting()
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextReplacement(setting.apiKey.uppercase())

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-4c96577b-90ce-4721-9710-b2b44125dbb8 시스템 프롬프트 앞에 공백만 더해도 저장 동작을 제공한다`() {
        val setting = geminiSetting()
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextReplacement(" ${setting.systemPrompt}")

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-abc5a5f7-d3f2-4b36-bbad-f592dd185ec1 모델만 바꿔도 저장 동작을 제공한다`() {
        val modelList = modelList()
        val setting = geminiSetting(model = modelList[0].id)
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting)),
            modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = modelList)),
        )

        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(modelList[1].id).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-89ad8fd4-f725-4301-8a9a-fe5690471d1f 바꾼 값을 저장된 값으로 되돌리면 저장 동작을 다시 제공하지 않는다`() {
        val editingSuffix = editingSuffix()
        val setting = geminiSetting()
        setScreen(settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = setting)))

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextInput(editingSuffix)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextReplacement(setting.apiKey)

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-ddd2e2c4-c02e-4d36-9a67-7026ca266269 저장에 성공하면 저장 동작을 더 제공하지 않는다`() {
        val editingSuffix = editingSuffix()
        val setting = geminiSetting()
        val uiStateFlow = MutableStateFlow<SettingGeminiUiState>(SettingGeminiUiState.Content(setting = setting))
        setScreen(settingViewModel = settingViewModel(uiStateFlow))

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(editingSuffix)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        uiStateFlow.value = SettingGeminiUiState.Content(setting = setting.copy(systemPrompt = setting.systemPrompt + editingSuffix))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(setting.systemPrompt + editingSuffix).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-5dae2e8d-ad42-466a-8fe2-891e95121233 저장에 실패하면 저장 동작을 계속 제공한다`() {
        val editingSuffix = editingSuffix()
        val setting = geminiSetting()
        val uiStateFlow = MutableStateFlow<SettingGeminiUiState>(SettingGeminiUiState.Content(setting = setting))
        setScreen(settingViewModel = settingViewModel(uiStateFlow))

        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextInput(editingSuffix)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        uiStateFlow.value = SettingGeminiUiState.Content(setting = setting)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-118ec14b-10fc-4fa9-bbc7-0aaae74e3fdd 저장을 처리하는 동안 값을 되돌리면 진행 표시까지 사라진다`() {
        val editingSuffix = editingSuffix()
        val setting = geminiSetting()
        val uiStateFlow = MutableStateFlow<SettingGeminiUiState>(SettingGeminiUiState.Content(setting = setting))
        setScreen(settingViewModel = settingViewModel(uiStateFlow))

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextInput(editingSuffix)
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        uiStateFlow.value = SettingGeminiUiState.Content(setting = setting, isInProgress = true)
        composeRule.waitForIdle()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()

        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextReplacement(setting.apiKey)

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-ef45d0e5-8837-402d-a394-2c7b9e9592f9 다시 조회에 실패해도 받아 둔 목록과 고른 모델을 유지한다`() {
        val modelList = modelList()
        val modelUiState = MutableStateFlow(SettingGeminiModelUiState(isLoaded = true, modelList = modelList))
        val failureChannel = Channel<SettingGeminiModelEffect>(Channel.BUFFERED)
        setScreen(
            settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = geminiSetting(model = modelList[0].id))),
            modelViewModel = modelViewModel(uiStateFlow = modelUiState, effectFlow = failureChannel.receiveAsFlow()),
        )
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(modelList[1].id).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()

        modelUiState.value = modelUiState.value.copy(failure = SettingGeminiModelFailure.UNKNOWN)
        failureChannel.trySend(SettingGeminiModelEffect.FetchFailed)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_FETCH_FAILED_MESSAGE).assertExists()
        modelList.forEach { model -> composeRule.onNodeWithText(model.id).assert(hasClickAction()) }
        composeRule.onNode(isSelected() and hasText(modelList[1].id)).assertExists()
        composeRule.onNode(isSelected() and hasText(modelList[0].id)).assertDoesNotExist()
    }

    @Test
    fun `TC-4f7ca966-2ddc-4ddc-858d-befcae34e2ee 저장에 성공하면 입력을 비우지 않고 성공을 알린다`() {
        assertSaveFeedback(effect = SettingGeminiEffect.SaveSucceeded, message = DEFAULT_SAVE_SUCCEEDED_MESSAGE)
    }

    @Test
    fun `TC-e4611d27-bb3d-41c8-b08d-0c1028146247 저장에 실패하면 입력을 유지하고 실패를 알린다`() {
        assertSaveFeedback(effect = SettingGeminiEffect.SaveFailed, message = DEFAULT_SAVE_FAILED_MESSAGE)
    }

    private fun assertSaveFeedback(
        effect: SettingGeminiEffect,
        message: String,
    ) {
        val modelList = modelList()
        val setting = geminiSetting(model = modelList[0].id)
        val editingApiKey = editingApiKey()
        val editingSystemPrompt = systemPrompt()
        val effectChannel = Channel<SettingGeminiEffect>(Channel.BUFFERED)
        val settingViewModel = settingViewModel(MutableStateFlow(SettingGeminiUiState.Content(setting = setting)), effectChannel.receiveAsFlow())
        setScreen(
            settingViewModel = settingViewModel,
            modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = modelList)),
        )
        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].performTextReplacement(editingApiKey)
        composeRule.onAllNodes(hasSetTextAction())[SYSTEM_PROMPT_INDEX].performTextReplacement(editingSystemPrompt)
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(modelList[1].id).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        effectChannel.trySend(effect)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(message).assertExists()
        verify(exactly = 1) { settingViewModel.save(setting = GeminiSetting(apiKey = editingApiKey, model = modelList[1].id, systemPrompt = editingSystemPrompt)) }
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_API_KEY_DESCRIPTION).performClick()
        composeRule.onAllNodes(hasSetTextAction())[API_KEY_INDEX].assert(hasText(editingApiKey))
        composeRule.onNodeWithText(editingSystemPrompt).assertExists()
        composeRule.onNode(hasContentDescription(modelList[1].id, substring = true)).assertExists()
    }

    @Test
    fun `TC-bdddedd2-c974-4c93-8fef-c8511fe74127 받아 둔 목록이 있을 때 모델 선택을 연 채 재생성되면 닫힌 채로 돌아오고 조회하지 않는다`() {
        val modelViewModel = modelViewModel(SettingGeminiModelUiState(isLoaded = true, modelList = modelList()))

        assertModelDialogClosedAfterRestore(modelViewModel = modelViewModel, fetchCount = 0)
    }

    @Test
    fun `TC-bdddedd2-c974-4c93-8fef-c8511fe74127 받아 둔 목록이 없을 때 모델 선택을 연 채 재생성되면 닫힌 채로 돌아오고 다시 조회하지 않는다`() {
        val modelViewModel = modelViewModel(SettingGeminiModelUiState())

        assertModelDialogClosedAfterRestore(modelViewModel = modelViewModel, fetchCount = 1)
    }

    private fun assertModelDialogClosedAfterRestore(
        modelViewModel: SettingGeminiModelViewModel,
        fetchCount: Int,
    ) {
        val settingViewModel = settingViewModel(SettingGeminiUiState.Content(setting = geminiSetting()))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                SettingGeminiScreen(
                    navigateUp = {},
                    componentVisibleProvider = { SettingGeminiScaffoldComponentVisible() },
                    settingViewModel = settingViewModel,
                    modelViewModel = modelViewModel,
                )
            }
        }
        composeRule.onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MODEL_PICKER_TITLE).assertDoesNotExist()
        verify(exactly = fetchCount) { modelViewModel.fetch(any()) }
    }

    private fun setScreen(
        settingViewModel: SettingGeminiViewModel = settingViewModel(SettingGeminiUiState.Content(setting = GeminiSetting.EMPTY)),
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
        private const val DEFAULT_RELOAD_DESCRIPTION = "Reload models"
        private const val DEFAULT_SHOW_API_KEY_DESCRIPTION = "Show API key"
        private const val DEFAULT_SAVE_SUCCEEDED_MESSAGE = "Saved."
        private const val DEFAULT_SAVE_FAILED_MESSAGE = "Couldn't save."
        private const val DEFAULT_FETCH_FAILED_MESSAGE = "Couldn't load models"

        private fun storedApiKey(): String = "storedApiKey${fixtureMonkey.giveMeOne<Uuid>()}"

        private fun editingApiKey(): String = "editingApiKey${fixtureMonkey.giveMeOne<Uuid>()}"

        private fun editingSuffix(): String = "editing${fixtureMonkey.giveMeOne<Uuid>()}"

        private fun modelId(): String = "models/${fixtureMonkey.giveMeOne<Uuid>()}"

        private fun systemPrompt(): String = "지시문 ${fixtureMonkey.giveMeOne<Uuid>()}"

        private fun geminiSetting(model: String = modelId()): GeminiSetting =
            GeminiSetting(
                apiKey = storedApiKey(),
                model = model,
                systemPrompt = systemPrompt(),
            )

        private fun modelList(): List<GeminiModel> =
            List(2) {
                val suffix = fixtureMonkey.giveMeOne<Uuid>()
                GeminiModel(id = "models/$suffix", displayName = "Gemini $suffix", description = "설명 $suffix")
            }

        private fun settingViewModel(uiState: SettingGeminiUiState): SettingGeminiViewModel = settingViewModel(MutableStateFlow(uiState))

        private fun settingViewModel(
            uiStateFlow: MutableStateFlow<SettingGeminiUiState>,
            effectFlow: Flow<SettingGeminiEffect> = emptyFlow(),
        ): SettingGeminiViewModel =
            mockk<SettingGeminiViewModel>(relaxed = true) {
                every { uiState } returns uiStateFlow
                every { effect } returns effectFlow
            }

        private fun modelViewModel(uiState: SettingGeminiModelUiState): SettingGeminiModelViewModel = modelViewModel(MutableStateFlow(uiState))

        private fun modelViewModel(
            uiStateFlow: MutableStateFlow<SettingGeminiModelUiState>,
            effectFlow: Flow<SettingGeminiModelEffect> = emptyFlow(),
        ): SettingGeminiModelViewModel =
            mockk<SettingGeminiModelViewModel>(relaxed = true) {
                every { uiState } returns uiStateFlow
                every { effect } returns effectFlow
            }
    }
}
