package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.detail.isRefreshingFlow
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
class TagDetailPlaceBoundsListTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-025 보이는 영역 안의 장소만 지도 모드의 목록에 표시한다`() {
        val place = tagPlace(title = FIRST_TITLE)
        val uiStateFlow = MutableStateFlow(TagDetailPlaceListUiState(isLoaded = true, placeList = listOf(place)))
        setBoundsList(uiStateFlow)

        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()

        composeRule.runOnIdle { uiStateFlow.value = TagDetailPlaceListUiState(isLoaded = true, placeList = emptyList()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-005 지도 모드의 목록에서 장소를 선택하면 그 장소의 상세 이동을 요청한다`() {
        val place = tagPlace(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailPlaceContentEvent>()
        setBoundsList(
            MutableStateFlow(TagDetailPlaceListUiState(isLoaded = true, placeList = listOf(place))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(TagDetailPlaceContentEvent.ClickPlace(id = place.id))
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-028 지도 모드에서 보이는 영역 안에 장소가 없으면 이 지역에 장소가 없음을 알린다`() {
        setBoundsList(MutableStateFlow(TagDetailPlaceListUiState(isLoaded = true, placeList = emptyList())))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_MAP_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_MAP_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-PLACE-FEATURE-028 한국어 환경에서 지도 모드 빈 상태 안내는 이 지역에 장소가 없습니다이다`() {
        setBoundsList(MutableStateFlow(TagDetailPlaceListUiState(isLoaded = true, placeList = emptyList())))

        composeRule.onNodeWithText(KOREAN_MAP_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_MAP_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-029 보이는 영역을 확인하기 전에는 빈 상태 안내를 표시하지 않는다`() {
        setBoundsList(MutableStateFlow(TagDetailPlaceListUiState()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-030 보이는 영역 안에 장소가 들어오면 빈 상태 안내가 사라진다`() {
        val place = tagPlace(title = FIRST_TITLE)
        val uiStateFlow = MutableStateFlow(TagDetailPlaceListUiState(isLoaded = true, placeList = emptyList()))
        setBoundsList(uiStateFlow)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.runOnIdle { uiStateFlow.value = TagDetailPlaceListUiState(isLoaded = true, placeList = listOf(place)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-016 지도 모드의 목록을 당기면 새로고침을 요청한다`() {
        val place = tagPlace(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailPlaceContentEvent>()
        setBoundsList(
            MutableStateFlow(TagDetailPlaceListUiState(isLoaded = true, placeList = listOf(place))),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(TAG_DETAIL_PLACE_BOUNDS_LIST_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailPlaceContentEvent.Refresh)
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-016 지도 모드의 빈 상태에서도 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailPlaceContentEvent>()
        setBoundsList(
            MutableStateFlow(TagDetailPlaceListUiState(isLoaded = true, placeList = emptyList())),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailPlaceContentEvent.Refresh)
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-FEATURE-016 지도 모드에서 새로고침이 진행 중이면 진행 표시가 나타난다`() {
        val place = tagPlace(title = FIRST_TITLE)
        setBoundsList(
            MutableStateFlow(TagDetailPlaceListUiState(isLoaded = true, placeList = listOf(place))),
            isRefreshingFlow = MutableStateFlow(true),
        )

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    private fun setBoundsList(
        uiStateFlow: MutableStateFlow<TagDetailPlaceListUiState>,
        isRefreshingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false),
        onEvent: (TagDetailPlaceContentEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            val uiState by uiStateFlow.collectAsStateWithLifecycle()
            val isRefreshing by isRefreshingFlow.collectAsStateWithLifecycle()

            DiaryTheme {
                TagDetailPlaceBoundsList(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    placeListUiStateProvider = { uiState },
                    isRefreshingProvider = { isRefreshing },
                )
            }
        }
    }

    private companion object {
        const val FIRST_TITLE = "AlphaBoundsPlace"
        const val DEFAULT_MAP_EMPTY_TITLE = "No places in this area"
        const val DEFAULT_MAP_EMPTY_DESCRIPTION = "Move the map to see other places."
        const val KOREAN_MAP_EMPTY_TITLE = "이 지역에 장소가 없습니다"
        const val KOREAN_MAP_EMPTY_DESCRIPTION = "지도를 옮기면 다른 장소를 볼 수 있습니다"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
