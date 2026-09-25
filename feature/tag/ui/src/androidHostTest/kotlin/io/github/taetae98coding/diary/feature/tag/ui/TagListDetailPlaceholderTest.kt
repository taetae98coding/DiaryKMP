@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.ui.add.TagAddLinkViewModel
import io.github.taetae98coding.diary.feature.tag.ui.add.TagAddViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.prepareTagDetailTabViewModels
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailTabViewModelModule
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.link.testTag
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 선택한 태그가 없을 때의 태그 추가는 목록과 상세를 함께 표시할 때만 상세 영역에 놓이므로, 두 영역을 함께 표시하는 너비의 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class TagListDetailPlaceholderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var currentInput: PlaceholderInput? = null
    private val linkViewModelList = mutableListOf<TagAddLinkViewModel>()
    private val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    // 같은 실행에서 앞서 돈 다른 테스트 클래스가 전역 Koin을 남겨 둘 수 있으므로 시작 전에도 정리한다.
    @Before
    fun setUp() {
        stopKoin()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-TAG-LIST-DETAIL-FEATURE-001 TC-TAG-LIST-DETAIL-FEATURE-002 선택한 태그가 없으면 목록과 함께 상세 영역에 태그 추가 화면이 표시된다`() {
        setTagNavDisplay(input = placeholderInput())

        composeRule.onNodeWithText(TAG_HOME_CONTENT).assertExists()
        composeRule.onNodeWithText(DEFAULT_TAG_ADD_TITLE).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(""))
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `TC-TAG-LIST-DETAIL-FEATURE-004 한 화면만 제공하는 환경에서는 현재 화면만 단독으로 표시한다`() {
        setTagNavDisplay(input = placeholderInput())

        composeRule.onNodeWithText(TAG_HOME_CONTENT).assertExists()
        composeRule.onNodeWithText(DEFAULT_TAG_ADD_TITLE).assertDoesNotExist()

        composeRule.runOnIdle { backStack.add(TagAddNavKey()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()
        composeRule.onNodeWithText(TAG_HOME_CONTENT).assertDoesNotExist()

        composeRule.runOnIdle {
            backStack.removeLastOrNull()
            backStack.add(TagDetailNavKey(id = fixtureId()))
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()
        composeRule.onNodeWithText(TAG_HOME_CONTENT).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-LIST-DETAIL-FEATURE-018 선택한 태그가 없을 때 태그 추가에서 연결한 태그를 누르면 그 태그를 선택한 것과 같다`() {
        val input = placeholderInput()
        setTagNavDisplay(input = input, isTagDetailScreen = true)
        composeRule.fillPlaceholder(input = input)
        val detailKey = TagDetailNavKey(id = input.linkedTag.id)

        composeRule.onAllNodesWithText(input.linkedTag.detail.title).onFirst().performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldContainExactly listOf(TagHomeNavKey, detailKey)
        composeRule.onNodeWithText(TAG_HOME_CONTENT).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-LIST-DETAIL-DOMAIN-002 다른 주요 목적지에 다녀오면 상세 영역의 태그 추가는 입력과 연결이 모두 비어 있는 상태로 시작한다`() {
        val input = placeholderInput()
        setTagNavDisplay(input = input)
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(OtherTopLevelNavKey)
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(TagHomeNavKey)
        }
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(""))
        composeRule.onAllNodesWithText(input.linkedTag.detail.title).assertCountEquals(0)
        linkViewModelList shouldHaveSize 2
    }

    @Test
    fun `TC-TAG-LIST-DETAIL-DOMAIN-003 배치를 떠나기 전에는 상세 영역에서 태그 상세를 보고 돌아와도 태그 추가의 입력과 연결이 남는다`() {
        assertPlaceholderRetained(route = TagDetailNavKey(id = fixtureId()))
    }

    @Test
    fun `TC-TAG-LIST-DETAIL-DOMAIN-003 배치를 떠나기 전에는 연결 입력으로 연 태그 추가에 다녀와도 태그 추가의 입력과 연결이 남는다`() {
        assertPlaceholderRetained(route = TagAddNavKey(requestKey = fixtureId()))
    }

    @Test
    fun `TC-TAG-LIST-DETAIL-FEATURE-017 목록에서 고른 태그의 상세에서 시스템 뒤로가기를 사용하면 태그 상세를 고르기 전 상태로 돌아간다`() {
        val pathCases =
            listOf(
                listOf(TagDetailNavKey(id = fixtureId())),
                listOf(TagDetailNavKey(id = fixtureId()), TagDetailNavKey(id = fixtureId())),
                listOf(TagDetailNavKey(id = fixtureId()), TagAddNavKey(requestKey = fixtureId()), TagDetailNavKey(id = fixtureId())),
                listOf(TagAddNavKey(), TagDetailNavKey(id = fixtureId()), TagDetailNavKey(id = fixtureId())),
            )
        setTagNavDisplay(input = placeholderInput())

        pathCases.forEach { pathKeyList ->
            val selectedId = fixtureId()
            composeRule.runOnIdle {
                backStack.addAll(pathKeyList)
                backStack.navigateToTagDetail(selectedId)
            }
            composeRule.waitForIdle()
            composeRule.onNodeWithText(detailContent(id = selectedId)).assertExists()

            pressBack()

            backStack.toList() shouldContainExactly listOf(TagHomeNavKey)
            composeRule.onNodeWithText(TAG_HOME_CONTENT).assertExists()
            composeRule.onNodeWithText(ROUTE_CONTENT).assertDoesNotExist()
            composeRule.onNodeWithText(DEFAULT_TAG_ADD_TITLE).assertExists()
        }
    }

    @Test
    fun `TC-TAG-LIST-DETAIL-FEATURE-019 태그 상세 위에 연 태그 추가에서 시스템 뒤로가기를 사용하면 그 태그 상세로 돌아온다`() {
        val detailKey = TagDetailNavKey(id = fixtureId())
        setTagNavDisplay(input = placeholderInput())
        composeRule.runOnIdle { backStack.navigateToTagDetail(detailKey.id) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.navigateToTagAdd() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(detailContent(id = detailKey.id)).assertDoesNotExist()

        pressBack()

        backStack.toList() shouldContainExactly listOf(TagHomeNavKey, detailKey)
        composeRule.onNodeWithText(TAG_HOME_CONTENT).assertExists()
        composeRule.onNodeWithText(detailContent(id = detailKey.id)).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-054 연결한 태그를 눌러 연 태그 상세에서 시스템 뒤로가기를 사용하면 목록을 유지하고 이전 태그의 상세로 돌아간다`() {
        val fromKey = TagDetailNavKey(id = fixtureId())
        val toKey = TagDetailNavKey(id = fixtureId())
        setTagNavDisplay(input = placeholderInput())
        composeRule.runOnIdle {
            backStack.add(fromKey)
            backStack.add(toKey)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(detailContent(id = toKey.id)).assertExists()

        pressBack()

        backStack.toList() shouldContainExactly listOf(TagHomeNavKey, fromKey)
        composeRule.onNodeWithText(TAG_HOME_CONTENT).assertExists()
        composeRule.onNodeWithText(detailContent(id = fromKey.id)).assertExists()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun assertPlaceholderRetained(route: ScreenNavKey) {
        val input = placeholderInput()
        setTagNavDisplay(input = input)
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle { backStack.add(route) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(input.title))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(input.description))
        composeRule.onAllNodesWithText(input.linkedTag.detail.title).onFirst().assertExists()
        linkViewModelList shouldHaveSize 1
    }

    private fun setTagNavDisplay(
        input: PlaceholderInput,
        isTagDetailScreen: Boolean = false,
    ) {
        currentInput = input
        prepareTagDetailTabViewModels()
        composeRule.setContent {
            TagPlaceholderTestHost {
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
                                entry<TagHomeNavKey>(
                                    clazzContentKey = { TAG_HOME_CONTENT_KEY },
                                    metadata = tagHomeListPaneMetadata(backStack = backStack),
                                ) { Text(text = TAG_HOME_CONTENT) }
                                entry<TagDetailNavKey>(
                                    metadata = ListDetailSceneStrategy.detailPane(sceneKey = TagHomeNavKey),
                                ) { key ->
                                    if (isTagDetailScreen) {
                                        RealTagDetailScreen(key = key)
                                    } else {
                                        Column {
                                            Text(text = ROUTE_CONTENT)
                                            Text(text = detailContent(id = key.id))
                                        }
                                    }
                                }
                                entry<TagAddNavKey>(
                                    metadata = ListDetailSceneStrategy.detailPane(sceneKey = TagHomeNavKey),
                                ) { Text(text = ROUTE_CONTENT) }
                                entry<OtherTopLevelNavKey> { Text(text = OTHER_TOP_LEVEL_CONTENT) }
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    // 제품의 TagDetail 화면과 그 화면이 쓰는 뒤로가기 표시 판정을 그대로 두어 상단 바에 보이는 결과를 확인한다.
    @Composable
    private fun RealTagDetailScreen(key: TagDetailNavKey) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        TagDetailScreen(
            navigateToTagAdd = {},
            tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
            navigateUp = { backStack.removeLastOrNull() },
            navigateToDetail = {},
            navigateToMemoAdd = {},
            navigateToMemoDetail = {},
            navigateToMemoFinishedList = {},
            id = key.id,
            componentVisibleProvider = { backStack.tagDetailScaffoldComponentVisible(key = key, isListPaneVisible = isListPaneVisible) },
            detailViewModel = remember(key) { screenTestViewModel(MutableStateFlow(tagDetailUiState(id = key.id, detail = tagDetail(TAG_TITLE)))) },
            placeMapViewModel = koinViewModel(),
            navigateToWebAdd = {},
            navigateToWebDetail = {},
            navigateToPlaceAdd = {},
            navigateToPlaceDetail = {},
        )
    }

    @Composable
    private fun TagPlaceholderTestHost(content: @Composable () -> Unit) {
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
            KoinApplication(configuration = koinConfiguration { modules(placeholderViewModelModule(), tagDetailTabViewModelModule) }) {
                DiaryTheme(content = content)
            }
        }
    }

    private fun placeholderViewModelModule() =
        module {
            factory { TagAddViewModel(addTagUseCase = mockk()) }
            factory { linkViewModel().also { linkViewModelList += it } }
        }

    private fun linkViewModel(): TagAddLinkViewModel {
        val tagList = listOf(requireNotNull(currentInput).linkedTag)
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns MutableStateFlow(Result.success(PagingData.from(tagList)))

        // 연결한 식별자로 저장소를 조회하는 동작을 준비된 목록에서 골라내는 방식으로 대신한다.
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<*>>()
            MutableStateFlow(Result.success(tagList.filter { tag -> tag.id in idSet }))
        }

        return TagAddLinkViewModel(pageTagUseCase = pageTagUseCase, getSelectedTagUseCase = getSelectedTagUseCase)
    }

    private fun ComposeContentTestRule.fillPlaceholder(input: PlaceholderInput) {
        onAllNodes(hasSetTextAction()).onFirst().performTextInput(input.title)
        onAllNodes(hasSetTextAction())[1].performTextInput(input.description)
        runOnIdle { linkViewModelList.last().link(input.linkedTag.id) }
        waitForIdle()
        onAllNodesWithText(input.linkedTag.detail.title).onFirst().assertExists()
    }

    private data class PlaceholderInput(
        val title: String,
        val description: String,
        val linkedTag: Tag,
    )

    private companion object {
        const val TAG_HOME_CONTENT = "TagHomeContent"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val ROUTE_CONTENT = "RouteContent"
        const val OTHER_TOP_LEVEL_CONTENT = "OtherTopLevelContent"
        const val DEFAULT_TAG_ADD_TITLE = "Add Tag"

        fun detailContent(id: Uuid): String = "TagDetail-$id"

        fun placeholderInput(): PlaceholderInput =
            PlaceholderInput(
                title = fixtureText(prefix = "title"),
                description = fixtureText(prefix = "description"),
                linkedTag = testTag(title = fixtureText(prefix = "tag")),
            )
    }
}

// 캘린더 홈처럼 태그가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "OtherTopLevel"
}
