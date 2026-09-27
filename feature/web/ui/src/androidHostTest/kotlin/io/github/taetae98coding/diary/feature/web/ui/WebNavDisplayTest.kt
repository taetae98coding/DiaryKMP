@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.web.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
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
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddTagViewModel
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddUiState
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddViewModel
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_MEMO_ADD_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoSyncViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.memoScreenPageViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.memoScreenWebViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.selectWebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.detail.testContentUiState
import io.github.taetae98coding.diary.feature.web.ui.home.WEB_HOME_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.web.ui.home.WebHomeSyncViewModel
import io.github.taetae98coding.diary.feature.web.ui.home.WebHomeUiState
import io.github.taetae98coding.diary.feature.web.ui.home.WebHomeViewModel
import io.github.taetae98coding.diary.feature.web.ui.home.testWeb
import io.github.taetae98coding.diary.feature.web.ui.home.webPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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

// 웹 목록과 그 목록에서 이어 가는 화면 사이의 전환을 제품 entry로 구성한 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w400dp-h800dp")
class WebNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(WebNavDisplayMoreNavKey, WebHomeNavKey)
    private var homeWebList: List<Web> = emptyList()

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
    fun `TC-WEB-HOME-DOMAIN-008 웹 추가로 이동한 뒤 뒤로가도 보던 목록 위치를 유지한다`() {
        assertPositionRetained { composeRule.onNodeWithContentDescription(DEFAULT_ADD_WEB_DESCRIPTION).performClick() }
    }

    @Test
    fun `TC-WEB-HOME-DOMAIN-008 웹 상세로 이동한 뒤 뒤로가도 보던 목록 위치를 유지한다`() {
        assertPositionRetained { webList -> composeRule.onNode(webCard(webList[POSITION_SCROLL_INDEX])).performClick() }
    }

    @Test
    fun `TC-WEB-HOME-DOMAIN-008 검색으로 이동한 뒤 뒤로가도 보던 목록 위치를 유지한다`() {
        assertPositionRetained { composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).performClick() }
    }

    @Test
    fun `TC-WEB-HOME-DOMAIN-009 더보기로 나갔다 다시 진입하면 목록의 맨 위부터 보여 준다`() {
        val webList = positionWebList()
        setWebNavDisplay(webList = webList)
        scrollToPositionWeb(webList)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.add(WebHomeNavKey) }
        composeRule.waitForIdle()

        composeRule.onNode(webCard(webList.first())).assertIsDisplayed()
        composeRule.onNode(webCard(webList[POSITION_SCROLL_INDEX])).assertDoesNotExist()
    }

    // 목록 위치는 기기에 남기지 않으므로, 앱을 다시 실행한 화면은 저장된 상태 없이 새로 그린 화면과 같다.
    @Test
    fun `TC-WEB-HOME-DOMAIN-009 앱을 다시 실행해 진입하면 목록의 맨 위부터 보여 준다`() {
        val webList = positionWebList()

        setWebNavDisplay(webList = webList)

        composeRule.onNode(webCard(webList.first())).assertIsDisplayed()
        composeRule.onNode(webCard(webList[POSITION_SCROLL_INDEX])).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-019 WebHome의 웹 목록에서 고른 웹 항목의 상세에서 메모 탭을 제공한다`() {
        val web = listedWeb()
        setWebNavDisplay(webList = listOf(web))

        composeRule.onNode(webCard(web)).performClick()
        composeRule.waitForIdle()
        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        backStack.toList() shouldBe listOf(WebNavDisplayMoreNavKey, WebHomeNavKey, WebDetailNavKey(id = web.id))
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-021 메모 탭에서 연 메모 추가에서 뒤로가면 메모 탭이 선택된 같은 웹 항목의 상세로 돌아온다`() {
        val web = openMemoTabOfListedWeb()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertIsDisplayed()
        pressBack()

        backStack.toList() shouldBe listOf(WebNavDisplayMoreNavKey, WebHomeNavKey, WebDetailNavKey(id = web.id))
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-021 메모 탭에서 연 메모 상세에서 뒤로가면 메모 탭이 선택된 같은 웹 항목의 상세로 돌아온다`() {
        val web = openMemoTabOfListedWeb()

        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertIsDisplayed()
        pressBack()

        backStack.toList() shouldBe listOf(WebNavDisplayMoreNavKey, WebHomeNavKey, WebDetailNavKey(id = web.id))
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    private fun assertPositionRetained(navigate: (List<Web>) -> Unit) {
        val webList = positionWebList()
        setWebNavDisplay(webList = webList)
        scrollToPositionWeb(webList)

        navigate(webList)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(WEB_HOME_LIST_TEST_TAG).assertDoesNotExist()
        pressBack()

        assertPositionWebDisplayed(webList)
    }

    private fun openMemoTabOfListedWeb(): Web {
        val web = listedWeb()
        setWebNavDisplay(webList = listOf(web))
        composeRule.onNode(webCard(web)).performClick()
        composeRule.waitForIdle()
        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        return web
    }

    private fun scrollToPositionWeb(webList: List<Web>) {
        waitUntilWebIsDisplayed(webList.first())
        composeRule.onNodeWithTag(WEB_HOME_LIST_TEST_TAG).performScrollToIndex(POSITION_SCROLL_INDEX)
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
        composeRule.setContent {
            WebNavDisplayTestHost {
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
                                entry<WebNavDisplayMoreNavKey> { Text(text = MORE_CONTENT) }
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
    private fun WebNavDisplayTestHost(content: @Composable () -> Unit) {
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
            KoinApplication(configuration = koinConfiguration { modules(navDisplayViewModelModule()) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory {
                mockk<WebHomeViewModel>(relaxed = true).apply {
                    every { webPagingData } returns MutableStateFlow(webPagingDataOf(homeWebList))
                    every { sort } returns MutableStateFlow(ListSort.TITLE)
                    every { effect } returns emptyFlow()
                }
            }
            factory {
                mockk<WebHomeSyncViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(WebHomeUiState())
                }
            }
            factory {
                mockk<WebAddViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(WebAddUiState())
                    every { effect } returns emptyFlow()
                }
            }
            factory { addTagViewModel() }
            factory { parameters ->
                val id = parameters.get<Uuid>()
                memoScreenWebViewModel(uiState = MutableStateFlow(testContentUiState(detail = homeWebList.first { web -> web.id == id }.detail)))
            }
            factory { memoScreenPageViewModel() }
            factory { detailTagScreenTestViewModel() }
            factory {
                mockk<WebDetailMemoViewModel>(relaxed = true).apply {
                    every { memoPagingData } returns flowOf(PagingData.empty())
                    every { sort } returns MutableStateFlow(ListSort.DEFAULT)
                    every { effect } returns emptyFlow()
                }
            }
            factory {
                mockk<WebDetailMemoSyncViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(MemoListUiState())
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
        const val DEFAULT_ADD_WEB_DESCRIPTION = "Add web"
        const val DEFAULT_SEARCH_DESCRIPTION = "Search"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val POSITION_WEB_COUNT = 30
        const val POSITION_SCROLL_INDEX = 25
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun webCard(web: Web): SemanticsMatcher = hasTestTag(WEB_CARD_TEST_TAG) and hasText(web.detail.title)

        fun listedWeb(): Web = testWeb(title = "list-${fixtureMonkey.giveMeOne<String>()}")

        fun positionWebList(): List<Web> = List(POSITION_WEB_COUNT) { index -> testWeb(title = "position-${index.toString().padStart(length = 2, padChar = '0')}-${fixtureMonkey.giveMeOne<Int>()}") }
    }
}

// 웹 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object WebNavDisplayMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
