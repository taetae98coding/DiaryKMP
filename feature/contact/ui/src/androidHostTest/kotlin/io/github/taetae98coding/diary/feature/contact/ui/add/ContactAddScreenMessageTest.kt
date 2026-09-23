package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
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
}
