@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.place.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_MEMO_ADD_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.FIRST_PLACE_ID
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailUiState
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.content
import io.github.taetae98coding.diary.feature.place.ui.detail.contentSharerFactoryOf
import io.github.taetae98coding.diary.feature.place.ui.detail.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PlaceDetailMemoViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.placeMemo
import io.github.taetae98coding.diary.feature.place.ui.detail.placeMemoPagingData
import io.github.taetae98coding.diary.feature.place.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.searchScreenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.selectPlaceDetailTab
import io.github.taetae98coding.diary.feature.place.ui.memo.finished.PlaceMemoFinishedListViewModel
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
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

// PlaceDetail 화면의 메모 탭에서 이어 가는 화면과의 전환을 제품 entry로 구성한 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w400dp-h800dp")
class PlaceNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(PlaceNavDisplayPreviousNavKey, PlaceDetailNavKey(id = FIRST_PLACE_ID))
    private val memo: Memo = placeMemo(title = MEMO_TITLE)
    private val otherPlaceMemo: Memo = placeMemo(title = OTHER_PLACE_MEMO_TITLE)

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
    fun `TC-d58e8805-d2ad-42f2-a534-5ff654141413 메모 탭에서 연 MemoAdd에서 뒤로가면 메모 탭이 선택된 같은 장소의 상세로 돌아온다`() {
        openMemoTab()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe MemoAddNavKey(initialPlaceId = FIRST_PLACE_ID)
        composeRule.onNodeWithText(ROUTE_CONTENT).assertIsDisplayed()
        pressBack()

        assertReturnedToMemoTab()
    }

    @Test
    fun `TC-d58e8805-d2ad-42f2-a534-5ff654141413 메모 탭에서 연 MemoDetail에서 뒤로가면 메모 탭이 선택된 같은 장소의 상세로 돌아온다`() {
        openMemoTab()

        composeRule.onNodeWithText(MEMO_TITLE).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe MemoDetailNavKey(id = memo.id)
        composeRule.onNodeWithText(ROUTE_CONTENT).assertIsDisplayed()
        pressBack()

        assertReturnedToMemoTab()
    }

    @Test
    fun `TC-d58e8805-d2ad-42f2-a534-5ff654141413 메모 탭에서 연 PlaceMemoFinishedList에서 뒤로가면 메모 탭이 선택된 같은 장소의 상세로 돌아온다`() {
        openMemoTab()

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe PlaceMemoFinishedListNavKey(placeId = FIRST_PLACE_ID)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_SUBTITLE).assertIsDisplayed()
        pressBack()

        assertReturnedToMemoTab()
    }

    @Test
    fun `TC-53573661-7474-4f93-ad23-b9194032b27d 상세 대상이 다른 장소로 바뀌면 메모 탭 목록이 새 장소의 메모로 바뀐다`() {
        val otherPlaceId = Arb.kotlinUuid().next()
        openMemoTab()
        composeRule.onNodeWithText(OTHER_PLACE_MEMO_TITLE).assertDoesNotExist()

        // 장소 목록에서 다른 장소를 고르면 상세 영역의 대상이 그 장소로 바뀐다.
        composeRule.runOnIdle { backStack[backStack.lastIndex] = PlaceDetailNavKey(id = otherPlaceId) }
        composeRule.waitForIdle()
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(OTHER_PLACE_MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(OTHER_PLACE_MEMO_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()
    }

    private fun openMemoTab() {
        setPlaceNavDisplay()
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertReturnedToMemoTab() {
        backStack.toList() shouldBe listOf(PlaceNavDisplayPreviousNavKey, PlaceDetailNavKey(id = FIRST_PLACE_ID))
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setPlaceNavDisplay() {
        composeRule.setContent {
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
                    DiaryTheme {
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
                                        entry<PlaceNavDisplayPreviousNavKey> { Text(text = PREVIOUS_CONTENT) }
                                        placeEntry(backStack = backStack, contentSharerFactory = contentSharerFactoryOf())
                                        entry<MemoAddNavKey> { Text(text = ROUTE_CONTENT) }
                                        entry<MemoDetailNavKey> { Text(text = ROUTE_CONTENT) }
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory<PlaceDetailViewModel> { parameters -> screenTestViewModel(uiState = MutableStateFlow<PlaceDetailUiState>(content(id = parameters.get<Uuid>()))) }
            factory<PlaceSearchViewModel> { searchScreenTestViewModel() }
            factory<EntityDetailTagViewModel> { detailTagScreenTestViewModel() }
            factory { parameters ->
                val placeMemo = if (parameters.get<Uuid>() == FIRST_PLACE_ID) memo else otherPlaceMemo

                mockk<PlaceDetailMemoViewModel>(relaxed = true).apply {
                    every { memoPagingData } returns flowOf(placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = placeMemo))))
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
                    every { effect } returns emptyFlow()
                }
            }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(SyncRefreshUiState())
                }
            }
            factory {
                mockk<PlaceMemoFinishedListViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(EntityMemoFinishedListUiState())
                    every { memoPagingData } returns flowOf(PagingData.empty())
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
                    every { effect } returns emptyFlow()
                }
            }
        }

    private companion object {
        const val MEMO_TITLE = "PlaceNavDisplayMemo"
        const val OTHER_PLACE_MEMO_TITLE = "PlaceNavDisplayOtherPlaceMemo"
        const val PREVIOUS_CONTENT = "PreviousContent"
        const val ROUTE_CONTENT = "RouteContent"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val DEFAULT_FINISHED_LIST_SUBTITLE = "Finished Memos"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
    }
}

// PlaceDetail로 들어가는 이전 화면을 대신한다. 이전 화면은 이 모듈의 관심이 아니므로 대역을 사용한다.
private data object PlaceNavDisplayPreviousNavKey : ScreenNavKey {
    override val screenName: String
        get() = "PlaceNavDisplayPrevious"
}
