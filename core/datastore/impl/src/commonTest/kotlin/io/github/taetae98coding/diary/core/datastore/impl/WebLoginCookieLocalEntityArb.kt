package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginCookieLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginCookieSameSiteLocalEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.webLoginCookieLocalEntity(): Arb<WebLoginCookieLocalEntity> =
    arbitrary {
        WebLoginCookieLocalEntity(
            name = Arb.string().bind(),
            value = Arb.string().bind(),
            domain = Arb.string().bind(),
            path = Arb.string().bind(),
            isSecure = Arb.boolean().bind(),
            isHttpOnly = Arb.boolean().bind(),
            sameSite = Arb.enum<WebLoginCookieSameSiteLocalEntity>().bind(),
        )
    }
