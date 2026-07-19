package io.github.taetae98coding.diary.core.mapper.webtag

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class WebTagMapperTest :
    FunSpec({
        test("local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val local = localWebTag(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    WebTagRemoteEntity(
                        webId = local.webId,
                        tagId = local.tagId,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteWebTag(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    WebTagLocalEntity(
                        webId = remote.webId,
                        tagId = remote.tagId,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localWebTag(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }

        test("remote to local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteWebTag(isDeleted = isDeleted)

                remote.toLocal().toRemote() shouldBe remote
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun localWebTag(isDeleted: Boolean): WebTagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebTagLocalEntity>()
                .setExp(WebTagLocalEntity::isDeleted, isDeleted)
                .setExp(WebTagLocalEntity::updatedAt, instant())
                .setExp(WebTagLocalEntity::createdAt, instant())
                .sample()

        private fun remoteWebTag(isDeleted: Boolean): WebTagRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<WebTagRemoteEntity>()
                .setExp(WebTagRemoteEntity::isDeleted, isDeleted)
                .setExp(WebTagRemoteEntity::updatedAt, instant())
                .setExp(WebTagRemoteEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
