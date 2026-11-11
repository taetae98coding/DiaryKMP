package io.github.taetae98coding.diary.compose.map

import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.numericDouble

class DiaryMapCenterTest : FunSpec() {
    init {
        test("TC-93d90d11-2f6b-47aa-8859-73ace2ab75a7 지도가 알려 온 위치와 보이는 영역으로 좌표가 가운데에 있는지 판정한다") {
            val center = Arb.diaryMapCoordinate(latitudeRange = LATITUDE_RANGE, longitudeRange = LONGITUDE_RANGE).next()
            val span = Arb.numericDouble(min = SPAN_MIN, max = SPAN_MAX).next()
            val bounds =
                DiaryMapBounds(
                    south = center.latitude - span / 2,
                    north = center.latitude + span / 2,
                    west = center.longitude - span / 2,
                    east = center.longitude + span / 2,
                )
            val within = span * WITHIN_RATIO
            val beyond = span * BEYOND_RATIO

            listOf(
                Triple(bounds, center, true),
                Triple(bounds, center.copy(latitude = center.latitude + within, longitude = center.longitude - within), true),
                Triple(bounds, center.copy(latitude = center.latitude + beyond), false),
                Triple(bounds, center.copy(longitude = center.longitude - beyond), false),
                Triple(null, center, true),
                Triple(null, center.copy(latitude = center.latitude + within), false),
            ).forEach { (reportedBounds, target, expected) ->
                val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

                state.moveCamera(DiaryMapCamera(latitude = center.latitude, longitude = center.longitude, zoom = Arb.numericDouble(min = ZOOM_MIN, max = ZOOM_MAX).next(), bounds = reportedBounds))

                state.isCentered(target) shouldBe expected
            }

            DiaryMapState(initialProvider = DiaryMapProvider.NAVER).isCentered(center) shouldBe false

            val antimeridianBounds = DiaryMapBounds(south = center.latitude - span / 2, north = center.latitude + span / 2, west = ANTIMERIDIAN - span / 2, east = -ANTIMERIDIAN + span / 2)
            val antimeridianCenter = center.copy(longitude = ANTIMERIDIAN - within / 2)

            listOf(
                antimeridianCenter.copy(longitude = -ANTIMERIDIAN + within / 2) to true,
                antimeridianCenter.copy(longitude = -ANTIMERIDIAN + beyond) to false,
            ).forEach { (target, expected) ->
                val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

                state.moveCamera(DiaryMapCamera(latitude = antimeridianCenter.latitude, longitude = antimeridianCenter.longitude, zoom = Arb.numericDouble(min = ZOOM_MIN, max = ZOOM_MAX).next(), bounds = antimeridianBounds))

                state.isCentered(target) shouldBe expected
            }
        }

        test("TC-d2ae97cc-d8ad-43e6-a1f4-345e6700327b 지도를 옮기도록 지정하면 지도가 새 위치를 알려 오기 전에도 그 좌표가 가운데에 있다") {
            val reported = Arb.diaryMapCoordinate(latitudeRange = LATITUDE_RANGE, longitudeRange = LONGITUDE_RANGE).next()
            val target = Arb.diaryMapCoordinate(latitudeRange = LATITUDE_RANGE, longitudeRange = LONGITUDE_RANGE).next().copy(latitude = reported.latitude + OTHER_PLACE_OFFSET)
            val span = Arb.numericDouble(min = SPAN_MIN, max = SPAN_MAX).next()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.moveCamera(
                DiaryMapCamera(
                    latitude = reported.latitude,
                    longitude = reported.longitude,
                    zoom = Arb.numericDouble(min = ZOOM_MIN, max = ZOOM_MAX).next(),
                    bounds = DiaryMapBounds(south = reported.latitude - span, north = reported.latitude + span, west = reported.longitude - span, east = reported.longitude + span),
                ),
            )
            state.moveTo(target)

            state.isCentered(target) shouldBe true
            state.isCentered(reported) shouldBe false
        }
    }

    private companion object {
        private const val WITHIN_RATIO = 0.005
        private const val BEYOND_RATIO = 0.02
        private const val OTHER_PLACE_OFFSET = 5.0
        private const val ANTIMERIDIAN = 180.0
        private const val SPAN_MIN = 0.01
        private const val SPAN_MAX = 1.0
        private const val ZOOM_MIN = 3.0
        private const val ZOOM_MAX = 21.0

        // 좌표에 영역 폭과 다른 곳까지의 거리를 더해도 유효 범위를 벗어나지 않게 한다.
        private val LATITUDE_RANGE = -60.0..60.0
        private val LONGITUDE_RANGE = -170.0..170.0
    }
}
