package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagMemoFinishedListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-010 완료된 메모가 하나도 없어도 화면을 사용할 수 있다`() {
        val viewModel = mockk<TagMemoFinishedListViewModel>()
        every { viewModel.sort } returns MutableStateFlow(ListSort.DEFAULT)
        every { viewModel.memoPagingData } returns MutableStateFlow(tagMemoPagingData(itemList = emptyList()))
        every { viewModel.effect } returns Channel<TagMemoFinishedListEffect>(capacity = Channel.BUFFERED).receiveAsFlow()
        setTagMemoFinishedListScreen(
            viewModel = viewModel,
            uiState = TagMemoFinishedListUiState(title = TAG_TITLE),
        )

        composeRule.onNodeWithText(TAG_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_SUBTITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-016 다시 시작하면 카드가 사라지고 기본 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setTagMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restart(id = environment.memo.id) }
        assertMemoIsNotDisplayed(title = environment.memo.detail.title)
        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-016 다시 시작하면 한국어 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setTagMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_RESTARTED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-017 삭제하면 카드가 사라지고 기본 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setTagMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.delete(id = environment.memo.id) }
        assertMemoIsNotDisplayed(title = environment.memo.detail.title)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-017 삭제하면 한국어 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setTagMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-018 다시 시작 실행 취소는 메모 완료를 요청한다`() {
        val environment = screenTestEnvironment()
        setTagMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.finish(id = environment.memo.id) }
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-018 삭제 실행 취소는 메모 복구를 요청한다`() {
        val environment = screenTestEnvironment()
        setTagMemoFinishedListScreen(environment.viewModel)
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.memo.id) }
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-019 메모를 선택하면 그 메모의 상세로 이동한다`() {
        val environment = screenTestEnvironment()
        var navigatedMemoId: Uuid? = null
        setTagMemoFinishedListScreen(
            viewModel = environment.viewModel,
            navigateToMemoDetail = { id -> navigatedMemoId = id },
        )
        waitUntilMemoIsDisplayed(title = environment.memo.detail.title)

        composeRule.onNodeWithText(environment.memo.detail.title).performClick()

        navigatedMemoId shouldBe environment.memo.id
    }

    @Test
    fun `TC-TAG-MEMO-FINISHED-LIST-FEATURE-011 뒤로가기 버튼을 선택하면 화면을 떠난다`() {
        val environment = screenTestEnvironment()
        var navigateUpCount = 0
        setTagMemoFinishedListScreen(
            viewModel = environment.viewModel,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    private fun setTagMemoFinishedListScreen(
        viewModel: TagMemoFinishedListViewModel,
        uiState: TagMemoFinishedListUiState = TagMemoFinishedListUiState(),
        navigateUp: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
    ) {
        every { viewModel.uiState } returns MutableStateFlow(uiState)

        composeRule.setContent {
            DiaryTheme {
                TagMemoFinishedListScreen(
                    navigateUp = navigateUp,
                    navigateToMemoDetail = navigateToMemoDetail,
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
    }

    private fun assertMemoIsNotDisplayed(title: String) {
        composeRule
            .onNodeWithText(
                text = title,
                useUnmergedTree = true,
            ).assertIsNotDisplayed()
    }

    private fun waitUntilMemoIsDisplayed(title: String) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun screenTestEnvironment(): ScreenTestEnvironment {
        val memo = tagMemo(title = SCREEN_MEMO_TITLE)
        val pagingData =
            MutableStateFlow(
                tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            )
        val effectChannel = Channel<TagMemoFinishedListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<TagMemoFinishedListViewModel>()
        every { viewModel.sort } returns MutableStateFlow(ListSort.DEFAULT)

        every { viewModel.memoPagingData } returns pagingData
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.restart(id = memo.id) } answers {
            pagingData.value = tagMemoPagingData(itemList = emptyList())
            effectChannel.trySend(TagMemoFinishedListEffect.Restarted(id = memo.id)).getOrThrow()
        }
        every { viewModel.delete(id = memo.id) } answers {
            pagingData.value = tagMemoPagingData(itemList = emptyList())
            effectChannel.trySend(TagMemoFinishedListEffect.Deleted(id = memo.id)).getOrThrow()
        }
        every { viewModel.finish(id = memo.id) } answers {
            pagingData.value = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo)))
        }
        every { viewModel.restore(id = memo.id) } answers {
            pagingData.value = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo)))
        }

        return ScreenTestEnvironment(
            memo = memo,
            viewModel = viewModel,
        )
    }

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val TAG_TITLE = "📌 TagTitle"
        const val DEFAULT_SUBTITLE = "Finished Memos"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_RESTARTED_MESSAGE = "Memo restarted."
        const val KOREAN_RESTARTED_MESSAGE = "메모를 다시 시작했습니다."
        const val DEFAULT_DELETED_MESSAGE = "Memo deleted."
        const val KOREAN_DELETED_MESSAGE = "메모가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val SCREEN_MEMO_TITLE = "TagMemoFinishedListScreenMemo"
    }
}

private data class ScreenTestEnvironment(
    val memo: Memo,
    val viewModel: TagMemoFinishedListViewModel,
)
