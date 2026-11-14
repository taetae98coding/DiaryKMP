package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.memo.finished.screenTestSyncViewModel
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagRoutine
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
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
class TagRoutineFinishedListRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-d21ea977-c027-4835-adab-d345b1d5a5fa 화면이 재생성되어도 보던 자리가 그대로다`() {
        val titlePrefix = Arb.alphanumericString(prefix = "FinishedRoutine").next()
        val routineList = List(ROUTINE_COUNT) { index -> tagRoutine(title = "${titlePrefix}Index$index", isFinished = true) }
        val viewModel = mockk<TagRoutineFinishedListViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(TagRoutineFinishedListUiState())
        every { viewModel.routinePagingData } returns MutableStateFlow(tagEntityPagingData(itemList = routineList))
        every { viewModel.effect } returns emptyFlow()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                TagRoutineFinishedListScreen(
                    navigateUp = {},
                    navigateToRoutineDetail = {},
                    routineViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithText(
                    routineList
                        .first()
                        .routine.detail.title,
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.refreshableList().performScrollToIndex(SCROLL_INDEX)
        composeRule.onNodeWithText(routineList[SCROLL_INDEX].routine.detail.title).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                routineList
                    .first()
                    .routine.detail.title,
            ).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(routineList[SCROLL_INDEX].routine.detail.title).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                routineList
                    .first()
                    .routine.detail.title,
            ).assertDoesNotExist()
    }

    private companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val ROUTINE_COUNT = 40
        private const val SCROLL_INDEX = 30
    }
}
