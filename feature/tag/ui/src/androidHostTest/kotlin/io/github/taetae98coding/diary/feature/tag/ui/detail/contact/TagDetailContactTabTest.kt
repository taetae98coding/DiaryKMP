package io.github.taetae98coding.diary.feature.tag.ui.detail.contact

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.contact.CONTACT_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.tagContact
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityLoadingPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailContactTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-e8ba66c1-2a33-41b6-843f-3d1021b92467 대상 태그와 연결된 연락처를 목록에 표시한다`() {
        setContactTab(pagingData = tagEntityPagingData(itemList = listOf(tagContact(name = FIRST_TITLE), tagContact(name = SECOND_TITLE))))

        composeRule.refreshableList().assertExists()
        composeRule.onAllNodesWithTag(CONTACT_CARD_TEST_TAG).assertCountEquals(2)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-5ced89c9-a106-47ec-8c29-a056dbd587f3 최초 조회 실패는 오류와 재시도를 표시하지 않는다`() {
        setContactTab(
            pagingData =
                tagEntityPagingData<ContactListItem>(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException("Refresh failed")),
                ),
        )

        composeRule.onAllNodesWithTag(CONTACT_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-5ced89c9-a106-47ec-8c29-a056dbd587f3 추가 조회 실패는 표시된 연락처를 유지한다`() {
        setContactTab(
            pagingData =
                tagEntityPagingData(
                    itemList = listOf(tagContact(name = FIRST_TITLE)),
                    append = LoadState.Error(IllegalStateException("Append failed")),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(CONTACT_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-3d3d1f17-09df-420f-8a4f-cb47542ec96c 연락처를 선택하면 그 연락처의 상세 이동을 요청한다`() {
        val item = tagContact(name = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailContactContentEvent>()
        setContactTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onEvent = eventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(TagDetailContactContentEvent.ClickContact(id = item.contact.id))
    }

    @Test
    fun `TC-5bd220a7-c214-4917-a581-ca45adbe7256 연결된 연락처가 없으면 빈 상태 안내를 표시한다`() {
        setContactTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.refreshableList().assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-5bd220a7-c214-4917-a581-ca45adbe7256 한국어 환경에서 빈 상태 안내를 표시한다`() {
        setContactTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-1d2e04af-833f-4a5b-a3c7-ce2bdf2b803a 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setContactTab(pagingData = tagEntityLoadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-f76e8090-9cf9-4cba-bf68-84a5dcb0bcc8 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailContactContentEvent>()
        setContactTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagContact(name = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailContactContentEvent.Refresh)
    }

    @Test
    fun `TC-f76e8090-9cf9-4cba-bf68-84a5dcb0bcc8 빈 상태에서도 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailContactContentEvent>()
        setContactTab(pagingData = tagEntityPagingData(itemList = emptyList()), onEvent = eventList::add)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailContactContentEvent.Refresh)
    }

    @Test
    fun `TC-f76e8090-9cf9-4cba-bf68-84a5dcb0bcc8 새로고침이 진행 중이면 진행 표시가 나타나고 끝나면 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setContactTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagContact(name = FIRST_TITLE))),
            isRefreshingProvider = { isRefreshing.value },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-139803d7-0f5d-470b-b383-b638e02b0e04 연락처 카드를 삭제 방향으로 밀면 그 연락처의 삭제를 전달한다`() {
        val item = tagContact(name = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailContactContentEvent>()
        setContactTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailContactContentEvent.DeleteContact(id = item.contact.id))
    }

    @Test
    fun `TC-2d42ad22-79c8-408b-be0d-0019b721cd2a 연락처 카드를 반대 방향으로 밀면 아무 동작을 요청하지 않는다`() {
        val eventList = mutableListOf<TagDetailContactContentEvent>()
        setContactTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagContact(name = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
    }

    @Test
    fun `목록 위에 지금 정렬을 표시하고 정렬을 고르면 그 기준을 전달한다`() {
        val eventList = mutableListOf<TagDetailContactContentEvent>()
        setContactTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagContact(name = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(DEFAULT_SORT_LABEL).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()

        eventList shouldBe listOf(TagDetailContactContentEvent.ClickSort)
    }

    @Test
    fun `TC-5bd220a7-c214-4917-a581-ca45adbe7256 빈 상태에서는 정렬 컨트롤을 감춘다`() {
        setContactTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-24a8eb8a-cd1b-4ce8-8467-36157533e65d 연락처 탭에는 유무 필터를 제공하지 않는다`() {
        setContactTab(pagingData = tagEntityPagingData(itemList = listOf(tagContact(name = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).assertDoesNotExist()
    }

    private fun setContactTab(
        pagingData: PagingData<ContactListItem> = PagingData.empty(),
        onEvent: (TagDetailContactContentEvent) -> Unit = {},
        isRefreshingProvider: () -> Boolean = { false },
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagDetailContactTab(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    contactPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }

    private companion object {
        const val FIRST_TITLE = "AlphaContact"
        const val SECOND_TITLE = "BetaContact"
        const val DEFAULT_EMPTY_TITLE = "No contacts linked to this tag"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a contact."
        const val KOREAN_EMPTY_TITLE = "이 태그에 연결된 연락처가 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 연락처를 만들 수 있습니다"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val DEFAULT_SORT_LABEL = "Name"
        const val DEFAULT_FILTER_DESCRIPTION = "Filter"
    }
}
