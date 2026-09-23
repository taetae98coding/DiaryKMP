package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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
internal const val LINK_INPUT_INDEX = 2
internal const val INPUT_COUNT = 3

internal const val TYPED_LINK = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
internal const val OTHER_TYPED_LINK = "https://youtu.be/ArmDp-zijuc"
internal const val YOUTUBE_CHANNEL_LINK = "https://www.youtube.com/@channel"
internal const val NOT_YOUTUBE_LINK = "https://vimeo.com/76979871"
internal const val TYPED_TITLE = "MusicTitleInput"
internal const val TYPED_ARTIST = "MusicArtistInput"

internal const val FETCHED_TITLE = "FetchedMusicTitle"
internal const val FETCHED_ARTIST = "FetchedMusicArtist"

internal const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add music"
internal const val DEFAULT_FETCH_BUTTON_DESCRIPTION = "Fetch music info from link"
internal const val DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION = "Thumbnail preview"
internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"

internal fun screenTestViewModel(
    effect: Flow<MusicAddEffect> = emptyFlow(),
    uiState: MusicAddUiState = MusicAddUiState(),
): MusicAddViewModel {
    val viewModel = mockk<MusicAddViewModel>(relaxed = true)
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

internal fun fetchEffectViewModel(vararg effect: MusicAddEffect): MusicAddViewModel {
    val channel = Channel<MusicAddEffect>(capacity = Channel.BUFFERED)
    val viewModel = screenTestViewModel(effect = channel.receiveAsFlow())
    val remaining = effect.toMutableList()
    every { viewModel.fetchLink(any()) } answers {
        val next = remaining.removeFirstOrNull() ?: effect.last()
        channel.trySend(next).getOrThrow()
    }
    return viewModel
}

internal fun fetchedEffect(
    title: String = FETCHED_TITLE,
    artist: String = FETCHED_ARTIST,
): MusicAddEffect =
    MusicAddEffect.LinkFetched(
        title = title,
        artist = artist,
    )

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun ComposeContentTestRule.artistInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[ARTIST_INPUT_INDEX]

internal fun ComposeContentTestRule.linkInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[LINK_INPUT_INDEX]

internal fun ComposeContentTestRule.clickAdd() {
    onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.clickFetch() {
    onNodeWithContentDescription(DEFAULT_FETCH_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.inputCount(): Int = onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size

internal fun ComposeContentTestRule.thumbnailPreviewCount(): Int = onAllNodesWithContentDescription(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION).fetchSemanticsNodes().size

internal fun ComposeContentTestRule.thumbnailPreview(): SemanticsNodeInteraction = onNodeWithContentDescription(DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION)

internal fun ComposeContentTestRule.fillAllInput() {
    titleInput().performTextInput(TYPED_TITLE)
    artistInput().performTextInput(TYPED_ARTIST)
    linkInput().performTextInput(TYPED_LINK)
    waitForIdle()
}
