package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoGeminiCloseEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-023 상세 대상이 바뀌면 도우미를 닫는다`() {
        val viewModel = screenTestGeminiViewModel()
        var targetId by mutableStateOf<Uuid?>(Uuid.random())

        composeRule.setContent {
            MemoGeminiCloseEffect(targetId = targetId, geminiViewModel = viewModel)
        }

        composeRule.runOnIdle { verify(exactly = 0) { viewModel.close() } }

        composeRule.runOnIdle { targetId = Uuid.random() }

        composeRule.runOnIdle { verify(exactly = 1) { viewModel.close() } }
    }

    @Test
    fun `상세 대상이 그대로면 도우미를 닫지 않는다`() {
        val viewModel = screenTestGeminiViewModel()
        val id = Uuid.random()
        var targetId by mutableStateOf<Uuid?>(null)

        composeRule.setContent {
            MemoGeminiCloseEffect(targetId = targetId, geminiViewModel = viewModel)
        }

        composeRule.runOnIdle { targetId = id }
        composeRule.runOnIdle { targetId = null }
        composeRule.runOnIdle { targetId = id }

        composeRule.runOnIdle { verify(exactly = 0) { viewModel.close() } }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-022 화면이 재생성되어도 상세 대상이 그대로면 도우미를 닫지 않는다`() {
        val viewModel = screenTestGeminiViewModel()
        val id = Uuid.random()
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            MemoGeminiCloseEffect(targetId = id, geminiViewModel = viewModel)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { verify(exactly = 0) { viewModel.close() } }
    }
}
