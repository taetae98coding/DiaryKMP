@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
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
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.usecase.AddTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.ui.add.TagAddLinkViewModel
import io.github.taetae98coding.diary.feature.tag.ui.add.TagAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.tag.ui.add.TagAddScreen
import io.github.taetae98coding.diary.feature.tag.ui.add.TagAddViewModel
import io.github.taetae98coding.diary.feature.tag.ui.add.emojiInput
import io.github.taetae98coding.diary.feature.tag.ui.add.inputEmoji
import io.github.taetae98coding.diary.feature.tag.ui.link.testTag
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
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

// 목록에서 연 태그 추가와 선택 전 태그 추가는 화면 항목마다 따로 입력 상태를 가져야 하므로, 앱처럼 항목별 ViewModel 저장소를 쓰는 NavDisplay 위에서 제품 TagAddScreen으로 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class TagListDetailAddOpenedFromListTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(TagHomeNavKey)
    private val linkViewModelList = mutableListOf<TagAddLinkViewModel>()
    private val linkedTag: Tag = testTag(title = Arb.alphanumericString(prefix = "tag").next())

    @Before
    fun setUp() {
        stopKoin()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-739579c6-b63e-4829-9d2e-865c42cf1009 태그 상세 위에서 연 태그 추가는 빈 입력으로 시작한다`() {
        setNavDisplay()
        val input = fillPlaceholder()

        openAddOnDetail()

        backStack.last().shouldBeInstanceOf<TagAddNavKey>()
        composeRule.assertAddInputEmpty(input)
        composeRule.onNodeWithText(TAG_HOME_CONTENT).assertExists()
    }

    @Test
    fun `TC-1614376b-41a5-4fc7-becb-9399cf118304 목록에서 연 태그 추가를 뒤로가기로 닫아도 선택 전 태그 추가의 작성 내용이 남는다`() {
        setNavDisplay()
        val input = fillPlaceholder()
        openAddOnDetail()

        pressBack()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertExists()
        pressBack()

        backStack.toList() shouldContainExactly listOf(TagHomeNavKey)
        composeRule.assertPlaceholderRetained(input)
    }

    @Test
    fun `TC-1614376b-41a5-4fc7-becb-9399cf118304 목록에서 연 태그 추가를 다른 태그를 골라 걷어내도 선택 전 태그 추가의 작성 내용이 남는다`() {
        setNavDisplay()
        val input = fillPlaceholder()
        openAddOnDetail()

        composeRule.runOnIdle { backStack.navigateToTagDetail(Arb.kotlinUuid().next()) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertExists()
        pressBack()

        backStack.toList() shouldContainExactly listOf(TagHomeNavKey)
        composeRule.assertPlaceholderRetained(input)
    }

    private fun openAddOnDetail() {
        composeRule.runOnIdle { backStack.navigateToTagDetail(Arb.kotlinUuid().next()) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.navigateToTagAdd() }
        composeRule.waitForIdle()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun fillPlaceholder(): AddInput {
        val input = AddInput(emoji = EMOJI, title = Arb.alphanumericString(prefix = "title").next(), description = Arb.alphanumericString(prefix = "description").next())
        composeRule.inputEmoji(input.emoji)
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(input.title)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(input.description)
        composeRule.runOnIdle { linkViewModelList.last().link(linkedTag.id) }
        composeRule.waitForIdle()
        composeRule.assertPlaceholderRetained(input)
        return input
    }

    private fun ComposeContentTestRule.assertPlaceholderRetained(input: AddInput) {
        emojiInput().assert(hasText(input.emoji))
        onAllNodes(hasSetTextAction()).onFirst().assert(hasText(input.title))
        onAllNodes(hasSetTextAction())[1].assert(hasText(input.description))
        onAllNodesWithText(linkedTag.detail.title).onFirst().assertExists()
    }

    private fun ComposeContentTestRule.assertAddInputEmpty(input: AddInput) {
        onAllNodes(hasSetTextAction()).onFirst().assert(hasText(""))
        onAllNodes(hasSetTextAction())[1].assert(hasText(""))
        onAllNodesWithText(input.emoji).assertCountEquals(0)
        onAllNodesWithText(linkedTag.detail.title).assertCountEquals(0)
    }

    private fun setNavDisplay() {
        composeRule.setContent {
            val viewModelStoreOwner =
                remember {
                    object : ViewModelStoreOwner {
                        override val viewModelStore: ViewModelStore = ViewModelStore()
                    }
                }

            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner,
                LocalResultEventBus provides remember { ResultEventBus() },
            ) {
                KoinApplication(configuration = koinConfiguration { modules(viewModelModule()) }) {
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
                                        entry<TagHomeNavKey>(
                                            clazzContentKey = { TAG_HOME_CONTENT_KEY },
                                            metadata = tagHomeListPaneMetadata(backStack = backStack),
                                        ) { Text(text = TAG_HOME_CONTENT) }
                                        entry<TagDetailNavKey>(metadata = detailPaneMetadata()) {
                                            Text(text = DETAIL_CONTENT)
                                        }
                                        entry<TagAddNavKey>(metadata = detailPaneMetadata()) { key ->
                                            val tagAddRequestKey = rememberResultRequestKey()

                                            TagAddScreen(
                                                navigateUp = { backStack.removeLastOrNull() },
                                                navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
                                                navigateToDetail = { id -> backStack.add(TagDetailNavKey(id)) },
                                                addedResultRequestKey = key.requestKey,
                                                tagAddRequestKey = tagAddRequestKey,
                                                componentVisibleProvider = { TagAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                                                addViewModel = koinViewModel(),
                                                linkViewModel = koinViewModel(),
                                            )
                                        }
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun detailPaneMetadata(): Map<String, Any> = ListDetailSceneStrategy.detailPane(sceneKey = TagHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

    private fun viewModelModule() =
        module {
            factory { TagAddViewModel(addTagUseCase = mockk<AddTagUseCase>()) }
            factory { linkViewModel().also { linkViewModelList += it } }
        }

    private fun linkViewModel(): TagAddLinkViewModel {
        val tagList = listOf(linkedTag)
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns MutableStateFlow(Result.success(PagingData.from(tagList)))

        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<*>>()
            MutableStateFlow(Result.success(tagList.filter { tag -> tag.id in idSet }))
        }

        return TagAddLinkViewModel(pageTagUseCase = pageTagUseCase, getSelectedTagUseCase = getSelectedTagUseCase)
    }

    private data class AddInput(
        val emoji: String,
        val title: String,
        val description: String,
    )

    private companion object {
        const val TAG_HOME_CONTENT = "TagHomeContent"
        const val EMOJI = "🏃"
        const val DETAIL_CONTENT = "TagDetailContent"
    }
}
