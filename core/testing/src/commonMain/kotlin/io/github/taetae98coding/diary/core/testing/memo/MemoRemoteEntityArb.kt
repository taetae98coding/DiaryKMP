package io.github.taetae98coding.diary.core.testing.memo

import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDateTime
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

public fun Arb.Companion.memoDetailRemoteEntity(): Arb<MemoDetailRemoteEntity> =
    arbitrary {
        MemoDetailRemoteEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            isAllDay = Arb.boolean().orNull().bind(),
            start = Arb.kotlinxLocalDateTime().orNull().bind(),
            endInclusive = Arb.kotlinxLocalDateTime().orNull().bind(),
        )
    }

public fun Arb.Companion.memoRemoteEntity(): Arb<MemoRemoteEntity> =
    arbitrary {
        MemoRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.memoDetailRemoteEntity().bind(),
            primaryTagId = Arb.kotlinUuid().orNull().bind(),
            isFinished = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
