package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.feature.setting.ui.holiday.DEFAULT_LOADING_DESCRIPTION
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingGeminiModelDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-008 받아 온 모델을 모두 고를 수 있다`() {
        setDialog(uiState = SettingGeminiModelUiState(isLoaded = true, modelList = MODEL_LIST))

        MODEL_LIST.forEach { model ->
            composeRule.onNodeWithText(model.displayName).assertExists()
            composeRule.onNodeWithText(model.id).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-008 모델을 고르면 선택한 모델을 알린다`() {
        val selectedList = mutableListOf<String>()
        setDialog(
            uiState = SettingGeminiModelUiState(isLoaded = true, modelList = MODEL_LIST),
            onSelect = { id -> selectedList += id },
        )

        composeRule.onNodeWithText(MODEL_LIST[1].id).performClick()
        composeRule.waitForIdle()

        selectedList shouldBe listOf(MODEL_LIST[1].id)
    }

    @Test
    fun `현재 선택한 모델만 선택 상태로 표시한다`() {
        setDialog(
            uiState = SettingGeminiModelUiState(isLoaded = true, modelList = MODEL_LIST),
            selectedModel = MODEL_LIST[0].id,
        )

        composeRule.onNodeWithText(MODEL_LIST[0].id).assertIsSelected()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-009 고를 수 있는 모델이 없으면 안내를 표시한다`() {
        setDialog(uiState = SettingGeminiModelUiState(isLoaded = true, modelList = emptyList()))

        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-010 인증 정보가 유효하지 않으면 인증 정보 확인을 안내한다`() {
        setDialog(uiState = SettingGeminiModelUiState(failure = SettingGeminiModelFailure.INVALID_API_KEY))

        composeRule.onNodeWithText(DEFAULT_INVALID_API_KEY_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FETCH_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_ACTION).assert(hasClickAction())
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-010 그 밖의 실패는 조회 실패를 안내한다`() {
        setDialog(uiState = SettingGeminiModelUiState(failure = SettingGeminiModelFailure.UNKNOWN))

        composeRule.onNodeWithText(DEFAULT_FETCH_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_INVALID_API_KEY_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_ACTION).assert(hasClickAction())
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-006 목록이 없는 동안 조회하면 진행 표시를 보여 준다`() {
        setDialog(uiState = SettingGeminiModelUiState(isInProgress = true))

        composeRule.onAllNodesWithContentDescription(DEFAULT_LOADING_DESCRIPTION).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-011 조회에 실패해도 받아 둔 목록을 계속 고를 수 있다`() {
        setDialog(
            uiState =
                SettingGeminiModelUiState(
                    isLoaded = true,
                    modelList = MODEL_LIST,
                    failure = SettingGeminiModelFailure.UNKNOWN,
                ),
        )

        composeRule.onNodeWithText(DEFAULT_FETCH_FAILED_MESSAGE).assertDoesNotExist()
        MODEL_LIST.forEach { model ->
            composeRule.onNodeWithText(model.id).assert(hasClickAction())
        }
    }

    @Test
    fun `다시 불러오기 동작을 제공한다`() {
        var reloadCount = 0
        setDialog(
            uiState = SettingGeminiModelUiState(isLoaded = true, modelList = MODEL_LIST),
            onReload = { reloadCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_RELOAD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        reloadCount shouldBe 1
    }

    @Test
    fun `설명이 없는 모델은 설명 줄을 표시하지 않는다`() {
        val model = GeminiModel(id = "models/no-description", displayName = "No Description", description = "")
        setDialog(uiState = SettingGeminiModelUiState(isLoaded = true, modelList = listOf(model)))

        composeRule.onNodeWithText(model.displayName).assertExists()
        composeRule.onNodeWithText(model.id).assertExists()
    }

    private fun setDialog(
        uiState: SettingGeminiModelUiState,
        selectedModel: String = "",
        onSelect: (String) -> Unit = {},
        onReload: () -> Unit = {},
        onDismissRequest: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingGeminiModelDialog(
                    onDismissRequest = onDismissRequest,
                    onEvent = { event ->
                        when (event) {
                            is SettingGeminiModelDialogEvent.ClickReload -> onReload()
                            is SettingGeminiModelDialogEvent.SelectModel -> onSelect(event.id)
                        }
                    },
                    selectedModelProvider = { selectedModel },
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_EMPTY_MESSAGE = "No available models"
        private const val DEFAULT_INVALID_API_KEY_MESSAGE = "Check your API key"
        private const val DEFAULT_FETCH_FAILED_MESSAGE = "Couldn't load models"
        private const val DEFAULT_RETRY_ACTION = "Retry"
        private const val DEFAULT_RELOAD_DESCRIPTION = "Reload models"
        private const val DEFAULT_LOADING_DESCRIPTION = "Loading models"

        private val MODEL_LIST =
            listOf(
                GeminiModel(id = "models/gemini-flash", displayName = "Gemini Flash", description = "빠른 범용 모델"),
                GeminiModel(id = "models/gemini-pro", displayName = "Gemini Pro", description = "정확한 범용 모델"),
            )
    }
}
