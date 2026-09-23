package io.github.taetae98coding.diary.compose.map.naver

import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.http.Url
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

class NaverMapHtmlTest :
    FunSpec({
        test("TC-DIARY-MAP-DOMAIN-007: 유효한 인증 정보로 전체 영역을 사용하는 네이버 지도 문서를 만든다") {
            val ncpKeyId = "test-key_123"

            val html = requireNotNull(createNaverMapHtml(ncpKeyId = ncpKeyId, camera = null))

            html shouldContain "width: 100%"
            html shouldContain "height: 100%"
            html shouldContain "ncpKeyId=$ncpKeyId"
            html shouldContain "new naver.maps.Map"
            html shouldContain """src="/naver-maps.js""""
        }

        test("TC-DIARY-MAP-DOMAIN-011: 네이버 지도가 제공하는 지도 컨트롤과 조작을 모두 켠 지도를 만든다") {
            val html = requireNotNull(createNaverMapHtml(ncpKeyId = "test-key_123", camera = null))

            val enabledOptions =
                listOf(
                    "zoomControl: true",
                    "scaleControl: true",
                    "mapTypeControl: true",
                    "mapDataControl: true",
                    "logoControl: true",
                    "draggable: true",
                    "pinchZoom: true",
                    "scrollWheel: true",
                    "keyboardShortcuts: true",
                    "disableDoubleClickZoom: false",
                    "disableDoubleTapZoom: false",
                    "disableTwoFingerTapZoom: false",
                    "disableKineticPan: false",
                )

            enabledOptions.forEach { option ->
                html shouldContain option
            }
        }

        test("TC-DIARY-MAP-DOMAIN-043: 현 위치 표시를 켜지 않은 지도를 만든다") {
            val html = requireNotNull(createNaverMapHtml(ncpKeyId = "test-key_123", camera = null))

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
                    latitude = 37.5666102,
                    longitude = 126.9783881,
                    zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM,
                )

            val html = requireNotNull(createNaverMapHtml(ncpKeyId = "test-key_123", camera = camera))

            html shouldContain """"latitude": 37.5666102"""
            html shouldContain """"longitude": 126.9783881"""
            html shouldContain """"zoom": 15.0"""
            html shouldContain "options.center = { lat: diaryMapCamera.latitude, lng: diaryMapCamera.longitude }"
        }

        test("TC-DIARY-MAP-DOMAIN-024: 초기 위치를 지정하지 않으면 지도 문서에 시작 위치를 담지 않는다") {
            val html = requireNotNull(createNaverMapHtml(ncpKeyId = "test-key_123", camera = null))

            html shouldContain "var diaryMapCamera = null;"
        }

        test("지도 움직임이 멈추면 보고 있는 위치를 앱에 알리는 스크립트를 담는다") {
            val html = requireNotNull(createNaverMapHtml(ncpKeyId = "test-key_123", camera = null))

            html shouldContain """naver.maps.Event.addListener(window.diaryNaverMap, "idle", diaryPostCamera);"""
            html shouldContain "window.ipc.postMessage"
        }

        // 네이버 지도의 idle은 최초 로드에서 발생하지 않으므로 초기 위치 소식을 직접 알려야 한다.
        test("지도가 처음 표시되면 보고 있는 위치를 앱에 알리는 스크립트를 담는다") {
            val html = requireNotNull(createNaverMapHtml(ncpKeyId = "test-key_123", camera = null))

            html shouldContain """naver.maps.Event.once(window.diaryNaverMap, "init", diaryPostCamera);"""
            html shouldContain "window.diaryNaverMap.autoResize();"
            html shouldContain "diaryPostCamera();"
        }

        test("지도가 쓸 수 없는 초기 위치는 지도 문서에 담지 않는다") {
            listOf(
                DiaryMapCamera(latitude = Double.NaN, longitude = 0.0, zoom = 15.0),
                DiaryMapCamera(latitude = 0.0, longitude = Double.POSITIVE_INFINITY, zoom = 15.0),
                DiaryMapCamera(latitude = 0.0, longitude = 0.0, zoom = Double.NEGATIVE_INFINITY),
            ).forEach { camera ->
                val html = requireNotNull(createNaverMapHtml(ncpKeyId = "test-key_123", camera = camera))

                html shouldContain "var diaryMapCamera = null;"
            }
        }

        test("TC-DIARY-MAP-DOMAIN-008: 인증 정보가 비어 있으면 지도 문서를 만들지 않는다") {
            listOf("", " ").forEach { ncpKeyId ->
                createNaverMapHtml(ncpKeyId = ncpKeyId, camera = null).shouldBeNull()
            }
        }

        test("TC-DIARY-MAP-DOMAIN-008: HTML에 삽입할 수 없는 인증 정보는 지도 문서를 만들지 않는다") {
            createNaverMapHtml(ncpKeyId = """"><script>alert("invalid")</script>""", camera = null).shouldBeNull()
        }

        test("TC-DIARY-MAP-DOMAIN-008: 인증 정보가 유효하지 않으면 지도 서버를 시작하지 않는다") {
            listOf("", " ", """"><script>alert("invalid")</script>""").forEach { ncpKeyId ->
                NaverMapHttpServer.start(ncpKeyId = ncpKeyId, camera = null).shouldBeNull()
            }
        }

        test("시스템 프로퍼티의 인증 정보로 지도 서버를 시작한다") {
            val propertyName = "io.github.taetae98coding.diary.naverMapNcpKeyId"
            val originalValue = System.getProperty(propertyName)

            try {
                System.setProperty(propertyName, "test-key_123")

                requireNotNull(NaverMapHttpServer.start(camera = null)).use { server ->
                    val html =
                        URI(server.url)
                            .toURL()
                            .openStream()
                            .bufferedReader()
                            .use { it.readText() }

                    html shouldContain "ncpKeyId=test-key_123"
                }
            } finally {
                if (originalValue == null) {
                    System.clearProperty(propertyName)
                } else {
                    System.setProperty(propertyName, originalValue)
                }
            }
        }

        test("TC-DIARY-MAP-DOMAIN-007: 지도 문서를 localhost에서 제공한다") {
            val html = "<html><body>Naver Map</body></html>"
            val sdk = "Naver Maps SDK"
            val proxyUrl = "http://nrbe.pstatic.net/styles/basic.json?callback=test"
            val proxiedResource = "Naver Maps resource"
            var loadedProxyUrl: String? = null

            NaverMapHttpServer
                .start(
                    html = html,
                    naverMapsSdkLoader = { sdk },
                    naverMapsProxyLoader = { url ->
                        loadedProxyUrl = url
                        proxiedResource
                    },
                ).use { server ->
                    val uri = URI(server.url)
                    val servedHtml =
                        uri
                            .toURL()
                            .openStream()
                            .bufferedReader()
                            .use { it.readText() }
                    val servedSdk =
                        uri.resolve("/naver-maps.js").toURL().openStream().bufferedReader().use {
                            it.readText()
                        }
                    val servedProxy =
                        uri
                            .resolve("/naver-proxy?url=${URLEncoder.encode(proxyUrl, Charsets.UTF_8)}")
                            .toURL()
                            .openStream()
                            .bufferedReader()
                            .use { it.readText() }

                    uri.scheme shouldBe "http"
                    uri.host shouldBe "localhost"
                    servedHtml shouldBe html
                    servedSdk shouldBe sdk
                    servedProxy shouldBe proxiedResource
                    loadedProxyUrl shouldBe proxyUrl
                }
        }

        test("정의되지 않거나 허용되지 않은 HTTP 요청을 거절한다") {
            NaverMapHttpServer
                .start(
                    html = "",
                    naverMapsSdkLoader = { "" },
                    naverMapsProxyLoader = { "" },
                ).use { server ->
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

        test("지도 리소스를 불러오지 못하면 잘못된 게이트웨이로 응답한다") {
            NaverMapHttpServer
                .start(
                    html = "",
                    naverMapsSdkLoader = { error("SDK unavailable") },
                    naverMapsProxyLoader = { "" },
                ).use { server ->
                    val connection =
                        URI(server.url)
                            .resolve("/naver-maps.js")
                            .toURL()
                            .openConnection() as HttpURLConnection

                    connection.responseCode shouldBe HttpURLConnection.HTTP_BAD_GATEWAY
                    connection.getHeaderField("Cache-Control") shouldBe "no-store"
                    connection.disconnect()
                }
        }

        test("SDK의 JSONP 요청을 localhost 프록시로 변경하고 HTTPS 리소스를 사용한다") {
            val source =
                "before;o.src=e;middle;" +
                    """et=tt||0===t.location.protocol.indexOf("https");after"""

            val transformed = transformNaverMapsSdk(source)

            transformed shouldContain """fetch("/naver-proxy?url="+encodeURIComponent(e))"""
            transformed shouldContain "et=true"
            transformed shouldNotContain "o.src=e"
        }

        test("지원하지 않는 SDK 형식은 사용하지 않는다") {
            shouldThrow<IllegalArgumentException> {
                transformNaverMapsSdk("unsupported")
            }
        }

        test("허용된 네이버 지도 리소스만 HTTPS로 중계한다") {
            val remoteUrl = "http://nrbe.pstatic.net/styles/basic.json?callback=test"

            createNaverMapProxyUrl(remoteUrl) shouldBe
                Url("https://nrbe.pstatic.net/styles/basic.json?callback=test")
        }

        test("허용되지 않은 네이버 지도 리소스는 중계하지 않는다") {
            val urls =
                listOf(
                    "https://example.com/script.js",
                    "https://user@nrbe.pstatic.net/script.js",
                    "https://nrbe.pstatic.net:8080/script.js",
                    "https://nrbe.pstatic.net/script.js#fragment",
                )

            urls.forEach { url ->
                shouldThrow<IllegalArgumentException> {
                    createNaverMapProxyUrl(url)
                }
            }
        }
    })
