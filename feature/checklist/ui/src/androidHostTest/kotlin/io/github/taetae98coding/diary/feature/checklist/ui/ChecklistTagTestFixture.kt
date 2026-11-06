package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagSelectionUiState
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.tag.api.TagAddedResult
import io.github.taetae98coding.diary.feature.tag.api.tagAddedResultKey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.uuid.Uuid

internal val TEST_TAG_ADD_REQUEST_KEY: Uuid = Uuid.parse("40000000-0000-0000-0000-000000000001")

internal const val DEFAULT_ENTITY_TAG_LABEL = "Select tag"
internal const val DEFAULT_ENTITY_TAG_PICKER_TITLE = "Select Tag"

// TagAdd 화면은 자기를 연 태그 입력의 요청 키로만 결과를 돌려주므로, 테스트도 같은 키로 추가 결과를 보낸다.
internal fun ResultEventBus.sendTagAddedResult(
    id: Uuid,
    requestKey: Uuid = TEST_TAG_ADD_REQUEST_KEY,
) {
    sendResult(resultKey = tagAddedResultKey(requestKey = requestKey), result = TagAddedResult(id = id))
}

internal fun addTagScreenTestViewModel(tagList: List<Tag> = emptyList()): EntityAddTagViewModel {
    val viewModel = mockk<EntityAddTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    every { viewModel.selectionUiState } returns MutableStateFlow(EntityAddTagSelectionUiState(tagIdSet = tagList.map { tag -> tag.id }.toSet()))
    every { viewModel.tagPagingData } returns MutableStateFlow(PagingData.from(tagList))
    every { viewModel.selectableTagPagingData } returns MutableStateFlow(PagingData.from(tagList))
    return viewModel
}

internal fun detailTagScreenTestViewModel(
    tagList: List<Tag> = emptyList(),
    selectableTagList: List<Tag> = emptyList(),
): EntityDetailTagViewModel {
    val viewModel = mockk<EntityDetailTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    every { viewModel.tagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
    every { viewModel.selectableTagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
    return viewModel
}

// 태그 입력은 본문의 마지막 입력이라 화면 아래에 놓일 수 있으므로, 누르는 위치와 무관한 클릭 동작으로 누른다.
internal fun ComposeContentTestRule.clickEntityTagInput() {
    onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

// 태그 입력은 TagAdd 결과를 [LocalResultEventBus]로 받으므로, 화면을 직접 배치하는 테스트는 이 테마로 감싼다.
@Composable
internal fun ChecklistTagTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}
