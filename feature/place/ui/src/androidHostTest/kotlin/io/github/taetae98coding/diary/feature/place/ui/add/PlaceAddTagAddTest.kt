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
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.sendTagAddedResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
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

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-004 나타낼 태그가 있으면 태그 추가 항목이 태그 선택 목록을 연다`() {
        var tagAddCount = 0
        setPlaceAddScreen(tagList = listOf(placeTestTag(title = WORK_TAG_TITLE)), navigateToTagAdd = { tagAddCount += 1 })

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-024 나타낼 태그가 없으면 태그 추가 항목이 TagAdd 이동을 요청한다`() {
        var tagAddCount = 0
        setPlaceAddScreen(tagList = emptyList(), navigateToTagAdd = { tagAddCount += 1 })

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-025 목록의 대상을 확인하는 중에는 태그 추가 항목이 목록을 연다`() {
        var tagAddCount = 0
        setPlaceAddScreen(
            tagPagingData = MutableStateFlow(refreshingTagPagingData()),
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
        setPlaceAddScreen(tagList = listOf(placeTestTag(title = WORK_TAG_TITLE)), navigateToTagAdd = { tagAddCount += 1 })
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-028 TagAdd 화면에서 추가한 태그 하나가 돌아왔을 때 이전 연결과 함께 연결 대상이 된다`() {
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
    fun `TC-ENTITY-TAG-INPUT-FEATURE-028 TagAdd 화면에서 추가한 태그 여러 개가 돌아왔을 때 모두 연결 대상이 된다`() {
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
    fun `TC-ENTITY-TAG-INPUT-FEATURE-029 태그를 추가하지 않고 돌아오면 연결이 그대로 유지된다`() {
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
    fun `TC-ENTITY-TAG-INPUT-FEATURE-030 TagAdd 화면에서 돌아와도 태그 선택 목록이 저절로 열리지 않는다`() {
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
    fun `TC-ENTITY-TAG-INPUT-DOMAIN-014 자동 선택된 태그의 연결을 해제하면 다시 선택되지 않는다`() {
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
    fun `TC-ENTITY-TAG-INPUT-DOMAIN-015 이 입력에서 이동하지 않은 TagAdd 화면의 태그는 자동 선택되지 않는다`() {
        val addedTag = placeTestTag(title = WORK_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        setPlaceAddScreen(tagList = listOf(addedTag), resultEventBus = resultEventBus)

        resultEventBus.sendTagAddedResult(id = addedTag.id, requestKey = OTHER_REQUEST_KEY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    private fun dialogNodeWithText(text: String): SemanticsNodeInteraction = composeRule.onNode(hasText(text) and hasAnyAncestor(isDialog()))

    private fun closeDialogByBack() {
        val dialog = ShadowDialog.getLatestDialog() as ComponentDialog

        composeRule.runOnUiThread { dialog.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun setPlaceAddScreen(
        tagList: List<Tag> = emptyList(),
        tagPagingData: MutableStateFlow<PagingData<Tag>> = MutableStateFlow(loadedTagPagingDataOf(tagList)),
        navigateToTagAdd: () -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        val tagViewModel = selectionTagViewModel(tagList = tagList, tagPagingData = tagPagingData)

        composeRule.setContent {
            PlaceAddScreenTestTheme(resultEventBus = resultEventBus) {
                PlaceAddScreen(
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

        fun refreshingTagPagingData(): PagingData<Tag> =
            PagingData.from(
                data = emptyList(),
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.Loading,
                        prepend = LoadState.NotLoading(endOfPaginationReached = false),
                        append = LoadState.NotLoading(endOfPaginationReached = false),
                    ),
            )

        /**
         * 고른 태그를 보관하고 표시하는 동작만 남긴 태그 ViewModel을 만든다.
         */
        fun selectionTagViewModel(
            tagList: List<Tag>,
            tagPagingData: MutableStateFlow<PagingData<Tag>>,
        ): PlaceAddTagViewModel {
            val tagIdSet = MutableStateFlow(emptySet<Uuid>())
            val uiState = MutableStateFlow(EntityTagInputUiState())

            fun reflect() {
                uiState.value = EntityTagInputUiState(tagList = tagList.filter { tag -> tag.id in tagIdSet.value })
            }

            return mockk<PlaceAddTagViewModel>(relaxed = true).apply {
                every { this@apply.uiState } returns uiState
                every { this@apply.tagPagingData } returns tagPagingData
                every { this@apply.tagIdSet } returns tagIdSet
                every { add(id = any()) } answers {
                    tagIdSet.value += firstArg<Uuid>()
                    reflect()
                }
                every { remove(id = any()) } answers {
                    tagIdSet.value -= firstArg<Uuid>()
                    reflect()
                }
            }
        }
    }
}
