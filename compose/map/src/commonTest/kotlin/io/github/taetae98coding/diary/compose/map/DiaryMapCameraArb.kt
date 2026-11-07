package io.github.taetae98coding.diary.compose.map

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.arbitrary.orNull

internal fun Arb.Companion.diaryMapCamera(): Arb<DiaryMapCamera> =
    arbitrary {
        DiaryMapCamera(
            latitude = Arb.numericDouble().bind(),
            longitude = Arb.numericDouble().bind(),
            zoom = Arb.numericDouble().bind(),
            bounds = Arb.diaryMapBounds().orNull().bind(),
        )
    }
