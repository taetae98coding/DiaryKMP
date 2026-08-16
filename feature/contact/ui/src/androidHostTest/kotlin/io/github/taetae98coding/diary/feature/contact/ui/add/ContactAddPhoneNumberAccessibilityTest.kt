package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddPhoneNumberAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 환경에서 번호 입력의 접근성 이름에 항목 순서를 담는다`() {
        composeRule.setContactAddScreen()
        composeRule.addPhoneNumberRow()
        composeRule.addPhoneNumberRow()

        composeRule.onNodeWithContentDescription(FIRST_NUMBER_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(SECOND_NUMBER_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "+ko")
    fun `한국어 환경에서 번호 입력의 접근성 이름에 항목 순서를 담는다`() {
        composeRule.setContactAddScreen()
        addKoreanPhoneNumberRow()
        addKoreanPhoneNumberRow()

        composeRule.onNodeWithContentDescription(KOREAN_FIRST_NUMBER_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_SECOND_NUMBER_DESCRIPTION).assertExists()
    }

    @Test
    fun `전화번호 항목마다 서로 다른 접근성 이름을 제공한다`() {
        composeRule.setContactAddScreen()
        composeRule.addPhoneNumberRow()
        composeRule.addPhoneNumberRow()

        composeRule.onAllNodesWithContentDescription(FIRST_NUMBER_DESCRIPTION).assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription(SECOND_NUMBER_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `전화번호 항목을 삭제하면 남은 항목의 순서를 다시 센다`() {
        composeRule.setContactAddScreen()
        composeRule.addPhoneNumberRow()
        composeRule.addPhoneNumberRow()
        composeRule.onNodeWithContentDescription(SECOND_NUMBER_DESCRIPTION).performTextInput(TYPED_SECOND_PHONE_NUMBER)
        composeRule.waitForIdle()

        composeRule.removePhoneNumberRow()

        composeRule.onAllNodesWithContentDescription(SECOND_NUMBER_DESCRIPTION).assertCountEquals(0)
        composeRule.onNodeWithContentDescription(FIRST_NUMBER_DESCRIPTION).assert(hasText(TYPED_SECOND_PHONE_NUMBER))
    }

    private fun addKoreanPhoneNumberRow() {
        composeRule.onNodeWithContentDescription(KOREAN_PHONE_NUMBER_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private companion object {
        private const val FIRST_NUMBER_DESCRIPTION = "Number 1"
        private const val SECOND_NUMBER_DESCRIPTION = "Number 2"
        private const val KOREAN_FIRST_NUMBER_DESCRIPTION = "1번째 번호"
        private const val KOREAN_SECOND_NUMBER_DESCRIPTION = "2번째 번호"
    }
}
