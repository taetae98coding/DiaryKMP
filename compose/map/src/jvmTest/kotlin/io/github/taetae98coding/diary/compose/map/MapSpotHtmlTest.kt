package io.github.taetae98coding.diary.compose.map

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

class MapSpotHtmlTest :
    FunSpec({
        test("TC-DIARY-MAP-DOMAIN-027: 지점 선택 사용 여부를 지도 요청 내용에 담는다") {
            listOf(true, false).forEach { isSpotSelectable ->
                val naverHtml =
                    requireNotNull(
                        createNaverMapHtml(
                            ncpKeyId = TEST_KEY,
                            camera = null,
                            isSpotSelectable = isSpotSelectable,
                        ),
                    )
                val googleHtml =
                    requireNotNull(
                        createGoogleMapHtml(
                            apiKey = TEST_KEY,
                            camera = null,
                            isSpotSelectable = isSpotSelectable,
                        ),
                    )

                naverHtml shouldContain "var diaryMapSpotSelectable = $isSpotSelectable;"
                googleHtml shouldContain "var diaryMapSpotSelectable = $isSpotSelectable;"
            }
        }

        test("TC-DIARY-MAP-DOMAIN-028: 고른 지점을 지도 요청 내용에 담는다") {
            val naverSpot = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978)
            val naverHtml =
                requireNotNull(
                    createNaverMapHtml(
                        ncpKeyId = TEST_KEY,
                        camera = null,
                        spot = naverSpot,
                        isSpotSelectable = true,
                    ),
                )

            naverHtml shouldContain """var diaryMapSpot = { "latitude": 37.5665, "longitude": 126.978 };"""

            val googleSpot = DiaryMapCoordinate(latitude = -33.4489, longitude = -70.6693)
            val googleHtml =
                requireNotNull(
                    createGoogleMapHtml(
                        apiKey = TEST_KEY,
                        camera = null,
                        spot = googleSpot,
                        isSpotSelectable = true,
                    ),
                )

            googleHtml shouldContain """var diaryMapSpot = { "latitude": -33.4489, "longitude": -70.6693 };"""
        }

        test("TC-DIARY-MAP-DOMAIN-028: 고른 지점이 없으면 지도 요청 내용에 지점 표시를 담지 않는다") {
            val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null, spot = null))
            val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null, spot = null))

            naverHtml shouldContain "var diaryMapSpot = null;"
            googleHtml shouldContain "var diaryMapSpot = null;"
        }

        test("지도 요청 내용에 준비 완료를 알리는 경로가 담긴다") {
            val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null))
            val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null))

            listOf(naverHtml, googleHtml).forEach { html ->
                html shouldContain """window.ipc.postMessage(JSON.stringify({ type: "ready" }));"""
                html shouldContain "diaryPostReady();"
            }
        }

        test("지도가 쓸 수 없는 지점 값은 지점이 없는 것으로 담는다") {
            val spotList =
                listOf(
                    DiaryMapCoordinate(latitude = Double.NaN, longitude = 126.978),
                    DiaryMapCoordinate(latitude = 37.5665, longitude = Double.POSITIVE_INFINITY),
                    DiaryMapCoordinate(latitude = Double.NEGATIVE_INFINITY, longitude = Double.NaN),
                )

            spotList.forEach { spot ->
                val naverHtml = requireNotNull(createNaverMapHtml(ncpKeyId = TEST_KEY, camera = null, spot = spot))
                val googleHtml = requireNotNull(createGoogleMapHtml(apiKey = TEST_KEY, camera = null, spot = spot))

                naverHtml shouldContain "var diaryMapSpot = null;"
                googleHtml shouldContain "var diaryMapSpot = null;"
            }
        }
    }) {
    private companion object {
        private const val TEST_KEY = "test-key_123"
    }
}
