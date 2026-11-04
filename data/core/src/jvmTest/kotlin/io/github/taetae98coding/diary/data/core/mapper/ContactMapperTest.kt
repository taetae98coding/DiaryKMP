package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.testing.contact.contactCaseList
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb

class ContactMapperTest :
    FunSpec({
        test("domain to local") {
            contactCaseList().forEach { domain ->
                domain.toLocal() shouldBe
                    ContactLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        isFavorite = domain.isFavorite,
                        isDeleted = domain.isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            contactCaseList().forEach { value ->
                val local = value.toLocal()

                local.toDomain() shouldBe
                    Contact(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        isFavorite = local.isFavorite,
                        isDeleted = local.isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            contactCaseList().forEach { domain ->
                domain.toLocal().toDomain() shouldBe domain
            }
        }
    })
