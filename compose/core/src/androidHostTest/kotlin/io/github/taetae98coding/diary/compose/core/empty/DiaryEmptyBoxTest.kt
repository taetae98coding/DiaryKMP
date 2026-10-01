package io.github.taetae98coding.diary.compose.core.empty

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryEmptyBoxTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `제목과 보조 문구 묶음은 나타나면 화면 낭독 도구가 읽도록 알린다`() {
        composeRule.setContent {
            DiaryTheme {
                DiaryEmptyBox(
                    title = TITLE,
                    description = DESCRIPTION,
                    icon = {},
                )
            }
        }

        composeRule
            .onNodeWithText(TITLE)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        composeRule
            .onNodeWithText(DESCRIPTION)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    private companion object {
        const val TITLE = "제목"
        const val DESCRIPTION = "보조 문구"
    }
}
