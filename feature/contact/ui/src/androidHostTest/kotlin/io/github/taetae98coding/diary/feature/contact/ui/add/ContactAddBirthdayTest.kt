package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.datetime.LocalDate
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
    fun `TC-190f3b9f-b842-4dbc-9007-c4e5298ebb8d 생일을 골라 지정할 수 있다`() {
        composeRule.setContactAddScreen()

        composeRule.selectBirthday()

        composeRule.onNodeWithText(todayDisplayText()).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-7f9986ff-4183-4f43-a73e-d49fe82663fe 생일 고르기를 취소하면 고르지 않은 상태로 남는다`() {
        composeRule.setContactAddScreen()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_DATE_PICKER_CANCEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-98af11d8-cc45-4994-9e58-70de762a100c 고른 생일을 지울 수 있다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()

        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-f9703b6d-e2ce-49a0-8f26-62e04f62ce88 생일을 고르면 달력 구분이 양력으로 시작한다`() {
        composeRule.setContactAddScreen()

        composeRule.selectBirthday()

        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CALENDAR_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsNotSelected()
    }

    @Test
    fun `TC-8e19869a-6221-4f89-9d6b-9d87aac46b32 생일을 고르지 않은 동안에는 달력 구분을 고르는 수단을 제공하지 않는다`() {
        composeRule.setContactAddScreen()

        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CALENDAR_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertDoesNotExist()
    }

    @Test
    fun `TC-2ec1bd13-5fec-4878-a5d0-20b3897acdf8 고른 생일을 음력으로 바꿀 수 있다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()

        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsNotSelected()
        composeRule.onNodeWithText(todayDisplayText()).assertExists()
    }

    @Test
    fun `TC-2ec1bd13-5fec-4878-a5d0-20b3897acdf8 음력으로 바꾼 생일을 양력으로 되돌릴 수 있다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_SOLAR)

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsNotSelected()
        composeRule.onNodeWithText(todayDisplayText()).assertExists()
    }

    @Test
    fun `TC-34577e79-b042-4b35-a57c-cb9844ce4715 생일을 지우면 달력 구분을 고르는 수단도 사라진다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CLEAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CALENDAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-ea8e375a-09fd-4c7f-b252-34e5728b6f27 생일을 지운 뒤 다시 고르면 달력 구분이 양력으로 시작한다`() {
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
    fun `TC-7475cbee-74a9-4c80-81dd-a330192e847a 화면이 재생성되어도 고른 달력 구분을 유지한다`() {
        val viewModel = screenTestViewModel()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            ContactAddScreenTestTheme {
                ContactAddScreen(
                    addedResultRequestKey = null,
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
    fun `TC-13b450a4-db32-4a85-ad4b-f8d6102a596c 생일 날짜만 다시 골라도 달력 구분은 유지된다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        val reselectedDisplayText = composeRule.reselectBirthday()

        composeRule.onNodeWithText(reselectedDisplayText).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsNotSelected()
    }

    @Test
    fun `TC-b8b2e74e-d6e1-4d15-8a3f-8018e5ef1578 생일을 고르지 않았으면 생일 고르기가 오늘 날짜에서 시작한다`() {
        composeRule.setContactAddScreen()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).performClick()
        composeRule.waitForIdle()

        composeRule.pickerSelection(today()).assertExists()
    }

    @Test
    fun `TC-76c9eecd-5b3d-4d22-937d-3b6a5332402a 양력으로 고른 생일이 있으면 생일 고르기가 그 날짜에서 시작한다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()
        val reselectedDisplayText = composeRule.reselectBirthday()

        composeRule.onNodeWithText(reselectedDisplayText).performClick()
        composeRule.waitForIdle()

        composeRule.pickerSelection(reselectBirthdayDate()).assertExists()
        composeRule.pickerSelection(today()).assertDoesNotExist()
    }

    @Test
    fun `TC-76c9eecd-5b3d-4d22-937d-3b6a5332402a 음력으로 고른 생일이 있으면 생일 고르기가 그 날짜에서 시작한다`() {
        composeRule.setContactAddScreen()
        composeRule.selectBirthday()
        val reselectedDisplayText = composeRule.reselectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)

        composeRule.onNodeWithText(reselectedDisplayText).performClick()
        composeRule.waitForIdle()

        composeRule.pickerSelection(reselectBirthdayDate()).assertExists()
        composeRule.pickerSelection(today()).assertDoesNotExist()
    }

    // 날짜 고르기는 머리글에서 지금 가리키는 날짜를 기본 환경의 문구로 알린다.
    private fun ComposeContentTestRule.pickerSelection(date: LocalDate): SemanticsNodeInteraction = onNodeWithContentDescription("$DEFAULT_DATE_PICKER_CURRENT_SELECTION_PREFIX${dayCellText(date)}")

    private companion object {
        private const val DEFAULT_DATE_PICKER_CURRENT_SELECTION_PREFIX = "Current selection: "
    }
}
