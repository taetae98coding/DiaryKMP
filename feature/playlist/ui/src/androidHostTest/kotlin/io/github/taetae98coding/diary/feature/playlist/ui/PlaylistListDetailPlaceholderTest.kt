@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.ui.add.MusicAddViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.add.TYPED_LINK
import io.github.taetae98coding.diary.feature.playlist.ui.add.artistInput
import io.github.taetae98coding.diary.feature.playlist.ui.add.linkInput
import io.github.taetae98coding.diary.feature.playlist.ui.add.screenTestViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.add.titleInput
import io.github.taetae98coding.diary.feature.playlist.ui.addTagViewModel
import io.github.taetae98coding.diary.feature.playlist.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 선택한 곡이 없을 때의 곡 추가는 목록과 상세를 함께 표시할 때만 상세 영역에 놓이므로, 두 영역을 함께 표시하는 너비의 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class PlaylistListDetailPlaceholderTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val viewModelList = mutableListOf<MusicAddViewModel>()
    private val tagViewModelList = mutableListOf<EntityAddTagViewModel>()
    private val backStack = NavBackStack<ScreenNavKey>(MoreNavKey, PlaylistHomeNavKey)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-8279258c-59dc-42a7-9b8f-d621e66679fb 더보기로 돌아갔다가 다시 진입하면 상세 영역의 곡 추가는 입력이 모두 비어 있는 상태로 시작한다`() {
        val input = placeholderInput()
        setPlaylistNavDisplay(tag = input.tag)
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(PlaylistHomeNavKey) }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(""))
        composeRule.artistInput().assert(hasText(""))
        composeRule.linkInput().assert(hasText(""))
        viewModelList shouldHaveSize 2
    }

    @Test
    fun `TC-5ce27ad0-3a3c-4b52-a561-2448e09823f4 배치를 떠나기 전에는 상세 영역에서 곡 상세를 보고 돌아와도 곡 추가의 입력이 남는다`() {
        val input = placeholderInput()
        setPlaylistNavDisplay(tag = input.tag)
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle { backStack.add(MusicDetailNavKey(id = Uuid.random())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(input.title))
        composeRule.artistInput().assert(hasText(input.artist))
        composeRule.linkInput().assert(hasText(TYPED_LINK))
        viewModelList shouldHaveSize 1
    }

    @Test
    fun `TC-12d9d797-f3e8-4613-b86b-7039231331e7 더보기로 돌아갔다가 다시 진입하면 곡 추가는 태그를 고르지 않은 상태로 시작한다`() {
        val input = placeholderInput()
        setPlaylistNavDisplay(tag = input.tag)
        selectPlaceholderTag(tag = input.tag)

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(PlaylistHomeNavKey) }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(input.tag.detail.title).assertCountEquals(0)
        tagViewModelList shouldHaveSize 2
        composeRule.runOnIdle {
            tagViewModelList
                .last()
                .selectionUiState.value.tagIdSet
                .shouldBeEmpty()
        }
    }

    @Test
    fun `TC-10e9b582-bf4e-49c6-abbe-354605fed580 배치를 떠나기 전에는 곡 상세를 보고 돌아와도 곡 추가에서 고른 태그가 남는다`() {
        val input = placeholderInput()
        setPlaylistNavDisplay(tag = input.tag)
        selectPlaceholderTag(tag = input.tag)

        composeRule.runOnIdle { backStack.add(MusicDetailNavKey(id = Arb.kotlinUuid().next())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()
        composeRule.onAllNodesWithText(input.tag.detail.title).assertCountEquals(0)
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(input.tag.detail.title).onFirst().assertExists()
        tagViewModelList shouldHaveSize 1
    }

    private fun selectPlaceholderTag(tag: Tag) {
        composeRule.runOnIdle { tagViewModelList.last().add(tagId = tag.id) }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(tag.detail.title).onFirst().assertExists()
    }

    private fun setPlaylistNavDisplay(tag: Tag) {
        composeRule.setContent {
            PlaylistPlaceholderTestHost(tag = tag) {
                val holder = rememberListDetailPlaceholderStateHolder()

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider =
                            entryProvider {
                                entry<MoreNavKey> { Text(text = MORE_CONTENT) }
                                entry<PlaylistHomeNavKey>(
                                    clazzContentKey = { PLAYLIST_HOME_CONTENT_KEY },
                                    metadata = playlistHomeListPaneMetadata(backStack = backStack),
                                ) { Text(text = PLAYLIST_HOME_CONTENT) }
                                entry<MusicDetailNavKey>(
                                    metadata = ListDetailSceneStrategy.detailPane(sceneKey = PlaylistHomeNavKey),
                                ) { Text(text = ROUTE_CONTENT) }
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun PlaylistPlaceholderTestHost(
        tag: Tag,
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
            KoinApplication(configuration = koinConfiguration { modules(placeholderViewModelModule(tag = tag)) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 배치를 떠나면 ViewModel이 정리되므로 곡 추가 ViewModel은 정리 호출에 답하는 relaxed mock을 쓴다. 태그 ViewModel은 고른 태그가 남는지 봐야 하므로 실제 객체를 쓴다.
    // 둘 다 만들어진 인스턴스를 세어 저장 범위를 확인한다.
    private fun placeholderViewModelModule(tag: Tag) =
        module {
            factory { parameters ->
                addTagViewModel(initialTagId = parameters.getOrNull(), allTagList = listOf(tag)).also { viewModel -> tagViewModelList += viewModel }
            }
            factory { detailTagScreenTestViewModel() }
            factory { screenTestViewModel().also { viewModel -> viewModelList += viewModel } }
        }

    private fun ComposeContentTestRule.fillPlaceholder(input: PlaceholderInput) {
        titleInput().performTextInput(input.title)
        artistInput().performTextInput(input.artist)
        linkInput().performTextInput(TYPED_LINK)
        waitForIdle()
        titleInput().assert(hasText(input.title))
    }

    private data class PlaceholderInput(
        val title: String,
        val artist: String,
        val tag: Tag,
    )

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val PLAYLIST_HOME_CONTENT = "PlaylistHomeContent"
        const val ROUTE_CONTENT = "RouteContent"
        const val PLACEHOLDER_TAG_TITLE = "PlaylistPlaceholderTag"

        fun placeholderInput(): PlaceholderInput =
            PlaceholderInput(
                title = "title-${Arb.string().next()}",
                artist = "artist-${Arb.string().next()}",
                tag = Arb.tag(title = PLACEHOLDER_TAG_TITLE).next(),
            )
    }
}

// 플레이리스트 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object MoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
