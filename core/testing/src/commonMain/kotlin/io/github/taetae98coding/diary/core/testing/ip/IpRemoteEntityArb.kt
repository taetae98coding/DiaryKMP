package io.github.taetae98coding.diary.core.testing.ip

import io.github.taetae98coding.diary.core.ip.network.api.entity.IpRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble

public fun Arb.Companion.ipRemoteEntity(): Arb<IpRemoteEntity> =
    arbitrary {
        IpRemoteEntity(
            latitude = Arb.numericDouble().bind(),
            longitude = Arb.numericDouble().bind(),
        )
    }
