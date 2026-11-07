package io.github.taetae98coding.diary.core.testing.web

import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebLogin
import io.github.taetae98coding.diary.core.model.web.WebLoginCookie
import io.github.taetae98coding.diary.core.model.web.WebLoginCookieSameSite
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string

public fun Arb.Companion.web(): Arb<Web> =
    arbitrary {
        Web(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.webDetail().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.webDetail(): Arb<WebDetail> =
    arbitrary {
        WebDetail(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            url = Arb.string().bind(),
            headerList = Arb.list(Arb.webHeader(), 0..MAX_COLLECTION_SIZE).bind(),
        )
    }

public fun Arb.Companion.webHeader(): Arb<WebHeader> =
    arbitrary {
        WebHeader(
            name = Arb.string().bind(),
            value = Arb.string().bind(),
        )
    }

public fun Arb.Companion.webLogin(): Arb<WebLogin> =
    arbitrary {
        WebLogin(
            sessionCookieList = Arb.list(Arb.webLoginCookie(), 0..MAX_COLLECTION_SIZE).bind(),
            siteUrlSet = Arb.list(Arb.string(), 0..MAX_COLLECTION_SIZE).map { it.toSet() }.bind(),
        )
    }

public fun Arb.Companion.webLoginCookie(): Arb<WebLoginCookie> =
    arbitrary {
        WebLoginCookie(
            name = Arb.string().bind(),
            value = Arb.string().bind(),
            domain = Arb.string().bind(),
            path = Arb.string().bind(),
            isSecure = Arb.boolean().bind(),
            isHttpOnly = Arb.boolean().bind(),
            sameSite = Arb.enum<WebLoginCookieSameSite>().bind(),
        )
    }

public fun Arb.Companion.webPage(): Arb<WebPage> =
    arbitrary {
        WebPage(
            baseUrl = Arb.string().bind(),
            body = Arb.string().bind(),
        )
    }
