package io.github.taetae98coding.diary.core.testing.tag

import io.github.taetae98coding.diary.core.network.api.tag.entity.TagDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string

public fun Arb.Companion.tagDetailRemoteEntity(): Arb<TagDetailRemoteEntity> =
    arbitrary {
        TagDetailRemoteEntity(
            emoji = Arb.string().bind(),
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
        )
    }

public fun Arb.Companion.tagRemoteEntity(): Arb<TagRemoteEntity> =
    arbitrary {
        TagRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.tagDetailRemoteEntity().bind(),
            isFinished = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
