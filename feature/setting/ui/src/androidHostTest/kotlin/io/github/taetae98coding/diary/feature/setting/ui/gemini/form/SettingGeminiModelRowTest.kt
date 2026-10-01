package io.github.taetae98coding.diary.feature.setting.ui.gemini.form

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingGeminiModelRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `모델 선택 줄은 버튼 역할과 누름 동작을 제공한다`() {
        composeRule.setContent {
            DiaryTheme {
                SettingGeminiModelRow(onClick = {})
            }
        }

        composeRule
            .onNode(hasContentDescription(DEFAULT_MODEL_LABEL, substring = true))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(hasClickAction())
    }

    private companion object {
        private const val DEFAULT_MODEL_LABEL = "Model"
    }
}
