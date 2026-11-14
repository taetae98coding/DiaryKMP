package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.datetime.LocalDate

// 기기의 오늘이 언제든 진행 중이도록 시작 날짜를 충분히 앞에 둔다.
private val FILTER_CASE_IN_PROGRESS_START_DATE: LocalDate = LocalDate(year = 2000, month = 1, day = 1)

// 목록에 노출되는 진행 중인 루틴이다. 제목은 화면에서 글자로 찾으므로 테스트가 정한다.
internal fun filterCaseRoutineSummaryOf(
    title: String,
    cycleAmount: Long = 0L,
    transform: (RoutineDetail) -> RoutineDetail = { detail -> detail },
): RoutineSummary {
    val summary = Arb.routineSummary().next()

    return summary.copy(
        routine =
            summary.routine.copy(
                detail =
                    transform(
                        summary.routine.detail.copy(
                            title = title,
                            startDate = FILTER_CASE_IN_PROGRESS_START_DATE,
                            cycle = RoutineCycle.DAILY,
                            unit = "회",
                            goalAmount = null,
                            amountPerRecord = 1,
                        ),
                    ),
                isFinished = false,
                isDeleted = false,
            ),
        cycleAmount = cycleAmount,
    )
}

internal fun filterCaseRoutinePagingDataOf(
    summaryList: List<RoutineSummary>,
    isRefreshing: Boolean = false,
): PagingData<RoutineSummary> =
    PagingData.from(
        data = summaryList,
        sourceLoadStates =
            LoadStates(
                refresh = if (isRefreshing) LoadState.Loading else LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun filterCaseLoadingRoutinePagingData(): PagingData<RoutineSummary> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun filterCaseFailedRoutinePagingData(): PagingData<RoutineSummary> =
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
internal fun SemanticsNodeInteractionsProvider.filterCaseRefreshableList(): SemanticsNodeInteraction = onNode(hasScrollToIndexAction() and hasAnyAncestor(hasTestTag(PULL_TO_REFRESH_TEST_TAG)))

internal fun filterCaseRoutineCard(title: String): SemanticsMatcher = hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title)

// 격자는 행 우선으로 놓이므로 위에서 아래, 같은 줄에서는 시작에서 끝 순서로 읽은 카드의 제목이다.
internal fun SemanticsNodeInteractionsProvider.filterCaseCardTitleList(titleList: Collection<String>): List<String> =
    onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG))
        .fetchSemanticsNodes()
        .sortedWith(compareBy({ node -> node.boundsInRoot.top }, { node -> node.boundsInRoot.left }))
        .mapNotNull { node ->
            node.config
                .getOrNull(SemanticsProperties.Text)
                .orEmpty()
                .map { text -> text.text }
                .firstOrNull { text -> text in titleList }
        }

internal fun hasFilterCaseStateDescription(value: String): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)
