package io.github.taetae98coding.diary.core.testing.memotag

import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean

public fun Arb.Companion.memoTagLocalEntity(): Arb<MemoTagLocalEntity> =
    arbitrary {
        MemoTagLocalEntity(
            memoId = Arb.kotlinUuid().bind(),
            tagId = Arb.kotlinUuid().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
