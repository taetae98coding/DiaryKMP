package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateText
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrContentFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrTextField
import io.github.taetae98coding.diary.feature.qr.ui.add.form.ReflectQrCoordinateEffect
import io.github.taetae98coding.diary.feature.qr.ui.add.form.WriteQrFieldsEffect
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrContentFormState
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 지도 제공자의 실제 표시 요소는 만들 수 없으므로, 화면이 지도 기능에 지정하는 지점과 지도 위치로 반영 결과를 확인한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class QrAddLocationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-QR-ADD-FEATURE-038 위치 포맷에서 지도로 위치를 고르면 위도와 경도가 채워지고 그 위치의 QR을 그린다`() {
        val state = setLocationState()
        val selected = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978)

        composeRule.write { state().selectSpotOnMap(selected) }

        composeRule.runOnIdle {
            state().fieldState(QrTextField.LATITUDE).text shouldBe selected.latitude.toPlaceCoordinateText()
            state().fieldState(QrTextField.LONGITUDE).text shouldBe selected.longitude.toPlaceCoordinateText()
            state().mapState.spot shouldBe selected
            state().qrValue shouldBe "geo:37.5665,126.978"
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-039 위치 포맷에서 지도로 다른 위치를 고르면 좌표가 새 위치로 바뀐다`() {
        val state = setLocationState()
        val first = DiaryMapCoordinate(latitude = 37.5, longitude = 127.0)
        val second = DiaryMapCoordinate(latitude = -33.868820, longitude = 151.209296)

        composeRule.write { state().selectSpotOnMap(first) }
        composeRule.write { state().selectSpotOnMap(second) }

        composeRule.runOnIdle {
            state().fieldState(QrTextField.LATITUDE).text shouldBe second.latitude.toPlaceCoordinateText()
            state().fieldState(QrTextField.LONGITUDE).text shouldBe second.longitude.toPlaceCoordinateText()
            state().mapState.spot shouldBe second
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-040 위치 포맷에서 좌표를 직접 입력하면 입력이 멈춘 뒤 지점과 지도가 그 좌표로 옮겨진다`() {
        composeRule.mainClock.autoAdvance = false
        val state = setLocationState()

        composeRule.write {
            state().fieldState(QrTextField.LATITUDE).setText("37.5")
            state().fieldState(QrTextField.LONGITUDE).setText("127")
        }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS / 2)
        composeRule.runOnIdle { state().mapState.spot.shouldBeNull() }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS)

        val expected = DiaryMapCoordinate(latitude = 37.5, longitude = 127.0)
        composeRule.runOnIdle {
            state().mapState.spot shouldBe expected
            state().mapState.coordinate shouldBe expected
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-041 위치 포맷에서 성립하지 않는 좌표를 입력하면 지점 표시가 사라진다`() {
        composeRule.mainClock.autoAdvance = false
        val state = setLocationState()
        val selected = DiaryMapCoordinate(latitude = 37.5, longitude = 127.0)

        listOf("", "abc", "90.1").forEach { latitude ->
            composeRule.write { state().selectSpotOnMap(selected) }
            composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)
            val cameraBefore = composeRule.runOnIdle { state().mapState.coordinate }

            composeRule.write { state().fieldState(QrTextField.LATITUDE).setText(latitude) }
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
    fun `TC-QR-ADD-FEATURE-062 위치 좌표를 담은 QR 값에서 위치 포맷을 고르면 반영을 기다린 뒤 지점과 지도가 그 좌표로 옮겨진다`() {
        composeRule.mainClock.autoAdvance = false
        val state = setContentState()
        composeRule.write { state().fieldState(QrTextField.TEXT).setText("geo:37.5,127") }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)

        composeRule.write { state().selectFormat(QrFormat.LOCATION) }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS / 2)
        composeRule.runOnIdle { state().mapState.spot.shouldBeNull() }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS)

        val expected = DiaryMapCoordinate(latitude = 37.5, longitude = 127.0)
        composeRule.runOnIdle {
            state().mapState.spot shouldBe expected
            state().mapState.coordinate shouldBe expected
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-042 기본 지도를 확인하지 못하면 지도 없이 좌표를 입력해 위치 QR을 추가한다`() {
        val eventList = mutableListOf<QrAddScaffoldEvent>()
        var state: QrAddFormState? = null
        composeRule.setContent {
            val formState = rememberQrAddFormState()
            state = formState

            DiaryTheme {
                QrAddScaffold(onEvent = { event -> eventList += event }, state = formState)
            }
        }
        composeRule.onTitleInput().performTextInput("title")
        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.write { checkNotNull(state).contentState.selectFormat(QrFormat.LOCATION) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(MAP_CONTENT_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(LOADING_TEXT).assertDoesNotExist()

        composeRule.onNode(hasSetTextAction() and hasText("Latitude")).performTextInput("37.5")
        composeRule.onNode(hasSetTextAction() and hasText("Longitude")).performTextInput("127")
        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrAddScaffoldEvent.ClickAdd)
        composeRule.runOnIdle { checkNotNull(state).detail.value shouldBe "geo:37.5,127" }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-043 위치 포맷의 지도는 저장된 기본 지도로 시작하고 초기 위치를 지정하지 않는다`() {
        val providerMap = mapOf(MapProvider.NAVER to DiaryMapProvider.NAVER, MapProvider.GOOGLE to DiaryMapProvider.GOOGLE)
        val stateMap = mutableMapOf<MapProvider, QrContentFormState>()
        composeRule.setContent {
            providerMap.keys.forEach { provider ->
                key(provider) {
                    stateMap[provider] = rememberQrContentFormState(defaultProvider = provider)
                }
            }
        }

        composeRule.runOnIdle {
            providerMap.forEach { (provider, expected) ->
                withClue("기본 지도=$provider") {
                    stateMap.getValue(provider).mapState.provider shouldBe expected
                    stateMap
                        .getValue(provider)
                        .mapState.coordinate
                        .shouldBeNull()
                }
            }
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-046 위치 포맷으로 추가에 성공하면 지점 표시만 사라지고 지도 위치는 유지된다`() {
        val effectChannel = Channel<QrAddEffect>(capacity = Channel.BUFFERED)
        var state: QrAddFormState? = null
        val selected = DiaryMapCoordinate(latitude = 37.5, longitude = 127.0)
        composeRule.setContent {
            val formState = rememberQrAddFormState(defaultProvider = MapProvider.NAVER)
            state = formState

            DiaryTheme {
                QrAddScreenEffect(effect = remember { effectChannel.receiveAsFlow() }, state = formState)
            }
        }
        composeRule.write {
            checkNotNull(state).contentState.selectFormat(QrFormat.LOCATION)
            checkNotNull(state).contentState.mapState.moveTo(selected)
            checkNotNull(state).contentState.selectSpotOnMap(selected)
        }
        val cameraBefore = composeRule.runOnIdle { checkNotNull(state).contentState.mapState.coordinate }

        composeRule.runOnIdle { effectChannel.trySend(QrAddEffect.AddSucceeded) }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            checkNotNull(state)
                .contentState.mapState.spot
                .shouldBeNull()
            checkNotNull(state).contentState.mapState.coordinate shouldBe cameraBefore
        }
    }

    @Test
    fun `TC-QR-ADD-DOMAIN-025 화면이 재생성되어도 위치 포맷의 고른 지점과 보고 있던 지도를 유지한다`() {
        var state: QrContentFormState? = null
        val selected = DiaryMapCoordinate(latitude = 37.5, longitude = 127.0)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            val contentState = rememberQrContentFormState(defaultProvider = MapProvider.NAVER)
            state = contentState
            ReflectQrCoordinateEffect(state = contentState)
        }
        composeRule.write {
            checkNotNull(state).selectFormat(QrFormat.LOCATION)
            checkNotNull(state).selectSpotOnMap(selected)
            checkNotNull(state).mapState.select(DiaryMapProvider.GOOGLE)
        }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            val restored = checkNotNull(state)
            restored.fieldState(QrTextField.LATITUDE).text shouldBe selected.latitude.toPlaceCoordinateText()
            restored.fieldState(QrTextField.LONGITUDE).text shouldBe selected.longitude.toPlaceCoordinateText()
            restored.mapState.spot shouldBe selected
            restored.mapState.provider shouldBe DiaryMapProvider.GOOGLE
        }
    }

    private fun setLocationState(defaultProvider: MapProvider = MapProvider.NAVER): () -> QrContentFormState {
        val state = setContentState(defaultProvider = defaultProvider)
        composeRule.write { state().selectFormat(QrFormat.LOCATION) }
        return state
    }

    private fun setContentState(defaultProvider: MapProvider = MapProvider.NAVER): () -> QrContentFormState {
        var state: QrContentFormState? = null
        composeRule.setContent {
            val contentState = rememberQrContentFormState(defaultProvider = defaultProvider)
            state = contentState
            ReflectQrCoordinateEffect(state = contentState)
            WriteQrFieldsEffect(state = contentState)
        }
        return { checkNotNull(state) }
    }

    private companion object {
        const val REFLECT_DELAY_MILLIS = 200L
        const val MAP_CONTENT_DESCRIPTION = "QR location map"
        const val ADD_DESCRIPTION = "Add QR code"
        const val LOADING_TEXT = "Loading"
    }
}
