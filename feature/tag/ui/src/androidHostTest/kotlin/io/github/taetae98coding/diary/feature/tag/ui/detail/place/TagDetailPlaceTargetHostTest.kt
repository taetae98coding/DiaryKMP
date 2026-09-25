package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailPlaceTargetHostTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var placeState: TagDetailPlaceState
    private lateinit var mapState: DiaryMapState
    private var targetMarker: Uuid? = null

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-008 상세 대상이 다른 태그로 바뀌어도 지도 모드와 옮겨 둔 지도 위치를 유지한다`() {
        val fetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val movedCoordinate = fixtureMonkey.giveMeOne<Coordinate>().toDiaryMapCoordinate()
        var id by mutableStateOf(Uuid.random())
        setHost(idProvider = { id }, mapViewModel = mapViewModel(fetch))
        composeRule.runOnIdle { placeState.toggleViewMode() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { mapState.moveTo(movedCoordinate) }
        val beforeMapState = mapState
        val beforeMarker = targetMarker

        composeRule.runOnIdle { id = Uuid.random() }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            placeState.viewMode shouldBe TagDetailPlaceViewMode.MAP
            mapState shouldBeSameInstanceAs beforeMapState
            mapState.coordinate shouldBe movedCoordinate
            (targetMarker == beforeMarker) shouldBe false
        }
        coVerify(exactly = 1) { fetch(parameter = Unit) }
    }

    @Test
    fun `상세 대상이 그대로면 대상마다 기억하는 상태도 그대로 유지한다`() {
        val fetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val id = Uuid.random()
        var recomposeTrigger by mutableStateOf(0)
        setHost(idProvider = { recomposeTrigger.let { id } }, mapViewModel = mapViewModel(fetch))
        val beforeMarker = targetMarker

        composeRule.runOnIdle { recomposeTrigger += 1 }
        composeRule.waitForIdle()

        composeRule.runOnIdle { targetMarker shouldBe beforeMarker }
    }

    // Robolectric은 지도 SDK의 뷰를 불러오지 못하므로 탭 본문 대신 보기 모드, 현재 위치 확인, 지도 상태를 정하는 부분만 구성한다.
    private fun setHost(
        idProvider: () -> Uuid,
        mapViewModel: TagDetailPlaceMapViewModel,
    ) {
        composeRule.setContent {
            TagDetailPlaceTargetHost(
                id = idProvider(),
                placeMapViewModel = mapViewModel,
            ) { state, placeMapState ->
                placeState = state
                mapState = placeMapState
                targetMarker = rememberSaveable { Uuid.random() }
                TagDetailPlaceFetchCurrentLocationEffect(mapViewModel = mapViewModel, state = state)
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun fetchCurrentLocationUseCase(coordinate: Coordinate): FetchCurrentLocationUseCase {
            val useCase = mockk<FetchCurrentLocationUseCase>()
            coEvery { useCase(parameter = Unit) } returns Result.success(coordinate)

            return useCase
        }

        private fun mapViewModel(fetchCurrentLocationUseCase: FetchCurrentLocationUseCase): TagDetailPlaceMapViewModel {
            val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
            every { getDefaultMapProviderUseCase(parameter = Unit) } returns flowOf(Result.success(MapProvider.NAVER))

            return TagDetailPlaceMapViewModel(
                fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
            )
        }
    }
}
