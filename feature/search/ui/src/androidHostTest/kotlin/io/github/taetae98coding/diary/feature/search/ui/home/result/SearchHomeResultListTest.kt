package io.github.taetae98coding.diary.feature.search.ui.home.result

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
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.RESULT_EMOJI
import io.github.taetae98coding.diary.feature.search.ui.home.loadingPagingDataFlowOf
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoList
import io.github.taetae98coding.diary.feature.search.ui.home.pagingDataFlowOf
import io.github.taetae98coding.diary.feature.search.ui.home.place.SearchHomePlaceList
import io.github.taetae98coding.diary.feature.search.ui.home.resultAddress
import io.github.taetae98coding.diary.feature.search.ui.home.resultMemo
import io.github.taetae98coding.diary.feature.search.ui.home.resultPlace
import io.github.taetae98coding.diary.feature.search.ui.home.resultTag
import io.github.taetae98coding.diary.feature.search.ui.home.resultWeb
import io.github.taetae98coding.diary.feature.search.ui.home.tag.SearchHomeTagList
import io.github.taetae98coding.diary.feature.search.ui.home.web.SearchHomeWebList
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
    fun `TC-25cf1b5e-6b1d-45a8-8fa9-27690b27c069 메모 결과에 제목과 기간을 표시한다`() {
        val dateTime =
            MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = Month.AUGUST, day = 1)..LocalDate(year = 2026, month = Month.AUGUST, day = 5))
        val memo = resultMemo(dateTime = dateTime)

        setMemoList(memoList = listOf(memo))

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNodeWithTag(MEMO_DATE_TIME_TEST_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `TC-25cf1b5e-6b1d-45a8-8fa9-27690b27c069 기간이 없는 메모 결과는 제목만 표시한다`() {
        val memo = resultMemo()

        setMemoList(memoList = listOf(memo))

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNodeWithTag(MEMO_DATE_TIME_TEST_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-25cf1b5e-6b1d-45a8-8fa9-27690b27c069 태그 결과에 이모지와 제목을 표시한다`() {
        val tag = resultTag(emoji = RESULT_EMOJI)
        val tagPagingDataFlow = pagingDataFlowOf(listOf(tag))

        composeRule.setContent {
            DiaryTheme {
                SearchHomeTagList(
                    onEvent = {},
                    onTagListEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }

        composeRule.onNodeWithText(tag.detail.emojiWithTitle).assertIsDisplayed()
    }

    @Test
    fun `TC-25cf1b5e-6b1d-45a8-8fa9-27690b27c069 장소 결과에 제목과 주소를 표시한다`() {
        val place = resultPlace(address = resultAddress())
        val placePagingDataFlow = pagingDataFlowOf(listOf(place))

        composeRule.setContent {
            DiaryTheme {
                SearchHomePlaceList(
                    onEvent = {},
                    onItemEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    placePagingItems = placePagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(place.detail.address).assertIsDisplayed()
    }

    @Test
    fun `TC-25cf1b5e-6b1d-45a8-8fa9-27690b27c069 웹 결과에 제목과 URL을 표시한다`() {
        val web = resultWeb()
        val webPagingDataFlow = pagingDataFlowOf(listOf(web))

        composeRule.setContent {
            DiaryTheme {
                SearchHomeWebList(
                    onEvent = {},
                    onItemEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    webPagingItems = webPagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }

        composeRule.onNodeWithText(web.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(web.detail.url).assertIsDisplayed()
    }

    @Test
    fun `TC-d71fce30-8fd0-422f-9428-2e1c6beeccb9 질의를 입력했는데 결과가 없으면 결과 없음을 알린다`() {
        setMemoList(query = QUERY)

        composeRule.onNodeWithText(EMPTY_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(EMPTY_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-d71fce30-8fd0-422f-9428-2e1c6beeccb9 한국어 환경에서 결과 없음 문구를 표시한다`() {
        setMemoList(query = QUERY)

        composeRule.onNodeWithText("검색 결과가 없습니다").assertIsDisplayed()
        composeRule.onNodeWithText("다른 검색어로 찾아보세요").assertIsDisplayed()
    }

    @Test
    fun `TC-1aae2f41-1e12-4453-9380-c359e10156b0 질의가 비어 있으면 결과 없음을 알리지 않는다`() {
        setMemoList()

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-d71fce30-8fd0-422f-9428-2e1c6beeccb9 질의를 입력했는데 태그 결과가 없으면 결과 없음을 알린다`() {
        setEmptyResultList(type = SearchHomeType.TAG, query = QUERY)

        composeRule.onNodeWithText(EMPTY_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(EMPTY_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-d71fce30-8fd0-422f-9428-2e1c6beeccb9 질의를 입력했는데 장소 결과가 없으면 결과 없음을 알린다`() {
        setEmptyResultList(type = SearchHomeType.PLACE, query = QUERY)

        composeRule.onNodeWithText(EMPTY_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(EMPTY_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-d71fce30-8fd0-422f-9428-2e1c6beeccb9 질의를 입력했는데 웹 결과가 없으면 결과 없음을 알린다`() {
        setEmptyResultList(type = SearchHomeType.WEB, query = QUERY)

        composeRule.onNodeWithText(EMPTY_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(EMPTY_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-1aae2f41-1e12-4453-9380-c359e10156b0 질의가 비어 있으면 태그 결과에도 결과 없음을 알리지 않는다`() {
        setEmptyResultList(type = SearchHomeType.TAG, query = "")

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-1aae2f41-1e12-4453-9380-c359e10156b0 질의가 비어 있으면 장소 결과에도 결과 없음을 알리지 않는다`() {
        setEmptyResultList(type = SearchHomeType.PLACE, query = "")

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-1aae2f41-1e12-4453-9380-c359e10156b0 질의가 비어 있으면 웹 결과에도 결과 없음을 알리지 않는다`() {
        setEmptyResultList(type = SearchHomeType.WEB, query = "")

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-82173665-c0c7-4ac5-94d4-2e16efa29fa5 결과를 준비하는 동안에는 결과 없음을 알리지 않는다`() {
        val memoPagingDataFlow = loadingPagingDataFlowOf<Memo>()

        composeRule.setContent {
            DiaryTheme {
                SearchHomeMemoList(
                    onEvent = {},
                    onMemoListEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                    query = QUERY,
                )
            }
        }

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-9ba315a8-c354-4239-883f-a849c283f9a1 결과가 있으면 결과 없음을 알리지 않는다`() {
        val memo = resultMemo()

        setMemoList(memoList = listOf(memo), query = QUERY)

        composeRule.onNodeWithText(memo.detail.title).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-af25c0b5-1b0a-4c9a-815c-f9ab7983a4cd 결과를 선택하면 그 항목의 식별자를 전달한다`() {
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
                    onEvent = {},
                    onMemoListEvent = { event ->
                        if (event is MemoListEvent.ClickMemo) onClickResult(event.id)
                    },
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                    query = query,
                )
            }
        }
    }

    // 태그·장소·웹 결과 목록을 빈 조회 결과와 테스트 데이터의 질의로 구성한다.
    private fun setEmptyResultList(
        type: SearchHomeType,
        query: String,
    ) {
        val tagPagingDataFlow = pagingDataFlowOf(emptyList<Tag>())
        val placePagingDataFlow = pagingDataFlowOf(emptyList<Place>())
        val webPagingDataFlow = pagingDataFlowOf(emptyList<Web>())

        composeRule.setContent {
            DiaryTheme {
                if (type == SearchHomeType.TAG) {
                    SearchHomeTagList(
                        onEvent = {},
                        onTagListEvent = {},
                        modifier = Modifier.fillMaxSize(),
                        tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                        query = query,
                    )
                } else if (type == SearchHomeType.PLACE) {
                    SearchHomePlaceList(
                        onEvent = {},
                        onItemEvent = {},
                        modifier = Modifier.fillMaxSize(),
                        placePagingItems = placePagingDataFlow.collectAsLazyPagingItems(),
                        query = query,
                    )
                } else {
                    SearchHomeWebList(
                        onEvent = {},
                        onItemEvent = {},
                        modifier = Modifier.fillMaxSize(),
                        webPagingItems = webPagingDataFlow.collectAsLazyPagingItems(),
                        query = query,
                    )
                }
            }
        }
    }

    private companion object {
        private const val QUERY = "여행"
        private const val EMPTY_TITLE = "No search results"
        private const val EMPTY_DESCRIPTION = "Try a different search query."
    }
}
