package io.github.taetae98coding.diary.data.account

import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.supabaseUser(): Arb<SupabaseUser> =
    arbitrary {
        SupabaseUser(
            id = Arb.kotlinUuid().bind(),
            email = Arb.string().bind(),
            profileImage = Arb.string().orNull().bind(),
            providerSet = Arb.list(Arb.string(), 0..MAX_COLLECTION_SIZE).map { it.toSet() }.bind(),
        )
    }
