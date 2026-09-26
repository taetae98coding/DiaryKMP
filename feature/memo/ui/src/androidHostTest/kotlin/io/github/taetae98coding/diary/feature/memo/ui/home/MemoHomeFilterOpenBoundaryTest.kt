@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.ui.home.filter.MemoHomeFilterUiState
import io.github.taetae98coding.diary.feature.memo.ui.home.filter.MemoHomeFilterViewModel
import io.github.taetae98coding.diary.feature.memo.ui.memoEntry
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
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

// 필터는 메모 목록 위에 쌓이는 별도 화면이라, 제품 memoEntry와 앱과 같은 장면 전략으로 메모 목적지를 띄운 채 경계를 지난다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoHomeFilterOpenBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(FilterBoundaryTopLevelNavKey, MemoHomeNavKey, MemoHomeFilterNavKey)
    private val filterViewModelList = mutableListOf<MemoHomeFilterViewModel>()

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
    fun `TC-MEMO-HOME-FEATURE-073 필터를 연 채 화면이 재생성되어도 필터는 열린 채로 남는다`() {
        val tag = filterTag()
        val viewModelStoreOwner = testViewModelStoreOwner()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            MemoDestination(tag = tag, viewModelStoreOwner = viewModelStoreOwner)
        }
        assertFilterOpened(tag = tag)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertFilterOpened(tag = tag)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-073 필터를 연 채 앱이 백그라운드에 다녀와도 필터는 열린 채로 남는다`() {
        val tag = filterTag()
        val viewModelStoreOwner = testViewModelStoreOwner()
        composeRule.setContent {
            MemoDestination(tag = tag, viewModelStoreOwner = viewModelStoreOwner)
        }
        assertFilterOpened(tag = tag)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertFilterOpened(tag = tag)
    }

    @Test
    fun `TC-MEMO-HOME-DOMAIN-023 필터를 연 채 시스템이 앱을 정리한 뒤 다시 만들면 필터가 열린 채로 다시 보인다`() {
        val tag = filterTag()
        val restorationTester = StateRestorationTester(composeRule)
        // 시스템이 앱을 정리하면 ViewModel도 사라지므로, 다시 만들 때 새 소유자에서 새 ViewModel을 받게 한다.
        restorationTester.setContent {
            val viewModelStoreOwner = remember { testViewModelStoreOwner() }
            MemoDestination(tag = tag, viewModelStoreOwner = viewModelStoreOwner)
        }
        assertFilterOpened(tag = tag)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertFilterOpened(tag = tag)
        filterViewModelList.size shouldBe 2
    }

    @Composable
    private fun MemoDestination(
        tag: Tag,
        viewModelStoreOwner: ViewModelStoreOwner,
    ) {
        val resultEventBus = remember { ResultEventBus() }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner,
            LocalResultEventBus provides resultEventBus,
        ) {
            KoinApplication(configuration = koinConfiguration { modules(viewModelModule(tag = tag)) }) {
                DiaryTheme {
                    val holder = rememberListDetailPlaceholderStateHolder()

                    CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                        NavDisplay(
                            backStack = backStack,
                            sceneStrategies =
                                listOf(
                                    remember { BottomSheetSceneStrategy<ScreenNavKey>() },
                                    rememberDiaryListDetailSceneStrategy(),
                                ),
                            entryDecorators =
                                listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator(),
                                    rememberListDetailPlaceholderNavEntryDecorator(holder),
                                ),
                            entryProvider =
                                entryProvider {
                                    entry<FilterBoundaryTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                                    memoEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                                },
                        )
                    }
                }
            }
        }
    }

    private fun viewModelModule(tag: Tag) =
        module {
            factory<MemoHomeViewModel> {
                mockk<MemoHomeViewModel>(relaxed = true) {
                    every { sort } returns MutableStateFlow(ListSort.DEFAULT)
                    every { memoPagingData } returns MutableStateFlow(memoPagingDataOf(itemList = emptyList()))
                    every { filterUiState } returns MutableStateFlow(MemoHomeScaffoldFilterUiState(existence = storedExistence()))
                    every { effect } returns emptyFlow()
                }
            }
            factory<MemoHomeSyncViewModel> { screenTestSyncViewModel() }
            factory<MemoHomeFilterViewModel> {
                // 필터 선택은 기기에 저장되어 있으므로 새로 만든 ViewModel도 같은 선택을 읽는다.
                mockk<MemoHomeFilterViewModel>(relaxed = true) {
                    every { uiState } returns
                        MutableStateFlow(MemoHomeFilterUiState(selectedTagIdSet = setOf(tag.id), existence = storedExistence()))
                    every { tagPagingData } returns flowOf(PagingData.from(listOf(tag)))
                }.also { viewModel -> filterViewModelList += viewModel }
            }
        }

    private fun assertFilterOpened(tag: Tag) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithContentDescription("$DATE_LABEL $EXIST_LABEL").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("$DATE_LABEL $EXIST_LABEL").assertIsSelected()
        composeRule.onNodeWithText(tag.detail.title).assertIsSelected()
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val DATE_LABEL = "Date"
        const val EXIST_LABEL = "With"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun filterTag(): Tag = testTag(title = "filter-tag-${fixtureMonkey.giveMeOne<String>()}")

        fun storedExistence(): MemoExistenceFilter = MemoExistenceFilter(date = MemoFilterExistence.EXIST)

        fun testViewModelStoreOwner(): ViewModelStoreOwner =
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }
    }
}

// 캘린더 홈처럼 메모가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object FilterBoundaryTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "FilterBoundaryTopLevel"
}
