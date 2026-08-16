package io.github.taetae98coding.diary.feature.contact.ui.home

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
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ContactHomeSortTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CONTACT-HOME-FEATURE-010 목록 위에 현재 정렬을 이름순으로 표시한다`() {
        setContactHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_NAME_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-011 정렬 컨트롤을 누르면 이름순과 최근 수정순을 고를 수 있다`() {
        setContactHomeScaffold(sortSheetState = DialogState(isVisible = true))

        composeRule.onNodeWithText(DEFAULT_SORT_SHEET_TITLE).assertExists()
        composeRule.onAllNodesWithText(DEFAULT_NAME_SORT).onLast().assertExists()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-012 최근 수정순을 고르면 그 정렬을 전달하고 선택 목록이 닫힌다`() {
        val eventList = mutableListOf<ContactHomeScaffoldEvent>()
        val sortSheetState = DialogState(isVisible = true)
        setContactHomeScaffold(onEvent = eventList::add, sortSheetState = sortSheetState)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(ContactHomeScaffoldEvent.SelectSort(sort = ListSort.RECENTLY_UPDATED))
        sortSheetState.isVisible shouldBe false
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-013 목록이 비어 있어도 정렬을 고를 수 있다`() {
        val eventList = mutableListOf<ContactHomeScaffoldEvent>()
        setContactHomeScaffold(onEvent = eventList::add)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()

        eventList shouldBe listOf(ContactHomeScaffoldEvent.ClickSort)
    }

    @Test
    fun `정렬 컨트롤은 선택한 정렬의 이름을 표시한다`() {
        setContactHomeScaffold(sort = ListSort.RECENTLY_UPDATED)

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_NAME_SORT).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 연락처 목록의 정렬 이름은 제목순이 아니라 이름순이다`() {
        setContactHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_SORT_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithText(KOREAN_NAME_SORT).assertExists()
        composeRule.onNodeWithText(KOREAN_TITLE_SORT).assertDoesNotExist()
    }

    private fun setContactHomeScaffold(
        onEvent: (ContactHomeScaffoldEvent) -> Unit = {},
        sortSheetState: DialogState = DialogState(),
        sort: ListSort = ListSort.NAME,
    ) {
        val contactPagingDataFlow = MutableStateFlow(contactPagingDataOf(emptyList()))

        composeRule.setContent {
            DiaryTheme {
                ContactHomeScaffold(
                    onEvent = onEvent,
                    contactPagingItems = contactPagingDataFlow.collectAsLazyPagingItems(),
                    sortSheetState = sortSheetState,
                    sortProvider = { sort },
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val KOREAN_SORT_DESCRIPTION = "목록 정렬"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_NAME_SORT = "Name"
        private const val KOREAN_NAME_SORT = "이름순"
        private const val DEFAULT_TITLE_SORT = "Title"
        private const val KOREAN_TITLE_SORT = "제목순"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        private const val DEFAULT_DEFAULT_SORT = "Default"
    }
}
