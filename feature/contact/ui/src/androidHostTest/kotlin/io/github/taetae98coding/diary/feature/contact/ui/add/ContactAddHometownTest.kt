package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddHometownTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-ADD-FEATURE-028 고향을 입력한 뒤 지우면 비어 있는 상태로 되돌아간다`() {
        composeRule.setContactAddScreen()

        composeRule.hometownInput().performTextInput(TYPED_HOMETOWN)
        composeRule.hometownInput().assert(hasText(TYPED_HOMETOWN))

        composeRule.onNode(hasContentDescription(DEFAULT_CLEAR_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()

        composeRule.hometownInput().assert(hasText(""))
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-029 고향은 입력한 문자를 그대로 반영한다`() {
        composeRule.setContactAddScreen()

        HOMETOWN_CASE_LIST.forEach { hometown ->
            composeRule.hometownInput().performTextClearance()
            composeRule.hometownInput().performTextInput(hometown)
            composeRule.waitForIdle()

            composeRule.hometownInput().assert(hasText(hometown))
        }
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-029 형식을 검사하지 않으므로 어떤 문자를 넣어도 오류로 표시하지 않는다`() {
        composeRule.setContactAddScreen()

        composeRule.hometownInput().performTextInput(INVALID_LOOKING_HOMETOWN)
        composeRule.waitForIdle()

        composeRule.hometownInput().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
    }

    private companion object {
        private const val DEFAULT_CLEAR_BUTTON_DESCRIPTION = "Clear text"
        private const val INVALID_LOOKING_HOMETOWN = "123-!@#"

        private val HOMETOWN_CASE_LIST =
            listOf(
                "서울",
                "강원도 춘천시",
                "Seoul, Korea",
                "123-!@#",
            )
    }
}
