package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.web.ui.refreshableList
import io.github.taetae98coding.diary.feature.web.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 정렬을 고르면 새 정렬로 다시 조회한 목록이 이어서 전달되는 전환을 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebHomeSortScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-30358b69-646a-4f9e-80ed-a91f9438667d 정렬을 바꾸면 목록이 맨 위에서 다시 시작한다`() {
        val titleSortedWebList = List(WEB_COUNT) { index -> testWeb(title = webTitle(index = index)) }
        // 새 순서의 첫 항목은 스크롤해 둔 자리에서 보이지 않던 항목이라, 보던 항목을 따라 자리를 지키면 첫 항목이 보이지 않는다.
        val recentlyUpdatedWebList = listOf(titleSortedWebList.first()) + titleSortedWebList.drop(1).reversed()
        var sort by mutableStateOf(ListSort.TITLE)
        val pagingDataFlow = MutableStateFlow(webPagingDataOf(titleSortedWebList))
        val sortSheetState = DialogState()
        composeRule.setContent {
            DiaryTheme {
                WebHomeScaffold(
                    onEvent = { event ->
                        when (event) {
                            is WebHomeScaffoldEvent.ClickSort -> sortSheetState.show()

                            is WebHomeScaffoldEvent.SelectSort -> {
                                sort = event.sort
                                pagingDataFlow.value = webPagingDataOf(recentlyUpdatedWebList)
                            }

                            else -> Unit
                        }
                    },
                    sortSheetState = sortSheetState,
                    webPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    sortProvider = { sort },
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) { isDisplayed(titleSortedWebList.first().detail.title) }
        composeRule.refreshableList().performScrollToIndex(titleSortedWebList.lastIndex)
        composeRule.waitForIdle()
        isDisplayed(titleSortedWebList.first().detail.title) shouldBe false

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) { isDisplayed(recentlyUpdatedWebList.first().detail.title) }
        composeRule.onNodeWithText(recentlyUpdatedWebList[1].detail.title).assertIsDisplayed()
        isDisplayed(recentlyUpdatedWebList.last().detail.title) shouldBe false
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private fun webTitle(index: Int): String = "$TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}"

    private companion object {
        const val WEB_COUNT = 30
        const val TITLE_PREFIX = "WebSortScroll"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
    }
}
