package io.github.taetae98coding.diary.compose.map

import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import kotlin.uuid.Uuid

internal fun Arb.Companion.diaryMapPin(id: Uuid? = null): Arb<DiaryMapPin> =
    arbitrary {
        DiaryMapPin(
            id = id ?: Arb.kotlinUuid().bind(),
            coordinate = Arb.diaryMapCoordinate().bind(),
            color = Arb.color().bind(),
            label = Arb.string().bind(),
        )
    }
