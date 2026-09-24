package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.feature.playlist.ui.music.MUSIC_DOWNLOAD_BADGE_TEST_TAG
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaylistHomeDownloadTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-018 목록에 곡이 있어도 다운로드를 실행할 수 있다`() {
        val eventList = mutableListOf<PlaylistHomeScaffoldEvent>()
        setPlaylistHomeScaffold(musicList = listOf(testMusic(title = FIRST_TITLE)), onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_DOWNLOAD_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_DOWNLOAD_DESCRIPTION).performClick()

        eventList shouldBe listOf(PlaylistHomeScaffoldEvent.ClickDownload)
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-018 목록이 비어 있어도 다운로드를 실행할 수 있다`() {
        val eventList = mutableListOf<PlaylistHomeScaffoldEvent>()
        setPlaylistHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_DOWNLOAD_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_DOWNLOAD_DESCRIPTION).performClick()

        eventList shouldBe listOf(PlaylistHomeScaffoldEvent.ClickDownload)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 다운로드 버튼 접근성 이름을 제공한다`() {
        setPlaylistHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_DOWNLOAD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MUSIC-DOWNLOAD-FEATURE-008 다운로드를 실행하지 않으면 어떤 곡에도 상태를 표시하지 않는다`() {
        setPlaylistHomeScaffold(musicList = listOf(testMusic(title = FIRST_TITLE), testMusic(title = SECOND_TITLE)))

        composeRule.onAllNodesWithTag(MUSIC_DOWNLOAD_BADGE_TEST_TAG, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun `TC-MUSIC-DOWNLOAD-FEATURE-001 대기 중인 곡의 항목에서 대기를 확인한다`() {
        assertStateDisplayed(state = MusicDownloadState.Pending, description = DEFAULT_PENDING_DESCRIPTION)
    }

    @Test
    fun `TC-MUSIC-DOWNLOAD-FEATURE-002 진행 중인 곡의 항목에서 받은 만큼을 백분율로 확인한다`() {
        assertStateDisplayed(state = MusicDownloadState.Running(progress = 0.62F), description = DEFAULT_RUNNING_DESCRIPTION)
    }

    @Test
    fun `TC-MUSIC-DOWNLOAD-FEATURE-018 백분율이 없는 진행 중인 곡의 항목에서 백분율 없이 진행 중임을 확인한다`() {
        assertStateDisplayed(state = MusicDownloadState.Running(progress = null), description = DEFAULT_RUNNING_INDETERMINATE_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_RUNNING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MUSIC-DOWNLOAD-FEATURE-018 한국어 환경에서 백분율이 없는 진행 중을 확인한다`() {
        assertStateDisplayed(state = MusicDownloadState.Running(progress = null), description = KOREAN_RUNNING_INDETERMINATE_DESCRIPTION)
    }

    @Test
    fun `TC-MUSIC-DOWNLOAD-FEATURE-003 받기를 마친 곡의 항목에서 완료를 확인한다`() {
        assertStateDisplayed(state = MusicDownloadState.Done, description = DEFAULT_DONE_DESCRIPTION)
    }

    @Test
    fun `TC-MUSIC-DOWNLOAD-FEATURE-004 받지 못한 곡의 항목에서 실패를 확인한다`() {
        assertStateDisplayed(state = MusicDownloadState.Failed, description = DEFAULT_FAILED_DESCRIPTION)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MUSIC-DOWNLOAD-FEATURE-002 한국어 환경에서 진행 중인 곡의 백분율을 확인한다`() {
        assertStateDisplayed(state = MusicDownloadState.Running(progress = 0.62F), description = KOREAN_RUNNING_DESCRIPTION)
    }

    @Test
    fun `TC-MUSIC-DOWNLOAD-FEATURE-005 실패한 곡과 완료된 곡을 함께 표시한다`() {
        val failed = testMusic(title = FIRST_TITLE)
        val done = testMusic(title = SECOND_TITLE)

        setPlaylistHomeScaffold(
            musicList = listOf(failed, done),
            downloadUiState =
                PlaylistHomeDownloadUiState(
                    stateMap =
                        mapOf(
                            failed.id to MusicDownloadState.Failed,
                            done.id to MusicDownloadState.Done,
                        ),
                ),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_FAILED_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DONE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MUSIC-DOWNLOAD-FEATURE-007 다운로드 대상이 아닌 곡에는 상태를 표시하지 않는다`() {
        val target = testMusic(title = FIRST_TITLE)
        val withoutLink = testMusic(title = SECOND_TITLE, link = "")

        setPlaylistHomeScaffold(
            musicList = listOf(target, withoutLink),
            downloadUiState = PlaylistHomeDownloadUiState(stateMap = mapOf(target.id to MusicDownloadState.Done)),
        )

        composeRule.onAllNodesWithTag(MUSIC_DOWNLOAD_BADGE_TEST_TAG, useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun `TC-MUSIC-DOWNLOAD-FEATURE-009 다운로드가 진행되는 동안에도 다른 조작을 실행할 수 있다`() {
        val music = testMusic(title = FIRST_TITLE)
        val eventList = mutableListOf<PlaylistHomeScaffoldEvent>()

        setPlaylistHomeScaffold(
            musicList = listOf(music),
            onEvent = eventList::add,
            downloadUiState = PlaylistHomeDownloadUiState(stateMap = mapOf(music.id to MusicDownloadState.Running(progress = 0.62F))),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe
            listOf(
                PlaylistHomeScaffoldEvent.ClickAdd,
                PlaylistHomeScaffoldEvent.ClickNavigateUp,
            )
    }

    private fun assertStateDisplayed(
        state: MusicDownloadState,
        description: String,
    ) {
        val music = testMusic(title = FIRST_TITLE)

        setPlaylistHomeScaffold(
            musicList = listOf(music),
            downloadUiState = PlaylistHomeDownloadUiState(stateMap = mapOf(music.id to state)),
        )

        composeRule.onNodeWithContentDescription(description).assertExists()
    }

    private fun setPlaylistHomeScaffold(
        musicList: List<Music> = emptyList(),
        onEvent: (PlaylistHomeScaffoldEvent) -> Unit = {},
        downloadUiState: PlaylistHomeDownloadUiState = PlaylistHomeDownloadUiState(),
    ) {
        val musicPagingDataFlow: MutableStateFlow<PagingData<Music>> = MutableStateFlow(musicPagingDataOf(musicList))

        composeRule.setContent {
            DiaryTheme {
                PlaylistHomeScaffold(
                    onEvent = onEvent,
                    musicPagingItems = musicPagingDataFlow.collectAsLazyPagingItems(),
                    downloadUiStateProvider = { downloadUiState },
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_DOWNLOAD_DESCRIPTION = "Download music"
        private const val KOREAN_DOWNLOAD_DESCRIPTION = "곡 다운로드"
        private const val DEFAULT_PENDING_DESCRIPTION = "Waiting to download"
        private const val DEFAULT_RUNNING_DESCRIPTION = "Downloading 62%"
        private const val KOREAN_RUNNING_DESCRIPTION = "다운로드 중 62%"
        private const val DEFAULT_RUNNING_INDETERMINATE_DESCRIPTION = "Downloading"
        private const val KOREAN_RUNNING_INDETERMINATE_DESCRIPTION = "다운로드 중"
        private const val DEFAULT_DONE_DESCRIPTION = "Downloaded"
        private const val DEFAULT_FAILED_DESCRIPTION = "Download failed"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add music"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val FIRST_TITLE = "AlphaMusic"
        private const val SECOND_TITLE = "BravoMusic"
    }
}
