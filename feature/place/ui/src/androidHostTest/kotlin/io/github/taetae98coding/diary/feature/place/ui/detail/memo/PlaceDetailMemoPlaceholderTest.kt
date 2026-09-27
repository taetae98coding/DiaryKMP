package io.github.taetae98coding.diary.feature.place.ui.detail.memo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.place.ui.detail.placeMemo
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailMemoPlaceholderTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val pagingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @After
    fun tearDown() {
        pagingScope.cancel()
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-002 자리 표시 상태의 메모는 선택하거나 완료·삭제를 요청하지 않는다`() {
        val eventList = mutableListOf<MemoListEvent>()
        val loadedMemoTitle = "PlaceMemoLoaded${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"
        val memo = placeMemo(title = loadedMemoTitle)
        val pagingDataFlow = loadedPlaceholderPagingDataFlow(item = MemoListItem.Content(memo = memo))

        composeRule.setContent {
            DiaryTheme {
                PlaceDetailMemoTab(
                    onEvent = {},
                    onMemoListEvent = eventList::add,
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(loadedMemoTitle).fetchSemanticsNodes() shouldHaveSize 1
        composeRule.onAllNodesWithTag(MEMO_CARD_TEST_TAG).fetchSemanticsNodes() shouldHaveSize CARD_COUNT

        val placeholder = composeRule.onAllNodesWithTag(MEMO_CARD_TEST_TAG)[PLACEHOLDER_INDEX]
        placeholder.performClick()
        placeholder.performTouchInput { swipeRight() }
        placeholder.performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
    }

    // 화면이 페이지를 받아 오는 일은 화면 스레드의 공용 디스패처를 거치는데, Robolectric이 테스트 사이에 그 디스패처에 걸린
    // 예약만 지워 앞선 테스트가 남긴 예약이 있으면 이후 테스트에서 페이지가 영영 도착하지 않는다. 그래서 목록을 화면 밖에서
    // 미리 불러 둔 채로 넘겨, 화면이 처음 그릴 때부터 불러온 메모와 자리 표시가 함께 놓이게 한다.
    private fun loadedPlaceholderPagingDataFlow(item: MemoListItem): Flow<PagingData<MemoListItem>> {
        val pagingDataFlow =
            Pager(config = PagingConfig(pageSize = 1, enablePlaceholders = true)) { oneLoadedOnePlaceholderPagingSource(item = item) }
                .flow
                .cachedIn(pagingScope)

        runBlocking {
            withTimeout(PAGING_ITEMS_TIMEOUT_MILLIS) { pagingDataFlow.asSnapshot() }
        }

        return pagingDataFlow
    }

    private fun oneLoadedOnePlaceholderPagingSource(item: MemoListItem): PagingSource<Int, MemoListItem> {
        val pagingSource = mockk<PagingSource<Int, MemoListItem>>(relaxed = true)
        coEvery { pagingSource.load(any()) } returns
            PagingSource.LoadResult.Page(
                data = listOf(item),
                prevKey = null,
                nextKey = null,
                itemsBefore = 0,
                itemsAfter = 1,
            )

        return pagingSource
    }

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val CARD_COUNT = 2
        const val PLACEHOLDER_INDEX = 1
    }
}
