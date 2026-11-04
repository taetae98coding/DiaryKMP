package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
import io.github.taetae98coding.diary.core.testing.isDeletedCaseList
import io.github.taetae98coding.diary.core.testing.place.localPlace
import io.github.taetae98coding.diary.core.testing.place.remotePlace
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class PlaceRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = Arb.localPlace(isDeleted = isDeleted).next()

                local.toRemote() shouldBe
                    PlaceRemoteEntity(
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
                val remote = Arb.remotePlace(isDeleted = isDeleted).next()

                remote.toLocal() shouldBe
                    PlaceLocalEntity(
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
                val local = Arb.localPlace(isDeleted = isDeleted).next()

                local.toRemote().toLocal() shouldBe local
            }
        }
    })
