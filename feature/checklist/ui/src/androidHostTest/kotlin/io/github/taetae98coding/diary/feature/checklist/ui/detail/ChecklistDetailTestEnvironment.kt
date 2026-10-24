package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.feature.checklist.ui.checklistTestFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.uuid.Uuid

internal class ChecklistDetailTestEnvironment(
    detail: ChecklistDetail = ChecklistDetail(title = TRIP, description = WEEKEND),
    itemList: List<ChecklistItem> = emptyList(),
    isLoaded: Boolean = true,
    val id: Uuid = Uuid.random(),
) {
    val uiState: MutableStateFlow<ChecklistDetailUiState> = MutableStateFlow(ChecklistDetailUiState.Content(id = id, detail = detail))
    val itemUiState: MutableStateFlow<ChecklistDetailItemUiState> = MutableStateFlow(ChecklistDetailItemUiState(itemList = itemList, isLoaded = isLoaded))
    val effect: Channel<ChecklistDetailEffect> = Channel(Channel.BUFFERED)
    val itemEffect: Channel<ChecklistDetailItemEffect> = Channel(Channel.BUFFERED)

    val checklistViewModel: ChecklistDetailViewModel =
        mockk<ChecklistDetailViewModel>(relaxed = true).apply {
            every { uiState } returns this@ChecklistDetailTestEnvironment.uiState
            every { effect } returns this@ChecklistDetailTestEnvironment.effect.receiveAsFlow()
        }

    val itemViewModel: ChecklistDetailItemViewModel =
        mockk<ChecklistDetailItemViewModel>(relaxed = true).apply {
            every { uiState } returns this@ChecklistDetailTestEnvironment.itemUiState
            every { effect } returns this@ChecklistDetailTestEnvironment.itemEffect.receiveAsFlow()
        }

    companion object {
        const val TRIP = "여행 준비"
        const val WEEKEND = "주말"
        const val PASSPORT = "여권"
        const val CHARGER = "충전기"
        const val UMBRELLA = "우산"
    }
}

internal fun item(
    content: String,
    isChecked: Boolean = false,
): ChecklistItem =
    checklistTestFixtureMonkey.checklistItem(
        isChecked = isChecked,
        isDeleted = false,
        content = content,
    )

internal fun SemanticsNodeInteractionsProvider.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[0]

internal fun SemanticsNodeInteractionsProvider.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[1]

internal fun SemanticsNodeInteractionsProvider.itemInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[2]

internal fun itemRow(content: String): SemanticsMatcher = isToggleable() and hasText(content)

internal fun SemanticsNodeInteractionsProvider.rowContentList(): List<String> =
    onAllNodes(isToggleable())
        .fetchSemanticsNodes()
        .map { node -> node.config[SemanticsProperties.Text].joinToString { text -> text.text } }

internal fun removeButton(content: String): SemanticsMatcher = hasContentDescription("Remove item, $content")

internal fun progressIndicator(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
