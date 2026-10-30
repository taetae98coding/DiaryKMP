package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaylistHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-97f58e54-aa0d-407e-b435-dc552077fa0c 조회한 곡을 목록에 표시한다`() {
        val music = testMusic(title = MUSIC_TITLE, artist = MUSIC_ARTIST)

        setPlaylistHomeScreen(musicList = listOf(music))

        composeRule.onNodeWithText(MUSIC_TITLE).assertExists()
        composeRule.onNodeWithText(MUSIC_ARTIST).assertExists()
    }

    @Test
    fun `TC-4f4be13e-7d7d-4f0f-9a92-592307a5fa81 뒤로가기를 선택하면 돌아가기 행동을 한 번 전달한다`() {
        var navigateUpCount = 0

        setPlaylistHomeScreen(navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-61313533-3370-4dcc-9f6d-48121804db99 곡 추가를 선택하면 곡 추가 화면으로 이동한다`() {
        var navigateToAddCount = 0

        setPlaylistHomeScreen(navigateToAdd = { navigateToAddCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        navigateToAddCount shouldBe 1
    }

    @Test
    fun `TC-cdffb691-2c41-4b33-a6da-8c243b81a200 목록의 곡을 선택하면 그 곡의 상세 화면으로 이동한다`() {
        val music = testMusic(title = MUSIC_TITLE, artist = MUSIC_ARTIST)
        val navigatedIdList = mutableListOf<Uuid>()

        setPlaylistHomeScreen(musicList = listOf(music), navigateToDetail = { id -> navigatedIdList += id })
        composeRule.onNodeWithText(MUSIC_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(music.id)
    }

    @Test
    fun `TC-b33a6b98-eb07-418b-9686-9ae2b3850279 목록을 당기면 새로고침을 요청한다`() {
        val syncViewModel = syncViewModel()

        setPlaylistHomeScreen(musicList = listOf(testMusic(title = MUSIC_TITLE)), syncViewModel = syncViewModel)
        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { syncViewModel.refresh() }
    }

    private fun setPlaylistHomeScreen(
        musicList: List<Music> = emptyList(),
        navigateUp: () -> Unit = {},
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
        componentVisible: PlaylistHomeScaffoldComponentVisible = PlaylistHomeScaffoldComponentVisible(),
        syncViewModel: SyncRefreshViewModel = syncViewModel(),
    ) {
        setPlaylistHomeScreen(
            musicPagingDataFlow = MutableStateFlow(musicPagingDataOf(musicList)),
            navigateUp = navigateUp,
            navigateToAdd = navigateToAdd,
            navigateToDetail = navigateToDetail,
            componentVisible = componentVisible,
            syncViewModel = syncViewModel,
        )
    }

    private fun setPlaylistHomeScreen(
        musicPagingDataFlow: MutableStateFlow<PagingData<Music>>,
        navigateUp: () -> Unit = {},
        navigateToAdd: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
        componentVisible: PlaylistHomeScaffoldComponentVisible = PlaylistHomeScaffoldComponentVisible(),
        syncViewModel: SyncRefreshViewModel = syncViewModel(),
    ) {
        val musicViewModel = mockk<PlaylistHomeViewModel>(relaxed = true)
        every { musicViewModel.musicPagingData } returns musicPagingDataFlow
        every { musicViewModel.uiState } returns MutableStateFlow(PlaylistHomeUiState())
        every { musicViewModel.effect } returns emptyFlow()

        composeRule.setContent {
            DiaryTheme {
                PlaylistHomeScreen(
                    navigateUp = navigateUp,
                    navigateToAdd = navigateToAdd,
                    navigateToDetail = navigateToDetail,
                    componentVisibleProvider = { componentVisible },
                    musicViewModel = musicViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun syncViewModel(): SyncRefreshViewModel {
        val viewModel = mockk<SyncRefreshViewModel>()
        every { viewModel.uiState } returns MutableStateFlow(SyncRefreshUiState())
        justRun { viewModel.refresh() }
        return viewModel
    }

    private companion object {
        private const val MUSIC_TITLE = "ScreenMusicTitle"
        private const val MUSIC_ARTIST = "ScreenMusicArtist"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add music"
    }
}
