@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.web.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
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
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.feature.web.ui.add.INPUT_COUNT_WITHOUT_HEADER
import io.github.taetae98coding.diary.feature.web.ui.add.TYPED_URL
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddEffect
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddTagViewModel
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddUiState
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddViewModel
import io.github.taetae98coding.diary.feature.web.ui.add.clickAdd
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.add.inputCount
import io.github.taetae98coding.diary.feature.web.ui.add.titleInput
import io.github.taetae98coding.diary.feature.web.ui.add.urlInput
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.memoScreenPageViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.memoScreenWebViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.testContentUiState
import io.github.taetae98coding.diary.feature.web.ui.home.WebHomeViewModel
import io.github.taetae98coding.diary.feature.web.ui.home.testWeb
import io.github.taetae98coding.diary.feature.web.ui.home.webPagingDataOf
import io.github.taetae98coding.diary.feature.web.ui.memo.finished.WebMemoFinishedListViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
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

// 웹 목록과 상세 영역의 웹 추가를 함께 표시하는 너비에서, 제품 webEntry로 구성한 NavDisplay 위에서 배치의 전환을 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class WebListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(WebListDetailMoreNavKey, WebHomeNavKey)
    private var homeWebList: List<Web> = emptyList()
    private val homePagingData = MutableStateFlow(webPagingDataOf(emptyList()))

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트 앞뒤로 전역 Koin을 정리한다.
    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-0436f577-1128-4d24-8b33-c59681e04d56 상세 영역의 웹 추가에서 추가에 성공해도 상세 영역은 웹 추가로 남고 추가한 웹 항목이 목록에 나타난다`() {
        val listedWeb = listedWeb()
        setWebNavDisplay(webList = listOf(listedWeb))
        waitUntilWebIsDisplayed(listedWeb)
        composeRule.titleInput().performTextInput(ADDED_TITLE)
        composeRule.urlInput().performTextInput(TYPED_URL)
        composeRule.waitForIdle()

        composeRule.clickAdd()

        backStack.toList() shouldBe listOf(WebListDetailMoreNavKey, WebHomeNavKey)
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER
        composeRule.titleInput().assert(hasText(""))
        composeRule.urlInput().assert(hasText(""))
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasTestTag(WEB_CARD_TEST_TAG) and hasText(ADDED_TITLE)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(webCard(listedWeb)).assertIsDisplayed()
    }

    @Test
    fun `TC-18fd5320-3e21-4050-b98b-c3ee38fc6fec 웹 상세에서 뒤로 돌아오면 목록과 웹 추가를 함께 쓰는 상태로 돌아오고 보던 목록을 그대로 본다`() {
        val webList = positionWebList()
        setWebNavDisplay(webList = webList)
        scrollToPositionWeb(webList)

        composeRule.onNode(webCard(webList[POSITION_SCROLL_INDEX])).performClick()
        composeRule.waitForIdle()

        backStack.last() shouldBe WebDetailNavKey(id = webList[POSITION_SCROLL_INDEX].id)
        composeRule.refreshableList().assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_WEB_DESCRIPTION).assertDoesNotExist()

        pressBack()

        backStack.toList() shouldBe listOf(WebListDetailMoreNavKey, WebHomeNavKey)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_WEB_DESCRIPTION).assertIsDisplayed()
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER
        assertPositionWebDisplayed(webList)
    }

    @Test
    fun `TC-89f89f68-82bb-4884-9cc1-c0c269e180d6 목록에서 검색으로 이동하면 검색 화면을 목록 없이 단독으로 표시한다`() {
        val listedWeb = listedWeb()
        setWebNavDisplay(webList = listOf(listedWeb))
        waitUntilWebIsDisplayed(listedWeb)
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.last().shouldBeInstanceOf<SearchHomeNavKey>()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertIsDisplayed()
        composeRule.refreshableList().assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    @Test
    fun `TC-90d8f866-86e9-4286-9a54-885e416c1536 웹 추가가 함께 놓여 있어도 목록의 뒤로가기는 더보기로 돌아간다`() {
        val listedWeb = listedWeb()
        setWebNavDisplay(webList = listOf(listedWeb))
        waitUntilWebIsDisplayed(listedWeb)
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(WebListDetailMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertIsDisplayed()
    }

    @Test
    fun `TC-a4f96282-7dfd-4902-b5f8-874cb19af10d 상세 영역에 웹 추가가 놓인 상태에서 시스템 뒤로가기를 사용하면 더보기로 돌아간다`() {
        val listedWeb = listedWeb()
        setWebNavDisplay(webList = listOf(listedWeb))
        waitUntilWebIsDisplayed(listedWeb)
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER

        pressBack()

        backStack.toList() shouldBe listOf(WebListDetailMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertIsDisplayed()
        composeRule.onNode(webCard(listedWeb)).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    private fun scrollToPositionWeb(webList: List<Web>) {
        waitUntilWebIsDisplayed(webList.first())
        composeRule.refreshableList().performScrollToIndex(POSITION_SCROLL_INDEX)
        composeRule.waitForIdle()
        assertPositionWebDisplayed(webList)
    }

    private fun assertPositionWebDisplayed(webList: List<Web>) {
        composeRule.onNode(webCard(webList[POSITION_SCROLL_INDEX])).assertIsDisplayed()
        composeRule.onNode(webCard(webList.first())).assertDoesNotExist()
    }

    private fun waitUntilWebIsDisplayed(web: Web) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(web.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setWebNavDisplay(webList: List<Web>) {
        homeWebList = webList
        homePagingData.value = webPagingDataOf(webList)
        composeRule.setContent {
            WebListDetailTestHost {
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
                                entry<WebListDetailMoreNavKey> { Text(text = MORE_CONTENT) }
                                webEntry(backStack = backStack)
                                entry<SearchHomeNavKey> { Text(text = ROUTE_CONTENT) }
                                entry<MemoAddNavKey> { Text(text = ROUTE_CONTENT) }
                                entry<MemoDetailNavKey> { Text(text = ROUTE_CONTENT) }
                                entry<TagAddNavKey> { Text(text = ROUTE_CONTENT) }
                                entry<TagDetailNavKey> { Text(text = ROUTE_CONTENT) }
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun WebListDetailTestHost(content: @Composable () -> Unit) {
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
            KoinApplication(configuration = koinConfiguration { modules(viewModelModule()) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun viewModelModule() =
        module {
            factory {
                mockk<WebHomeViewModel>(relaxed = true).apply {
                    every { webPagingData } returns homePagingData
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))
                    every { effect } returns emptyFlow()
                }
            }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(SyncRefreshUiState())
                }
            }
            factory {
                val effectChannel = Channel<WebAddEffect>(capacity = Channel.BUFFERED)
                mockk<WebAddViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(WebAddUiState())
                    every { effect } returns effectChannel.receiveAsFlow()
                    // 추가가 저장되면 목록이 다시 조회되는 것과 같게 추가한 웹 항목을 목록 조회 결과에 더한다.
                    every { add(detail = any(), tagIdSet = any()) } answers {
                        val detail = firstArg<WebDetail>()
                        val addedWeb = testWeb(title = detail.title, url = detail.url)
                        homeWebList = homeWebList + addedWeb
                        homePagingData.value = webPagingDataOf(homeWebList)
                        effectChannel.trySend(WebAddEffect.AddSucceeded(id = addedWeb.id)).getOrThrow()
                    }
                }
            }
            factory { addTagViewModel() }
            factory { parameters ->
                val id = parameters.get<Uuid>()
                memoScreenWebViewModel(uiState = MutableStateFlow(testContentUiState(detail = homeWebList.first { web -> web.id == id }.detail)))
            }
            factory { memoScreenPageViewModel() }
            factory {
                mockk<WebMemoFinishedListViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(EntityMemoFinishedListUiState())
                    every { memoPagingData } returns flowOf(PagingData.empty())
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
                    every { effect } returns emptyFlow()
                }
            }
            factory { detailTagScreenTestViewModel() }
            factory {
                mockk<WebDetailMemoViewModel>(relaxed = true).apply {
                    every { memoPagingData } returns flowOf(PagingData.empty())
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
                    every { effect } returns emptyFlow()
                }
            }
        }

    private fun addTagViewModel(): WebAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList())))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))

        return WebAddTagViewModel(
            initialTagId = null,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val ROUTE_CONTENT = "RouteContent"
        const val ADDED_TITLE = "added-web-title"
        const val DEFAULT_ADD_WEB_DESCRIPTION = "Add web"
        const val DEFAULT_SEARCH_DESCRIPTION = "Search"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val POSITION_WEB_COUNT = 60
        const val POSITION_SCROLL_INDEX = 50
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun webCard(web: Web): SemanticsMatcher = hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)

        fun listedWeb(): Web = testWeb(title = "list-${fixtureMonkey.giveMeOne<String>()}")

        fun positionWebList(): List<Web> = List(POSITION_WEB_COUNT) { index -> testWeb(title = "position-${index.toString().padStart(length = 2, padChar = '0')}-${fixtureMonkey.giveMeOne<Int>()}") }
    }
}

// 웹 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object WebListDetailMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
