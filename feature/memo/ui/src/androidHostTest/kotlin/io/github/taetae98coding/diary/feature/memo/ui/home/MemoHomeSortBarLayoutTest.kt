package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoHomeSortBarLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `정렬 컨트롤의 시작 쪽 끝은 목록 항목의 시작 쪽 끝과 같은 자리에 놓인다`() {
        setMemoHomeScaffold()

        val memoBounds = composeRule.onNodeWithText(MEMO_TITLE).getBoundsInRoot()
        val sortBounds = composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).getBoundsInRoot()

        sortBounds.left shouldBe memoBounds.left
    }

    @Test
    fun `완료된 목록 진입 버튼의 끝 쪽 끝은 목록 항목의 끝 쪽 끝과 같은 자리에 놓인다`() {
        setMemoHomeScaffold()

        val memoBounds = composeRule.onNodeWithText(MEMO_TITLE).getBoundsInRoot()
        val finishedListBounds = composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).getBoundsInRoot()

        finishedListBounds.right shouldBe memoBounds.right
    }

    private fun setMemoHomeScaffold() {
        val memoPagingData = MutableStateFlow(memoPagingDataOf(listOf(MemoListItem.Content(memo = memo(title = MEMO_TITLE)))))

        composeRule.setContent {
            DiaryTheme {
                MemoHomeScaffold(
                    onEvent = {},
                    onMemoListEvent = {},
                    memoPagingItems = memoPagingData.collectAsLazyPagingItems(),
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun memo(title: String): Memo =
        fixtureMonkey
            .giveMeKotlinBuilder<Memo>()
            .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = null))
            .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
            .sample()

    private companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val MEMO_TITLE = "SortBarLayoutMemo"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
