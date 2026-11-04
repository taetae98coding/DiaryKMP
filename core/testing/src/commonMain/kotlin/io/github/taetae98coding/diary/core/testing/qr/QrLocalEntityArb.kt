package io.github.taetae98coding.diary.core.testing.qr

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.qrDetailLocalEntity(): Arb<QrDetailLocalEntity> =
    arbitrary {
        QrDetailLocalEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            value = Arb.string().bind(),
        )
    }

public fun Arb.Companion.qrLocalEntity(): Arb<QrLocalEntity> =
    arbitrary {
        QrLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.qrDetailLocalEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
