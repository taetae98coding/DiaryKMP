package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.detail.form.TagDetailLinkViewModel
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_PICKER_TAG_ADD
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_PICKER_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_TAG_LINK_LABEL
import io.github.taetae98coding.diary.feature.tag.ui.link.EXERCISE_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.github.taetae98coding.diary.feature.tag.ui.link.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.awaitTagLinkPickerRows
import io.github.taetae98coding.diary.feature.tag.ui.link.dialogNodeWithText
import io.github.taetae98coding.diary.feature.tag.ui.link.refreshFailedTagPagingData
import io.github.taetae98coding.diary.feature.tag.ui.link.refreshingTagPagingData
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
    fun `TC-35995748-4049-436d-af96-2e0466241b9e 저장된 연결의 태그가 연결 입력에 표시된다`() {
        val linkedTag = testTag(title = WORK_TAG_TITLE)
        setTagDetailScreen(linkUiState = TagLinkInputUiState(linkedTagList = listOf(linkedTag)))

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).assertExists()
    }

    @Test
    fun `TC-664344b5-d1b5-422d-a12c-ea7dcbe45e2e 목록에서 태그를 누르면 연결을 요청한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setTagDetailScreen(selectableTagList = listOf(tag))

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { linkViewModel().link(tagId = tag.id) }
    }

    @Test
    fun `TC-6e01ef75-9bf3-4f5d-9dc4-60ea526233da 목록에서 연결된 태그를 누르면 해제를 요청한다`() {
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
    fun `TC-2f0ac0b1-7f1e-42c1-9724-f2b684cc9601 연결이 바뀌어도 수정 반영 동작을 제공하지 않는다`() {
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
    fun `TC-b78bf494-cd32-46af-a051-3c96ea4c256c 조회 중에는 연결 입력을 표시하지 않는다`() {
        setTagDetailScreen(
            uiState = MutableStateFlow(TagDetailUiState.Loading),
            linkUiState = TagLinkInputUiState(linkedTagList = listOf(testTag(title = WORK_TAG_TITLE))),
        )

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-f24a43a2-e275-4227-bca6-8d2f9793e98f 저장된 연결이 바뀌면 연결 입력에 반영된다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setTagDetailScreen()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()

        linkUiStateFlow.value = TagLinkInputUiState(linkedTagList = listOf(tag))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-8076e669-03c7-4d3d-aa4b-a370ee7a86ed 다른 태그를 선택하면 연결 입력도 새 태그의 연결로 바뀐다`() {
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
    fun `TC-495d3d3e-6c64-4c35-b241-a5580fa83fd6 태그 칩을 누르면 그 태그의 상세로 이동한다`() {
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
    fun `TC-88e17216-d82b-4fba-bc3c-3d682a799802 태그 연결 칩을 누르면 태그 선택 목록이 열린다`() {
        val tag = testTag(title = EXERCISE_TAG_TITLE)
        setTagDetailScreen(selectableTagList = listOf(tag))

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-5f49f83f-744f-4940-81bd-847cb076bf35 연결 대상으로 표시하는 태그는 선택 목록의 페이지와 무관하게 표시된다`() {
        val linkedTagList = List(LINKED_TAG_COUNT) { index -> testTag(title = "$LINKED_TAG_TITLE_PREFIX$index") }
        setTagDetailScreen(linkUiState = TagLinkInputUiState(linkedTagList = linkedTagList))

        linkedTagList.forEach { tag ->
            composeRule.onNodeWithText(tag.detail.title).assertExists()
        }
    }

    @Test
    fun `TC-5cd7e2ee-a6d8-4732-bfcc-bddf8f95b97f 나타낼 태그가 없으면 태그 연결 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setTagDetailScreen(navigateToTagAdd = { tagAddCount += 1 })

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-195afaca-9147-4f38-874f-a76b68481672 목록의 대상을 확인하는 중에는 태그 연결 칩이 목록을 연다`() {
        var tagAddCount = 0
        setTagDetailScreen(
            tagPagingData = refreshingTagPagingData(),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-c9f7e125-bb4c-46e3-9dab-906e275e0945 목록의 대상을 처음 불러오지 못하면 태그 연결 칩이 목록을 연다`() {
        var tagAddCount = 0
        setTagDetailScreen(
            tagPagingData = refreshFailedTagPagingData(),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-8770c574-43cd-40d6-bad5-828568e805ab 연결할 수 있는 태그가 없어도 목록 대상이 있으면 목록이 열린다`() {
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
    fun `TC-d7346bdd-fd91-440c-99d0-0ffa429db209 목록의 태그 추가 항목을 누르면 목록이 닫히고 TagAdd 이동을 요청한다`() {
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
    fun `TC-430ad93d-aa00-47b2-b4b7-3435df9312be TagAdd 화면에서 추가한 태그가 돌아왔을 때 상세 대상 태그에 연결된다`() {
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
        tagPagingData: PagingData<Tag> = tagPagingDataOf(selectableTagList),
        navigateToDetail: (Uuid) -> Unit = {},
        navigateToTagAdd: () -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(uiState = uiState),
            linkUiState = linkUiState,
            tagPagingData = tagPagingData,
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
