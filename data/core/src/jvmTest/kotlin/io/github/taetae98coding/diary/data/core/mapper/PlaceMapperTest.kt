package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.testing.isDeletedCaseList
import io.github.taetae98coding.diary.core.testing.place.localPlace
import io.github.taetae98coding.diary.core.testing.place.place
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class PlaceMapperTest :
    FunSpec({
        test("domain to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val domain = Arb.place(isDeleted = isDeleted).next()

                domain.toLocal() shouldBe
                    PlaceLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        isDeleted = isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = Arb.localPlace(isDeleted = isDeleted).next()

                local.toDomain() shouldBe
                    Place(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        isDeleted = isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            isDeletedCaseList.forEach { isDeleted ->
                val domain = Arb.place(isDeleted = isDeleted).next()

                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to domain to local") {
            isDeletedCaseList.forEach { isDeleted ->
                val local = Arb.localPlace(isDeleted = isDeleted).next()

                local.toDomain().toLocal() shouldBe local
            }
        }
    })
