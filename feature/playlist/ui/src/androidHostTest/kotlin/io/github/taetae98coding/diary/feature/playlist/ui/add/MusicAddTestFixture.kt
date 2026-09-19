package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal const val TITLE_INPUT_INDEX = 0
internal const val ARTIST_INPUT_INDEX = 1
internal const val INPUT_COUNT = 2

internal const val TYPED_TITLE = "MusicTitleInput"
internal const val TYPED_ARTIST = "MusicArtistInput"

internal const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add music"
internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"

internal fun screenTestViewModel(
    effect: Flow<MusicAddEffect> = emptyFlow(),
    uiState: MusicAddUiState = MusicAddUiState(),
): MusicAddViewModel {
    val viewModel = mockk<MusicAddViewModel>()
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    every { viewModel.effect } returns effect
    return viewModel
}

internal fun effectViewModel(effect: MusicAddEffect): MusicAddViewModel {
    val channel = Channel<MusicAddEffect>(capacity = Channel.BUFFERED)
    val viewModel = screenTestViewModel(effect = channel.receiveAsFlow())
    every { viewModel.add(any()) } answers { channel.trySend(effect).getOrThrow() }
    return viewModel
}

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun ComposeContentTestRule.artistInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[ARTIST_INPUT_INDEX]

internal fun ComposeContentTestRule.clickAdd() {
    onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.inputCount(): Int = onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size

internal fun ComposeContentTestRule.fillAllInput() {
    titleInput().performTextInput(TYPED_TITLE)
    artistInput().performTextInput(TYPED_ARTIST)
    waitForIdle()
}
