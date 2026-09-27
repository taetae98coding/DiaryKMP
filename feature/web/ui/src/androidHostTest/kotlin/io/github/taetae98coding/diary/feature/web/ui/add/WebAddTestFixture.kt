package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailTagViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val TITLE_INPUT_INDEX = 0
internal const val DESCRIPTION_INPUT_INDEX = 1
internal const val URL_INPUT_INDEX = 2
internal const val HEADER_NAME_INPUT_INDEX = 3
internal const val HEADER_VALUE_INPUT_INDEX = 4
internal const val INPUT_COUNT_WITHOUT_HEADER = 3
internal const val INPUT_COUNT_PER_HEADER = 2

internal const val TYPED_TITLE = "WebTitleInput"
internal const val TYPED_DESCRIPTION = "WebDescriptionInput"
internal const val TYPED_URL = "https://developer.android.com"
internal const val TYPED_FIRST_HEADER_NAME = "Authorization"
internal const val TYPED_FIRST_HEADER_VALUE = "Bearer token"
internal const val TYPED_SECOND_HEADER_NAME = "Accept"
internal const val TYPED_SECOND_HEADER_VALUE = "application/json"

internal const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add web"
internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
internal const val DEFAULT_HEADER_ADD_BUTTON_DESCRIPTION = "Add header"
internal const val DEFAULT_HEADER_REMOVE_BUTTON_DESCRIPTION = "Remove header"

internal fun screenTestViewModel(
    effect: Flow<WebAddEffect> = emptyFlow(),
    uiState: WebAddUiState = WebAddUiState(),
): WebAddViewModel {
    val viewModel = mockk<WebAddViewModel>()
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    every { viewModel.effect } returns effect
    return viewModel
}

internal fun effectViewModel(effect: WebAddEffect): WebAddViewModel {
    val channel = Channel<WebAddEffect>(capacity = Channel.BUFFERED)
    val viewModel = screenTestViewModel(effect = channel.receiveAsFlow())
    every { viewModel.add(any(), tagIdSet = any()) } answers { channel.trySend(effect).getOrThrow() }
    return viewModel
}

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

internal fun ComposeContentTestRule.urlInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[URL_INPUT_INDEX]

internal fun ComposeContentTestRule.headerNameInput(row: Int = 0): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[HEADER_NAME_INPUT_INDEX + row * INPUT_COUNT_PER_HEADER]

internal fun ComposeContentTestRule.headerValueInput(row: Int = 0): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[HEADER_VALUE_INPUT_INDEX + row * INPUT_COUNT_PER_HEADER]

internal fun ComposeContentTestRule.addHeaderRow() {
    onNodeWithContentDescription(DEFAULT_HEADER_ADD_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.clickAdd() {
    onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.inputCount(): Int = onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size

internal fun ComposeContentTestRule.fillAllInput() {
    titleInput().performTextInput(TYPED_TITLE)
    descriptionInput().performTextInput(TYPED_DESCRIPTION)
    urlInput().performTextInput(TYPED_URL)
    addHeaderRow()
    headerNameInput().performTextInput(TYPED_FIRST_HEADER_NAME)
    headerValueInput().performTextInput(TYPED_FIRST_HEADER_VALUE)
    addHeaderRow()
    headerNameInput(row = 1).performTextInput(TYPED_SECOND_HEADER_NAME)
    headerValueInput(row = 1).performTextInput(TYPED_SECOND_HEADER_VALUE)
    waitForIdle()
}

internal fun addTagScreenTestViewModel(tagList: List<Tag> = emptyList()): WebAddTagViewModel {
    val viewModel = mockk<WebAddTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    every { viewModel.tagIdSet } returns MutableStateFlow(tagList.map { tag -> tag.id }.toSet())
    every { viewModel.tagPagingData } returns flowOf(PagingData.from(tagList))
    every { viewModel.selectableTagPagingData } returns flowOf(PagingData.from(tagList))
    return viewModel
}

internal fun webTestTag(title: String): Tag =
    Tag(
        id = Uuid.random(),
        detail = TagDetail(emoji = "", title = title, description = "", color = 0xFF3A7BD5),
        isFinished = false,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun detailTagScreenTestViewModel(
    tagList: List<Tag> = emptyList(),
    selectableTagList: List<Tag> = emptyList(),
): WebDetailTagViewModel {
    val viewModel = mockk<WebDetailTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    every { viewModel.tagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
    every { viewModel.selectableTagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
    return viewModel
}

/**
 * WebAdd 화면은 추가 결과를 [LocalResultEventBus]로 알리므로, 화면을 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun WebAddScreenTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}
