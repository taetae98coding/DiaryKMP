package io.github.taetae98coding.diary.core.testing.tag

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string

public fun Arb.Companion.tagDetailLocalEntity(): Arb<TagDetailLocalEntity> =
    arbitrary {
        TagDetailLocalEntity(
            emoji = Arb.string().bind(),
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
        )
    }

public fun Arb.Companion.tagLocalEntity(): Arb<TagLocalEntity> =
    arbitrary {
        TagLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.tagDetailLocalEntity().bind(),
            isFinished = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
