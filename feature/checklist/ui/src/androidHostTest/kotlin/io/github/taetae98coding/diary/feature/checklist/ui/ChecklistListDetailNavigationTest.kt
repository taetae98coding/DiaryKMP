@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.checklist.checklistItem
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.checklist.api.checklistNavKeys
import io.github.taetae98coding.diary.feature.checklist.ui.add.ChecklistAddEffect
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
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 제품 entry의 배치 정보와 entry별 화면 상태를 그대로 쓰도록, 두 영역을 함께 표시하는 너비에서 제품 checklistEntry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class ChecklistListDetailNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
    private val homeViewModelList = mutableListOf<ChecklistHomeViewModel>()
    private var homeSummaryList: List<ChecklistSummary> = emptyList()

    private var directiveOverride by mutableStateOf<PaneScaffoldDirective?>(null)
    private val homePagingData = MutableStateFlow(checklistPagingDataOf(emptyList()))

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
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-001 목록과 상세를 함께 쓰는 환경에서는 체크리스트 목록과 체크리스트 추가를 함께 표시한다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)

        setChecklistNavDisplay(summaryList = listOf(summary))

        composeRule.onNode(card(summary)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-002 한 화면만 제공하는 환경에서는 현재 화면만 단독으로 표시한다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))

        composeRule.onNode(card(summary)).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(card(summary)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-005 목록과 함께 표시되면 상세 영역의 화면에서 뒤로가기 수단을 제공하지 않는다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))

        assertAddOnDetailPane()
        composeRule.onAllNodesWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)

        selectChecklist(summary)

        assertDetailOnDetailPane()
        composeRule.onAllNodesWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-006 단독으로 표시되면 그 화면에서 뒤로가기 수단을 제공한다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)

        selectChecklist(summary)
        assertDetailOnDetailPane()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-007 목록에서 체크리스트를 선택하면 목록을 유지한 채 상세 영역이 그 체크리스트의 상세로 바뀐다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))

        selectChecklist(summary)

        backStack.last() shouldBe ChecklistDetailNavKey(id = summary.checklist.id)
        composeRule.onNode(card(summary)).assertExists()
        assertDetailOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-008 상세 영역에서 뒤로가면 체크리스트 추가로 되돌아간다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        selectChecklist(summary)

        pressBack()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
        composeRule.onNode(card(summary)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-009 상세에서 체크리스트를 삭제하면 상세 영역이 체크리스트 추가로 되돌아간다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        selectChecklist(summary)

        composeRule.onNode(detailDeleteButton()).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
        composeRule.onNode(card(summary)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-010 체크리스트 상세가 놓인 동안 체크리스트 추가를 실행하면 상세 영역이 체크리스트 추가로 바뀐다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))

        openAddOnDetail(summary)

        backStack.last() shouldBe ChecklistAddNavKey
        composeRule.onNode(card(summary)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-011 체크리스트 상세 위에서 연 체크리스트 추가에서 뒤로가면 이전 체크리스트 상세로 돌아간다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        openAddOnDetail(summary)

        pressBack()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey, ChecklistDetailNavKey(id = summary.checklist.id))
        composeRule.onNode(card(summary)).assertExists()
        assertDetailOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-012 체크리스트 상세 위에서 연 체크리스트 추가는 빈 입력으로 시작한다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        fillPlaceholder()

        openAddOnDetail(summary)

        backStack.last() shouldBe ChecklistAddNavKey
        assertAddInputEmpty()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-013 체크리스트 상세가 놓인 동안 다른 체크리스트를 고르면 교체하고 뒤로가면 체크리스트 추가로 돌아간다`() {
        val first = testChecklistSummary(titlePrefix = FIRST_PREFIX)
        val second = testChecklistSummary(titlePrefix = SECOND_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(first, second))

        selectChecklist(first)
        selectChecklist(second)

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey, ChecklistDetailNavKey(id = second.checklist.id))
        composeRule.onAllNodesWithText(detailTitle(second)).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onAllNodesWithText(detailTitle(first)).fetchSemanticsNodes().isEmpty() shouldBe true

        pressBack()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
        composeRule.onNode(card(first)).assertExists()
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-014 체크리스트 상세 위에서 연 체크리스트 추가가 놓인 동안 다른 체크리스트를 고르면 추가를 걷어내고 그 상세로 바꾼다`() {
        val first = testChecklistSummary(titlePrefix = FIRST_PREFIX)
        val second = testChecklistSummary(titlePrefix = SECOND_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(first, second))
        openAddOnDetail(first)
        titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        selectChecklist(second)
        assertDetailOnDetailPane()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey, ChecklistDetailNavKey(id = second.checklist.id))

        pressBack()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
        assertAddOnDetailPane()
        composeRule.onNodeWithText(TYPED_ADD_TITLE).assertDoesNotExist()
        assertAddInputEmpty()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-014 좁은 창에서 연 체크리스트 추가가 창이 넓어져 상세 영역에 놓인 동안 체크리스트를 고르면 추가를 걷어내고 그 상세로 바꾼다`() {
        val summary = testChecklistSummary(titlePrefix = FIRST_PREFIX)
        directiveOverride = singlePaneDirective()
        setChecklistNavDisplay(summaryList = listOf(summary))
        waitUntilDisplayed(summary)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(card(summary)).assertDoesNotExist()
        titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle { directiveOverride = null }
        composeRule.waitForIdle()
        selectChecklist(summary)
        assertDetailOnDetailPane()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey, ChecklistDetailNavKey(id = summary.checklist.id))

        pressBack()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
        assertAddOnDetailPane()
        composeRule.onNodeWithText(TYPED_ADD_TITLE).assertDoesNotExist()
        assertAddInputEmpty()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-015 체크리스트를 고르지 않은 상태에서 뒤로가면 더보기로 돌아간다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))

        pressBack()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.onNode(card(summary)).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-027 체크리스트 상세가 함께 놓여 있어도 목록의 뒤로가기는 더보기로 돌아간다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        selectChecklist(summary)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-028 상세에 열린 체크리스트를 목록에서 밀어 삭제해도 상세 영역은 그 체크리스트의 상세로 남는다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        selectChecklist(summary)

        composeRule.onNode(card(summary)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { homeViewModelList.last().delete(id = summary.checklist.id) }
        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey, ChecklistDetailNavKey(id = summary.checklist.id))
        assertDetailOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-DOMAIN-004 더보기로 돌아갔다가 다시 진입하면 상세 선택이 초기화된다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        selectChecklist(summary)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(ChecklistHomeNavKey) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-DOMAIN-005 더보기로 돌아갔다가 다시 진입하면 상세 영역의 체크리스트 추가는 입력이 모두 비어 있는 상태로 시작한다`() {
        setChecklistNavDisplay(summaryList = listOf(testChecklistSummary(titlePrefix = LIST_PREFIX)))
        fillPlaceholder()

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(ChecklistHomeNavKey) }
        composeRule.waitForIdle()

        assertAddInputEmpty()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-DOMAIN-006 배치를 떠나기 전에는 상세 영역에서 체크리스트 상세를 보고 돌아와도 체크리스트 추가의 입력이 남는다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        fillPlaceholder()

        selectChecklist(summary)
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
        pressBack()

        assertAddOnDetailPane()
        assertPlaceholderRetained()
    }

    private fun assertProgressChanged(
        checked: Int,
        total: Int,
        action: () -> Unit,
    ) {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX, checkedCount = 1, totalCount = 3)
        setChecklistNavDisplay(summaryList = listOf(summary))
        selectChecklist(summary)
        composeRule.onNode(card(summary) and hasContentDescription(progressDescription(checked = 1, total = 3))).assertExists()

        action()
        composeRule.waitForIdle()

        assertDetailOnDetailPane()
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(card(summary) and hasContentDescription(progressDescription(checked = checked, total = total))).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertPlaceholderRetained() {
        assertAddOnDetailPane()
        titleInput().editableText() shouldBe PLACEHOLDER_TITLE
        descriptionInput().editableText() shouldBe PLACEHOLDER_DESCRIPTION
        composeRule.onNodeWithText(PLACEHOLDER_ITEM).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-016 선택 전 체크리스트 추가에서 추가에 성공해도 상세 영역은 체크리스트 추가로 남고 추가한 체크리스트가 목록에 나타난다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        titleInput().performTextInput(ADDED_TITLE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
        assertAddOnDetailPane()
        assertAddInputEmpty()
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(ADDED_TITLE)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(card(summary)).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-017 상세 영역에서 체크하지 않은 항목을 체크하면 목록의 체크한 항목 수가 하나 늘어난다`() {
        assertProgressChanged(checked = 2, total = 3) {
            composeRule.onNodeWithText(DETAIL_ITEM).performClick()
        }
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-017 상세 영역에서 새 항목을 넣으면 목록의 전체 항목 수가 하나 늘어난다`() {
        assertProgressChanged(checked = 1, total = 4) {
            itemInput().performTextInput(NEW_DETAIL_ITEM)
            itemInput().performImeAction()
        }
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-017 상세 영역에서 체크하지 않은 항목을 빼면 목록의 전체 항목 수가 하나 줄어든다`() {
        assertProgressChanged(checked = 1, total = 2) {
            composeRule.onNodeWithContentDescription("$DEFAULT_REMOVE_ITEM_PREFIX$DETAIL_ITEM").performClick()
        }
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-FEATURE-017 상세 영역에서 리셋을 확인하면 목록의 체크한 항목 수가 0이 된다`() {
        assertProgressChanged(checked = 0, total = 3) {
            composeRule.onNodeWithContentDescription(DEFAULT_RESET_DESCRIPTION).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText(DEFAULT_RESET_CONFIRM).performClick()
        }
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-DOMAIN-010 목록에서 연 체크리스트 추가를 뒤로가기로 닫아도 선택 전 체크리스트 추가의 작성 내용이 남는다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        fillPlaceholder()
        openAddOnDetail(summary)
        assertAddInputEmpty()

        pressBack()
        assertDetailOnDetailPane()
        pressBack()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
        assertPlaceholderRetained()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-DOMAIN-010 목록에서 연 체크리스트 추가를 다른 체크리스트를 골라 걷어내도 선택 전 체크리스트 추가의 작성 내용이 남는다`() {
        val first = testChecklistSummary(titlePrefix = FIRST_PREFIX)
        val second = testChecklistSummary(titlePrefix = SECOND_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(first, second))
        fillPlaceholder()
        openAddOnDetail(first)
        assertAddInputEmpty()

        selectChecklist(second)
        assertDetailOnDetailPane()
        pressBack()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey)
        assertPlaceholderRetained()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-DOMAIN-007 화면이 재생성되어도 상세 선택을 유지한다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var restoredBackStack: NavBackStack<ScreenNavKey>
        setRestorableNavDisplay(summaryList = listOf(summary), restorationTester = restorationTester) { stack -> restoredBackStack = stack }
        selectChecklist(summary)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        restoredBackStack.toList() shouldBe listOf(ChecklistHomeNavKey, ChecklistDetailNavKey(id = summary.checklist.id))
        waitUntilDisplayed(summary)
        assertDetailOnDetailPane()
        pressBack()
        restoredBackStack.toList() shouldBe listOf(ChecklistHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-DOMAIN-008 시스템이 앱을 정리했다가 다시 만들어도 상세 선택과 뒤로가기 기록을 유지한다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var restoredBackStack: NavBackStack<ScreenNavKey>
        setRestorableNavDisplay(summaryList = listOf(summary), restorationTester = restorationTester) { stack -> restoredBackStack = stack }
        openAddOnDetail(summary)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        restoredBackStack.toList() shouldBe listOf(ChecklistHomeNavKey, ChecklistDetailNavKey(id = summary.checklist.id), ChecklistAddNavKey)
        waitUntilDisplayed(summary)
        assertAddOnDetailPane()
        pressBack()
        restoredBackStack.toList() shouldBe listOf(ChecklistHomeNavKey, ChecklistDetailNavKey(id = summary.checklist.id))
        assertDetailOnDetailPane()
        pressBack()
        restoredBackStack.toList() shouldBe listOf(ChecklistHomeNavKey)
        assertAddOnDetailPane()
    }

    @Test
    fun `TC-CHECKLIST-LIST-DETAIL-DOMAIN-009 앱이 백그라운드에 다녀와도 상세 선택을 유지한다`() {
        val summary = testChecklistSummary(titlePrefix = LIST_PREFIX)
        setChecklistNavDisplay(summaryList = listOf(summary))
        selectChecklist(summary)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(ChecklistListDetailMoreNavKey, ChecklistHomeNavKey, ChecklistDetailNavKey(id = summary.checklist.id))
        composeRule.onNode(card(summary)).assertExists()
        assertDetailOnDetailPane()
        pressBack()
        assertAddOnDetailPane()
    }

    private fun titleInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[0]

    private fun descriptionInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[1]

    private fun itemInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[2]

    private fun fillPlaceholder() {
        titleInput().performTextInput(PLACEHOLDER_TITLE)
        descriptionInput().performTextInput(PLACEHOLDER_DESCRIPTION)
        itemInput().performTextInput(PLACEHOLDER_ITEM)
        itemInput().performImeAction()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(PLACEHOLDER_ITEM).assertExists()
    }

    private fun assertAddInputEmpty() {
        titleInput().editableText() shouldBe ""
        descriptionInput().editableText() shouldBe ""
        itemInput().editableText() shouldBe ""
        composeRule.onNodeWithText(PLACEHOLDER_ITEM).assertDoesNotExist()
    }

    private fun assertAddOnDetailPane() {
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_RESET_DESCRIPTION).assertDoesNotExist()
    }

    private fun assertDetailOnDetailPane() {
        composeRule.onNodeWithContentDescription(DEFAULT_RESET_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_ADD_TITLE).assertDoesNotExist()
    }

    private fun selectChecklist(summary: ChecklistSummary) {
        waitUntilDisplayed(summary)
        composeRule.onNode(card(summary)).performClick()
        composeRule.waitForIdle()
    }

    private fun openAddOnDetail(summary: ChecklistSummary) {
        selectChecklist(summary)
        assertDetailOnDetailPane()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun waitUntilDisplayed(summary: ChecklistSummary) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(summary.checklist.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun singlePaneDirective(): PaneScaffoldDirective = PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 1)

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setChecklistNavDisplay(summaryList: List<ChecklistSummary>) {
        homeSummaryList = summaryList
        homePagingData.value = checklistPagingDataOf(summaryList)
        val module = viewModelModule()
        composeRule.setContent {
            ChecklistListDetailTestHost(module = module) {
                val holder = rememberListDetailPlaceholderStateHolder()

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies =
                            listOf(
                                rememberDiaryListDetailSceneStrategy(
                                    directive = directiveOverride ?: calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()),
                                ),
                            ),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider =
                            entryProvider {
                                entry<ChecklistListDetailMoreNavKey> { Text(text = MORE_CONTENT) }
                                checklistEntry(backStack = backStack)
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        summaryList.firstOrNull()?.let(::waitUntilDisplayed)
    }

    // 시스템이 되살릴 때 전환 이력이 저장된 상태에서 다시 만들어지도록 제품 내비게이션 키 직렬화 구성으로 전환 이력을 기억한다.
    private fun setRestorableNavDisplay(
        summaryList: List<ChecklistSummary>,
        restorationTester: StateRestorationTester,
        onBackStack: (NavBackStack<ScreenNavKey>) -> Unit,
    ) {
        homeSummaryList = summaryList
        homePagingData.value = checklistPagingDataOf(summaryList)
        val module = viewModelModule()
        restorationTester.setContent {
            ChecklistListDetailTestHost(module = module) {
                val restorableBackStack =
                    rememberSerializable(
                        configuration = checklistSavedStateConfiguration,
                        serializer = NavBackStackSerializer(PolymorphicSerializer(ScreenNavKey::class)),
                    ) { NavBackStack<ScreenNavKey>(ChecklistHomeNavKey) }
                val holder = rememberListDetailPlaceholderStateHolder()
                onBackStack(restorableBackStack)

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = restorableBackStack,
                        sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider = entryProvider { checklistEntry(backStack = restorableBackStack) },
                    )
                }
            }
        }
        composeRule.waitForIdle()
        summaryList.firstOrNull()?.let(::waitUntilDisplayed)
    }

    @Composable
    private fun ChecklistListDetailTestHost(
        module: Module,
        content: @Composable () -> Unit,
    ) {
        // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(module) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 체크 항목이 저장되면 목록의 진행 정도가 다시 조회되는 것과 같게 목록 조회 결과를 바꾼다.
    private fun updateProgress(
        id: Uuid,
        transform: (ChecklistSummary) -> ChecklistSummary,
    ) {
        homeSummaryList = homeSummaryList.map { summary -> if (summary.checklist.id == id) transform(summary) else summary }
        homePagingData.value = checklistPagingDataOf(homeSummaryList)
    }

    // 배치를 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun viewModelModule(): Module =
        module {
            factory {
                mockk<ChecklistHomeViewModel>(relaxed = true)
                    .apply {
                        every { checklistPagingData } returns homePagingData
                        every { effect } returns emptyFlow()
                    }.also(homeViewModelList::add)
            }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(SyncRefreshUiState())
                }
            }
            factory { parameters ->
                val id = parameters.get<Uuid>()
                val summary = homeSummaryList.first { summary -> summary.checklist.id == id }
                val effectChannel = Channel<ChecklistDetailEffect>(capacity = Channel.BUFFERED)
                mockk<ChecklistDetailViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(ChecklistDetailUiState.Content(id = id, detail = summary.checklist.detail.copy(title = detailTitle(summary))))
                    every { effect } returns effectChannel.receiveAsFlow()
                    every { delete() } answers { effectChannel.trySend(ChecklistDetailEffect.DeleteSucceeded).getOrThrow() }
                }
            }
            factory { parameters ->
                val id = parameters.get<Uuid>()
                mockk<ChecklistDetailItemViewModel>(relaxed = true).apply {
                    every { uiState } returns
                        MutableStateFlow(
                            ChecklistDetailItemUiState(
                                itemList =
                                    listOf(
                                        checklistTestFixtureMonkey.checklistItem(isChecked = false, isDeleted = false, content = DETAIL_ITEM),
                                        checklistTestFixtureMonkey.checklistItem(isChecked = true, isDeleted = false, content = DETAIL_CHECKED_ITEM),
                                    ),
                                isLoaded = true,
                                sort = ChecklistItemSort.ADDED,
                            ),
                        )
                    every { effect } returns emptyFlow()
                    every { toggle(id = any(), isChecked = false) } answers { updateProgress(id = id) { summary -> summary.copy(checkedCount = summary.checkedCount + 1) } }
                    every { add(content = any()) } answers { updateProgress(id = id) { summary -> summary.copy(totalCount = summary.totalCount + 1) } }
                    every { delete(itemId = any()) } answers { updateProgress(id = id) { summary -> summary.copy(totalCount = summary.totalCount - 1) } }
                    every { reset() } answers { updateProgress(id = id) { summary -> summary.copy(checkedCount = 0) } }
                }
            }
            factory {
                val effectChannel = Channel<ChecklistAddEffect>(capacity = Channel.BUFFERED)
                mockk<ChecklistAddViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(ChecklistAddUiState())
                    every { effect } returns effectChannel.receiveAsFlow()
                    // 추가가 저장되면 목록이 다시 조회되는 것과 같게 추가한 체크리스트를 목록 조회 결과에 더한다.
                    every { add(detail = any(), itemContentList = any()) } answers {
                        val added = testChecklistSummary().let { summary -> summary.copy(checklist = summary.checklist.copy(detail = firstArg<ChecklistDetail>())) }
                        homeSummaryList = homeSummaryList + added
                        homePagingData.value = checklistPagingDataOf(homeSummaryList)
                        effectChannel.trySend(ChecklistAddEffect.AddSucceeded).getOrThrow()
                    }
                }
            }
        }

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val LIST_PREFIX = "list-"
        const val FIRST_PREFIX = "a-$LIST_PREFIX"
        const val SECOND_PREFIX = "b-$LIST_PREFIX"
        const val DETAIL_PREFIX = "detail-"
        const val ADDED_TITLE = "added-title"
        const val DETAIL_ITEM = "detail-item"
        const val DETAIL_CHECKED_ITEM = "detail-checked-item"
        const val NEW_DETAIL_ITEM = "new-detail-item"
        const val DEFAULT_REMOVE_ITEM_PREFIX = "Remove item, "
        const val DEFAULT_RESET_CONFIRM = "Reset"
        const val PLACEHOLDER_TITLE = "placeholder-title"
        const val PLACEHOLDER_DESCRIPTION = "placeholder-description"
        const val PLACEHOLDER_ITEM = "placeholder-item"
        const val TYPED_ADD_TITLE = "typed-add-title"
        const val DEFAULT_ADD_TITLE = "Add checklist"
        const val DEFAULT_ADD_DESCRIPTION = "Add checklist"
        const val DEFAULT_DELETE_DESCRIPTION = "Delete checklist"
        const val DEFAULT_RESET_DESCRIPTION = "Uncheck all"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L

        val checklistSavedStateConfiguration: SavedStateConfiguration =
            SavedStateConfiguration {
                serializersModule =
                    SerializersModule {
                        polymorphic(ScreenNavKey::class) { checklistNavKeys() }
                    }
            }

        fun detailTitle(summary: ChecklistSummary): String = DETAIL_PREFIX + summary.checklist.detail.title

        fun progressDescription(
            checked: Int,
            total: Int,
        ): String = "$checked of $total checked"

        fun card(summary: ChecklistSummary): SemanticsMatcher = hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(summary.checklist.detail.title)

        fun detailDeleteButton(): SemanticsMatcher = hasContentDescription(DEFAULT_DELETE_DESCRIPTION) and hasClickAction()
    }
}

// 체크리스트 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object ChecklistListDetailMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
