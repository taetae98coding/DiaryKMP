package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performScrollTo
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactDetailScrollPositionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-f04f8a20-41ad-4db3-9ffe-98d4b0898c5e 처리 중 상태가 되거나 저장 내용이 갱신되어도 보던 화면 위치를 유지한다`() {
        val name = "name-${Arb.string().next()}"
        val updatedName = "updated-name-${Arb.string().next()}"
        val phoneNumberMiddle =
            Arb
                .int()
                .next()
                .mod(PHONE_NUMBER_PART_BOUND)
                .toPhoneNumberPart()
        val detail =
            testContactDetail(
                name = name,
                phoneNumberList = List(PHONE_NUMBER_COUNT) { index -> "010-$phoneNumberMiddle-${index.toPhoneNumberPart()}" },
            )
        val content = ContactDetailUiState.Content(id = Arb.kotlinUuid().next(), detail = detail)
        var uiState: ContactDetailUiState by mutableStateOf(content)
        composeRule.setContent {
            DiaryTheme {
                ContactDetailTestScaffold(
                    onEvent = {},
                    state = rememberContactDetailFormState(initialDetail = detail),
                    uiStateProvider = { uiState },
                )
            }
        }
        composeRule.onNodeWithContentDescription(LAST_NUMBER_DESCRIPTION).performScrollTo()
        composeRule.waitForIdle()
        assertLastPhoneNumberIsShown(name = name)

        val changeList: List<ContactDetailUiState> =
            listOf(
                content.copy(isUpdateInProgress = true),
                content.copy(isFavoriteInProgress = true),
                content.copy(isDeleteInProgress = true),
                content.copy(detail = detail.copy(name = updatedName), isFavorite = true),
            )

        changeList.forEach { changed ->
            composeRule.runOnIdle { uiState = changed }
            composeRule.waitForIdle()

            assertLastPhoneNumberIsShown(name = name)
        }
    }

    private fun assertLastPhoneNumberIsShown(name: String) {
        composeRule.onNodeWithContentDescription(LAST_NUMBER_DESCRIPTION).assertIsDisplayed()
        composeRule.onNode(hasText(name) and hasSetTextAction()).assertIsNotDisplayed()
    }

    private fun Int.toPhoneNumberPart(): String = toString().padStart(length = PHONE_NUMBER_PART_LENGTH, padChar = '0')

    private companion object {
        private const val PHONE_NUMBER_COUNT = 30
        private const val PHONE_NUMBER_PART_LENGTH = 4
        private const val PHONE_NUMBER_PART_BOUND = 10_000
        private const val LAST_NUMBER_DESCRIPTION = "Number 30"
    }
}
