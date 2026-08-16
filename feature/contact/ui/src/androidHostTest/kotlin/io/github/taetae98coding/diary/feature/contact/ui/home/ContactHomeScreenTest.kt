package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-HOME-FEATURE-001 조회한 연락처를 목록에 표시한다`() {
        val contact = testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER))

        setContactHomeScreen(contactList = listOf(contact))

        composeRule.onNodeWithText(CONTACT_NAME).assertExists()
        composeRule.onNodeWithText(CONTACT_PHONE_NUMBER).assertExists()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-014 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0

        setContactHomeScreen(navigateUp = { navigateUpCount++ })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-009 연락처 추가를 선택하면 연락처 추가 화면으로 이동한다`() {
        var navigateToAddCount = 0

        setContactHomeScreen(navigateToAdd = { navigateToAddCount++ })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateToAddCount shouldBe 1
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-015 연락처를 선택하면 그 연락처의 상세 화면으로 이동한다`() {
        val contact = testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER))
        val navigatedIdList = mutableListOf<Uuid>()

        setContactHomeScreen(contactList = listOf(contact), navigateToDetail = navigatedIdList::add)
        composeRule.onAllNodesWithTag(CONTACT_CARD_TEST_TAG).onFirst().performClick()

        navigatedIdList shouldBe listOf(contact.id)
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-012 고른 정렬을 정렬 컨트롤에 반영한다`() {
        setContactHomeScreen()

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_NAME_SORT).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-017 목록을 당기면 새로고침을 한 번 실행한다`() {
        val syncViewModel = syncViewModel()

        setContactHomeScreen(
            contactList = listOf(testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER))),
            syncViewModel = syncViewModel,
        )
        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-019 동기화가 진행 중이면 진행 표시가 나타난다`() {
        setContactHomeScreen(syncViewModel = syncViewModel(uiState = ContactHomeUiState(isRefreshing = true)))

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    private fun setContactHomeScreen(
        contactList: List<Contact> = emptyList(),
        navigateUp: () -> Unit = {},
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
        componentVisible: ContactHomeScaffoldComponentVisible = ContactHomeScaffoldComponentVisible(),
        syncViewModel: ContactHomeSyncViewModel = syncViewModel(),
    ) {
        val contactViewModel = mockk<ContactHomeViewModel>()
        val sort = MutableStateFlow(ListSort.NAME)
        every { contactViewModel.sort } returns sort
        every { contactViewModel.contactPagingData } returns MutableStateFlow(contactPagingDataOf(contactList))
        every { contactViewModel.select(sort = any()) } answers { sort.value = firstArg() }

        composeRule.setContent {
            DiaryTheme {
                ContactHomeScreen(
                    navigateUp = navigateUp,
                    navigateToAdd = navigateToAdd,
                    navigateToDetail = navigateToDetail,
                    componentVisibleProvider = { componentVisible },
                    contactViewModel = contactViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
    }

    private companion object {
        private const val CONTACT_NAME = "ContactHomeScreenName"
        private const val CONTACT_PHONE_NUMBER = "010-1111-2222"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add contact"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val DEFAULT_NAME_SORT = "Name"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        private const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"

        private fun syncViewModel(uiState: ContactHomeUiState = ContactHomeUiState()): ContactHomeSyncViewModel {
            val viewModel = mockk<ContactHomeSyncViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(uiState)
            justRun { viewModel.refresh() }

            return viewModel
        }
    }
}
