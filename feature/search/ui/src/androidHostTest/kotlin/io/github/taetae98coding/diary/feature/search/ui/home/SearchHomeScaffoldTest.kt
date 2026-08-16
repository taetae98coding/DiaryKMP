package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.material3.Text
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SEARCH-HOME-FEATURE-001 진입하면 질의가 비어 있다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).assertExists()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-005 메모 시작 유형이면 메모 유형이 선택된다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_MEMO_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_TAG_TAB_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_PLACE_TAB_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_WEB_TAB_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-005 태그 시작 유형이면 태그 유형이 선택된다`() {
        setSearchHomeScaffold(initialType = SearchHomeType.TAG)

        composeRule.onNodeWithText(DEFAULT_TAG_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_MEMO_TAB_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_PLACE_TAB_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_WEB_TAB_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-005 장소 시작 유형이면 장소 유형이 선택된다`() {
        setSearchHomeScaffold(initialType = SearchHomeType.PLACE)

        composeRule.onNodeWithText(DEFAULT_PLACE_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_MEMO_TAB_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_TAG_TAB_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_WEB_TAB_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-005 웹 시작 유형이면 웹 유형이 선택된다`() {
        setSearchHomeScaffold(initialType = SearchHomeType.WEB)

        composeRule.onNodeWithText(DEFAULT_WEB_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_MEMO_TAB_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_TAG_TAB_LABEL).assertIsNotSelected()
        composeRule.onNodeWithText(DEFAULT_PLACE_TAB_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-006 태그 유형으로 바꾸면 태그 유형을 본다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TAG_TAB_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_TAG_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_MEMO_TAB_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-006 장소 유형으로 바꾸면 장소 유형을 본다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_PLACE_TAB_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PLACE_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_MEMO_TAB_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-006 웹 유형으로 바꾸면 웹 유형을 본다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_WEB_TAB_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_WEB_TAB_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_MEMO_TAB_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-007 유형을 바꿔도 질의가 유지된다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.onNodeWithText(DEFAULT_TAG_TAB_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(QUERY).assertExists()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-014 뒤로가기 버튼을 선택하면 뒤로가기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<SearchHomeScaffoldEvent>()
        setSearchHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(SearchHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `지우기 버튼을 누르면 검색어를 지우고 초점을 검색어 입력에 남긴다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).assertExists()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).assertIsFocused()
    }

    @Test
    fun `검색어가 없으면 지우기 버튼을 두지 않는다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `유형 탭을 메모, 태그, 장소, 웹 순서로 표시한다`() {
        setSearchHomeScaffold()

        val leftList =
            listOf(DEFAULT_MEMO_TAB_LABEL, DEFAULT_TAG_TAB_LABEL, DEFAULT_PLACE_TAB_LABEL, DEFAULT_WEB_TAB_LABEL)
                .map { label -> composeRule.onNodeWithText(label).getUnclippedBoundsInRoot().left }

        leftList.zipWithNext().forEach { (left, next) -> left shouldBeLessThan next }
    }

    @Test
    fun `기본 환경에서 유형 이름과 자리 표시 문구를 표시한다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).assertExists()
        composeRule.onNodeWithText(DEFAULT_MEMO_TAB_LABEL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_TAG_TAB_LABEL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_PLACE_TAB_LABEL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_WEB_TAB_LABEL).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_TAB_ROW_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 유형 이름과 자리 표시 문구를 표시한다`() {
        setSearchHomeScaffold()

        composeRule.onNodeWithText("검색어를 입력하세요").assertExists()
        composeRule.onNodeWithText("메모").assert(hasClickAction())
        composeRule.onNodeWithText("태그").assert(hasClickAction())
        composeRule.onNodeWithText("장소").assert(hasClickAction())
        composeRule.onNodeWithText("웹").assert(hasClickAction())
        composeRule.onNodeWithContentDescription("검색 결과 유형").assertExists()
    }

    private fun setSearchHomeScaffold(
        initialType: SearchHomeType = SearchHomeType.MEMO,
        onEvent: (SearchHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SearchHomeScaffold(
                    onEvent = onEvent,
                    state = rememberSearchHomeScaffoldState(initialType = initialType),
                ) { type ->
                    Text(text = type.name)
                }
            }
        }
    }

    public companion object {
        private const val QUERY = "여행"
        private const val DEFAULT_QUERY_PLACEHOLDER = "Enter a search query"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_CLEAR_DESCRIPTION = "Clear text"
        private const val DEFAULT_TAB_ROW_DESCRIPTION = "Search result type"
        private const val DEFAULT_MEMO_TAB_LABEL = "Memo"
        private const val DEFAULT_TAG_TAB_LABEL = "Tag"
        private const val DEFAULT_PLACE_TAB_LABEL = "Place"
        private const val DEFAULT_WEB_TAB_LABEL = "Web"
    }
}
