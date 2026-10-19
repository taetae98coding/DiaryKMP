package io.github.taetae98coding.diary.feature.core.memo.finished

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun entityMemo(
    title: String,
    dateTime: MemoDateTime? = null,
): Memo = fixtureMonkey.memo(title = title, dateTime = dateTime)

internal fun entityMemoPagingData(
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

internal fun fixtureText(prefix: String): String = prefix + fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)

internal fun fixtureId(): Uuid = fixtureMonkey.giveMeOne<Uuid>()

// 화면에 보이는 목록은 하나이므로 당겨서 새로고침 영역 안의 스크롤 목록을 찾는다.
internal fun SemanticsNodeInteractionsProvider.refreshableList(): SemanticsNodeInteraction = onNode(hasScrollToIndexAction() and hasAnyAncestor(hasTestTag(PULL_TO_REFRESH_TEST_TAG)))

internal fun screenTestSyncViewModel(uiState: SyncRefreshUiState = SyncRefreshUiState()): SyncRefreshViewModel {
    val viewModel = mockk<SyncRefreshViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}
