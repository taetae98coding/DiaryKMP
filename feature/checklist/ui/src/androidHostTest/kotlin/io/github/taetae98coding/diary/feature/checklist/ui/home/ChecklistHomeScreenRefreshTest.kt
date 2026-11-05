package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.checklist.ui.card.CHECKLIST_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.checklist.ui.checklistPagingDataOf
import io.github.taetae98coding.diary.feature.checklist.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.checklist.ui.testChecklistSummary
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ChecklistHomeScreenRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-cf5344b6-e2ce-4548-a4eb-5b8446509b5b 체크리스트가 표시된 목록을 당기면 계정 데이터의 서버 동기화를 한 번 요청한다`() {
        val syncViewModel = syncViewModel()

        setChecklistHomeScreen(summaryList = listOf(testChecklistSummary()), syncViewModel = syncViewModel)
        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-cf5344b6-e2ce-4548-a4eb-5b8446509b5b 빈 상태 안내가 표시된 목록을 당기면 계정 데이터의 서버 동기화를 한 번 요청한다`() {
        val syncViewModel = syncViewModel()

        setChecklistHomeScreen(summaryList = emptyList(), syncViewModel = syncViewModel)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { syncViewModel.refresh() }
    }

    private fun setChecklistHomeScreen(
        summaryList: List<ChecklistSummary>,
        syncViewModel: SyncRefreshViewModel,
    ) {
        val checklistViewModel = mockk<ChecklistHomeViewModel>()
        every { checklistViewModel.checklistPagingData } returns MutableStateFlow(checklistPagingDataOf(summaryList))
        every { checklistViewModel.effect } returns emptyFlow()

        composeRule.setContent {
            DiaryTheme {
                ChecklistHomeScreen(
                    navigateUp = {},
                    navigateToAdd = {},
                    navigateToDetail = {},
                    componentVisibleProvider = { ChecklistHomeScaffoldComponentVisible() },
                    checklistViewModel = checklistViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun syncViewModel(): SyncRefreshViewModel {
        val viewModel = mockk<SyncRefreshViewModel>()
        every { viewModel.uiState } returns MutableStateFlow(SyncRefreshUiState())
        justRun { viewModel.refresh() }

        return viewModel
    }
}
