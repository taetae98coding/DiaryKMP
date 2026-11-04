package io.github.taetae98coding.diary.core.testing.account

import io.github.taetae98coding.diary.core.network.api.account.entity.AppleAuthorizationRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

public fun Arb.Companion.appleAuthorizationRemoteEntity(): Arb<AppleAuthorizationRemoteEntity> =
    arbitrary {
        AppleAuthorizationRemoteEntity(
            authorizationCode = Arb.string().bind(),
            clientId = Arb.string().bind(),
        )
    }
