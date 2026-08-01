package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.rememberMemoListState
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
        private const val TIMEOUT_MILLIS = 5_000L

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(title: String): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = null))
                .setExp(Memo::isFinished, true)
                .setExp(Memo::isDeleted, false)
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
