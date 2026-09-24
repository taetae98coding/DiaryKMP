package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailFormTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-001 저장된 이름과 설명, 키, 신발 사이즈, 생일, 고향을 입력에 채운다`() {
        val detail =
            testContactDetail(
                name = CONTACT_NAME,
                description = CONTACT_DESCRIPTION,
                height = 180.5.centimeter,
                footSize = 270.millimeter,
                birthday = ContactBirthday(date = LocalDate(1994, 3, 21), calendar = ContactBirthdayCalendar.LUNAR),
                hometown = CONTACT_HOMETOWN,
            )

        setContactDetailScaffold(detail = detail)

        // 설명은 입력과 미리보기에 함께 나타나므로 편집할 수 있는 입력 노드를 지목한다.
        composeRule.onNode(hasText(CONTACT_DESCRIPTION) and hasSetTextAction()).assertExists()
        composeRule.onNodeWithText(HEIGHT_TEXT).assertExists()
        composeRule.onNodeWithText(FOOT_SIZE_TEXT).assertExists()
        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertExists()
        composeRule.onNode(hasText(CONTACT_HOMETOWN) and hasSetTextAction()).assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-024 채워진 고향을 다른 값으로 바꿀 수 있다`() {
        val detail = testContactDetail(name = CONTACT_NAME, hometown = CONTACT_HOMETOWN)

        setContactDetailScaffold(detail = detail)
        composeRule.onNode(hasText(CONTACT_HOMETOWN) and hasSetTextAction()).performTextReplacement(OTHER_CONTACT_HOMETOWN)
        composeRule.waitForIdle()

        composeRule.onNode(hasText(OTHER_CONTACT_HOMETOWN) and hasSetTextAction()).assertExists()
        composeRule.onNode(hasText(CONTACT_HOMETOWN) and hasSetTextAction()).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-024 고향이 없던 연락처에도 고향을 입력할 수 있다`() {
        val detail = testContactDetail(name = CONTACT_NAME, hometown = "")

        setContactDetailScaffold(detail = detail)
        composeRule.onNode(hasText(HOMETOWN_LABEL) and hasSetTextAction()).performTextReplacement(CONTACT_HOMETOWN)
        composeRule.waitForIdle()

        composeRule.onNode(hasText(CONTACT_HOMETOWN) and hasSetTextAction()).assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-023 저장된 양력 구분을 골라진 상태로 채운다`() {
        setContactDetailScaffold(detail = birthdayDetail(calendar = ContactBirthdayCalendar.SOLAR))

        composeRule.onNodeWithText(BIRTHDAY_CALENDAR_SOLAR).assertIsSelected()
        composeRule.onNodeWithText(BIRTHDAY_CALENDAR_LUNAR).assertIsNotSelected()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-023 저장된 음력 구분을 골라진 상태로 채운다`() {
        setContactDetailScaffold(detail = birthdayDetail(calendar = ContactBirthdayCalendar.LUNAR))

        composeRule.onNodeWithText(BIRTHDAY_CALENDAR_LUNAR).assertIsSelected()
        composeRule.onNodeWithText(BIRTHDAY_CALENDAR_SOLAR).assertIsNotSelected()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 달력 구분을 한국어 문구로 표시한다`() {
        setContactDetailScaffold(detail = birthdayDetail(calendar = ContactBirthdayCalendar.LUNAR))

        composeRule.onNodeWithContentDescription(KOREAN_BIRTHDAY_CALENDAR_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(KOREAN_BIRTHDAY_CALENDAR_SOLAR).assertIsNotSelected()
        composeRule.onNodeWithText(KOREAN_BIRTHDAY_CALENDAR_LUNAR).assertIsSelected()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-002 저장된 생일이 없으면 달력 구분을 고르는 수단을 제공하지 않는다`() {
        setContactDetailScaffold(detail = testContactDetail(name = CONTACT_NAME))

        composeRule.onNodeWithContentDescription(BIRTHDAY_CALENDAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-011 저장된 전화번호를 저장된 순서대로 채운다`() {
        val detail = testContactDetail(name = CONTACT_NAME, phoneNumberList = listOf(FIRST_PHONE_NUMBER, SECOND_PHONE_NUMBER))

        setContactDetailScaffold(detail = detail)

        composeRule.onNodeWithText(FIRST_PHONE_NUMBER).assertExists()
        composeRule.onNodeWithText(SECOND_PHONE_NUMBER).assertExists()
        composeRule.onAllNodesWithContentDescription(FIRST_NUMBER_DESCRIPTION).assertCountEquals(expectedSize = 1)
        composeRule.onAllNodesWithContentDescription(SECOND_NUMBER_DESCRIPTION).assertCountEquals(expectedSize = 1)
    }

    @Test
    fun `TC-CONTACT-DETAIL-DOMAIN-003 같은 번호가 여러 개여도 각각의 전화번호 항목으로 채운다`() {
        val detail =
            testContactDetail(
                name = CONTACT_NAME,
                phoneNumberList = listOf(FIRST_PHONE_NUMBER, SECOND_PHONE_NUMBER, FIRST_PHONE_NUMBER),
            )
        lateinit var state: ContactFormState

        composeRule.setContent {
            DiaryTheme {
                state = rememberContactDetailFormState(initialDetail = detail)

                ContactDetailTestScaffold(
                    onEvent = {},
                    state = state,
                    uiStateProvider = { ContactDetailUiState.Content(id = Uuid.random(), detail = detail) },
                )
            }
        }

        composeRule.runOnIdle {
            state.detail.phoneNumberList.map { phoneNumber -> phoneNumber.number } shouldBe
                listOf(FIRST_PHONE_NUMBER, SECOND_PHONE_NUMBER, FIRST_PHONE_NUMBER)
        }
        composeRule.onAllNodesWithContentDescription(FIRST_NUMBER_DESCRIPTION).assertCountEquals(expectedSize = 1)
        composeRule.onAllNodesWithContentDescription(THIRD_NUMBER_DESCRIPTION).assertCountEquals(expectedSize = 1)
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-002 값이 없는 정보는 비어 있는 상태로 채운다`() {
        val detail = testContactDetail(name = CONTACT_NAME, description = "", hometown = "")

        setContactDetailScaffold(detail = detail)

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onAllNodesWithContentDescription(FIRST_NUMBER_DESCRIPTION).assertCountEquals(expectedSize = 0)
        composeRule.onNode(hasText(HOMETOWN_LABEL) and hasSetTextAction()).assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-022 진입할 때 어느 입력에도 초점을 두지 않는다`() {
        setContactDetailScaffold(detail = testContactDetail(name = CONTACT_NAME))

        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).assertIsNotFocused()
    }

    @Test
    fun `TC-CONTACT-DETAIL-FEATURE-010 이름을 비워도 저장된 이름은 화면 제목에 남는다`() {
        val detail = testContactDetail(name = CONTACT_NAME)

        setContactDetailScaffold(detail = detail)
        composeRule.onNode(hasText(CONTACT_NAME) and hasSetTextAction()).performTextReplacement("")
        composeRule.waitForIdle()

        composeRule
            .onNode(hasText(CONTACT_NAME) and SemanticsMatcher.keyNotDefined(SemanticsProperties.EditableText))
            .assertExists()
    }

    @Test
    fun `TC-CONTACT-DETAIL-DOMAIN-009 전화번호의 순서를 바꾸면 수정 동작을 제공한다`() {
        val detail = testContactDetail(name = CONTACT_NAME, phoneNumberList = listOf(FIRST_PHONE_NUMBER, SECOND_PHONE_NUMBER))
        lateinit var state: ContactFormState

        composeRule.setContent {
            DiaryTheme {
                state = rememberContactDetailFormState(initialDetail = detail)

                ContactDetailTestScaffold(
                    onEvent = {},
                    state = state,
                    uiStateProvider = { ContactDetailUiState.Content(id = Uuid.random(), detail = detail) },
                )
            }
        }

        composeRule.runOnIdle {
            state.phoneNumberState.rowList[0]
                .numberState
                .setTextAndPlaceCursorAtEnd(SECOND_PHONE_NUMBER)
            state.phoneNumberState.rowList[1]
                .numberState
                .setTextAndPlaceCursorAtEnd(FIRST_PHONE_NUMBER)
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            state.detail.phoneNumberList.map { phoneNumber -> phoneNumber.number } shouldBe
                listOf(SECOND_PHONE_NUMBER, FIRST_PHONE_NUMBER)
        }
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    private fun setContactDetailScaffold(detail: ContactDetail) {
        composeRule.setContent {
            DiaryTheme {
                ContactDetailTestScaffold(
                    onEvent = {},
                    state = rememberContactDetailFormState(initialDetail = detail),
                    uiStateProvider = { ContactDetailUiState.Content(id = Uuid.random(), detail = detail) },
                )
            }
        }
    }

    private companion object {
        private const val CONTACT_NAME = "ContactDetailFormName"
        private const val CONTACT_DESCRIPTION = "ContactDetailFormDescription"
        private const val CONTACT_HOMETOWN = "ContactDetailFormHometown"
        private const val OTHER_CONTACT_HOMETOWN = "ContactDetailFormOtherHometown"
        private const val HOMETOWN_LABEL = "Hometown"
        private const val HEIGHT_TEXT = "180.5"
        private const val FOOT_SIZE_TEXT = "270"
        private const val BIRTHDAY_TEXT = "Mar 21, 1994"
        private const val FIRST_PHONE_NUMBER = "010-1111-2222"
        private const val SECOND_PHONE_NUMBER = "02-333-4444"
        private const val FIRST_NUMBER_DESCRIPTION = "Number 1"
        private const val SECOND_NUMBER_DESCRIPTION = "Number 2"
        private const val THIRD_NUMBER_DESCRIPTION = "Number 3"
        private const val DEFAULT_BIRTHDAY_NOT_SET = "Not set"
        private const val BIRTHDAY_CALENDAR_DESCRIPTION = "Birthday calendar"
        private const val BIRTHDAY_CALENDAR_SOLAR = "Solar"
        private const val BIRTHDAY_CALENDAR_LUNAR = "Lunar"
        private const val KOREAN_BIRTHDAY_CALENDAR_DESCRIPTION = "생일 달력"
        private const val KOREAN_BIRTHDAY_CALENDAR_SOLAR = "양력"
        private const val KOREAN_BIRTHDAY_CALENDAR_LUNAR = "음력"
        private const val DEFAULT_UPDATE_BUTTON_DESCRIPTION = "Update contact"

        private fun birthdayDetail(calendar: ContactBirthdayCalendar): ContactDetail =
            testContactDetail(
                name = CONTACT_NAME,
                birthday = ContactBirthday(date = LocalDate(1994, 3, 21), calendar = calendar),
            )
    }
}
