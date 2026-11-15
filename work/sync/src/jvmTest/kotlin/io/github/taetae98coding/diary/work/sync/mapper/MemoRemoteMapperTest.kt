package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.memo.memoCaseList
import io.github.taetae98coding.diary.core.testing.memo.remoteMemo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class MemoRemoteMapperTest :
    FunSpec({
        test("TC-429c3283-a871-461c-99ea-9f5f4fbce798 local to remote") {
            memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val local = Arb.localMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId).next()

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
            memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val remote = Arb.remoteMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId).next()

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

        test("local to remote to local") {
            memoCaseList().forEach { (isFinished, isDeleted, primaryTagId) ->
                val local = Arb.localMemo(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId).next()

                local.toRemote().toLocal() shouldBe local
            }
        }
    })
