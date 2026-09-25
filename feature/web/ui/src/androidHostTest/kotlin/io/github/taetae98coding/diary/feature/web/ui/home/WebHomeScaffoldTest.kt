package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
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
class WebHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-HOME-FEATURE-001 목록에 웹 항목의 제목과 URL을 카드로 표시한다`() {
        val first = testWeb(title = FIRST_TITLE, url = FIRST_URL)
        val second = testWeb(title = SECOND_TITLE, url = SECOND_URL)

        setWebHomeScaffold(webList = listOf(first, second))

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(FIRST_URL).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_URL).assertExists()
        composeRule.onAllNodesWithTag(WEB_CARD_TEST_TAG).assertCountEquals(2)
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-009 설명이 있는 웹 항목도 목록에 설명을 표시하지 않는다`() {
        val described = testWeb(title = FIRST_TITLE, url = FIRST_URL, description = DESCRIPTION)
        val blankDescribed = testWeb(title = SECOND_TITLE, url = SECOND_URL, description = "")

        setWebHomeScaffold(webList = listOf(described, blankDescribed))

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertExists()
        composeRule.onNodeWithText(DESCRIPTION).assertDoesNotExist()
    }

    // 저장 전후의 목록을 각각 구성해 확인한다. 화면을 꾸민 뒤의 전환은 WebHomeListUpdateTest가 확인한다.
    @Test
    fun `TC-WEB-HOME-FEATURE-004 저장 전 목록에는 새 웹 항목이 없다`() {
        setWebHomeScaffold(webList = listOf(testWeb(title = FIRST_TITLE, url = FIRST_URL)))

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-004 저장 뒤 목록에는 새 웹 항목이 있다`() {
        setWebHomeScaffold(
            webList =
                listOf(
                    testWeb(title = FIRST_TITLE, url = FIRST_URL),
                    testWeb(title = SECOND_TITLE, url = SECOND_URL),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertExists()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-017 웹 카드를 선택하면 그 웹 항목의 선택을 한 번 전달한다`() {
        val web = testWeb(title = FIRST_TITLE, url = FIRST_URL)
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        setWebHomeScaffold(webList = listOf(web), onEvent = eventList::add)

        composeRule.onAllNodesWithTag(WEB_CARD_TEST_TAG).onFirst().performClick()

        eventList shouldBe listOf(WebHomeScaffoldEvent.ClickWeb(web.id))
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-005 목록에 웹 항목이 있어도 웹 추가를 선택할 수 있다`() {
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        setWebHomeScaffold(webList = listOf(testWeb(title = FIRST_TITLE, url = FIRST_URL)), onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebHomeScaffoldEvent.ClickAdd)
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-005 목록이 비어 있어도 웹 추가를 선택할 수 있다`() {
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        setWebHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebHomeScaffoldEvent.ClickAdd)
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-016 목록에 웹 항목이 있어도 검색을 선택할 수 있다`() {
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        setWebHomeScaffold(webList = listOf(testWeb(title = FIRST_TITLE, url = FIRST_URL)), onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebHomeScaffoldEvent.ClickSearch)
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-016 목록이 비어 있어도 검색을 선택할 수 있다`() {
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        setWebHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebHomeScaffoldEvent.ClickSearch)
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-008 뒤로가기를 선택하면 돌아가기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        setWebHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(WebHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    @Config(qualifiers = "w1000dp-h1000dp")
    fun `넓은 화면에서도 웹 카드는 두 열로 배치된다`() {
        val webList =
            List(GRID_WEB_COUNT) { index ->
                testWeb(title = "$FIRST_TITLE-$index", url = "$FIRST_URL/$index")
            }

        setWebHomeScaffold(webList = webList)

        val cardBounds =
            composeRule
                .onAllNodesWithTag(WEB_CARD_TEST_TAG)
                .fetchSemanticsNodes()
                .map { node -> node.boundsInRoot }
        cardBounds.size shouldBe GRID_WEB_COUNT
        cardBounds[0].top shouldBe cardBounds[1].top
        cardBounds[0].left shouldBe cardBounds[2].left
        (cardBounds[1].left > cardBounds[0].left) shouldBe true
        (cardBounds[2].top > cardBounds[0].top) shouldBe true
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 상단 바 제목은 웹이고 추가 버튼 이름은 웹 추가이다`() {
        setWebHomeScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(KOREAN_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 환경에서 상단 바 제목은 Web이고 추가 버튼 이름은 Add web이다`() {
        setWebHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-WEB-LIST-DETAIL-FEATURE-003 상세 영역에 웹 추가가 놓이면 웹 추가 버튼이 표시되지 않는다`() {
        setWebHomeScaffold(componentVisible = WebHomeScaffoldComponentVisible(isAddButtonVisible = false))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-LIST-DETAIL-FEATURE-004 추가 버튼 표시 상태이면 웹 추가 버튼이 표시된다`() {
        setWebHomeScaffold(componentVisible = WebHomeScaffoldComponentVisible(isAddButtonVisible = true))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-WEB-LIST-DETAIL-FEATURE-007 상세 영역에 웹 추가가 놓여도 검색 버튼은 표시된다`() {
        setWebHomeScaffold(componentVisible = WebHomeScaffoldComponentVisible(isAddButtonVisible = false))

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    private fun setWebHomeScaffold(
        webList: List<Web> = emptyList(),
        onEvent: (WebHomeScaffoldEvent) -> Unit = {},
        componentVisible: WebHomeScaffoldComponentVisible = WebHomeScaffoldComponentVisible(),
    ) {
        val webPagingDataFlow = MutableStateFlow(webPagingDataOf(webList))

        composeRule.setContent {
            DiaryTheme {
                WebHomeScaffold(
                    onEvent = onEvent,
                    webPagingItems = webPagingDataFlow.collectAsLazyPagingItems(),
                    componentVisibleProvider = { componentVisible },
                )
            }
        }
    }

    private companion object {
        private const val FIRST_TITLE = "FirstWebTitle"
        private const val SECOND_TITLE = "SecondWebTitle"
        private const val FIRST_URL = "https://first.example.com"
        private const val SECOND_URL = "https://second.example.com"
        private const val DESCRIPTION = "WebDescription"
        private const val GRID_WEB_COUNT = 3
        private const val DEFAULT_TITLE = "Web"
        private const val KOREAN_TITLE = "웹"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add web"
        private const val KOREAN_ADD_BUTTON_DESCRIPTION = "웹 추가"
        private const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search"
        private const val KOREAN_SEARCH_BUTTON_DESCRIPTION = "검색"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
    }
}
