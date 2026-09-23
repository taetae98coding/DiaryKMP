package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoContactPickerDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-001 목록에 연락처의 제목과 URL, 선택 여부를 함께 표시한다`() {
        val selectedContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val unselectedContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        composeRule.setMemoContactPickerDialog(
            contactList = listOf(selectedContact, unselectedContact),
            uiState = MemoContactInputUiState(selectedContactList = listOf(selectedContact)),
        )
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_PHONE_NUMBER).assertExists()
        composeRule.contactDialogNodeWithText(SECOND_CONTACT_NAME).assertExists()
        composeRule.contactDialogNodeWithText(SECOND_CONTACT_PHONE_NUMBER).assertExists()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).onFirst().assertIsOn()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).onLast().assertIsOff()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-011 목록에서 연락처를 선택하면 선택 행동을 즉시 전달한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val selectedIdList = mutableListOf<Uuid>()
        val unselectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoContactPickerDialog(
            contactList = listOf(contact),
            onContactSelect = selectedIdList::add,
            onContactUnselect = unselectedIdList::add,
        )
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).performClick()

        selectedIdList shouldBe listOf(contact.id)
        unselectedIdList shouldBe emptyList()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-012 목록에서 선택을 해제하면 해제 행동을 즉시 전달한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val selectedIdList = mutableListOf<Uuid>()
        val unselectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoContactPickerDialog(
            contactList = listOf(contact),
            uiState = MemoContactInputUiState(selectedContactList = listOf(contact)),
            onContactSelect = selectedIdList::add,
            onContactUnselect = unselectedIdList::add,
        )
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).performClick()

        unselectedIdList shouldBe listOf(contact.id)
        selectedIdList shouldBe emptyList()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-DOMAIN-002 목록의 연락처는 전달된 이름 오름차순으로 표시된다`() {
        val firstContact = testContact(name = APPLE_CONTACT_TITLE, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val lastContact = testContact(name = ZEBRA_CONTACT_TITLE, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        composeRule.setMemoContactPickerDialog(contactList = listOf(firstContact, lastContact))
        composeRule.awaitContactPickerRows()

        val firstTop = composeRule.contactDialogNodeWithText(APPLE_CONTACT_TITLE).getUnclippedBoundsInRoot().top
        val lastTop = composeRule.contactDialogNodeWithText(ZEBRA_CONTACT_TITLE).getUnclippedBoundsInRoot().top

        firstTop shouldBeLessThan lastTop
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-022 목록의 연락처를 눌러도 상세 이동을 요청하지 않는다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoContactPickerDialog(contactList = listOf(contact), onContactSelect = selectedIdList::add)
        composeRule.awaitContactPickerRows()

        // 목록 항목에는 상세 이동 동작 이름이 없고 선택만 전달한다.
        composeRule
            .onAllNodes(hasContactClickLabel(DEFAULT_CONTACT_DETAIL_ACTION) and hasAnyAncestor(isDialog()))
            .assertCountEquals(0)
        composeRule.contactDialogNodeWithText(FIRST_CONTACT_NAME).performClick()

        selectedIdList shouldBe listOf(contact.id)
    }

    @Test
    fun `목록에 확인 버튼을 두지 않는다`() {
        composeRule.setMemoContactPickerDialog(contactList = listOf(testContact(name = FIRST_CONTACT_NAME)))
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(DEFAULT_CONFIRM_LABEL).assertDoesNotExist()
    }

    @Test
    fun `기본 환경에서 연락처 선택 목록의 문구를 표시한다`() {
        composeRule.setMemoContactPickerDialog()

        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertExists()
        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_ADD_LABEL).assertExists()
        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 연락처 선택 목록의 문구를 표시한다`() {
        composeRule.setMemoContactPickerDialog()

        composeRule.contactDialogNodeWithText(KOREAN_CONTACT_PICKER_TITLE).assertExists()
        composeRule.contactDialogNodeWithText(KOREAN_CONTACT_PICKER_ADD_LABEL).assertExists()
        composeRule.contactDialogNodeWithText(KOREAN_CONTACT_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    public companion object {
        private const val APPLE_CONTACT_TITLE = "AppleContact"
        private const val ZEBRA_CONTACT_TITLE = "ZebraContact"
        private const val DEFAULT_CONFIRM_LABEL = "Confirm"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoContactPickerDialogAddTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-006 나타낼 연락처가 없어도 연락처 추가 항목을 표시한다`() {
        composeRule.setMemoContactPickerDialog(contactList = emptyList())

        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_ADD_LABEL).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-006 나타낼 연락처가 여러 개여도 연락처 추가 항목을 표시한다`() {
        composeRule.setMemoContactPickerDialog(contactList = listOf(testContact(name = FIRST_CONTACT_NAME), testContact(name = SECOND_CONTACT_NAME)))
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_ADD_LABEL).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-007 연락처 추가 항목을 누르면 연락처 추가 행동만 전달한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME)
        var contactAddCount = 0
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoContactPickerDialog(
            contactList = listOf(contact),
            onContactSelect = selectedIdList::add,
            onContactAdd = { contactAddCount += 1 },
        )
        composeRule.awaitContactPickerRows()

        composeRule.contactDialogNodeWithText(DEFAULT_CONTACT_PICKER_ADD_LABEL).performClick()

        contactAddCount shouldBe 1
        selectedIdList shouldBe emptyList()
    }

    @Test
    fun `연락처 추가 항목에 연락처 추가 동작 이름을 제공한다`() {
        composeRule.setMemoContactPickerDialog()

        composeRule
            .onAllNodes(hasContactClickLabel(DEFAULT_CONTACT_PICKER_ADD_LABEL) and hasAnyAncestor(isDialog()))
            .assertCountEquals(1)
    }
}
