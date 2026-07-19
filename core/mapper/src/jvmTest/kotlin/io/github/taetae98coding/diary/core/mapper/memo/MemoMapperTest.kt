package io.github.taetae98coding.diary.core.mapper.memo

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoMapperTest :
    FunSpec({
        test("domain to local") {
            memoCases().forEach { (isFinished, isDeleted, primaryTagId) ->
                val domain = memo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                domain.toLocal() shouldBe
                    MemoLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        primaryTagId = primaryTagId,
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            memoCases().forEach { (isFinished, isDeleted, primaryTagId) ->
                val local = localMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                local.toDomain() shouldBe
                    Memo(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        primaryTagId = primaryTagId,
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        // TC-DATA-SYNC-DOMAIN-006, TC-DATA-SYNC-DOMAIN-007
        test("local to remote") {
            memoCases().forEach { (isFinished, isDeleted, primaryTagId) ->
                val local = localMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                local.toRemote() shouldBe
                    MemoRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        primaryTagId = primaryTagId,
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            memoCases().forEach { (isFinished, isDeleted, primaryTagId) ->
                val remote = remoteMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                remote.toLocal() shouldBe
                    MemoLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        primaryTagId = primaryTagId,
                        isFinished = isFinished,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            memoCases().forEach { (isFinished, isDeleted, primaryTagId) ->
                val domain = memo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to remote to local") {
            memoCases().forEach { (isFinished, isDeleted, primaryTagId) ->
                val local = localMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)

                local.toRemote().toLocal() shouldBe local
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memoCases(): List<Triple<Boolean, Boolean, Uuid?>> =
            listOf(true, false).flatMap { isFinished ->
                listOf(true, false).flatMap { isDeleted ->
                    listOf(null, fixtureMonkey.giveMeOne<Uuid>()).map { primaryTagId ->
                        Triple(isFinished, isDeleted, primaryTagId)
                    }
                }
            }

        private fun memo(
            isFinished: Boolean,
            isDeleted: Boolean,
            primaryTagId: Uuid?,
        ): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::isFinished, isFinished)
                .setExp(Memo::isDeleted, isDeleted)
                .setExp(Memo::updatedAt, instant())
                .setExp(Memo::createdAt, instant())
                .sample()
                .copy(primaryTagId = primaryTagId)

        private fun localMemo(
            isFinished: Boolean,
            isDeleted: Boolean,
            primaryTagId: Uuid?,
        ): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::isFinished, isFinished)
                .setExp(MemoLocalEntity::isDeleted, isDeleted)
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()
                .copy(primaryTagId = primaryTagId)

        private fun remoteMemo(
            isFinished: Boolean,
            isDeleted: Boolean,
            primaryTagId: Uuid?,
        ): MemoRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoRemoteEntity>()
                .setExp(MemoRemoteEntity::isFinished, isFinished)
                .setExp(MemoRemoteEntity::isDeleted, isDeleted)
                .setExp(MemoRemoteEntity::updatedAt, instant())
                .setExp(MemoRemoteEntity::createdAt, instant())
                .sample()
                .copy(primaryTagId = primaryTagId)

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
