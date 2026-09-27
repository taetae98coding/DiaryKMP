package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagPlace
import io.github.taetae98coding.diary.feature.tag.ui.tagWeb
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailCardSwipePriorityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-066 메모 카드 위에서 오른쪽으로 밀면 탭은 바뀌지 않고 그 메모가 완료된다`() {
        val memo = tagMemo(title = MEMO_TITLE)
        setScreen(tabDescription = DEFAULT_MEMO_TAB_DESCRIPTION, memo = memo)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(memoViewModelRef).finish(id = memo.id) }
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-066 웹 카드 위에서 오른쪽으로 밀면 탭은 바뀌지 않고 아무 상태 변경도 요청하지 않는다`() {
        setScreen(tabDescription = DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNode(hasTestTag(WEB_CARD_TEST_TAG) and hasText(WEB_TITLE)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        val webViewModel = requireNotNull(webViewModelRef)
        verify(exactly = 0) { webViewModel.delete(id = any()) }
        verify(exactly = 0) { webViewModel.restore(id = any()) }
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-066 장소 카드 위에서 오른쪽으로 밀면 탭은 바뀌지 않고 아무 상태 변경도 요청하지 않는다`() {
        setScreen(tabDescription = DEFAULT_PLACE_TAB_DESCRIPTION)

        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(PLACE_TITLE)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        val placeViewModel = requireNotNull(placeViewModelRef)
        verify(exactly = 0) { placeViewModel.delete(id = any()) }
        verify(exactly = 0) { placeViewModel.restore(id = any()) }
        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-067 카드 밖 여백에서 왼쪽으로 밀면 옆 탭으로 전환된다`() {
        setScreen(tabDescription = DEFAULT_MEMO_TAB_DESCRIPTION)

        // 카드는 목록 맨 위에 하나만 있으므로, 떠 있는 버튼을 피한 목록 아래쪽 여백을 가로질러 민다.
        composeRule.onNodeWithTag(TAG_DETAIL_PAGER_TEST_TAG).performTouchInput {
            val y = top + height * EMPTY_AREA_Y_FRACTION
            swipe(start = Offset(x = right - EDGE_PADDING, y = y), end = Offset(x = left + EDGE_PADDING, y = y))
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        val memoViewModel = requireNotNull(memoViewModelRef)
        verify(exactly = 0) { memoViewModel.finish(id = any()) }
        verify(exactly = 0) { memoViewModel.delete(id = any()) }
    }

    private fun setScreen(
        tabDescription: String,
        memo: Memo = tagMemo(title = MEMO_TITLE),
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            memoPagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            webPagingData = tagEntityPagingData(itemList = listOf(tagWeb(title = WEB_TITLE))),
            placePagingData = tagEntityPagingData(itemList = listOf(tagPlace(title = PLACE_TITLE))),
        )
        composeRule.selectTagDetailTab(tabDescription)
        val title =
            mapOf(
                DEFAULT_WEB_TAB_DESCRIPTION to WEB_TITLE,
                DEFAULT_PLACE_TAB_DESCRIPTION to PLACE_TITLE,
            ).getOrDefault(tabDescription, MEMO_TITLE)
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val MEMO_TITLE = "TagDetailSwipePriorityMemo"
        const val WEB_TITLE = "TagDetailSwipePriorityWeb"
        const val PLACE_TITLE = "TagDetailSwipePriorityPlace"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val EMPTY_AREA_Y_FRACTION = 0.6f
        const val EDGE_PADDING = 8f
    }
}
