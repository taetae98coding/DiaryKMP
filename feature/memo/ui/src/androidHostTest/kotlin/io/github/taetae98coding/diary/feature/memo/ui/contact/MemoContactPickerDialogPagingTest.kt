package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.height
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asPagingSourceFactory
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Before
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

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-b8db3134-e06a-460b-8a52-10df8e8de0b4 목록의 끝으로 이동하면 다음 연락처가 이어서 나타난다`() {
        val contactList = List(PAGED_CONTACT_COUNT) { index -> testContact(name = "PagedContact${index.toString().padStart(length = 3, padChar = '0')}") }
        // 기기에 저장된 연락처를 한 번에 PAGED_CONTACT_PAGE_SIZE씩 나누어 불러오는 목록을 실제 페이지 조회로 구성한다.
        composeRule.setMemoContactPickerDialog(
            contactPagingDataFlow =
                Pager(
                    config =
                        PagingConfig(
                            pageSize = PAGED_CONTACT_PAGE_SIZE,
                            initialLoadSize = PAGED_CONTACT_PAGE_SIZE,
                            prefetchDistance = 1,
                            enablePlaceholders = true,
                        ),
                    pagingSourceFactory = contactList.asPagingSourceFactory(),
                ).flow,
        )
        composeRule.waitUntil(timeoutMillis = PAGED_CONTACT_TIMEOUT_MILLIS) { isContactDisplayed(contactList.first().detail.name) }
        composeRule.contactDialogNodeWithText(contactList.last().detail.name).assertDoesNotExist()

        // 준비되지 않은 자리가 채워지면 목록의 끝이 다시 밀리므로, 사용자가 끝에 닿을 때까지 이어서 내리는 것처럼 끝으로 다시 이동한다.
        composeRule.waitUntil(timeoutMillis = PAGED_CONTACT_TIMEOUT_MILLIS) {
            composeRule.contactPickerList().performScrollToIndex(contactList.lastIndex)
            composeRule.waitForIdle()
            isContactDisplayed(contactList.last().detail.name)
        }

        composeRule.contactDialogNodeWithText(contactList.last().detail.name).assertIsDisplayed()
        composeRule.contactDialogNodeWithText(contactList[contactList.lastIndex - 1].detail.name).assertIsDisplayed()
    }

    @Test
    fun `TC-efe418f3-e965-4a0c-ac33-03eacc2bafad 다음 연락처를 불러오는 동안에도 이미 나타난 연락처를 조작할 수 있다`() {
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
    fun `TC-c13f3adb-1cbe-4dd2-a095-9ca7890096ed 다음 연락처를 불러오지 못해도 이미 나타난 연락처를 유지한다`() {
        val wikiContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val docsContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        composeRule.setMemoContactPickerDialog(contactPagingData = appendFailedContactPagingDataOf(listOf(wikiContact, docsContact)))

        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.contactDialogNodeWithText(SECOND_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-a68a0a0f-53e6-49d9-bf55-e0d70b4b88ce 목록을 처음 불러오는 중에는 목록 자리를 비워 둔다`() {
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

    private fun isContactDisplayed(name: String): Boolean =
        runCatching { composeRule.contactDialogNodeWithText(name).assertIsDisplayed() }
            .isSuccess

    public companion object {
        private const val PREPARED_ROW_TEST_TAG: String = "PreparedContactPickerRow"
        private const val PLACEHOLDER_ROW_TEST_TAG: String = "PlaceholderContactPickerRow"
        private const val PAGED_CONTACT_PAGE_SIZE: Int = 20
        private const val PAGED_CONTACT_COUNT: Int = PAGED_CONTACT_PAGE_SIZE * 3
        private const val PAGED_CONTACT_TIMEOUT_MILLIS: Long = 5_000L
    }
}
