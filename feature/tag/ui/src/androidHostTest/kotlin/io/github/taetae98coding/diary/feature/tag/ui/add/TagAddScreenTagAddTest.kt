package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.tag.api.TagAddedResult
import io.github.taetae98coding.diary.feature.tag.api.tagAddedResultKey
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_PICKER_TAG_ADD
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_PICKER_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_TAG_LINK_LABEL
import io.github.taetae98coding.diary.feature.tag.ui.link.EXERCISE_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.github.taetae98coding.diary.feature.tag.ui.link.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.awaitTagLinkPickerRows
import io.github.taetae98coding.diary.feature.tag.ui.link.closeDialogByBack
import io.github.taetae98coding.diary.feature.tag.ui.link.dialogNodeWithText
import io.github.taetae98coding.diary.feature.tag.ui.link.refreshFailedTagPagingData
import io.github.taetae98coding.diary.feature.tag.ui.link.refreshingTagPagingData
import io.github.taetae98coding.diary.feature.tag.ui.link.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.link.testTag
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.sendTagAddedResult
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 연결 입력은 칩 영역 안에서만 스크롤되므로, 바깥 본문을 스크롤하지 않고도 칩이 보이는 창 크기로 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class TagAddScreenTagAddTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-5cd7e2ee-a6d8-4732-bfcc-bddf8f95b97f TC-bafce675-468e-430e-b1ca-2721e029fcd5 저장된 태그가 없으면 목록을 연 적이 없어도 첫 누름에 태그 연결 칩이 TagAdd 이동을 요청한다`() {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(tagPagingDataOf(emptyList())))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
        var tagAddCount = 0
        setTagAddScreen(
            tagLinkViewModel = TagAddLinkViewModel(pageTagUseCase = pageTagUseCase, getSelectedTagUseCase = getSelectedTagUseCase),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-195afaca-9147-4f38-874f-a76b68481672 목록의 대상을 확인하는 중에는 태그 연결 칩이 목록을 연다`() {
        var tagAddCount = 0
        setTagAddScreen(
            tagPagingData = MutableStateFlow(refreshingTagPagingData()),
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
        setTagAddScreen(
            tagPagingData = MutableStateFlow(refreshFailedTagPagingData()),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-d7346bdd-fd91-440c-99d0-0ffa429db209 목록의 태그 추가 항목을 누르면 목록이 닫히고 TagAdd 이동을 요청한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        var tagAddCount = 0
        setTagAddScreen(tagList = listOf(tag), navigateToTagAdd = { tagAddCount += 1 })

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-430ad93d-aa00-47b2-b4b7-3435df9312be TC-193b43fe-563c-4dd6-843a-b21d589800f1 TagAdd 화면에서 추가한 태그 하나가 돌아왔을 때 연결된다`() {
        val linkedTag = testTag(title = WORK_TAG_TITLE)
        val addedTag = testTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setTagAddScreen(tagList = listOf(linkedTag, addedTag), resultEventBus = resultEventBus)
        linkTag(title = WORK_TAG_TITLE)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-430ad93d-aa00-47b2-b4b7-3435df9312be TagAdd 화면에서 추가한 태그 여러 개가 돌아왔을 때 모두 연결된다`() {
        val firstAddedTag = testTag(title = WORK_TAG_TITLE)
        val secondAddedTag = testTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setTagAddScreen(tagList = listOf(firstAddedTag, secondAddedTag), resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = firstAddedTag.id)
        resultEventBus.sendTagAddedResult(id = secondAddedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-6b4c99d6-75d5-435e-ba8a-20bb171d4203 태그를 추가하지 않고 돌아오면 연결이 그대로 유지된다`() {
        val linkedTag = testTag(title = WORK_TAG_TITLE)
        val otherTag = testTag(title = EXERCISE_TAG_TITLE)
        setTagAddScreen(tagList = listOf(linkedTag, otherTag))
        linkTag(title = WORK_TAG_TITLE)

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-2b6eb189-b3ca-4ee3-ac1e-7874341d9149 TagAdd 화면에서 돌아와도 태그 선택 목록이 저절로 열리지 않는다`() {
        val addedTag = testTag(title = WORK_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setTagAddScreen(tagList = listOf(addedTag), resultEventBus = resultEventBus)
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-c0f9fe7b-1665-41c2-893e-df56af537926 연결 입력에서 태그 추가로 이동해도 입력한 내용과 고른 연결이 유지된다`() {
        val linkedTag = testTag(title = WORK_TAG_TITLE)
        setTagAddScreen(tagList = listOf(linkedTag))
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        linkTag(title = WORK_TAG_TITLE)

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-afc4dbf5-59ab-4d18-8b94-25e2e011dd00 자동 연결된 태그의 연결을 해제하면 다시 연결되지 않는다`() {
        val addedTag = testTag(title = WORK_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setTagAddScreen(tagList = listOf(addedTag), resultEventBus = resultEventBus)
        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-e8494f06-12dd-4921-a602-7eb94eb75d59 이 입력에서 이동하지 않은 TagAdd 화면의 태그는 자동 연결되지 않는다`() {
        val addedTag = testTag(title = WORK_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setTagAddScreen(tagList = listOf(addedTag), resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = addedTag.id, requestKey = OTHER_REQUEST_KEY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-41d921c0-e05d-4826-a6d5-65cf0d31f8da 이 화면에서 추가한 태그는 연결 대상으로 선택되지 않는다`() {
        val addedId = Uuid.random()
        val effect = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModel.add(any(), any()) } answers { effect.trySend(TagAddEffect.AddSucceeded(id = addedId)).getOrThrow() }
        setTagAddScreen(
            viewModel = viewModel,
            tagList = listOf(testTag(title = WORK_TAG_TITLE).copy(id = addedId)),
            addedResultRequestKey = REQUESTED_KEY,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).assertExists()
    }

    @Test
    fun `TC-50bb74e0-3cc5-4597-b820-cb3367b3f720 추가 결과는 이 화면을 연 태그 입력에만 돌려준다`() {
        val firstAddedId = Uuid.random()
        val secondAddedId = Uuid.random()
        val effect = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        val addedIdList = mutableListOf<Uuid>()
        val otherRequestIdList = mutableListOf<Uuid>()
        every { viewModel.add(any(), any()) } answers { effect.trySend(TagAddEffect.AddSucceeded(id = firstAddedId)).getOrThrow() }
        setTagAddScreen(
            viewModel = viewModel,
            addedResultRequestKey = REQUESTED_KEY,
            resultCollector = {
                CollectTagAddedResult(requestKey = REQUESTED_KEY, idList = addedIdList)
                CollectTagAddedResult(requestKey = OTHER_REQUEST_KEY, idList = otherRequestIdList)
            },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        every { viewModel.add(any(), any()) } answers { effect.trySend(TagAddEffect.AddSucceeded(id = secondAddedId)).getOrThrow() }
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        addedIdList shouldBe listOf(firstAddedId, secondAddedId)
        otherRequestIdList.shouldBeEmpty()
    }

    @Test
    fun `TC-c444d6cb-bf1b-46ab-aa27-b46ba054b0dc 태그 입력에서 열지 않은 화면의 추가 결과는 어디에도 전달되지 않는다`() {
        val effect = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        val addedIdList = mutableListOf<Uuid>()
        every { viewModel.add(any(), any()) } answers { effect.trySend(TagAddEffect.AddSucceeded(id = Uuid.random())).getOrThrow() }
        setTagAddScreen(
            viewModel = viewModel,
            addedResultRequestKey = null,
            resultCollector = {
                CollectTagAddedResult(requestKey = REQUESTED_KEY, idList = addedIdList)
                CollectTagAddedResult(requestKey = OTHER_REQUEST_KEY, idList = addedIdList)
            },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        addedIdList.shouldBeEmpty()
    }

    @Composable
    private fun CollectTagAddedResult(
        requestKey: Uuid,
        idList: MutableList<Uuid>,
    ) {
        ResultEffect<TagAddedResult>(resultKey = tagAddedResultKey(requestKey = requestKey)) { result ->
            idList += result.id
        }
    }

    private fun linkTag(title: String) {
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(title).performClick()
        composeRule.closeDialogByBack()
    }

    private fun setTagAddScreen(
        viewModel: TagAddViewModel = screenTestViewModel(),
        tagList: List<Tag> = emptyList(),
        tagPagingData: MutableStateFlow<PagingData<Tag>> = MutableStateFlow(tagPagingDataOf(tagList)),
        navigateToTagAdd: () -> Unit = {},
        addedResultRequestKey: Uuid? = null,
        resultEventBus: ResultEventBus = ResultEventBus(),
        resultCollector: @Composable () -> Unit = {},
        tagLinkViewModel: TagAddLinkViewModel = linkViewModel(tagList = tagList, tagPagingData = tagPagingData),
    ) {
        composeRule.setContent {
            TagAddScreenTestTheme(resultEventBus = resultEventBus) {
                resultCollector()
                TagAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToDetail = {},
                    addedResultRequestKey = addedResultRequestKey,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { TagAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    linkViewModel = tagLinkViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val DEFAULT_ADD_BUTTON_DESCRIPTION: String = "Add tag"
        const val TYPED_TITLE: String = "TagAddTitleInput"
        val REQUESTED_KEY: Uuid = Uuid.parse("30000000-0000-0000-0000-000000000002")
        val OTHER_REQUEST_KEY: Uuid = Uuid.parse("30000000-0000-0000-0000-000000000003")

        /**
         * 고른 태그를 보관하고 표시하는 동작만 남긴 연결 ViewModel을 만든다.
         */
        fun linkViewModel(
            tagList: List<Tag>,
            tagPagingData: MutableStateFlow<PagingData<Tag>>,
        ): TagAddLinkViewModel {
            val selectionUiState = MutableStateFlow(TagAddLinkSelectionUiState())
            val uiState = MutableStateFlow(TagLinkInputUiState())

            fun reflect() {
                uiState.value = TagLinkInputUiState(linkedTagList = tagList.filter { tag -> tag.id in selectionUiState.value.linkedTagIdSet })
            }

            return mockk<TagAddLinkViewModel>(relaxed = true).apply {
                every { this@apply.uiState } returns uiState
                every { this@apply.tagPagingData } returns tagPagingData
                every { this@apply.selectableTagPagingData } returns tagPagingData
                every { this@apply.selectionUiState } returns selectionUiState
                every { link(id = any()) } answers {
                    selectionUiState.value = selectionUiState.value.copy(linkedTagIdSet = selectionUiState.value.linkedTagIdSet + firstArg<Uuid>())
                    reflect()
                }
                every { unlink(id = any()) } answers {
                    selectionUiState.value = selectionUiState.value.copy(linkedTagIdSet = selectionUiState.value.linkedTagIdSet - firstArg<Uuid>())
                    reflect()
                }
                every { clear() } answers {
                    selectionUiState.value = TagAddLinkSelectionUiState()
                    reflect()
                }
            }
        }
    }
}
