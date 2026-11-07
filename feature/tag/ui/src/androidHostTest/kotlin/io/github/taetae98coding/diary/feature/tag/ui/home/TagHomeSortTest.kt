package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.tag.ui.list.loadingTagPagingData
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
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
@Config(sdk = [36])
class TagHomeSortTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-db1176a5-b24d-446b-a36f-445267ef29c9 목록 위에 현재 정렬을 표시한다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(listOf(testTag())))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-db1176a5-b24d-446b-a36f-445267ef29c9 한국어 환경에서 정렬 컨트롤은 목록 정렬이고 정렬 이름은 제목순이다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(listOf(testTag())))

        composeRule.onNodeWithContentDescription(KOREAN_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(KOREAN_TITLE_SORT).assertExists()
    }

    @Test
    fun `TC-db1176a5-b24d-446b-a36f-445267ef29c9 최근 수정순이 선택되어 있으면 정렬 컨트롤이 최근 수정순을 알린다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(listOf(testTag())), sort = ListSort.RECENTLY_UPDATED)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertDoesNotExist()
    }

    @Test
    fun `TC-cb1837dc-2e25-465c-bc1a-c5e1e9948c73 정렬 컨트롤을 누르면 정렬 선택 목록을 연다`() {
        val eventList = mutableListOf<TagHomeScaffoldEvent>()
        setTagHomeScaffold(pagingData = tagPagingDataOf(listOf(testTag())), onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()

        eventList shouldBe listOf(TagHomeScaffoldEvent.ClickSort)
    }

    @Test
    fun `TC-cb1837dc-2e25-465c-bc1a-c5e1e9948c73 정렬 선택 목록은 제목순과 최근 수정순을 보이고 현재 정렬을 선택 상태로 표시한다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(listOf(testTag())), sortSheetState = DialogState(isVisible = true))

        composeRule.onNodeWithText(DEFAULT_SORT_SHEET_TITLE).assertExists()
        composeRule.onNode(hasText(DEFAULT_TITLE_SORT) and isSelectable()).assertIsSelected()
        composeRule.onNode(hasText(DEFAULT_RECENTLY_UPDATED_SORT) and isSelectable()).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertDoesNotExist()
    }

    @Test
    fun `TC-8057a94c-d9bc-491f-b11f-aa65820ee2a3 최근 수정순을 고르면 그 정렬을 전달하고 선택 목록이 닫힌다`() {
        val eventList = mutableListOf<TagHomeScaffoldEvent>()
        val sortSheetState = DialogState(isVisible = true)
        setTagHomeScaffold(pagingData = tagPagingDataOf(listOf(testTag())), onEvent = eventList::add, sortSheetState = sortSheetState)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagHomeScaffoldEvent.SelectSort(sort = ListSort.RECENTLY_UPDATED))
        sortSheetState.isVisible shouldBe false
    }

    @Test
    fun `TC-789dd2df-4ab2-4892-b499-29150f142804 지금 선택된 정렬을 다시 골라도 선택 목록이 닫힌다`() {
        val eventList = mutableListOf<TagHomeScaffoldEvent>()
        val sortSheetState = DialogState(isVisible = true)
        setTagHomeScaffold(pagingData = tagPagingDataOf(listOf(testTag())), onEvent = eventList::add, sortSheetState = sortSheetState)

        composeRule.onAllNodesWithText(DEFAULT_TITLE_SORT).onLast().performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagHomeScaffoldEvent.SelectSort(sort = ListSort.TITLE))
        sortSheetState.isVisible shouldBe false
    }

    @Test
    fun `TC-5f6fa573-716c-4091-bf61-986c927aaeb3 목록이 비어 있으면 정렬 컨트롤이 표시되지 않는다`() {
        setTagHomeScaffold(pagingData = tagPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-5f6fa573-716c-4091-bf61-986c927aaeb3 목록을 준비하는 동안에는 정렬 컨트롤이 표시되지 않는다`() {
        setTagHomeScaffold(pagingData = loadingTagPagingData())

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    private fun setTagHomeScaffold(
        pagingData: PagingData<Tag>,
        onEvent: (TagHomeScaffoldEvent) -> Unit = {},
        sortSheetState: DialogState = DialogState(),
        sort: ListSort = ListSort.TITLE,
    ) {
        val tagPagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagHomeScaffold(
                    onTagListEvent = {},
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                    sortSheetState = sortSheetState,
                    sortProvider = { sort },
                )
            }
        }
    }

    private fun testTag(): Tag = Arb.tag(title = TAG_TITLE, isFinished = false).next()

    private companion object {
        private const val TAG_TITLE = "TagHomeSortTitle"
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val KOREAN_SORT_DESCRIPTION = "목록 정렬"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_TITLE_SORT = "Title"
        private const val KOREAN_TITLE_SORT = "제목순"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        private const val DEFAULT_DEFAULT_SORT = "Default"
    }
}
