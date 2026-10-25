package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddScreenEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-7971c46e-b7a3-4195-8b16-1abc5db887bc 추가에 성공하면 다음 연락처를 작성할 수 있는 상태로 초기화한다`() {
        composeRule.setContactAddScreen(viewModel = effectViewModel(effect = ContactAddEffect.AddSucceeded(id = Uuid.random())))
        composeRule.fillAllInput()

        composeRule.clickAdd()

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
    fun `TC-ad43183b-4204-40e6-8cec-8aff309a9daf 추가에 성공하면 이름 입력으로 초점을 옮긴다`() {
        composeRule.setContactAddScreen(viewModel = effectViewModel(effect = ContactAddEffect.AddSucceeded(id = Uuid.random())))
        composeRule.fillAllInput()
        composeRule.heightInput().assertIsFocused()

        composeRule.clickAdd()

        composeRule.nameInput().assertIsFocused()
    }

    @Test
    fun `TC-c789e3c9-b8be-473b-9c75-18d96ab63594 이름 미입력 안내 뒤에도 입력 중이던 내용을 유지한다`() {
        composeRule.setContactAddScreen(viewModel = effectViewModel(effect = ContactAddEffect.NameBlank))
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

        composeRule.clickAdd()

        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.heightInput().assert(hasText(TYPED_HEIGHT))
        composeRule.footSizeInput().assert(hasText(TYPED_FOOT_SIZE))
        composeRule.hometownInput().assert(hasText(TYPED_HOMETOWN))
        composeRule.onNodeWithText(todayDisplayText()).assertExists()
        composeRule.birthYearInput().assert(hasText(today().year.toString()))
        composeRule.phoneNumberRowCount() shouldBe 2
        composeRule.phoneNumberInput().assert(hasText(TYPED_FIRST_PHONE_NUMBER))
        composeRule.phoneNumberInput(row = 1).assert(hasText(TYPED_SECOND_PHONE_NUMBER))
    }

    @Test
    fun `TC-a56601a2-8685-4fa2-9662-4251a7386a77 전화번호 미입력 안내 뒤에도 입력 중이던 내용을 유지한다`() {
        composeRule.setContactAddScreen(viewModel = effectViewModel(effect = ContactAddEffect.PhoneNumberBlank))
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput().performTextInput(TYPED_FIRST_PHONE_NUMBER)
        composeRule.addPhoneNumberRow()
        composeRule.waitForIdle()

        composeRule.clickAdd()

        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.phoneNumberRowCount() shouldBe 2
        composeRule.phoneNumberInput().assert(hasText(TYPED_FIRST_PHONE_NUMBER))
        composeRule.phoneNumberInput(row = 1).assert(hasText(""))
    }

    @Test
    fun `TC-92b2747c-ade0-4564-b49a-d1f36081ad7f 전화번호 미입력으로 추가하면 보고 있던 입력에 초점을 그대로 둔다`() {
        composeRule.setContactAddScreen(viewModel = effectViewModel(effect = ContactAddEffect.PhoneNumberBlank))
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.addPhoneNumberRow()
        composeRule.heightInput().performTextInput(TYPED_HEIGHT)
        composeRule.heightInput().assertIsFocused()

        composeRule.clickAdd()

        composeRule.heightInput().assertIsFocused()
    }

    @Test
    fun `TC-92b2747c-ade0-4564-b49a-d1f36081ad7f 이름 미입력으로 추가하면 이름 입력으로 초점을 옮긴다`() {
        composeRule.setContactAddScreen(viewModel = effectViewModel(effect = ContactAddEffect.NameBlank))
        composeRule.heightInput().performTextInput(TYPED_HEIGHT)
        composeRule.heightInput().assertIsFocused()

        composeRule.clickAdd()

        composeRule.nameInput().assertIsFocused()
    }
}
