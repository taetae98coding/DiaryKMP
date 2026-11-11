package io.github.taetae98coding.diary.compose.map

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.arbitrary.orNull

/**
 * 지구 위의 위치를 보는 카메라를 만든다.
 */
internal fun Arb.Companion.diaryMapCamera(): Arb<DiaryMapCamera> =
    arbitrary {
        val coordinate = Arb.diaryMapCoordinate().bind()

        DiaryMapCamera(
            latitude = coordinate.latitude,
            longitude = coordinate.longitude,
            zoom = Arb.numericDouble().bind(),
            bounds = Arb.diaryMapBounds().orNull().bind(),
        )
    }
