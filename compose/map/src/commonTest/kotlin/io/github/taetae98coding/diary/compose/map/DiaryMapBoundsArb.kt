package io.github.taetae98coding.diary.compose.map

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary

/**
 * 지구 위의 영역을 만든다. 남쪽 위도는 북쪽 위도보다 크지 않고, 날짜변경선을 걸치는 영역처럼 서쪽 경도가 동쪽 경도보다 클 수 있다.
 */
internal fun Arb.Companion.diaryMapBounds(): Arb<DiaryMapBounds> =
    arbitrary {
        val first = Arb.diaryMapCoordinate().bind()
        val second = Arb.diaryMapCoordinate().bind()

        DiaryMapBounds(
            south = minOf(first.latitude, second.latitude),
            north = maxOf(first.latitude, second.latitude),
            west = first.longitude,
            east = second.longitude,
        )
    }
