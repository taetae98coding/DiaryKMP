package io.github.taetae98coding.diary.core.testing.web

import io.github.taetae98coding.diary.core.network.api.web.entity.WebDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string

public fun Arb.Companion.webDetailRemoteEntity(): Arb<WebDetailRemoteEntity> =
    arbitrary {
        WebDetailRemoteEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            url = Arb.string().bind(),
            headerList = Arb.list(Arb.webHeaderRemoteEntity(), 0..MAX_COLLECTION_SIZE).bind(),
        )
    }

public fun Arb.Companion.webHeaderRemoteEntity(): Arb<WebHeaderRemoteEntity> =
    arbitrary {
        WebHeaderRemoteEntity(
            name = Arb.string().bind(),
            value = Arb.string().bind(),
        )
    }

public fun Arb.Companion.webRemoteEntity(): Arb<WebRemoteEntity> =
    arbitrary {
        WebRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.webDetailRemoteEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
