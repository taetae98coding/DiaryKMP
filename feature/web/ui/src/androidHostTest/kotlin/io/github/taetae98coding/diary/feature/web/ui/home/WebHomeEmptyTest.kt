package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
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
class WebHomeEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-HOME-FEATURE-010 표시할 웹 항목이 없으면 빈 상태 안내를 표시한다`() {
        setWebHomeScaffold(pagingData = webPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-WEB-HOME-FEATURE-010 한국어 환경에서 빈 상태 안내는 아직 웹이 없습니다이다`() {
        setWebHomeScaffold(pagingData = webPagingDataOf(emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-011 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setWebHomeScaffold(pagingData = loadingWebPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-012 표시할 웹 항목이 있으면 빈 상태 안내를 표시하지 않는다`() {
        setWebHomeScaffold(pagingData = webPagingDataOf(listOf(testWeb(title = WEB_TITLE))))

        composeRule.onNodeWithTag(WEB_CARD_TEST_TAG, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-HOME-DATA-003 목록을 조회하지 못하면 빈 상태 안내를 표시하고 오류를 알리지 않는다`() {
        setWebHomeScaffold(pagingData = failedWebPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithTag(WEB_CARD_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-005 빈 상태에서도 웹 추가를 실행할 수 있다`() {
        setWebHomeScaffold(pagingData = webPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-014 빈 상태에서도 목록을 당겨 새로고침할 수 있다`() {
        val eventList = mutableListOf<WebHomeScaffoldEvent>()
        setWebHomeScaffold(pagingData = webPagingDataOf(emptyList()), onEvent = eventList::add)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(WebHomeScaffoldEvent.Refresh)
    }

    private fun setWebHomeScaffold(
        pagingData: PagingData<Web>,
        onEvent: (WebHomeScaffoldEvent) -> Unit = {},
    ) {
        val webPagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                WebHomeScaffold(
                    onEvent = onEvent,
                    webPagingItems = webPagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_EMPTY_TITLE = "No webs yet"
        private const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a web."
        private const val KOREAN_EMPTY_TITLE = "아직 웹이 없습니다"
        private const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 웹을 만들 수 있습니다"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add web"
        private const val WEB_TITLE = "EmptyStateWebTitle"
    }
}
