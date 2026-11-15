package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RememberShareResultMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `영어 안내 문구`() {
        val message = setMessage()

        message.messageOf(result = ShareResult.COPIED) shouldBe "Copied to clipboard."
        message.messageOf(result = ShareResult.FAILED) shouldBe "Couldn't share."
    }

    @Config(qualifiers = "ko")
    @Test
    fun `한국어 안내 문구`() {
        val message = setMessage()

        message.messageOf(result = ShareResult.COPIED) shouldBe "클립보드에 복사했습니다."
        message.messageOf(result = ShareResult.FAILED) shouldBe "공유하지 못했습니다."
    }

    private fun setMessage(): ShareResultMessage {
        lateinit var message: ShareResultMessage
        composeRule.setContent { message = rememberShareResultMessage() }
        composeRule.waitForIdle()
        return message
    }
}
