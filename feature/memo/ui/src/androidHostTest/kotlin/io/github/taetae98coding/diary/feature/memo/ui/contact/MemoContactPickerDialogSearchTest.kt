package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoContactPickerDialogSearchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-023 목록을 열면 검색어가 비어 있고 대상 연락처가 모두 나타난다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER), testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER))
        val queryList = mutableListOf<String>()
        composeRule.setMemoContactPickerDialogHost(contactList = contactList, onQueryChange = queryList::add)
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.contactDialogNodeWithText(SECOND_CONTACT_NAME).assertExists()
        queryList shouldBe listOf("")
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-030 목록을 열면 검색어 입력에 초점이 놓인다`() {
        // 터치 모드가 아니면 대화상자가 첫 입력에 스스로 초점을 주므로, 휴대폰과 같은 터치 모드에서 확인한다.
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER), testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER))
        composeRule.setMemoContactPickerDialogHost(contactList = contactList)
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogSearchField().assertIsFocused()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-024 검색어를 입력하면 확정 동작 없이 그 검색어가 즉시 반영된다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME), testContact(name = SECOND_CONTACT_NAME))
        val queryList = mutableListOf<String>()
        composeRule.setMemoContactPickerDialogHost(contactList = contactList, onQueryChange = queryList::add)
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogSearchField().performTextInput(WIKI_CONTACT_QUERY)
        composeRule.waitForIdle()

        queryList.last() shouldBe WIKI_CONTACT_QUERY
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-025 검색어를 지우면 검색어가 없는 상태가 즉시 반영된다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME), testContact(name = SECOND_CONTACT_NAME))
        val queryList = mutableListOf<String>()
        composeRule.setMemoContactPickerDialogHost(contactList = contactList, onQueryChange = queryList::add)
        composeRule.contactDialogSearchField().performTextInput(WIKI_CONTACT_QUERY)
        composeRule.waitForIdle()

        composeRule.contactDialogSearchField().performTextClearance()
        composeRule.waitForIdle()

        queryList.last() shouldBe ""
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-026 검색어로 좁힌 목록에서도 선택을 전달한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoContactPickerDialogHost(contactList = listOf(contact), onContactSelect = selectedIdList::add)
        composeRule.awaitContactPickerRows()
        composeRule.contactDialogSearchField().performTextInput(WIKI_CONTACT_QUERY)
        composeRule.waitForIdle()

        // 검색 입력에도 같은 문자열이 들어 있으므로 목록 항목은 URL로 가려 누른다.
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_PHONE_NUMBER).performClick()

        selectedIdList shouldBe listOf(contact.id)
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-027 검색어에 맞는 연락처가 없으면 결과 없음을 알린다`() {
        composeRule.setMemoContactPickerDialogHost(contactList = emptyList())
        composeRule.contactDialogSearchField().performTextInput(WIKI_CONTACT_QUERY)
        composeRule.waitForIdle()

        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 연락처 선택 목록의 검색 결과 없음 문구를 표시한다`() {
        composeRule.setMemoContactPickerDialogHost(contactList = emptyList())
        composeRule.contactDialogSearchField().performTextInput(WIKI_CONTACT_QUERY)
        composeRule.waitForIdle()

        composeRule.contactDialogNodeWithText(KOREAN_CONTACT_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.contactDialogNodeWithText(KOREAN_CONTACT_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-005 검색어가 비어 있으면 나타낼 연락처가 없어도 결과 없음을 알리지 않는다`() {
        composeRule.setMemoContactPickerDialogHost(contactList = emptyList())
        composeRule.waitForIdle()

        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-028 검색 결과가 없어도 연락처 추가 항목으로 이동을 전달한다`() {
        var contactAddCount = 0
        composeRule.setMemoContactPickerDialogHost(contactList = emptyList(), onContactAdd = { contactAddCount += 1 })
        composeRule.contactDialogSearchField().performTextInput(WIKI_CONTACT_QUERY)
        composeRule.waitForIdle()

        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        contactAddCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-029 목록을 닫았다가 다시 열면 검색어가 비어 있다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME), testContact(name = SECOND_CONTACT_NAME))
        val dialogState = DialogState(isVisible = true)
        val queryList = mutableListOf<String>()
        composeRule.setMemoContactPickerDialogHost(dialogState = dialogState, contactList = contactList, onQueryChange = queryList::add)
        composeRule.contactDialogSearchField().performTextInput(WIKI_CONTACT_QUERY)
        composeRule.waitForIdle()

        composeRule.runOnIdle { dialogState.hide() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { dialogState.show() }
        composeRule.awaitContactPickerRows()

        queryList.last() shouldBe ""
        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.contactDialogNodeWithText(SECOND_CONTACT_NAME).assertExists()
    }

    public companion object {
        private const val WIKI_CONTACT_QUERY = "Wiki"
    }
}
