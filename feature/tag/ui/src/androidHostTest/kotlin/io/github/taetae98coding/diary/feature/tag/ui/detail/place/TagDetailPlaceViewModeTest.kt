package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.tag.ui.detail.placePagingDataFlow
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagPlace
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailPlaceViewModeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-020 장소 탭을 선택하면 목록 모드로 시작한다`() {
        val place = tagPlace(title = PLACE_TITLE)
        setPlaceTab(pagingData = tagEntityPagingData(itemList = listOf(place)))

        composeRule.onNodeWithText(PLACE_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `목록 모드에서 보기 모드 전환 버튼을 길게 누르면 접근성 이름과 같은 설명을 표시한다`() {
        setPlaceTab(pagingData = tagEntityPagingData(itemList = listOf(tagPlace(title = PLACE_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).performTouchInput { longClick() }

        composeRule.onNodeWithText(DEFAULT_SHOW_MAP_DESCRIPTION).assertExists()
    }

    @Test
    fun `지도 모드에서 보기 모드 전환 버튼을 길게 누르면 접근성 이름과 같은 설명을 표시한다`() {
        setPlaceTab(
            state = TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.MAP),
            pagingData = tagEntityPagingData(itemList = listOf(tagPlace(title = PLACE_TITLE))),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).performTouchInput { longClick() }

        composeRule.onNodeWithText(DEFAULT_SHOW_LIST_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-021 보기 모드를 바꾸면 목록 모드의 목록이 사라지고 전환 컨트롤이 목록으로 보기를 가리킨다`() {
        val place = tagPlace(title = PLACE_TITLE)
        setPlaceTab(pagingData = tagEntityPagingData(itemList = listOf(place)))

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(PLACE_TITLE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-022 지도 모드에서 다시 전환하면 목록 모드의 목록이 나타난다`() {
        val place = tagPlace(title = PLACE_TITLE)
        setPlaceTab(
            state = TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.MAP),
            pagingData = tagEntityPagingData(itemList = listOf(place)),
        )
        composeRule.onNodeWithText(PLACE_TITLE).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(PLACE_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-023 지도가 표시되지 않는 지도 모드에서 전환하면 목록 모드로 바뀌고 장소 전체가 목록에 표시된다`() {
        val place = tagPlace(title = PLACE_TITLE)
        setPlaceTab(
            state = TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.MAP),
            pagingData = tagEntityPagingData(itemList = listOf(place)),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).assert(hasClickAction())

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(PLACE_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-023 지도가 표시되지 않는 목록 모드에서 전환하면 지도 모드로 바뀌고 지도와 장소 목록 영역이 모두 표시되지 않는다`() {
        val place = tagPlace(title = PLACE_TITLE)
        setPlaceTab(
            pagingData = tagEntityPagingData(itemList = listOf(place)),
            placeListUiState = TagDetailPlaceListUiState(isLoaded = true, placeList = listOf(place)),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).assert(hasClickAction())

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithTag(TAG_DETAIL_PLACE_LIST_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(TAG_DETAIL_PLACE_BOUNDS_LIST_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-024 지도를 확인하기 전에는 지도 모드의 목록과 빈 상태를 표시하지 않는다`() {
        setPlaceTab(
            state = TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.MAP),
            pagingData = tagEntityPagingData(itemList = listOf(tagPlace(title = PLACE_TITLE))),
            placeListUiState = TagDetailPlaceListUiState(isLoaded = true, placeList = emptyList()),
        )

        composeRule.onNodeWithText(PLACE_TITLE).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-006 화면이 회전하거나 창 크기가 바뀌어도 바꿔 둔 보기 모드를 유지한다`() {
        val place = tagPlace(title = PLACE_TITLE)
        val pagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = listOf(place)))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            ViewModeTestTagDetailPlaceTab(
                onEvent = {},
                state = rememberTagDetailPlaceState(),
                placePagingDataFlow = pagingDataFlow,
            )
        }
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(PLACE_TITLE).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(PLACE_TITLE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 보기 모드 전환 버튼은 이동할 모드를 알린다`() {
        setPlaceTab()

        composeRule.onNodeWithContentDescription(KOREAN_SHOW_MAP_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(KOREAN_SHOW_LIST_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DATA-009 보기 모드를 바꿔도 새로고침을 요청하지 않는다`() {
        val eventList = mutableListOf<TagDetailPlaceContentEvent>()
        setPlaceTab(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe emptyList()
    }

    private fun setPlaceTab(
        state: TagDetailPlaceState = TagDetailPlaceState(),
        pagingData: PagingData<Place> = tagEntityPagingData(itemList = emptyList()),
        placeListUiState: TagDetailPlaceListUiState = TagDetailPlaceListUiState(),
        onEvent: (TagDetailPlaceContentEvent) -> Unit = {},
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            ViewModeTestTagDetailPlaceTab(
                onEvent = onEvent,
                state = state,
                placePagingDataFlow = pagingDataFlow,
                placeListUiState = placeListUiState,
            )
        }
    }

    private companion object {
        const val PLACE_TITLE = "TagDetailPlaceViewMode"
    }
}
