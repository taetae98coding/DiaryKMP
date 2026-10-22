package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.checklist.ui.checklistPagingDataOf
import io.github.taetae98coding.diary.feature.checklist.ui.refreshableList
import io.github.taetae98coding.diary.feature.checklist.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.checklist.ui.testChecklistSummary
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ChecklistHomeListRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-864af22b-49c5-40cb-ac0a-2663914b7a28 화면이 재생성되어도 보던 목록 위치를 유지한다`() {
        val summaryList = restorationChecklistList()
        val pagingDataFlow = MutableStateFlow(checklistPagingDataOf(summaryList))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                ChecklistHomeList(
                    onEvent = {},
                    checklistPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.refreshableList().performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(summaryList[RESTORATION_SCROLL_INDEX].checklist.detail.title).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                summaryList
                    .first()
                    .checklist.detail.title,
            ).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(summaryList[RESTORATION_SCROLL_INDEX].checklist.detail.title).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                summaryList
                    .first()
                    .checklist.detail.title,
            ).assertDoesNotExist()
    }

    @Test
    fun `TC-f6ee4744-8294-41a3-ac82-cb213db4441e 앱이 백그라운드에 갔다가 돌아와도 보던 목록 위치를 유지한다`() {
        val summaryList = restorationChecklistList()
        val pagingDataFlow = MutableStateFlow(checklistPagingDataOf(summaryList))
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    ChecklistHomeList(
                        onEvent = {},
                        checklistPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    )
                }
            }
        }
        composeRule.refreshableList().performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(summaryList[RESTORATION_SCROLL_INDEX].checklist.detail.title).assertIsDisplayed()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(summaryList[RESTORATION_SCROLL_INDEX].checklist.detail.title).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                summaryList
                    .first()
                    .checklist.detail.title,
            ).assertDoesNotExist()
    }

    private fun restorationChecklistList(): List<ChecklistSummary> =
        List(RESTORATION_COUNT) { index ->
            testChecklistSummary(titlePrefix = "${index.toString().padStart(length = 2, padChar = '0')}-")
        }

    private companion object {
        private const val RESTORATION_COUNT = 40
        private const val RESTORATION_SCROLL_INDEX = 30
    }
}
