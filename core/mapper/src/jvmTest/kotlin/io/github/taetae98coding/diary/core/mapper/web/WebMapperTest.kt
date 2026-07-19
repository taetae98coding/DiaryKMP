package io.github.taetae98coding.diary.core.mapper.web

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class WebMapperTest :
    FunSpec({
        test("domain to local") {
            listOf(true, false).forEach { isDeleted ->
                val domain = web(isDeleted = isDeleted)

                domain.toLocal() shouldBe
                    WebLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        isDeleted = isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            listOf(true, false).forEach { isDeleted ->
                val local = localWeb(isDeleted = isDeleted)

                local.toDomain() shouldBe
                    Web(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val local = localWeb(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    WebRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteWeb(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    WebLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            listOf(true, false).forEach { isDeleted ->
                val domain = web(isDeleted = isDeleted)

                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to domain to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localWeb(isDeleted = isDeleted)

                local.toDomain().toLocal() shouldBe local
            }
        }

        test("local to remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localWeb(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun web(isDeleted: Boolean): Web =
            fixtureMonkey
                .giveMeKotlinBuilder<Web>()
                .setExp(Web::isDeleted, isDeleted)
                .setExp(Web::updatedAt, instant())
                .setExp(Web::createdAt, instant())
                .sample()

        private fun localWeb(isDeleted: Boolean): WebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebLocalEntity>()
                .setExp(WebLocalEntity::isDeleted, isDeleted)
                .setExp(WebLocalEntity::updatedAt, instant())
                .setExp(WebLocalEntity::createdAt, instant())
                .sample()

        private fun remoteWeb(isDeleted: Boolean): WebRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebRemoteEntity>()
                .setExp(WebRemoteEntity::isDeleted, isDeleted)
                .setExp(WebRemoteEntity::updatedAt, instant())
                .setExp(WebRemoteEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
