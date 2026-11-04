package io.github.taetae98coding.diary.core.testing.webtag

import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean

public fun Arb.Companion.webTagRemoteEntity(): Arb<WebTagRemoteEntity> =
    arbitrary {
        WebTagRemoteEntity(
            webId = Arb.kotlinUuid().bind(),
            tagId = Arb.kotlinUuid().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
