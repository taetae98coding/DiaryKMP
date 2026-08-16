package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-HOME-FEATURE-016 아직 준비되지 않은 자리는 선택할 수 없다`() {
        var clickCount = 0

        setContactCard(contact = null, onClick = { clickCount++ })
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performClick()

        clickCount shouldBe 0
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-015 준비된 연락처 카드는 선택할 수 있다`() {
        var clickCount = 0

        setContactCard(
            contact = testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER)),
            onClick = { clickCount++ },
        )
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).assertHasClickAction()
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performClick()

        clickCount shouldBe 1
    }

    private fun setContactCard(
        contact: Contact?,
        onClick: () -> Unit,
    ) {
        composeRule.setContent {
            DiaryTheme {
                ContactCard(
                    onClick = onClick,
                    contact = contact,
                )
            }
        }
    }

    private companion object {
        private const val CONTACT_NAME = "ContactCardName"
        private const val CONTACT_PHONE_NUMBER = "010-9999-8888"
    }
}
