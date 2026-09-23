package io.github.taetae98coding.diary.feature.playlist.ui.detail

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

internal const val TITLE_INPUT_INDEX = 0
internal const val ARTIST_INPUT_INDEX = 1
internal const val LINK_INPUT_INDEX = 2

internal const val STORED_LINK = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
internal const val STORED_TITLE = "MusicDetailStoredTitle"
internal const val STORED_ARTIST = "MusicDetailStoredArtist"
internal const val YOUTUBE_CHANNEL_LINK = "https://www.youtube.com/@channel"

internal const val EDITING_TITLE = "MusicDetailEditingTitle"
internal const val CHANGED_TITLE = "MusicDetailChangedTitle"

internal const val FETCHED_TITLE = "MusicDetailFetchedTitle"
internal const val FETCHED_ARTIST = "MusicDetailFetchedArtist"

internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
internal const val DEFAULT_OPEN_IN_NEW_DESCRIPTION = "Open externally"
internal const val DEFAULT_DELETE_BUTTON_DESCRIPTION = "Delete music"
internal const val DEFAULT_UPDATE_BUTTON_DESCRIPTION = "Update music"
internal const val DEFAULT_FETCH_BUTTON_DESCRIPTION = "Fetch music info from link"
internal const val DEFAULT_THUMBNAIL_PREVIEW_DESCRIPTION = "Thumbnail preview"

internal fun testMusicDetail(
    title: String = STORED_TITLE,
    artist: String = STORED_ARTIST,
    link: String = STORED_LINK,
): MusicDetail =
    MusicDetail(
        title = title,
        artist = artist,
        link = link,
    )

internal fun detailViewModel(
    uiState: MutableStateFlow<MusicDetailUiState>,
    effect: Flow<MusicDetailEffect> = emptyFlow(),
): MusicDetailViewModel {
    val viewModel = mockk<MusicDetailViewModel>()
    every { viewModel.uiState } returns uiState
    every { viewModel.effect } returns effect
    justRun { viewModel.update(detail = any<MusicDetail>()) }
    justRun { viewModel.fetchLink(link = any()) }
    justRun { viewModel.delete() }

    return viewModel
}

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun ComposeContentTestRule.artistInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[ARTIST_INPUT_INDEX]

internal fun ComposeContentTestRule.linkInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[LINK_INPUT_INDEX]

internal fun ComposeContentTestRule.clickUpdate() {
    onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.clickFetch() {
    onNodeWithContentDescription(DEFAULT_FETCH_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.nodeCount(contentDescription: String): Int = onAllNodesWithContentDescription(contentDescription).fetchSemanticsNodes().size
