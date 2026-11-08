package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
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
class DDayHomeListRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    // 필터 선택과 목록은 화면 재생성을 넘어 유지되는 ViewModel이 들고 있으므로 같은 흐름을 다시 구독한다.
    @Test
    fun `TC-05d1d379-139a-418c-b40e-58533e86a090 화면이 재생성되어도 보던 목록 위치와 필터 선택을 유지한다`() {
        val tagId = Arb.kotlinUuid().next()
        val ddayList = List(DDAY_COUNT) { index -> homeDDayOf(title = "$TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}") }
        val viewModel = mockk<DDayHomeViewModel>()
        every { viewModel.ddayPagingData } returns MutableStateFlow(homeDDayPagingDataOf(ddayList))
        every { viewModel.filterUiState } returns MutableStateFlow(DDayHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)))
        every { viewModel.effect } returns emptyFlow()
        val syncViewModel = mockk<SyncRefreshViewModel>()
        every { syncViewModel.uiState } returns MutableStateFlow(SyncRefreshUiState())
        justRun { syncViewModel.refresh() }
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                DDayHomeScreen(
                    navigateUp = {},
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = {},
                    componentVisibleProvider = { DDayHomeScaffoldComponentVisible() },
                    ddayViewModel = viewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(ddayList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.homeRefreshableList().performScrollToIndex(SCROLLED_INDEX)
        composeRule.onNodeWithText(ddayList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(ddayList.first().detail.title).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        filterButtonStateDescription() shouldBe DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION
        composeRule.onNodeWithText(ddayList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(ddayList.first().detail.title).assertDoesNotExist()
    }

    private fun filterButtonStateDescription(): String? =
        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription)

    private companion object {
        const val DDAY_COUNT = 40
        const val SCROLLED_INDEX = 30
        const val TIMEOUT_MILLIS = 5_000L
        const val TITLE_PREFIX = "RestorationDDay"
        const val DEFAULT_FILTER_DESCRIPTION = "Filter by tag"
        const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Tag filter applied"
    }
}
