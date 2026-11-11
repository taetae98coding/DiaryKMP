@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.music.MUSIC_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.music.MusicListEvent
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.ui.add.MusicAddViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.add.screenTestViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.addTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailEffect
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailUiState
import io.github.taetae98coding.diary.feature.playlist.ui.detail.MusicDetailViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.detail.testMusicDetail
import io.github.taetae98coding.diary.feature.playlist.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.playlistEntry
import io.github.taetae98coding.diary.feature.playlist.ui.refreshableList
import io.github.taetae98coding.diary.feature.playlist.ui.resetAndroidUiDispatcher
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.After
import org.junit.Before
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

// 한 화면만 제공하는 너비에서 제품 playlistEntry로 NavDisplay를 구성해 목록 위치가 이동·복귀·재진입에서 어떻게 되는지 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaylistHomeListRetentionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(RetentionMoreNavKey, PlaylistHomeNavKey)

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-44ff64ff-1586-4ae7-8839-3b40454b3801 화면이 재생성되어도 보던 목록 위치를 유지한다`() {
        val musicList = retentionMusicList()
        val pagingDataFlow = MutableStateFlow(musicPagingDataOf(musicList))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                PlaylistHomeList(
                    onEvent = {},
                    onMusicListEvent = {},
                    musicPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.refreshableList().performScrollToIndex(SCROLL_INDEX)
        composeRule.onNodeWithText(musicList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(musicList.first().detail.title).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(musicList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(musicList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-cb1fef07-5e11-43e9-864b-f1fe2d91fd9c 앱이 백그라운드에 갔다가 돌아와도 보던 목록 위치를 유지한다`() {
        val musicList = retentionMusicList()
        val pagingDataFlow = MutableStateFlow(musicPagingDataOf(musicList))
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    PlaylistHomeList(
                        onEvent = {},
                        onMusicListEvent = {},
                        musicPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    )
                }
            }
        }
        composeRule.refreshableList().performScrollToIndex(SCROLL_INDEX)
        composeRule.onNodeWithText(musicList[SCROLL_INDEX].detail.title).assertIsDisplayed()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(musicList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(musicList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-cb1fef07-5e11-43e9-864b-f1fe2d91fd9c 곡 추가로 이동한 뒤 뒤로가도 보던 목록 위치를 유지한다`() {
        val musicList = retentionMusicList()
        setPlaylistNavDisplay(musicList = musicList)
        scrollToPositionMusic(musicList)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_MUSIC_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.refreshableList().assertDoesNotExist()
        pressBack()

        assertPositionMusicDisplayed(musicList)
    }

    @Test
    fun `TC-cb1fef07-5e11-43e9-864b-f1fe2d91fd9c 곡 상세로 이동한 뒤 뒤로가도 보던 목록 위치를 유지한다`() {
        val musicList = retentionMusicList()
        setPlaylistNavDisplay(musicList = musicList)
        scrollToPositionMusic(musicList)

        composeRule.onNode(musicCard(musicList[SCROLL_INDEX])).performClick()
        composeRule.waitForIdle()
        composeRule.refreshableList().assertDoesNotExist()
        pressBack()

        assertPositionMusicDisplayed(musicList)
    }

    @Test
    fun `TC-74008fbe-7493-4283-96aa-d70ac4a67548 더보기로 나갔다 플레이리스트 바로가기로 다시 진입하면 목록의 맨 위부터 보여 준다`() {
        val musicList = retentionMusicList()
        setPlaylistNavDisplay(musicList = musicList)
        scrollToPositionMusic(musicList)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.add(PlaylistHomeNavKey) }
        composeRule.waitForIdle()

        composeRule.onNode(musicCard(musicList.first())).assertIsDisplayed()
        composeRule.onNode(musicCard(musicList[SCROLL_INDEX])).assertDoesNotExist()
    }

    @Test
    fun `TC-74008fbe-7493-4283-96aa-d70ac4a67548 앱을 다시 실행해 진입하면 목록의 맨 위부터 보여 준다`() {
        val musicList = retentionMusicList()
        var launchCount by mutableIntStateOf(0)
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없고 화면 상태를 들고 있던 객체도 새로 만들어지므로, 새 키로 저장 상태 없이 화면을 다시 구성한다.
        setPlaylistNavDisplay(musicList = musicList, launchCountProvider = { launchCount })
        scrollToPositionMusic(musicList)

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()
        waitUntilMusicIsDisplayed(musicList.first())

        composeRule.onNode(musicCard(musicList.first())).assertIsDisplayed()
        composeRule.onNode(musicCard(musicList[SCROLL_INDEX])).assertDoesNotExist()
    }

    private fun retentionMusicList(): List<Music> =
        List(MUSIC_COUNT) { index ->
            testMusic(title = "${index.toString().padStart(length = 2, padChar = '0')}-retention")
        }

    private fun musicCard(music: Music) = hasTestTag(MUSIC_CARD_TEST_TAG) and hasText(music.detail.title)

    private fun scrollToPositionMusic(musicList: List<Music>) {
        waitUntilMusicIsDisplayed(musicList.first())
        composeRule.refreshableList().performScrollToIndex(SCROLL_INDEX)
        composeRule.waitForIdle()
        assertPositionMusicDisplayed(musicList)
    }

    private fun assertPositionMusicDisplayed(musicList: List<Music>) {
        composeRule.onNode(musicCard(musicList[SCROLL_INDEX])).assertIsDisplayed()
        composeRule.onNode(musicCard(musicList.first())).assertDoesNotExist()
    }

    private fun waitUntilMusicIsDisplayed(music: Music) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(music.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setPlaylistNavDisplay(
        musicList: List<Music>,
        launchCountProvider: () -> Int = { 0 },
    ) {
        val module = viewModelModule(musicList = musicList)
        composeRule.setContent {
            key(launchCountProvider()) {
                PlaylistNavDisplay(module = module)
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun PlaylistNavDisplay(module: Module) {
        RetentionTestHost(module = module) {
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
                            entry<RetentionMoreNavKey> { Text(text = MORE_CONTENT) }
                            playlistEntry(backStack = backStack)
                        },
                )
            }
        }
    }

    @Composable
    private fun RetentionTestHost(
        module: Module,
        content: @Composable () -> Unit,
    ) {
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

    private fun viewModelModule(musicList: List<Music>): Module =
        module {
            factory { addTagScreenTestViewModel() }
            factory { detailTagScreenTestViewModel() }
            factory<PlaylistHomeViewModel> {
                mockk<PlaylistHomeViewModel>(relaxed = true) {
                    every { musicPagingData } returns MutableStateFlow(musicPagingDataOf(musicList))
                    every { uiState } returns MutableStateFlow(PlaylistHomeUiState())
                    every { effect } returns emptyFlow()
                }
            }
            factory<SyncRefreshViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SyncRefreshUiState())
                }
            }
            factory<MusicAddViewModel> { screenTestViewModel() }
            factory<MusicDetailViewModel> { parameters ->
                val id: Uuid = parameters.get()
                val effectChannel = Channel<MusicDetailEffect>(capacity = Channel.BUFFERED)
                // 뒤로가기로 entry가 닫히면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
                mockk<MusicDetailViewModel>(relaxed = true) {
                    every { uiState } returns MutableStateFlow(MusicDetailUiState.Content(id = id, detail = testMusicDetail()))
                    every { effect } returns effectChannel.receiveAsFlow()
                }
            }
        }

    private companion object {
        const val MUSIC_COUNT = 40
        const val SCROLL_INDEX = 30
        const val MORE_CONTENT = "RetentionMoreContent"
        const val DEFAULT_ADD_MUSIC_DESCRIPTION = "Add music"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
    }
}

// 플레이리스트 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object RetentionMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "RetentionMore"
}
