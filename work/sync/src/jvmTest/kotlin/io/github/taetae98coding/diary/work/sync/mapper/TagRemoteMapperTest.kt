package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity
import io.github.taetae98coding.diary.core.testing.finishedAndDeletedCaseList
import io.github.taetae98coding.diary.core.testing.tag.localTag
import io.github.taetae98coding.diary.core.testing.tag.remoteTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class TagRemoteMapperTest :
    FunSpec({
        test("TC-429c3283-a871-461c-99ea-9f5f4fbce798 local to remote") {
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val local = Arb.localTag(isFinished = isFinished, isDeleted = isDeleted).next()

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
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val remote = Arb.remoteTag(isFinished = isFinished, isDeleted = isDeleted).next()

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

        test("local to remote to local") {
            finishedAndDeletedCaseList.forEach { (isFinished, isDeleted) ->
                val local = Arb.localTag(isFinished = isFinished, isDeleted = isDeleted).next()

                local.toRemote().toLocal() shouldBe local
            }
        }
    })
