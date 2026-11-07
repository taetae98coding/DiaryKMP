package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity
import io.github.taetae98coding.diary.core.testing.contact.localContactCaseList
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb

class ContactRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            localContactCaseList().forEach { local ->
                local.toRemote() shouldBe
                    ContactRemoteEntity(
                        id = local.id,
                        detail = local.detail.toRemote(),
                        isFavorite = local.isFavorite,
                        isDeleted = local.isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("remote to local") {
            localContactCaseList().forEach { value ->
                val remote = value.toRemote()

                remote.toLocal() shouldBe
                    ContactLocalEntity(
                        id = remote.id,
                        detail = remote.detail.toLocal(),
                        isFavorite = remote.isFavorite,
                        isDeleted = remote.isDeleted,
                        updatedAt = remote.updatedAt,
                        createdAt = remote.createdAt,
                    )
            }
        }

        test("local to remote to local") {
            localContactCaseList().forEach { local ->
                local.toRemote().toLocal() shouldBe local
            }
        }
    })
