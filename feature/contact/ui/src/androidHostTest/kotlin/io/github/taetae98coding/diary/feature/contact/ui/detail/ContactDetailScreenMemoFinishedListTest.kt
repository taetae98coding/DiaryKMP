package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContactDetailScreenMemoFinishedListTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-FEATURE-028 완료된 메모 확인을 선택하면 완료된 메모 목록 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnMemoTab(navigateToMemoFinishedList = { navigateCount += 1 })

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertHasClickAction().performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CONTACT-DETAIL-MEMO-FEATURE-028 한국어 환경 완료된 메모 확인 라벨을 표시한다`() {
        setScreenOnMemoTab(memoTabDescription = KOREAN_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(KOREAN_FINISHED_LIST_LABEL).assertHasClickAction()
    }

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-FEATURE-029 연결된 메모가 표시되어 있어도 완료된 메모 확인을 실행할 수 있다`() {
        val memo = contactMemo(title = MEMO_TITLE)
        var navigateCount = 0
        setScreenOnMemoTab(
            memoPagingData = contactMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            navigateToMemoFinishedList = { navigateCount += 1 },
        )
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memo.detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-FEATURE-029 TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-024 빈 상태와 대상 조회 상태와 무관하게 완료된 메모 확인을 실행할 수 있다`() {
        val uiStateList = listOf(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = testContactDetail()), ContactDetailUiState.Loading)
        val uiStateFlow = MutableStateFlow<ContactDetailUiState>(uiStateList.first())
        var navigateCount = 0
        setScreenOnMemoTab(uiStateFlow = uiStateFlow, navigateToMemoFinishedList = { navigateCount += 1 })

        uiStateList.forEachIndexed { index, uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

            navigateCount shouldBe index + 1
        }
    }

    @Test
    fun `TC-CONTACT-DETAIL-MEMO-FEATURE-030 연락처 디테일 탭에서는 완료된 메모 확인을 제공하지 않고 메모 탭으로 전환하면 제공한다`() {
        var navigateCount = 0
        val uiStateFlow = MutableStateFlow<ContactDetailUiState>(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = testContactDetail()))
        composeRule.setContactDetailScreen(viewModel = screenTestViewModel(uiStateFlow), navigateToMemoFinishedList = { navigateCount += 1 })
        composeRule.selectContactDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()

        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    private fun setScreenOnMemoTab(
        memoPagingData: PagingData<MemoListItem> = contactMemoPagingData(itemList = emptyList()),
        memoTabDescription: String = DEFAULT_MEMO_TAB_DESCRIPTION,
        uiStateFlow: MutableStateFlow<ContactDetailUiState> = MutableStateFlow(ContactDetailUiState.Content(id = FIRST_CONTACT_ID, detail = testContactDetail())),
        navigateToMemoFinishedList: () -> Unit = {},
    ) {
        composeRule.setContactDetailScreen(
            viewModel = screenTestViewModel(uiStateFlow),
            memoPagingData = memoPagingData,
            navigateToMemoFinishedList = navigateToMemoFinishedList,
        )
        composeRule.selectContactDetailTab(memoTabDescription)
    }

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val MEMO_TITLE = "ContactDetailFinishedEntryMemo"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val KOREAN_FINISHED_LIST_LABEL = "완료된 메모"
    }
}
