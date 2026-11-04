package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.memo.memoCaseList
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class MemoMapperTest :
    FunSpec({
        test("domain to local") {
            memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val domain = Arb.memo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId).next()

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
            memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val local = Arb.localMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId).next()

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

        test("domain to local to domain") {
            memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val domain = Arb.memo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId).next()

                domain.toLocal().toDomain() shouldBe domain
            }
        }
    })
