package io.github.taetae98coding.diary.core.mapper.taglink

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class TagLinkMapperTest :
    FunSpec({
        test("local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val local = localTagLink(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    TagLinkRemoteEntity(
                        fromTagId = local.fromTagId,
                        toTagId = local.toTagId,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteTagLink(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    TagLinkLocalEntity(
                        fromTagId = remote.fromTagId,
                        toTagId = remote.toTagId,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localTagLink(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }

        test("remote to local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteTagLink(isDeleted = isDeleted)

                remote.toLocal().toRemote() shouldBe remote
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun localTagLink(isDeleted: Boolean): TagLinkLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLinkLocalEntity>()
                .setExp(TagLinkLocalEntity::isDeleted, isDeleted)
                .setExp(TagLinkLocalEntity::updatedAt, instant())
                .setExp(TagLinkLocalEntity::createdAt, instant())
                .sample()

        private fun remoteTagLink(isDeleted: Boolean): TagLinkRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLinkRemoteEntity>()
                .setExp(TagLinkRemoteEntity::isDeleted, isDeleted)
                .setExp(TagLinkRemoteEntity::updatedAt, instant())
                .setExp(TagLinkRemoteEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
