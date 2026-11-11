package io.github.taetae98coding.diary.compose.map

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble

private val LATITUDE_RANGE = -90.0..90.0
private val LONGITUDE_RANGE = -180.0..180.0

/**
 * 지구 위의 좌표를 만든다. 좌표에 값을 더하거나 빼는 테스트는 범위를 좁혀 결과가 유효 범위를 벗어나지 않게 한다.
 */
internal fun Arb.Companion.diaryMapCoordinate(
    latitudeRange: ClosedFloatingPointRange<Double> = LATITUDE_RANGE,
    longitudeRange: ClosedFloatingPointRange<Double> = LONGITUDE_RANGE,
): Arb<DiaryMapCoordinate> =
    arbitrary {
        DiaryMapCoordinate(
            latitude = Arb.numericDouble(min = latitudeRange.start, max = latitudeRange.endInclusive).bind(),
            longitude = Arb.numericDouble(min = longitudeRange.start, max = longitudeRange.endInclusive).bind(),
        )
    }
