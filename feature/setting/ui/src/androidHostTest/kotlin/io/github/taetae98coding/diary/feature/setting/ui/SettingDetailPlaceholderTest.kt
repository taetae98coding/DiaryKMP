package io.github.taetae98coding.diary.feature.setting.ui

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingDetailPlaceholderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-LIST-DETAIL-FEATURE-005 한국어 선택 전 안내를 표시한다`() {
        setSettingDetailPlaceholder()

        assertPlaceholder(message = "설정 항목을 선택하세요")
    }

    @Test
    fun `TC-SETTING-LIST-DETAIL-FEATURE-005 기본 환경의 선택 전 안내를 표시한다`() {
        setSettingDetailPlaceholder()

        assertPlaceholder(message = "Choose a setting")
    }

    private fun assertPlaceholder(message: String) {
        composeRule.onNodeWithTag(SETTING_DETAIL_PLACEHOLDER_ICON_TEST_TAG).assertExists()
        composeRule.onNodeWithText(message).assertExists()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 0
    }

    private fun setSettingDetailPlaceholder() {
        composeRule.setContent {
            DiaryTheme {
                SettingDetailPlaceholder()
            }
        }
    }
}
