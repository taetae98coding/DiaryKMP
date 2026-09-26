package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PICKER_TAG_ADD
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PRIMARY_SET_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PRIMARY_TAG_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PRIMARY_UNSET_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.tag.EXERCISE_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.awaitTagPickerRows
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodesWithContentDescription
import io.github.taetae98coding.diary.feature.memo.ui.tag.refreshingTagPagingData
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPickerList
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoAddScreenTagTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-008 목록에서 태그를 선택하면 태그 칩으로 즉시 표시된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = tagList))

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-009 목록에서 선택을 해제하면 태그 칩이 즉시 사라진다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = listOf(tag)))

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-015 대표 태그로 지정하면 그 태그가 함께 선택된다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = listOf(tag)))

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-016 대표 태그 지정 버튼을 다시 누르면 대표 지정만 해제된다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = listOf(tag)))

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_UNSET_DESCRIPTION)[0].performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-017 다른 태그를 대표로 지정하면 이전 대표 지정이 해제된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = tagList))

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[1].performClick()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onAllNodesWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-018 대표 태그의 선택을 해제하면 대표 지정도 해제된다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = listOf(tag)))

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-021 태그 칩을 누르면 그 태그의 TagDetail 화면으로 이동한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)
        val navigatedTagIdList = mutableListOf<Uuid>()
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(tagList = listOf(workTag, exerciseTag)),
            navigateToTagAdd = {},
            navigateToTagDetail = { id -> navigatedTagIdList += id },
            navigateToPlaceAdd = {},
            navigateToPlaceDetail = {},
        )
        selectWorkTagAndPrimaryExerciseTag()

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).performClick()

        navigatedTagIdList shouldBe listOf(workTag.id, exerciseTag.id)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-022 태그 칩을 눌러도 선택 상태와 태그 선택 목록은 바뀌지 않는다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = tagList))
        selectWorkTagAndPrimaryExerciseTag()

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onAllNodesWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-022 대표 태그 칩을 눌러도 선택 상태와 대표 지정, 태그 선택 목록은 바뀌지 않는다`() {
        val navigatedTagIdList = mutableListOf<Uuid>()
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = listOf(workTag, exerciseTag)), navigateToTagDetail = navigatedTagIdList::add)
        selectWorkTagAndPrimaryExerciseTag()

        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedTagIdList shouldBe listOf(exerciseTag.id)
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onAllNodesWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-020 목록을 닫아도 반영된 선택과 대표 태그 지정이 유지된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = tagList))

        selectWorkTagAndPrimaryExerciseTag()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onAllNodesWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertCountEquals(1)
    }

    private fun selectWorkTagAndPrimaryExerciseTag() {
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[1].performClick()
        composeRule.closeDialogByBack()
    }

    private fun setMemoAddScreen(
        viewModels: MemoAddScreenViewModels,
        navigateToTagAdd: () -> Unit = {},
        navigateToTagDetail: (Uuid) -> Unit = {},
        navigateToPlaceAdd: (Coordinate?) -> Unit = {},
        navigateToPlaceDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setContent {
            MemoAddScreenTestTheme {
                MemoAddScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addViewModel = viewModels.viewModel,
                    tagViewModel = viewModels.tagViewModel,
                    webViewModel = viewModels.webViewModel,
                    contactViewModel = viewModels.contactViewModel,
                    placeViewModel = viewModels.placeViewModel,
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = navigateToTagDetail,
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = navigateToPlaceAdd,
                    navigateToPlaceDetail = navigateToPlaceDetail,
                    initialDateRange = null,
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoAddScreenTagPickerOpenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-003 추가 항목을 누르면 선택할 수 있는 태그 목록이 열린다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        var tagAddCount = 0
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(tagList = tagList),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        tagAddCount shouldBe 0
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-024 목록의 끝으로 이동하면 다음 태그가 이어서 나타난다`() {
        val tagList = List(PICKER_TAG_COUNT) { index -> testTag(title = "$PICKER_TAG_TITLE_PREFIX${index.toString().padStart(length = 3, padChar = '0')}") }
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = tagList))

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(tagList.first().detail.title).assertIsDisplayed()
        composeRule.dialogNodeWithText(tagList.last().detail.title).assertDoesNotExist()

        composeRule.tagPickerList().performScrollToIndex(tagList.lastIndex)
        composeRule.waitForIdle()

        composeRule.dialogNodeWithText(tagList.last().detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-027 목록을 닫았다가 다시 열면 앞부분부터 나타난다`() {
        val tagList = List(PICKER_TAG_COUNT) { index -> testTag(title = "$PICKER_TAG_TITLE_PREFIX${index.toString().padStart(length = 3, padChar = '0')}") }
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagList = tagList))

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.tagPickerList().performScrollToIndex(tagList.lastIndex)
        composeRule.waitForIdle()
        composeRule.dialogNodeWithText(tagList.first().detail.title).assertIsNotDisplayed()

        composeRule.closeDialogByBack()
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()

        composeRule.dialogNodeWithText(tagList.first().detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-029 TC-MEMO-TAG-INPUT-FEATURE-047 나타낼 태그가 없으면 목록을 연 적이 없어도 첫 누름에 추가 항목이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(tagList = emptyList()),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-030 목록의 대상을 확인하는 중에는 추가 항목이 목록을 연다`() {
        var tagAddCount = 0
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(tagPagingDataFlow = MutableStateFlow(refreshingTagPagingData())),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
        tagAddCount shouldBe 0
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-031 확인 중에 연 목록이 대상 없음으로 확정되면 목록 영역이 비어 있는 채로 유지된다`() {
        val tagPagingDataFlow = MutableStateFlow(refreshingTagPagingData())
        setMemoAddScreen(viewModels = screenTestRealViewModel(tagPagingDataFlow = tagPagingDataFlow))
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.waitForIdle()

        tagPagingDataFlow.value = tagPagingDataOf(emptyList())
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).assertCountEquals(0)
    }

    private fun setMemoAddScreen(
        viewModels: MemoAddScreenViewModels,
        navigateToTagAdd: () -> Unit = {},
    ) {
        composeRule.setContent {
            MemoAddScreenTestTheme {
                MemoAddScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addViewModel = viewModels.viewModel,
                    tagViewModel = viewModels.tagViewModel,
                    webViewModel = viewModels.webViewModel,
                    contactViewModel = viewModels.contactViewModel,
                    placeViewModel = viewModels.placeViewModel,
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    initialDateRange = null,
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    private companion object {
        private const val PICKER_TAG_COUNT: Int = 30
        private const val PICKER_TAG_TITLE_PREFIX: String = "MemoTagPicker"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenTagRequestTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-042 TagDetail 메모 탭에서 진입하면 대상 태그를 대표 태그로 표시한다`() {
        val initialPrimaryTag = testTag(title = INITIAL_PRIMARY_TAG_TITLE)

        setMemoAddScreen(
            viewModels =
                screenTestRealViewModel(
                    initialPrimaryTagId = initialPrimaryTag.id,
                    tagList = listOf(initialPrimaryTag),
                ),
        )

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(INITIAL_PRIMARY_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    fun `TagDetail 메모 탭에서 진입한 대상 태그를 메모 추가 요청에 전달한다`() {
        val initialPrimaryTag = testTag(title = INITIAL_PRIMARY_TAG_TITLE)
        val addMemoUseCase = successAddMemoUseCase()
        setMemoAddScreen(
            viewModels =
                screenTestRealViewModel(
                    initialPrimaryTagId = initialPrimaryTag.id,
                    tagList = listOf(initialPrimaryTag),
                    addMemoUseCase = addMemoUseCase,
                ),
        )

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) {
            addMemoUseCase(
                match<AddMemoUseCase.Parameter> { parameter ->
                    parameter.primaryTagId == initialPrimaryTag.id &&
                        parameter.tagIdSet == setOf(initialPrimaryTag.id)
                },
            )
        }
    }

    @Test
    @Config(qualifiers = "w480dp-h1200dp")
    fun `TC-MEMO-ADD-FEATURE-028 추가에 성공해도 선택한 태그와 대표 태그 지정이 유지된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        setMemoAddScreen(
            viewModels =
                screenTestRealViewModel(
                    tagList = tagList,
                    addMemoUseCase = successAddMemoUseCase(),
                ),
        )

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[1].performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "w480dp-h1200dp")
    fun `선택한 태그와 대표 태그를 추가 요청에 전달한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val addMemoUseCase = successAddMemoUseCase()
        setMemoAddScreen(
            viewModels =
                screenTestRealViewModel(
                    tagList = tagList,
                    addMemoUseCase = addMemoUseCase,
                ),
        )

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) {
            addMemoUseCase(
                match<AddMemoUseCase.Parameter> { parameter ->
                    parameter.primaryTagId == tagList.first().id &&
                        parameter.tagIdSet == setOf(tagList.first().id, tagList.last().id)
                },
            )
        }
    }

    private fun setMemoAddScreen(
        viewModels: MemoAddScreenViewModels,
        navigateToTagAdd: () -> Unit = {},
        navigateToTagDetail: (Uuid) -> Unit = {},
        navigateToPlaceAdd: (Coordinate?) -> Unit = {},
        navigateToPlaceDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setContent {
            MemoAddScreenTestTheme {
                MemoAddScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addViewModel = viewModels.viewModel,
                    tagViewModel = viewModels.tagViewModel,
                    webViewModel = viewModels.webViewModel,
                    contactViewModel = viewModels.contactViewModel,
                    placeViewModel = viewModels.placeViewModel,
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = navigateToTagDetail,
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = navigateToPlaceAdd,
                    navigateToPlaceDetail = navigateToPlaceDetail,
                    initialDateRange = null,
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    public companion object {
        private const val TYPED_TITLE = "MemoTitleInput"
        private const val INITIAL_PRIMARY_TAG_TITLE = "InitialPrimaryTag"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"

        private fun successAddMemoUseCase(): AddMemoUseCase {
            val addMemoUseCase = mockk<AddMemoUseCase>()
            coEvery { addMemoUseCase(any<AddMemoUseCase.Parameter>()) } returns Result.success(Uuid.random())

            return addMemoUseCase
        }
    }
}
