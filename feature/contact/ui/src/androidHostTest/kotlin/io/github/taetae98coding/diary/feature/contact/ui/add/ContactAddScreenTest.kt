package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-ADD-FEATURE-001 화면에 처음 진입하면 입력이 모두 비어 있다`() {
        composeRule.setContactAddScreen()

        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_PHONE_NUMBER
        composeRule.nameInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.heightInput().assert(hasText(""))
        composeRule.footSizeInput().assert(hasText(""))
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.phoneNumberRowCount() shouldBe 0
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-003 전화번호 항목을 추가해 번호를 입력할 수 있다`() {
        composeRule.setContactAddScreen()

        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput().performTextInput(TYPED_FIRST_PHONE_NUMBER)
        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput(row = 1).performTextInput(TYPED_SECOND_PHONE_NUMBER)
        composeRule.waitForIdle()

        composeRule.phoneNumberRowCount() shouldBe 2
        composeRule.phoneNumberInput().assert(hasText(TYPED_FIRST_PHONE_NUMBER))
        composeRule.phoneNumberInput(row = 1).assert(hasText(TYPED_SECOND_PHONE_NUMBER))
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-004 전화번호 항목을 삭제해도 다른 입력은 유지된다`() {
        composeRule.setContactAddScreen()
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput().performTextInput(TYPED_FIRST_PHONE_NUMBER)
        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput(row = 1).performTextInput(TYPED_SECOND_PHONE_NUMBER)
        composeRule.waitForIdle()

        composeRule.removePhoneNumberRow()

        composeRule.phoneNumberRowCount() shouldBe 1
        composeRule.phoneNumberInput().assert(hasText(TYPED_SECOND_PHONE_NUMBER))
        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-014 전화번호 항목을 추가하거나 삭제해도 초점을 옮기지 않는다`() {
        composeRule.setContactAddScreen()
        composeRule.waitForIdle()

        composeRule.addPhoneNumberRow()

        composeRule.nameInput().assertIsFocused()

        composeRule.removePhoneNumberRow()

        composeRule.nameInput().assertIsFocused()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-002 이름과 설명을 입력하면 화면에 그대로 표시된다`() {
        composeRule.setContactAddScreen()

        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.waitForIdle()

        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-017 키와 신발 사이즈를 입력하면 화면에 그대로 표시된다`() {
        composeRule.setContactAddScreen()

        composeRule.heightInput().performTextInput(TYPED_HEIGHT)
        composeRule.footSizeInput().performTextInput(TYPED_FOOT_SIZE)
        composeRule.waitForIdle()

        composeRule.heightInput().assert(hasText(TYPED_HEIGHT))
        composeRule.footSizeInput().assert(hasText(TYPED_FOOT_SIZE))
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-018 키는 정한 문자와 자리 수만 반영한다`() {
        composeRule.setContactAddScreen()

        listOf(
            "17a5b" to "175",
            "17.5.5" to "17.5",
            "175.55" to "175.5",
            "1755" to "175",
            ".5" to "5",
        ).forEach { (typed, expected) ->
            composeRule.heightInput().performTextClearance()
            composeRule.heightInput().performTextInput(typed)
            composeRule.waitForIdle()

            composeRule.heightInput().assert(hasText(expected))
        }
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-018 신발 사이즈는 정한 문자와 자리 수만 반영한다`() {
        composeRule.setContactAddScreen()

        listOf(
            "2.5a0" to "250",
            "2500" to "250",
        ).forEach { (typed, expected) ->
            composeRule.footSizeInput().performTextClearance()
            composeRule.footSizeInput().performTextInput(typed)
            composeRule.waitForIdle()

            composeRule.footSizeInput().assert(hasText(expected))
        }
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-013 화면에 처음 진입하면 이름 입력에 초점이 있다`() {
        composeRule.setContactAddScreen()

        composeRule.waitForIdle()

        composeRule.nameInput().assertIsFocused()
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-012 화면이 재생성되어도 작성 중이던 내용을 유지한다`() {
        val viewModel = screenTestViewModel()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            ContactAddScreenTestTheme {
                ContactAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { ContactAddScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.heightInput().performTextInput(TYPED_HEIGHT)
        composeRule.footSizeInput().performTextInput(TYPED_FOOT_SIZE)
        composeRule.selectBirthday()
        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput().performTextInput(TYPED_FIRST_PHONE_NUMBER)
        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput(row = 1).performTextInput(TYPED_SECOND_PHONE_NUMBER)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.heightInput().assert(hasText(TYPED_HEIGHT))
        composeRule.footSizeInput().assert(hasText(TYPED_FOOT_SIZE))
        composeRule.onNodeWithText(todayDisplayText()).assertExists()
        composeRule.phoneNumberInput().assert(hasText(TYPED_FIRST_PHONE_NUMBER))
        composeRule.phoneNumberInput(row = 1).assert(hasText(TYPED_SECOND_PHONE_NUMBER))
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-006 단독으로 표시되면 뒤로가기를 선택해 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        composeRule.setContactAddScreen(navigateUp = { navigateUpCount++ })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-005 목록과 함께 표시되면 뒤로가기 버튼이 표시되지 않는다`() {
        composeRule.setContactAddScreen(componentVisible = ContactAddScaffoldComponentVisible(isNavigateUpButtonVisible = false))

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `추가 버튼을 누르면 입력한 내용으로 추가를 한 번 요청한다`() {
        val viewModel = screenTestViewModel()
        composeRule.setContactAddScreen(viewModel = viewModel)

        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.heightInput().performTextInput(TYPED_HEIGHT)
        composeRule.footSizeInput().performTextInput(TYPED_FOOT_SIZE)
        composeRule.hometownInput().performTextInput(TYPED_HOMETOWN)
        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput().performTextInput(TYPED_FIRST_PHONE_NUMBER)
        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput(row = 1).performTextInput(TYPED_SECOND_PHONE_NUMBER)
        composeRule.clickAdd()

        verify(exactly = 1) {
            viewModel.add(
                detail =
                    ContactDetail.EMPTY.copy(
                        name = TYPED_NAME,
                        description = TYPED_DESCRIPTION,
                        height = TYPED_HEIGHT.toDouble().centimeter,
                        footSize = TYPED_FOOT_SIZE.toInt().millimeter,
                        hometown = TYPED_HOMETOWN,
                        phoneNumberList =
                            listOf(
                                ContactPhoneNumber(number = TYPED_FIRST_PHONE_NUMBER),
                                ContactPhoneNumber(number = TYPED_SECOND_PHONE_NUMBER),
                            ),
                    ),
            )
        }
    }

    @Test
    fun `기본 환경에서 상단 바 제목과 입력 이름을 기본 문구로 표시한다`() {
        composeRule.setContactAddScreen()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_NAME_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_HEIGHT_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_FOOT_SIZE_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_HOMETOWN_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.selectBirthday()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CALENDAR_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertExists()
        composeRule.onNodeWithText(DEFAULT_PHONE_NUMBER_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 상단 바 제목과 입력 이름을 한국어 문구로 표시한다`() {
        composeRule.setContactAddScreen()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_NAME_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_HEIGHT_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_FOOT_SIZE_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_BIRTHDAY_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_HOMETOWN_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onNodeWithText(KOREAN_PHONE_NUMBER_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }
}
