package io.github.taetae98coding.diary.core.mapper.tag

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class TagMapperTest :
    FunSpec({
        test("domain to local") {
            flagCases().forEach { (isFinished, isDeleted) ->
                val domain = tag(isFinished = isFinished, isDeleted = isDeleted)

                domain.toLocal() shouldBe
                    TagLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            flagCases().forEach { (isFinished, isDeleted) ->
                val local = localTag(isFinished = isFinished, isDeleted = isDeleted)

                local.toDomain() shouldBe
                    Tag(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        // TC-DATA-SYNC-DOMAIN-006, TC-DATA-SYNC-DOMAIN-007
        test("local to remote") {
            flagCases().forEach { (isFinished, isDeleted) ->
                val local = localTag(isFinished = isFinished, isDeleted = isDeleted)

                local.toRemote() shouldBe
                    TagRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            flagCases().forEach { (isFinished, isDeleted) ->
                val remote = remoteTag(isFinished = isFinished, isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    TagLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            flagCases().forEach { (isFinished, isDeleted) ->
                val domain = tag(isFinished = isFinished, isDeleted = isDeleted)

                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to remote to local") {
            flagCases().forEach { (isFinished, isDeleted) ->
                val local = localTag(isFinished = isFinished, isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun flagCases(): List<Pair<Boolean, Boolean>> =
            listOf(
                true to true,
                true to false,
                false to true,
                false to false,
            )

        private fun tag(
            isFinished: Boolean,
            isDeleted: Boolean,
        ): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::isFinished, isFinished)
                .setExp(Tag::isDeleted, isDeleted)
                .setExp(Tag::updatedAt, instant())
                .setExp(Tag::createdAt, instant())
                .sample()

        private fun localTag(
            isFinished: Boolean,
            isDeleted: Boolean,
        ): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::isFinished, isFinished)
                .setExp(TagLocalEntity::isDeleted, isDeleted)
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private fun remoteTag(
            isFinished: Boolean,
            isDeleted: Boolean,
        ): TagRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagRemoteEntity>()
                .setExp(TagRemoteEntity::isFinished, isFinished)
                .setExp(TagRemoteEntity::isDeleted, isDeleted)
                .setExp(TagRemoteEntity::updatedAt, instant())
                .setExp(TagRemoteEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
