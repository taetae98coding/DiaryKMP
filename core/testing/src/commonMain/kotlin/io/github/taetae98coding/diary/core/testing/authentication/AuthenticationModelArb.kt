package io.github.taetae98coding.diary.core.testing.authentication

import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

public fun Arb.Companion.appleAuthorization(): Arb<AppleAuthorization> =
    arbitrary {
        AppleAuthorization(
            code = Arb.string().bind(),
            clientId = Arb.string().bind(),
        )
    }

public fun Arb.Companion.appleCredential(): Arb<AppleCredential> =
    arbitrary {
        AppleCredential(
            idToken = Arb.string().bind(),
            nonce = Arb.string().bind(),
        )
    }

public fun Arb.Companion.googleCredentialAuthorizationCode(): Arb<GoogleCredential.AuthorizationCode> =
    arbitrary {
        GoogleCredential.AuthorizationCode(
            code = Arb.string().bind(),
            clientId = Arb.string().bind(),
            redirectUri = Arb.string().bind(),
            codeVerifier = Arb.string().orNull().bind(),
        )
    }

public fun Arb.Companion.googleCredentialIdToken(): Arb<GoogleCredential.IdToken> =
    arbitrary {
        GoogleCredential.IdToken(
            idToken = Arb.string().bind(),
            nonce = Arb.string().bind(),
        )
    }
