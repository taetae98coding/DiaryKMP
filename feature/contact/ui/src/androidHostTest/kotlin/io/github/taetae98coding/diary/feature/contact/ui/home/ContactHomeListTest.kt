package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.feature.contact.ui.refreshableList
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactHomeListTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ae158c85-2169-44b2-8ac6-954c26974660 저장된 연락처의 이름과 전화번호를 표시한다`() {
        val first = testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER))
        val second = testContact(name = OTHER_CONTACT_NAME, phoneNumberList = listOf(OTHER_CONTACT_PHONE_NUMBER))

        setContactHomeList(contactList = listOf(first, second))

        composeRule.onNodeWithText(CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(CONTACT_PHONE_NUMBER).assertExists()
        composeRule.onNodeWithText(OTHER_CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(OTHER_CONTACT_PHONE_NUMBER).assertExists()
    }

    @Test
    fun `TC-2f021c70-86a4-4276-a1fb-56241c3a9b88 전화번호가 여러 개면 첫 번째 전화번호만 표시한다`() {
        val contact = testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER, OTHER_PHONE_NUMBER))

        setContactHomeList(contactList = listOf(contact))

        composeRule.onNodeWithText(CONTACT_PHONE_NUMBER).assertExists()
        composeRule.onNodeWithText(OTHER_PHONE_NUMBER).assertDoesNotExist()
    }

    @Test
    fun `TC-86555829-09bd-4932-9399-2a77dea79a9e 전화번호가 없는 연락처는 이름만 표시한다`() {
        val contact = testContact(name = CONTACT_NAME, phoneNumberList = emptyList())

        setContactHomeList(contactList = listOf(contact))

        // 카드는 클릭 가능한 컨테이너여서 자식 문구를 하나로 합치므로, 합쳐진 문구로 줄 수를 확인한다.
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).assertTextEquals(CONTACT_NAME)
    }

    @Test
    fun `TC-2f021c70-86a4-4276-a1fb-56241c3a9b88 전화번호가 있는 연락처는 이름과 첫 번째 전화번호를 함께 읽힌다`() {
        val contact = testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER, OTHER_PHONE_NUMBER))

        setContactHomeList(contactList = listOf(contact))

        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).assertTextEquals(CONTACT_NAME, CONTACT_PHONE_NUMBER)
    }

    @Test
    fun `TC-b0683e6f-02ad-4483-a1b7-6a50b491e6ac 연락처의 설명과 고향은 목록에 표시하지 않는다`() {
        val contact =
            testContact(
                name = CONTACT_NAME,
                phoneNumberList = listOf(CONTACT_PHONE_NUMBER),
                description = CONTACT_DESCRIPTION,
                hometown = CONTACT_HOMETOWN,
            )

        setContactHomeList(contactList = listOf(contact))

        composeRule.onNodeWithText(CONTACT_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(CONTACT_HOMETOWN).assertDoesNotExist()
    }

    @Test
    fun `TC-f2c81ef9-b642-4471-b828-6a40267bc604 표시할 연락처가 있으면 빈 상태 안내를 표시하지 않는다`() {
        setContactHomeList(contactList = listOf(testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER))))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-ab3831fa-80e1-4110-9367-289b6856fc3a 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setContactHomeList(pagingData = loadingContactPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-9c2309f9-b843-4de1-adb0-fa01300765e7 목록 조회에 실패하면 오류 안내 없이 빈 상태 안내를 표시한다`() {
        setContactHomeList(pagingData = failedContactPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-1703c8ae-d3c7-4acc-8d12-3bfc6838c2e1 연락처 카드를 누르면 그 연락처를 선택한 이벤트를 전달한다`() {
        val contact = testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER))
        val eventList = mutableListOf<ContactHomeScaffoldEvent>()

        setContactHomeList(contactList = listOf(contact), onEvent = eventList::add)
        composeRule.onAllNodesWithTag(CONTACT_CARD_TEST_TAG).onFirst().performClick()

        eventList shouldBe listOf(ContactHomeScaffoldEvent.ClickContact(id = contact.id))
    }

    @Test
    fun `TC-4e87f335-aaff-447d-98b4-5bc14e1e6d38 화면이 재생성되어도 보던 목록 위치를 유지한다`() {
        val contactList =
            List(RESTORATION_CONTACT_COUNT) { index ->
                testContact(name = "연락처-${index.toString().padStart(length = 2, padChar = '0')}")
            }
        val pagingDataFlow = MutableStateFlow(contactPagingDataOf(contactList))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                ContactHomeList(
                    onEvent = {},
                    contactPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.refreshableList().performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(contactList[RESTORATION_SCROLL_INDEX].detail.name).assertIsDisplayed()
        composeRule.onNodeWithText(contactList.first().detail.name).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(contactList[RESTORATION_SCROLL_INDEX].detail.name).assertIsDisplayed()
        composeRule.onNodeWithText(contactList.first().detail.name).assertDoesNotExist()
    }

    private fun setContactHomeList(
        contactList: List<Contact> = emptyList(),
        pagingData: PagingData<Contact> = contactPagingDataOf(contactList),
        onEvent: (ContactHomeScaffoldEvent) -> Unit = {},
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                ContactHomeList(
                    onEvent = onEvent,
                    contactPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
    }

    private companion object {
        private const val CONTACT_NAME = "김철수"
        private const val OTHER_CONTACT_NAME = "이영희"
        private const val CONTACT_PHONE_NUMBER = "010-1111-2222"
        private const val OTHER_PHONE_NUMBER = "02-333-4444"
        private const val OTHER_CONTACT_PHONE_NUMBER = "010-3333-4444"
        private const val CONTACT_DESCRIPTION = "ContactHomeListDescription"
        private const val CONTACT_HOMETOWN = "ContactHomeListHometown"
        private const val RESTORATION_CONTACT_COUNT = 40
        private const val RESTORATION_SCROLL_INDEX = 30
    }
}
