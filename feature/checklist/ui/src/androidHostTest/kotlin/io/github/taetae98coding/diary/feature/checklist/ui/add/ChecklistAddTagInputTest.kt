package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.checklist.ui.ChecklistTagTestTheme
import io.github.taetae98coding.diary.feature.checklist.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.checklist.ui.addTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.checklist.ui.clickEntityTagInput
import io.github.taetae98coding.diary.feature.checklist.ui.detail.titleInput
import io.github.taetae98coding.diary.feature.checklist.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.checklist.ui.sendTagAddedResult
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ChecklistAddTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-f4697366-0c8b-4af7-85b9-49425cd1b02d 추가에 성공해도 고른 태그는 유지한다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val viewModel = succeedingViewModel()
        setScreen(viewModel = viewModel, tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)))
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.waitForIdle()

        clickAdd()

        composeRule.titleInput().assert(hasText(""))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
        verify(exactly = 1) { viewModel.add(detail = any(), itemContentList = any(), tagIdSet = setOf(tag.id)) }
    }

    @Test
    fun `TC-b12e27fa-50c0-48cb-91fe-b46336c19d2d 화면이 재생성되어도 고른 태그가 유지된다`() {
        val tag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            ChecklistTagTestTheme {
                ChecklistAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { ChecklistAddScaffoldComponentVisible() },
                    addViewModel = relaxedViewModel(),
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-6da117b9-1166-49fc-8cbf-ad0deeae93a6 TagDetail 체크리스트 탭에서 진입했으면 복원 뒤 입력은 남고 태그는 그 탭의 태그만 선택된다`() {
        assertMemoryRestore(
            initialTag = Arb.tag(title = INITIAL_TAG_TITLE).next(),
            expectedTitleListAfterRestore = listOf(INITIAL_TAG_TITLE),
        )
    }

    @Test
    fun `TC-6da117b9-1166-49fc-8cbf-ad0deeae93a6 그 밖의 경로로 진입했으면 복원 뒤 입력은 남고 선택된 태그가 없다`() {
        assertMemoryRestore(
            initialTag = null,
            expectedTitleListAfterRestore = emptyList(),
        )
    }

    @Test
    fun `TagAdd 화면에서 추가한 태그가 돌아오면 고른 태그로 더한다`() {
        val addedTag = Arb.tag(title = SELECTED_TAG_TITLE).next()
        val tagViewModel = addTagScreenTestViewModel()
        val resultEventBus = ResultEventBus()
        setScreen(tagViewModel = tagViewModel, resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.add(tagId = addedTag.id) }
    }

    @Test
    fun `고를 수 있는 태그가 없으면 태그 추가 칩이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setScreen(navigateToTagAdd = { tagAddCount += 1 })

        composeRule.clickEntityTagInput()

        tagAddCount shouldBe 1
    }

    private fun assertMemoryRestore(
        initialTag: Tag?,
        expectedTitleListAfterRestore: List<String>,
    ) {
        val otherTag = Arb.tag(title = OTHER_TAG_TITLE).next()
        val allTagList = listOfNotNull(initialTag, otherTag)
        val tagViewModelList = mutableListOf<EntityAddTagViewModel>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 태그 선택을 들고 있던 객체도 새로 만들어지므로 복원할 때마다 진입 경로의 초기값으로 새로 만든다.
            val tagViewModel =
                remember {
                    tagViewModel(initialTagId = initialTag?.id, allTagList = allTagList).also(tagViewModelList::add)
                }

            ChecklistTagTestTheme {
                ChecklistAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { ChecklistAddScaffoldComponentVisible() },
                    addViewModel = remember { relaxedViewModel() },
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.titleInput().performTextInput(TYPED_TITLE)
        composeRule.runOnIdle { tagViewModelList.last().add(tagId = otherTag.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        tagViewModelList.size shouldBe 2
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.onNodeWithText(OTHER_TAG_TITLE).assertDoesNotExist()
        expectedTitleListAfterRestore.forEach { title -> composeRule.onNodeWithText(title).assertExists() }
        composeRule.runOnIdle {
            tagViewModelList
                .last()
                .selectionUiState.value.tagIdSet shouldBe setOfNotNull(initialTag?.id)
        }
    }

    private fun setScreen(
        viewModel: ChecklistAddViewModel = relaxedViewModel(),
        tagViewModel: EntityAddTagViewModel = addTagScreenTestViewModel(),
        navigateToTagAdd: () -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContent {
            ChecklistTagTestTheme(resultEventBus = resultEventBus) {
                ChecklistAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    componentVisibleProvider = { ChecklistAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun clickAdd() {
        composeRule.onNodeWithContentDescription(DEFAULT_ADD).performClick()
        composeRule.waitForIdle()
    }

    private fun relaxedViewModel(): ChecklistAddViewModel =
        mockk<ChecklistAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(ChecklistAddUiState())
            every { effect } returns emptyFlow()
        }

    private fun succeedingViewModel(): ChecklistAddViewModel {
        val channel = Channel<ChecklistAddEffect>(capacity = Channel.BUFFERED)
        return mockk<ChecklistAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(ChecklistAddUiState())
            every { effect } returns channel.receiveAsFlow()
            every { add(detail = any(), itemContentList = any(), tagIdSet = any()) } answers { channel.trySend(ChecklistAddEffect.AddSucceeded) }
        }
    }

    private fun tagViewModel(
        initialTagId: Uuid?,
        allTagList: List<Tag>,
    ): EntityAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(allTagList)))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            flowOf(Result.success(allTagList.filter { tag -> tag.id in idSet }))
        }

        return EntityAddTagViewModel(
            initialTagId = initialTagId,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    private companion object {
        const val DEFAULT_ADD = "Add checklist"
        const val TYPED_TITLE = "ChecklistAddTagTypedTitle"
        const val SELECTED_TAG_TITLE = "ChecklistAddSelectedTag"
        const val INITIAL_TAG_TITLE = "ChecklistAddRestoreInitialTag"
        const val OTHER_TAG_TITLE = "ChecklistAddRestoreOtherTag"
    }
}
