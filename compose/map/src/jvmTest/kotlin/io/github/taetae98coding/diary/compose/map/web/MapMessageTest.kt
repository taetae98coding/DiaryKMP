package io.github.taetae98coding.diary.compose.map.web

import androidx.compose.ui.graphics.Color
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.DiaryMapBounds
import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.google.createGoogleMapHtml
import io.github.taetae98coding.diary.compose.map.naver.createNaverMapHtml
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.uuid.Uuid

class MapMessageTest :
    FunSpec({
        test("TC-DIARY-MAP-DOMAIN-025: 지도가 알려온 마지막 위치를 전환한 지도 요청 내용에 담는다") {
            val toGoogleMessage = """{"type": "camera", "latitude": 37.5665, "longitude": 126.978, "zoom": 11.0}"""
            val toGoogleCamera = requireNotNull(toGoogleMessage.toMapMessageOrNull() as? MapMessage.Camera).camera
            val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = "test-key_123", camera = toGoogleCamera))

            googleHtml shouldContain """"latitude": 37.5665"""
            googleHtml shouldContain """"longitude": 126.978"""
            googleHtml shouldContain """"zoom": 11.0"""

            val toNaverMessage = """{"type": "camera", "latitude": -33.4489, "longitude": -70.6693, "zoom": 9.0}"""
            val toNaverCamera = requireNotNull(toNaverMessage.toMapMessageOrNull() as? MapMessage.Camera).camera
            val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = "test-key_123", camera = toNaverCamera))

            naverHtml shouldContain """"latitude": -33.4489"""
            naverHtml shouldContain """"longitude": -70.6693"""
            naverHtml shouldContain """"zoom": 9.0"""
        }

        test("TC-DIARY-MAP-DOMAIN-026: 확인할 수 없는 위치 소식은 무시되어 전환한 지도 요청 내용이 초기 지도 위치 정책을 따른다") {
            val messages =
                listOf(
                    "지도 위치 형식이 아닌 내용",
                    """{"latitude": 37.5665, "longitude": 126.978, "zoom": 11.0}""",
                    """{"type": "camera", "latitude": 37.5665, "zoom": 11.0}""",
                    """{"type": "camera", "latitude": "north", "longitude": 126.978, "zoom": 11.0}""",
                    """{"type": "camera", "latitude": null, "longitude": 126.978, "zoom": 11.0}""",
                    """{"type": "camera", "latitude": 1e999, "longitude": 126.978, "zoom": 11.0}""",
                )

            messages.forEach { message ->
                val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

                state.handleMapMessage(message = message, onReady = {}, onSpotClick = null, onPinClick = null)

                val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = "test-key_123", camera = state.camera))
                googleHtml shouldContain "var diaryMapCamera = null;"
            }
        }

        test("TC-DIARY-MAP-DOMAIN-029: 지도가 알려온 누르기 소식의 좌표를 지점으로 전달한다") {
            val coordinateList =
                listOf(
                    DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978),
                    DiaryMapCoordinate(latitude = -33.4489, longitude = -70.6693),
                    DiaryMapCoordinate(latitude = 90.0, longitude = 180.0),
                    DiaryMapCoordinate(latitude = -90.0, longitude = -180.0),
                )

            coordinateList.forEach { coordinate ->
                val message =
                    """{"type": "click", "latitude": ${coordinate.latitude}, "longitude": ${coordinate.longitude}}"""

                val deliveredList = mutableListOf<DiaryMapCoordinate>()

                DiaryMapState(initialProvider = DiaryMapProvider.NAVER).handleMapMessage(
                    message = message,
                    onReady = {},
                    onSpotClick = { spot -> deliveredList += spot },
                    onPinClick = null,
                )

                deliveredList shouldBe listOf(coordinate)
            }
        }

        test("TC-DIARY-MAP-DOMAIN-030: 확인할 수 없는 누르기 소식은 지점으로 다루지 않는다") {
            val messages =
                listOf(
                    "지도 누르기 형식이 아닌 내용",
                    """{"type": "click", "latitude": 37.5665}""",
                    """{"type": "click", "longitude": 126.978}""",
                    """{"type": "click", "latitude": "north", "longitude": 126.978}""",
                    """{"type": "click", "latitude": null, "longitude": 126.978}""",
                    """{"type": "click", "latitude": 1e999, "longitude": 126.978}""",
                )

            messages.forEach { message ->
                val deliveredList = mutableListOf<DiaryMapCoordinate>()

                DiaryMapState(initialProvider = DiaryMapProvider.NAVER).handleMapMessage(
                    message = message,
                    onReady = {},
                    onSpotClick = { spot -> deliveredList += spot },
                    onPinClick = null,
                )

                deliveredList shouldBe emptyList()
            }
        }

        test("TC-DIARY-MAP-DOMAIN-039: 지도가 알려온 핀 누르기 소식의 식별 값을 전달한다") {
            val idList = List(2) { Uuid.random() }
            val deliveredList = mutableListOf<Uuid>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.updatePins(idList.map { id -> pin(id = id) })

            idList.forEach { id ->
                state.handleMapMessage(
                    message = """{"type": "pinClick", "id": "$id"}""",
                    onReady = {},
                    onSpotClick = null,
                    onPinClick = { pinId -> deliveredList += pinId },
                )
            }

            deliveredList shouldBe idList
        }

        test("TC-DIARY-MAP-DOMAIN-039: 확인할 수 없거나 표시된 핀에 없는 핀 누르기 소식은 전달하지 않는다") {
            val messages =
                listOf(
                    """{"type": "pinClick"}""",
                    """{"type": "pinClick", "id": null}""",
                    """{"type": "pinClick", "id": ""}""",
                    """{"type": "pinClick", "id": 1}""",
                    """{"type": "pinClick", "id": "식별자가 아닌 값"}""",
                    // 표시된 핀에 없는 식별 값
                    """{"type": "pinClick", "id": "${Uuid.random()}"}""",
                )
            val deliveredList = mutableListOf<Uuid>()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
            state.updatePins(List(2) { pin(id = Uuid.random()) })

            messages.forEach { message ->
                state.handleMapMessage(
                    message = message,
                    onReady = {},
                    onSpotClick = null,
                    onPinClick = { pinId -> deliveredList += pinId },
                )
            }

            deliveredList shouldBe emptyList()
        }

        test("핀 누르기 소식과 지도 누르기 소식을 서로 구분한다") {
            val coordinate = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978)
            val clickMessage =
                """{"type": "click", "latitude": ${coordinate.latitude}, "longitude": ${coordinate.longitude}}"""
            val pinId = Uuid.random()
            val pinClickMessage = """{"type": "pinClick", "id": "$pinId"}"""

            clickMessage.toMapMessageOrNull() shouldBe MapMessage.Click(coordinate = coordinate)
            pinClickMessage.toMapMessageOrNull() shouldBe MapMessage.PinClick(id = pinId)
        }

        test("TC-DIARY-MAP-DOMAIN-031: 누르기 소식과 위치 소식을 서로 구분한다") {
            val camera = DiaryMapCamera(latitude = 37.5665, longitude = 126.978, zoom = 11.0)
            val coordinate = DiaryMapCoordinate(latitude = -33.4489, longitude = -70.6693)
            val cameraMessage =
                """{"type": "camera", "latitude": ${camera.latitude}, "longitude": ${camera.longitude}, "zoom": ${camera.zoom}}"""
            val clickMessage =
                """{"type": "click", "latitude": ${coordinate.latitude}, "longitude": ${coordinate.longitude}}"""

            cameraMessage.toMapMessageOrNull() shouldBe MapMessage.Camera(camera = camera)
            clickMessage.toMapMessageOrNull() shouldBe MapMessage.Click(coordinate = coordinate)
        }

        test("TC-DIARY-MAP-DOMAIN-034: 지도가 알려온 위치 소식의 보이는 영역을 확인한다") {
            val boundsList =
                listOf(
                    DiaryMapBounds(south = 37.4, north = 37.7, west = 126.8, east = 127.2),
                    // 날짜변경선을 걸쳐 서쪽 경도가 동쪽 경도보다 큰 영역
                    DiaryMapBounds(south = -10.0, north = 10.0, west = 170.0, east = -170.0),
                )

            boundsList.forEach { bounds ->
                val message =
                    """{"type": "camera", "latitude": 37.5665, "longitude": 126.978, "zoom": 11.0, """ +
                        """"south": ${bounds.south}, "north": ${bounds.north}, "west": ${bounds.west}, "east": ${bounds.east}}"""

                message.toMapMessageOrNull() shouldBe
                    MapMessage.Camera(
                        camera = DiaryMapCamera(latitude = 37.5665, longitude = 126.978, zoom = 11.0, bounds = bounds),
                    )
            }
        }

        test("TC-DIARY-MAP-DOMAIN-035: 보이는 영역을 확인할 수 없는 위치 소식은 영역이 없는 것으로 다룬다") {
            val boundsValues =
                listOf(
                    """"south": 37.4, "north": 37.7, "west": 126.8""",
                    """"south": 37.4, "north": 37.7, "west": 126.8, "east": "east"""",
                    """"south": 37.4, "north": 37.7, "west": 126.8, "east": null""",
                    """"south": 37.4, "north": 37.7, "west": 126.8, "east": 1e999""",
                )

            boundsValues.forEach { boundsValue ->
                val message =
                    """{"type": "camera", "latitude": 37.5665, "longitude": 126.978, "zoom": 11.0, $boundsValue}"""

                message.toMapMessageOrNull() shouldBe
                    MapMessage.Camera(
                        camera = DiaryMapCamera(latitude = 37.5665, longitude = 126.978, zoom = 11.0, bounds = null),
                    )
            }
        }

        test("지도가 알려온 위치 소식을 같은 값의 카메라로 읽는다") {
            val camera = fixtureMonkey.giveMeOne<DiaryMapCamera>().copy(bounds = null)
            val message =
                """{"type": "camera", "latitude": ${camera.latitude}, "longitude": ${camera.longitude}, "zoom": ${camera.zoom}}"""

            message.toMapMessageOrNull() shouldBe MapMessage.Camera(camera = camera)
        }

        test("지도 문서가 알려온 준비 완료 소식을 읽는다") {
            val message = """{"type": "ready"}"""

            message.toMapMessageOrNull() shouldBe MapMessage.Ready
        }

        test("준비 완료 소식에 다른 값이 함께 담겨도 준비 완료로 읽는다") {
            val message = """{"type": "ready", "latitude": 37.5665, "longitude": 126.978}"""

            message.toMapMessageOrNull() shouldBe MapMessage.Ready
        }

        test("알 수 없는 종류의 소식은 무시한다") {
            val message = """{"type": "unknown", "latitude": 37.5665, "longitude": 126.978, "zoom": 11.0}"""

            message.toMapMessageOrNull().shouldBeNull()
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun pin(id: Uuid): DiaryMapPin =
            DiaryMapPin(
                id = id,
                coordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>(),
                color = Color(fixtureMonkey.giveMeOne<Int>()),
                label = "label-${fixtureMonkey.giveMeOne<Int>()}",
            )
    }
}
