@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.ui.add.MusicAddViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.add.TYPED_LINK
import io.github.taetae98coding.diary.feature.playlist.ui.add.artistInput
import io.github.taetae98coding.diary.feature.playlist.ui.add.linkInput
import io.github.taetae98coding.diary.feature.playlist.ui.add.screenTestViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.add.titleInput
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailEffect
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailUiState
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.detail.testMusicDetail
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeDownloadUiState
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeDownloadViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeSyncViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeUiState
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.home.musicPagingDataOf
import io.github.taetae98coding.diary.feature.playlist.ui.home.testMusic
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 제품 entry의 배치 정보와 entry별 화면 상태를 그대로 쓰도록, 두 영역을 함께 표시하는 너비에서 제품 playlistEntry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class PlaylistListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(NavigationTestMoreNavKey, PlaylistHomeNavKey)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-013 곡 상세 위에서 추가 버튼으로 연 곡 추가는 빈 입력으로 시작한다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)
        composeRule.fillPlaceholder()
        openAddOnDetail(music = music)

        backStack.last() shouldBe MusicAddNavKey
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
        composeRule.titleInput().assert(hasText(""))
        composeRule.artistInput().assert(hasText(""))
        composeRule.linkInput().assert(hasText(""))
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-009 상세 영역에서 뒤로가면 곡 추가로 되돌아간다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)
        composeRule.onNodeWithText(music.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey)
        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-012 곡 상세 위에서 연 곡 추가에서 뒤로가면 이전 곡 상세로 돌아간다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)
        openAddOnDetail(music = music)

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey, MusicDetailNavKey(id = music.id))
        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-001 목록과 상세를 함께 쓰는 환경에서는 곡 목록과 곡 추가를 함께 표시한다`() {
        val music = testMusic(title = listedMusicTitle())

        setPlaylistNavDisplay(music = music)

        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-002 한 화면만 제공하는 환경에서는 현재 화면만 단독으로 표시한다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)

        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_MUSIC_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(music.detail.title).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-008 목록에서 곡을 선택하면 목록을 유지한 채 상세 영역이 그 곡의 상세로 바뀐다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)

        composeRule.onNodeWithText(music.detail.title).performClick()
        composeRule.waitForIdle()

        backStack.last() shouldBe MusicDetailNavKey(id = music.id)
        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-010 상세에서 곡을 삭제하면 상세 영역이 곡 추가로 되돌아간다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)
        composeRule.onNodeWithText(music.detail.title).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey)
        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-011 곡 상세가 놓인 동안 곡 추가를 실행하면 상세 영역이 곡 추가로 바뀐다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)

        openAddOnDetail(music = music)

        backStack.last() shouldBe MusicAddNavKey
        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-014 곡 상세가 놓인 동안 다른 곡을 고르면 교체하고 뒤로가면 곡 추가로 돌아간다`() {
        val first = testMusic(title = "a-${listedMusicTitle()}")
        val second = testMusic(title = "b-${listedMusicTitle()}")
        setPlaylistNavDisplay(musicList = listOf(first, second))

        composeRule.onNodeWithText(first.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(second.detail.title).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey, MusicDetailNavKey(id = second.id))

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey)
        composeRule.onNodeWithText(first.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-FEATURE-015 곡을 고르지 않은 상태에서 뒤로가면 더보기로 돌아간다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.onNodeWithText(music.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-PLAYLIST-LIST-DETAIL-DOMAIN-006 더보기로 돌아갔다가 다시 진입하면 상세 선택이 초기화된다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)
        composeRule.onNodeWithText(music.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(PlaylistHomeNavKey) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey)
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
    }

    private fun ComposeContentTestRule.fillPlaceholder() {
        titleInput().performTextInput("placeholder-title-${fixtureMonkey.giveMeOne<Int>()}")
        artistInput().performTextInput("placeholder-artist-${fixtureMonkey.giveMeOne<Int>()}")
        linkInput().performTextInput(TYPED_LINK)
        waitForIdle()
    }

    private fun openAddOnDetail(music: Music) {
        composeRule.onNodeWithText(music.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_MUSIC_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun listedMusicTitle(): String = "list-${fixtureMonkey.giveMeOne<Int>()}"

    private fun setPlaylistNavDisplay(music: Music) {
        setPlaylistNavDisplay(musicList = listOf(music))
    }

    private fun setPlaylistNavDisplay(musicList: List<Music>) {
        val module = viewModelModule(musicList = musicList, detailTitle = "detail-${fixtureMonkey.giveMeOne<Int>()}")
        composeRule.setContent {
            PlaylistNavigationTestHost(module = module) {
                val holder = rememberListDetailPlaceholderStateHolder()

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider =
                            entryProvider {
                                entry<NavigationTestMoreNavKey> { Text(text = MORE_CONTENT) }
                                playlistEntry(backStack = backStack)
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun PlaylistNavigationTestHost(
        module: Module,
        content: @Composable () -> Unit,
    ) {
        // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }
        val resultEventBus = remember { ResultEventBus() }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner,
            LocalResultEventBus provides resultEventBus,
        ) {
            KoinApplication(configuration = koinConfiguration { modules(module) }) {
                DiaryTheme(content = content)
            }
        }
    }

    private fun viewModelModule(
        musicList: List<Music>,
        detailTitle: String,
    ): Module =
        module {
            factory<PlaylistHomeViewModel> {
                mockk(relaxed = true) {
                    every { musicPagingData } returns MutableStateFlow(musicPagingDataOf(musicList))
                    every { sort } returns MutableStateFlow(ListSort.TITLE)
                }
            }
            factory<PlaylistHomeSyncViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(PlaylistHomeUiState())
                }
            }
            factory<PlaylistHomeDownloadViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(PlaylistHomeDownloadUiState())
                    every { effect } returns emptyFlow()
                }
            }
            factory<MusicAddViewModel> { screenTestViewModel() }
            factory<MusicDetailViewModel> { parameters ->
                val id: Uuid = parameters.get()
                val effectChannel = Channel<MusicDetailEffect>(capacity = Channel.BUFFERED)
                // 뒤로가기로 entry가 닫히면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
                mockk<MusicDetailViewModel>(relaxed = true) {
                    every { uiState } returns MutableStateFlow(MusicDetailUiState.Content(id = id, detail = testMusicDetail(title = detailTitle)))
                    every { effect } returns effectChannel.receiveAsFlow()
                    every { delete() } answers { effectChannel.trySend(MusicDetailEffect.DeleteSucceeded).getOrThrow() }
                }
            }
        }

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val DEFAULT_ADD_MUSIC_DESCRIPTION = "Add music"
        const val DEFAULT_DELETE_MUSIC_DESCRIPTION = "Delete music"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}

// 플레이리스트 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object NavigationTestMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
