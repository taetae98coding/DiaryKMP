package io.github.taetae98coding.diary.feature.place.ui.detail.memo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_MEMO_ADD_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.FIRST_PLACE_ID
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailUiState
import io.github.taetae98coding.diary.feature.place.ui.detail.content
import io.github.taetae98coding.diary.feature.place.ui.detail.placeDetail
import io.github.taetae98coding.diary.feature.place.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.selectPlaceDetailTab
import io.github.taetae98coding.diary.feature.place.ui.detail.setPlaceDetailScreen
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailMemoEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-012 연결된 메모가 없으면 빈 상태 안내를 표시한다`() {
        setMemoTab(pagingData = loadedEmptyPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-012 한국어 환경에서 빈 상태 안내는 이 장소에 연결된 메모가 없습니다이다`() {
        setMemoTab(pagingData = loadedEmptyPagingData())

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-013 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setMemoTab(pagingData = loadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-014 대상 장소를 조회하지 못해도 빈 상태 안내를 표시한다`() {
        setScreenOnMemoTab(memoPagingData = loadedEmptyPagingData(), uiState = PlaceDetailUiState.Loading)

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-015 빈 상태에서도 메모 추가를 실행할 수 있다`() {
        var navigateToMemoAddCount = 0
        setScreenOnMemoTab(memoPagingData = loadedEmptyPagingData(), navigateToMemoAdd = { navigateToMemoAddCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()

        navigateToMemoAddCount shouldBe 1
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-017 대상 장소를 조회하지 못해도 메모 추가를 실행할 수 있다`() {
        setScreenOnMemoTab(memoPagingData = loadedEmptyPagingData(), uiState = PlaceDetailUiState.Loading)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).assertExists()
    }

    private fun setMemoTab(pagingData: PagingData<MemoListItem>) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                PlaceDetailMemoTab(
                    onEvent = {},
                    onMemoListEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
    }

    private fun setScreenOnMemoTab(
        memoPagingData: PagingData<MemoListItem>,
        uiState: PlaceDetailUiState = content(id = FIRST_PLACE_ID, detail = placeDetail(title = CONTACT_NAME)),
        navigateToMemoAdd: () -> Unit = {},
    ) {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(uiState)),
            memoPagingData = memoPagingData,
            navigateToMemoAdd = navigateToMemoAdd,
        )
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
    }

    private companion object {
        const val CONTACT_NAME = "PlaceDetailMemoEmptyName"
        const val DEFAULT_EMPTY_TITLE = "No memos linked to this place"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a memo."
        const val KOREAN_EMPTY_TITLE = "이 장소에 연결된 메모가 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 메모를 만들 수 있습니다"

        fun loadedEmptyPagingData(): PagingData<MemoListItem> =
            PagingData.from(
                data = emptyList(),
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
            )

        fun loadingPagingData(): PagingData<MemoListItem> =
            PagingData.from(
                data = emptyList(),
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.Loading,
                        prepend = LoadState.NotLoading(endOfPaginationReached = false),
                        append = LoadState.NotLoading(endOfPaginationReached = false),
                    ),
            )
    }
}
