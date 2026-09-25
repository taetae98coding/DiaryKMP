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
    fun `TC-PLACE-HOME-FEATURE-024 TC-TAG-DETAIL-PLACE-FEATURE-026 목록의 장소가 좌표와 컬러와 제목을 가진 핀으로 지정된다`() {
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
    fun `TC-PLACE-HOME-FEATURE-025 TC-TAG-DETAIL-PLACE-FEATURE-025 목록이 갱신되면 핀도 같은 장소들로 함께 갱신된다`() {
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
    fun `TC-PLACE-HOME-FEATURE-026 목록이 비어 있으면 핀도 없다`() {
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

        // FixtureMonkey가 Instant를 생성하지 못하므로 장소는 직접 만든다.
        private fun place(): Place =
            Place(
                id = Uuid.random(),
                detail = fixtureMonkey.giveMeOne<PlaceDetail>(),
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
    }
}
