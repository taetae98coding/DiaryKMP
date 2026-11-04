package io.github.taetae98coding.diary.compose.map

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble

internal fun Arb.Companion.diaryMapCoordinate(): Arb<DiaryMapCoordinate> =
    arbitrary {
        DiaryMapCoordinate(
            latitude = Arb.numericDouble().bind(),
            longitude = Arb.numericDouble().bind(),
        )
    }
