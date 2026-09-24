package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.add.webTestTag
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageViewModel
import io.github.taetae98coding.diary.feature.web.ui.sendTagAddedResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebDetailTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-DETAIL-FEATURE-032 조회 중에는 태그 입력을 표시하지 않는다`() {
        setWebDetailScreen(uiState = WebDetailUiState.Loading, selectFormTab = false)

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-032 조회가 끝나면 태그 입력을 표시한다`() {
        setWebDetailScreen()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-034 태그를 연결해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        val selectableTag = webTestTag(title = SELECTABLE_TAG_TITLE)
        setWebDetailScreen(selectableTagList = listOf(selectableTag))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-035 태그를 연결해도 웹 페이지를 다시 불러오지 않는다`() {
        val pageViewModel = pageViewModel(pageUiState = WebDetailPageUiState.Content(page = testWebPage()))
        setWebDetailScreen(
            pageViewModel = pageViewModel,
            selectableTagList = listOf(webTestTag(title = SELECTABLE_TAG_TITLE)),
            selectFormTab = false,
        )
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.selectFormTab()
        verify(exactly = 1) { pageViewModel.load() }

        linkTag(title = SELECTABLE_TAG_TITLE)

        verify(exactly = 1) { pageViewModel.load() }
        verify(exactly = 0) { pageViewModel.retry() }
        verify(exactly = 0) { pageViewModel.refresh() }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-036 연결한 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val linkedTag = webTestTag(title = LINKED_TAG_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setWebDetailScreen(
            tagList = listOf(linkedTag),
            navigateToTagDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(LINKED_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(linkedTag.id)
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-036 태그를 연결해도 입력 중인 내용은 바뀌지 않는다`() {
        setWebDetailScreen(selectableTagList = listOf(webTestTag(title = SELECTABLE_TAG_TITLE)))
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.urlInput().performTextReplacement(TYPED_URL)
        composeRule.waitForIdle()

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.urlInput().assert(hasText(TYPED_URL))
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-024 나타낼 태그가 없으면 태그 추가 항목이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setWebDetailScreen(navigateToTagAdd = { tagAddCount += 1 })

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-DOMAIN-013 연결할 수 있는 태그가 없어도 목록 대상이 있으면 목록이 열린다`() {
        val finishedTag = webTestTag(title = LINKED_TAG_TITLE).copy(isFinished = true)
        var tagAddCount = 0
        setWebDetailScreen(
            tagList = listOf(finishedTag),
            selectableTagList = listOf(finishedTag),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-027 목록의 태그 추가 항목을 누르면 목록이 닫히고 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setWebDetailScreen(
            selectableTagList = listOf(webTestTag(title = SELECTABLE_TAG_TITLE)),
            navigateToTagAdd = { tagAddCount += 1 },
        )
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-028 TagAdd 화면에서 추가한 태그가 돌아왔을 때 상세 대상 웹 항목에 연결된다`() {
        val addedTag = webTestTag(title = SELECTABLE_TAG_TITLE)
        val tagViewModel = detailTagScreenTestViewModel()
        val resultEventBus = ResultEventBus()
        setWebDetailScreen(resultEventBus = resultEventBus, tagViewModel = tagViewModel)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.add(tagId = addedTag.id) }
    }

    private fun linkTag(title: String) {
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()
        awaitPickerRow(title = title)
        composeRule.onNodeWithText(title).performClick()
        composeRule.waitForIdle()
    }

    // 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
    private fun awaitPickerRow(title: String) {
        repeat(PICKER_WAIT_ATTEMPT_COUNT) {
            composeRule.waitForIdle()
            if (composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()) return
            composeRule.mainClock.advanceTimeByFrame()
            @Suppress("ForbiddenMethodCall")
            Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
        }

        error("태그 선택 목록에 $title 이 나타나지 않았다")
    }

    private fun setWebDetailScreen(
        uiState: WebDetailUiState = testContentUiState(),
        pageViewModel: WebDetailPageViewModel = pageViewModel(),
        tagList: List<Tag> = emptyList(),
        selectableTagList: List<Tag> = emptyList(),
        navigateToTagDetail: (Uuid) -> Unit = {},
        navigateToTagAdd: () -> Unit = {},
        selectFormTab: Boolean = true,
        resultEventBus: ResultEventBus = ResultEventBus(),
        tagViewModel: WebDetailTagViewModel =
            detailTagScreenTestViewModel(
                tagList = tagList,
                selectableTagList = selectableTagList,
            ),
    ) {
        composeRule.setContent {
            WebDetailScreenTestTheme(resultEventBus = resultEventBus) {
                WebDetailScreen(
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    id = FIRST_WEB_ID,
                    navigateToTagAdd = navigateToTagAdd,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    webViewModel = webViewModel(uiState = uiState),
                    pageViewModel = pageViewModel,
                    navigateToTagDetail = navigateToTagDetail,
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()

        if (selectFormTab) {
            composeRule.selectFormTab()
        }
    }

    private companion object {
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
        const val DEFAULT_ENTITY_TAG_LABEL = "Select tag"
        const val DEFAULT_PICKER_TITLE = "Select Tag"
        const val DEFAULT_PICKER_TAG_ADD = "Add tag"
        const val LINKED_TAG_TITLE = "WebDetailLinkedTag"
        const val SELECTABLE_TAG_TITLE = "WebDetailSelectableTag"
        const val TYPED_TITLE = "WebDetailTagTypedTitle"
        const val TYPED_URL = "https://tag.example.com"

        fun webViewModel(uiState: WebDetailUiState): WebDetailViewModel {
            val viewModel = mockk<WebDetailViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(uiState)
            every { viewModel.effect } returns emptyFlow()
            justRun { viewModel.update(any()) }
            justRun { viewModel.delete() }

            return viewModel
        }

        fun pageViewModel(pageUiState: WebDetailPageUiState = WebDetailPageUiState.Loading): WebDetailPageViewModel {
            val viewModel = mockk<WebDetailPageViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(pageUiState)
            justRun { viewModel.load() }
            justRun { viewModel.retry() }
            justRun { viewModel.refresh() }

            return viewModel
        }
    }
}
