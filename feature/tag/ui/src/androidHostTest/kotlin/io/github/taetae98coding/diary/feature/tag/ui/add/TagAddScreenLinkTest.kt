package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_PICKER_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.DEFAULT_TAG_LINK_LABEL
import io.github.taetae98coding.diary.feature.tag.ui.link.EXERCISE_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.github.taetae98coding.diary.feature.tag.ui.link.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.link.awaitTagLinkPickerRows
import io.github.taetae98coding.diary.feature.tag.ui.link.closeDialogByBack
import io.github.taetae98coding.diary.feature.tag.ui.link.dialogNodeWithText
import io.github.taetae98coding.diary.feature.tag.ui.link.tagLinkPickerList
import io.github.taetae98coding.diary.feature.tag.ui.link.tagPagingDataOf
import io.github.taetae98coding.diary.feature.tag.ui.link.testTag
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 연결 입력은 칩 영역 안에서만 스크롤되므로, 바깥 본문을 스크롤하지 않고도 칩이 보이는 창 크기로 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class TagAddScreenLinkTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-004 태그 연결 칩을 누르면 태그 선택 목록이 열린다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        setTagAddScreen(tagList = tagList)

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-024 나타낼 태그가 없으면 태그 연결 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setTagAddScreen(tagList = emptyList(), navigateToTagAdd = { tagAddCount += 1 })

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-010 목록을 닫아도 반영된 연결이 유지된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        setTagAddScreen(tagList = tagList)

        linkWorkTag()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-019 연결을 골라도 태그 추가를 실행하지 않는다`() {
        val viewModel = screenTestViewModel()
        setTagAddScreen(viewModel = viewModel, tagList = listOf(testTag(title = WORK_TAG_TITLE)))

        linkWorkTag()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        verify(exactly = 0) { viewModel.add(any(), any()) }
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-020 추가에 성공하면 고른 연결도 비운다`() {
        val effect = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModel.add(any(), any()) } answers {
            effect.trySend(TagAddEffect.AddSucceeded(id = Uuid.random())).getOrThrow()
        }
        setTagAddScreen(viewModel = viewModel, tagList = listOf(testTag(title = WORK_TAG_TITLE)))
        linkWorkTag()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).assertExists()
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-010 제목 미입력으로 추가를 실행해도 고른 연결은 유지된다`() {
        val effect = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        val linkedTagIdSet = MutableStateFlow(emptySet<Uuid>())
        val tag = testTag(title = WORK_TAG_TITLE)
        every { viewModel.add(any(), any()) } answers {
            effect.trySend(TagAddEffect.TitleBlank).getOrThrow()
        }
        setTagAddScreen(viewModel = viewModel, tagList = listOf(tag), linkedTagIdSet = linkedTagIdSet)
        linkWorkTag()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        linkedTagIdSet.value shouldBe setOf(tag.id)
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-020 추가에 성공하면 고른 연결의 식별자도 비운다`() {
        val effect = Channel<TagAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        val linkedTagIdSet = MutableStateFlow(emptySet<Uuid>())
        val tag = testTag(title = WORK_TAG_TITLE)
        every { viewModel.add(any(), any()) } answers {
            effect.trySend(TagAddEffect.AddSucceeded(id = Uuid.random())).getOrThrow()
        }
        setTagAddScreen(viewModel = viewModel, tagList = listOf(tag), linkedTagIdSet = linkedTagIdSet)
        linkWorkTag()

        linkedTagIdSet.value shouldBe setOf(tag.id)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        linkedTagIdSet.value.shouldBeEmpty()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-013 목록의 끝으로 이동하면 다음 태그가 이어서 나타난다`() {
        val tagList = List(PICKER_TAG_COUNT) { index -> testTag(title = pickerTagTitle(index)) }
        setTagAddScreen(tagList = tagList)

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(tagList.last().detail.title).assertIsNotDisplayed()
        composeRule.tagLinkPickerList().performScrollToIndex(tagList.lastIndex)
        composeRule.waitForIdle()

        composeRule.dialogNodeWithText(tagList.last().detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-016 목록을 닫았다가 다시 열면 앞부분부터 나타난다`() {
        val tagList = List(PICKER_TAG_COUNT) { index -> testTag(title = pickerTagTitle(index)) }
        setTagAddScreen(tagList = tagList)

        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.tagLinkPickerList().performScrollToIndex(tagList.lastIndex)
        composeRule.waitForIdle()
        composeRule.dialogNodeWithText(tagList.first().detail.title).assertIsNotDisplayed()

        composeRule.closeDialogByBack()
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()

        composeRule.dialogNodeWithText(tagList.first().detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-017 화면이 재생성되어도 태그 선택 목록이 열린 상태로 유지된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE))
        val viewModel = screenTestViewModel()
        val linkViewModel = linkViewModel(tagList = tagList)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            TagAddScreenTestTheme {
                TagAddScreen(
                    navigateToTagAdd = {},
                    addedResultRequestKey = null,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    addViewModel = viewModel,
                    linkViewModel = linkViewModel,
                    componentVisibleProvider = { TagAddScaffoldComponentVisible() },
                )
            }
        }
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.awaitTagLinkPickerRows()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-011 태그 칩을 누르면 그 태그의 상세로 이동한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setTagAddScreen(tagList = listOf(tag), navigateToDetail = navigatedIdList::add)
        linkWorkTag()

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(tag.id)
    }

    private fun linkWorkTag() {
        composeRule.onNodeWithText(DEFAULT_TAG_LINK_LABEL).performClick()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.closeDialogByBack()
    }

    private fun setTagAddScreen(
        viewModel: TagAddViewModel = screenTestViewModel(),
        tagList: List<Tag> = emptyList(),
        linkedTagIdSet: MutableStateFlow<Set<Uuid>> = MutableStateFlow(emptySet()),
        navigateToDetail: (Uuid) -> Unit = {},
        navigateToTagAdd: () -> Unit = {},
    ) {
        val linkViewModel = linkViewModel(tagList = tagList, linkedTagIdSet = linkedTagIdSet)

        composeRule.setContent {
            TagAddScreenTestTheme {
                TagAddScreen(
                    navigateToTagAdd = navigateToTagAdd,
                    addedResultRequestKey = null,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = navigateToDetail,
                    addViewModel = viewModel,
                    linkViewModel = linkViewModel,
                    componentVisibleProvider = { TagAddScaffoldComponentVisible() },
                )
            }
        }
    }

    public companion object {
        private const val PICKER_TAG_COUNT: Int = 30
        private const val PICKER_TAG_TITLE_PREFIX: String = "TagLinkPicker"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION: String = "Add tag"

        private fun pickerTagTitle(index: Int): String = "$PICKER_TAG_TITLE_PREFIX${index.toString().padStart(length = 3, padChar = '0')}"

        /**
         * 고른 태그를 보관하고 표시하는 동작만 남긴 연결 ViewModel을 만든다.
         */
        private fun linkViewModel(
            tagList: List<Tag>,
            linkedTagIdSet: MutableStateFlow<Set<Uuid>> = MutableStateFlow(emptySet()),
        ): TagAddLinkViewModel {
            val uiState = MutableStateFlow(TagLinkInputUiState())
            val tagPagingData = MutableStateFlow(tagPagingDataOf(tagList))

            fun reflect() {
                uiState.value = TagLinkInputUiState(linkedTagList = tagList.filter { tag -> tag.id in linkedTagIdSet.value })
            }

            val viewModel = mockk<TagAddLinkViewModel>(relaxed = true)
            every { viewModel.uiState } returns uiState
            every { viewModel.tagPagingData } returns tagPagingData
            every { viewModel.linkedTagIdSet } returns linkedTagIdSet
            every { viewModel.link(any()) } answers {
                linkedTagIdSet.value = linkedTagIdSet.value + firstArg<Uuid>()
                reflect()
            }
            every { viewModel.unlink(any()) } answers {
                linkedTagIdSet.value = linkedTagIdSet.value - firstArg<Uuid>()
                reflect()
            }
            every { viewModel.clear() } answers {
                linkedTagIdSet.value = emptySet()
                reflect()
            }

            return viewModel
        }
    }
}
