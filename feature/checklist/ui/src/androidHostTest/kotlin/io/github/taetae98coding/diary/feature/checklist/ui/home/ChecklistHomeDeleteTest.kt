package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.domain.checklist.usecase.DeleteChecklistUseCase
import io.github.taetae98coding.diary.domain.checklist.usecase.PageChecklistUseCase
import io.github.taetae98coding.diary.feature.checklist.ui.card.CHECKLIST_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.checklist.ui.checklistPagingDataOf
import io.github.taetae98coding.diary.feature.checklist.ui.checklistTestFixtureMonkey
import io.github.taetae98coding.diary.feature.checklist.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.checklist.ui.testChecklistSummary
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ChecklistHomeDeleteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-HOME-FEATURE-019 체크리스트 카드를 삭제 방향으로 밀면 삭제되고 안내와 실행 취소가 표시된다`() {
        val environment = setChecklistHomeScreen()

        swipeCardLeft(summary = environment.first)

        verify(exactly = 1) { environment.viewModel.delete(id = environment.first.checklist.id) }
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-019 기본 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        val environment = setChecklistHomeScreen()

        swipeCardLeft(summary = environment.first)

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-020 삭제를 실행 취소하면 그 체크리스트의 삭제가 되돌려진다`() {
        val environment = setChecklistHomeScreen()

        swipeCardLeft(summary = environment.first)
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.first.checklist.id) }
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-021 체크리스트 카드를 반대 방향으로 밀면 아무 동작도 실행하지 않는다`() {
        val environment = setChecklistHomeScreen()

        composeRule.onNode(hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(environment.first.checklist.detail.title)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 0) { environment.viewModel.delete(id = any()) }
        composeRule.onNode(hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(environment.first.checklist.detail.title)).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-023 안내가 보이는 동안 다른 체크리스트를 삭제하면 마지막 삭제의 안내만 남는다`() {
        val environment = setChecklistHomeScreen()

        swipeCardLeft(summary = environment.first)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        environment.effectChannel.trySend(ChecklistHomeEffect.Deleted(id = environment.second.checklist.id)).getOrThrow()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.second.checklist.id) }
        verify(exactly = 0) { environment.viewModel.restore(id = environment.first.checklist.id) }
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-024 삭제가 저장되지 못하면 안내를 표시하지 않는다`() {
        val first = testChecklistSummary()
        val pageChecklistUseCase = mockk<PageChecklistUseCase>()
        every { pageChecklistUseCase(parameter = Unit) } returns flowOf(Result.success(checklistPagingDataOf(listOf(first))))
        val deleteChecklistUseCase = mockk<DeleteChecklistUseCase>()
        coEvery { deleteChecklistUseCase(parameter = any()) } returns Result.failure(IllegalStateException(checklistTestFixtureMonkey.giveMeOne<String>()))
        val viewModel =
            ChecklistHomeViewModel(
                pageChecklistUseCase = pageChecklistUseCase,
                deleteChecklistUseCase = deleteChecklistUseCase,
                restoreChecklistUseCase = mockk(),
            )
        setChecklistHomeScreenContent(viewModel = viewModel, first = first)

        swipeCardLeft(summary = first)

        coVerify(exactly = 1) { deleteChecklistUseCase(parameter = first.checklist.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-025 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val environment = setChecklistHomeScreen()

        swipeCardLeft(summary = environment.first)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-026 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        var isShown by mutableStateOf(true)
        val environment = setChecklistHomeScreen(isShownProvider = { isShown })

        swipeCardLeft(summary = environment.first)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `목록에서 체크리스트를 삭제해도 화면을 이동하지 않는다`() {
        var navigateCount = 0
        val environment = setChecklistHomeScreen(onNavigate = { navigateCount += 1 })

        swipeCardLeft(summary = environment.first)

        navigateCount shouldBe 0
    }

    private fun swipeCardLeft(summary: ChecklistSummary) {
        composeRule.onNode(hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(summary.checklist.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun setChecklistHomeScreen(
        isShownProvider: () -> Boolean = { true },
        onNavigate: () -> Unit = {},
    ): Environment {
        val first = testChecklistSummary()
        val second = testChecklistSummary()
        val effectChannel = Channel<ChecklistHomeEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<ChecklistHomeViewModel>()
        every { viewModel.checklistPagingData } returns MutableStateFlow(checklistPagingDataOf(listOf(first, second)))
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.delete(id = any()) } answers {
            effectChannel.trySend(ChecklistHomeEffect.Deleted(id = firstArg())).getOrThrow()
        }
        justRun { viewModel.restore(id = any()) }

        setChecklistHomeScreenContent(
            viewModel = viewModel,
            first = first,
            isShownProvider = isShownProvider,
            onNavigate = onNavigate,
        )

        return Environment(
            viewModel = viewModel,
            effectChannel = effectChannel,
            first = first,
            second = second,
        )
    }

    private fun setChecklistHomeScreenContent(
        viewModel: ChecklistHomeViewModel,
        first: ChecklistSummary,
        isShownProvider: () -> Boolean = { true },
        onNavigate: () -> Unit = {},
    ) {
        val syncViewModel = mockk<SyncRefreshViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(SyncRefreshUiState())
        justRun { syncViewModel.refresh() }

        composeRule.setContent {
            DiaryTheme {
                if (isShownProvider()) {
                    ChecklistHomeScreen(
                        navigateUp = onNavigate,
                        navigateToAdd = onNavigate,
                        navigateToDetail = { onNavigate() },
                        componentVisibleProvider = { ChecklistHomeScaffoldComponentVisible() },
                        checklistViewModel = viewModel,
                        syncViewModel = syncViewModel,
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(first.checklist.detail.title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private class Environment(
        val viewModel: ChecklistHomeViewModel,
        val effectChannel: Channel<ChecklistHomeEffect>,
        val first: ChecklistSummary,
        val second: ChecklistSummary,
    )

    private companion object {
        const val DEFAULT_DELETED_MESSAGE = "Checklist deleted."
        const val KOREAN_DELETED_MESSAGE = "체크리스트가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
    }
}
