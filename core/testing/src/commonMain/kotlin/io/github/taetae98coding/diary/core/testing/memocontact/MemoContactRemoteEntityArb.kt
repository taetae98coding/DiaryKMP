package io.github.taetae98coding.diary.core.testing.memocontact

import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean

public fun Arb.Companion.memoContactRemoteEntity(): Arb<MemoContactRemoteEntity> =
    arbitrary {
        MemoContactRemoteEntity(
            memoId = Arb.kotlinUuid().bind(),
            contactId = Arb.kotlinUuid().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
