package io.github.taetae98coding.diary.core.data.testing.web

import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string

public fun Arb.Companion.localWeb(isDeleted: Boolean): Arb<WebLocalEntity> = Arb.webLocalEntity().map { web -> web.copy(isDeleted = isDeleted) }

public fun Arb.Companion.remoteWeb(isDeleted: Boolean): Arb<WebRemoteEntity> = Arb.webRemoteEntity().map { web -> web.copy(isDeleted = isDeleted) }

public fun Arb.Companion.localWebDetail(headerNameList: List<String>): Arb<WebDetailLocalEntity> =
    arbitrary {
        Arb.webDetailLocalEntity().bind().copy(headerList = headerNameList.map { name -> WebHeaderLocalEntity(name = name, value = Arb.string().bind()) })
    }

public fun Arb.Companion.remoteWebDetail(headerNameList: List<String>): Arb<WebDetailRemoteEntity> =
    arbitrary {
        Arb.webDetailRemoteEntity().bind().copy(headerList = headerNameList.map { name -> WebHeaderRemoteEntity(name = name, value = Arb.string().bind()) })
    }
