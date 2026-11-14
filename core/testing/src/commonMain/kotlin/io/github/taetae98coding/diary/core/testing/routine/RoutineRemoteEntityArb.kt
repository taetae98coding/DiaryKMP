package io.github.taetae98coding.diary.core.testing.routine

import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineCycleRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

public fun Arb.Companion.routineDetailRemoteEntity(): Arb<RoutineDetailRemoteEntity> =
    arbitrary {
        RoutineDetailRemoteEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            startDate = Arb.kotlinxLocalDate().bind(),
            cycle = Arb.enum<RoutineCycleRemoteEntity>().bind(),
            unit = Arb.routineUnit().bind(),
            goalAmount = Arb.routineAmount().orNull().bind(),
            amountPerRecord = Arb.routineAmount().bind(),
        )
    }

public fun Arb.Companion.routineRemoteEntity(): Arb<RoutineRemoteEntity> =
    arbitrary {
        RoutineRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.routineDetailRemoteEntity().bind(),
            isFinished = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.routineRecordRemoteEntity(): Arb<RoutineRecordRemoteEntity> =
    arbitrary {
        RoutineRecordRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            routineId = Arb.kotlinUuid().bind(),
            date = Arb.kotlinxLocalDate().bind(),
            amount = Arb.routineAmount().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
