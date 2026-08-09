package io.github.taetae98coding.diary.compose.map

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
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

        test("TC-DIARY-MAP-DOMAIN-026: 확인할 수 없는 위치 소식은 무시되어 초기 지도 위치 정책을 따른다") {
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
                message.toMapMessageOrNull().shouldBeNull()
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

                message.toMapMessageOrNull() shouldBe MapMessage.Click(coordinate = coordinate)
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
                message.toMapMessageOrNull().shouldBeNull()
            }
        }

        test("TC-DIARY-MAP-DOMAIN-039: 지도가 알려온 핀 누르기 소식의 식별 값을 전달한다") {
            val idList = List(2) { Uuid.random() }

            idList.forEach { id ->
                val message = """{"type": "pinClick", "id": "$id"}"""

                message.toMapMessageOrNull() shouldBe MapMessage.PinClick(id = id)
            }
        }

        test("TC-DIARY-MAP-DOMAIN-039: 확인할 수 없는 핀 누르기 소식은 전달하지 않는다") {
            val messages =
                listOf(
                    """{"type": "pinClick"}""",
                    """{"type": "pinClick", "id": null}""",
                    """{"type": "pinClick", "id": ""}""",
                    """{"type": "pinClick", "id": 1}""",
                    """{"type": "pinClick", "id": "식별자가 아닌 값"}""",
                )

            messages.forEach { message ->
                message.toMapMessageOrNull().shouldBeNull()
            }
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
    }
}
