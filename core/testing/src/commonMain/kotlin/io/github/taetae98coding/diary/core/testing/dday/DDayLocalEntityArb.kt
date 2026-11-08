package io.github.taetae98coding.diary.core.testing.dday

import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string

public fun Arb.Companion.ddayDetailLocalEntity(): Arb<DDayDetailLocalEntity> =
    arbitrary {
        DDayDetailLocalEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            baseDate = Arb.kotlinxLocalDate().bind(),
            countsBaseDay = Arb.boolean().bind(),
            repeats = Arb.set(Arb.ddayRepeat(), 0..MAX_COLLECTION_SIZE).bind(),
            endDate = Arb.kotlinxLocalDate().orNull().bind(),
            overflowPolicy = Arb.enum<DDayOverflowPolicyLocalEntity>().bind(),
        )
    }

public fun Arb.Companion.ddayLocalEntity(): Arb<DDayLocalEntity> =
    arbitrary {
        DDayLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.ddayDetailLocalEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.calendarDDayLocalEntity(): Arb<CalendarDDayLocalEntity> =
    arbitrary {
        CalendarDDayLocalEntity(
            ddayId = Arb.kotlinUuid().bind(),
            title = Arb.string().bind(),
            color = Arb.long().bind(),
            date = Arb.kotlinxLocalDate().bind(),
            kind = Arb.enum<CalendarDDayKindLocalEntity>().bind(),
            amount = Arb.int(min = 0).bind(),
        )
    }
