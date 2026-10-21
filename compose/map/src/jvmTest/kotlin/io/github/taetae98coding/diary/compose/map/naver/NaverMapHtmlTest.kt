package io.github.taetae98coding.diary.compose.map.naver

import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.net.URI

class NaverMapHtmlTest :
    FunSpec({
        test("TC-DIARY-MAP-DOMAIN-007: 유효한 인증 정보로 전체 영역을 사용하는 네이버 지도 문서를 만든다") {
            val ncpKeyId = "test-key_123"

            val html = requireNotNull(createNaverMapHtml(ncpKeyId = ncpKeyId, camera = null))

            html shouldContain "width: 100%"
            html shouldContain "height: 100%"
            html shouldContain "ncpKeyId=$ncpKeyId"
            html shouldContain "new naver.maps.Map"
            html shouldContain """src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=$ncpKeyId""""
        }

        test("네이버 지도 문서는 SDK가 https로 요청하도록 localhost의 https 출처로 연다") {
            val uri = URI(NAVER_MAP_BASE_URL)

            uri.scheme shouldBe "https"
            uri.host shouldBe "localhost"
        }

        test("네이버 지도 문서는 문서 출처의 자원을 불러오지 않는다") {
            val html = requireNotNull(createNaverMapHtml(ncpKeyId = "test-key_123", camera = null))

            html shouldNotContain """src="/"""
            html shouldNotContain """href="/"""
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

        test("시스템 프로퍼티의 인증 정보로 지도 문서를 만든다") {
            val propertyName = "io.github.taetae98coding.diary.naverMapNcpKeyId"
            val originalValue = System.getProperty(propertyName)

            try {
                System.setProperty(propertyName, "test-key_123")

                requireNotNull(createNaverMapHtml(camera = null)) shouldContain "ncpKeyId=test-key_123"
            } finally {
                if (originalValue == null) {
                    System.clearProperty(propertyName)
                } else {
                    System.setProperty(propertyName, originalValue)
                }
            }
        }
    })
