package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TAG_DETAIL_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.detail.web.TAG_DETAIL_WEB_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagWeb
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.viewmodel.koinViewModel
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 내비게이션은 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태와 ViewModel만 보관한다.
// 이동한 화면이 위에 놓였다가 뒤로가기나 삭제 성공으로 닫히는 것을 같은 방식으로 재현한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailReturnTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var isTagDetailOnTop by mutableStateOf(true)
    private val destinationList = mutableListOf<String>()

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-033 메모 추가에서 뒤로가면 메모 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(MEMO_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-033 메모 상세에서 뒤로가면 메모 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(MEMO_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(MEMO_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(MEMO_TITLE).assertExists()
    }

    // 완료된 메모 목록의 뒤로가기가 그 목록과 상세를 함께 닫는 이력은 TagNavigationTest가, 넓은 창의 배치는 TagMemoFinishedListLayoutTest가 확인한다.
    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-017 TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-018 완료된 메모 목록이 닫히면 메모 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()
        composeRule.waitForIdle()
        assertLeftTo(MEMO_FINISHED_LIST)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(MEMO_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-020 웹 추가에서 뒤로가면 웹 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_WEB_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertLeftTo(WEB_ADD)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_WEB_LIST_TEST_TAG).assertExists()
    }

    // 웹 상세는 삭제에 성공하면 뒤로가기와 같은 닫기를 부른다(TC-WEB-DETAIL-FEATURE-020, WebDetailScreenTest).
    // TagDetail에서 본 두 경우의 결과는 위에 놓인 웹 상세가 닫히는 것으로 같다.
    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-020 웹 상세가 닫히면 웹 탭이 선택된 TagDetail로 돌아온다`() {
        setReturnableTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithText(WEB_TITLE).performClick()
        composeRule.waitForIdle()
        assertLeftTo(WEB_DETAIL)

        returnToTagDetail()

        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(WEB_TITLE).assertExists()
    }

    private fun assertLeftTo(destination: String) {
        destinationList.last() shouldBe destination
        composeRule.onNodeWithText(DESTINATION_CONTENT).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertDoesNotExist()
    }

    private fun returnToTagDetail() {
        composeRule.runOnIdle { isTagDetailOnTop = true }
        composeRule.waitForIdle()
    }

    private fun leaveTo(destination: String) {
        destinationList += destination
        isTagDetailOnTop = false
    }

    private fun setReturnableTagDetailScreen() {
        val detailViewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE))))
        prepareTagDetailTabViewModels(
            memoPagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = tagMemo(title = MEMO_TITLE)))),
            webPagingData = tagEntityPagingData(itemList = listOf(tagWeb(title = WEB_TITLE))),
        )

        composeRule.setContent {
            TagDetailScreenTestHost {
                val saveableStateHolder = rememberSaveableStateHolder()

                if (isTagDetailOnTop) {
                    saveableStateHolder.SaveableStateProvider(key = TAG_DETAIL_ENTRY_KEY) {
                        TagDetailScreen(
                            navigateToTagAdd = {},
                            tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                            navigateUp = {},
                            navigateToDetail = {},
                            navigateToMemoAdd = { leaveTo(MEMO_ADD) },
                            navigateToMemoDetail = { leaveTo(MEMO_DETAIL) },
                            navigateToMemoFinishedList = { leaveTo(MEMO_FINISHED_LIST) },
                            id = FIRST_TAG_ID,
                            componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                            detailViewModel = detailViewModel,
                            placeMapViewModel = koinViewModel(),
                            navigateToWebAdd = { leaveTo(WEB_ADD) },
                            navigateToWebDetail = { leaveTo(WEB_DETAIL) },
                            navigateToPlaceAdd = {},
                            navigateToPlaceDetail = {},
                        )
                    }
                } else {
                    Text(text = DESTINATION_CONTENT)
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TAG_DETAIL_ENTRY_KEY = "TagDetail"
        const val DESTINATION_CONTENT = "DestinationContent"
        const val MEMO_ADD = "MemoAdd"
        const val MEMO_DETAIL = "MemoDetail"
        const val MEMO_FINISHED_LIST = "TagMemoFinishedList"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val WEB_ADD = "WebAdd"
        const val WEB_DETAIL = "WebDetail"
        const val MEMO_TITLE = "TagDetailReturnMemo"
        const val WEB_TITLE = "TagDetailReturnWeb"
        const val DEFAULT_MEMO_ADD_DESCRIPTION = "Add memo"
        const val DEFAULT_WEB_ADD_DESCRIPTION = "Add web"
    }
}
