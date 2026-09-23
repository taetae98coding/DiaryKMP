package io.github.taetae98coding.diary.feature.setting.ui.gemini.form

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val EDITING_API_KEY = "editingApiKey"
private const val OTHER_API_KEY = "otherApiKey"
private const val STORED_API_KEY = "storedApiKey"
private const val MODEL_ID = "models/gemini-flash"
private const val OTHER_MODEL_ID = "models/gemini-pro"
private const val SYSTEM_PROMPT = "첫 줄\n둘째 줄"
private const val STORED_SYSTEM_PROMPT = "지시문"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingGeminiFormStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-GEMINI-FEATURE-023 화면이 재생성되어도 입력 중이던 값을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: SettingGeminiFormState

        restorationTester.setContent {
            state = rememberSettingGeminiFormState()
        }

        composeRule.runOnIdle {
            state.apiKeyState.setTextAndPlaceCursorAtEnd(EDITING_API_KEY)
            state.systemPromptState.setTextAndPlaceCursorAtEnd(SYSTEM_PROMPT)
            state.model = MODEL_ID
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            state.setting shouldBe
                GeminiSetting(
                    apiKey = EDITING_API_KEY,
                    model = MODEL_ID,
                    systemPrompt = SYSTEM_PROMPT,
                )
        }
    }

    @Test
    fun `화면이 재생성되면 인증 정보를 다시 가린 상태로 되돌린다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: SettingGeminiFormState

        restorationTester.setContent {
            state = rememberSettingGeminiFormState()
        }

        composeRule.runOnIdle { state.isApiKeyVisible = true }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { state.isApiKeyVisible shouldBe false }
    }

    @Test
    fun `저장된 설정을 초기 값으로 편집 상태를 만든다`() {
        val setting = GeminiSetting(apiKey = STORED_API_KEY, model = OTHER_MODEL_ID, systemPrompt = STORED_SYSTEM_PROMPT)
        lateinit var state: SettingGeminiFormState

        composeRule.setContent {
            state = rememberSettingGeminiFormState(initialSetting = setting)
        }

        composeRule.runOnIdle { state.setting shouldBe setting }
    }

    @Test
    fun `TC-SETTING-GEMINI-DOMAIN-003 저장된 설정이 바뀌어도 편집 중인 값을 덮어쓰지 않는다`() {
        val initial = GeminiSetting(apiKey = STORED_API_KEY, model = OTHER_MODEL_ID, systemPrompt = STORED_SYSTEM_PROMPT)
        val storedSetting = mutableStateOf(initial)
        lateinit var state: SettingGeminiFormState

        composeRule.setContent {
            val setting by storedSetting
            state = rememberSettingGeminiFormState(initialSetting = setting)
        }

        composeRule.runOnIdle { state.apiKeyState.setTextAndPlaceCursorAtEnd(EDITING_API_KEY) }
        composeRule.runOnIdle { storedSetting.value = initial.copy(apiKey = OTHER_API_KEY) }

        composeRule.runOnIdle { state.apiKey shouldBe EDITING_API_KEY }
    }
}
