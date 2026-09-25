package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.memo.MEMO_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.TAG_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.compose.web.WEB_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SEARCH_HOME_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoEffect
import io.github.taetae98coding.diary.feature.search.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SearchHomeSwipeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-030 메모 카드를 시작 방향으로 밀면 완료 여부에 맞는 동작을 요청하고 기본 안내를 표시한다`() {
        val unfinished = resultMemo()
        val finished = resultMemo(isFinished = true)
        composeRule.setSearchHomeScreen(memoList = listOf(unfinished, finished))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = unfinished.detail.title, isRight = true)

        verify(exactly = 1) { memoViewModel().finish(id = unfinished.id) }
        composeRule.onNodeWithText(DEFAULT_MEMO_FINISHED).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertIsDisplayed()

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = finished.detail.title, isRight = true)

        verify(exactly = 1) { memoViewModel().restart(id = finished.id) }
        verify(exactly = 0) { memoViewModel().finish(id = finished.id) }
        composeRule.onNodeWithText(DEFAULT_MEMO_RESTARTED).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SEARCH-HOME-FEATURE-030 한국어 환경에서 메모 완료와 다시 시작 안내를 표시한다`() {
        val unfinished = resultMemo()
        val finished = resultMemo(isFinished = true)
        composeRule.setSearchHomeScreen(memoList = listOf(unfinished, finished))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = unfinished.detail.title, isRight = true)

        composeRule.onNodeWithText(KOREAN_MEMO_FINISHED).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO).assertIsDisplayed()

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = finished.detail.title, isRight = true)

        composeRule.onNodeWithText(KOREAN_MEMO_RESTARTED).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-031 태그 카드를 시작 방향으로 밀면 완료 여부에 맞는 동작을 요청하고 기본 안내를 표시한다`() {
        val unfinished = resultTag()
        val finished = resultTag(isFinished = true)
        composeRule.setSearchHomeScreen(tagList = listOf(unfinished, finished))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = unfinished.detail.title, isRight = true)

        verify(exactly = 1) { tagViewModel().finish(id = unfinished.id) }
        composeRule.onNodeWithText(DEFAULT_TAG_FINISHED).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertIsDisplayed()

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = finished.detail.title, isRight = true)

        verify(exactly = 1) { tagViewModel().restart(id = finished.id) }
        verify(exactly = 0) { tagViewModel().finish(id = finished.id) }
        composeRule.onNodeWithText(DEFAULT_TAG_RESTARTED).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SEARCH-HOME-FEATURE-031 한국어 환경에서 태그 완료와 다시 시작 안내를 표시한다`() {
        val unfinished = resultTag()
        val finished = resultTag(isFinished = true)
        composeRule.setSearchHomeScreen(tagList = listOf(unfinished, finished))
        composeRule.selectSearchHomeTab(KOREAN_TAG_TAB_LABEL)

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = unfinished.detail.title, isRight = true)

        composeRule.onNodeWithText(KOREAN_TAG_FINISHED).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO).assertIsDisplayed()

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = finished.detail.title, isRight = true)

        composeRule.onNodeWithText(KOREAN_TAG_RESTARTED).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-032 네 유형의 결과 카드를 삭제 방향으로 밀면 삭제를 요청하고 기본 안내를 표시한다`() {
        val memo = resultMemo()
        val tag = resultTag()
        val place = resultPlace()
        val web = resultWeb()
        composeRule.setSearchHomeScreen(memoList = listOf(memo), tagList = listOf(tag), placeList = listOf(place), webList = listOf(web))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = false)
        verify(exactly = 1) { memoViewModel().delete(id = memo.id) }
        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertIsDisplayed()

        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)
        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = false)
        verify(exactly = 1) { tagViewModel().delete(id = tag.id) }
        composeRule.onNodeWithText(DEFAULT_TAG_DELETED).assertIsDisplayed()

        composeRule.selectSearchHomeTab(PLACE_TAB_LABEL)
        swipeCard(testTag = PLACE_CARD_TEST_TAG, title = place.detail.title, isRight = false)
        verify(exactly = 1) { placeViewModel().delete(id = place.id) }
        composeRule.onNodeWithText(DEFAULT_PLACE_DELETED).assertIsDisplayed()

        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)
        swipeCard(testTag = WEB_CARD_TEST_TAG, title = web.detail.title, isRight = false)
        verify(exactly = 1) { webViewModel().delete(id = web.id) }
        composeRule.onNodeWithText(DEFAULT_WEB_DELETED).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SEARCH-HOME-FEATURE-032 한국어 환경에서 네 유형의 삭제 안내를 표시한다`() {
        val memo = resultMemo()
        val tag = resultTag()
        val place = resultPlace()
        val web = resultWeb()
        composeRule.setSearchHomeScreen(memoList = listOf(memo), tagList = listOf(tag), placeList = listOf(place), webList = listOf(web))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = false)
        composeRule.onNodeWithText(KOREAN_MEMO_DELETED).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO).assertIsDisplayed()

        composeRule.selectSearchHomeTab(KOREAN_TAG_TAB_LABEL)
        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = false)
        composeRule.onNodeWithText(KOREAN_TAG_DELETED).assertIsDisplayed()

        composeRule.selectSearchHomeTab(KOREAN_PLACE_TAB_LABEL)
        swipeCard(testTag = PLACE_CARD_TEST_TAG, title = place.detail.title, isRight = false)
        composeRule.onNodeWithText(KOREAN_PLACE_DELETED).assertIsDisplayed()

        composeRule.selectSearchHomeTab(KOREAN_WEB_TAB_LABEL)
        swipeCard(testTag = WEB_CARD_TEST_TAG, title = web.detail.title, isRight = false)
        composeRule.onNodeWithText(KOREAN_WEB_DELETED).assertIsDisplayed()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-033 메모 완료를 실행 취소하면 그 완료를 되돌리는 요청을 보낸다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = true)
        clickUndo()

        verify(exactly = 1) { memoViewModel().undo(effect = SearchHomeMemoEffect.Finished(id = memo.id)) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-033 메모 다시 시작을 실행 취소하면 그 다시 시작을 되돌리는 요청을 보낸다`() {
        val memo = resultMemo(isFinished = true)
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = true)
        clickUndo()

        verify(exactly = 1) { memoViewModel().undo(effect = SearchHomeMemoEffect.Restarted(id = memo.id)) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-033 메모 삭제를 실행 취소하면 그 삭제를 되돌리는 요청을 보낸다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = false)
        clickUndo()

        verify(exactly = 1) { memoViewModel().undo(effect = SearchHomeMemoEffect.Deleted(id = memo.id)) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-033 태그 완료를 실행 취소하면 그 완료를 되돌리는 요청을 보낸다`() {
        val tag = resultTag()
        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = true)
        clickUndo()

        verify(exactly = 1) { tagViewModel().undo(effect = TagListEffect.Finished(id = tag.id)) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-033 태그 다시 시작을 실행 취소하면 그 다시 시작을 되돌리는 요청을 보낸다`() {
        val tag = resultTag(isFinished = true)
        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = true)
        clickUndo()

        verify(exactly = 1) { tagViewModel().undo(effect = TagListEffect.Restarted(id = tag.id)) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-033 태그 삭제를 실행 취소하면 그 삭제를 되돌리는 요청을 보낸다`() {
        val tag = resultTag()
        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = false)
        clickUndo()

        verify(exactly = 1) { tagViewModel().undo(effect = TagListEffect.Deleted(id = tag.id)) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-033 장소와 웹 항목의 삭제를 실행 취소하면 그 삭제를 되돌리는 요청을 보낸다`() {
        val place = resultPlace()
        val web = resultWeb()
        composeRule.setSearchHomeScreen(placeList = listOf(place), webList = listOf(web))

        composeRule.selectSearchHomeTab(PLACE_TAB_LABEL)
        swipeCard(testTag = PLACE_CARD_TEST_TAG, title = place.detail.title, isRight = false)
        clickUndo()
        verify(exactly = 1) { placeViewModel().restore(id = place.id) }

        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)
        swipeCard(testTag = WEB_CARD_TEST_TAG, title = web.detail.title, isRight = false)
        clickUndo()
        verify(exactly = 1) { webViewModel().restore(id = web.id) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-034 장소와 웹 카드를 시작 방향으로 밀면 삭제를 요청하지 않고 안내도 표시하지 않는다`() {
        val place = resultPlace()
        val web = resultWeb()
        composeRule.setSearchHomeScreen(placeList = listOf(place), webList = listOf(web))

        composeRule.selectSearchHomeTab(PLACE_TAB_LABEL)
        swipeCard(testTag = PLACE_CARD_TEST_TAG, title = place.detail.title, isRight = true)
        verify(exactly = 0) { placeViewModel().delete(id = any()) }
        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()

        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)
        swipeCard(testTag = WEB_CARD_TEST_TAG, title = web.detail.title, isRight = true)
        verify(exactly = 0) { webViewModel().delete(id = any()) }
        composeRule.onNodeWithText(web.detail.title).assertIsDisplayed()

        composeRule.onNodeWithText(DEFAULT_UNDO).assertDoesNotExist()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-037 안내가 보이는 동안 다른 메모가 삭제되면 마지막 삭제에만 실행 취소가 적용된다`() {
        val first = resultMemo()
        val second = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(first, second))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = first.detail.title, isRight = false)
        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertIsDisplayed()

        sendSearchMemoEffect(SearchHomeMemoEffect.Deleted(id = second.id))
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(DEFAULT_MEMO_DELETED).fetchSemanticsNodes().size shouldBe 1
        clickUndo()

        verify(exactly = 1) { memoViewModel().undo(effect = SearchHomeMemoEffect.Deleted(id = second.id)) }
        verify(exactly = 0) { memoViewModel().undo(effect = SearchHomeMemoEffect.Deleted(id = first.id)) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-038 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = false)
        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertDoesNotExist()
        verify(exactly = 0) { memoViewModel().undo(effect = any()) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-039 안내가 보이는 동안 다른 유형으로 바꾸면 안내가 닫히고 되돌릴 수 없다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = false)
        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertIsDisplayed()

        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)
        composeRule.selectSearchHomeTab(MEMO_TAB_LABEL)

        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertDoesNotExist()
        verify(exactly = 0) { memoViewModel().undo(effect = any()) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-040 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        var isShown by mutableStateOf(true)
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo), isShownProvider = { isShown })

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = false)
        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertIsDisplayed()
        val viewModel = memoViewModel()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertDoesNotExist()
        verify(exactly = 0) { viewModel.undo(effect = any()) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-041 안내가 보이는 동안 질의를 고쳐도 안내가 남고 실행 취소할 수 있다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = false)
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertIsDisplayed()
        clickUndo()

        verify(exactly = 1) { memoViewModel().undo(effect = SearchHomeMemoEffect.Deleted(id = memo.id)) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-041 안내가 보이는 동안 정렬을 바꿔도 안내가 남고 실행 취소할 수 있다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = false)
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitUntil(timeoutMillis = SORT_SHEET_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_SORT_SHEET_TITLE).fetchSemanticsNodes().isEmpty()
        }

        verify(exactly = 1) { memoViewModel().select(sort = ListSort.RECENTLY_UPDATED) }
        composeRule.onNodeWithText(DEFAULT_MEMO_DELETED).assertIsDisplayed()
        clickUndo()

        verify(exactly = 1) { memoViewModel().undo(effect = SearchHomeMemoEffect.Deleted(id = memo.id)) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-042 메모 카드 위에서 오른쪽으로 밀면 유형은 바뀌지 않고 그 메모의 완료를 요청한다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = true)

        verify(exactly = 1) { memoViewModel().finish(id = memo.id) }
        composeRule.onNodeWithText(MEMO_TAB_LABEL).assertIsSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-042 태그 카드 위에서 왼쪽으로 밀면 유형은 바뀌지 않고 그 태그의 삭제를 요청한다`() {
        val tag = resultTag()
        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = false)

        verify(exactly = 1) { tagViewModel().delete(id = tag.id) }
        composeRule.onNodeWithText(TAG_TAB_LABEL).assertIsSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-042 웹 카드 위에서 오른쪽으로 밀면 유형은 바뀌지 않고 아무 동작도 요청하지 않는다`() {
        val web = resultWeb()
        composeRule.setSearchHomeScreen(webList = listOf(web))
        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)

        swipeCard(testTag = WEB_CARD_TEST_TAG, title = web.detail.title, isRight = true)

        verify(exactly = 0) { webViewModel().delete(id = any()) }
        verify(exactly = 0) { webViewModel().restore(id = any()) }
        composeRule.onNodeWithText(WEB_TAB_LABEL).assertIsSelected()
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-043 카드 밖 여백에서 왼쪽으로 밀면 옆 유형으로 바뀐다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        // 카드는 목록 맨 위에 하나만 있으므로 목록 아래쪽 여백을 가로질러 민다.
        composeRule.onNodeWithTag(SEARCH_HOME_MEMO_LIST_TEST_TAG).performTouchInput {
            val y = top + height * EMPTY_AREA_Y_FRACTION
            swipe(start = Offset(x = right - EDGE_PADDING, y = y), end = Offset(x = left + EDGE_PADDING, y = y))
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TAG_TAB_LABEL).assertIsSelected()
        verify(exactly = 0) { memoViewModel().finish(id = any()) }
        verify(exactly = 0) { memoViewModel().delete(id = any()) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-044 완료한 메모가 결과에 남으면 그 카드는 다시 시작을 요청한다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = true)
        verify(exactly = 1) { memoViewModel().finish(id = memo.id) }
        updateMemoResultAndAssertCardShown(memo.copy(isFinished = true))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = true)

        verify(exactly = 1) { memoViewModel().restart(id = memo.id) }
        verify(exactly = 1) { memoViewModel().finish(id = memo.id) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-044 다시 시작한 메모가 결과에 남으면 그 카드는 완료를 요청한다`() {
        val memo = resultMemo(isFinished = true)
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = true)
        verify(exactly = 1) { memoViewModel().restart(id = memo.id) }
        updateMemoResultAndAssertCardShown(memo.copy(isFinished = false))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = true)

        verify(exactly = 1) { memoViewModel().finish(id = memo.id) }
        verify(exactly = 1) { memoViewModel().restart(id = memo.id) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-044 완료를 실행 취소한 메모가 결과에 남으면 그 카드는 다시 완료를 요청한다`() {
        val memo = resultMemo()
        composeRule.setSearchHomeScreen(memoList = listOf(memo))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = true)
        updateMemoResultAndAssertCardShown(memo.copy(isFinished = true))
        clickUndo()
        verify(exactly = 1) { memoViewModel().undo(effect = SearchHomeMemoEffect.Finished(id = memo.id)) }
        updateMemoResultAndAssertCardShown(memo.copy(isFinished = false))

        swipeCard(testTag = MEMO_CARD_TEST_TAG, title = memo.detail.title, isRight = true)

        verify(exactly = 2) { memoViewModel().finish(id = memo.id) }
        verify(exactly = 0) { memoViewModel().restart(id = memo.id) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-044 완료한 태그가 결과에 남으면 그 카드는 다시 시작을 요청한다`() {
        val tag = resultTag()
        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = true)
        verify(exactly = 1) { tagViewModel().finish(id = tag.id) }
        updateTagResultAndAssertCardShown(tag.copy(isFinished = true))

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = true)

        verify(exactly = 1) { tagViewModel().restart(id = tag.id) }
        verify(exactly = 1) { tagViewModel().finish(id = tag.id) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-044 다시 시작한 태그가 결과에 남으면 그 카드는 완료를 요청한다`() {
        val tag = resultTag(isFinished = true)
        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = true)
        verify(exactly = 1) { tagViewModel().restart(id = tag.id) }
        updateTagResultAndAssertCardShown(tag.copy(isFinished = false))

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = true)

        verify(exactly = 1) { tagViewModel().finish(id = tag.id) }
        verify(exactly = 1) { tagViewModel().restart(id = tag.id) }
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-044 완료를 실행 취소한 태그가 결과에 남으면 그 카드는 다시 완료를 요청한다`() {
        val tag = resultTag()
        composeRule.setSearchHomeScreen(tagList = listOf(tag))
        composeRule.selectSearchHomeTab(TAG_TAB_LABEL)

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = true)
        updateTagResultAndAssertCardShown(tag.copy(isFinished = true))
        clickUndo()
        verify(exactly = 1) { tagViewModel().undo(effect = TagListEffect.Finished(id = tag.id)) }
        updateTagResultAndAssertCardShown(tag.copy(isFinished = false))

        swipeCard(testTag = TAG_CARD_TEST_TAG, title = tag.detail.title, isRight = true)

        verify(exactly = 2) { tagViewModel().finish(id = tag.id) }
        verify(exactly = 0) { tagViewModel().restart(id = tag.id) }
    }

    private fun updateMemoResultAndAssertCardShown(memo: Memo) {
        updateSearchMemoResult(listOf(memo))
        composeRule.waitForIdle()
        composeRule.onNode(hasTestTag(MEMO_CARD_TEST_TAG) and hasText(memo.detail.title)).assertIsDisplayed()
    }

    private fun updateTagResultAndAssertCardShown(tag: Tag) {
        updateSearchTagResult(listOf(tag))
        composeRule.waitForIdle()
        composeRule.onNode(hasTestTag(TAG_CARD_TEST_TAG) and hasText(tag.detail.title)).assertIsDisplayed()
    }

    private fun swipeCard(
        testTag: String,
        title: String,
        isRight: Boolean,
    ) {
        composeRule.onNode(hasTestTag(testTag) and hasText(title)).performTouchInput {
            if (isRight) swipeRight() else swipeLeft()
        }
        composeRule.waitForIdle()
    }

    private fun clickUndo() {
        composeRule.onNodeWithText(DEFAULT_UNDO).performClick()
        composeRule.waitForIdle()
    }

    private fun memoViewModel() = requireNotNull(searchMemoViewModelRef)

    private fun tagViewModel() = requireNotNull(searchTagViewModelRef)

    private fun placeViewModel() = requireNotNull(searchPlaceViewModelRef)

    private fun webViewModel() = requireNotNull(searchWebViewModelRef)

    private companion object {
        const val QUERY = "여행"
        const val DEFAULT_UNDO = "Undo"
        const val KOREAN_UNDO = "실행 취소"
        const val DEFAULT_MEMO_FINISHED = "Memo finished."
        const val DEFAULT_MEMO_RESTARTED = "Memo restarted."
        const val DEFAULT_MEMO_DELETED = "Memo deleted."
        const val KOREAN_MEMO_FINISHED = "메모가 완료되었습니다."
        const val KOREAN_MEMO_RESTARTED = "메모를 다시 시작했습니다."
        const val KOREAN_MEMO_DELETED = "메모가 삭제되었습니다."
        const val DEFAULT_TAG_FINISHED = "Tag finished."
        const val DEFAULT_TAG_RESTARTED = "Tag restarted."
        const val DEFAULT_TAG_DELETED = "Tag deleted."
        const val KOREAN_TAG_FINISHED = "태그가 완료되었습니다."
        const val KOREAN_TAG_RESTARTED = "태그를 다시 시작했습니다."
        const val KOREAN_TAG_DELETED = "태그가 삭제되었습니다."
        const val DEFAULT_PLACE_DELETED = "Place deleted."
        const val KOREAN_PLACE_DELETED = "장소가 삭제되었습니다."
        const val DEFAULT_WEB_DELETED = "Web deleted."
        const val KOREAN_WEB_DELETED = "웹이 삭제되었습니다."
        const val KOREAN_TAG_TAB_LABEL = "태그"
        const val KOREAN_PLACE_TAB_LABEL = "장소"
        const val KOREAN_WEB_TAB_LABEL = "웹"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val EMPTY_AREA_Y_FRACTION = 0.6f
        const val EDGE_PADDING = 8f
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
        const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"
        const val SORT_SHEET_TIMEOUT_MILLIS = 5_000L
    }
}
