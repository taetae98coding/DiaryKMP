package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TAG_DETAIL_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 탭이 얻는 ViewModel 참조는 setTagDetailScreen이 테스트마다 비운 뒤 새로 채우므로, 이전 테스트의 호출 기록이 섞이지 않는다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailFormRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-061 태그 디테일 탭에서는 당겨도 새로고침을 요청하지 않는다`() {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            memoPagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = tagMemo(title = MEMO_TITLE)))),
        )
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()

        composeRule.titleInput().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        syncViewModelRef?.let { viewModel -> verify(exactly = 0) { viewModel.refresh() } }
        memoSyncViewModelRef?.let { viewModel -> verify(exactly = 0) { viewModel.refresh() } }
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()

        // 같은 화면에서 메모 탭을 당기면 새로고침이 요청되므로, 위의 결과는 당김이 관찰되지 않아서가 아니다.
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(memoSyncViewModelRef).refresh() }
    }

    private companion object {
        const val MEMO_TITLE = "TagDetailFormRefreshMemo"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
