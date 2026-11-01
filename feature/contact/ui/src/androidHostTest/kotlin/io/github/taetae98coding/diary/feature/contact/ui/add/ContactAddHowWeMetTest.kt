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
class ContactAddHowWeMetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-81f8ab0b-e12f-46a6-bdaa-ed48f2fb68b5 알게 된 경로를 입력한 뒤 지우면 비어 있는 상태로 되돌아간다`() {
        composeRule.setContactAddScreen()

        composeRule.howWeMetInput().performTextInput(TYPED_HOW_WE_MET)
        composeRule.howWeMetInput().assert(hasText(TYPED_HOW_WE_MET))

        composeRule.onNode(hasContentDescription(DEFAULT_CLEAR_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()

        composeRule.howWeMetInput().assert(hasText(""))
    }

    @Test
    fun `TC-c8f01f64-07ab-47c4-848d-c4cae1a2e7b6 알게 된 경로는 입력한 문자를 그대로 반영한다`() {
        composeRule.setContactAddScreen()

        HOW_WE_MET_CASE_LIST.forEach { howWeMet ->
            composeRule.howWeMetInput().performTextClearance()
            composeRule.howWeMetInput().performTextInput(howWeMet)
            composeRule.waitForIdle()

            composeRule.howWeMetInput().assert(hasText(howWeMet))
        }
    }

    @Test
    fun `TC-c8f01f64-07ab-47c4-848d-c4cae1a2e7b6 형식을 검사하지 않으므로 어떤 문자를 넣어도 오류로 표시하지 않는다`() {
        composeRule.setContactAddScreen()

        composeRule.howWeMetInput().performTextInput(INVALID_LOOKING_HOW_WE_MET)
        composeRule.waitForIdle()

        composeRule.howWeMetInput().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))
    }

    private companion object {
        private const val DEFAULT_CLEAR_BUTTON_DESCRIPTION = "Clear text"
        private const val INVALID_LOOKING_HOW_WE_MET = "123-!@#"

        private val HOW_WE_MET_CASE_LIST =
            listOf(
                "친구 소개",
                "대학 동아리 신입생 환영회",
                "Met at work",
                "123-!@#",
            )
    }
}
