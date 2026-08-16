package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_TIME_TEST_TAG
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SearchHomeResultListTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SEARCH-HOME-FEATURE-008 메모 결과에 제목과 기간을 표시한다`() {
        val dateTime =
            MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = Month.AUGUST, day = 1)..LocalDate(year = 2026, month = Month.AUGUST, day = 5))
        val memo = resultMemo(dateTime = dateTime)

        setMemoList(memoList = listOf(memo))

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNodeWithTag(MEMO_DATE_TIME_TEST_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-008 기간이 없는 메모 결과는 제목만 표시한다`() {
        val memo = resultMemo()

        setMemoList(memoList = listOf(memo))

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNodeWithTag(MEMO_DATE_TIME_TEST_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-008 태그 결과에 이모지와 제목을 표시한다`() {
        val tag = resultTag(emoji = RESULT_EMOJI)
        val tagPagingDataFlow = pagingDataFlowOf(listOf(tag))

        composeRule.setContent {
            DiaryTheme {
                SearchHomeTagList(
                    onEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }

        composeRule.onNodeWithText(tag.detail.emojiWithTitle).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-008 장소 결과에 제목과 주소를 표시한다`() {
        val place = resultPlace(address = resultAddress())
        val placePagingDataFlow = pagingDataFlowOf(listOf(place))

        composeRule.setContent {
            DiaryTheme {
                SearchHomePlaceList(
                    onEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    placePagingItems = placePagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(place.detail.address).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-008 웹 결과에 제목과 URL을 표시한다`() {
        val web = resultWeb()
        val webPagingDataFlow = pagingDataFlowOf(listOf(web))

        composeRule.setContent {
            DiaryTheme {
                SearchHomeWebList(
                    onEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    webPagingItems = webPagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }

        composeRule.onNodeWithText(web.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(web.detail.url).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-016 질의를 입력했는데 결과가 없으면 결과 없음을 알린다`() {
        setMemoList(query = QUERY)

        composeRule.onNodeWithText(EMPTY_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(EMPTY_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SEARCH-HOME-FEATURE-016 한국어 환경에서 결과 없음 문구를 표시한다`() {
        setMemoList(query = QUERY)

        composeRule.onNodeWithText("검색 결과가 없습니다").assertIsDisplayed()
        composeRule.onNodeWithText("다른 검색어로 찾아보세요").assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-018 질의가 비어 있으면 결과 없음을 알리지 않는다`() {
        setMemoList()

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-019 결과를 준비하는 동안에는 결과 없음을 알리지 않는다`() {
        val memoPagingDataFlow = loadingPagingDataFlowOf<Memo>()

        composeRule.setContent {
            DiaryTheme {
                SearchHomeMemoList(
                    onEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                    query = QUERY,
                )
            }
        }

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-020 결과가 있으면 결과 없음을 알리지 않는다`() {
        val memo = resultMemo()

        setMemoList(memoList = listOf(memo), query = QUERY)

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-012 결과를 선택하면 그 항목의 식별자를 전달한다`() {
        val memo = resultMemo()
        val idList = mutableListOf<Uuid>()

        setMemoList(memoList = listOf(memo), onClickResult = idList::add)
        composeRule.onNodeWithText(memo.detail.title).performClick()
        composeRule.waitForIdle()

        idList shouldBe listOf(memo.id)
    }

    private fun setMemoList(
        memoList: List<Memo> = emptyList(),
        query: String = "",
        onClickResult: (Uuid) -> Unit = {},
    ) {
        val memoPagingDataFlow = pagingDataFlowOf(memoList)

        composeRule.setContent {
            DiaryTheme {
                SearchHomeMemoList(
                    onEvent = { event ->
                        if (event is SearchHomeResultEvent.ClickResult) onClickResult(event.id)
                    },
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                    query = query,
                )
            }
        }
    }

    private companion object {
        private const val QUERY = "여행"
        private const val EMPTY_TITLE = "No search results"
        private const val EMPTY_DESCRIPTION = "Try a different search query."
    }
}
