package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoHomeFilterScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-HOME-FEATURE-059 유무 필터 축을 바꾸고 좁힌 목록이 놓이면 목록을 처음부터 다시 본다`() {
        val allItemList = memoItemList(ALL_MEMO_COUNT)
        val filterUiState = mutableStateOf(MemoHomeScaffoldFilterUiState())
        val memoPagingData = MutableStateFlow(memoPagingDataOf(allItemList))
        setMemoHomeScaffold(filterUiState = filterUiState, memoPagingData = memoPagingData)

        scrollToLast(itemList = allItemList)
        changeFilter(
            filterUiState = filterUiState,
            value = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(place = MemoFilterExistence.EXIST)),
        )
        place(memoPagingData = memoPagingData, itemList = allItemList.take(FILTERED_MEMO_COUNT))

        composeRule.onNodeWithText(allItemList.first().title()).assertExists()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-060 태그를 선택하고 좁힌 목록이 놓이면 목록을 처음부터 다시 본다`() {
        val tagIdList = tagIdList()

        assertScrolledToTop(
            before = MemoHomeScaffoldFilterUiState(),
            after = MemoHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagIdList.first())),
        )
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-060 태그 선택을 해제하고 넓힌 목록이 놓이면 목록을 처음부터 다시 본다`() {
        val tagIdList = tagIdList()

        assertScrolledToTop(
            before = MemoHomeScaffoldFilterUiState(selectedTagIdSet = tagIdList.toSet()),
            after = MemoHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagIdList.first())),
        )
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-060 태그 선택을 전체 해제하고 넓힌 목록이 놓이면 목록을 처음부터 다시 본다`() {
        val tagIdList = tagIdList()

        assertScrolledToTop(
            before = MemoHomeScaffoldFilterUiState(selectedTagIdSet = tagIdList.toSet()),
            after = MemoHomeScaffoldFilterUiState(),
        )
    }

    @Test
    fun `TC-MEMO-HOME-DOMAIN-017 이미 고른 값을 다시 골라도 목록 위치를 유지한다`() {
        val allItemList = memoItemList(ALL_MEMO_COUNT)
        val applied = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(place = MemoFilterExistence.EXIST))
        val filterUiState = mutableStateOf(applied)
        val memoPagingData = MutableStateFlow(memoPagingDataOf(allItemList))
        setMemoHomeScaffold(filterUiState = filterUiState, memoPagingData = memoPagingData)

        scrollToLast(itemList = allItemList)
        changeFilter(filterUiState = filterUiState, value = applied)
        place(memoPagingData = memoPagingData, itemList = allItemList + memoItem(title = "${TITLE_PREFIX}Added"))

        composeRule.onNodeWithText(allItemList.first().title()).assertDoesNotExist()
    }

    private fun assertScrolledToTop(
        before: MemoHomeScaffoldFilterUiState,
        after: MemoHomeScaffoldFilterUiState,
    ) {
        val allItemList = memoItemList(ALL_MEMO_COUNT)
        val filterUiState = mutableStateOf(before)
        val memoPagingData = MutableStateFlow(memoPagingDataOf(allItemList))
        setMemoHomeScaffold(filterUiState = filterUiState, memoPagingData = memoPagingData)

        scrollToLast(itemList = allItemList)
        changeFilter(filterUiState = filterUiState, value = after)
        place(memoPagingData = memoPagingData, itemList = allItemList.take(FILTERED_MEMO_COUNT))

        composeRule.onNodeWithText(allItemList.first().title()).assertExists()
    }

    // 필터를 바꾸기 전에 사용자가 첫 메모 카드가 보이지 않는 자리까지 이동해 둔 상태를 만든다.
    private fun scrollToLast(itemList: List<MemoListItem>) {
        composeRule.onNodeWithTag(MEMO_HOME_LIST_TEST_TAG).performScrollToIndex(itemList.lastIndex)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(itemList.first().title()).assertDoesNotExist()
    }

    private fun changeFilter(
        filterUiState: MutableState<MemoHomeScaffoldFilterUiState>,
        value: MemoHomeScaffoldFilterUiState,
    ) {
        composeRule.runOnIdle { filterUiState.value = value }
        composeRule.waitForIdle()
    }

    // 필터를 바꾼 뒤 한 박자 늦게 놓이는 목록을 만든다.
    private fun place(
        memoPagingData: MutableStateFlow<PagingData<MemoListItem>>,
        itemList: List<MemoListItem>,
    ) {
        composeRule.runOnIdle { memoPagingData.value = memoPagingDataOf(itemList) }
        composeRule.waitForIdle()
    }

    private fun setMemoHomeScaffold(
        filterUiState: MutableState<MemoHomeScaffoldFilterUiState>,
        memoPagingData: MutableStateFlow<PagingData<MemoListItem>>,
    ) {
        composeRule.setContent {
            DiaryTheme {
                // MemoHomeScreen이 collectAsStateWithLifecycle로 읽어 넘기는 것과 같은 흐름을 만든다.
                val currentFilterUiState by filterUiState

                MemoHomeScaffold(
                    onEvent = {},
                    onMemoListEvent = {},
                    memoPagingItems = memoPagingData.collectAsLazyPagingItems(),
                    filterUiStateProvider = { currentFilterUiState },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val ALL_MEMO_COUNT = 100
        private const val FILTERED_MEMO_COUNT = 60
        private const val TITLE_PREFIX = "ScrollMemoTitle"
        private const val SELECTED_TAG_COUNT = 2

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun MemoListItem.title(): String = (this as MemoListItem.Content).memo.detail.title

        private fun tagIdList(): List<Uuid> = List(SELECTED_TAG_COUNT) { fixtureMonkey.giveMeOne<Uuid>() }

        private fun memoItemList(count: Int): List<MemoListItem> = List(count) { index -> memoItem(title = "$TITLE_PREFIX$index") }

        private fun memoItem(title: String): MemoListItem = MemoListItem.Content(memo = memo(title = title))

        private fun memo(title: String): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = null))
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
