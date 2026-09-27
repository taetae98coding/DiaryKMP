package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.ReflectCoordinateEffect
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.feature.place.ui.toCoordinateText
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

/**
 * 지도 제공자의 실제 표시 요소는 만들 수 없으므로 PlaceAdd 화면이 쓰는 입력 상태와 좌표 반영, 추가 결과 처리를 직접 구성한다.
 * 지도 누르기는 화면이 지도 누르기를 받았을 때 하는 일로 대신하고, 결과는 화면이 지도 기능에 지정한 지점과 지도 위치로 확인한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddFormEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-ADD-FEATURE-019 지도에서 고른 좌표로 장소를 추가하고 성공 피드백을 제공한다`() {
        val selectedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val addPlaceUseCase = mockk<AddPlaceUseCase>()
        coEvery { addPlaceUseCase(any()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
        val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
        every { getDefaultMapProviderUseCase(parameter = Unit) } returns emptyFlow()
        val viewModel =
            PlaceAddViewModel(
                addPlaceUseCase = addPlaceUseCase,
                getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
            )
        val state = setPlaceAddForm(effect = viewModel.effect)

        composeRule.write {
            state().titleState.setText(TYPED_TITLE)
            state().selectSpotOnMap(selectedCoordinate)
        }
        // 화면의 추가 버튼은 입력 중인 내용으로 추가를 요청하므로 같은 내용으로 추가를 실행한다.
        composeRule.runOnIdle { viewModel.add(detail = state().detail, tagIdSet = emptySet()) }
        composeRule.waitForIdle()

        coVerify(exactly = 1) {
            addPlaceUseCase(
                match { parameter ->
                    parameter.detail.coordinate == Coordinate(latitude = selectedCoordinate.latitude, longitude = selectedCoordinate.longitude)
                },
            )
        }
        composeRule.runOnIdle {
            state()
                .hostState.currentSnackbarData
                ?.visuals
                ?.message shouldBe DEFAULT_ADD_SUCCEEDED_MESSAGE
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-022 추가에 성공하면 지점 표시만 사라지고 지도 위치는 유지된다`() {
        val selectedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val viewedCoordinate = generateSequence { fixtureMonkey.mapCoordinateInFormPrecision() }.first { coordinate -> coordinate != selectedCoordinate }
        val effectChannel = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)
        val state = setPlaceAddForm(effect = effectChannel.receiveAsFlow())

        composeRule.write {
            state().selectSpotOnMap(selectedCoordinate)
            // 기능 화면은 사용자의 지도 조작을 만들 수 없으므로, 지도가 고른 지점과 다른 곳을 보는 상태를 이동 지정으로 만든다.
            state().mapState.moveTo(viewedCoordinate)
        }
        composeRule.runOnIdle { state().mapState.spot shouldBe selectedCoordinate }

        composeRule.runOnIdle { effectChannel.trySend(PlaceAddEffect.AddSucceeded(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            state().mapState.spot.shouldBeNull()
            state().mapState.coordinate shouldBe viewedCoordinate
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-041 좌표가 성립하지 않아도 지도가 보고 있는 위치를 옮기지 않는다`() {
        val selectedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val effectChannel = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)
        val state = setPlaceAddForm(effect = effectChannel.receiveAsFlow())

        composeRule.write { state().selectSpotOnMap(selectedCoordinate) }
        composeRule.waitForIdle()
        val cameraBefore = composeRule.runOnIdle { state().mapState.coordinate }

        composeRule.write { state().latitudeState.setTextAndPlaceCursorAtEnd(INVALID_LATITUDE_TEXT) }
        composeRule.runOnIdle { effectChannel.trySend(PlaceAddEffect.CoordinateInvalid) }
        composeRule.mainClock.advanceTimeBy(REFLECT_WAIT_MILLIS)
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            state().detail.coordinate.isRepresentable shouldBe false
            state().mapState.coordinate shouldBe cameraBefore
        }
    }

    @Test
    fun `TC-PLACE-ADD-DOMAIN-013 지도가 다른 위치를 보게 되어도 입력한 좌표는 바뀌지 않는다`() {
        val selectedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val viewedCoordinate = generateSequence { fixtureMonkey.mapCoordinateInFormPrecision() }.first { coordinate -> coordinate != selectedCoordinate }
        val state = setPlaceAddForm(effect = emptyFlow())

        composeRule.write { state().selectSpotOnMap(selectedCoordinate) }
        composeRule.waitForIdle()
        // 기능 화면은 지도가 알려오는 위치 변화를 만들 수 없으므로, 지도가 보는 위치가 바뀐 상태를 이동 지정으로 만든다.
        composeRule.write { state().mapState.moveTo(viewedCoordinate) }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            state().latitudeState.text.toString() shouldBe selectedCoordinate.latitude.toCoordinateText()
            state().longitudeState.text.toString() shouldBe selectedCoordinate.longitude.toCoordinateText()
            state().mapState.spot shouldBe selectedCoordinate
        }
    }

    private fun setPlaceAddForm(effect: Flow<PlaceAddEffect>): () -> PlaceFormState {
        lateinit var state: PlaceFormState

        composeRule.setContent {
            PlaceAddScreenTestTheme {
                state =
                    rememberPlaceAddFormState(
                        defaultProvider = MapProvider.NAVER,
                        initialCoordinate = fixtureMonkey.mapCoordinateInFormPrecision(),
                    )
                ReflectCoordinateEffect(state = state)
                AddEffect(effect = effect, scaffoldState = state)
            }
        }
        composeRule.waitForIdle()

        return { state }
    }

    // 화면에 입력 칸을 두지 않으므로, 프레임이 상태 변경을 알리는 것과 같게 변경 직후 알림을 보낸다.
    private fun ComposeContentTestRule.write(action: () -> Unit) {
        runOnIdle {
            action()
            Snapshot.sendApplyNotifications()
        }
    }

    private companion object {
        private const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Place added."
        private const val INVALID_LATITUDE_TEXT = "abc"

        // 입력 정지 대기 시간이 넉넉히 지나도록 기다린다.
        private const val REFLECT_WAIT_MILLIS = 400L
    }
}
