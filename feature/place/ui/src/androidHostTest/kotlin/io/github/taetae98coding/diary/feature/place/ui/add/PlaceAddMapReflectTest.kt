package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.ReflectCoordinateEffect
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.feature.place.ui.toCoordinateText
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

/**
 * 지도 제공자의 실제 표시 요소는 만들 수 없으므로, 화면이 지도 기능에 지정하는 지점과 지도 위치로 반영 결과를 확인한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddMapReflectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-ADD-DOMAIN-012 지도에서 고른 좌표는 기다리지 않고 곧바로 반영한다`() {
        composeRule.mainClock.autoAdvance = false
        val selectedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val state = setReflectCoordinate(initialCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())

        composeRule.write { state().selectSpotOnMap(selectedCoordinate) }

        composeRule.runOnIdle {
            state().latitudeState.text.toString() shouldBe selectedCoordinate.latitude.toCoordinateText()
            state().longitudeState.text.toString() shouldBe selectedCoordinate.longitude.toCoordinateText()
            state().mapState.spot shouldBe selectedCoordinate
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-020 좌표를 직접 입력하면 지점 표시와 지도가 그 좌표로 옮겨진다`() {
        composeRule.mainClock.autoAdvance = false
        val typedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val state = setReflectCoordinate(initialCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())

        composeRule.write { state().typeCoordinate(typedCoordinate) }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)

        composeRule.runOnIdle {
            state().mapState.spot shouldBe typedCoordinate
            state().mapState.coordinate shouldBe typedCoordinate
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-021 성립하지 않는 좌표를 입력하면 지점 표시가 사라지고 지도는 옮기지 않는다`() {
        composeRule.mainClock.autoAdvance = false
        val selectedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val state = setReflectCoordinate(initialCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())

        listOf("", "abc", "90.1").forEach { latitude ->
            composeRule.write { state().selectSpotOnMap(selectedCoordinate) }
            composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)
            composeRule.runOnIdle { state().mapState.spot shouldBe selectedCoordinate }
            val cameraBefore = composeRule.runOnIdle { state().mapState.coordinate }

            composeRule.write { state().latitudeState.setTextAndPlaceCursorAtEnd(latitude) }
            composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)

            composeRule.runOnIdle {
                withClue("위도=$latitude") {
                    state().mapState.spot.shouldBeNull()
                    state().mapState.coordinate shouldBe cameraBefore
                }
            }
        }
    }

    @Test
    fun `TC-PLACE-ADD-DOMAIN-010 직접 입력한 좌표는 입력이 멈춘 뒤 지도에 반영한다`() {
        composeRule.mainClock.autoAdvance = false
        val typedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val state = setReflectCoordinate(initialCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())

        composeRule.write { state().typeCoordinate(typedCoordinate) }
        // 테스트 시계는 프레임 단위로 올려 진행하므로 대기 시간의 절반에서 반영 전을 확인한다.
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS / 2)

        composeRule.runOnIdle { state().mapState.spot.shouldBeNull() }

        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS)

        composeRule.runOnIdle { state().mapState.spot shouldBe typedCoordinate }
    }

    @Test
    fun `TC-PLACE-ADD-DOMAIN-011 기다리는 동안 다시 입력하면 마지막으로 입력한 좌표만 반영한다`() {
        composeRule.mainClock.autoAdvance = false
        val typedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val changedCoordinate =
            generateSequence { fixtureMonkey.mapCoordinateInFormPrecision() }
                .first { coordinate -> coordinate.latitude != typedCoordinate.latitude }
                .copy(longitude = typedCoordinate.longitude)
        val state = setReflectCoordinate(initialCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())

        composeRule.write { state().typeCoordinate(typedCoordinate) }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS / 2)
        composeRule.write { state().latitudeState.setTextAndPlaceCursorAtEnd(changedCoordinate.latitude.toCoordinateText()) }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS - REFLECT_DELAY_MILLIS / 4)

        composeRule.runOnIdle { state().mapState.spot.shouldBeNull() }

        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS)

        composeRule.runOnIdle { state().mapState.spot shouldBe changedCoordinate }
    }

    @Test
    fun `TC-PLACE-ADD-DOMAIN-015 넘겨받은 지도 위치를 초기 지도 위치로 지정한다`() {
        val initialCoordinateList = List(2) { fixtureMonkey.mapCoordinateInFormPrecision() }
        val stateList = setPlaceAddFormStateList(initialCoordinateList = initialCoordinateList)

        composeRule.runOnIdle {
            stateList().map { state -> state.mapState.coordinate } shouldBe initialCoordinateList
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-001 넘겨받은 지도 위치가 있어도 위도와 경도는 비어 있고 지점 표시가 없다`() {
        composeRule.mainClock.autoAdvance = false
        val initialCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val state = setReflectCoordinate(initialCoordinate = initialCoordinate)

        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)

        composeRule.runOnIdle {
            state().latitudeState.text.toString() shouldBe ""
            state().longitudeState.text.toString() shouldBe ""
            state().mapState.spot.shouldBeNull()
            state().mapState.coordinate shouldBe initialCoordinate
        }
    }

    @Test
    fun `TC-PLACE-ADD-DOMAIN-016 넘겨받은 지도 위치가 없으면 초기 위치를 지정하지 않는다`() {
        val stateList = setPlaceAddFormStateList(initialCoordinateList = listOf(null))

        composeRule.runOnIdle {
            stateList()
                .single()
                .mapState.coordinate
                .shouldBeNull()
        }
    }

    @Test
    fun `TC-PLACE-ADD-DOMAIN-027 표시 중에 기본 지도가 바뀌면 바뀐 지도로 새로 시작하고 입력을 유지한다`() {
        composeRule.mainClock.autoAdvance = false
        val initialCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val typedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        var defaultProvider by mutableStateOf(MapProvider.NAVER)
        lateinit var state: PlaceFormState

        composeRule.setContent {
            DiaryTheme {
                state =
                    rememberPlaceAddFormState(
                        defaultProvider = defaultProvider,
                        initialCoordinate = initialCoordinate,
                    )
                ReflectCoordinateEffect(state = state)
            }
        }
        composeRule.write {
            state.titleState.setText(TYPED_TITLE)
            state.descriptionState.setText(TYPED_DESCRIPTION)
            state.addressState.setTextAndPlaceCursorAtEnd(TYPED_ADDRESS)
            state.typeCoordinate(typedCoordinate)
        }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)
        val colorBefore = composeRule.runOnIdle { state.detail.color }

        composeRule.write { defaultProvider = MapProvider.GOOGLE }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS / 2)

        composeRule.runOnIdle {
            state.mapState.provider shouldBe DiaryMapProvider.GOOGLE
            state.mapState.coordinate shouldBe initialCoordinate
            state.mapState.spot.shouldBeNull()
            state.detail.title shouldBe TYPED_TITLE
            state.detail.description shouldBe TYPED_DESCRIPTION
            state.detail.color shouldBe colorBefore
            state.detail.address shouldBe TYPED_ADDRESS
            state.latitudeState.text.toString() shouldBe typedCoordinate.latitude.toCoordinateText()
            state.longitudeState.text.toString() shouldBe typedCoordinate.longitude.toCoordinateText()
        }

        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS)

        composeRule.runOnIdle {
            state.mapState.spot shouldBe typedCoordinate
            state.mapState.coordinate shouldBe typedCoordinate
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-025 화면이 재생성되어도 고른 지점과 보고 있던 지도를 유지한다`() {
        val initialCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val selectedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val movedCoordinate = generateSequence { fixtureMonkey.mapCoordinateInFormPrecision() }.first { coordinate -> coordinate != selectedCoordinate }
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: PlaceFormState

        restorationTester.setContent {
            DiaryTheme {
                state =
                    rememberPlaceAddFormState(
                        defaultProvider = MapProvider.NAVER,
                        initialCoordinate = initialCoordinate,
                    )
            }
        }
        composeRule.write {
            state.selectSpotOnMap(selectedCoordinate)
            state.mapState.select(DiaryMapProvider.GOOGLE)
            // 기능 화면은 사용자의 지도 조작을 만들 수 없으므로, 지도가 고른 지점과 다른 곳을 보고 있는 상태를 이동 지정으로 만든다.
            state.mapState.moveTo(movedCoordinate)
        }
        val latitudeBefore = composeRule.runOnIdle { state.latitudeState.text.toString() }
        val longitudeBefore = composeRule.runOnIdle { state.longitudeState.text.toString() }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            state.latitudeState.text.toString() shouldBe latitudeBefore
            state.longitudeState.text.toString() shouldBe longitudeBefore
            state.mapState.spot shouldBe selectedCoordinate
            state.mapState.coordinate shouldBe movedCoordinate
            state.mapState.provider shouldBe DiaryMapProvider.GOOGLE
        }
    }

    private fun setReflectCoordinate(initialCoordinate: DiaryMapCoordinate): () -> PlaceFormState {
        lateinit var state: PlaceFormState

        composeRule.setContent {
            DiaryTheme {
                state =
                    rememberPlaceAddFormState(
                        defaultProvider = MapProvider.NAVER,
                        initialCoordinate = initialCoordinate,
                    )
                ReflectCoordinateEffect(state = state)
            }
        }

        return { state }
    }

    private fun setPlaceAddFormStateList(initialCoordinateList: List<DiaryMapCoordinate?>): () -> List<PlaceFormState> {
        val stateList = mutableListOf<PlaceFormState>()

        composeRule.setContent {
            DiaryTheme {
                stateList.clear()
                initialCoordinateList.forEachIndexed { index, initialCoordinate ->
                    key(index) {
                        stateList +=
                            rememberPlaceAddFormState(
                                defaultProvider = MapProvider.NAVER,
                                initialCoordinate = initialCoordinate,
                            )
                    }
                }
            }
        }

        return { stateList.toList() }
    }

    // 화면에 입력 칸을 두지 않으므로, 프레임이 상태 변경을 알리는 것과 같게 변경 직후 알림을 보낸다.
    private fun ComposeContentTestRule.write(action: () -> Unit) {
        runOnIdle {
            action()
            Snapshot.sendApplyNotifications()
        }
    }

    private fun PlaceFormState.typeCoordinate(coordinate: DiaryMapCoordinate) {
        latitudeState.setTextAndPlaceCursorAtEnd(coordinate.latitude.toCoordinateText())
        longitudeState.setTextAndPlaceCursorAtEnd(coordinate.longitude.toCoordinateText())
    }

    private companion object {
        private const val REFLECT_DELAY_MILLIS = 200L
    }
}
