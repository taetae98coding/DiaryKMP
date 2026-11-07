package io.github.taetae98coding.diary.core.testing.web

import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string

public fun Arb.Companion.webDetailLocalEntity(): Arb<WebDetailLocalEntity> =
    arbitrary {
        WebDetailLocalEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            url = Arb.string().bind(),
            headerList = Arb.list(Arb.webHeaderLocalEntity(), 0..MAX_COLLECTION_SIZE).bind(),
        )
    }

public fun Arb.Companion.webHeaderLocalEntity(): Arb<WebHeaderLocalEntity> =
    arbitrary {
        WebHeaderLocalEntity(
            name = Arb.string().bind(),
            value = Arb.string().bind(),
        )
    }

public fun Arb.Companion.webLocalEntity(): Arb<WebLocalEntity> =
    arbitrary {
        WebLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.webDetailLocalEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
