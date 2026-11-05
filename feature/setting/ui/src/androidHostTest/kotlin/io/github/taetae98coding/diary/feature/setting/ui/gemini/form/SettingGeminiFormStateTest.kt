package io.github.taetae98coding.diary.feature.setting.ui.gemini.form

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.core.testing.gemini.geminiSetting
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingGeminiFormStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-1aa3bc50-802d-4ef8-b2e2-9fe319706954 화면이 재생성되어도 입력 중이던 값을 유지한다`() {
        val editing = Arb.geminiSetting().next().copy(systemPrompt = "${Arb.nonBlankString().next()}\n${Arb.nonBlankString().next()}")
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: SettingGeminiFormState

        restorationTester.setContent {
            state = rememberSettingGeminiFormState()
        }

        composeRule.runOnIdle {
            state.apiKeyState.setTextAndPlaceCursorAtEnd(editing.apiKey)
            state.systemPromptState.setTextAndPlaceCursorAtEnd(editing.systemPrompt)
            state.model = editing.model
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            state.setting shouldBe editing
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
        val setting = Arb.geminiSetting().next()
        lateinit var state: SettingGeminiFormState

        composeRule.setContent {
            state = rememberSettingGeminiFormState(initialSetting = setting)
        }

        composeRule.runOnIdle { state.setting shouldBe setting }
    }

    @Test
    fun `TC-4d92bc2f-8b06-40ba-a362-61422ff97702 저장된 설정이 바뀌어도 편집 중인 값을 덮어쓰지 않는다`() {
        val initial = Arb.geminiSetting().next()
        val editingApiKey = initial.apiKey + Arb.nonBlankString().next()
        val otherApiKey = initial.apiKey + Arb.nonBlankString().next()
        val storedSetting = mutableStateOf(initial)
        lateinit var state: SettingGeminiFormState

        composeRule.setContent {
            val setting by storedSetting
            state = rememberSettingGeminiFormState(initialSetting = setting)
        }

        composeRule.runOnIdle { state.apiKeyState.setTextAndPlaceCursorAtEnd(editingApiKey) }
        composeRule.runOnIdle { storedSetting.value = initial.copy(apiKey = otherApiKey) }

        composeRule.runOnIdle { state.apiKey shouldBe editingApiKey }
    }
}
