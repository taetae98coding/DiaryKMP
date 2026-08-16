package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
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
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactHomeScaffoldRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-HOME-FEATURE-017 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<ContactHomeScaffoldEvent>()
        setContactHomeScaffold(
            contactList = listOf(testContact(name = CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(ContactHomeScaffoldEvent.Refresh)
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-018 빈 상태에서도 당겨서 새로고침을 요청한다`() {
        val eventList = mutableListOf<ContactHomeScaffoldEvent>()
        setContactHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(ContactHomeScaffoldEvent.Refresh)
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-019 진행 표시 상태이면 진행 표시가 나타난다`() {
        setContactHomeScaffold(isRefreshingProvider = { true })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-019 동기화가 끝나면 진행 표시가 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setContactHomeScaffold(isRefreshingProvider = { isRefreshing.value })
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun setContactHomeScaffold(
        contactList: List<Contact> = emptyList(),
        isRefreshingProvider: () -> Boolean = { false },
        onEvent: (ContactHomeScaffoldEvent) -> Unit = {},
    ) {
        val contactPagingDataFlow = MutableStateFlow(contactPagingDataOf(contactList))

        composeRule.setContent {
            DiaryTheme {
                ContactHomeScaffold(
                    onEvent = onEvent,
                    contactPagingItems = contactPagingDataFlow.collectAsLazyPagingItems(),
                    uiStateProvider = { ContactHomeUiState(isRefreshing = isRefreshingProvider()) },
                )
            }
        }
    }

    private companion object {
        private const val CONTACT_NAME = "RefreshContactName"
        private const val CONTACT_PHONE_NUMBER = "010-5555-6666"
        private const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
