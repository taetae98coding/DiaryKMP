@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
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
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.ui.home.MemoHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.memo.ui.home.MemoHomeSyncViewModel
import io.github.taetae98coding.diary.feature.memo.ui.home.MemoHomeViewModel
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.memoEntry
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
import io.github.taetae98coding.diary.feature.memo.ui.home.screenTestSyncViewModel as homeScreenTestSyncViewModel

// 짝이 되는 목록으로 돌아갔다가 다시 들어오는 것은 화면 전환 이력의 변화이므로, 제품 memoEntry로 메모 목적지를 띄워 이력을 바꾼다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoFinishedListReentryTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val backStack = NavBackStack<ScreenNavKey>(ReentryTopLevelNavKey, MemoHomeNavKey, MemoFinishedListNavKey)
    private val finishedViewModelList = mutableListOf<MemoFinishedListViewModel>()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-DOMAIN-009 MemoHome 목록으로 돌아갔다가 다시 진입하면 처음 정렬로 맨 위부터 보인다`() {
        val itemList = memoItemList()
        setMemoNavDisplay(itemList = itemList)
        composeRule.onNodeWithContentDescription(SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(TITLE_SORT).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(SORT_SHEET_TITLE).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText(TITLE_SORT).assertIsDisplayed()
        composeRule.onNodeWithTag(MEMO_FINISHED_LIST_TEST_TAG).performScrollToIndex(itemList.lastIndex)
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(memoTitle(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MEMO_HOME_MARKER_TITLE).assertIsDisplayed()
        composeRule.runOnIdle { backStack.add(MemoFinishedListNavKey) }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoTitle(index = 0)).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(DEFAULT_SORT).assertIsDisplayed()
        composeRule.onNodeWithText(TITLE_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(memoTitle(index = 0)).assertIsDisplayed()
        finishedViewModelList.size shouldBe 2
    }

    private fun viewModelModule(itemList: List<MemoListItem>) =
        module {
            factory<MemoFinishedListViewModel> {
                val sort = MutableStateFlow(ListSort.DEFAULT)

                mockk<MemoFinishedListViewModel>(relaxed = true) {
                    every { this@mockk.sort } returns sort
                    every { select(sort = any()) } answers { sort.value = firstArg() }
                    every { memoPagingData } returns MutableStateFlow(memoPagingDataOf(itemList = itemList))
                    every { effect } returns emptyFlow()
                }.also { viewModel -> finishedViewModelList += viewModel }
            }
            factory<MemoFinishedListSyncViewModel> { screenTestSyncViewModel() }
            factory<MemoHomeViewModel> {
                mockk<MemoHomeViewModel>(relaxed = true) {
                    every { sort } returns MutableStateFlow(ListSort.DEFAULT)
                    every { memoPagingData } returns
                        MutableStateFlow(memoPagingDataOf(itemList = listOf(MemoListItem.Content(memo = finishedMemo(title = MEMO_HOME_MARKER_TITLE)))))
                    every { filterUiState } returns MutableStateFlow(MemoHomeScaffoldFilterUiState())
                    every { effect } returns emptyFlow()
                }
            }
            factory<MemoHomeSyncViewModel> { homeScreenTestSyncViewModel() }
        }

    private fun setMemoNavDisplay(itemList: List<MemoListItem>) {
        val viewModelModule = viewModelModule(itemList = itemList)

        composeRule.setContent {
            // 테스트 호스트의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
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
                KoinApplication(configuration = koinConfiguration { modules(viewModelModule) }) {
                    DiaryTheme {
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
                                        entry<ReentryTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                                        memoEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoTitle(index = 0)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val MEMO_COUNT = 40
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val MEMO_HOME_MARKER_TITLE = "MemoHomeReentryMarker"
        const val SORT_DESCRIPTION = "List sort"
        const val SORT_SHEET_TITLE = "Sort"
        const val DEFAULT_SORT = "Default"
        const val TITLE_SORT = "Title"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun memoTitle(index: Int): String = "MemoFinishedListReentryTitle${index.toString().padStart(length = 2, padChar = '0')}"

        fun finishedMemo(title: String): Memo =
            fixtureMonkey.memo(isFinished = true, isDeleted = false, primaryTagId = null).let { memo ->
                memo.copy(detail = memo.detail.copy(title = title, dateTime = null))
            }

        fun memoItemList(): List<MemoListItem> = List(MEMO_COUNT) { index -> MemoListItem.Content(memo = finishedMemo(title = memoTitle(index = index))) }
    }
}

// 캘린더 홈처럼 메모가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object ReentryTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "ReentryTopLevel"
}
