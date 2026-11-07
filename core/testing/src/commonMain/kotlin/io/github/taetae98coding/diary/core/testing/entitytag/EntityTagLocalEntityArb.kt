package io.github.taetae98coding.diary.core.testing.entitytag

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean

public fun Arb.Companion.entityTagLocalEntity(): Arb<EntityTagLocalEntity> =
    arbitrary {
        EntityTagLocalEntity(
            entityId = Arb.kotlinUuid().bind(),
            tagId = Arb.kotlinUuid().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
