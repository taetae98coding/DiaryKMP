@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntry
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
import kotlin.time.Instant

// 공통 내비게이션이 목적지를 옮길 때처럼 전환 이력에서 태그 목적지를 뺐다가 다시 넣고, 제품 tagEntry가 그 목적지를 처음 들어온 것처럼 시작하는지 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagHomeDestinationReturnTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val backStack = NavBackStack<ScreenNavKey>(DestinationTestTopLevelNavKey, TagHomeNavKey)
    private val tagViewModelList = mutableListOf<TagHomeViewModel>()

    // 필터 선택은 기기에 저장되어 화면을 다시 만들어도 이어지므로, ViewModel이 바뀌어도 같은 저장값을 읽게 한다.
    private val storedFilterUiState =
        MutableStateFlow(TagHomeScaffoldFilterUiState(isApplied = true))

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로, 앞선 테스트가 남긴 전역 Koin도 시작 전에 정리한다.
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
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-015 태그 목적지에 다녀오면 첫 항목과 처음 정렬로 시작하고 필터는 이어진다`() {
        val tagList = tagList()
        setTagNavDisplay(tagList = tagList)
        composeRule.runOnIdle { tagViewModelList.last().select(sort = ListSort.RECENTLY_UPDATED) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(RECENTLY_UPDATED_SORT_LABEL).assertIsDisplayed()
        composeRule.onNodeWithTag(TAG_HOME_LIST_TEST_TAG).performScrollToIndex(tagList.lastIndex)
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(tagTitle(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true

        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(DestinationTestTopLevelNavKey)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TOP_LEVEL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.add(TagHomeNavKey) }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tagTitle(index = 0)).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(tagTitle(index = 0)).assertIsDisplayed()
        composeRule.onNodeWithText(TITLE_SORT_LABEL).assertIsDisplayed()
        composeRule.onNode(hasStateDescription(FILTER_APPLIED_STATE_DESCRIPTION)).assertExists()
    }

    private fun setTagNavDisplay(tagList: List<Tag>) {
        val viewModelModule =
            module {
                factory<TagHomeViewModel> {
                    val sort = MutableStateFlow(ListSort.TITLE)

                    mockk<TagHomeViewModel>(relaxed = true) {
                        every { this@mockk.sort } returns sort
                        every { select(sort = any()) } answers { sort.value = firstArg() }
                        every { tagPagingData } returns MutableStateFlow(tagPagingDataOf(tagList))
                        every { filterUiState } returns storedFilterUiState
                        every { effect } returns emptyFlow()
                    }.also { viewModel -> tagViewModelList += viewModel }
                }
                factory<TagHomeSyncViewModel> { screenTestSyncViewModel() }
            }

        composeRule.setContent {
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
                                        entry<DestinationTestTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                                        tagEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tagTitle(index = 0)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        const val TAG_COUNT = 40
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val TITLE_SORT_LABEL = "Title"
        const val RECENTLY_UPDATED_SORT_LABEL = "Recently updated"
        const val FILTER_APPLIED_STATE_DESCRIPTION = "Filter applied"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun tagTitle(index: Int): String = "TagHomeDestinationReturnTitle$index"

        fun tagList(): List<Tag> =
            List(TAG_COUNT) { index ->
                fixtureMonkey
                    .giveMeKotlinBuilder<Tag>()
                    .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = tagTitle(index = index)))
                    .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                    .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                    .sample()
            }
    }
}

// 캘린더 홈처럼 태그가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object DestinationTestTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "DestinationTestTopLevel"
}
