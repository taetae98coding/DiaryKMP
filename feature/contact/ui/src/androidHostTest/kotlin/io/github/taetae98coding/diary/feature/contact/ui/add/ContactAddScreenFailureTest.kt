package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.domain.contact.usecase.AddContactUseCase
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ContactAddScreenFailureTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-ADD-FEATURE-030 저장에 실패하면 안내 없이 작성 내용을 유지하고 같은 내용으로 다시 추가할 수 있다`() {
        val useCase = mockk<AddContactUseCase>()
        val parameterList = mutableListOf<AddContactUseCase.Parameter>()
        coEvery { useCase(capture(parameterList)) } returns Result.failure(IllegalStateException("저장 실패"))
        composeRule.setContactAddScreen(viewModel = ContactAddViewModel(addContactUseCase = useCase))
        composeRule.nameInput().performTextInput(TYPED_NAME)
        composeRule.descriptionInput().performTextInput(TYPED_DESCRIPTION)
        composeRule.addPhoneNumberRow()
        composeRule.phoneNumberInput().performTextInput(TYPED_FIRST_PHONE_NUMBER)
        composeRule.waitForIdle()

        composeRule.clickAdd()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_NAME_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_PHONE_NUMBER_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.nameInput().assert(hasText(TYPED_NAME))
        composeRule.descriptionInput().assert(hasText(TYPED_DESCRIPTION))
        composeRule.phoneNumberRowCount() shouldBe 1
        composeRule.phoneNumberInput().assert(hasText(TYPED_FIRST_PHONE_NUMBER))

        composeRule.clickAdd()

        parameterList.size shouldBe 2
        parameterList[1] shouldBe parameterList[0]
        parameterList[0].detail.name shouldBe TYPED_NAME
        parameterList[0].detail.description shouldBe TYPED_DESCRIPTION
        parameterList[0].detail.phoneNumberList shouldBe listOf(ContactPhoneNumber(number = TYPED_FIRST_PHONE_NUMBER))
    }
}
