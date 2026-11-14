package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.contact.ui.addTagScreenTestViewModel
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddInitialDetailTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `공유로 연 화면은 받은 이름, 설명과 전화번호를 순서대로 채운다`() {
        composeRule.setContactAddScreen(initialDetail = sharedKey().initialDetail())
        composeRule.waitForIdle()

        composeRule.nameInput().assert(hasText(SHARED_NAME))
        composeRule.descriptionInput().assert(hasText(SHARED_DESCRIPTION))
        composeRule.phoneNumberRowCount() shouldBe 2
        composeRule.phoneNumberInput(row = 0).assert(hasText(SHARED_FIRST_PHONE_NUMBER))
        composeRule.phoneNumberInput(row = 1).assert(hasText(SHARED_SECOND_PHONE_NUMBER))
        composeRule.heightInput().assert(hasText(""))
        composeRule.footSizeInput().assert(hasText(""))
        composeRule.hometownInput().assert(hasText(""))
        composeRule.howWeMetInput().assert(hasText(""))
    }

    @Test
    fun `공유로 받은 생일은 양력 생일로 채우고 태어난 연도는 생일의 연도가 된다`() {
        composeRule.setContactAddScreen(initialDetail = sharedKey().initialDetail())
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARED_BIRTHDAY_TEXT).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsSelected()
        composeRule.birthYearInput().assert(hasText(SHARED_BIRTHDAY.year.toString()))
    }

    @Test
    fun `공유로 받은 생일이 없으면 생일과 태어난 연도는 비어 있다`() {
        composeRule.setContactAddScreen(initialDetail = sharedKey().copy(initialBirthday = null).initialDetail())
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.birthYearInput().assert(hasText(""))
    }

    @Test
    fun `공유로 채운 값으로 추가하면 채운 값 그대로 추가를 요청한다`() {
        val viewModel = screenTestViewModel()
        composeRule.setContactAddScreen(viewModel = viewModel, initialDetail = sharedKey().initialDetail())
        composeRule.waitForIdle()

        composeRule.clickAdd()

        val expected =
            ContactDetail.EMPTY.copy(
                name = SHARED_NAME,
                description = SHARED_DESCRIPTION,
                birthday = ContactBirthday(date = SHARED_BIRTHDAY, calendar = ContactBirthdayCalendar.SOLAR),
                birthYear = SHARED_BIRTHDAY.year,
                phoneNumberList = listOf(ContactPhoneNumber(number = SHARED_FIRST_PHONE_NUMBER), ContactPhoneNumber(number = SHARED_SECOND_PHONE_NUMBER)),
            )
        verify(exactly = 1) { viewModel.add(detail = expected, tagIdSet = emptySet()) }
    }

    @Test
    fun `공유로 연 화면도 처음 진입할 때와 같이 이름 입력에 초점이 있다`() {
        composeRule.setContactAddScreen(initialDetail = sharedKey().initialDetail())
        composeRule.waitForIdle()

        composeRule.nameInput().assertIsFocused()
    }

    @Test
    fun `TC-713137bd-b0dc-4b28-ac7f-6fbbe6fae8d4 공유로 화면을 열기만 해서는 연락처를 추가하지 않는다`() {
        val viewModel = screenTestViewModel()

        composeRule.setContactAddScreen(viewModel = viewModel, initialDetail = sharedKey().initialDetail())
        composeRule.waitForIdle()

        verify(exactly = 0) { viewModel.add(detail = any(), tagIdSet = any()) }
    }

    @Test
    fun `공유로 채운 값을 고친 뒤 화면이 재생성되면 고친 값을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            ContactAddScreenTestTheme {
                ContactAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addedResultRequestKey = null,
                    initialDetail = sharedKey().initialDetail(),
                    componentVisibleProvider = { ContactAddScaffoldComponentVisible() },
                    addViewModel = screenTestViewModel(),
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
        composeRule.nameInput().performTextReplacement(TYPED_NAME)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.phoneNumberRowCount() shouldBe 2
        composeRule.onNodeWithText(SHARED_BIRTHDAY_TEXT).assertExists()
    }

    private fun sharedKey(): ContactAddNavKey =
        ContactAddNavKey(
            initialName = SHARED_NAME,
            initialDescription = SHARED_DESCRIPTION,
            initialPhoneNumberList = listOf(SHARED_FIRST_PHONE_NUMBER, SHARED_SECOND_PHONE_NUMBER),
            initialBirthday = SHARED_BIRTHDAY,
            isStandalone = true,
        )

    private companion object {
        const val SHARED_NAME = "홍길동"
        const val SHARED_DESCRIPTION = "대학 동기\n이메일: a@b.com"
        const val SHARED_FIRST_PHONE_NUMBER = "010-1234-5678"
        const val SHARED_SECOND_PHONE_NUMBER = "02-987-6543"
        const val SHARED_BIRTHDAY_TEXT = "Mar 15, 1990"
        val SHARED_BIRTHDAY = LocalDate(year = 1990, month = Month.MARCH, day = 15)
    }
}
