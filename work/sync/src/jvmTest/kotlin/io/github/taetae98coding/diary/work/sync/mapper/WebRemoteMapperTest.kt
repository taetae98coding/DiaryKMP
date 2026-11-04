package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity
import io.github.taetae98coding.diary.core.testing.isDeletedCaseList
import io.github.taetae98coding.diary.core.testing.web.localWeb
import io.github.taetae98coding.diary.core.testing.web.remoteWeb
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WebRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = Arb.localWeb(isDeleted = isDeleted).next()

                local.toRemote() shouldBe
                    WebRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val remote = Arb.remoteWeb(isDeleted = isDeleted).next()

                remote.toLocal() shouldBe
                    WebLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        isDeleted = isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = Arb.localWeb(isDeleted = isDeleted).next()

                local.toRemote().toLocal() shouldBe local
            }
        }
    })
