package io.github.taetae98coding.diary.feature.tag.ui.detail.checklist

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
import io.github.taetae98coding.diary.compose.checklist.CHECKLIST_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.tagChecklist
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
class TagDetailChecklistTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-d28ab57d-cc71-4cb4-9bea-c381d6657d66 대상 태그와 연결된 체크리스트를 목록에 표시한다`() {
        setChecklistTab(pagingData = tagEntityPagingData(itemList = listOf(tagChecklist(title = FIRST_TITLE), tagChecklist(title = SECOND_TITLE))))

        composeRule.refreshableList().assertExists()
        composeRule.onAllNodesWithTag(CHECKLIST_CARD_TEST_TAG).assertCountEquals(2)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-49e49fc0-cd1e-44c6-8910-8f2a37732a30 최초 조회 실패는 오류와 재시도를 표시하지 않는다`() {
        setChecklistTab(
            pagingData =
                tagEntityPagingData<ChecklistSummary>(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException("Refresh failed")),
                ),
        )

        composeRule.onAllNodesWithTag(CHECKLIST_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-49e49fc0-cd1e-44c6-8910-8f2a37732a30 추가 조회 실패는 표시된 체크리스트를 유지한다`() {
        setChecklistTab(
            pagingData =
                tagEntityPagingData(
                    itemList = listOf(tagChecklist(title = FIRST_TITLE)),
                    append = LoadState.Error(IllegalStateException("Append failed")),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(CHECKLIST_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-93a86f7b-cfec-427e-a3ae-b8169779d557 체크리스트를 선택하면 그 체크리스트의 상세 이동을 요청한다`() {
        val item = tagChecklist(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailChecklistContentEvent>()
        setChecklistTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onEvent = eventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(TagDetailChecklistContentEvent.ClickChecklist(id = item.checklist.id))
    }

    @Test
    fun `TC-615b3aca-b92d-4590-a47b-1c58333caa7f 연결된 체크리스트가 없으면 빈 상태 안내를 표시한다`() {
        setChecklistTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.refreshableList().assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-615b3aca-b92d-4590-a47b-1c58333caa7f 한국어 환경에서 빈 상태 안내를 표시한다`() {
        setChecklistTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-9786ff03-e635-4a54-8dfc-5aeb4478fafe 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setChecklistTab(pagingData = tagEntityLoadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-08d27cf5-8623-4bf2-af1b-926f422bd9e5 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailChecklistContentEvent>()
        setChecklistTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagChecklist(title = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailChecklistContentEvent.Refresh)
    }

    @Test
    fun `TC-08d27cf5-8623-4bf2-af1b-926f422bd9e5 빈 상태에서도 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailChecklistContentEvent>()
        setChecklistTab(pagingData = tagEntityPagingData(itemList = emptyList()), onEvent = eventList::add)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailChecklistContentEvent.Refresh)
    }

    @Test
    fun `TC-08d27cf5-8623-4bf2-af1b-926f422bd9e5 새로고침이 진행 중이면 진행 표시가 나타나고 끝나면 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setChecklistTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagChecklist(title = FIRST_TITLE))),
            isRefreshingProvider = { isRefreshing.value },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-7e3282b8-98a9-41f0-bee2-7bd0fcbfe608 체크리스트 카드를 삭제 방향으로 밀면 그 체크리스트의 삭제를 전달한다`() {
        val item = tagChecklist(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailChecklistContentEvent>()
        setChecklistTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailChecklistContentEvent.DeleteChecklist(id = item.checklist.id))
    }

    @Test
    fun `TC-46d2b79a-3029-4b06-97f0-0cd9e132e627 체크리스트 카드를 반대 방향으로 밀면 아무 동작을 요청하지 않는다`() {
        val eventList = mutableListOf<TagDetailChecklistContentEvent>()
        setChecklistTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagChecklist(title = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-9b782344-4cc1-433b-a0b5-1ff1537cb462 체크리스트 탭은 정렬 선택을 제공하지 않는다`() {
        setChecklistTab(pagingData = tagEntityPagingData(itemList = listOf(tagChecklist(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-5355fbe4-edb6-4a79-911a-c7504715b547 각 체크리스트의 진행 정도를 표시한다`() {
        setChecklistTab(pagingData = tagEntityPagingData(itemList = listOf(tagChecklist(title = FIRST_TITLE))))

        composeRule.onNodeWithText(PROGRESS_TEXT).assertIsDisplayed()
    }

    private fun setChecklistTab(
        pagingData: PagingData<ChecklistSummary> = PagingData.empty(),
        onEvent: (TagDetailChecklistContentEvent) -> Unit = {},
        isRefreshingProvider: () -> Boolean = { false },
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagDetailChecklistTab(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    checklistPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }

    private companion object {
        const val FIRST_TITLE = "AlphaChecklist"
        const val SECOND_TITLE = "BetaChecklist"
        const val DEFAULT_EMPTY_TITLE = "No checklists linked to this tag"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a checklist."
        const val KOREAN_EMPTY_TITLE = "이 태그에 연결된 체크리스트가 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 체크리스트를 만들 수 있습니다"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val PROGRESS_TEXT = "1/2"
    }
}
