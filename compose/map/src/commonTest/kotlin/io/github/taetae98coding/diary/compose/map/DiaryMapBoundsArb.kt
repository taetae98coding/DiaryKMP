package io.github.taetae98coding.diary.compose.map

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble

internal fun Arb.Companion.diaryMapBounds(): Arb<DiaryMapBounds> =
    arbitrary {
        DiaryMapBounds(
            south = Arb.numericDouble().bind(),
            north = Arb.numericDouble().bind(),
            west = Arb.numericDouble().bind(),
            east = Arb.numericDouble().bind(),
        )
    }
