package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

// 목록에 노출되는 디데이다. 제목은 화면에서 글자로 찾으므로 테스트가 정한다.
internal fun homeDDayOf(
    title: String,
    transform: (DDayDetail) -> DDayDetail = { detail -> detail },
): DDay =
    Arb
        .dday()
        .next()
        .copy(
            detail = transform(Arb.ddayDetail().next().copy(title = title)),
            isDeleted = false,
        )

internal fun homeDDayPagingDataOf(
    ddayList: List<DDay>,
    isRefreshing: Boolean = false,
): PagingData<DDay> =
    PagingData.from(
        data = ddayList,
        sourceLoadStates =
            LoadStates(
                refresh = if (isRefreshing) LoadState.Loading else LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun loadingHomeDDayPagingData(): PagingData<DDay> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun failedHomeDDayPagingData(): PagingData<DDay> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Error(IllegalStateException(Arb.string().next())),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

// 화면에 보이는 목록은 하나이므로 당겨서 새로고침 영역 안의 스크롤 목록을 찾는다.
internal fun SemanticsNodeInteractionsProvider.homeRefreshableList(): SemanticsNodeInteraction = onNode(hasScrollToIndexAction() and hasAnyAncestor(hasTestTag(PULL_TO_REFRESH_TEST_TAG)))

internal fun SemanticsNodeInteractionsProvider.homeVisibleTextList(): List<String> =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }

internal fun SemanticsNodeInteractionsProvider.homeActionNameList(): List<String> =
    onAllNodes(hasClickAction())
        .fetchSemanticsNodes()
        .map { node ->
            node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                ?: node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .joinToString(separator = " ") { text -> text.text }
        }

// 격자는 행 우선으로 놓이므로 위에서 아래, 같은 줄에서는 시작에서 끝 순서로 읽은 카드의 제목이다.
internal fun SemanticsNodeInteractionsProvider.homeCardTitleList(titleList: Collection<String>): List<String> =
    onAllNodes(hasTestTag(DDAY_CARD_TEST_TAG))
        .fetchSemanticsNodes()
        .sortedWith(compareBy({ node -> node.boundsInRoot.top }, { node -> node.boundsInRoot.left }))
        .mapNotNull { node ->
            node.config
                .getOrNull(SemanticsProperties.Text)
                .orEmpty()
                .map { text -> text.text }
                .firstOrNull { text -> text in titleList }
        }
