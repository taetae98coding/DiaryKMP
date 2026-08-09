package io.github.taetae98coding.diary.compose.map

import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldNotContain
import kotlin.uuid.Uuid

class MapPinHtmlTest :
    FunSpec({
        test("TC-DIARY-MAP-DOMAIN-036: 전달된 핀을 지도 요청 내용에 담는다") {
            val pins =
                listOf(
                    DiaryMapPin(
                        id = FIRST_PIN_ID,
                        coordinate = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978),
                        color = Color(0xFF3A7BD5),
                        label = "카페",
                    ),
                    DiaryMapPin(
                        id = SECOND_PIN_ID,
                        coordinate = DiaryMapCoordinate(latitude = -33.4489, longitude = -70.6693),
                        color = Color(0xFF00FF00),
                        label = "서점",
                    ),
                )
            val expected =
                """var diaryMapPins = [""" +
                    """{ "id": "$FIRST_PIN_ID", "latitude": 37.5665, "longitude": 126.978, "color": "#3a7bd5", "label": "카페" }, """ +
                    """{ "id": "$SECOND_PIN_ID", "latitude": -33.4489, "longitude": -70.6693, "color": "#00ff00", "label": "서점" }];"""

            val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null, pins = pins))
            val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null, pins = pins))

            naverHtml shouldContain expected
            googleHtml shouldContain expected
        }

        test("두 지도 요청 내용에 같은 핀 마커 모양을 담는다") {
            val expected = """var diaryMapPinMarker = { "path": "$PIN_MARKER_PATH_DATA","""

            val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null))
            val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null))

            listOf(naverHtml, googleHtml).forEach { html ->
                html shouldContain expected
                html shouldContain """"viewport": $PIN_MARKER_VIEWPORT_SIZE"""
                html shouldContain """"size": $PIN_MARKER_SIZE_DP"""
                html shouldContain """"labelHeight": $PIN_MARKER_LABEL_HEIGHT_DP"""
                html shouldNotContain PIN_MARKER_PLACEHOLDER
            }
        }

        test("TC-DIARY-MAP-DOMAIN-036: 핀이 없으면 지도 요청 내용에 핀 표시를 담지 않는다") {
            val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null))
            val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null))

            naverHtml shouldContain "var diaryMapPins = [];"
            googleHtml shouldContain "var diaryMapPins = [];"
        }

        test("TC-DIARY-MAP-DOMAIN-037: 핀 라벨에 화면 구성을 훼손할 수 있는 문자가 있어도 요청 내용이 훼손되지 않는다") {
            val backslash = '\\'
            val labelList =
                listOf(
                    // 따옴표가 포함된 문구
                    """quote-"quoted"-label""" to """quote-$backslash"quoted$backslash"-label""",
                    // 태그 형태의 문자가 포함된 문구
                    "tag-</script><b>-label" to "tag-${backslash}u003c/script${backslash}u003e${backslash}u003cb${backslash}u003e-label",
                    // 줄바꿈이 포함된 문구
                    "line\nbreak-label" to "line${backslash}u000abreak-label",
                )

            labelList.forEach { (label, escapedLabel) ->
                val pins =
                    listOf(
                        DiaryMapPin(
                            id = FIRST_PIN_ID,
                            coordinate = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978),
                            color = Color(0xFF3A7BD5),
                            label = label,
                        ),
                    )

                val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null, pins = pins))
                val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null, pins = pins))

                listOf(naverHtml, googleHtml).forEach { html ->
                    html shouldContain """"label": "$escapedLabel""""
                    html shouldNotContain label
                }
            }
        }

        test("TC-DIARY-MAP-DOMAIN-038: 핀 선택 사용 여부와 핀의 식별 값을 지도 요청 내용에 담는다") {
            val pins =
                listOf(
                    DiaryMapPin(
                        id = FIRST_PIN_ID,
                        coordinate = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978),
                        color = Color(0xFF3A7BD5),
                        label = "카페",
                    ),
                    DiaryMapPin(
                        id = SECOND_PIN_ID,
                        coordinate = DiaryMapCoordinate(latitude = -33.4489, longitude = -70.6693),
                        color = Color(0xFF00FF00),
                        label = "서점",
                    ),
                )

            listOf(true, false).forEach { isPinSelectable ->
                val naverHtml =
                    requireNotNull(
                        createNaverMapHtml(
                            ncpKeyId = TEST_KEY,
                            camera = null,
                            pins = pins,
                            isPinSelectable = isPinSelectable,
                        ),
                    )
                val googleHtml =
                    requireNotNull(
                        createGoogleMapHtml(
                            apiKey = TEST_KEY,
                            camera = null,
                            pins = pins,
                            isPinSelectable = isPinSelectable,
                        ),
                    )

                listOf(naverHtml, googleHtml).forEach { html ->
                    html shouldContain "var diaryMapPinSelectable = $isPinSelectable;"
                    html shouldContain """"id": "$FIRST_PIN_ID""""
                    html shouldContain """"id": "$SECOND_PIN_ID""""
                }
            }
        }

        test("핀 선택 사용 여부를 지정하지 않으면 사용하지 않는 것으로 담는다") {
            val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null))
            val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null))

            naverHtml shouldContain "var diaryMapPinSelectable = false;"
            googleHtml shouldContain "var diaryMapPinSelectable = false;"
        }

        test("핀 식별 값은 표준 표기의 식별자로 담긴다") {
            val id = Uuid.random()
            val pins =
                listOf(
                    DiaryMapPin(
                        id = id,
                        coordinate = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978),
                        color = Color(0xFF3A7BD5),
                        label = "카페",
                    ),
                )

            val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null, pins = pins))
            val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null, pins = pins))

            listOf(naverHtml, googleHtml).forEach { html ->
                html shouldContain """"id": "$id""""
                id.toString() shouldMatch UUID_TEXT_PATTERN
            }
        }

        test("지도가 쓸 수 없는 핀 좌표는 요청 내용에 담지 않는다") {
            val pins =
                listOf(
                    DiaryMapPin(
                        id = FIRST_PIN_ID,
                        coordinate = DiaryMapCoordinate(latitude = Double.NaN, longitude = 126.978),
                        color = Color(0xFF3A7BD5),
                        label = "nan-pin",
                    ),
                    DiaryMapPin(
                        id = SECOND_PIN_ID,
                        coordinate = DiaryMapCoordinate(latitude = 37.5665, longitude = Double.POSITIVE_INFINITY),
                        color = Color(0xFF3A7BD5),
                        label = "infinite-pin",
                    ),
                )

            val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null, pins = pins))
            val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null, pins = pins))

            naverHtml shouldContain "var diaryMapPins = [];"
            googleHtml shouldContain "var diaryMapPins = [];"
        }
    }) {
    private companion object {
        private const val TEST_KEY = "test-key_123"
        private val FIRST_PIN_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")
        private val SECOND_PIN_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000002")
        private val UUID_TEXT_PATTERN = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    }
}
