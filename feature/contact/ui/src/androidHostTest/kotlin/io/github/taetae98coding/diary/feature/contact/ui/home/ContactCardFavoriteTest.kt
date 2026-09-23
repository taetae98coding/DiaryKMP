package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.paging.compose.collectAsLazyPagingItems
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
class ContactCardFavoriteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-HOME-FEATURE-023 즐겨찾기인 연락처에만 즐겨찾기 표시가 나타난다`() {
        val favorite = testContact(name = FAVORITE_CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER), isFavorite = true)
        val other = testContact(name = CONTACT_NAME, phoneNumberList = listOf(OTHER_CONTACT_PHONE_NUMBER), isFavorite = false)

        setContactHomeList(contactList = listOf(favorite, other))

        composeRule.onAllNodesWithContentDescription(FAVORITE_CONTENT_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-HOME-FEATURE-023 한국어 환경에서 즐겨찾기 표시의 이름을 제공한다`() {
        setContactHomeList(
            contactList = listOf(testContact(name = FAVORITE_CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER), isFavorite = true)),
        )

        composeRule.onNodeWithContentDescription(KOREAN_FAVORITE_CONTENT_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-024 즐겨찾기 표시를 눌러도 즐겨찾기는 바뀌지 않고 그 연락처를 선택한 이벤트만 전달한다`() {
        val favorite = testContact(name = FAVORITE_CONTACT_NAME, phoneNumberList = listOf(CONTACT_PHONE_NUMBER), isFavorite = true)
        val eventList = mutableListOf<ContactHomeScaffoldEvent>()

        setContactHomeList(contactList = listOf(favorite), onEvent = eventList::add)
        composeRule.onNodeWithContentDescription(FAVORITE_CONTENT_DESCRIPTION).performClick()

        eventList shouldBe listOf(ContactHomeScaffoldEvent.ClickContact(id = favorite.id))
        composeRule.onNodeWithContentDescription(FAVORITE_CONTENT_DESCRIPTION).assertExists()
    }

    private fun setContactHomeList(
        contactList: List<Contact>,
        onEvent: (ContactHomeScaffoldEvent) -> Unit = {},
    ) {
        val pagingDataFlow = MutableStateFlow(contactPagingDataOf(contactList))

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
        private const val CONTACT_NAME = "이영희"
        private const val FAVORITE_CONTACT_NAME = "김철수"
        private const val CONTACT_PHONE_NUMBER = "010-1111-2222"
        private const val OTHER_CONTACT_PHONE_NUMBER = "010-3333-4444"

        private const val FAVORITE_CONTENT_DESCRIPTION = "Favorite"
        private const val KOREAN_FAVORITE_CONTENT_DESCRIPTION = "즐겨찾기"
    }
}
