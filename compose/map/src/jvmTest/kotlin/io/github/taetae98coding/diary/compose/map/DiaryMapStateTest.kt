package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.map.provider.label
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class DiaryMapStateTest :
    FunSpec({
        test("provider는 초기 제공자를 반환한다") {
            val initialProvider = Arb.enum<DiaryMapProvider>().next()
            val state = DiaryMapState(initialProvider = initialProvider)

            state.provider shouldBe initialProvider
        }

        test("select는 제공자를 새 값으로 바꾼다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.select(DiaryMapProvider.GOOGLE)

            state.provider shouldBe DiaryMapProvider.GOOGLE
        }

        test("select를 같은 제공자로 반복해도 제공자가 유지된다") {
            val provider = Arb.enum<DiaryMapProvider>().next()
            val state = DiaryMapState(initialProvider = provider)

            repeat(REPEAT_COUNT) { state.select(provider) }

            state.provider shouldBe provider
        }

        test("핀은 빈 목록으로 시작하고 updatePins가 전달한 목록으로 바뀐다") {
            val pins =
                List(PIN_COUNT) {
                    DiaryMapPin(
                        id = Uuid.random(),
                        coordinate = Arb.diaryMapCoordinate().next(),
                        color = Color(Arb.int().next()),
                        label = "label-${Arb.string().next()}",
                    )
                }
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.pins shouldBe emptyList()

            state.updatePins(pins)
            state.pins shouldBe pins

            state.updatePins(emptyList())
            state.pins shouldBe emptyList()
        }

        test("TC-2d40dec4-f57f-4c2d-af52-063fd67cf067 초기 위치를 지정하지 않으면 시작 카메라를 정하지 않아 제공자의 기본 위치로 시작한다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.camera.shouldBeNull()
        }

        test("TC-dd13cbb2-390e-4fa5-a5cc-1a8d05e3e22c 초기 위치를 지정하면 그 위치를 가운데에 두고 동네 수준 확대로 시작 카메라를 삼는다") {
            List(INITIAL_COORDINATE_COUNT) { validCoordinate() }.forEach { coordinate ->
                val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER, initialCoordinate = coordinate)

                state.camera shouldBe
                    DiaryMapCamera(
                        latitude = coordinate.latitude,
                        longitude = coordinate.longitude,
                        zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
                    )
            }
        }

        test("TC-85d33219-3dc6-48c2-8ed9-a0d99cef51d1 제공자를 바꿔도 마지막 카메라를 이어받는다") {
            val camera = Arb.diaryMapCamera().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(camera)

            state.select(DiaryMapProvider.GOOGLE)

            state.camera shouldBe camera
        }

        test("이전 제공자로 되돌아가도 마지막 카메라를 이어받는다") {
            val naverCamera = Arb.diaryMapCamera().next()
            val googleCamera = Arb.diaryMapCamera().next()
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

        test("TC-3f774f94-c465-4866-ba04-c7e5304e4cac: 지도가 알려온 카메라의 보이는 영역을 확인한다") {
            val bounds = Arb.diaryMapBounds().next()
            val camera = Arb.diaryMapCamera().next().copy(bounds = bounds)
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.moveCamera(camera)

            state.bounds shouldBe bounds
        }

        test("TC-5cd1c7e9-0a1e-4eb4-a461-faea1bbf63d2: 알려온 카메라에 보이는 영역이 없으면 영역이 없는 것으로 다룬다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(Arb.diaryMapCamera().next().copy(bounds = Arb.diaryMapBounds().next()))

            state.moveCamera(Arb.diaryMapCamera().next().copy(bounds = null))

            state.bounds.shouldBeNull()
        }

        test("moveTo는 보이는 영역을 바꾸지 않는다") {
            val bounds = Arb.diaryMapBounds().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(Arb.diaryMapCamera().next().copy(bounds = bounds))

            state.moveTo(Arb.diaryMapCoordinate().next())

            state.bounds shouldBe bounds
        }

        test("Saver는 보이는 영역을 저장하고 복원한다") {
            val bounds = Arb.diaryMapBounds().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(Arb.diaryMapCamera().next().copy(bounds = bounds))

            val restored = state.saveAndRestore()

            restored?.bounds shouldBe bounds
        }

        test("Saver는 보이는 영역이 없으면 영역이 없는 상태로 복원한다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(Arb.diaryMapCamera().next().copy(bounds = null))

            val restored = state.saveAndRestore()

            restored?.bounds.shouldBeNull()
        }

        test("Saver는 선택한 제공자를 저장하고 복원한다") {
            val provider = Arb.enum<DiaryMapProvider>().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.select(provider)

            val restored = state.saveAndRestore()

            restored?.provider shouldBe provider
        }

        test("Saver는 보고 있던 카메라를 저장하고 복원한다") {
            val camera = Arb.diaryMapCamera().next()
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
            val spot = Arb.diaryMapCoordinate().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.selectSpot(spot)

            state.spot shouldBe spot
        }

        test("selectSpot을 다른 지점으로 부르면 마지막 지점만 남는다") {
            val first = Arb.diaryMapCoordinate().next()
            val second = Arb.diaryMapCoordinate().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.selectSpot(first)
            state.selectSpot(second)

            state.spot shouldBe second
        }

        test("TC-6170238d-af31-4849-9d2e-9d0642ecddf1 다른 핀 목록을 전달하면 빠진 핀은 사라지고 새 핀이 더해진다") {
            val kept = pin()
            val removed = pin()
            val added = pin()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.updatePins(listOf(removed, kept))

            state.updatePins(listOf(kept, added))

            state.pins shouldBe listOf(kept, added)
        }

        test("TC-0b42bac1-148e-42f4-8416-c8738c4246b5 지도가 옮겨진 위치를 알려와도 고른 지점은 그대로 남는다") {
            val spot = Arb.diaryMapCoordinate().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.selectSpot(spot)

            state.moveCamera(Arb.diaryMapCamera().next())
            state.moveTo(Arb.diaryMapCoordinate().next())

            state.spot shouldBe spot
        }

        test("TC-e890a3ff-0159-46cc-b17d-6c276659e0fd 화면이 지점을 지우면 지점이 없어지고 보고 있던 위치와 확대 수준은 그대로다") {
            val camera = Arb.diaryMapCamera().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(camera)
            state.selectSpot(Arb.diaryMapCoordinate().next())

            state.selectSpot(null)

            state.spot.shouldBeNull()
            state.camera shouldBe camera
        }

        test("selectSpot에 값이 없으면 고른 지점을 지운다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.selectSpot(Arb.diaryMapCoordinate().next())

            state.selectSpot(null)

            state.spot.shouldBeNull()
        }

        test("coordinate는 보고 있는 지도 위치를 반환한다") {
            val camera = Arb.diaryMapCamera().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.moveCamera(camera)

            state.coordinate shouldBe DiaryMapCoordinate(latitude = camera.latitude, longitude = camera.longitude)
        }

        test("보고 있는 위치가 없으면 coordinate도 없다") {
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.coordinate.shouldBeNull()
        }

        test("moveTo는 보고 있던 확대 수준을 유지하며 가운데 위치만 옮긴다") {
            val camera = Arb.diaryMapCamera().next()
            val target = Arb.diaryMapCoordinate().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(camera)

            state.moveTo(target)

            state.coordinate shouldBe target
            state.camera?.zoom shouldBe camera.zoom
        }

        test("보고 있던 위치가 없으면 moveTo는 동네 수준 확대로 옮긴다") {
            val target = Arb.diaryMapCoordinate().next()
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
                val target = Arb.diaryMapCoordinate().next()
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

        test("TC-c37cd3e5-4f57-4948-a671-c7f24ea37f50 화면이 지도를 옮기도록 지정하면 지도에 보내는 이동 요청은 보고 있던 확대 수준을 유지한다") {
            runTest {
                val camera = Arb.diaryMapCamera().next()
                val target = Arb.diaryMapCoordinate().next()
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
                val target = Arb.diaryMapCoordinate().next()
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
                val first = Arb.diaryMapCoordinate().next()
                val last = Arb.diaryMapCoordinate().next()
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

        test("TC-6710929d-f7fc-4c18-b6ad-76185ddb3cea 화면이 다른 곳을 지점으로 지정하면 그 곳만 지점이 되고 보고 있던 위치와 확대 수준은 그대로다") {
            val camera = Arb.diaryMapCamera().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.moveCamera(camera)
            state.selectSpot(Arb.diaryMapCoordinate().next())
            val newSpot = Arb.diaryMapCoordinate().next()

            state.selectSpot(newSpot)

            state.spot shouldBe newSpot
            state.camera shouldBe camera
        }

        test("TC-b1cd40a3-293f-4ba7-96d2-451c17d137d5 지도가 옮겨진 위치를 알려와도 전달된 핀 목록은 그대로 남는다") {
            val pins = List(PIN_COUNT) { pin() }
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.updatePins(pins)

            state.moveCamera(Arb.diaryMapCamera().next())
            state.moveTo(Arb.diaryMapCoordinate().next())

            state.pins shouldBe pins
        }

        test("TC-091389e2-aa31-47c7-b84f-09529ba04d7a 사용자가 지도를 옮겨 새 위치를 알려와도 지도를 되돌리는 이동 요청은 보내지지 않는다") {
            val camera = Arb.diaryMapCamera().next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER, initialCoordinate = Arb.diaryMapCoordinate().next())

            state.moveCommand.test {
                state.moveCamera(camera)

                state.camera shouldBe camera
                expectNoEvents()
            }
        }

        test("TC-d87428ad-50ce-4e35-8200-afcd4b9026fa 지점을 바꿔도 핀은 그대로이고 핀 목록을 바꿔도 지점은 그대로다") {
            val spot = Arb.diaryMapCoordinate().next()
            val pins = List(PIN_COUNT) { pin() }
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.selectSpot(spot)
            state.updatePins(pins)

            state.updatePins(listOf(pin()))
            state.updatePins(emptyList())

            state.spot shouldBe spot

            state.updatePins(pins)
            state.selectSpot(Arb.diaryMapCoordinate().next())
            state.selectSpot(null)

            state.pins shouldBe pins
        }

        test("Saver는 고른 지점을 저장하고 복원한다") {
            val spot = Arb.diaryMapCoordinate().next()
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

        private const val MAX_LATITUDE = 900_000
        private const val MAX_LONGITUDE = 1_800_000
        private const val COORDINATE_SCALE = 10_000.0
        private const val INITIAL_COORDINATE_COUNT = 2

        // 임의로 만든 Double은 좌표 범위를 크게 벗어나므로, 정수로 유효 범위의 위도와 경도를 만든다.
        private fun validCoordinate(): DiaryMapCoordinate =
            DiaryMapCoordinate(
                latitude = Arb.int().next() % MAX_LATITUDE / COORDINATE_SCALE,
                longitude = Arb.int().next() % MAX_LONGITUDE / COORDINATE_SCALE,
            )

        private fun pin(): DiaryMapPin =
            DiaryMapPin(
                id = Arb.kotlinUuid().next(),
                coordinate = Arb.diaryMapCoordinate().next(),
                color = Color(Arb.int().next()),
                label = "label-${Arb.string().next()}",
            )

        private fun DiaryMapState.saveAndRestore(): DiaryMapState? {
            val saved = with(DiaryMapState.Saver) { with(SaverScope { true }) { save(this@saveAndRestore) } }

            return saved?.let { DiaryMapState.Saver.restore(it) }
        }
    }
}
