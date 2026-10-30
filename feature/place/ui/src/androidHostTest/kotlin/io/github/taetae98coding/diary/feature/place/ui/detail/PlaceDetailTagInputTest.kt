package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagPickerEvent
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.add.placeTestTag
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.handlePlaceFormEvent
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import io.github.taetae98coding.diary.feature.place.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceDetailTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-7a4dc5e4-4acc-407c-9ca1-d01879fe7018 조회 중에는 태그 입력을 표시하지 않는다`() {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(PlaceDetailUiState.Loading)),
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-7a4dc5e4-4acc-407c-9ca1-d01879fe7018 조회가 끝나면 태그 입력을 표시한다`() {
        setPlaceDetailScreen()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-6e1e8318-ce9f-4318-898d-09657d185da4 태그를 연결해도 수정 동작의 제공 여부는 바뀌지 않는다`() {
        setPlaceDetailScreen(selectableTagList = listOf(placeTestTag(title = SELECTABLE_TAG_TITLE)))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-fe56377e-77fa-47cc-a518-f65eef34dcb1 연결한 태그를 누르면 그 태그의 상세 이동을 한 번 요청한다`() {
        val linkedTag = placeTestTag(title = LINKED_TAG_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setPlaceDetailScreen(
            tagList = listOf(linkedTag),
            navigateToTagDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(LINKED_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(linkedTag.id)
    }

    @Test
    fun `TC-80e48e65-0601-4073-9161-87db979bc146 태그를 연결해도 입력 중인 내용은 바뀌지 않는다`() {
        setPlaceDetailScreen(selectableTagList = listOf(placeTestTag(title = SELECTABLE_TAG_TITLE)))
        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.input(LATITUDE_INDEX).performTextReplacement(CHANGED_LATITUDE)
        composeRule.waitForIdle()

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(CHANGED_DESCRIPTION))
        composeRule.input(ADDRESS_INDEX).assert(hasText(CHANGED_ADDRESS))
        composeRule.input(LATITUDE_INDEX).assert(hasText(CHANGED_LATITUDE))
    }

    /**
     * 지도가 보고 있는 위치와 지점 표시는 화면 밖에서 읽을 수 없으므로, 화면과 같은 입력 상태를 넘겨 배치한 본문에서 태그를 연결하고 해제한 뒤 그 상태로 확인한다.
     */
    @Test
    fun `TC-1f48861e-d312-4334-8d4f-a251df532203 태그를 연결하거나 해제해도 지도가 보고 있는 위치와 지점 표시는 바뀌지 않는다`() {
        val detail = placeDetail(coordinate = fixtureMonkey.coordinateInFormPrecision())
        val savedCoordinate = detail.coordinate.toDiaryMapCoordinate()
        val tag = placeTestTag(title = SELECTABLE_TAG_TITLE)
        val tagUiState = mutableStateOf(EntityTagInputUiState())
        val state = setTagLinkScaffold(detail = detail, tag = tag, tagUiState = tagUiState)

        // 입력된 좌표는 입력이 멈춘 뒤에 지점 표시로 옮겨진다.
        composeRule.mainClock.advanceTimeBy(INPUT_IDLE_DELAY.inWholeMilliseconds * 2)
        composeRule.runOnIdle {
            state.mapState.coordinate shouldBe savedCoordinate
            state.mapState.spot shouldBe savedCoordinate
        }

        linkTag(title = SELECTABLE_TAG_TITLE)

        composeRule.runOnIdle {
            tagUiState.value.tagList shouldBe listOf(tag)
            state.mapState.coordinate shouldBe savedCoordinate
            state.mapState.spot shouldBe savedCoordinate
        }

        composeRule.onNode(hasText(SELECTABLE_TAG_TITLE) and isToggleable()).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            tagUiState.value.tagList shouldBe emptyList()
            state.mapState.coordinate shouldBe savedCoordinate
            state.mapState.spot shouldBe savedCoordinate
        }
    }

    private fun setTagLinkScaffold(
        detail: PlaceDetail,
        tag: Tag,
        tagUiState: MutableState<EntityTagInputUiState>,
    ): PlaceFormState {
        lateinit var state: PlaceFormState

        preparePlaceDetailTabViewModels()
        composeRule.setContent {
            PlaceDetailScreenTestTheme {
                state = rememberPlaceDetailFormState(initialDetail = detail)
                val tagPagingItems = remember { flowOf(PagingData.from(listOf(tag))) }.collectAsLazyPagingItems()

                PlaceDetailScaffold(
                    state = state,
                    onEvent = {},
                    onSearchEvent = {},
                    onTagPickerEvent = { event ->
                        when (event) {
                            is EntityTagPickerEvent.Add -> tagUiState.value = EntityTagInputUiState(tagList = listOf(tag))
                            is EntityTagPickerEvent.Remove -> tagUiState.value = EntityTagInputUiState()
                            else -> Unit
                        }
                    },
                    tagPagingItems = tagPagingItems,
                    uiStateProvider = { content(detail = detail) },
                    tagUiStateProvider = { tagUiState.value },
                    tabFloatingActionButton = {},
                ) {
                    PlaceDetailScaffoldContent(
                        onFormEvent = { event ->
                            handlePlaceFormEvent(
                                event = event,
                                state = state,
                                selectableTagPagingItems = tagPagingItems,
                                navigateToTagAdd = {},
                                navigateToTagDetail = {},
                            )
                        },
                        state = state,
                        uiStateProvider = { content(detail = detail) },
                        tagUiStateProvider = { tagUiState.value },
                    )
                }
            }
        }
        return state
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

    private fun setPlaceDetailScreen(
        tagList: List<Tag> = emptyList(),
        selectableTagList: List<Tag> = emptyList(),
        navigateToTagDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(),
            navigateToTagDetail = navigateToTagDetail,
            tagViewModel =
                detailTagScreenTestViewModel(
                    tagList = tagList,
                    selectableTagList = selectableTagList,
                ),
        )
        composeRule.waitForIdle()
    }

    private companion object {
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
        const val DEFAULT_ENTITY_TAG_LABEL = "Select tag"
        const val LINKED_TAG_TITLE = "PlaceDetailLinkedTag"
        const val SELECTABLE_TAG_TITLE = "PlaceDetailSelectableTag"
    }
}
