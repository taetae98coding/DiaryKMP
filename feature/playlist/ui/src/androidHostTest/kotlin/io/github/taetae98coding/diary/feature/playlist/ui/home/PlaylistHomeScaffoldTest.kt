package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.feature.playlist.ui.music.MUSIC_CARD_TEST_TAG
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaylistHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-PLAYLIST-HOME-FEATURE-001 한국어 환경에서 상단 바에 제목을 표시한다`() {
        setPlaylistHomeScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-001 기본 환경에서 상단 바에 제목을 표시한다`() {
        setPlaylistHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setPlaylistHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-004 목록에 곡의 제목과 가수를 카드로 표시한다`() {
        val first = testMusic(title = FIRST_TITLE, artist = FIRST_ARTIST)
        val second = testMusic(title = SECOND_TITLE, artist = SECOND_ARTIST)

        setPlaylistHomeScaffold(musicList = listOf(first, second))

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(FIRST_ARTIST).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_ARTIST).assertExists()
        composeRule.onAllNodesWithTag(MUSIC_CARD_TEST_TAG).assertCountEquals(2)
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-012 링크가 없는 곡도 목록에 그대로 나타난다`() {
        val withLink = testMusic(title = FIRST_TITLE, artist = FIRST_ARTIST)
        val withoutLink = testMusic(title = SECOND_TITLE, artist = SECOND_ARTIST, link = "")

        setPlaylistHomeScaffold(musicList = listOf(withLink, withoutLink))

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(FIRST_ARTIST).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_ARTIST).assertExists()
        composeRule.onAllNodesWithTag(MUSIC_CARD_TEST_TAG).assertCountEquals(2)
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-017 가수가 없는 곡은 제목만 표시한다`() {
        setPlaylistHomeScaffold(musicList = listOf(testMusic(title = FIRST_TITLE, artist = "")))

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onAllNodesWithTag(MUSIC_CARD_TEST_TAG).assertCountEquals(1)
    }

    @Test
    fun `목록에 없는 곡은 표시하지 않는다`() {
        setPlaylistHomeScaffold(musicList = listOf(testMusic(title = FIRST_TITLE)))

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertDoesNotExist()
    }

    @Test
    fun `목록의 곡을 모두 표시한다`() {
        setPlaylistHomeScaffold(
            musicList =
                listOf(
                    testMusic(title = FIRST_TITLE),
                    testMusic(title = SECOND_TITLE),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertExists()
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-015 곡 카드를 누르면 그 곡의 선택 이벤트를 내보낸다`() {
        val music = testMusic(title = FIRST_TITLE)
        val eventList = mutableListOf<PlaylistHomeScaffoldEvent>()
        setPlaylistHomeScaffold(musicList = listOf(music), onEvent = eventList::add)

        composeRule.onAllNodesWithTag(MUSIC_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onAllNodesWithTag(MUSIC_CARD_TEST_TAG).onFirst().performClick()

        eventList shouldBe listOf(PlaylistHomeScaffoldEvent.ClickMusic(id = music.id))
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-006 목록에 곡이 있어도 곡 추가를 선택할 수 있다`() {
        val eventList = mutableListOf<PlaylistHomeScaffoldEvent>()
        setPlaylistHomeScaffold(musicList = listOf(testMusic(title = FIRST_TITLE)), onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(PlaylistHomeScaffoldEvent.ClickAdd)
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-006 목록이 비어 있어도 곡 추가를 선택할 수 있다`() {
        val eventList = mutableListOf<PlaylistHomeScaffoldEvent>()
        setPlaylistHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(PlaylistHomeScaffoldEvent.ClickAdd)
    }

    @Test
    fun `뒤로가기를 누르면 뒤로가기 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<PlaylistHomeScaffoldEvent>()
        setPlaylistHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(PlaylistHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-003 상세 영역에 곡 추가가 놓이면 곡 추가 버튼이 표시되지 않는다`() {
        setPlaylistHomeScaffold(componentVisible = PlaylistHomeScaffoldComponentVisible(isAddButtonVisible = false))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-004 추가 버튼 표시 상태이면 곡 추가 버튼이 표시된다`() {
        setPlaylistHomeScaffold(componentVisible = PlaylistHomeScaffoldComponentVisible(isAddButtonVisible = true))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-007 상세 영역에 곡 추가가 놓여도 정렬 컨트롤은 표시된다`() {
        setPlaylistHomeScaffold(
            musicList = listOf(testMusic(title = FIRST_TITLE)),
            componentVisible = PlaylistHomeScaffoldComponentVisible(isAddButtonVisible = false),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLAYLIST-HOME-FEATURE-014 상세 영역에 곡 추가가 놓여도 뒤로가기 버튼은 표시된다`() {
        val eventList = mutableListOf<PlaylistHomeScaffoldEvent>()
        setPlaylistHomeScaffold(
            onEvent = eventList::add,
            componentVisible = PlaylistHomeScaffoldComponentVisible(isAddButtonVisible = false),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(PlaylistHomeScaffoldEvent.ClickNavigateUp)
    }

    private fun setPlaylistHomeScaffold(
        musicList: List<Music> = emptyList(),
        onEvent: (PlaylistHomeScaffoldEvent) -> Unit = {},
        componentVisible: PlaylistHomeScaffoldComponentVisible = PlaylistHomeScaffoldComponentVisible(),
    ) {
        val musicPagingDataFlow: MutableStateFlow<PagingData<Music>> = MutableStateFlow(musicPagingDataOf(musicList))

        composeRule.setContent {
            DiaryTheme {
                PlaylistHomeScaffold(
                    onEvent = onEvent,
                    musicPagingItems = musicPagingDataFlow.collectAsLazyPagingItems(),
                    componentVisibleProvider = { componentVisible },
                )
            }
        }
    }

    private companion object {
        private const val KOREAN_TITLE = "플레이리스트"
        private const val DEFAULT_TITLE = "Playlist"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add music"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val FIRST_TITLE = "AlphaMusic"
        private const val FIRST_ARTIST = "AlphaArtist"
        private const val SECOND_TITLE = "BravoMusic"
        private const val SECOND_ARTIST = "BravoArtist"
    }
}
