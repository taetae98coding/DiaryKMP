package io.github.taetae98coding.diary.core.testing.authentication

import io.github.taetae98coding.diary.core.network.api.authentication.entity.SessionRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

public fun Arb.Companion.sessionRemoteEntity(): Arb<SessionRemoteEntity> =
    arbitrary {
        SessionRemoteEntity(
            accessToken = Arb.string().bind(),
            refreshToken = Arb.string().bind(),
        )
    }
