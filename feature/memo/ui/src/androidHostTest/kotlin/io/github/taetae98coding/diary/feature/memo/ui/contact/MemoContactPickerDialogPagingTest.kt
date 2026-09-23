package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.height
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoContactPickerDialogPagingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-019 다음 연락처를 불러오는 동안에도 이미 나타난 연락처를 조작할 수 있다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoContactPickerDialog(
            contactPagingData = appendingContactPagingDataOf(listOf(contact)),
            onContactSelect = selectedIdList::add,
        )

        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).performClick()
        composeRule.waitForIdle()

        selectedIdList shouldBe listOf(contact.id)
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-020 다음 연락처를 불러오지 못해도 이미 나타난 연락처를 유지한다`() {
        val wikiContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val docsContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        composeRule.setMemoContactPickerDialog(contactPagingData = appendFailedContactPagingDataOf(listOf(wikiContact, docsContact)))

        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.contactDialogNodeWithText(SECOND_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-005 목록을 처음 불러오는 중에는 목록 자리를 비워 둔다`() {
        composeRule.setMemoContactPickerDialog(contactPagingData = refreshingContactPagingData())

        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertExists()
        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
        composeRule
            .contactPickerList()
            .fetchSemanticsNode()
            .children
            .shouldBeEmpty()
    }

    @Test
    fun `준비되지 않은 자리는 준비된 항목과 같은 높이의 빈 항목으로 표시한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val unselectedIdList = mutableListOf<Uuid>()

        composeRule.setContent {
            DiaryTheme {
                Column {
                    MemoContactPickerRow(
                        onEvent = { event -> if (event is MemoContactPickerEvent.Unselect) unselectedIdList += event.id },
                        contact = contact,
                        isSelected = true,
                        modifier = Modifier.testTag(PREPARED_ROW_TEST_TAG),
                    )
                    MemoContactPickerRow(
                        onEvent = { event -> if (event is MemoContactPickerEvent.Unselect) unselectedIdList += event.id },
                        contact = null,
                        isSelected = false,
                        modifier = Modifier.testTag(PLACEHOLDER_ROW_TEST_TAG),
                    )
                }
            }
        }

        val placeholder = composeRule.onNodeWithTag(PLACEHOLDER_ROW_TEST_TAG)
        placeholder.performClick()
        composeRule.waitForIdle()

        placeholder.getUnclippedBoundsInRoot().height shouldBe
            composeRule
                .onNodeWithTag(PREPARED_ROW_TEST_TAG)
                .getUnclippedBoundsInRoot()
                .height
        unselectedIdList.shouldBeEmpty()
    }

    public companion object {
        private const val PREPARED_ROW_TEST_TAG: String = "PreparedContactPickerRow"
        private const val PLACEHOLDER_ROW_TEST_TAG: String = "PlaceholderContactPickerRow"
    }
}
