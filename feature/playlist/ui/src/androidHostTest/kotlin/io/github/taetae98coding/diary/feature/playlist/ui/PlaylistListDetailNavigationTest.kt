@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.api.playlistNavKeys
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
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeUiState
import io.github.taetae98coding.diary.feature.playlist.ui.home.PlaylistHomeViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.home.musicPagingDataOf
import io.github.taetae98coding.diary.feature.playlist.ui.home.testMusic
import io.github.taetae98coding.diary.feature.playlist.ui.music.MUSIC_CARD_TEST_TAG
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
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

// 제품 entry의 배치 정보와 entry별 화면 상태를 그대로 쓰도록, 두 영역을 함께 표시하는 너비에서 제품 playlistEntry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class PlaylistListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(NavigationTestMoreNavKey, PlaylistHomeNavKey)
    private val homeViewModelList = mutableListOf<PlaylistHomeViewModel>()

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-c0bed6d3-755f-4187-8d31-8845604c4c23 곡 상세 위에서 추가 버튼으로 연 곡 추가는 빈 입력으로 시작한다`() {
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
    fun `TC-ea2d2487-a6bc-482f-ac6e-4c18279e061a 상세 영역에서 뒤로가면 곡 추가로 되돌아간다`() {
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
    fun `TC-8af8f6ba-c9cb-4cd0-a5a5-389033a1373a 곡 상세 위에서 연 곡 추가에서 뒤로가면 이전 곡 상세로 돌아간다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)
        openAddOnDetail(music = music)

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey, MusicDetailNavKey(id = music.id))
        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-d050e4fb-5c08-45e7-919c-cb932e42263f 목록과 상세를 함께 쓰는 환경에서는 곡 목록과 곡 추가를 함께 표시한다`() {
        val music = testMusic(title = listedMusicTitle())

        setPlaylistNavDisplay(music = music)

        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-aa493d72-2aa1-4e8b-b9ac-0dd146d5a082 한 화면만 제공하는 환경에서는 현재 화면만 단독으로 표시한다`() {
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
    fun `TC-2745dd6f-a834-4115-8f14-a6fd53a1dc7d 목록에서 곡을 선택하면 목록을 유지한 채 상세 영역이 그 곡의 상세로 바뀐다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)

        composeRule.onNodeWithText(music.detail.title).performClick()
        composeRule.waitForIdle()

        backStack.last() shouldBe MusicDetailNavKey(id = music.id)
        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-2be87e24-6163-436f-a44a-998a3cec59b7 상세에서 곡을 삭제하면 상세 영역이 곡 추가로 되돌아간다`() {
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
    fun `TC-67658cd1-9707-4adc-9ee1-a370604faae2 곡 상세가 놓인 동안 곡 추가를 실행하면 상세 영역이 곡 추가로 바뀐다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)

        openAddOnDetail(music = music)

        backStack.last() shouldBe MusicAddNavKey
        composeRule.onNodeWithText(music.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
    }

    @Test
    fun `TC-d7aaa386-5c2b-4908-81d9-16d4eb8006bb 곡 상세가 놓인 동안 다른 곡을 고르면 교체하고 뒤로가면 곡 추가로 돌아간다`() {
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
    fun `TC-c41335b9-dce4-40f5-8581-5930af99d5e3 곡 상세 위에서 연 곡 추가가 놓인 동안 다른 곡을 고르면 추가를 걷어내고 그 상세로 바꾼다`() {
        val first = testMusic(title = "a-${listedMusicTitle()}")
        val second = testMusic(title = "b-${listedMusicTitle()}")
        val typedTitle = "typed-title-${fixtureMonkey.giveMeOne<Int>()}"
        setPlaylistNavDisplay(musicList = listOf(first, second))
        openAddOnDetail(music = first)
        composeRule.titleInput().performTextInput(typedTitle)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(second.detail.title).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey, MusicDetailNavKey(id = second.id))

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey)
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(typedTitle).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(""))
        composeRule.artistInput().assert(hasText(""))
        composeRule.linkInput().assert(hasText(""))
    }

    @Test
    fun `TC-6dadd803-f2f1-42ce-9aac-41d8a9891bb5 상세에 열린 곡을 목록에서 밀어 삭제해도 상세 영역은 그 곡의 상세로 남는다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)
        composeRule.onNodeWithText(music.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()

        composeRule.onNode(hasTestTag(MUSIC_CARD_TEST_TAG) and hasText(music.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { homeViewModelList.last().delete(id = music.id) }
        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey, MusicDetailNavKey(id = music.id))
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-4a4529a5-993c-4387-b6f8-e7a870c96eaf 곡을 고르지 않은 상태에서 뒤로가면 더보기로 돌아간다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)

        pressBack()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.onNodeWithText(music.detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-ffecf0be-2458-4005-8e41-182612a5a590 더보기로 돌아갔다가 다시 진입하면 상세 선택이 초기화된다`() {
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

    @Test
    fun `TC-a28906cd-ce6e-45dc-af96-372616a4c5de 화면이 재생성되어도 상세 선택을 유지한다`() {
        val music = testMusic(title = listedMusicTitle())
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var restoredBackStack: NavBackStack<ScreenNavKey>
        setRestorablePlaylistNavDisplay(musicList = listOf(music), restorationTester = restorationTester) { stack -> restoredBackStack = stack }
        composeRule.onNodeWithText(music.detail.title).performClick()
        composeRule.waitForIdle()
        assertDetailOnDetailPane()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        restoredBackStack.toList() shouldBe listOf(PlaylistHomeNavKey, MusicDetailNavKey(id = music.id))
        waitUntilDisplayed(music)
        assertDetailOnDetailPane()
        pressBack()
        restoredBackStack.toList() shouldBe listOf(PlaylistHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-5f690a2a-54b9-4d68-b98a-4db0c11ff17c 시스템이 앱을 정리했다가 다시 만들어도 상세 선택과 뒤로가기 기록을 유지한다`() {
        val music = testMusic(title = listedMusicTitle())
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var restoredBackStack: NavBackStack<ScreenNavKey>
        setRestorablePlaylistNavDisplay(musicList = listOf(music), restorationTester = restorationTester) { stack -> restoredBackStack = stack }
        openAddOnDetail(music = music)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        restoredBackStack.toList() shouldBe listOf(PlaylistHomeNavKey, MusicDetailNavKey(id = music.id), MusicAddNavKey)
        waitUntilDisplayed(music)
        assertAddOnDetailPane()
        pressBack()
        restoredBackStack.toList() shouldBe listOf(PlaylistHomeNavKey, MusicDetailNavKey(id = music.id))
        assertDetailOnDetailPane()
        pressBack()
        restoredBackStack.toList() shouldBe listOf(PlaylistHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-6d84aaed-9550-46d8-873e-87b1fe455235 앱이 백그라운드에 다녀와도 상세 선택을 유지한다`() {
        val music = testMusic(title = listedMusicTitle())
        setPlaylistNavDisplay(music = music)
        composeRule.onNodeWithText(music.detail.title).performClick()
        composeRule.waitForIdle()
        assertDetailOnDetailPane()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey, MusicDetailNavKey(id = music.id))
        composeRule.onNodeWithText(music.detail.title).assertExists()
        assertDetailOnDetailPane()
        pressBack()
        backStack.toList() shouldBe listOf(NavigationTestMoreNavKey, PlaylistHomeNavKey)
        assertAddOnDetailPane()
    }

    private fun assertDetailOnDetailPane() {
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertExists()
    }

    private fun assertAddOnDetailPane() {
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_MUSIC_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
    }

    private fun waitUntilDisplayed(music: Music) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(music.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
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

    // 시스템이 되살릴 때 전환 이력이 저장된 상태에서 다시 만들어지도록 제품 내비게이션 키 직렬화 구성으로 전환 이력을 기억한다.
    private fun setRestorablePlaylistNavDisplay(
        musicList: List<Music>,
        restorationTester: StateRestorationTester,
        onBackStack: (NavBackStack<ScreenNavKey>) -> Unit,
    ) {
        val module = viewModelModule(musicList = musicList, detailTitle = "detail-${fixtureMonkey.giveMeOne<Int>()}")
        restorationTester.setContent {
            PlaylistNavigationTestHost(module = module) {
                val restorableBackStack =
                    rememberSerializable(
                        configuration = playlistSavedStateConfiguration,
                        serializer = NavBackStackSerializer(PolymorphicSerializer(ScreenNavKey::class)),
                    ) { NavBackStack<ScreenNavKey>(PlaylistHomeNavKey) }
                val holder = rememberListDetailPlaceholderStateHolder()
                onBackStack(restorableBackStack)

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = restorableBackStack,
                        sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider = entryProvider { playlistEntry(backStack = restorableBackStack) },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        musicList.firstOrNull()?.let(::waitUntilDisplayed)
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
                mockk<PlaylistHomeViewModel>(relaxed = true) {
                    every { musicPagingData } returns MutableStateFlow(musicPagingDataOf(musicList))
                    every { uiState } returns MutableStateFlow(PlaylistHomeUiState())
                    every { effect } returns emptyFlow()
                }.also { viewModel -> homeViewModelList += viewModel }
            }
            factory<SyncRefreshViewModel> {
                mockk(relaxed = true) {
                    every { uiState } returns MutableStateFlow(SyncRefreshUiState())
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
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
        val playlistSavedStateConfiguration: SavedStateConfiguration =
            SavedStateConfiguration {
                serializersModule =
                    SerializersModule {
                        polymorphic(ScreenNavKey::class) { playlistNavKeys() }
                    }
            }
    }
}

// 플레이리스트 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object NavigationTestMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
