package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.checklist.ui.add.ChecklistAddUiState
import io.github.taetae98coding.diary.feature.checklist.ui.add.ChecklistAddViewModel
import io.github.taetae98coding.diary.feature.checklist.ui.card.CHECKLIST_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailEffect
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailItemUiState
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailItemViewModel
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailUiState
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailViewModel
import io.github.taetae98coding.diary.feature.checklist.ui.home.ChecklistHomeViewModel
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ChecklistNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(ChecklistNavDisplayMoreNavKey, ChecklistHomeNavKey)
    private val addViewModelList = mutableListOf<ChecklistAddViewModel>()
    private val detailViewModelList = mutableListOf<ChecklistDetailViewModel>()
    private val itemViewModelList = mutableListOf<ChecklistDetailItemViewModel>()
    private val syncViewModelList = mutableListOf<SyncRefreshViewModel>()
    private var detailEffect: ChecklistDetailEffect? = null
    private var homeSummaryList: List<ChecklistSummary> = emptyList()

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
    fun `TC-CHECKLIST-HOME-FEATURE-009 목록의 체크리스트를 선택하면 그 체크리스트의 ChecklistDetail 화면으로 이동한다`() {
        val summaryList = listOf(testChecklistSummary(), testChecklistSummary())
        setNavDisplay(summaryList = summaryList)
        waitUntilDisplayed(summaryList.last())

        composeRule.onNode(card(summaryList.last())).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistNavDisplayMoreNavKey, ChecklistHomeNavKey, ChecklistDetailNavKey(id = summaryList.last().checklist.id))
        titleInput().editableText() shouldBe
            summaryList
                .last()
                .checklist.detail.title
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-011 체크리스트 추가를 선택하면 ChecklistAdd 화면으로 이동한다`() {
        setNavDisplay()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistNavDisplayMoreNavKey, ChecklistHomeNavKey, ChecklistAddNavKey)
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-003 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        setNavDisplay()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistNavDisplayMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertIsDisplayed()
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-014 뒤로가기를 선택하면 ChecklistHome 화면으로 돌아가고 아무것도 저장하지 않는다`() {
        setNavDisplay()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        titleInput().performTextInput(TITLE)
        itemInput().performTextInput(ITEM)
        itemInput().performImeAction()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistNavDisplayMoreNavKey, ChecklistHomeNavKey)
        addViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.add(detail = any(), itemContentList = any()) } }
    }

    @Test
    fun `TC-CHECKLIST-HOME-DOMAIN-010 체크리스트 추가로 이동한 뒤 뒤로가도 보던 목록 위치를 유지한다`() {
        val summaryList = positionSummaryList()
        setNavDisplay(summaryList = summaryList)
        scrollToPosition(summaryList)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.refreshableList().assertDoesNotExist()
        pressBack()

        assertPositionDisplayed(summaryList)
    }

    @Test
    fun `TC-CHECKLIST-HOME-DOMAIN-010 체크리스트 상세로 이동한 뒤 뒤로가도 보던 목록 위치를 유지한다`() {
        val summaryList = positionSummaryList()
        setNavDisplay(summaryList = summaryList)
        scrollToPosition(summaryList)

        composeRule.onNode(card(summaryList[POSITION_SCROLL_INDEX])).performClick()
        composeRule.waitForIdle()
        composeRule.refreshableList().assertDoesNotExist()
        pressBack()

        assertPositionDisplayed(summaryList)
    }

    @Test
    fun `TC-CHECKLIST-HOME-DOMAIN-011 더보기로 나갔다 체크리스트 바로가기로 다시 진입하면 목록의 맨 위부터 보여 준다`() {
        val summaryList = positionSummaryList()
        setNavDisplay(summaryList = summaryList)
        scrollToPosition(summaryList)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.add(ChecklistHomeNavKey) }
        composeRule.waitForIdle()

        composeRule.onNode(card(summaryList.first())).assertIsDisplayed()
        composeRule.onNode(card(summaryList[POSITION_SCROLL_INDEX])).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-HOME-DOMAIN-011 앱을 다시 실행해 진입하면 목록의 맨 위부터 보여 준다`() {
        val summaryList = positionSummaryList()

        setNavDisplay(summaryList = summaryList)
        waitUntilDisplayed(summaryList.first())

        composeRule.onNode(card(summaryList.first())).assertIsDisplayed()
        composeRule.onNode(card(summaryList[POSITION_SCROLL_INDEX])).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-030 TC-CHECKLIST-DETAIL-FEATURE-033 상세에서 삭제하면 ChecklistHome으로 돌아가고 삭제 안내와 실행 취소를 표시하지 않는다`() {
        val summary = testChecklistSummary()
        detailEffect = ChecklistDetailEffect.DeleteSucceeded
        setNavDisplay(summaryList = listOf(summary))
        waitUntilDisplayed(summary)
        composeRule.onNode(card(summary)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistNavDisplayMoreNavKey, ChecklistHomeNavKey)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-037 상세에서 뒤로가면 반영하지 않은 내용을 저장하지 않고 ChecklistHome 화면으로 돌아간다`() {
        val summary = testChecklistSummary()
        setNavDisplay(summaryList = listOf(summary))
        waitUntilDisplayed(summary)
        composeRule.onNode(card(summary)).performClick()
        composeRule.waitForIdle()
        titleInput().performTextInput("2")
        itemInput().performTextInput(ITEM)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistNavDisplayMoreNavKey, ChecklistHomeNavKey)
        detailViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.update(detail = any()) } }
        itemViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.add(content = any()) } }
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-FEATURE-038 화면을 떠났다가 다시 들어오면 저장된 내용과 처음 정렬로 다시 시작한다`() {
        val summary = testChecklistSummary()
        setNavDisplay(summaryList = listOf(summary))
        waitUntilDisplayed(summary)
        composeRule.onNode(card(summary)).performClick()
        composeRule.waitForIdle()
        titleInput().performTextInput("2")
        itemInput().performTextInput(ITEM)
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(isSelectable() and hasText(DEFAULT_UNCHECKED_FIRST)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNCHECKED_FIRST, useUnmergedTree = true).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(card(summary)).performClick()
        composeRule.waitForIdle()

        titleInput().editableText() shouldBe summary.checklist.detail.title
        itemInput().editableText() shouldBe ""
        composeRule.onNodeWithText(DEFAULT_ADDED_ORDER, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNCHECKED_FIRST, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-DETAIL-DOMAIN-016 앱을 다시 실행해 진입하면 저장된 내용과 처음 정렬로 시작한다`() {
        val summary = testChecklistSummary()
        backStack.add(ChecklistDetailNavKey(id = summary.checklist.id))

        setNavDisplay(summaryList = listOf(summary))

        titleInput().editableText() shouldBe summary.checklist.detail.title
        composeRule.onAllNodes(hasSetTextAction())[1].editableText() shouldBe summary.checklist.detail.description
        itemInput().editableText() shouldBe ""
        composeRule.onNodeWithText(DEFAULT_ADDED_ORDER, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-HOME-DATA-003 TC-CHECKLIST-DETAIL-DATA-007 ChecklistHome과 ChecklistDetail 진입만으로는 서버와 맞추지 않는다`() {
        val summary = testChecklistSummary()
        setNavDisplay(summaryList = listOf(summary))
        waitUntilDisplayed(summary)

        composeRule.onNode(card(summary)).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistNavDisplayMoreNavKey, ChecklistHomeNavKey, ChecklistDetailNavKey(id = summary.checklist.id))
        syncViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.refresh() } }
    }

    private fun titleInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[0]

    private fun itemInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[2]

    private fun scrollToPosition(summaryList: List<ChecklistSummary>) {
        waitUntilDisplayed(summaryList.first())
        composeRule.refreshableList().performScrollToIndex(POSITION_SCROLL_INDEX)
        composeRule.waitForIdle()
        assertPositionDisplayed(summaryList)
    }

    private fun assertPositionDisplayed(summaryList: List<ChecklistSummary>) {
        composeRule.onNode(card(summaryList[POSITION_SCROLL_INDEX])).assertIsDisplayed()
        composeRule.onNode(card(summaryList.first())).assertDoesNotExist()
    }

    private fun waitUntilDisplayed(summary: ChecklistSummary) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(summary.checklist.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setNavDisplay(summaryList: List<ChecklistSummary> = emptyList()) {
        homeSummaryList = summaryList
        composeRule.setContent {
            NavDisplayTestHost {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<ChecklistNavDisplayMoreNavKey> { Text(text = MORE_CONTENT) }
                            checklistEntry(backStack = backStack)
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun NavDisplayTestHost(content: @Composable () -> Unit) {
        // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(navDisplayViewModelModule()) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory {
                mockk<ChecklistHomeViewModel>(relaxed = true).apply {
                    every { checklistPagingData } returns MutableStateFlow(checklistPagingDataOf(homeSummaryList))
                    every { effect } returns emptyFlow()
                }
            }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns MutableStateFlow(SyncRefreshUiState())
                    }.also(syncViewModelList::add)
            }
            factory { parameters ->
                val id = parameters.get<Uuid>()
                val summary = homeSummaryList.first { summary -> summary.checklist.id == id }
                mockk<ChecklistDetailViewModel>(relaxed = true)
                    .apply {
                        val effectChannel = Channel<ChecklistDetailEffect>(capacity = Channel.BUFFERED)
                        every { uiState } returns MutableStateFlow(ChecklistDetailUiState.Content(id = id, detail = summary.checklist.detail))
                        every { effect } returns effectChannel.receiveAsFlow()
                        every { delete() } answers { detailEffect?.let(effectChannel::trySend) }
                    }.also(detailViewModelList::add)
            }
            factory {
                val itemUiState =
                    MutableStateFlow(
                        ChecklistDetailItemUiState(
                            itemList = listOf(item(content = ITEM)),
                            isLoaded = true,
                            sort = ChecklistItemSort.ADDED,
                        ),
                    )
                mockk<ChecklistDetailItemViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns itemUiState
                        every { effect } returns emptyFlow()
                        every { selectSort(sort = any()) } answers { itemUiState.update { state -> state.copy(sort = firstArg()) } }
                    }.also(itemViewModelList::add)
            }
            factory {
                mockk<ChecklistAddViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns MutableStateFlow(ChecklistAddUiState())
                        every { effect } returns emptyFlow()
                    }.also(addViewModelList::add)
            }
        }

    private companion object {
        const val DEFAULT_ADD_DESCRIPTION = "Add checklist"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_DELETE_DESCRIPTION = "Delete checklist"
        const val DEFAULT_DELETED_MESSAGE = "Checklist deleted."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val DEFAULT_ADDED_ORDER = "Added order"
        const val DEFAULT_UNCHECKED_FIRST = "Unchecked first"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val MORE_CONTENT = "MoreContent"
        const val TITLE = "여행 준비"
        const val ITEM = "여권"
        const val POSITION_COUNT = 30
        const val POSITION_SCROLL_INDEX = 25
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L

        fun card(summary: ChecklistSummary): SemanticsMatcher = hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(summary.checklist.detail.title)

        fun positionSummaryList(): List<ChecklistSummary> =
            List(POSITION_COUNT) { index ->
                testChecklistSummary(titlePrefix = "${index.toString().padStart(length = 2, padChar = '0')}-")
            }

        fun item(content: String): ChecklistItem = checklistTestFixtureMonkey.checklistItem(isChecked = false, isDeleted = false, content = content)
    }
}

// 체크리스트 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object ChecklistNavDisplayMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
