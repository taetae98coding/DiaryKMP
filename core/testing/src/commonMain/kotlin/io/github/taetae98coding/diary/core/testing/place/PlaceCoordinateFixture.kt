package io.github.taetae98coding.diary.core.testing.place

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate

private const val MAX_LATITUDE_IN_TEN_THOUSANDTHS = 900_000
private const val MAX_LONGITUDE_IN_TEN_THOUSANDTHS = 1_800_000
private const val TEN_THOUSAND = 10_000.0

// 입력란은 좌표를 소수점 아래 여섯 자리로 옮겨 적으므로, 그 자리 안에서 표현되는 값으로 만들어야 옮겨 적은 뒤에도 같은 값으로 읽힌다.
public fun FixtureMonkey.coordinateInFormPrecision(): Coordinate =
    Coordinate(
        latitude = giveMeOne<Int>() % MAX_LATITUDE_IN_TEN_THOUSANDTHS / TEN_THOUSAND,
        longitude = giveMeOne<Int>() % MAX_LONGITUDE_IN_TEN_THOUSANDTHS / TEN_THOUSAND,
    )
