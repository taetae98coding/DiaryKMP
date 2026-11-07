package io.github.taetae98coding.diary.core.webcookie.impl

import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieSameSiteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.webViewCookieEntity(): Arb<WebViewCookieEntity> =
    arbitrary {
        WebViewCookieEntity(
            name = Arb.string().bind(),
            value = Arb.string().bind(),
            domain = Arb.string().bind(),
            path = Arb.string().bind(),
            isSecure = Arb.boolean().bind(),
            isHttpOnly = Arb.boolean().bind(),
            sameSite = Arb.enum<WebViewCookieSameSiteEntity>().bind(),
        )
    }
