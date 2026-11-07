package io.github.taetae98coding.diary.feature.tag.ui.home

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
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagHomeSortScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-c7a3dd6f-e6ed-47f5-8bae-d638ef9e6ed4 정렬을 바꾸면 목록이 맨 위에서 다시 시작한다`() {
        val titleSortedTagList = List(TAG_COUNT) { index -> testTag(title = tagTitle(index = index)) }
        // 새 순서의 첫 태그는 스크롤해 둔 자리에서 보이지 않던 태그라, 보던 태그를 따라 자리를 지키면 첫 태그가 보이지 않는다.
        val recentlyUpdatedTagList = listOf(titleSortedTagList.first()) + titleSortedTagList.drop(1).reversed()
        var sort by mutableStateOf(ListSort.TITLE)
        val pagingDataFlow = MutableStateFlow(tagPagingDataOf(titleSortedTagList))
        val sortSheetState = DialogState()
        composeRule.setContent {
            DiaryTheme {
                TagHomeScaffold(
                    onTagListEvent = {},
                    onEvent = { event ->
                        when (event) {
                            is TagHomeScaffoldEvent.ClickSort -> {
                                sortSheetState.show()
                            }

                            is TagHomeScaffoldEvent.SelectSort -> {
                                sort = event.sort
                                pagingDataFlow.value = tagPagingDataOf(recentlyUpdatedTagList)
                            }

                            else -> {
                                Unit
                            }
                        }
                    },
                    sortSheetState = sortSheetState,
                    tagPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { TagHomeScaffoldFilterUiState(isLoaded = true) },
                    sortProvider = { sort },
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) { isDisplayed(titleSortedTagList.first().detail.title) }
        composeRule.refreshableList().performScrollToIndex(titleSortedTagList.lastIndex)
        composeRule.waitForIdle()
        isDisplayed(titleSortedTagList.first().detail.title) shouldBe false

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) { isDisplayed(recentlyUpdatedTagList.first().detail.title) }
        composeRule.onNodeWithText(recentlyUpdatedTagList[1].detail.title).assertIsDisplayed()
        isDisplayed(recentlyUpdatedTagList.last().detail.title) shouldBe false
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private fun tagTitle(index: Int): String = "$TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}"

    private fun testTag(title: String): Tag = Arb.tag(title = title, isFinished = false).next()

    private companion object {
        const val TAG_COUNT = 30
        const val TITLE_PREFIX = "TagSortScroll"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
    }
}
