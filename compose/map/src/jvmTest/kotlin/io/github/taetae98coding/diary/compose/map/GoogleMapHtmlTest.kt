package io.github.taetae98coding.diary.compose.map

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.net.HttpURLConnection
import java.net.URI

class GoogleMapHtmlTest :
    FunSpec({
        test("TC-DIARY-MAP-DOMAIN-007: 유효한 인증 정보로 지도 영역 전체를 사용하는 지도를 기기 안에서만 제공한다") {
            val apiKey = "test-key_123"

            requireNotNull(GoogleMapHttpServer.start(apiKey = apiKey, camera = null)).use { server ->
                val uri = URI(server.url)
                val html =
                    uri
                        .toURL()
                        .openStream()
                        .bufferedReader()
                        .use { it.readText() }

                uri.scheme shouldBe "http"
                uri.host shouldBe "localhost"
                html shouldContain "width: 100%"
                html shouldContain "height: 100%"
                html shouldContain "key=$apiKey"
                html shouldContain "new google.maps.Map"
            }
        }

        test("TC-DIARY-MAP-DOMAIN-012: Google 지도가 제공하는 지도 컨트롤과 조작을 모두 켠 지도를 만든다") {
            val html = requireNotNull(createGoogleMapHtml(apiKey = "test-key_123", camera = null))

            val enabledOptions =
                listOf(
                    "zoomControl: true",
                    "scaleControl: true",
                    "mapTypeControl: true",
                    "streetViewControl: true",
                    "fullscreenControl: true",
                    "cameraControl: true",
                    "rotateControl: true",
                    "keyboardShortcuts: true",
                    "clickableIcons: true",
                    """gestureHandling: "greedy"""",
                )

            enabledOptions.forEach { option ->
                html shouldContain option
            }
        }

        test("TC-DIARY-MAP-DOMAIN-043: 현 위치 표시를 켜지 않은 지도를 만든다") {
            val html = requireNotNull(createGoogleMapHtml(apiKey = "test-key_123", camera = null))

            val myLocationKeywordList =
                listOf(
                    "geolocation",
                    "getCurrentPosition",
                    "watchPosition",
                    "myLocation",
                    "locationButton",
                )

            myLocationKeywordList.forEach { keyword ->
                html shouldNotContain keyword
            }
        }

        test("TC-DIARY-MAP-DOMAIN-023: 초기 위치를 지정하면 그 위치와 동네 수준 확대를 지도 문서에 담는다") {
            val camera =
                DiaryMapCamera(
                    latitude = -33.8688,
                    longitude = -70.6693,
                    zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
                )

            val html = requireNotNull(createGoogleMapHtml(apiKey = "test-key_123", camera = camera))

            html shouldContain """"latitude": -33.8688"""
            html shouldContain """"longitude": -70.6693"""
            html shouldContain """"zoom": 15.0"""
            html shouldContain "center = { lat: diaryMapCamera.latitude, lng: diaryMapCamera.longitude }"
        }

        test("TC-DIARY-MAP-DOMAIN-024: 초기 위치를 지정하지 않으면 지도 문서에 시작 위치를 담지 않는다") {
            val html = requireNotNull(createGoogleMapHtml(apiKey = "test-key_123", camera = null))

            html shouldContain "var diaryMapCamera = null;"
        }

        test("지도 움직임이 멈추면 보고 있는 위치를 앱에 알리는 스크립트를 담는다") {
            val html = requireNotNull(createGoogleMapHtml(apiKey = "test-key_123", camera = null))

            html shouldContain """window.diaryGoogleMap.addListener("idle""""
            html shouldContain "window.ipc.postMessage"
        }

        test("지도가 쓸 수 없는 초기 위치는 지도 문서에 담지 않는다") {
            listOf(
                DiaryMapCamera(latitude = Double.NaN, longitude = 0.0, zoom = 15.0),
                DiaryMapCamera(latitude = 0.0, longitude = Double.POSITIVE_INFINITY, zoom = 15.0),
                DiaryMapCamera(latitude = 0.0, longitude = 0.0, zoom = Double.NEGATIVE_INFINITY),
            ).forEach { camera ->
                val html = requireNotNull(createGoogleMapHtml(apiKey = "test-key_123", camera = camera))

                html shouldContain "var diaryMapCamera = null;"
            }
        }

        test("TC-DIARY-MAP-DOMAIN-008: 인증 정보가 유효한 형식이 아니면 지도를 요청하지 않는다") {
            val apiKeys =
                listOf(
                    "",
                    " ",
                    """"><script>alert("invalid")</script>""",
                )

            apiKeys.forEach { apiKey ->
                createGoogleMapHtml(apiKey = apiKey, camera = null).shouldBeNull()
                GoogleMapHttpServer.start(apiKey = apiKey, camera = null).shouldBeNull()
            }
        }

        test("시스템 프로퍼티의 인증 정보로 지도 서버를 시작한다") {
            val propertyName = "io.github.taetae98coding.diary.googleMapApiKey"
            val originalValue = System.getProperty(propertyName)

            try {
                System.setProperty(propertyName, "test-key_123")

                requireNotNull(GoogleMapHttpServer.start(camera = null)).use { server ->
                    val html =
                        URI(server.url)
                            .toURL()
                            .openStream()
                            .bufferedReader()
                            .use { it.readText() }

                    html shouldContain "key=test-key_123"
                }
            } finally {
                if (originalValue == null) {
                    System.clearProperty(propertyName)
                } else {
                    System.setProperty(propertyName, originalValue)
                }
            }
        }

        test("인증 정보에 둘러싼 공백이 있어도 지도 문서를 만든다") {
            val html = requireNotNull(createGoogleMapHtml(apiKey = " test-key_123 ", camera = null))

            html shouldContain "key=test-key_123"
        }

        test("정의되지 않거나 허용되지 않은 HTTP 요청을 거절한다") {
            requireNotNull(GoogleMapHttpServer.start(apiKey = "test-key_123", camera = null)).use { server ->
                val root = URI(server.url)
                val requests =
                    listOf(
                        Triple(root.resolve("/unknown"), "GET", HttpURLConnection.HTTP_NOT_FOUND),
                        Triple(root, "POST", HttpURLConnection.HTTP_BAD_METHOD),
                    )

                requests.forEach { (uri, method, expectedStatus) ->
                    val connection = uri.toURL().openConnection() as HttpURLConnection
                    connection.requestMethod = method

                    connection.responseCode shouldBe expectedStatus
                    connection.disconnect()
                }
            }
        }
    })
