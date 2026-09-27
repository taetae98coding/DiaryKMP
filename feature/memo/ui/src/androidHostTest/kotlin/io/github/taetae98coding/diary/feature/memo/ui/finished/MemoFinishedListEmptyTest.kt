package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.home.loadingMemoPagingData
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoFinishedListEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-022 완료된 메모가 없으면 빈 상태 안내를 표시한다`() {
        setMemoFinishedListScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-FINISHED-LIST-FEATURE-022 한국어 환경에서 빈 상태 안내는 완료한 메모가 없습니다이다`() {
        setMemoFinishedListScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-022 빈 상태에 메모 추가를 권하는 안내를 두지 않는다`() {
        setMemoFinishedListScaffold(MutableStateFlow(memoPagingDataOf(emptyList())))

        composeRule.onNodeWithText(MEMO_HOME_EMPTY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-023 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setMemoFinishedListScaffold(MutableStateFlow(loadingMemoPagingData()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-DATA-005 처음 불러오기에 실패하면 오류 안내와 다시 시도 없이 빈 상태 안내를 표시한다`() {
        setMemoFinishedListScaffold(
            MutableStateFlow(
                memoPagingDataWithLoadStates(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                ),
            ),
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-DATA-005 이어서 불러오기에 실패해도 앞서 불러온 메모를 그대로 표시한다`() {
        setMemoFinishedListScaffold(
            MutableStateFlow(
                memoPagingDataWithLoadStates(
                    itemList = listOf(MemoListItem.Content(memo = memo(title = MEMO_TITLE))),
                    append = LoadState.Error(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                ),
            ),
        )
        waitUntilTextDisplayed(MEMO_TITLE)

        composeRule.onNodeWithText(MEMO_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(MEMO_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    private fun memoPagingDataWithLoadStates(
        itemList: List<MemoListItem>,
        refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
        append: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
    ): PagingData<MemoListItem> =
        PagingData.from(
            data = itemList,
            sourceLoadStates =
                LoadStates(
                    refresh = refresh,
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = append,
                ),
        )

    private fun waitUntilTextDisplayed(text: String) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setMemoFinishedListScaffold(memoPagingDataFlow: MutableStateFlow<PagingData<MemoListItem>>) {
        composeRule.setContent {
            DiaryTheme {
                MemoFinishedListScaffold(
                    memoListState = rememberMemoListState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = {},
                    onMemoListEvent = {},
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_EMPTY_TITLE = "No finished memos"
        private const val KOREAN_EMPTY_TITLE = "완료한 메모가 없습니다"
        private const val MEMO_HOME_EMPTY_DESCRIPTION = "Use the add button to create a memo."
        private const val MEMO_TITLE = "FinishedEmptyStateMemoTitle"
        private const val DEFAULT_RETRY_TEXT = "Retry"
        private const val TIMEOUT_MILLIS = 5_000L

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(title: String): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = null))
                .setExp(Memo::isFinished, true)
                .setExp(Memo::isDeleted, false)
                .setExp(Memo::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Memo::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
