package io.github.taetae98coding.diary.work.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class TagLinkRemoteMapperTest :
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
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private fun localTagLink(isDeleted: Boolean): TagLinkLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<TagLinkLocalEntity>()
        .setExp(TagLinkLocalEntity::isDeleted, isDeleted)
        .setExp(TagLinkLocalEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
        .setExp(TagLinkLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
        .sample()

private fun remoteTagLink(isDeleted: Boolean): TagLinkRemoteEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<TagLinkRemoteEntity>()
        .setExp(TagLinkRemoteEntity::isDeleted, isDeleted)
        .setExp(TagLinkRemoteEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
        .setExp(TagLinkRemoteEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
        .sample()
