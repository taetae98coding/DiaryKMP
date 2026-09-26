package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddScreenMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-ADD-FEATURE-007 기본 환경 추가 성공 안내`() {
        assertMessage(effect = ContactAddEffect.AddSucceeded(id = Uuid.random()), expectedMessage = DEFAULT_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-ADD-FEATURE-007 한국어 추가 성공 안내`() {
        assertMessage(effect = ContactAddEffect.AddSucceeded(id = Uuid.random()), expectedMessage = KOREAN_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-009 기본 환경 이름 미입력 안내`() {
        assertMessage(effect = ContactAddEffect.NameBlank, expectedMessage = DEFAULT_NAME_BLANK_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-ADD-FEATURE-009 한국어 이름 미입력 안내`() {
        assertMessage(effect = ContactAddEffect.NameBlank, expectedMessage = KOREAN_NAME_BLANK_MESSAGE)
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-009 기본 환경 전화번호 미입력 안내`() {
        assertMessage(effect = ContactAddEffect.PhoneNumberBlank, expectedMessage = DEFAULT_PHONE_NUMBER_BLANK_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-ADD-FEATURE-009 한국어 전화번호 미입력 안내`() {
        assertMessage(effect = ContactAddEffect.PhoneNumberBlank, expectedMessage = KOREAN_PHONE_NUMBER_BLANK_MESSAGE)
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-009 이름이 비어 있으면 그 입력으로 추가하고 이름 입력이 필요함을 알린다`() {
        assertBlankInput(effect = ContactAddEffect.NameBlank, expectedMessage = DEFAULT_NAME_BLANK_MESSAGE, name = "", phoneNumber = null)
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-009 이름이 공백 문자로만 이루어지면 그 입력으로 추가하고 이름 입력이 필요함을 알린다`() {
        assertBlankInput(effect = ContactAddEffect.NameBlank, expectedMessage = DEFAULT_NAME_BLANK_MESSAGE, name = BLANK_TEXT, phoneNumber = null)
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-009 번호가 비어 있는 전화번호 항목이 있으면 그 입력으로 추가하고 전화번호 입력이 필요함을 알린다`() {
        assertBlankInput(effect = ContactAddEffect.PhoneNumberBlank, expectedMessage = DEFAULT_PHONE_NUMBER_BLANK_MESSAGE, name = TYPED_NAME, phoneNumber = "")
    }

    @Test
    fun `TC-CONTACT-ADD-FEATURE-009 번호가 공백 문자로만 이루어진 전화번호 항목이 있으면 그 입력으로 추가하고 전화번호 입력이 필요함을 알린다`() {
        assertBlankInput(effect = ContactAddEffect.PhoneNumberBlank, expectedMessage = DEFAULT_PHONE_NUMBER_BLANK_MESSAGE, name = TYPED_NAME, phoneNumber = BLANK_TEXT)
    }

    private fun assertBlankInput(
        effect: ContactAddEffect,
        expectedMessage: String,
        name: String,
        phoneNumber: String?,
    ) {
        val viewModel = effectViewModel(effect = effect)
        composeRule.setContactAddScreen(viewModel = viewModel)
        composeRule.nameInput().performTextInput(name)
        if (phoneNumber != null) {
            composeRule.addPhoneNumberRow()
            composeRule.phoneNumberInput().performTextInput(phoneNumber)
        }
        composeRule.waitForIdle()

        composeRule.clickAdd()

        verify(exactly = 1) {
            viewModel.add(
                detail =
                    match { detail ->
                        detail.name == name && detail.phoneNumberList.map { value -> value.number } == listOfNotNull(phoneNumber)
                    },
            )
        }
        composeRule.onNodeWithText(expectedMessage).assertExists()
    }

    private fun assertMessage(
        effect: ContactAddEffect,
        expectedMessage: String,
    ) {
        composeRule.setContactAddScreen(viewModel = effectViewModel(effect = effect))

        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.nameInput().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(expectedMessage).assertExists()
    }

    private companion object {
        private const val BLANK_TEXT = "   "
    }
}
