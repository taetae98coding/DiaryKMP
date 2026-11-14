package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.MEMO_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.tag.TAG_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.list.TagListEvent
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.search.ui.home.dday.SearchHomeDDayList
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoList
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceList
import io.github.taetae98coding.diary.feature.search.ui.home.resultDDay
import io.github.taetae98coding.diary.feature.search.ui.home.resultMemo
import io.github.taetae98coding.diary.feature.search.ui.home.resultPlace
import io.github.taetae98coding.diary.feature.search.ui.home.resultRoutine
import io.github.taetae98coding.diary.feature.search.ui.home.resultTag
import io.github.taetae98coding.diary.feature.search.ui.home.resultWeb
import io.github.taetae98coding.diary.feature.search.ui.home.routine.SearchHomeRoutineList
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagList
import io.github.taetae98coding.diary.feature.search.ui.home.web.SearchHomeWebList
import io.github.taetae98coding.diary.feature.search.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SearchHomeResultPlaceholderSwipeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-90faa2f5-2b53-45ca-b379-6899323a311d 메모 결과의 자리 표시 자리는 밀어도 동작을 요청하지 않는다`() {
        val eventList = mutableListOf<MemoListEvent>()
        val pagingDataFlow = loadedPlaceholderPagingDataFlow(item = resultMemo())

        composeRule.setContent {
            DiaryTheme {
                SearchHomeMemoList(
                    onEvent = {},
                    onMemoListEvent = eventList::add,
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    query = QUERY,
                )
            }
        }
        swipePlaceholder(testTag = MEMO_CARD_TEST_TAG)

        eventList.shouldBeEmpty()
    }

    @Test
    fun `TC-90faa2f5-2b53-45ca-b379-6899323a311d 태그 결과의 자리 표시 자리는 밀어도 동작을 요청하지 않는다`() {
        val eventList = mutableListOf<TagListEvent>()
        val pagingDataFlow = loadedPlaceholderPagingDataFlow(item = resultTag())

        composeRule.setContent {
            DiaryTheme {
                SearchHomeTagList(
                    onEvent = {},
                    onTagListEvent = eventList::add,
                    modifier = Modifier.fillMaxSize(),
                    tagPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    query = QUERY,
                )
            }
        }
        swipePlaceholder(testTag = TAG_CARD_TEST_TAG)

        eventList.shouldBeEmpty()
    }

    @Test
    fun `TC-90faa2f5-2b53-45ca-b379-6899323a311d 장소 결과의 자리 표시 자리는 밀어도 삭제를 요청하지 않는다`() {
        val eventList = mutableListOf<SearchHomeResultItemEvent>()
        val pagingDataFlow = loadedPlaceholderPagingDataFlow(item = resultPlace())

        composeRule.setContent {
            DiaryTheme {
                SearchHomePlaceList(
                    onEvent = {},
                    onItemEvent = eventList::add,
                    modifier = Modifier.fillMaxSize(),
                    placePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    query = QUERY,
                )
            }
        }
        swipePlaceholder(testTag = PLACE_CARD_TEST_TAG)

        eventList.shouldBeEmpty()
    }

    @Test
    fun `TC-90faa2f5-2b53-45ca-b379-6899323a311d 웹 결과의 자리 표시 자리는 밀어도 삭제를 요청하지 않는다`() {
        val eventList = mutableListOf<SearchHomeResultItemEvent>()
        val pagingDataFlow = loadedPlaceholderPagingDataFlow(item = resultWeb())

        composeRule.setContent {
            DiaryTheme {
                SearchHomeWebList(
                    onEvent = {},
                    onItemEvent = eventList::add,
                    modifier = Modifier.fillMaxSize(),
                    webPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    query = QUERY,
                )
            }
        }
        swipePlaceholder(testTag = WEB_CARD_TEST_TAG)

        eventList.shouldBeEmpty()
    }

    @Test
    fun `TC-90faa2f5-2b53-45ca-b379-6899323a311d 디데이 결과의 자리 표시 자리는 밀어도 삭제를 요청하지 않는다`() {
        val eventList = mutableListOf<SearchHomeResultItemEvent>()
        val pagingDataFlow = loadedPlaceholderPagingDataFlow(item = resultDDay())

        composeRule.setContent {
            DiaryTheme {
                SearchHomeDDayList(
                    onEvent = {},
                    onItemEvent = eventList::add,
                    modifier = Modifier.fillMaxSize(),
                    ddayPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    query = QUERY,
                )
            }
        }
        swipePlaceholder(testTag = DDAY_CARD_TEST_TAG)

        eventList.shouldBeEmpty()
    }

    @Test
    fun `TC-90faa2f5-2b53-45ca-b379-6899323a311d 루틴 결과의 자리 표시 자리는 밀어도 동작을 요청하지 않는다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        val pagingDataFlow = loadedPlaceholderPagingDataFlow(item = resultRoutine())

        composeRule.setContent {
            DiaryTheme {
                SearchHomeRoutineList(
                    onRoutineListEvent = eventList::add,
                    modifier = Modifier.fillMaxSize(),
                    routinePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    query = QUERY,
                )
            }
        }
        swipePlaceholder(testTag = ROUTINE_CARD_TEST_TAG)

        eventList.shouldBeEmpty()
    }

    private fun swipePlaceholder(testTag: String) {
        // 첫 페이지가 도착해 불러온 항목 뒤에 자리 표시가 놓일 때까지 기다린다.
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes().size == CARD_COUNT
        }
        composeRule.onAllNodesWithTag(testTag).fetchSemanticsNodes() shouldHaveSize CARD_COUNT

        val placeholder = composeRule.onAllNodesWithTag(testTag)[PLACEHOLDER_INDEX]
        placeholder.performTouchInput { swipeRight() }
        placeholder.performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    // 뒤에 항목이 하나 더 있다고 알리는 첫 페이지만 돌려주어, 불러온 항목 뒤에 자리 표시가 놓이게 한다.
    private fun <T : Any> loadedPlaceholderPagingDataFlow(item: T): Flow<PagingData<T>> {
        val pagingSource = mockk<PagingSource<Int, T>>(relaxed = true)
        coEvery { pagingSource.load(any()) } returns
            PagingSource.LoadResult.Page(
                data = listOf(item),
                prevKey = null,
                nextKey = null,
                itemsBefore = 0,
                itemsAfter = 1,
            )

        return Pager(config = PagingConfig(pageSize = 1, enablePlaceholders = true)) { pagingSource }.flow
    }

    private companion object {
        const val QUERY = "여행"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val CARD_COUNT = 2
        const val PLACEHOLDER_INDEX = 1
    }
}
