package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.activity.ComponentDialog
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.place.ui.sendTagAddedResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceAddTagAddTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-c6e8c411-91bc-4b0b-b38e-dbd5ac99fcb7 나타낼 태그가 있으면 태그 추가 항목이 태그 선택 목록을 연다`() {
        var tagAddCount = 0
        setPlaceAddScreen(tagList = listOf(placeTestTag(title = WORK_TAG_TITLE)), navigateToTagAdd = { tagAddCount += 1 })

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-6397d89f-db81-4ca1-bbe0-13d8857d9eed 목록의 태그 추가 항목을 누르면 목록이 닫히고 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setPlaceAddScreen(tagList = listOf(placeTestTag(title = WORK_TAG_TITLE)), navigateToTagAdd = { tagAddCount += 1 })
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-4efa8874-4b68-4a4f-a21d-6799efec18a2 TagAdd 화면에서 추가한 태그 하나가 돌아왔을 때 이전 연결과 함께 연결 대상이 된다`() {
        val linkedTag = placeTestTag(title = EXERCISE_TAG_TITLE)
        val addedTag = placeTestTag(title = WORK_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setPlaceAddScreen(tagList = listOf(linkedTag, addedTag), resultEventBus = resultEventBus)
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()
        dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.waitForIdle()
        closeDialogByBack()

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-4efa8874-4b68-4a4f-a21d-6799efec18a2 TagAdd 화면에서 추가한 태그 여러 개가 돌아왔을 때 모두 연결 대상이 된다`() {
        val firstAddedTag = placeTestTag(title = WORK_TAG_TITLE)
        val secondAddedTag = placeTestTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setPlaceAddScreen(tagList = listOf(firstAddedTag, secondAddedTag), resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = firstAddedTag.id)
        resultEventBus.sendTagAddedResult(id = secondAddedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-e0845b94-fdc3-47b2-a91e-e9da40b9785c 태그를 추가하지 않고 돌아오면 연결이 그대로 유지된다`() {
        val linkedTag = placeTestTag(title = WORK_TAG_TITLE)
        val otherTag = placeTestTag(title = EXERCISE_TAG_TITLE)
        setPlaceAddScreen(tagList = listOf(linkedTag, otherTag))
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-97f9154e-3aa5-4898-817e-522f1298f870 TagAdd 화면에서 돌아와도 태그 선택 목록이 저절로 열리지 않는다`() {
        val addedTag = placeTestTag(title = WORK_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setPlaceAddScreen(tagList = listOf(addedTag), resultEventBus = resultEventBus)
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-d4b6ec10-1460-4ebc-b556-2948ae5eb265 자동 선택된 태그의 연결을 해제하면 다시 선택되지 않는다`() {
        val addedTag = placeTestTag(title = WORK_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setPlaceAddScreen(tagList = listOf(addedTag), resultEventBus = resultEventBus)
        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()
        dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        closeDialogByBack()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-37b90455-d40a-4673-9405-c67eaebde0ee 이 입력에서 이동하지 않은 TagAdd 화면의 태그는 자동 선택되지 않는다`() {
        val addedTag = placeTestTag(title = WORK_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setPlaceAddScreen(tagList = listOf(addedTag), resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = addedTag.id, requestKey = OTHER_REQUEST_KEY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    private fun dialogNodeWithText(text: String): SemanticsNodeInteraction = composeRule.onNode(hasText(text) and hasAnyAncestor(isDialog()))

    @Test
    fun `TC-d875e7bb-5b14-4bf7-a15f-1189e73c3464 저장된 태그가 하나도 없어도 태그 추가 항목을 누르면 태그 선택 목록이 열린다`() {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(loadedTagPagingDataOf(emptyList())))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
        val tagViewModel = PlaceAddTagViewModel(initialTagId = null, pageTagUseCase = pageTagUseCase, getSelectedTagUseCase = getSelectedTagUseCase)
        var tagAddCount = 0
        setPlaceAddScreen(tagViewModel = tagViewModel, navigateToTagAdd = { tagAddCount += 1 })

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
        dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).assertExists()
    }

    private fun closeDialogByBack() {
        val dialog = ShadowDialog.getLatestDialog() as ComponentDialog

        composeRule.runOnUiThread { dialog.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setPlaceAddScreen(
        tagList: List<Tag> = emptyList(),
        navigateToTagAdd: () -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
        tagViewModel: PlaceAddTagViewModel =
            selectionTagViewModel(tagList = tagList, tagPagingData = MutableStateFlow(loadedTagPagingDataOf(tagList))),
    ) {
        composeRule.setContent {
            PlaceAddScreenTestTheme(resultEventBus = resultEventBus) {
                PlaceAddScreen(
                    addedResultRequestKey = null,
                    navigateUp = {},
                    navigateToTagAdd = navigateToTagAdd,
                    navigateToTagDetail = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    initialCoordinate = null,
                    addViewModel = screenTestViewModel(),
                    searchViewModel = searchScreenTestViewModel(),
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val WORK_TAG_TITLE: String = "PlaceAddTagAddWork"
        const val EXERCISE_TAG_TITLE: String = "PlaceAddTagAddExercise"
        const val DEFAULT_ENTITY_TAG_LABEL: String = "Select tag"
        const val DEFAULT_PICKER_TITLE: String = "Select Tag"
        const val DEFAULT_PICKER_TAG_ADD: String = "Add tag"
        val OTHER_REQUEST_KEY: Uuid = Uuid.parse("20000000-0000-0000-0000-000000000002")

        fun loadedTagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
            PagingData.from(
                data = tagList,
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
            )

        /**
         * 고른 태그를 보관하고 표시하는 동작만 남긴 태그 ViewModel을 만든다.
         */
        fun selectionTagViewModel(
            tagList: List<Tag>,
            tagPagingData: MutableStateFlow<PagingData<Tag>>,
        ): PlaceAddTagViewModel {
            val tagIdSet = MutableStateFlow(PlaceAddTagSelectionUiState())
            val uiState = MutableStateFlow(EntityTagInputUiState())

            fun reflect() {
                uiState.value = EntityTagInputUiState(tagList = tagList.filter { tag -> tag.id in tagIdSet.value.tagIdSet })
            }

            return mockk<PlaceAddTagViewModel>(relaxed = true).apply {
                every { this@apply.uiState } returns uiState
                every { this@apply.tagPagingData } returns tagPagingData
                every { this@apply.selectionUiState } returns tagIdSet
                every { add(id = any()) } answers {
                    tagIdSet.value = tagIdSet.value.copy(tagIdSet = tagIdSet.value.tagIdSet + firstArg<Uuid>())
                    reflect()
                }
                every { remove(id = any()) } answers {
                    tagIdSet.value = tagIdSet.value.copy(tagIdSet = tagIdSet.value.tagIdSet - firstArg<Uuid>())
                    reflect()
                }
            }
        }
    }
}
