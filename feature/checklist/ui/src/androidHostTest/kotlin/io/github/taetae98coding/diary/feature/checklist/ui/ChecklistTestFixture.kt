package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey

internal val checklistTestFixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

internal fun testChecklistSummary(
    titlePrefix: String = "",
    checkedCount: Int = 1,
    totalCount: Int = 3,
): ChecklistSummary {
    val detail = checklistTestFixtureMonkey.checklistDetail()

    return ChecklistSummary(
        checklist = checklistTestFixtureMonkey.checklist(isDeleted = false, detail = detail.copy(title = titlePrefix + detail.title)),
        checkedCount = checkedCount,
        totalCount = totalCount,
    )
}

internal fun checklistPagingDataOf(summaryList: List<ChecklistSummary>): PagingData<ChecklistSummary> =
    PagingData.from(
        data = summaryList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun loadingChecklistPagingData(): PagingData<ChecklistSummary> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun failedChecklistPagingData(): PagingData<ChecklistSummary> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Error(IllegalStateException(checklistTestFixtureMonkey.giveMeOne<String>())),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

// 화면에 보이는 목록은 하나이므로 당겨서 새로고침 영역 안의 스크롤 목록을 찾는다.
internal fun SemanticsNodeInteractionsProvider.refreshableList(): SemanticsNodeInteraction = onNode(hasScrollToIndexAction() and hasAnyAncestor(hasTestTag(PULL_TO_REFRESH_TEST_TAG)))

internal fun SemanticsNodeInteractionsProvider.actionNameList(): List<String> =
    onAllNodes(hasClickAction() or hasSetTextAction())
        .fetchSemanticsNodes()
        .map { node ->
            node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                ?: node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .joinToString { text -> text.text }
        }

internal fun SemanticsNodeInteractionsProvider.visibleTextList(): List<String> =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }

internal fun SemanticsNodeInteraction.editableText(): String = fetchSemanticsNode().config[SemanticsProperties.EditableText].text

internal fun SemanticsNodeInteractionsProvider.onInputWithLabel(label: String): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(label))

// 입력 칸을 거치지 않고 상태를 바꾸므로, 프레임이 상태 변경을 알리는 것과 같게 변경 직후 알림을 보낸다.
internal fun ComposeTestRule.write(action: () -> Unit) {
    runOnIdle {
        action()
        Snapshot.sendApplyNotifications()
    }
}
