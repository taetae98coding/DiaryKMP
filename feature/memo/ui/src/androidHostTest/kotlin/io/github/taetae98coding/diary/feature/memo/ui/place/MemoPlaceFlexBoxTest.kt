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
        val otherPlace = testPlace(title = OFFICE_PLACE_TITLE)
        val clickedIdList = mutableListOf<Uuid>()
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        mapState.moveTo(place.detail.coordinate.toDiaryMapCoordinate())
        setFlexBox(placeList = listOf(place, otherPlace), mapState = mapState, onPlaceClick = clickedIdList::add)

        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        clickedIdList shouldBe listOf(place.id)
        mapState.coordinate shouldBe place.detail.coordinate.toDiaryMapCoordinate()
        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-ee21ac92-5dbf-4583-b9a0-109c65de50b4 카드의 장소 목록에서 같은 장소를 두 번 누르면 처음에는 지도를 옮기고 다음에는 상세 이동을 요청한다`() {
        val place = testPlace(title = HOME_PLACE_TITLE)
        val clickedIdList = mutableListOf<Uuid>()
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        mapState.moveTo(
            place.detail.coordinate
                .toDiaryMapCoordinate()
                .shifted(),
        )
        setFlexBox(placeList = listOf(place), mapState = mapState, onPlaceClick = clickedIdList::add)

        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        mapState.coordinate shouldBe place.detail.coordinate.toDiaryMapCoordinate()
        clickedIdList shouldBe emptyList()

        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        clickedIdList shouldBe listOf(place.id)
    }

    @Test
    fun `TC-662794b9-b18b-4787-aac5-ff399b42a925 지도 가운데에 있지 않은 장소를 누르면 지도가 그 장소로 이동하고 상세 이동은 요청하지 않는다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        val clickedIdList = mutableListOf<Uuid>()
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        mapState.moveTo(
            placeList
                .first()
                .detail.coordinate
                .toDiaryMapCoordinate()
                .shifted(),
        )
        setFlexBox(placeList = placeList, mapState = mapState, onPlaceClick = clickedIdList::add)

        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        mapState.coordinate shouldBe
            placeList
                .last()
                .detail.coordinate
                .toDiaryMapCoordinate()
        clickedIdList shouldBe emptyList()
    }

    @Test
    fun `TC-68786efe-067a-4b6b-9f6f-8a4eb581e7c4 지도를 다른 곳으로 옮긴 뒤 같은 장소를 다시 누르면 지도를 다시 그 좌표로 옮긴다`() {
        val place = testPlace(title = HOME_PLACE_TITLE)
        val clickedIdList = mutableListOf<Uuid>()
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        setFlexBox(placeList = listOf(place), mapState = mapState, onPlaceClick = clickedIdList::add)

        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.runOnIdle {
            mapState.moveTo(
                place.detail.coordinate
                    .toDiaryMapCoordinate()
                    .shifted(),
            )
        }
        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        mapState.coordinate shouldBe place.detail.coordinate.toDiaryMapCoordinate()
        clickedIdList shouldBe emptyList()
    }

    @Test
    fun `TC-900c168f-4d21-46d2-bc65-fe3e44cd6992 지도가 다른 위치를 가운데에 두고 있으면 목록의 장소를 눌러도 상세 이동을 요청하지 않고 장소가 그대로 남는다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        val clickedIdList = mutableListOf<Uuid>()
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        mapState.moveTo(
            placeList
                .first()
                .detail.coordinate
                .toDiaryMapCoordinate()
                .shifted(),
        )
        setFlexBox(placeList = placeList, mapState = mapState, onPlaceClick = clickedIdList::add)

        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        clickedIdList shouldBe emptyList()
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
