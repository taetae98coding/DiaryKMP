package io.github.taetae98coding.diary.feature.contact.ui.memo.finished

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_memo_finished_list_empty_title
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListScaffold
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContactMemoFinishedListEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-9f3e57db-9d45-4aa0-af87-c433fffac21f ContactMemoFinishedList의 빈 상태 안내를 기본 환경 문구로 표시한다`() {
        setEmptyScaffold()

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-9f3e57db-9d45-4aa0-af87-c433fffac21f ContactMemoFinishedList의 빈 상태 안내를 한국어 문구로 표시한다`() {
        setEmptyScaffold()

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
    }

    private fun setEmptyScaffold() {
        val pagingDataFlow =
            MutableStateFlow(
                PagingData.from(
                    data = emptyList<MemoListItem>(),
                    sourceLoadStates =
                        LoadStates(
                            refresh = LoadState.NotLoading(endOfPaginationReached = true),
                            prepend = LoadState.NotLoading(endOfPaginationReached = true),
                            append = LoadState.NotLoading(endOfPaginationReached = true),
                        ),
                ),
            )

        composeRule.setContent {
            DiaryTheme {
                EntityMemoFinishedListScaffold(
                    onEvent = {},
                    onMemoListEvent = {},
                    emptyTitle = stringResource(Res.string.contact_memo_finished_list_empty_title),
                    memoPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_EMPTY_TITLE = "No finished memos for this contact"
        const val KOREAN_EMPTY_TITLE = "이 연락처에서 완료한 메모가 없습니다"
    }
}
