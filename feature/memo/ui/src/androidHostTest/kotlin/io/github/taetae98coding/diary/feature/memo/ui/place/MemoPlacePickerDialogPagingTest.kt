package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asPagingSourceFactory
import io.github.taetae98coding.diary.compose.core.dialog.DIARY_PAGING_PICKER_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 기기에 저장된 장소를 한 번에 PAGE_SIZE씩 나누어 불러오는 목록을 실제 페이지 조회로 구성한다.
// 첫 조회는 앞쪽 한 페이지만 불러오고 나머지 자리는 아직 준비되지 않은 자리로 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoPlacePickerDialogPagingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-74f38916-1af8-4f2f-9f17-148fe33177b5 목록을 내려 보면 아직 준비되지 않은 구간의 장소가 이어서 나타난다`() {
        val placeList = List(PLACE_COUNT) { index -> testPlace(title = "PagedPlace${index.toString().padStart(length = 3, padChar = '0')}") }
        composeRule.setMemoPlacePickerDialog(
            placePagingData =
                Pager(
                    config =
                        PagingConfig(
                            pageSize = PAGE_SIZE,
                            initialLoadSize = PAGE_SIZE,
                            prefetchDistance = 1,
                            enablePlaceholders = true,
                        ),
                    pagingSourceFactory = placeList.asPagingSourceFactory(),
                ).flow,
        )
        composeRule.waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) { isDisplayed(placeList.first().detail.title) }
        composeRule.placeDialogNodeWithText(placeList.last().detail.title).assertDoesNotExist()

        // 준비되지 않은 자리가 채워지면 목록의 끝이 다시 밀리므로, 사용자가 끝에 닿을 때까지 이어서 내리는 것처럼 끝으로 다시 이동한다.
        composeRule.waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) {
            composeRule.onNode(hasTestTag(DIARY_PAGING_PICKER_LIST_TEST_TAG)).performScrollToIndex(placeList.lastIndex)
            composeRule.waitForIdle()
            isDisplayed(placeList.last().detail.title)
        }

        composeRule.placeDialogNodeWithText(placeList.last().detail.title).assertIsDisplayed()
        composeRule.placeDialogNodeWithText(placeList[placeList.lastIndex - 1].detail.title).assertIsDisplayed()
    }

    private fun isDisplayed(title: String): Boolean =
        runCatching { composeRule.placeDialogNodeWithText(title).assertIsDisplayed() }
            .isSuccess

    private companion object {
        private const val PAGE_SIZE = 20
        private const val PLACE_COUNT = PAGE_SIZE * 3
        private const val LOAD_TIMEOUT_MILLIS = 5_000L
    }
}
