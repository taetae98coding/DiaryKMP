package io.github.taetae98coding.diary.core.mapper.web

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.web.entity.WebDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.network.api.web.entity.WebDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WebDetailMapperTest :
    FunSpec({
        test("domain to local") {
            headerCaseList.forEach { headerNameList ->
                val domain = detail(headerNameList = headerNameList)

                domain.toLocal() shouldBe
                    WebDetailLocalEntity(
                        title = domain.title,
                        description = domain.description,
                        url = domain.url,
                        headerList = domain.headerList.map { header -> header.toLocal() },
                    )
            }
        }

        test("local to domain") {
            headerCaseList.forEach { headerNameList ->
                val local = localDetail(headerNameList = headerNameList)

                local.toDomain() shouldBe
                    WebDetail(
                        title = local.title,
                        description = local.description,
                        url = local.url,
                        headerList = local.headerList.map { header -> header.toDomain() },
                    )
            }
        }

        test("local to remote") {
            headerCaseList.forEach { headerNameList ->
                val local = localDetail(headerNameList = headerNameList)

                local.toRemote() shouldBe
                    WebDetailRemoteEntity(
                        title = local.title,
                        description = local.description,
                        url = local.url,
                        headerList = local.headerList.map { header -> header.toRemote() },
                    )
            }
        }

        test("remote to local") {
            headerCaseList.forEach { headerNameList ->
                val remote = remoteDetail(headerNameList = headerNameList)

                remote.toLocal() shouldBe
                    WebDetailLocalEntity(
                        title = remote.title,
                        description = remote.description,
                        url = remote.url,
                        headerList = remote.headerList.map { header -> header.toLocal() },
                    )
            }
        }

        test("domain to local to domain") {
            headerCaseList.forEach { headerNameList ->
                val domain = detail(headerNameList = headerNameList)

                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to remote to local") {
            headerCaseList.forEach { headerNameList ->
                val local = localDetail(headerNameList = headerNameList)

                local.toRemote().toLocal() shouldBe local
            }
        }
    }) {
    public companion object {
        // 헤더 목록은 비어 있을 수 있고 이름이 같은 항목도 합치지 않으므로 순서와 중복을 함께 확인한다.
        private val headerCaseList: List<List<String>> =
            listOf(
                emptyList(),
                listOf("Authorization"),
                listOf("Authorization", "Authorization", "X-Region"),
            )

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun detail(headerNameList: List<String>): WebDetail =
            fixtureMonkey
                .giveMeKotlinBuilder<WebDetail>()
                .setExp(
                    WebDetail::headerList,
                    headerNameList.map { name -> WebHeader(name = name, value = fixtureMonkey.giveMeOne<String>()) },
                ).sample()

        private fun localDetail(headerNameList: List<String>): WebDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebDetailLocalEntity>()
                .setExp(
                    WebDetailLocalEntity::headerList,
                    headerNameList.map { name -> WebHeaderLocalEntity(name = name, value = fixtureMonkey.giveMeOne<String>()) },
                ).sample()

        private fun remoteDetail(headerNameList: List<String>): WebDetailRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebDetailRemoteEntity>()
                .setExp(
                    WebDetailRemoteEntity::headerList,
                    headerNameList.map { name -> WebHeaderRemoteEntity(name = name, value = fixtureMonkey.giveMeOne<String>()) },
                ).sample()
    }
}
