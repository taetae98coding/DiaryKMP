package io.github.taetae98coding.diary.core.testing.place

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int

private const val MAX_LATITUDE_IN_TEN_THOUSANDTHS = 900_000
private const val MAX_LONGITUDE_IN_TEN_THOUSANDTHS = 1_800_000
private const val TEN_THOUSAND = 10_000.0

// 입력란은 좌표를 소수점 아래 여섯 자리로 옮겨 적으므로, 그 자리 안에서 표현되는 값으로 만들어야 옮겨 적은 뒤에도 같은 값으로 읽힌다.
public fun Arb.Companion.coordinateInFormPrecision(): Arb<Coordinate> =
    arbitrary {
        Coordinate(
            latitude = Arb.int(-MAX_LATITUDE_IN_TEN_THOUSANDTHS + 1 until MAX_LATITUDE_IN_TEN_THOUSANDTHS).bind() / TEN_THOUSAND,
            longitude = Arb.int(-MAX_LONGITUDE_IN_TEN_THOUSANDTHS + 1 until MAX_LONGITUDE_IN_TEN_THOUSANDTHS).bind() / TEN_THOUSAND,
        )
    }
