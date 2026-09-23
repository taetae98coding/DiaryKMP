package io.github.taetae98coding.diary.feature.tag.ui.detail.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityLoadingPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagWeb
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailWebTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-001 대상 태그와 연결된 웹 항목을 목록에 표시한다`() {
        val firstWeb = tagWeb(title = FIRST_TITLE)
        val secondWeb = tagWeb(title = SECOND_TITLE)
        setWebTab(pagingData = tagEntityPagingData(itemList = listOf(firstWeb, secondWeb)))

        composeRule.onNodeWithTag(TAG_DETAIL_WEB_LIST_TEST_TAG).assertExists()
        composeRule.onAllNodesWithTag(WEB_CARD_TEST_TAG).assertCountEquals(2)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-004 최초 조회 실패는 오류와 재시도를 표시하지 않는다`() {
        setWebTab(
            pagingData =
                tagEntityPagingData<Web>(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException("Refresh failed")),
                ),
        )

        composeRule.onAllNodesWithTag(WEB_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-004 추가 조회 실패는 표시된 웹 항목을 유지한다`() {
        setWebTab(
            pagingData =
                tagEntityPagingData(
                    itemList = listOf(tagWeb(title = FIRST_TITLE)),
                    append = LoadState.Error(IllegalStateException("Append failed")),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(WEB_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-005 웹 항목을 선택하면 그 웹 항목의 상세 이동을 요청한다`() {
        val web = tagWeb(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailWebContentEvent>()
        setWebTab(pagingData = tagEntityPagingData(itemList = listOf(web)), onEvent = eventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(TagDetailWebContentEvent.ClickWeb(id = web.id))
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-010 연결된 웹 항목이 없으면 빈 상태 안내를 표시한다`() {
        setWebTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.onNodeWithTag(TAG_DETAIL_WEB_LIST_TEST_TAG).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-WEB-FEATURE-010 한국어 환경에서 빈 상태 안내는 이 태그에 연결된 웹이 없습니다이다`() {
        setWebTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-011 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setWebTab(pagingData = tagEntityLoadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-016 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailWebContentEvent>()
        setWebTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagWeb(title = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(TAG_DETAIL_WEB_LIST_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailWebContentEvent.Refresh)
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-016 빈 상태에서도 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailWebContentEvent>()
        setWebTab(pagingData = tagEntityPagingData(itemList = emptyList()), onEvent = eventList::add)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailWebContentEvent.Refresh)
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-016 새로고침이 진행 중이면 진행 표시가 나타나고 끝나면 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setWebTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagWeb(title = FIRST_TITLE))),
            isRefreshingProvider = { isRefreshing.value },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-WEB-FEATURE-017 목록의 웹 항목을 좌우로 밀어도 아무 동작을 요청하지 않는다`() {
        val eventList = mutableListOf<TagDetailWebContentEvent>()
        setWebTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagWeb(title = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(FIRST_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(FIRST_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.none { event -> event is TagDetailWebContentEvent.Refresh } shouldBe true
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
    }

    private fun setWebTab(
        pagingData: PagingData<Web> = PagingData.empty(),
        onEvent: (TagDetailWebContentEvent) -> Unit = {},
        isRefreshingProvider: () -> Boolean = { false },
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagDetailWebTab(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    webPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }

    private companion object {
        const val FIRST_TITLE = "AlphaWeb"
        const val SECOND_TITLE = "BetaWeb"
        const val DEFAULT_EMPTY_TITLE = "No webs linked to this tag"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a web."
        const val KOREAN_EMPTY_TITLE = "이 태그에 연결된 웹이 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 웹을 만들 수 있습니다"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
