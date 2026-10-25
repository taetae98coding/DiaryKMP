@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DELETE_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_FINISH_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_RESTART_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.prepareTagDetailTabViewModels
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailTabViewModelModule
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntry
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
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
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

// 두 영역을 함께 표시하는 너비에서 제품 tagEntry로 완료된 태그 목록과 태그 상세를 띄우고, 상세 화면의 동작이 배치에 남기는 결과를 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class TagFinishedListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(FinishedListTestTopLevelNavKey, TagHomeNavKey, TagFinishedListNavKey)
    private var finishedTagList: List<Tag> = emptyList()
    private val finishedPagingData = MutableStateFlow(tagPagingDataOf(emptyList()))
    private val detailUiStateMap = mutableMapOf<Uuid, MutableStateFlow<TagDetailUiState>>()

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
    fun `TC-07e3d6d4-b18c-427a-8af8-3b78499f499b 목록에서 고른 태그 상세에서 삭제하면 선택 전 상태로 돌아간다`() {
        val tagList = tagList()
        setTagNavDisplay(tagList = tagList)
        selectTag(tagList[0])

        deleteOnDetail()

        backStack.toList() shouldBe listOf(FinishedListTestTopLevelNavKey, TagHomeNavKey, TagFinishedListNavKey)
        composeRule.onNodeWithText(listTitle(index = 0)).assertExists()
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertExists()
    }

    @Test
    fun `TC-07e3d6d4-b18c-427a-8af8-3b78499f499b 목록에서 다른 태그를 고른 뒤 삭제하면 선택 전 상태로 돌아간다`() {
        val tagList = tagList()
        setTagNavDisplay(tagList = tagList)
        selectTag(tagList[0])
        selectTag(tagList[1])

        deleteOnDetail()

        backStack.toList() shouldBe listOf(FinishedListTestTopLevelNavKey, TagHomeNavKey, TagFinishedListNavKey)
        composeRule.onNodeWithText(DEFAULT_PLACEHOLDER_TEXT).assertExists()
    }

    @Test
    fun `TC-07e3d6d4-b18c-427a-8af8-3b78499f499b 연결한 태그의 상세에서 삭제하면 목록에서 고른 태그 상세로 돌아간다`() {
        val tagList = tagList()
        val linkedId = fixtureMonkey.giveMeOne<Uuid>()
        setTagNavDisplay(tagList = tagList)
        selectTag(tagList[0])
        composeRule.runOnIdle { backStack.add(TagDetailNavKey(id = linkedId)) }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(detailTitle(id = linkedId)).fetchSemanticsNodes().isNotEmpty() shouldBe true

        deleteOnDetail()

        backStack.toList() shouldBe listOf(FinishedListTestTopLevelNavKey, TagHomeNavKey, TagFinishedListNavKey, TagDetailNavKey(id = tagList[0].id))
        composeRule.onAllNodesWithText(detailTitle(id = tagList[0].id)).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onAllNodesWithText(detailTitle(id = linkedId)).fetchSemanticsNodes().isEmpty() shouldBe true
    }

    @Test
    fun `TC-4f96edc1-e28d-4306-89aa-ea1aa3e27ce9 상세 영역에서 태그를 다시 시작하면 목록에서 사라지고 상세는 그대로 남는다`() {
        val tagList = tagList()
        setTagNavDisplay(tagList = tagList)
        selectTag(tagList[0])

        composeRule.onNode(actionButton(DEFAULT_RESTART_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()

        backStack.last() shouldBe TagDetailNavKey(id = tagList[0].id)
        composeRule.onAllNodesWithText(listTitle(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true
        composeRule.onNodeWithText(listTitle(index = 1)).assertExists()
        composeRule.onAllNodesWithText(detailTitle(id = tagList[0].id)).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onNode(actionButton(DEFAULT_FINISH_BUTTON_DESCRIPTION)).assertExists()
    }

    private fun selectTag(tag: Tag) {
        composeRule.onNodeWithText(tag.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(detailTitle(id = tag.id)).fetchSemanticsNodes().isNotEmpty() shouldBe true
    }

    private fun deleteOnDetail() {
        composeRule.onNode(actionButton(DEFAULT_DELETE_BUTTON_DESCRIPTION)).performClick()
        composeRule.waitForIdle()
    }

    private fun setTagNavDisplay(tagList: List<Tag>) {
        finishedTagList = tagList
        finishedPagingData.value = tagPagingDataOf(tagList)
        prepareTagDetailTabViewModels()
        val viewModelModule =
            module {
                factory<TagFinishedListViewModel> {
                    mockk<TagFinishedListViewModel>(relaxed = true) {
                        every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))
                        every { tagPagingData } returns finishedPagingData
                        every { effect } returns emptyFlow()
                    }
                }
                factory<TagDetailViewModel> { parameters -> detailViewModel(id = parameters.get<Uuid>()) }
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
                KoinApplication(configuration = koinConfiguration { modules(tagDetailTabViewModelModule, viewModelModule) }) {
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
                                        entry<FinishedListTestTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                                        tagEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(listTitle(index = 0)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun detailViewModel(id: Uuid): TagDetailViewModel {
        val uiState =
            detailUiStateMap.getOrPut(id) {
                MutableStateFlow(tagDetailUiState(id = id, detail = tagDetail(title = detailTitle(id = id)), isFinished = true))
            }
        val effectChannel = Channel<TagDetailEffect>(capacity = Channel.BUFFERED)

        return mockk<TagDetailViewModel>(relaxed = true) {
            every { this@mockk.uiState } returns uiState
            every { effect } returns effectChannel.receiveAsFlow()
            every { delete() } answers { effectChannel.trySend(TagDetailEffect.DeleteSucceeded).getOrThrow() }
            // 다시 시작이 저장되면 완료된 태그 목록이 다시 조회되는 것과 같게 목록 조회 결과에서 그 태그를 뺀다.
            every { restart() } answers {
                uiState.value = tagDetailUiState(id = id, detail = tagDetail(title = detailTitle(id = id)), isFinished = false)
                finishedTagList = finishedTagList.filterNot { tag -> tag.id == id }
                finishedPagingData.value = tagPagingDataOf(finishedTagList)
            }
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val TAG_COUNT = 3
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val DEFAULT_PLACEHOLDER_TEXT = "Choose a tag"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun listTitle(index: Int): String = "TagFinishedListDetailTitle$index"

        fun detailTitle(id: Uuid): String = "TagFinishedListDetailScreen$id"

        fun actionButton(contentDescription: String): SemanticsMatcher = hasContentDescription(contentDescription) and hasClickAction()

        fun tagList(): List<Tag> =
            List(TAG_COUNT) { index ->
                fixtureMonkey
                    .giveMeKotlinBuilder<Tag>()
                    .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = listTitle(index = index)))
                    .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                    .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                    .sample()
            }
    }
}

// 캘린더 홈처럼 태그가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object FinishedListTestTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "FinishedListTestTopLevel"
}
