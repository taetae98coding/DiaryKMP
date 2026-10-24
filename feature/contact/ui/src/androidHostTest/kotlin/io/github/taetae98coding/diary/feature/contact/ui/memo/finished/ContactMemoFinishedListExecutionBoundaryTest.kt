package io.github.taetae98coding.diary.feature.contact.ui.memo.finished

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedContactMemoUseCase
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.contact.ui.refreshableList
import io.github.taetae98coding.diary.feature.contact.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListScreen
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private fun fixtureId(): Uuid = fixtureMonkey.giveMeOne<Uuid>()

private fun fixtureText(prefix: String): String = prefix + fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)

private fun screenTestSyncViewModel(): SyncRefreshViewModel {
    val viewModel = mockk<SyncRefreshViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(SyncRefreshUiState())
    return viewModel
}

// 화면을 떠났다 돌아올 때의 상태는 전환 이력의 항목마다 보관되므로, 앱과 같은 방식으로 항목을 그리는 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ContactMemoFinishedListExecutionBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val contactId = fixtureId()
    private val finishedListKey = ContactMemoFinishedListNavKey(contactId = contactId)
    private val backStack = NavBackStack<ScreenNavKey>(ContactDetailNavKey(id = contactId))
    private val syncViewModel = screenTestSyncViewModel()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-022 화면에 들어오거나 다른 화면에서 돌아오는 것만으로는 새로고침하지 않는다`() {
        setFinishedListNavDisplay(memoList = memoList())

        openFinishedList()
        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureId())) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        verify(exactly = 0) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-023 메모 상세에서 돌아오면 목록에서 보던 자리가 그대로다`() {
        val memoList = memoList()
        setFinishedListNavDisplay(memoList = memoList)
        openFinishedList()
        scrollList(memoList = memoList)

        composeRule.onNodeWithText(memoList[SCROLL_INDEX].detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        assertScrolledPosition(memoList = memoList)
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-DOMAIN-007 상세 화면으로 돌아갔다가 다시 진입하면 처음 정렬로 맨 위부터 보인다`() {
        val memoList = memoList()
        setFinishedListNavDisplay(memoList = memoList)
        openFinishedList()
        selectTitleSort()
        scrollList(memoList = memoList)

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONTACT_DETAIL_CONTENT).assertIsDisplayed()
        openFinishedList()

        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(memoList.first().detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-ENTITY-MEMO-FINISHED-LIST-DOMAIN-008 앱이 백그라운드에 다녀와도 정렬 선택과 보던 자리가 그대로다`() {
        val memoList = memoList()
        setFinishedListNavDisplay(memoList = memoList)
        openFinishedList()
        selectTitleSort()
        scrollList(memoList = memoList)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
        assertScrolledPosition(memoList = memoList)
    }

    private fun openFinishedList() {
        composeRule.runOnIdle { backStack.add(finishedListKey) }
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_DEFAULT_SORT).fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText(DEFAULT_TITLE_SORT).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun selectTitleSort() {
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).performClick()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_SORT_SHEET_TITLE).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun scrollList(memoList: List<Memo>) {
        composeRule.refreshableList().performScrollToIndex(SCROLL_INDEX)
        composeRule.waitForIdle()
        assertScrolledPosition(memoList = memoList)
    }

    private fun assertScrolledPosition(memoList: List<Memo>) {
        composeRule.onNodeWithText(memoList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    private fun setFinishedListNavDisplay(memoList: List<Memo>) {
        val pageFinishedContactMemoUseCase = mockk<PageFinishedContactMemoUseCase>()
        every { pageFinishedContactMemoUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(memoList))) }
        val findContactUseCase = mockk<FindContactUseCase>()
        every { findContactUseCase(parameter = contactId) } returns flowOf(Result.success(null))

        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    entryProvider =
                        entryProvider {
                            entry<ContactDetailNavKey> { Text(text = CONTACT_DETAIL_CONTENT) }
                            entry<ContactMemoFinishedListNavKey> { key ->
                                EntityMemoFinishedListScreen(
                                    navigateUp = { backStack.removeLastOrNull() },
                                    navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id = id)) },
                                    emptyTitle = EMPTY_TITLE,
                                    memoViewModel =
                                        viewModel {
                                            ContactMemoFinishedListViewModel(
                                                contactId = key.contactId,
                                                pageFinishedContactMemoUseCase = pageFinishedContactMemoUseCase,
                                                findContactUseCase = findContactUseCase,
                                                restartMemoUseCase = mockk(),
                                                finishMemoUseCase = mockk(),
                                                deleteMemoUseCase = mockk(),
                                                restoreMemoUseCase = mockk(),
                                            )
                                        },
                                    syncViewModel = syncViewModel,
                                )
                            }
                            entry<MemoDetailNavKey> { Text(text = DETAIL_CONTENT) }
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun memoList(): List<Memo> {
        val titlePrefix = fixtureText(prefix = "FinishedMemo")

        return List(MEMO_COUNT) { index -> fixtureMonkey.memo(title = "${titlePrefix}Index$index") }
    }

    private companion object {
        private const val EMPTY_TITLE = "EmptyTitle"
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val MEMO_COUNT = 40
        private const val SCROLL_INDEX = 30
        private const val CONTACT_DETAIL_CONTENT = "ContactDetailContent"
        private const val DETAIL_CONTENT = "MemoDetailContent"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_DEFAULT_SORT = "Default"
        private const val DEFAULT_TITLE_SORT = "Title"
    }
}
