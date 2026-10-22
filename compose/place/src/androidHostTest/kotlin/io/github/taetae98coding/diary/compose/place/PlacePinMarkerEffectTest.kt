package io.github.taetae98coding.diary.compose.place

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlacePinMarkerEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-c4fdf19e-6b46-4b2f-9cb8-cb7a20e2e956 TC-18256c1a-157c-41e3-8fee-e9d9eed3dbfa 목록의 장소가 좌표와 컬러와 제목을 가진 핀으로 지정된다`() {
        val place = place()
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        setEffect(mapState = mapState, placeListFlow = MutableStateFlow(listOf(place)))

        val pin = mapState.pins.single()

        pin.id shouldBe place.id
        pin.coordinate shouldBe place.detail.coordinate.toDiaryMapCoordinate()
        pin.color shouldBe place.detail.color.toColor()
        pin.label shouldBe place.detail.title
    }

    @Test
    fun `TC-e6f37e09-9cae-466d-83a6-e60a43d2a81a TC-bfb27e9f-3201-4ee7-861f-fd9c26ee3584 목록이 갱신되면 핀도 같은 장소들로 함께 갱신된다`() {
        val firstPlace = place()
        val secondPlace = place()
        val placeListFlow = MutableStateFlow(listOf(firstPlace))
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        setEffect(mapState = mapState, placeListFlow = placeListFlow)
        mapState.pins.map { pin -> pin.id } shouldBe listOf(firstPlace.id)

        composeRule.runOnIdle { placeListFlow.value = listOf(secondPlace) }
        composeRule.waitForIdle()

        mapState.pins.map { pin -> pin.id } shouldBe listOf(secondPlace.id)
    }

    @Test
    fun `TC-19028440-2ccf-442f-894d-5d3255537382 목록이 비어 있으면 핀도 없다`() {
        val placeListFlow = MutableStateFlow(listOf(place()))
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        setEffect(mapState = mapState, placeListFlow = placeListFlow)

        composeRule.runOnIdle { placeListFlow.value = emptyList() }
        composeRule.waitForIdle()

        mapState.pins shouldBe emptyList()
    }

    private fun setEffect(
        mapState: DiaryMapState,
        placeListFlow: MutableStateFlow<List<Place>>,
    ) {
        composeRule.setContent {
            val placeList by placeListFlow.collectAsStateWithLifecycle()

            PlacePinMarkerEffect(
                mapState = mapState,
                placeListProvider = { placeList },
            )
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(): Place =
            Place(
                id = Uuid.random(),
                detail = fixtureMonkey.giveMeOne<PlaceDetail>(),
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
    }
}
