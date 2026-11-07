package io.github.taetae98coding.diary.data.integrity.repository

import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityToken
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.playIntegrityToken(): Arb<PlayIntegrityToken> =
    arbitrary {
        PlayIntegrityToken(
            token = Arb.string().bind(),
            packageName = Arb.string().bind(),
        )
    }
