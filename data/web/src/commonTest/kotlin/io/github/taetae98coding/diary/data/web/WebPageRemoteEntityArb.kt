package io.github.taetae98coding.diary.data.web

import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.webPageRemoteEntity(): Arb<WebPageRemoteEntity> =
    arbitrary {
        WebPageRemoteEntity(
            url = Arb.string().bind(),
            body = Arb.string().bind(),
        )
    }
