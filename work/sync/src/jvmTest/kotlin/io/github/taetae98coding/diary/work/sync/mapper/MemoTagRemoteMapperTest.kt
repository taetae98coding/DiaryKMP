package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity
import io.github.taetae98coding.diary.core.testing.memotag.memoTagLocalEntity
import io.github.taetae98coding.diary.core.testing.memotag.memoTagRemoteEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
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

private fun localMemoTag(isDeleted: Boolean): MemoTagLocalEntity =
    Arb
        .memoTagLocalEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

private fun remoteMemoTag(isDeleted: Boolean): MemoTagRemoteEntity =
    Arb
        .memoTagRemoteEntity()
        .next()
        .copy(
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )
