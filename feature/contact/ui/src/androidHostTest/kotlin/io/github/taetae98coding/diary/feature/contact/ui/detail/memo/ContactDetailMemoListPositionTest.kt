package io.github.taetae98coding.diary.feature.contact.ui.detail.memo

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.contact.ui.detail.contactMemo
import io.github.taetae98coding.diary.feature.contact.ui.detail.contactMemoPagingData
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContactDetailMemoListPositionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-DOMAIN-006 앱이 백그라운드에 다녀와도 메모 탭에서 보던 목록 위치가 유지된다`() {
        val titleList = positionMemoTitleList()
        val pagingDataFlow = pagingDataFlowOf(titleList)
        composeRule.setContent { MemoTab(pagingDataFlow = pagingDataFlow) }
        scrollToLast(titleList)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertScrolledToLast(titleList)
    }

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-DOMAIN-007 시스템이 앱을 정리했다가 되살려도 메모 탭에서 보던 목록 위치를 복원한다`() {
        val titleList = positionMemoTitleList()
        val pagingDataFlow = pagingDataFlowOf(titleList)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { MemoTab(pagingDataFlow = pagingDataFlow) }
        scrollToLast(titleList)

        // 되살린 화면은 목록을 처음부터 다시 받으므로 조회 결과도 새로 전달한다.
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { pagingDataFlow.value = memoPagingData(titleList) }
        waitUntilMemoIsDisplayed(title = titleList.last())

        assertScrolledToLast(titleList)
    }

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-DOMAIN-005 상세 화면을 떠난 뒤 다시 들어오면 메모 탭 목록을 맨 위부터 보여 준다`() {
        val titleList = positionMemoTitleList()
        val pagingDataFlow = pagingDataFlowOf(titleList)
        var isDetailDisplayed by mutableStateOf(true)
        composeRule.setContent {
            if (isDetailDisplayed) MemoTab(pagingDataFlow = pagingDataFlow)
        }
        scrollToLast(titleList)

        composeRule.runOnIdle { isDetailDisplayed = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isDetailDisplayed = true }
        waitUntilMemoIsDisplayed(title = titleList.first())

        composeRule.onNodeWithText(titleList.first()).assertIsDisplayed()
        composeRule.onNodeWithText(titleList.last()).assertIsNotDisplayed()
    }

    @Composable
    private fun MemoTab(pagingDataFlow: MutableStateFlow<PagingData<MemoListItem>>) {
        DiaryTheme {
            ContactDetailMemoTab(
                onEvent = {},
                onMemoListEvent = {},
                modifier = Modifier.fillMaxSize(),
                memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
            )
        }
    }

    private fun scrollToLast(titleList: List<String>) {
        waitUntilMemoIsDisplayed(title = titleList.first())
        composeRule.onNodeWithTag(CONTACT_DETAIL_MEMO_LIST_TEST_TAG).performScrollToNode(hasText(titleList.last()))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(titleList.last()).assertIsDisplayed()
    }

    private fun assertScrolledToLast(titleList: List<String>) {
        composeRule.onNodeWithText(titleList.last()).assertIsDisplayed()
        composeRule.onNodeWithText(titleList.first()).assertIsNotDisplayed()
    }

    private fun waitUntilMemoIsDisplayed(title: String) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun pagingDataFlowOf(titleList: List<String>): MutableStateFlow<PagingData<MemoListItem>> = MutableStateFlow(memoPagingData(titleList))

    private fun memoPagingData(titleList: List<String>): PagingData<MemoListItem> = contactMemoPagingData(itemList = titleList.map { title -> MemoListItem.Content(memo = contactMemo(title = title)) })

    private fun positionMemoTitleList(): List<String> = List(POSITION_MEMO_COUNT) { index -> "$POSITION_MEMO_TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}" }

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val POSITION_MEMO_COUNT = 30
        const val POSITION_MEMO_TITLE_PREFIX = "ContactMemoPosition"
    }
}
