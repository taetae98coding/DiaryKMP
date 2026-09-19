package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaylistHomeScaffoldRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-008 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<PlaylistHomeScaffoldEvent>()
        setPlaylistHomeScaffold(
            musicList = listOf(testMusic(title = MUSIC_TITLE)),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(PlaylistHomeScaffoldEvent.Refresh)
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-009 진행 표시 상태이면 진행 표시가 나타난다`() {
        setPlaylistHomeScaffold(isRefreshingProvider = { true })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-009 동기화가 끝나면 진행 표시가 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setPlaylistHomeScaffold(isRefreshingProvider = { isRefreshing.value })
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun setPlaylistHomeScaffold(
        musicList: List<Music> = emptyList(),
        isRefreshingProvider: () -> Boolean = { false },
        onEvent: (PlaylistHomeScaffoldEvent) -> Unit = {},
    ) {
        val musicPagingDataFlow = MutableStateFlow(musicPagingDataOf(musicList))

        composeRule.setContent {
            DiaryTheme {
                PlaylistHomeScaffold(
                    onEvent = onEvent,
                    musicPagingItems = musicPagingDataFlow.collectAsLazyPagingItems(),
                    uiStateProvider = { PlaylistHomeUiState(isRefreshing = isRefreshingProvider()) },
                )
            }
        }
    }

    private companion object {
        private const val MUSIC_TITLE = "RefreshMusicTitle"
        private const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
