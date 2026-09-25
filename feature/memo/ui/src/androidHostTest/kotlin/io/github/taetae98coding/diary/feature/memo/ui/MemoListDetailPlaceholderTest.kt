@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.MemoAddWebViewModel
import io.github.taetae98coding.diary.feature.memo.ui.add.screenTestRealViewModel
import io.github.taetae98coding.diary.feature.memo.ui.contact.testContact
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.feature.memo.ui.web.testWeb
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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

// 선택한 메모가 없을 때의 메모 추가는 목록과 상세를 함께 표시할 때만 상세 영역에 놓이므로, 두 영역을 함께 표시하는 너비의 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class MemoListDetailPlaceholderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var currentInput: PlaceholderInput? = null
    private val tagViewModelList = mutableListOf<MemoAddTagViewModel>()
    private val webViewModelList = mutableListOf<MemoAddWebViewModel>()
    private val contactViewModelList = mutableListOf<MemoAddContactViewModel>()
    private val placeViewModelList = mutableListOf<MemoAddPlaceViewModel>()
    private val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-MEMO-LIST-DETAIL-DOMAIN-002 다른 주요 목적지에 다녀오면 상세 영역의 메모 추가는 입력과 선택이 모두 비어 있는 상태로 시작한다`() {
        val input = placeholderInput()
        setMemoNavDisplay(input = input)
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(OtherTopLevelNavKey)
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(MemoHomeNavKey)
        }
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(""))
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().assertIsOff()
        input.selectedTitleList.forEach { title -> composeRule.onAllNodesWithText(title).assertCountEquals(0) }
        tagViewModelList shouldHaveSize 2
    }

    @Test
    fun `TC-MEMO-LIST-DETAIL-DOMAIN-003 배치를 떠나기 전에는 상세 영역에서 메모 상세를 보고 돌아와도 메모 추가의 입력과 선택이 남는다`() {
        assertPlaceholderRetained(route = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))
    }

    @Test
    fun `TC-MEMO-LIST-DETAIL-DOMAIN-003 배치를 떠나기 전에는 메모 추가에서 연 연결 항목 화면에 다녀와도 메모 추가의 입력과 선택이 남는다`() {
        assertPlaceholderRetained(route = TagAddNavKey(requestKey = fixtureMonkey.giveMeOne<Uuid>()))
    }

    @Test
    fun `TC-MEMO-LIST-DETAIL-FEATURE-021 메모 추가에 이어 연 메모 상세에서 뒤로가면 목록을 유지하고 메모 추가로 되돌아간다`() {
        val input = placeholderInput()
        val detail = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        setMemoNavDisplay(input = input)
        composeRule.runOnIdle {
            backStack.add(MemoAddNavKey())
            backStack.add(detail)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()

        pressBack()

        backStack.toList() shouldBe listOf(MemoHomeNavKey, MemoAddNavKey())
        composeRule.onNodeWithText(MEMO_HOME_CONTENT).assertExists()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertDoesNotExist()
        composeRule.onNodeWithText(MEMO_ADD_CONTENT).assertExists()
    }

    @Test
    fun `TC-MEMO-LIST-DETAIL-FEATURE-021 선택한 메모 없이 목록에서 연 메모 상세에서 뒤로가면 목록을 유지하고 선택 전 메모 추가로 되돌아간다`() {
        val input = placeholderInput()
        setMemoNavDisplay(input = input)
        composeRule.fillPlaceholder(input = input)
        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()

        pressBack()

        backStack.toList() shouldBe listOf(MemoHomeNavKey)
        composeRule.onNodeWithText(MEMO_HOME_CONTENT).assertExists()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(input.title))
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun assertPlaceholderRetained(route: ScreenNavKey) {
        val input = placeholderInput()
        setMemoNavDisplay(input = input)
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle { backStack.add(route) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(input.title))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(input.description))
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().assertIsOn()
        input.selectedTitleList.forEach { title -> composeRule.onAllNodesWithText(title).onFirst().assertExists() }
        tagViewModelList shouldHaveSize 1
    }

    private fun setMemoNavDisplay(input: PlaceholderInput) {
        currentInput = input
        composeRule.setContent {
            MemoPlaceholderTestHost {
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
                                entry<MemoHomeNavKey>(
                                    clazzContentKey = { MEMO_HOME_CONTENT_KEY },
                                    metadata = memoHomeListPaneMetadata(backStack = backStack),
                                ) { Text(text = MEMO_HOME_CONTENT) }
                                entry<MemoDetailNavKey>(
                                    metadata = ListDetailSceneStrategy.detailPane(sceneKey = MemoHomeNavKey),
                                ) { Text(text = ROUTE_CONTENT) }
                                entry<MemoAddNavKey>(
                                    metadata = ListDetailSceneStrategy.detailPane(sceneKey = MemoHomeNavKey),
                                ) { Text(text = MEMO_ADD_CONTENT) }
                                entry<TagAddNavKey> { Text(text = ROUTE_CONTENT) }
                                entry<OtherTopLevelNavKey> { Text(text = OTHER_TOP_LEVEL_CONTENT) }
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun MemoPlaceholderTestHost(content: @Composable () -> Unit) {
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
            factory { screenTestRealViewModel().viewModel }
            factory { realViewModels().tagViewModel.also { tagViewModelList += it } }
            factory { realViewModels().webViewModel.also { webViewModelList += it } }
            factory { realViewModels().contactViewModel.also { contactViewModelList += it } }
            factory { realViewModels().placeViewModel.also { placeViewModelList += it } }
            factory { relaxedPlaceMapViewModel() }
            factory { relaxedGeminiViewModel() }
        }

    private fun realViewModels() =
        requireNotNull(currentInput).let { input ->
            screenTestRealViewModel(
                tagList = listOf(input.tag),
                webList = listOf(input.web),
                contactList = listOf(input.contact),
                placeList = listOf(input.place),
            )
        }

    private fun ComposeContentTestRule.fillPlaceholder(input: PlaceholderInput) {
        onAllNodes(hasSetTextAction()).onFirst().performTextInput(input.title)
        onAllNodes(hasSetTextAction())[1].performTextInput(input.description)
        onNode(hasRole(Role.Switch)).performScrollTo().performClick()
        runOnIdle {
            tagViewModelList.last().selectTag(input.tag.id)
            webViewModelList.last().selectWeb(input.web.id)
            contactViewModelList.last().selectContact(input.contact.id)
            placeViewModelList.last().selectPlace(input.place.id)
        }
        waitForIdle()
        input.selectedTitleList.forEach { title -> onAllNodesWithText(title).onFirst().assertExists() }
    }

    private data class PlaceholderInput(
        val title: String,
        val description: String,
        val tag: Tag,
        val web: Web,
        val contact: Contact,
        val place: Place,
    ) {
        val selectedTitleList: List<String>
            get() = listOf(tag.detail.title, web.detail.title, contact.detail.name, place.detail.title)
    }

    private companion object {
        const val MEMO_HOME_CONTENT = "MemoHomeContent"
        const val ROUTE_CONTENT = "RouteContent"
        const val MEMO_ADD_CONTENT = "MemoAddContent"
        const val OTHER_TOP_LEVEL_CONTENT = "OtherTopLevelContent"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        // 배치를 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 mock을 쓴다.
        fun relaxedPlaceMapViewModel(): MemoPlaceMapViewModel = mockk<MemoPlaceMapViewModel>(relaxed = true) { every { uiState } returns MutableStateFlow(MemoPlaceMapUiState.Loading) }

        fun relaxedGeminiViewModel(): MemoGeminiViewModel =
            mockk<MemoGeminiViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(MemoGeminiUiState())
                every { effect } returns emptyFlow()
            }

        fun placeholderInput(): PlaceholderInput =
            PlaceholderInput(
                title = "title-${fixtureMonkey.giveMeOne<String>()}",
                description = "description-${fixtureMonkey.giveMeOne<String>()}",
                tag = testTag(title = "tag-${fixtureMonkey.giveMeOne<String>()}"),
                web = testWeb(title = "web-${fixtureMonkey.giveMeOne<String>()}"),
                contact = testContact(name = "contact-${fixtureMonkey.giveMeOne<String>()}"),
                place = testPlace(title = "place-${fixtureMonkey.giveMeOne<String>()}"),
            )

        fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }
}

// 캘린더 홈처럼 메모가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "OtherTopLevel"
}
