package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoGeminiCloseEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-023 상세 대상이 바뀌면 도우미를 닫는다`() {
        val viewModel = screenTestGeminiViewModel()
        var detailId by mutableStateOf(0)

        composeRule.setContent {
            key(detailId) {
                MemoGeminiCloseEffect(geminiViewModel = viewModel)
            }
        }

        composeRule.runOnIdle { verify(exactly = 1) { viewModel.close() } }

        composeRule.runOnIdle { detailId = 1 }

        composeRule.runOnIdle { verify(exactly = 2) { viewModel.close() } }
    }

    @Test
    fun `상세 대상이 그대로면 도우미를 닫지 않는다`() {
        val viewModel = screenTestGeminiViewModel()
        var recomposition by mutableStateOf(0)

        composeRule.setContent {
            key(0) {
                recomposition.toString()
                MemoGeminiCloseEffect(geminiViewModel = viewModel)
            }
        }

        composeRule.runOnIdle { recomposition = 1 }

        composeRule.runOnIdle { verify(exactly = 1) { viewModel.close() } }
    }
}
