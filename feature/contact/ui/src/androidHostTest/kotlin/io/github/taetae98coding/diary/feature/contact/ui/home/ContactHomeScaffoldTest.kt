package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContactHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-HOME-FEATURE-006 표시할 연락처가 없으면 빈 상태 안내를 표시한다`() {
        setContactHomeScaffold()

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-HOME-FEATURE-006 한국어 환경에서 빈 상태 안내는 아직 연락처가 없습니다이다`() {
        setContactHomeScaffold()

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-009 목록이 비어 있어도 연락처 추가를 선택할 수 있다`() {
        val eventList = mutableListOf<ContactHomeScaffoldEvent>()
        setContactHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(ContactHomeScaffoldEvent.ClickAdd)
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-014 뒤로가기를 선택하면 돌아가기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<ContactHomeScaffoldEvent>()
        setContactHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(ContactHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-003 상세 영역에 연락처 추가가 놓이면 연락처 추가 버튼이 표시되지 않는다`() {
        setContactHomeScaffold(componentVisible = ContactHomeScaffoldComponentVisible(isAddButtonVisible = false))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-004 추가 버튼 표시 상태이면 연락처 추가 버튼이 표시된다`() {
        setContactHomeScaffold(componentVisible = ContactHomeScaffoldComponentVisible(isAddButtonVisible = true))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-007 상세 영역에 연락처 추가가 놓여도 정렬 컨트롤은 표시된다`() {
        setContactHomeScaffold(
            contactList = listOf(testContact(name = CONTACT_NAME)),
            componentVisible = ContactHomeScaffoldComponentVisible(isAddButtonVisible = false),
        )
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(CONTACT_NAME).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 환경에서 상단 바 제목은 Contacts이고 추가 버튼 이름은 Add contact이다`() {
        setContactHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 상단 바 제목은 연락처가고 추가 버튼 이름은 연락처 추가이다`() {
        setContactHomeScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `상단 바에는 검색 버튼을 두지 않는다`() {
        setContactHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    private fun setContactHomeScaffold(
        contactList: List<Contact> = emptyList(),
        onEvent: (ContactHomeScaffoldEvent) -> Unit = {},
        componentVisible: ContactHomeScaffoldComponentVisible = ContactHomeScaffoldComponentVisible(),
    ) {
        val contactPagingDataFlow = MutableStateFlow(contactPagingDataOf(contactList))

        composeRule.setContent {
            DiaryTheme {
                ContactHomeScaffold(
                    onEvent = onEvent,
                    contactPagingItems = contactPagingDataFlow.collectAsLazyPagingItems(),
                    componentVisibleProvider = { componentVisible },
                )
            }
        }
    }

    private companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val CONTACT_NAME = "ContactHomeScaffoldName"
        private const val DEFAULT_TITLE = "Contacts"
        private const val KOREAN_TITLE = "연락처"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add contact"
        private const val KOREAN_ADD_BUTTON_DESCRIPTION = "연락처 추가"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val DEFAULT_EMPTY_TITLE = "No contacts yet"
        private const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a contact."
        private const val KOREAN_EMPTY_TITLE = "아직 연락처가 없습니다"
        private const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 연락처를 만들 수 있습니다"
    }
}
