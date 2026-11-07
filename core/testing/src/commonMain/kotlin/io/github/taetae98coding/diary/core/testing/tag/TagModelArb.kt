package io.github.taetae98coding.diary.core.testing.tag

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string

public fun Arb.Companion.tag(): Arb<Tag> =
    arbitrary {
        Tag(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.tagDetail().bind(),
            isFinished = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.tagDetail(): Arb<TagDetail> =
    arbitrary {
        TagDetail(
            emoji = Arb.string().bind(),
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
        )
    }
