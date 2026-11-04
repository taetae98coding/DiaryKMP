package io.github.taetae98coding.diary.core.testing.web

import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.network.api.web.entity.WebDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string

// 헤더 목록은 비어 있을 수 있고 이름이 같은 항목도 합치지 않으므로 순서와 중복을 함께 확인한다.
public val webHeaderNameCaseList: List<List<String>> =
    listOf(
        emptyList(),
        listOf(AUTHORIZATION_HEADER),
        listOf(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER, "X-Region"),
    )

private const val AUTHORIZATION_HEADER = "Authorization"

public fun Arb.Companion.web(isDeleted: Boolean): Arb<Web> = Arb.web().map { web -> web.copy(isDeleted = isDeleted) }

public fun Arb.Companion.web(
    title: String,
    isDeleted: Boolean,
): Arb<Web> =
    arbitrary {
        Web(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.webDetail().bind().copy(title = title),
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.localWeb(isDeleted: Boolean): Arb<WebLocalEntity> = Arb.webLocalEntity().map { web -> web.copy(isDeleted = isDeleted) }

public fun Arb.Companion.remoteWeb(isDeleted: Boolean): Arb<WebRemoteEntity> = Arb.webRemoteEntity().map { web -> web.copy(isDeleted = isDeleted) }

public fun Arb.Companion.webDetail(headerNameList: List<String>): Arb<WebDetail> =
    arbitrary {
        Arb.webDetail().bind().copy(headerList = headerNameList.map { name -> WebHeader(name = name, value = Arb.string().bind()) })
    }

public fun Arb.Companion.localWebDetail(headerNameList: List<String>): Arb<WebDetailLocalEntity> =
    arbitrary {
        Arb.webDetailLocalEntity().bind().copy(headerList = headerNameList.map { name -> WebHeaderLocalEntity(name = name, value = Arb.string().bind()) })
    }

public fun Arb.Companion.remoteWebDetail(headerNameList: List<String>): Arb<WebDetailRemoteEntity> =
    arbitrary {
        Arb.webDetailRemoteEntity().bind().copy(headerList = headerNameList.map { name -> WebHeaderRemoteEntity(name = name, value = Arb.string().bind()) })
    }
