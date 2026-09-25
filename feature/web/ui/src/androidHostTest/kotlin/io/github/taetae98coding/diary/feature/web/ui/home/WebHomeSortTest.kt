package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebHomeSortTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-HOME-FEATURE-018 목록 위에 현재 정렬을 표시한다`() {
        setWebHomeScaffold(webList = listOf(testWeb(title = TITLE)))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-019 정렬 컨트롤을 누르면 고를 수 있는 정렬이 나타난다`() {
        setWebHomeScaffold(webList = listOf(testWeb(title = TITLE)), sortSheetState = DialogState(isVisible = true))

        composeRule.onNodeWithText(DEFAULT_SORT_SHEET_TITLE).assertExists()
        composeRule.onAllNodesWithText(DEFAULT_TITLE_SORT).onLast().assertExists()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-020 최근 수정순을 고르면 그 정렬을 전달하고 선택 목록이 닫힌다`() {
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        val sortSheetState = DialogState(isVisible = true)
        setWebHomeScaffold(webList = listOf(testWeb(title = TITLE)), onEvent = eventList::add, sortSheetState = sortSheetState)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(WebHomeScaffoldEvent.SelectSort(sort = ListSort.RECENTLY_UPDATED))
        sortSheetState.isVisible shouldBe false
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-021 지금 선택된 정렬을 다시 골라도 선택 목록이 닫힌다`() {
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        val sortSheetState = DialogState(isVisible = true)
        setWebHomeScaffold(webList = listOf(testWeb(title = TITLE)), onEvent = eventList::add, sortSheetState = sortSheetState)

        composeRule.onAllNodesWithText(DEFAULT_TITLE_SORT).onLast().performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(WebHomeScaffoldEvent.SelectSort(sort = ListSort.TITLE))
        sortSheetState.isVisible shouldBe false
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-024 목록이 비어 있으면 정렬 컨트롤이 표시되지 않는다`() {
        setWebHomeScaffold()

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `정렬 컨트롤은 선택한 정렬의 이름을 표시한다`() {
        setWebHomeScaffold(webList = listOf(testWeb(title = TITLE)), sort = ListSort.RECENTLY_UPDATED)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 정렬 컨트롤 이름은 목록 정렬이고 정렬 이름은 제목순이다`() {
        setWebHomeScaffold(webList = listOf(testWeb(title = TITLE)))

        composeRule.onNodeWithContentDescription(KOREAN_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(KOREAN_TITLE_SORT).assertExists()
    }

    private fun setWebHomeScaffold(
        webList: List<Web> = emptyList(),
        onEvent: (WebHomeScaffoldEvent) -> Unit = {},
        sortSheetState: DialogState = DialogState(),
        sort: ListSort = ListSort.TITLE,
    ) {
        val webPagingDataFlow = MutableStateFlow(webPagingDataOf(webList))

        composeRule.setContent {
            DiaryTheme {
                WebHomeScaffold(
                    onEvent = onEvent,
                    sortSheetState = sortSheetState,
                    webPagingItems = webPagingDataFlow.collectAsLazyPagingItems(),
                    sortProvider = { sort },
                )
            }
        }
    }

    private companion object {
        private const val TITLE = "WebTitle"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val KOREAN_SORT_DESCRIPTION = "목록 정렬"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_TITLE_SORT = "Title"
        private const val KOREAN_TITLE_SORT = "제목순"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        private const val DEFAULT_DEFAULT_SORT = "Default"
    }
}
