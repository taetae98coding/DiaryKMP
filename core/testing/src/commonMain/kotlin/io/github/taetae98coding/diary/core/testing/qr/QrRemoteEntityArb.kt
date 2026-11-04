package io.github.taetae98coding.diary.core.testing.qr

import io.github.taetae98coding.diary.core.network.api.qr.entity.QrDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.qr.entity.QrRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.qrDetailRemoteEntity(): Arb<QrDetailRemoteEntity> =
    arbitrary {
        QrDetailRemoteEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            value = Arb.string().bind(),
        )
    }

public fun Arb.Companion.qrRemoteEntity(): Arb<QrRemoteEntity> =
    arbitrary {
        QrRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.qrDetailRemoteEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
