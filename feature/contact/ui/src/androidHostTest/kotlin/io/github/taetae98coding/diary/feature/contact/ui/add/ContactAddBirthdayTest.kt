package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddBirthdayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-ADD-FEATURE-019 생일을 골라 지정할 수 있다`() {
        composeRule.setContactAddScreen()

        composeRule.selectBirthday()

        composeRule.onNodeWithText(todayDisplayText()).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-020 생일 고르기를 취소하면 고르지 않은 상태로 남는다`() {
        composeRule.setContactAddScreen()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_DATE_PICKER_CANCEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-021 고른 생일을 지울 수 있다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()

        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-022 생일을 고르면 달력 구분이 양력으로 시작한다`() {
        composeRule.setContactAddScreen()

        composeRule.selectBirthday()

        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CALENDAR_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsNotSelected()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-022 생일을 고르지 않은 동안에는 달력 구분을 고르는 수단을 제공하지 않는다`() {
        composeRule.setContactAddScreen()

        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CALENDAR_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-023 고른 생일을 음력으로 바꿀 수 있다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()

        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsNotSelected()
        composeRule.onNodeWithText(todayDisplayText()).assertExists()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-023 음력으로 바꾼 생일을 양력으로 되돌릴 수 있다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_SOLAR)

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsNotSelected()
        composeRule.onNodeWithText(todayDisplayText()).assertExists()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-024 생일을 지우면 달력 구분을 고르는 수단도 사라진다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CALENDAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-025 생일을 지운 뒤 다시 고르면 달력 구분이 양력으로 시작한다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.selectBirthday()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsNotSelected()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-026 화면이 재생성되어도 고른 달력 구분을 유지한다`() {
        val viewModel = screenTestViewModel()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                ContactAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { ContactAddScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.selectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(todayDisplayText()).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsNotSelected()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-027 생일 날짜만 다시 골라도 달력 구분은 유지된다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        val reselectedDisplayText = composeRule.reselectBirthday()

        composeRule.onNodeWithText(reselectedDisplayText).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsNotSelected()
    }
}
