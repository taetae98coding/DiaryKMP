@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.ui.home.TagHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.tag.ui.home.TagHomeViewModel
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntry
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

// 제품 tagEntry로 TagHome을 띄우고 완료된 태그 확인을 골라, 완료된 태그가 없어도 TagFinishedList로 이동하는지 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagFinishedListEntryTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val backStack = NavBackStack<ScreenNavKey>(EntryTestTopLevelNavKey, TagHomeNavKey)

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
    fun `TC-5d4b7229-e18a-49aa-9a25-758c8303a550 완료된 태그가 없어도 화면에 진입할 수 있다`() {
        setTagNavDisplay()
        composeRule.onAllNodesWithText(DEFAULT_FINISHED_LIST_TITLE).fetchSemanticsNodes().isEmpty() shouldBe true

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_BUTTON_LABEL).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(EntryTestTopLevelNavKey, TagHomeNavKey, TagFinishedListNavKey)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertIsDisplayed()
    }

    private fun setTagNavDisplay() {
        val viewModelModule =
            module {
                factory<TagHomeViewModel> {
                    mockk<TagHomeViewModel>(relaxed = true) {
                        every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))
                        every { tagPagingData } returns MutableStateFlow(tagPagingDataOf(emptyList()))
                        every { filterUiState } returns MutableStateFlow(TagHomeScaffoldFilterUiState(isLoaded = true, isApplied = false))
                        every { effect } returns emptyFlow()
                    }
                }
                // 완료된 태그가 하나도 없는 계정의 완료된 태그 목록이다.
                factory<TagFinishedListViewModel> {
                    mockk<TagFinishedListViewModel>(relaxed = true) {
                        every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))
                        every { tagPagingData } returns MutableStateFlow(tagPagingDataOf(emptyList()))
                        every { effect } returns emptyFlow()
                    }
                }
                factory<SyncRefreshViewModel> { screenTestSyncViewModel() }
            }

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
                                        entry<EntryTestTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                                        tagEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val DEFAULT_FINISHED_LIST_BUTTON_LABEL = "Finished tags"
        const val DEFAULT_FINISHED_LIST_TITLE = "Finished Tags"
        const val DEFAULT_EMPTY_TITLE = "No finished tags"
    }
}

// 캘린더 홈처럼 태그가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object EntryTestTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "EntryTestTopLevel"
}
