package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.input.DIARY_EMOJI_INPUT_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

/**
 * TagAdd 화면은 결과 이벤트 버스를 [LocalResultEventBus]에서 읽으므로, 화면을 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun TagAddScreenTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}

internal const val DEFAULT_EMOJI_LABEL = "Emoji"
internal const val DEFAULT_EMOJI_CONFIRM = "Confirm"

internal fun screenTestViewModel(effect: Flow<TagAddEffect> = emptyFlow()): TagAddViewModel {
    val viewModel = mockk<TagAddViewModel>()
    every { viewModel.uiState } returns MutableStateFlow(TagAddUiState())
    every { viewModel.effect } returns effect
    return viewModel
}

internal fun screenTestLinkViewModel(
    uiState: StateFlow<TagLinkInputUiState> = MutableStateFlow(TagLinkInputUiState()),
    tagPagingData: Flow<PagingData<Tag>> = flowOf(PagingData.empty()),
    linkedTagIdSet: StateFlow<Set<Uuid>> = MutableStateFlow(emptySet()),
): TagAddLinkViewModel {
    val viewModel = mockk<TagAddLinkViewModel>(relaxed = true)
    every { viewModel.uiState } returns uiState
    every { viewModel.tagPagingData } returns tagPagingData
    every { viewModel.linkedTagIdSet } returns linkedTagIdSet
    return viewModel
}

internal fun ComposeContentTestRule.emojiInput(): SemanticsNodeInteraction = onNodeWithTag(DIARY_EMOJI_INPUT_TEST_TAG)

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[0]

internal fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[1]

internal fun ComposeContentTestRule.emojiDialogInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasAnyAncestor(isDialog()))

/** 이모지 칸을 눌러 다이얼로그에서 [emoji]를 입력하고 확인한다. */
internal fun ComposeContentTestRule.inputEmoji(emoji: String) {
    emojiInput().performScrollTo().performClick()
    waitForIdle()
    emojiDialogInput().performTextReplacement(emoji)
    waitForIdle()
    onNodeWithText(DEFAULT_EMOJI_CONFIRM).performClick()
    waitForIdle()
}
