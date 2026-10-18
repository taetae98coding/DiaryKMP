package io.github.taetae98coding.diary.feature.tag.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_PLACE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_WEB_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.FIRST_TAG_ID
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScreenTestHost
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceMapViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.prepareTagDetailTabViewModels
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.home.TagHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.tag.ui.home.TagHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.tag.ui.home.TagHomeScreen
import io.github.taetae98coding.diary.feature.tag.ui.home.TagHomeViewModel
import io.github.taetae98coding.diary.feature.tag.ui.home.screenTestSyncViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagHomeDetailShortcutTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var tagAddCount = 0
    private var memoAddCount = 0
    private var webAddCount = 0
    private var placeAddCount = 0

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `목록과 함께 표시한 TagDetail에서 메모·웹·장소 탭을 고르면 Cmd A는 그 탭의 추가만 실행하고 태그 추가는 실행하지 않는다`() {
        setTagHomeWithDetail()

        listOf(
            DEFAULT_MEMO_TAB_DESCRIPTION to { memoAddCount },
            DEFAULT_WEB_TAB_DESCRIPTION to { webAddCount },
            DEFAULT_PLACE_TAB_DESCRIPTION to { placeAddCount },
        ).forEach { (tabDescription, addCount) ->
            composeRule.selectTagDetailTab(tabDescription)
            val before = addCount()

            composeRule.onRoot().performAddShortcut()
            composeRule.waitForIdle()

            addCount() shouldBe before + 1
            tagAddCount shouldBe 0
        }
    }

    @Test
    fun `목록과 함께 표시한 TagDetail이 목록의 추가 단축키보다 늦게 나타나도 메모 탭을 고르면 Cmd A는 메모 추가를 실행한다`() {
        val isDetailVisible = mutableStateOf(false)
        setTagHomeWithDetail(isDetailVisible = isDetailVisible)

        isDetailVisible.value = true
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onRoot().performAddShortcut()
        composeRule.waitForIdle()

        memoAddCount shouldBe 1
        tagAddCount shouldBe 0
    }

    @Test
    fun `목록의 추가 단축키가 TagDetail보다 늦게 켜진 뒤 메모 탭을 고르면 Cmd A는 메모 추가만 실행한다`() {
        val isListAddVisible = mutableStateOf(false)
        setTagHomeWithDetail(isListAddVisible = isListAddVisible)
        isListAddVisible.value = true
        composeRule.waitForIdle()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onRoot().performAddShortcut()
        composeRule.waitForIdle()

        memoAddCount shouldBe 1
        tagAddCount shouldBe 0
    }

    @Test
    fun `목록과 함께 표시한 TagDetail에서 태그 디테일 탭으로 돌아오면 Cmd A로 그 탭의 추가를 실행하지 않는다`() {
        setTagHomeWithDetail()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onRoot().performAddShortcut()
        composeRule.waitForIdle()

        memoAddCount shouldBe 0
        webAddCount shouldBe 0
        placeAddCount shouldBe 0
    }

    private fun setTagHomeWithDetail(
        isDetailVisible: State<Boolean> = mutableStateOf(true),
        isListAddVisible: State<Boolean> = isDetailVisible,
    ) {
        prepareTagDetailTabViewModels()
        val detailViewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE))))
        val placeMapViewModel =
            mockk<TagDetailPlaceMapViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(TagDetailPlaceUiState.Loading)
            }

        composeRule.setContent {
            TagDetailScreenTestHost {
                Row {
                    TagHomeScreen(
                        navigateToAdd = { tagAddCount += 1 },
                        navigateToDetail = {},
                        navigateToFilter = {},
                        navigateToFinishedList = {},
                        navigateToSearch = {},
                        componentVisibleProvider = { TagHomeScaffoldComponentVisible(isAddButtonVisible = isListAddVisible.value) },
                        gridState = rememberLazyGridState(),
                        tagViewModel = tagHomeViewModel(),
                        syncViewModel = screenTestSyncViewModel(),
                        modifier = Modifier.weight(1F).fillMaxHeight(),
                    )
                    if (isDetailVisible.value) {
                        TagDetailScreen(
                            navigateUp = {},
                            navigateToTagAdd = {},
                            navigateToDetail = {},
                            navigateToMemoAdd = { memoAddCount += 1 },
                            navigateToMemoDetail = {},
                            navigateToMemoFinishedList = {},
                            navigateToWebAdd = { webAddCount += 1 },
                            navigateToWebDetail = {},
                            navigateToPlaceAdd = { placeAddCount += 1 },
                            navigateToPlaceDetail = {},
                            id = FIRST_TAG_ID,
                            tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                            componentVisibleProvider = { TagDetailScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                            detailViewModel = detailViewModel,
                            placeMapViewModel = placeMapViewModel,
                            modifier = Modifier.weight(1F).fillMaxHeight(),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun tagHomeViewModel(): TagHomeViewModel {
        val viewModel = mockk<TagHomeViewModel>()
        every { viewModel.sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))
        every { viewModel.tagPagingData } returns MutableStateFlow(PagingData.empty())
        every { viewModel.effect } returns emptyFlow()
        every { viewModel.filterUiState } returns MutableStateFlow(TagHomeScaffoldFilterUiState(isLoaded = true))
        return viewModel
    }
}

private fun SemanticsNodeInteraction.performAddShortcut() {
    performKeyInput {
        keyDown(Key.MetaLeft)
        keyDown(Key.A)
        keyUp(Key.A)
        keyUp(Key.MetaLeft)
    }
}
