package io.github.taetae98coding.diary.core.testing.account

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

public fun Arb.Companion.accountUser(): Arb<Account.User> =
    arbitrary {
        Account.User(
            id = Arb.kotlinUuid().bind(),
            profileImage = Arb.string().orNull().bind(),
            email = Arb.string().bind(),
            isSessionValid = Arb.boolean().bind(),
        )
    }

public fun Arb.Companion.userData(): Arb<UserData> =
    arbitrary {
        UserData(
            id = Arb.kotlinUuid().bind(),
            email = Arb.string().bind(),
            profileImage = Arb.string().orNull().bind(),
            loginProviderSet = Arb.list(Arb.enum<LoginProvider>(), 0..MAX_COLLECTION_SIZE).map { it.toSet() }.bind(),
        )
    }
