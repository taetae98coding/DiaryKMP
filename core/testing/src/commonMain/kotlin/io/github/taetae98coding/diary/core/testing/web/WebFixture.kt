package io.github.taetae98coding.diary.core.testing.web

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.network.api.web.entity.WebDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity

// 헤더 목록은 비어 있을 수 있고 이름이 같은 항목도 합치지 않으므로 순서와 중복을 함께 확인한다.
public val webHeaderNameCaseList: List<List<String>> =
    listOf(
        emptyList(),
        listOf(AUTHORIZATION_HEADER),
        listOf(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER, "X-Region"),
    )

private const val AUTHORIZATION_HEADER = "Authorization"

public fun FixtureMonkey.web(isDeleted: Boolean): Web =
    giveMeKotlinBuilder<Web>()
        .setExp(Web::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.localWeb(isDeleted: Boolean): WebLocalEntity =
    giveMeKotlinBuilder<WebLocalEntity>()
        .setExp(WebLocalEntity::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.remoteWeb(isDeleted: Boolean): WebRemoteEntity =
    giveMeKotlinBuilder<WebRemoteEntity>()
        .setExp(WebRemoteEntity::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.webDetail(headerNameList: List<String>): WebDetail =
    giveMeKotlinBuilder<WebDetail>()
        .setExp(WebDetail::headerList, headerNameList.map { name -> WebHeader(name = name, value = giveMeOne<String>()) })
        .sample()

public fun FixtureMonkey.localWebDetail(headerNameList: List<String>): WebDetailLocalEntity =
    giveMeKotlinBuilder<WebDetailLocalEntity>()
        .setExp(WebDetailLocalEntity::headerList, headerNameList.map { name -> WebHeaderLocalEntity(name = name, value = giveMeOne<String>()) })
        .sample()

public fun FixtureMonkey.remoteWebDetail(headerNameList: List<String>): WebDetailRemoteEntity =
    giveMeKotlinBuilder<WebDetailRemoteEntity>()
        .setExp(WebDetailRemoteEntity::headerList, headerNameList.map { name -> WebHeaderRemoteEntity(name = name, value = giveMeOne<String>()) })
        .sample()
