package io.github.taetae98coding.diary.compose.map

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class DiaryMapCenterTest : FunSpec() {
    init {
        test("TC-DIARY-MAP-DOMAIN-048 지도가 알려 온 위치와 보이는 영역으로 좌표가 가운데에 있는지 판정한다") {
            val center = randomCoordinate()
            val span = randomSpan()
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

                state.moveCamera(DiaryMapCamera(latitude = center.latitude, longitude = center.longitude, zoom = randomZoom(), bounds = reportedBounds))

                state.isCentered(target) shouldBe expected
            }

            DiaryMapState(initialProvider = DiaryMapProvider.NAVER).isCentered(center) shouldBe false
        }

        test("TC-DIARY-MAP-DOMAIN-049 지도를 옮기도록 지정하면 지도가 새 위치를 알려 오기 전에도 그 좌표가 가운데에 있다") {
            val reported = randomCoordinate()
            val target = randomCoordinate().copy(latitude = reported.latitude + OTHER_PLACE_OFFSET)
            val span = randomSpan()
            val state = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

            state.moveCamera(
                DiaryMapCamera(
                    latitude = reported.latitude,
                    longitude = reported.longitude,
                    zoom = randomZoom(),
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
        private const val LATITUDE_RANGE = 120
        private const val LATITUDE_MIN = -60.0
        private const val LONGITUDE_RANGE = 340
        private const val LONGITUDE_MIN = -170.0
        private const val SPAN_STEPS = 100
        private const val SPAN_UNIT = 0.01
        private const val ZOOM_RANGE = 18
        private const val ZOOM_MIN = 3.0

        private fun randomCoordinate(): DiaryMapCoordinate =
            DiaryMapCoordinate(
                latitude = LATITUDE_MIN + fixtureMonkey.giveMeOne<Int>().mod(LATITUDE_RANGE),
                longitude = LONGITUDE_MIN + fixtureMonkey.giveMeOne<Int>().mod(LONGITUDE_RANGE),
            )

        private fun randomSpan(): Double = (fixtureMonkey.giveMeOne<Int>().mod(SPAN_STEPS) + 1) * SPAN_UNIT

        private fun randomZoom(): Double = ZOOM_MIN + fixtureMonkey.giveMeOne<Int>().mod(ZOOM_RANGE)
    }
}
