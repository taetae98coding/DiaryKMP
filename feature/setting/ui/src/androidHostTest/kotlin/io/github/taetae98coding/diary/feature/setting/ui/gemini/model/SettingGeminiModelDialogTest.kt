package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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
    fun `TC-03a2c699-e091-4756-80e2-d0dcde9f0b3b 받아 온 모델을 모두 고를 수 있다`() {
        setDialog(uiState = SettingGeminiModelUiState(isLoaded = true, modelList = MODEL_LIST))

        MODEL_LIST.forEach { model ->
            composeRule.onNodeWithText(model.displayName).assertExists()
            composeRule.onNodeWithText(model.id).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-03a2c699-e091-4756-80e2-d0dcde9f0b3b 모델을 고르면 선택한 모델을 알린다`() {
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
    fun `TC-94a954b5-65a0-4eb1-ab10-090f97cc5707 고를 수 있는 모델이 없으면 안내를 표시한다`() {
        setDialog(uiState = SettingGeminiModelUiState(isLoaded = true, modelList = emptyList()))

        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-0f0c8862-4112-4544-8f20-216852b8a0ba 인증 정보가 유효하지 않으면 인증 정보 확인을 안내한다`() {
        setDialog(uiState = SettingGeminiModelUiState(failure = SettingGeminiModelFailure.INVALID_API_KEY))

        composeRule.onNodeWithText(DEFAULT_INVALID_API_KEY_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FETCH_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_ACTION).assert(hasClickAction())
    }

    @Test
    fun `TC-0f0c8862-4112-4544-8f20-216852b8a0ba 그 밖의 실패는 조회 실패를 안내한다`() {
        setDialog(uiState = SettingGeminiModelUiState(failure = SettingGeminiModelFailure.UNKNOWN))

        composeRule.onNodeWithText(DEFAULT_FETCH_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_INVALID_API_KEY_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_ACTION).assert(hasClickAction())
    }

    @Test
    fun `TC-5414e004-7086-416f-82d2-9554e9842fa6 조회하는 동안 진행 표시를 보여 준다`() {
        setDialog(uiState = SettingGeminiModelUiState(isInProgress = true))

        composeRule.onAllNodesWithContentDescription(DEFAULT_LOADING_DESCRIPTION).fetchSemanticsNodes().size shouldBe 1
    }

    @Test
    fun `TC-c041e172-eb66-4967-93e4-6589f737dd13 실패 안내에서 다시 시도하면 다시 조회를 요청한다`() {
        var retryCount = 0
        setDialog(
            uiState = SettingGeminiModelUiState(failure = SettingGeminiModelFailure.UNKNOWN),
            onRetry = { retryCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_RETRY_ACTION).performClick()
        composeRule.waitForIdle()

        retryCount shouldBe 1
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
        onRetry: () -> Unit = {},
        onDismissRequest: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingGeminiModelDialog(
                    onDismissRequest = onDismissRequest,
                    onEvent = { event ->
                        when (event) {
                            is SettingGeminiModelDialogEvent.ClickRetry -> onRetry()
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
        private const val DEFAULT_LOADING_DESCRIPTION = "Loading models"

        private val MODEL_LIST =
            listOf(
                GeminiModel(id = "models/gemini-flash", displayName = "Gemini Flash", description = "빠른 범용 모델"),
                GeminiModel(id = "models/gemini-pro", displayName = "Gemini Pro", description = "정확한 범용 모델"),
            )
    }
}
