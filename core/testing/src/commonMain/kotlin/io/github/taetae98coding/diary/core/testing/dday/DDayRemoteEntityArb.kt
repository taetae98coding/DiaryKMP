package io.github.taetae98coding.diary.core.testing.dday

import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayOverflowPolicyRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRepeatRemoteEntity
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string

public fun Arb.Companion.ddayRepeatRemoteEntity(): Arb<DDayRepeatRemoteEntity> =
    Arb.choice(
        Arb.int(min = 1).map { days -> DDayRepeatRemoteEntity.Days(days = days) },
        Arb.int(min = 1).map { months -> DDayRepeatRemoteEntity.Months(months = months) },
    )

public fun Arb.Companion.ddayDetailRemoteEntity(): Arb<DDayDetailRemoteEntity> =
    arbitrary {
        DDayDetailRemoteEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            baseDate = Arb.kotlinxLocalDate().bind(),
            countsBaseDay = Arb.boolean().bind(),
            repeats = Arb.set(Arb.ddayRepeatRemoteEntity(), 0..MAX_COLLECTION_SIZE).bind().toList(),
            endDate = Arb.kotlinxLocalDate().orNull().bind(),
            overflowPolicy = Arb.enum<DDayOverflowPolicyRemoteEntity>().bind(),
        )
    }

public fun Arb.Companion.ddayRemoteEntity(): Arb<DDayRemoteEntity> =
    arbitrary {
        DDayRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.ddayDetailRemoteEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
