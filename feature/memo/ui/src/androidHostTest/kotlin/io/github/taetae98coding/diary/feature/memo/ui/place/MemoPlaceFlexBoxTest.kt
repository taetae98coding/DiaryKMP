package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoPlaceFlexBoxTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-4ee9454b-e782-4029-bc5b-4ac789fe6047 지도 가운데에 있는 장소를 목록에서 누르면 그 장소의 상세 이동을 한 번 요청한다`() {
        val place = testPlace(title = HOME_PLACE_TITLE)

        assertPlaceClickRequestsDetail(place = place, mapCoordinate = place.detail.coordinate.toDiaryMapCoordinate())
    }

    @Test
    fun `TC-4ee9454b-e782-4029-bc5b-4ac789fe6047 지도 가운데에 있지 않은 장소를 목록에서 눌러도 지도를 옮기지 않고 그 장소의 상세 이동을 한 번 요청한다`() {
        val place = testPlace(title = HOME_PLACE_TITLE)

        assertPlaceClickRequestsDetail(
            place = place,
            mapCoordinate =
                place.detail.coordinate
                    .toDiaryMapCoordinate()
                    .shifted(),
        )
    }

    private fun assertPlaceClickRequestsDetail(
        place: Place,
        mapCoordinate: DiaryMapCoordinate,
    ) {
        val otherPlace = testPlace(title = OFFICE_PLACE_TITLE)
        val clickedIdList = mutableListOf<Uuid>()
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        mapState.moveTo(mapCoordinate)
        setFlexBox(placeList = listOf(place, otherPlace), mapState = mapState, onPlaceClick = clickedIdList::add)

        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        clickedIdList shouldBe listOf(place.id)
        mapState.coordinate shouldBe mapCoordinate
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-16def837-7242-4e9f-ac6e-b05b1748d3a7 지도가 표시되지 않는 동안 장소를 눌러도 장소 상세 이동을 요청하지 않는다`() {
        val place = testPlace(title = HOME_PLACE_TITLE)
        val clickedIdList = mutableListOf<Uuid>()
        setFlexBox(placeList = listOf(place), mapState = null, onPlaceClick = clickedIdList::add)

        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        clickedIdList shouldBe emptyList()
    }

    private fun setFlexBox(
        placeList: List<Place>,
        mapState: DiaryMapState?,
        onPlaceClick: (Uuid) -> Unit,
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoPlaceFlexBox(
                    onPlaceClick = onPlaceClick,
                    onAddClick = {},
                    mapState = mapState,
                    placeListProvider = { placeList },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val SHIFT_DEGREE = 1.0

        private fun DiaryMapCoordinate.shifted(): DiaryMapCoordinate = copy(latitude = latitude + SHIFT_DEGREE, longitude = longitude + SHIFT_DEGREE)
    }
}
