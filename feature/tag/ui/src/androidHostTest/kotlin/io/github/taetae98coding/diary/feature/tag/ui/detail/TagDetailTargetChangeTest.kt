package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TAG_DETAIL_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TAG_DETAIL_PLACE_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.detail.web.TAG_DETAIL_WEB_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.fixtureId
import io.github.taetae98coding.diary.feature.tag.ui.fixtureText
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagPlace
import io.github.taetae98coding.diary.feature.tag.ui.tagWeb
import io.kotest.matchers.booleans.shouldBeFalse
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 목록과 상세를 함께 쓰는 동안 목록에서 다른 태그를 고르면 같은 화면이 대상만 바꿔 이어진다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailTargetChangeTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val detailIdState = mutableStateOf(FIRST_TAG_ID)

    @Test
    fun `TC-TAG-DETAIL-FEATURE-063 대상이 바뀌면 메모 탭의 정렬 선택과 목록 위치가 처음 상태로 돌아간다`() {
        val titleList = titleList(prefix = "TargetMemo")
        setScreen()
        memoPagingDataFlow.value = tagMemoPagingData(itemList = titleList.map { title -> MemoListItem.Content(memo = tagMemo(title = title)) })
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        chooseSortAndScroll(
            sortFlow = requireNotNull(memoViewModelRef).sort,
            chosenSort = ListSort.TITLE,
            chosenLabel = DEFAULT_TITLE_LABEL,
            listTestTag = TAG_DETAIL_MEMO_LIST_TEST_TAG,
            titleList = titleList,
        )
        changeTarget()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_DEFAULT_LABEL).assertIsDisplayed()
        composeRule.onNodeWithText(titleList.first()).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-063 대상이 바뀌면 웹 탭의 정렬 선택과 목록 위치가 처음 상태로 돌아간다`() {
        val titleList = titleList(prefix = "TargetWeb")
        setScreen()
        webPagingDataFlow.value = tagEntityPagingData(itemList = titleList.map { title -> tagWeb(title = title) })
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        chooseSortAndScroll(
            sortFlow = requireNotNull(webViewModelRef).sort,
            chosenSort = ListSort.RECENTLY_UPDATED,
            chosenLabel = DEFAULT_RECENTLY_UPDATED_LABEL,
            listTestTag = TAG_DETAIL_WEB_LIST_TEST_TAG,
            titleList = titleList,
        )
        changeTarget()
        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_TITLE_LABEL).assertIsDisplayed()
        composeRule.onNodeWithText(titleList.first()).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-063 대상이 바뀌면 장소 탭의 정렬 선택과 목록 위치가 처음 상태로 돌아간다`() {
        val titleList = titleList(prefix = "TargetPlace")
        setScreen()
        placePagingDataFlow.value = tagEntityPagingData(itemList = titleList.map { title -> tagPlace(title = title) })
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        chooseSortAndScroll(
            sortFlow = requireNotNull(placeViewModelRef).sort,
            chosenSort = ListSort.RECENTLY_UPDATED,
            chosenLabel = DEFAULT_RECENTLY_UPDATED_LABEL,
            listTestTag = TAG_DETAIL_PLACE_LIST_TEST_TAG,
            titleList = titleList,
        )
        changeTarget()
        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_TITLE_LABEL).assertIsDisplayed()
        composeRule.onNodeWithText(titleList.first()).assertIsDisplayed()
    }

    // 정렬 선택은 목록의 상태 보관자가 가지므로, 사용자가 정렬을 고른 결과를 그 보관자에 반영한다.
    private fun chooseSortAndScroll(
        sortFlow: Any,
        chosenSort: ListSort,
        chosenLabel: String,
        listTestTag: String,
        titleList: List<String>,
    ) {
        @Suppress("UNCHECKED_CAST")
        val mutableSortFlow = sortFlow as MutableStateFlow<ListSort>
        composeRule.runOnIdle { mutableSortFlow.value = chosenSort }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(chosenLabel).assertIsDisplayed()

        composeRule.onNodeWithTag(listTestTag).performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()
        isDisplayed(titleList.first()).shouldBeFalse()
    }

    private fun changeTarget() {
        val nextId = fixtureId()
        composeRule.runOnIdle { detailIdState.value = nextId }
        composeRule.waitForIdle()
    }

    private fun setScreen() {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            detailIdState = detailIdState,
            viewModelFor = { id -> screenTestViewModel(MutableStateFlow(tagDetailUiState(id = id, detail = tagDetail(TAG_TITLE)))) },
        )
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private companion object {
        const val ITEM_COUNT = 40
        const val SCROLLED_INDEX = 30
        const val DEFAULT_DEFAULT_LABEL = "Default"
        const val DEFAULT_TITLE_LABEL = "Title"
        const val DEFAULT_RECENTLY_UPDATED_LABEL = "Recently updated"
        const val TITLE_PREFIX_LENGTH = 16

        fun titleList(prefix: String): List<String> {
            val titlePrefix = fixtureText(prefix = prefix).take(TITLE_PREFIX_LENGTH)
            return List(ITEM_COUNT) { index -> "${titlePrefix}Index${index.toString().padStart(length = 2, padChar = '0')}" }
        }
    }
}
