package io.github.taetae98coding.diary.feature.setting.ui.download.form

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingDownloadAddressInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `프록시 주소 입력의 접근성 이름은 라벨과 보조 문구를 이어 읽힌다`() {
        composeRule.setContent {
            DiaryTheme {
                SettingDownloadAddressInput()
            }
        }

        val config = composeRule.onNode(hasSetTextAction()).fetchSemanticsNode().config

        config.getOrNull(SemanticsProperties.Text).orEmpty().map { text -> text.text } shouldBe listOf(DEFAULT_LABEL)
        config.getOrNull(SemanticsProperties.ContentDescription).orEmpty() shouldBe listOf(DEFAULT_DESCRIPTION)
        composeRule.onAllNodes(hasText(DEFAULT_DESCRIPTION)).fetchSemanticsNodes().size shouldBe 0
    }
}

private const val DEFAULT_LABEL = "Proxy address"
private const val DEFAULT_DESCRIPTION = "Enter the address shown in Settings > Download of the desktop app running on the same network"
