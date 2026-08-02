package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_PICKER_TAG_ADD
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_PICKER_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_TAG_LINK_LABEL
import io.github.taetae98coding.diary.feature.tag.ui.link.EXERCISE_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.github.taetae98coding.diary.feature.tag.ui.link.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.awaitTagLinkPickerRows
import io.github.taetae98coding.diary.feature.tag.ui.link.dialogNodeWithText
import io.github.taetae98coding.diary.feature.tag.ui.link.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.link.testTag
import io.github.taetae98coding.diary.feature.tag.ui.sendTagAddedResult
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 연결 입력은 칩 영역 안에서만 스크롤되므로, 바깥 본문을 스크롤하지 않고도 칩이 보이는 창 크기로 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class TagDetailScreenLinkTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-030 저장된 연결의 태그가 연결 입력에 표시된다`() {
        val linkedTag = testTag(title = WORK_TAG_TITLE)
        setTagDetailScreen(linkUiState = TagLinkInputUiState(linkedTagList = listOf(linkedTag)))

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-031 목록에서 태그를 누르면 연결을 요청한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setTagDetailScreen(selectableTagList = listOf(tag))

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { linkViewModel().link(tagId = tag.id) }
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-032 목록에서 연결된 태그를 누르면 해제를 요청한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setTagDetailScreen(
            linkUiState = TagLinkInputUiState(linkedTagList = listOf(tag)),
            selectableTagList = listOf(tag),
        )

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { linkViewModel().unlink(tagId = tag.id) }
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-033 연결이 바뀌어도 수정 반영 동작을 제공하지 않는다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setTagDetailScreen(selectableTagList = listOf(tag))

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        linkUiStateFlow.value = TagLinkInputUiState(linkedTagList = listOf(tag))
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-034 조회 중에는 연결 입력을 표시하지 않는다`() {
        setTagDetailScreen(
            uiState = MutableStateFlow(TagDetailUiState.Loading),
            linkUiState = TagLinkInputUiState(linkedTagList = listOf(testTag(title = WORK_TAG_TITLE))),
        )

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-035 저장된 연결이 바뀌면 연결 입력에 반영된다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setTagDetailScreen()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()

        linkUiStateFlow.value = TagLinkInputUiState(linkedTagList = listOf(tag))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-036 다른 태그를 선택하면 연결 입력도 새 태그의 연결로 바뀐다`() {
        val firstTag = testTag(title = WORK_TAG_TITLE)
        val secondTag = testTag(title = EXERCISE_TAG_TITLE)
        val uiState =
            MutableStateFlow<TagDetailUiState>(
                tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE)),
            )
        setTagDetailScreen(uiState = uiState, linkUiState = TagLinkInputUiState(linkedTagList = listOf(firstTag)))

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()

        uiState.value = tagDetailUiState(id = SECOND_TAG_ID, detail = tagDetail(TAG_TITLE))
        linkUiStateFlow.value = TagLinkInputUiState(linkedTagList = listOf(secondTag))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-011 태그 칩을 누르면 그 태그의 상세로 이동한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setTagDetailScreen(
            linkUiState = TagLinkInputUiState(linkedTagList = listOf(tag)),
            navigateToDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(tag.id)
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-004 태그 연결 칩을 누르면 태그 선택 목록이 열린다`() {
        val tag = testTag(title = EXERCISE_TAG_TITLE)
        setTagDetailScreen(selectableTagList = listOf(tag))

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-DATA-002 연결 대상으로 표시하는 태그는 선택 목록의 페이지와 무관하게 표시된다`() {
        val linkedTagList = List(LINKED_TAG_COUNT) { index -> testTag(title = "$LINKED_TAG_TITLE_PREFIX$index") }
        setTagDetailScreen(linkUiState = TagLinkInputUiState(linkedTagList = linkedTagList))

        linkedTagList.forEach { tag ->
            composeRule.onNodeWithText(tag.detail.title).assertExists()
        }
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-024 나타낼 태그가 없으면 태그 연결 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setTagDetailScreen(navigateToTagAdd = { tagAddCount += 1 })

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-DOMAIN-011 연결할 수 있는 태그가 없어도 목록 대상이 있으면 목록이 열린다`() {
        val finishedTag = testTag(title = EXERCISE_TAG_TITLE, isFinished = true)
        var tagAddCount = 0
        setTagDetailScreen(
            linkUiState = TagLinkInputUiState(linkedTagList = listOf(finishedTag)),
            selectableTagList = listOf(finishedTag),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()

        tagAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-027 목록의 태그 추가 항목을 누르면 목록이 닫히고 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setTagDetailScreen(
            selectableTagList = listOf(testTag(title = EXERCISE_TAG_TITLE)),
            navigateToTagAdd = { tagAddCount += 1 },
        )
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-028 TagAdd 화면에서 추가한 태그가 돌아왔을 때 상세 대상 태그에 연결된다`() {
        val addedTag = testTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setTagDetailScreen(resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        verify(exactly = 1) { linkViewModel().link(tagId = addedTag.id) }
    }

    private fun setTagDetailScreen(
        uiState: MutableStateFlow<TagDetailUiState> =
            MutableStateFlow(tagDetailUiState(id = FIRST_TAG_ID, detail = tagDetail(TAG_TITLE))),
        linkUiState: TagLinkInputUiState = TagLinkInputUiState(),
        selectableTagList: List<Tag> = emptyList(),
        navigateToDetail: (Uuid) -> Unit = {},
        navigateToTagAdd: () -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(uiState = uiState),
            linkUiState = linkUiState,
            tagPagingData = tagPagingDataOf(selectableTagList),
            navigateToDetail = navigateToDetail,
            navigateToTagAdd = navigateToTagAdd,
            resultEventBus = resultEventBus,
        )
    }

    private fun linkViewModel(): TagDetailLinkViewModel = requireNotNull(linkViewModelRef)

    public companion object {
        private const val LINKED_TAG_COUNT: Int = 5
        private const val LINKED_TAG_TITLE_PREFIX: String = "TagDetailLinked"
        private val SECOND_TAG_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000002")
    }
}
