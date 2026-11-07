package io.github.taetae98coding.diary.core.testing.taglink

import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean

public fun Arb.Companion.tagLinkLocalEntity(): Arb<TagLinkLocalEntity> =
    arbitrary {
        TagLinkLocalEntity(
            fromTagId = Arb.kotlinUuid().bind(),
            toTagId = Arb.kotlinUuid().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
