package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
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
    fun `TC-97700e58-f5e2-4c28-a6ae-d18532c6e667 화면에 처음 진입하면 입력이 모두 비어 있다`() {
        composeRule.setContactAddScreen()

        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_PHONE_NUMBER
        composeRule.nameInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.heightInput().assert(hasText(""))
        composeRule.footSizeInput().assert(hasText(""))
        composeRule.birthYearInput().assert(hasText(""))
        composeRule.hometownInput().assert(hasText(""))
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.phoneNumberRowCount() shouldBe 0
    }

    @Test
    fun `TC-30c657f6-23a1-4194-a3b5-9a5e1a444b24 전화번호 항목을 추가해 번호를 입력할 수 있다`() {
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
    fun `TC-46b81d89-66f5-40b9-841a-91dbc2ff4d37 전화번호 항목을 삭제해도 다른 입력은 유지된다`() {
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
    fun `TC-a9e9700d-1916-4c96-9ae5-bbc8f8852b65 전화번호 항목을 추가하거나 삭제해도 초점을 옮기지 않는다`() {
        composeRule.setContactAddScreen()
        composeRule.waitForIdle()

        composeRule.addPhoneNumberRow()

        composeRule.nameInput().assertIsFocused()

        composeRule.removePhoneNumberRow()

        composeRule.nameInput().assertIsFocused()
    }

    @Test
    fun `TC-da306e19-71a0-4dea-868a-6cd140d12098 이름과 설명을 입력하면 화면에 그대로 표시된다`() {
        composeRule.setContactAddScreen()

        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.waitForIdle()

        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
    }

    @Test
    fun `TC-89ff8c09-ddaa-42df-b34d-30c38bb0f617 키와 신발 사이즈를 입력하면 화면에 그대로 표시된다`() {
        composeRule.setContactAddScreen()

        composeRule.heightInput().performTextInput(TYPED_HEIGHT)
        composeRule.footSizeInput().performTextInput(TYPED_FOOT_SIZE)
        composeRule.waitForIdle()

        composeRule.heightInput().assert(hasText(TYPED_HEIGHT))
        composeRule.footSizeInput().assert(hasText(TYPED_FOOT_SIZE))
    }

    @Test
    fun `TC-46eea2ad-c8f1-4378-8e3c-2ac6ab2258aa 키는 정한 문자와 자리 수만 반영한다`() {
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
    fun `TC-46eea2ad-c8f1-4378-8e3c-2ac6ab2258aa 신발 사이즈는 정한 문자와 자리 수만 반영한다`() {
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
    fun `TC-b85bd3cf-7226-41eb-90bc-8fbf34fda9a6 키는 전각 숫자를 반영하지 않는다`() {
        composeRule.setContactAddScreen()

        listOf(
            "１７５" to "",
            "17５.５" to "17.",
        ).forEach { (typed, expected) ->
            composeRule.heightInput().performTextClearance()
            composeRule.heightInput().performTextInput(typed)
            composeRule.waitForIdle()

            composeRule.heightInput().assert(hasText(expected))
        }
    }

    @Test
    fun `TC-b85bd3cf-7226-41eb-90bc-8fbf34fda9a6 신발 사이즈는 전각 숫자를 반영하지 않는다`() {
        composeRule.setContactAddScreen()

        listOf(
            "２５０" to "",
            "2５0" to "20",
        ).forEach { (typed, expected) ->
            composeRule.footSizeInput().performTextClearance()
            composeRule.footSizeInput().performTextInput(typed)
            composeRule.waitForIdle()

            composeRule.footSizeInput().assert(hasText(expected))
        }
    }

    @Test
    fun `TC-7b52f468-38b9-4a5c-a74e-8a36822d977a 키와 신발 사이즈는 입력한 값을 한 번에 지울 수 있다`() {
        composeRule.setContactAddScreen()

        listOf(
            composeRule.heightInput() to TYPED_HEIGHT,
            composeRule.footSizeInput() to TYPED_FOOT_SIZE,
        ).forEach { (input, typed) ->
            input.performTextInput(typed)
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_TEXT_DESCRIPTION).performClick()
            composeRule.waitForIdle()

            input.assert(hasText(""))
        }
    }

    @Test
    fun `TC-aa75ff27-c43c-431f-9693-9763c27407fa 화면에 처음 진입하면 이름 입력에 초점이 있다`() {
        composeRule.setContactAddScreen()

        composeRule.waitForIdle()

        composeRule.nameInput().assertIsFocused()
    }

    @Test
    fun `TC-37880f30-92d8-4d3f-976d-ae42f058cedc 화면이 재생성되어도 작성 중이던 내용을 유지한다`() {
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
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.heightInput().performTextInput(TYPED_HEIGHT)
        composeRule.footSizeInput().performTextInput(TYPED_FOOT_SIZE)
        composeRule.hometownInput().performTextInput(TYPED_HOMETOWN)
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
        composeRule.hometownInput().assert(hasText(TYPED_HOMETOWN))
        composeRule.onNodeWithText(todayDisplayText()).assertExists()
        composeRule.birthYearInput().assert(hasText(today().year.toString()))
        composeRule.phoneNumberInput().assert(hasText(TYPED_FIRST_PHONE_NUMBER))
        composeRule.phoneNumberInput(row = 1).assert(hasText(TYPED_SECOND_PHONE_NUMBER))
    }

    @Test
    fun `TC-d24b4c59-b6cc-44f5-a0d4-7101e84629e8 생일 없이 입력한 태어난 연도는 화면이 재생성되어도 유지된다`() {
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
        composeRule.birthYearInput().performTextInput(TYPED_BIRTH_YEAR)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.birthYearInput().assert(hasText(TYPED_BIRTH_YEAR))
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
    }

    @Test
    fun `TC-5b8bf97d-e565-481e-b0d4-fca092be9332 메모리 정리 뒤 복원해도 작성 중이던 내용을 모두 복원한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 화면 상태를 들고 있던 객체도 새로 만들어지므로 복원할 때마다 새 인스턴스를 쓴다.
            val viewModel = remember { screenTestViewModel() }

            ContactAddScreenTestTheme {
                ContactAddScreen(
                    addedResultRequestKey = null,
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
        composeRule.hometownInput().performTextInput(TYPED_HOMETOWN)
        composeRule.selectBirthday()
        composeRule.selectBirthdayCalendar(DEFAULT_BIRTHDAY_CALENDAR_LUNAR)
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
        composeRule.hometownInput().assert(hasText(TYPED_HOMETOWN))
        composeRule.onNodeWithText(todayDisplayText()).assertExists()
        composeRule.birthYearInput().assert(hasText(today().year.toString()))
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertIsNotSelected()
        composeRule.phoneNumberRowCount() shouldBe 2
        composeRule.phoneNumberInput().assert(hasText(TYPED_FIRST_PHONE_NUMBER))
        composeRule.phoneNumberInput(row = 1).assert(hasText(TYPED_SECOND_PHONE_NUMBER))
    }

    @Test
    fun `TC-aa4cea53-888d-47d2-9a1b-77e59c0f00fb TC-1a9ee424-8630-4ff9-98dc-606b97c57280 단독으로 표시되면 뒤로가기를 선택해 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        composeRule.setContactAddScreen(navigateUp = { navigateUpCount++ })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-0210acce-6535-4bb7-bba2-abcda27d289e 목록과 함께 표시되면 뒤로가기 버튼이 표시되지 않는다`() {
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
        composeRule.birthYearInput().performTextInput(TYPED_BIRTH_YEAR)
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
                        birthYear = TYPED_BIRTH_YEAR.toInt(),
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
        composeRule.onNodeWithText(DEFAULT_BIRTH_YEAR_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_HOMETOWN_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_NOT_SET).assertExists()
        composeRule.selectBirthday()
        composeRule.onNodeWithContentDescription(DEFAULT_BIRTHDAY_CALENDAR_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_SOLAR).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTHDAY_CALENDAR_LUNAR).assertExists()
        composeRule.onNodeWithText(DEFAULT_BIRTH_YEAR_FROM_BIRTHDAY).assertExists()
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
        composeRule.onNodeWithText(KOREAN_BIRTH_YEAR_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_HOMETOWN_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_BIRTHDAY_NOT_SET).assertExists()
        composeRule.onNodeWithText(KOREAN_PHONE_NUMBER_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    private companion object {
        private const val DEFAULT_CLEAR_TEXT_DESCRIPTION = "Clear text"
    }
}
