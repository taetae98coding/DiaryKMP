package io.github.taetae98coding.diary.core.testing.profile

import io.github.taetae98coding.diary.core.network.api.profile.entity.ProfileImageRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

public fun Arb.Companion.profileImageRemoteEntity(): Arb<ProfileImageRemoteEntity> =
    arbitrary {
        ProfileImageRemoteEntity(
            profileImage = Arb.string().bind(),
        )
    }
