package io.github.taetae98coding.diary.feature.tag.ui.detail.qr

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
import io.github.taetae98coding.diary.compose.qr.QR_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityLoadingPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagQr
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailQrTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-735f667d-e3f5-4345-8d75-50bd9d75be44 대상 태그와 연결된 QR을 목록에 표시한다`() {
        setQrTab(pagingData = tagEntityPagingData(itemList = listOf(tagQr(title = FIRST_TITLE), tagQr(title = SECOND_TITLE))))

        composeRule.refreshableList().assertExists()
        composeRule.onAllNodesWithTag(QR_CARD_TEST_TAG).assertCountEquals(2)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-47d4bfa3-5421-437e-a3a8-54024453fc20 최초 조회 실패는 오류와 재시도를 표시하지 않는다`() {
        setQrTab(
            pagingData =
                tagEntityPagingData<Qr>(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException("Refresh failed")),
                ),
        )

        composeRule.onAllNodesWithTag(QR_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-47d4bfa3-5421-437e-a3a8-54024453fc20 추가 조회 실패는 표시된 QR을 유지한다`() {
        setQrTab(
            pagingData =
                tagEntityPagingData(
                    itemList = listOf(tagQr(title = FIRST_TITLE)),
                    append = LoadState.Error(IllegalStateException("Append failed")),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(QR_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-3f21d090-ae88-4965-8026-4ceb4631a8c7 QR을 선택하면 그 QR의 상세 이동을 요청한다`() {
        val item = tagQr(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailQrContentEvent>()
        setQrTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onEvent = eventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(TagDetailQrContentEvent.ClickQr(id = item.id))
    }

    @Test
    fun `TC-1d99ac25-12c6-46e1-a573-545c92cae700 연결된 QR이 없으면 빈 상태 안내를 표시한다`() {
        setQrTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.refreshableList().assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-1d99ac25-12c6-46e1-a573-545c92cae700 한국어 환경에서 빈 상태 안내를 표시한다`() {
        setQrTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-e2cf9f72-76cf-4857-9d54-076249b2637b 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setQrTab(pagingData = tagEntityLoadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-20a6d574-a4b6-464d-87fe-16cfa558c6ce 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailQrContentEvent>()
        setQrTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagQr(title = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailQrContentEvent.Refresh)
    }

    @Test
    fun `TC-20a6d574-a4b6-464d-87fe-16cfa558c6ce 빈 상태에서도 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailQrContentEvent>()
        setQrTab(pagingData = tagEntityPagingData(itemList = emptyList()), onEvent = eventList::add)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailQrContentEvent.Refresh)
    }

    @Test
    fun `TC-20a6d574-a4b6-464d-87fe-16cfa558c6ce 새로고침이 진행 중이면 진행 표시가 나타나고 끝나면 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setQrTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagQr(title = FIRST_TITLE))),
            isRefreshingProvider = { isRefreshing.value },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-e08dea60-9f1d-4018-b2b0-785fd14aa058 QR 카드를 삭제 방향으로 밀면 그 QR의 삭제를 전달한다`() {
        val item = tagQr(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailQrContentEvent>()
        setQrTab(
            pagingData = tagEntityPagingData(itemList = listOf(item)),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(QR_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailQrContentEvent.DeleteQr(id = item.id))
    }

    @Test
    fun `TC-0d8a0ca6-5dbd-49e8-b9d4-a26457b1280b QR 카드를 반대 방향으로 밀면 아무 동작을 요청하지 않는다`() {
        val eventList = mutableListOf<TagDetailQrContentEvent>()
        setQrTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagQr(title = FIRST_TITLE))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(QR_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-5aad1138-20a6-4350-a357-fd44eaef9d72 QR 탭은 정렬 선택을 제공하지 않는다`() {
        setQrTab(pagingData = tagEntityPagingData(itemList = listOf(tagQr(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    private fun setQrTab(
        pagingData: PagingData<Qr> = PagingData.empty(),
        onEvent: (TagDetailQrContentEvent) -> Unit = {},
        isRefreshingProvider: () -> Boolean = { false },
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                TagDetailQrTab(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    qrPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }

    private companion object {
        const val FIRST_TITLE = "AlphaQr"
        const val SECOND_TITLE = "BetaQr"
        const val DEFAULT_EMPTY_TITLE = "No QR codes linked to this tag"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a QR code."
        const val KOREAN_EMPTY_TITLE = "이 태그에 연결된 QR이 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 QR을 만들 수 있습니다"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
    }
}
