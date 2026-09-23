package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactCardBirthdayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-HOME-FEATURE-020 양력 생일은 날짜 앞에 문구 없이 표시한다`() {
        setContactCard(contact = birthdayContact(calendar = ContactBirthdayCalendar.SOLAR))

        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertExists()
        composeRule.onNodeWithText(LUNAR_BIRTHDAY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-020 음력 생일은 날짜 앞에 음력임을 알리는 문구를 함께 표시한다`() {
        setContactCard(contact = birthdayContact(calendar = ContactBirthdayCalendar.LUNAR))

        composeRule.onNodeWithText(LUNAR_BIRTHDAY_TEXT).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-HOME-FEATURE-020 한국어 환경에서도 달력 구분에 따라 생일 문구가 갈린다`() {
        setContactCard(contact = birthdayContact(calendar = ContactBirthdayCalendar.LUNAR))

        composeRule.onNodeWithText(KOREAN_LUNAR_BIRTHDAY_TEXT).assertExists()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-021 생일이 없는 연락처는 생일을 표시하지 않는다`() {
        setContactCard(contact = testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER)))

        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(LUNAR_BIRTHDAY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-022 음력 생일도 저장된 연·월·일을 그대로 표시한다`() {
        setContactCard(contact = birthdayContact(calendar = ContactBirthdayCalendar.LUNAR))

        // 음력을 양력으로 환산하지 않으므로 저장된 1990-03-04이 그대로 읽힌다.
        composeRule.onNodeWithText(LUNAR_BIRTHDAY_TEXT).assertExists()
        composeRule.onNodeWithText(LUNAR_CONVERTED_BIRTHDAY_TEXT).assertDoesNotExist()
    }

    private fun birthdayContact(calendar: ContactBirthdayCalendar): Contact =
        testContact(
            name = CONTACT_NAME,
            phoneNumberList = listOf(CONTACT_PHONE_NUMBER),
            birthday = ContactBirthday(date = BIRTHDAY_DATE, calendar = calendar),
        )

    private fun setContactCard(contact: Contact) {
        composeRule.setContent {
            DiaryTheme {
                ContactCard(
                    onClick = {},
                    contact = contact,
                )
            }
        }
    }

    private companion object {
        private const val CONTACT_NAME = "ContactBirthdayName"
        private const val CONTACT_PHONE_NUMBER = "010-1111-2222"
        private val BIRTHDAY_DATE = LocalDate(year = 1990, month = 3, day = 4)

        private const val BIRTHDAY_TEXT = "Mar 4, 1990"
        private const val LUNAR_BIRTHDAY_TEXT = "Lunar $BIRTHDAY_TEXT"

        // 음력을 양력으로 환산하면 나올 다른 날짜로, 화면에 이 날짜가 나타나지 않는지 확인한다.
        private const val LUNAR_CONVERTED_BIRTHDAY_TEXT = "Lunar Mar 30, 1990"

        private const val KOREAN_LUNAR_BIRTHDAY_TEXT = "음력 1990. 3. 4."
    }
}
