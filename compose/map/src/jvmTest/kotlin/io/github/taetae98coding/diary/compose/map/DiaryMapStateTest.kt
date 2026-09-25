package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.map.provider.label
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class DiaryMapStateTest :
    FunSpec({
        test("provider는 초기 제공자를 반환한다") {
            val initialProvider = fixtureMonkey.giveMeOne<DiaryMapProvider>()
            val state = DiaryMapState(initialProvider = initialProvider)

            state.provider shouldBe initialProvider
        }

        test("select는 제공자를 새 값으로 바꾼다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.select(DiaryMapProvider.GOOGLE)

            state.provider shouldBe DiaryMapProvider.GOOGLE
        }

        test("select를 같은 제공자로 반복해도 제공자가 유지된다") {
            val provider = fixtureMonkey.giveMeOne<DiaryMapProvider>()
            val state = DiaryMapState(initialProvider = provider)

            repeat(REPEAT_COUNT) { state.select(provider) }

            state.provider shouldBe provider
        }

        test("핀은 빈 목록으로 시작하고 updatePins가 전달한 목록으로 바뀐다") {
            val pins =
                List(PIN_COUNT) {
                    DiaryMapPin(
                        id = Uuid.random(),
                        coordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>(),
                        color = Color(fixtureMonkey.giveMeOne<Int>()),
                        label = "label-${fixtureMonkey.giveMeOne<String>()}",
                    )
                }
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.pins shouldBe emptyList()

            state.updatePins(pins)
            state.pins shouldBe pins

            state.updatePins(emptyList())
            state.pins shouldBe emptyList()
        }

        test("초기 위치를 지정하지 않으면 시작 카메라가 없다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.camera.shouldBeNull()
        }

        test("초기 위치를 지정하면 그 위치와 동네 수준 확대를 시작 카메라로 삼는다") {
            val coordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()

            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER, initialCoordinate = coordinate)

            state.camera shouldBe
                DiaryMapCamera(
                    latitude = coordinate.latitude,
                    longitude = coordinate.longitude,
                    zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
                )
        }

        test("제공자를 바꿔도 마지막 카메라를 이어받는다") {
            val camera = fixtureMonkey.giveMeOne<DiaryMapCamera>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(camera)

            state.select(DiaryMapProvider.GOOGLE)

            state.camera shouldBe camera
        }

        test("이전 제공자로 되돌아가도 마지막 카메라를 이어받는다") {
            val naverCamera = fixtureMonkey.giveMeOne<DiaryMapCamera>()
            val googleCamera = fixtureMonkey.giveMeOne<DiaryMapCamera>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.moveCamera(naverCamera)
            state.select(DiaryMapProvider.GOOGLE)
            state.moveCamera(googleCamera)
            state.select(DiaryMapProvider.NAVER)

            state.camera shouldBe googleCamera
        }

        test("지도가 영역을 알려오기 전에는 보이는 영역이 없다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.bounds.shouldBeNull()
        }

        test("TC-DIARY-MAP-DOMAIN-034: 지도가 알려온 카메라의 보이는 영역을 확인한다") {
            val bounds = fixtureMonkey.giveMeOne<DiaryMapBounds>()
            val camera = fixtureMonkey.giveMeOne<DiaryMapCamera>().copy(bounds = bounds)
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.moveCamera(camera)

            state.bounds shouldBe bounds
        }

        test("TC-DIARY-MAP-DOMAIN-035: 알려온 카메라에 보이는 영역이 없으면 영역이 없는 것으로 다룬다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(fixtureMonkey.giveMeOne<DiaryMapCamera>().copy(bounds = fixtureMonkey.giveMeOne<DiaryMapBounds>()))

            state.moveCamera(fixtureMonkey.giveMeOne<DiaryMapCamera>().copy(bounds = null))

            state.bounds.shouldBeNull()
        }

        test("moveTo는 보이는 영역을 바꾸지 않는다") {
            val bounds = fixtureMonkey.giveMeOne<DiaryMapBounds>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(fixtureMonkey.giveMeOne<DiaryMapCamera>().copy(bounds = bounds))

            state.moveTo(fixtureMonkey.giveMeOne<DiaryMapCoordinate>())

            state.bounds shouldBe bounds
        }

        test("Saver는 보이는 영역을 저장하고 복원한다") {
            val bounds = fixtureMonkey.giveMeOne<DiaryMapBounds>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(fixtureMonkey.giveMeOne<DiaryMapCamera>().copy(bounds = bounds))

            val restored = state.saveAndRestore()

            restored?.bounds shouldBe bounds
        }

        test("Saver는 보이는 영역이 없으면 영역이 없는 상태로 복원한다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(fixtureMonkey.giveMeOne<DiaryMapCamera>().copy(bounds = null))

            val restored = state.saveAndRestore()

            restored?.bounds.shouldBeNull()
        }

        test("Saver는 선택한 제공자를 저장하고 복원한다") {
            val provider = fixtureMonkey.giveMeOne<DiaryMapProvider>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.select(provider)

            val restored = state.saveAndRestore()

            restored?.provider shouldBe provider
        }

        test("Saver는 보고 있던 카메라를 저장하고 복원한다") {
            val camera = fixtureMonkey.giveMeOne<DiaryMapCamera>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(camera)

            val restored = state.saveAndRestore()

            restored?.camera shouldBe camera
        }

        test("Saver는 카메라가 없으면 카메라가 없는 상태로 복원한다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            val restored = state.saveAndRestore()

            restored?.camera.shouldBeNull()
        }

        test("초기 지점을 지정하지 않으면 고른 지점이 없다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.spot.shouldBeNull()
        }

        test("selectSpot은 고른 지점을 새 값으로 바꾼다") {
            val spot = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.selectSpot(spot)

            state.spot shouldBe spot
        }

        test("selectSpot을 다른 지점으로 부르면 마지막 지점만 남는다") {
            val first = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
            val second = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.selectSpot(first)
            state.selectSpot(second)

            state.spot shouldBe second
        }

        test("selectSpot에 값이 없으면 고른 지점을 지운다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.selectSpot(fixtureMonkey.giveMeOne<DiaryMapCoordinate>())

            state.selectSpot(null)

            state.spot.shouldBeNull()
        }

        test("coordinate는 보고 있는 지도 위치를 반환한다") {
            val camera = fixtureMonkey.giveMeOne<DiaryMapCamera>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.moveCamera(camera)

            state.coordinate shouldBe DiaryMapCoordinate(latitude = camera.latitude, longitude = camera.longitude)
        }

        test("보고 있는 위치가 없으면 coordinate도 없다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.coordinate.shouldBeNull()
        }

        test("moveTo는 보고 있던 확대 수준을 유지하며 가운데 위치만 옮긴다") {
            val camera = fixtureMonkey.giveMeOne<DiaryMapCamera>()
            val target = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(camera)

            state.moveTo(target)

            state.coordinate shouldBe target
            state.camera?.zoom shouldBe camera.zoom
        }

        test("보고 있던 위치가 없으면 moveTo는 동네 수준 확대로 옮긴다") {
            val target = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.moveTo(target)

            state.camera shouldBe
                DiaryMapCamera(
                    latitude = target.latitude,
                    longitude = target.longitude,
                    zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
                )
        }

        test("moveTo는 옮길 위치를 명령으로 내보낸다") {
            runTest {
                val target = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
                val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

                state.moveTo(target)

                state.moveCommand.first() shouldBe
                    DiaryMapCamera(
                        latitude = target.latitude,
                        longitude = target.longitude,
                        zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
                    )
            }
        }

        test("TC-DIARY-MAP-DOMAIN-045 화면이 지도를 옮기도록 지정하면 지도에 보내는 이동 요청은 보고 있던 확대 수준을 유지한다") {
            runTest {
                val camera = fixtureMonkey.giveMeOne<DiaryMapCamera>()
                val target = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
                val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
                state.moveCamera(camera)

                state.moveTo(target)

                state.moveCommand.first() shouldBe
                    DiaryMapCamera(
                        latitude = target.latitude,
                        longitude = target.longitude,
                        zoom = camera.zoom,
                    )
            }
        }

        test("moveTo를 같은 위치로 반복해도 명령이 그때마다 전달된다") {
            runTest {
                val target = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
                val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

                state.moveTo(target)
                state.moveCommand.first()
                state.moveTo(target)

                state.moveCommand.first() shouldBe
                    DiaryMapCamera(
                        latitude = target.latitude,
                        longitude = target.longitude,
                        zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
                    )
            }
        }

        test("명령을 받기 전에 moveTo가 여러 번 불리면 마지막 위치만 전달된다") {
            runTest {
                val first = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
                val last = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
                val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

                state.moveTo(first)
                state.moveTo(last)

                state.moveCommand.first() shouldBe
                    DiaryMapCamera(
                        latitude = last.latitude,
                        longitude = last.longitude,
                        zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
                    )
            }
        }

        test("Saver는 고른 지점을 저장하고 복원한다") {
            val spot = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.selectSpot(spot)

            val restored = state.saveAndRestore()

            restored?.spot shouldBe spot
        }

        test("Saver는 고른 지점이 없으면 지점이 없는 상태로 복원한다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            val restored = state.saveAndRestore()

            restored?.spot.shouldBeNull()
        }
    }) {
    private companion object {
        private const val REPEAT_COUNT = 3
        private const val PIN_COUNT = 3

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun DiaryMapState.saveAndRestore(): DiaryMapState? {
            val saved = with(DiaryMapState.Saver) { with(SaverScope { true }) { save(this@saveAndRestore) } }

            return saved?.let { DiaryMapState.Saver.restore(it) }
        }
    }
}
