@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
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
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
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
import io.github.taetae98coding.diary.feature.tag.ui.link.testTag
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 선택한 태그가 없을 때의 태그 추가는 목록과 상세를 함께 표시할 때만 상세 영역에 놓이므로, 두 영역을 함께 표시하는 너비의 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class TagListDetailPlaceholderTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var currentInput: PlaceholderInput? = null
    private val linkViewModelList = mutableListOf<TagAddLinkViewModel>()
    private val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
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

    private fun setTagNavDisplay(input: PlaceholderInput) {
        currentInput = input
        composeRule.setContent {
            TagPlaceholderTestHost {
                val holder = rememberListDetailPlaceholderStateHolder()

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
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
                                ) { Text(text = ROUTE_CONTENT) }
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
            KoinApplication(configuration = koinConfiguration { modules(placeholderViewModelModule()) }) {
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
        const val ROUTE_CONTENT = "RouteContent"
        const val OTHER_TOP_LEVEL_CONTENT = "OtherTopLevelContent"

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
