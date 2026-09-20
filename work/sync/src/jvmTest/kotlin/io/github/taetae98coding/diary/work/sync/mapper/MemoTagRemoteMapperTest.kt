package io.github.taetae98coding.diary.work.sync.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class MemoTagRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val local = localMemoTag(isDeleted = isDeleted)

                local.toRemote() shouldBe
                    MemoTagRemoteEntity(
                        memoId = local.memoId,
                        tagId = local.tagId,
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteMemoTag(isDeleted = isDeleted)

                remote.toLocal() shouldBe
                    MemoTagLocalEntity(
                        memoId = remote.memoId,
                        tagId = remote.tagId,
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            listOf(true, false).forEach { isDeleted ->
                val local = localMemoTag(isDeleted = isDeleted)

                local.toRemote().toLocal() shouldBe local
            }
        }

        test("remote to local to remote") {
            listOf(true, false).forEach { isDeleted ->
                val remote = remoteMemoTag(isDeleted = isDeleted)

                remote.toLocal().toRemote() shouldBe remote
            }
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private fun localMemoTag(isDeleted: Boolean): MemoTagLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<MemoTagLocalEntity>()
        .setExp(MemoTagLocalEntity::isDeleted, isDeleted)
        .setExp(MemoTagLocalEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
        .setExp(MemoTagLocalEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
        .sample()

private fun remoteMemoTag(isDeleted: Boolean): MemoTagRemoteEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<MemoTagRemoteEntity>()
        .setExp(MemoTagRemoteEntity::isDeleted, isDeleted)
        .setExp(MemoTagRemoteEntity::updatedAt, fixtureMonkey.giveMeOne<Instant>())
        .setExp(MemoTagRemoteEntity::createdAt, fixtureMonkey.giveMeOne<Instant>())
        .sample()
