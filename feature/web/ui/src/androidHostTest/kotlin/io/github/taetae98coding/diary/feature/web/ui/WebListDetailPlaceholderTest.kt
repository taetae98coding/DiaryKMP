@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.web.ui

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
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
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.feature.web.ui.add.INPUT_COUNT_WITHOUT_HEADER
import io.github.taetae98coding.diary.feature.web.ui.add.TYPED_FIRST_HEADER_NAME
import io.github.taetae98coding.diary.feature.web.ui.add.TYPED_FIRST_HEADER_VALUE
import io.github.taetae98coding.diary.feature.web.ui.add.TYPED_URL
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddTagViewModel
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddUiState
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddViewModel
import io.github.taetae98coding.diary.feature.web.ui.add.addHeaderRow
import io.github.taetae98coding.diary.feature.web.ui.add.descriptionInput
import io.github.taetae98coding.diary.feature.web.ui.add.headerNameInput
import io.github.taetae98coding.diary.feature.web.ui.add.headerValueInput
import io.github.taetae98coding.diary.feature.web.ui.add.inputCount
import io.github.taetae98coding.diary.feature.web.ui.add.titleInput
import io.github.taetae98coding.diary.feature.web.ui.add.urlInput
import io.github.taetae98coding.diary.feature.web.ui.add.webTestTag
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
import kotlin.uuid.Uuid

// 상세 영역의 웹 추가는 목록과 상세를 함께 표시할 때만 놓이므로, 두 영역을 함께 표시하는 너비의 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class WebListDetailPlaceholderTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var currentTag: Tag? = null
    private val addViewModelList = mutableListOf<WebAddViewModel>()
    private val tagViewModelList = mutableListOf<WebAddTagViewModel>()
    private val backStack = NavBackStack<ScreenNavKey>(MoreNavKey, WebHomeNavKey)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-WEB-LIST-DETAIL-DOMAIN-004 더보기로 돌아갔다가 다시 진입하면 상세 영역의 웹 추가는 입력과 태그 선택이 모두 비어 있는 상태로 시작한다`() {
        val input = placeholderInput()
        setWebNavDisplay(input = input)
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(WebHomeNavKey) }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.urlInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_HEADER
        composeRule.onAllNodesWithText(input.tag.detail.title).assertCountEquals(0)
        tagViewModelList shouldHaveSize 2
        addViewModelList shouldHaveSize 2
    }

    @Test
    fun `TC-WEB-LIST-DETAIL-DOMAIN-005 배치를 떠나기 전에는 목록에서 연 웹 상세에 다녀와도 웹 추가의 입력과 태그 선택이 남는다`() {
        assertPlaceholderRetained(route = WebDetailNavKey(id = Uuid.random()))
    }

    @Test
    fun `TC-WEB-LIST-DETAIL-DOMAIN-005 배치를 떠나기 전에는 웹 추가의 태그 입력에서 연 태그 추가에 다녀와도 웹 추가의 입력과 태그 선택이 남는다`() {
        assertPlaceholderRetained(route = TagAddNavKey(requestKey = Uuid.random()))
    }

    private fun assertPlaceholderRetained(route: ScreenNavKey) {
        val input = placeholderInput()
        setWebNavDisplay(input = input)
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle { backStack.add(route) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(input.title))
        composeRule.descriptionInput().assert(hasText(input.description))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.headerNameInput().assert(hasText(TYPED_FIRST_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(TYPED_FIRST_HEADER_VALUE))
        composeRule.onAllNodesWithText(input.tag.detail.title).onFirst().assertExists()
        tagViewModelList shouldHaveSize 1
        addViewModelList shouldHaveSize 1
    }

    private fun setWebNavDisplay(input: PlaceholderInput) {
        currentTag = input.tag
        composeRule.setContent {
            WebPlaceholderTestHost {
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
                                entry<MoreNavKey> { Text(text = MORE_CONTENT) }
                                entry<WebHomeNavKey>(
                                    clazzContentKey = { WEB_HOME_CONTENT_KEY },
                                    metadata = webHomeListPaneMetadata(backStack = backStack),
                                ) { Text(text = WEB_HOME_CONTENT) }
                                entry<WebDetailNavKey> { Text(text = ROUTE_CONTENT) }
                                entry<TagAddNavKey> { Text(text = ROUTE_CONTENT) }
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun WebPlaceholderTestHost(content: @Composable () -> Unit) {
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

    // 태그 선택은 ViewModel이 들고 있으므로 실제 ViewModel을 쓰고, 만들어진 인스턴스를 세어 저장 범위를 확인한다.
    private fun placeholderViewModelModule() =
        module {
            factory { relaxedAddViewModel().also { viewModel -> addViewModelList += viewModel } }
            factory { realTagViewModel().also { viewModel -> tagViewModelList += viewModel } }
        }

    private fun realTagViewModel(): WebAddTagViewModel {
        val tag = requireNotNull(currentTag)
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(emptyList())))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            flowOf(Result.success(listOf(tag).filter { value -> value.id in idSet }))
        }

        return WebAddTagViewModel(
            initialTagId = null,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    private fun ComposeContentTestRule.fillPlaceholder(input: PlaceholderInput) {
        titleInput().performTextInput(input.title)
        descriptionInput().performTextInput(input.description)
        urlInput().performTextInput(TYPED_URL)
        addHeaderRow()
        headerNameInput().performTextInput(TYPED_FIRST_HEADER_NAME)
        headerValueInput().performTextInput(TYPED_FIRST_HEADER_VALUE)
        runOnIdle { tagViewModelList.last().add(input.tag.id) }
        waitForIdle()
        onAllNodesWithText(input.tag.detail.title).onFirst().assertExists()
    }

    private data class PlaceholderInput(
        val title: String,
        val description: String,
        val tag: Tag,
    )

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val WEB_HOME_CONTENT = "WebHomeContent"
        const val ROUTE_CONTENT = "RouteContent"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        // 배치를 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
        fun relaxedAddViewModel(): WebAddViewModel =
            mockk<WebAddViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(WebAddUiState())
                every { effect } returns emptyFlow()
            }

        fun placeholderInput(): PlaceholderInput =
            PlaceholderInput(
                title = "title-${fixtureMonkey.giveMeOne<String>()}",
                description = "description-${fixtureMonkey.giveMeOne<String>()}",
                tag = webTestTag(title = "tag-${fixtureMonkey.giveMeOne<String>()}"),
            )
    }
}

// 웹 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object MoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
