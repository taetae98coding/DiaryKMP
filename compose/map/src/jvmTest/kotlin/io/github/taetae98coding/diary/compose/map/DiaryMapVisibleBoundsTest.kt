package io.github.taetae98coding.diary.compose.map

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.numericDouble

class DiaryMapVisibleBoundsTest : FunSpec() {
    init {
        test("화면 모서리가 어느 지리 방향에 놓여도 같은 보이는 영역이 나온다") {
            val center = DiaryMapCoordinate(latitude = Arb.numericDouble(min = LATITUDE_RANGE.start, max = LATITUDE_RANGE.endInclusive).next(), longitude = Arb.numericDouble(min = LONGITUDE_RANGE.start, max = LONGITUDE_RANGE.endInclusive).next())
            val latitudeHalf = Arb.numericDouble(min = HALF_SPAN_RANGE.start, max = HALF_SPAN_RANGE.endInclusive).next()
            val longitudeHalf = Arb.numericDouble(min = HALF_SPAN_RANGE.start, max = HALF_SPAN_RANGE.endInclusive).next()
            val southWest = DiaryMapCoordinate(latitude = center.latitude - latitudeHalf, longitude = center.longitude - longitudeHalf)
            val southEast = DiaryMapCoordinate(latitude = center.latitude - latitudeHalf, longitude = center.longitude + longitudeHalf)
            val northWest = DiaryMapCoordinate(latitude = center.latitude + latitudeHalf, longitude = center.longitude - longitudeHalf)
            val northEast = DiaryMapCoordinate(latitude = center.latitude + latitudeHalf, longitude = center.longitude + longitudeHalf)
            val expected = DiaryMapBounds(south = southWest.latitude, north = northEast.latitude, west = southWest.longitude, east = northEast.longitude)

            // 화면 아래 왼쪽, 아래 오른쪽, 위 왼쪽, 위 오른쪽 순서다. 회전하지 않은 지도, 180° 돌린 지도, 90° 돌린 지도다.
            listOf(
                listOf(southWest, southEast, northWest, northEast),
                listOf(northEast, northWest, southEast, southWest),
                listOf(southEast, northEast, southWest, northWest),
            ).forEach { corners ->
                visibleBoundsOf(center = center, corners = corners) shouldBe expected
            }
        }

        test("비스듬히 돌린 지도는 네 모서리를 모두 담는 영역이 나온다") {
            val center = DiaryMapCoordinate(latitude = Arb.numericDouble(min = LATITUDE_RANGE.start, max = LATITUDE_RANGE.endInclusive).next(), longitude = Arb.numericDouble(min = LONGITUDE_RANGE.start, max = LONGITUDE_RANGE.endInclusive).next())
            val latitudeHalf = Arb.numericDouble(min = HALF_SPAN_RANGE.start, max = HALF_SPAN_RANGE.endInclusive).next()
            val longitudeHalf = Arb.numericDouble(min = HALF_SPAN_RANGE.start, max = HALF_SPAN_RANGE.endInclusive).next()
            val south = center.copy(latitude = center.latitude - latitudeHalf)
            val north = center.copy(latitude = center.latitude + latitudeHalf)
            val west = center.copy(longitude = center.longitude - longitudeHalf)
            val east = center.copy(longitude = center.longitude + longitudeHalf)

            visibleBoundsOf(center = center, corners = listOf(south, east, west, north)) shouldBe
                DiaryMapBounds(south = south.latitude, north = north.latitude, west = west.longitude, east = east.longitude)
        }

        test("날짜변경선을 걸친 화면만 서쪽 경도가 동쪽 경도보다 큰 영역이 된다") {
            val latitude = Arb.numericDouble(min = LATITUDE_RANGE.start, max = LATITUDE_RANGE.endInclusive).next()
            val latitudeHalf = Arb.numericDouble(min = HALF_SPAN_RANGE.start, max = HALF_SPAN_RANGE.endInclusive).next()
            val westOffset = Arb.numericDouble(min = HALF_SPAN_RANGE.start, max = HALF_SPAN_RANGE.endInclusive).next()
            val eastOffset = Arb.numericDouble(min = HALF_SPAN_RANGE.start, max = HALF_SPAN_RANGE.endInclusive).next()
            val westLongitude = ANTIMERIDIAN - westOffset
            val eastLongitude = -ANTIMERIDIAN + eastOffset
            val southWest = DiaryMapCoordinate(latitude = latitude - latitudeHalf, longitude = westLongitude)
            val southEast = DiaryMapCoordinate(latitude = latitude - latitudeHalf, longitude = eastLongitude)
            val northWest = DiaryMapCoordinate(latitude = latitude + latitudeHalf, longitude = westLongitude)
            val northEast = DiaryMapCoordinate(latitude = latitude + latitudeHalf, longitude = eastLongitude)
            val expected = DiaryMapBounds(south = southWest.latitude, north = northEast.latitude, west = westLongitude, east = eastLongitude)

            // 가운데가 날짜변경선의 서쪽에 있을 때와 동쪽에 있을 때, 그리고 그 화면을 180° 돌렸을 때다.
            listOf(
                DiaryMapCoordinate(latitude = latitude, longitude = ANTIMERIDIAN - westOffset / 2),
                DiaryMapCoordinate(latitude = latitude, longitude = -ANTIMERIDIAN + eastOffset / 2),
            ).forEach { center ->
                visibleBoundsOf(center = center, corners = listOf(southWest, southEast, northWest, northEast)) shouldBe expected
                visibleBoundsOf(center = center, corners = listOf(northEast, northWest, southEast, southWest)) shouldBe expected
            }
        }
    }

    private companion object {
        private const val ANTIMERIDIAN = 180.0
        private val LATITUDE_RANGE = -60.0..60.0
        private val LONGITUDE_RANGE = -170.0..170.0
        private val HALF_SPAN_RANGE = 0.01..1.0
    }
}
