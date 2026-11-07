package io.github.taetae98coding.diary.core.testing.memo

import io.github.taetae98coding.diary.core.database.api.memo.entity.CalendarMemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDateTime
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

public fun Arb.Companion.calendarMemoLocalEntity(): Arb<CalendarMemoLocalEntity> =
    arbitrary {
        CalendarMemoLocalEntity(
            id = Arb.kotlinUuid().bind(),
            title = Arb.string().bind(),
            color = Arb.long().bind(),
            isAllDay = Arb.boolean().bind(),
            start = Arb.kotlinxLocalDateTime().bind(),
            endInclusive = Arb.kotlinxLocalDateTime().bind(),
        )
    }

public fun Arb.Companion.memoDetailLocalEntity(): Arb<MemoDetailLocalEntity> =
    arbitrary {
        MemoDetailLocalEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            isAllDay = Arb.boolean().orNull().bind(),
            start = Arb.kotlinxLocalDateTime().orNull().bind(),
            endInclusive = Arb.kotlinxLocalDateTime().orNull().bind(),
        )
    }

public fun Arb.Companion.memoLocalEntity(): Arb<MemoLocalEntity> =
    arbitrary {
        MemoLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.memoDetailLocalEntity().bind(),
            primaryTagId = Arb.kotlinUuid().orNull().bind(),
            isFinished = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
