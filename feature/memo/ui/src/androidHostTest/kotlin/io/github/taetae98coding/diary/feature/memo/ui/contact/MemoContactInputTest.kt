package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoContactInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-011 선택한 연락처를 제목 칩으로 표시한다`() {
        val wikiContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val docsContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)

        composeRule.setMemoContactInput(uiState = MemoContactInputUiState(selectedContactList = listOf(wikiContact, docsContact)))

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assertExists()
    }

    @Test
    fun `칩에는 제목만 표시하고 URL은 표시하지 않는다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)

        composeRule.setMemoContactInput(uiState = MemoContactInputUiState(selectedContactList = listOf(contact)))

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(FIRST_CONTACT_PHONE_NUMBER).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-015 선택한 연락처의 이름이 바뀌면 연락처 칩에 반영된다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val renamedContact = contact.copy(detail = contact.detail.copy(name = RENAMED_CONTACT_TITLE))
        val selectContactList = composeRule.setMemoContactInputWithSelection()

        selectContactList(listOf(contact))
        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()

        selectContactList(listOf(renamedContact))

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertDoesNotExist()
        composeRule.onNodeWithText(RENAMED_CONTACT_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-012 선택을 해제한 연락처는 칩 영역에서 사라진다`() {
        val wikiContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val docsContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        val selectContactList = composeRule.setMemoContactInputWithSelection()

        selectContactList(listOf(wikiContact, docsContact))
        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assertExists()

        selectContactList(listOf(wikiContact))

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assertDoesNotExist()
    }

    @Test
    fun `칩이 칩 영역보다 많으면 연락처 입력이 그만큼 높아진다`() {
        val contactList = List(size = SCROLL_CONTACT_COUNT) { index -> testContact(name = "$SCROLL_CONTACT_NAME_PREFIX$index") }
        val selectContactList = composeRule.setMemoContactInputWithSelection()
        val emptyHeight = composeRule.memoContactInputHeight()

        selectContactList(contactList)

        composeRule.onNodeWithText(contactList.last().detail.name).assertExists()
        composeRule.memoContactInputHeight() shouldBeGreaterThan emptyHeight
    }

    @Test
    fun `칩이 많아도 칩 영역은 세로 스크롤을 가져가지 않는다`() {
        val contactList = List(size = SCROLL_CONTACT_COUNT) { index -> testContact(name = "$SCROLL_CONTACT_NAME_PREFIX$index") }

        composeRule.setMemoContactInput(uiState = MemoContactInputUiState(selectedContactList = contactList))

        composeRule.onNode(hasScrollAction()).assertDoesNotExist()
    }

    @Test
    fun `연락처를 선택해 칩이 칩 영역 안에 들어가면 연락처 입력의 높이가 바뀌지 않는다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME), testContact(name = SECOND_CONTACT_NAME))
        val selectContactList = composeRule.setMemoContactInputWithSelection()
        val emptyHeight = composeRule.memoContactInputHeight()

        selectContactList(contactList)

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assertExists()
        composeRule.memoContactInputHeight() shouldBe emptyHeight
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-017 연락처 칩을 눌러도 추가 항목의 동작이 실행되지 않는다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        var addClickCount = 0
        composeRule.setMemoContactInput(
            uiState = MemoContactInputUiState(selectedContactList = listOf(contact)),
            onAddClick = { addClickCount += 1 },
        )

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).performClick()

        addClickCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_CONTACT_PICKER_TITLE).assertDoesNotExist()
    }

    public companion object {
        private const val SCROLL_CONTACT_NAME_PREFIX = "MemoContactScroll"
        private const val SCROLL_CONTACT_COUNT = 30
        private const val RENAMED_CONTACT_TITLE = "MemoContactRenamed"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoContactInputAddChipTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 연락처 입력의 문구를 표시한다`() {
        composeRule.setMemoContactInput(uiState = MemoContactInputUiState())

        composeRule.onNodeWithText(KOREAN_CONTACT_SELECT_LABEL).assert(hasContactClickLabel(KOREAN_CONTACT_SELECT_LABEL))
    }

    @Test
    fun `기본 환경에서 연락처 입력의 문구를 표시한다`() {
        composeRule.setMemoContactInput(uiState = MemoContactInputUiState())

        composeRule.onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).assert(hasContactClickLabel(DEFAULT_CONTACT_SELECT_LABEL))
    }

    @Test
    fun `추가 항목을 누르면 추가 항목의 동작이 한 번 실행된다`() {
        var addClickCount = 0
        composeRule.setMemoContactInput(onAddClick = { addClickCount += 1 })

        composeRule.onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).performClick()

        addClickCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-002 선택한 연락처가 없거나 여러 개여도 연락처 추가 항목이 표시된다`() {
        val contactList = listOf(testContact(name = FIRST_CONTACT_NAME), testContact(name = SECOND_CONTACT_NAME))
        val selectContactList = composeRule.setMemoContactInputWithSelection()

        listOf(
            emptyList(),
            contactList,
        ).forEach { selectedContactList ->
            selectContactList(selectedContactList)

            composeRule.onNodeWithText(DEFAULT_CONTACT_SELECT_LABEL).assertExists()
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoContactInputNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-016 연락처 칩을 누르면 그 연락처를 대상으로 상세 이동을 요청한다`() {
        val wikiContact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)
        val docsContact = testContact(name = SECOND_CONTACT_NAME, phoneNumber = SECOND_CONTACT_PHONE_NUMBER)
        val clickedContactIdList = mutableListOf<Uuid>()

        composeRule.setMemoContactInput(
            uiState = MemoContactInputUiState(selectedContactList = listOf(wikiContact, docsContact)),
            onContactClick = { id -> clickedContactIdList += id },
        )

        composeRule.onNodeWithText(SECOND_CONTACT_NAME).assert(hasContactClickLabel(DEFAULT_CONTACT_DETAIL_ACTION))
        composeRule.onNodeWithText(SECOND_CONTACT_NAME).performClick()
        composeRule.onNodeWithText(FIRST_CONTACT_NAME).performClick()

        clickedContactIdList shouldBe listOf(docsContact.id, wikiContact.id)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-CONTACT-INPUT-FEATURE-016 한국어 환경에서 연락처 칩에 상세 이동 동작 이름을 제공한다`() {
        val contact = testContact(name = FIRST_CONTACT_NAME, phoneNumber = FIRST_CONTACT_PHONE_NUMBER)

        composeRule.setMemoContactInput(uiState = MemoContactInputUiState(selectedContactList = listOf(contact)))

        composeRule.onNodeWithText(FIRST_CONTACT_NAME).assert(hasContactClickLabel(KOREAN_CONTACT_DETAIL_ACTION))
    }
}
