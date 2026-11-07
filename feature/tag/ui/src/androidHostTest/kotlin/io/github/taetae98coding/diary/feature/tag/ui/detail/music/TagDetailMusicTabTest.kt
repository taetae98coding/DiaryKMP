package io.github.taetae98coding.diary.feature.tag.ui.detail.music

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
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.music.MUSIC_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityLoadingPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagMusic
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailMusicTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-1b53a403-fc5f-4cfb-a5d2-e5c71210b8bf 대상 태그와 연결된 곡을 목록에 표시한다`() {
        setMusicTab(pagingData = tagEntityPagingData(itemList = listOf(tagMusic(title = FIRST_TITLE), tagMusic(title = SECOND_TITLE))))

        composeRule.refreshableList().assertExists()
        composeRule.onAllNodesWithTag(MUSIC_CARD_TEST_TAG).assertCountEquals(2)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-452fda2a-9823-41b0-9bb4-a49e0920b026 최초 조회 실패는 오류와 재시도를 표시하지 않는다`() {
        setMusicTab(
            pagingData =
                tagEntityPagingData<Music>(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException("Refresh failed")),
                ),
        )

        composeRule.onAllNodesWithTag(MUSIC_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-452fda2a-9823-41b0-9bb4-a49e0920b026 추가 조회 실패는 표시된 곡을 유지한다`() {
        setMusicTab(
            pagingData =
                tagEntityPagingData(
                    itemList = listOf(tagMusic(title = FIRST_TITLE)),
                    append = LoadState.Error(IllegalStateException("Append failed")),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(MUSIC_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-13d845f3-f943-4de0-8c00-700b51190013 곡을 선택하면 그 곡의 상세 이동을 요청한다`() {
        val item = tagMusic(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailMusicContentEvent>()
        setMusicTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onEvent = eventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(TagDetailMusicContentEvent.ClickMusic(id = item.id))
    }

    @Test
    fun `TC-7b4bd46a-bc10-419c-8094-5c9ef50096ce 연결된 곡이 없으면 빈 상태 안내를 표시한다`() {
        setMusicTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.refreshableList().assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-7b4bd46a-bc10-419c-8094-5c9ef50096ce 한국어 환경에서 빈 상태 안내를 표시한다`() {
        setMusicTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-5958878a-7986-4063-8bcd-aaf7e7d6fadf 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setMusicTab(pagingData = tagEntityLoadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-9ab5829e-0226-4ad9-9a1c-7c169518d342 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailMusicContentEvent>()
        setMusicTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagMusic(title = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailMusicContentEvent.Refresh)
    }

    @Test
    fun `TC-9ab5829e-0226-4ad9-9a1c-7c169518d342 빈 상태에서도 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailMusicContentEvent>()
        setMusicTab(pagingData = tagEntityPagingData(itemList = emptyList()), onEvent = eventList::add)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailMusicContentEvent.Refresh)
    }

    @Test
    fun `TC-9ab5829e-0226-4ad9-9a1c-7c169518d342 새로고침이 진행 중이면 진행 표시가 나타나고 끝나면 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setMusicTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagMusic(title = FIRST_TITLE))),
            isRefreshingProvider = { isRefreshing.value },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-13f5cb56-189d-4536-9d2b-339c2028be0d 곡 카드를 삭제 방향으로 밀면 그 곡의 삭제를 전달한다`() {
        val item = tagMusic(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailMusicContentEvent>()
        setMusicTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailMusicContentEvent.DeleteMusic(id = item.id))
    }

    @Test
    fun `TC-756a123e-232e-45c9-9cde-d021fc248b8b 곡 카드를 반대 방향으로 밀면 아무 동작을 요청하지 않는다`() {
        val eventList = mutableListOf<TagDetailMusicContentEvent>()
        setMusicTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagMusic(title = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(MUSIC_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-e0b49707-d7ca-4003-a7bd-b216b33256f9 목록 위에 지금 정렬을 표시하고 정렬을 고르면 그 기준을 전달한다`() {
        val eventList = mutableListOf<TagDetailMusicContentEvent>()
        setMusicTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagMusic(title = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(DEFAULT_SORT_LABEL).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()

        eventList shouldBe listOf(TagDetailMusicContentEvent.ClickSort)
    }

    @Test
    fun `TC-7b4bd46a-bc10-419c-8094-5c9ef50096ce 빈 상태에서는 정렬 컨트롤을 감춘다`() {
        setMusicTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    private fun setMusicTab(
        pagingData: PagingData<Music> = PagingData.empty(),
        onEvent: (TagDetailMusicContentEvent) -> Unit = {},
        isRefreshingProvider: () -> Boolean = { false },
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagDetailMusicTab(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    musicPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }

    private companion object {
        const val FIRST_TITLE = "AlphaMusic"
        const val SECOND_TITLE = "BetaMusic"
        const val DEFAULT_EMPTY_TITLE = "No songs linked to this tag"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to add a song."
        const val KOREAN_EMPTY_TITLE = "이 태그에 연결된 곡이 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 곡을 만들 수 있습니다"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val DEFAULT_SORT_LABEL = "Title"
    }
}
