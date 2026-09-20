package io.github.taetae98coding.diary.data.contact.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.testing.contact.contactCaseList
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ContactMapperTest :
    FunSpec({
        test("domain to local") {
            fixtureMonkey.contactCaseList().forEach { domain ->
                domain.toLocal() shouldBe
                    ContactLocalEntity(
                        id = domain.id,
                        detail = domain.detail.toLocal(),
                        isDeleted = domain.isDeleted,
                        updatedAt = domain.updatedAt,
                        createdAt = domain.createdAt,
                    )
            }
        }

        test("local to domain") {
            fixtureMonkey.contactCaseList().forEach { value ->
                val local = value.toLocal()

                local.toDomain() shouldBe
                    Contact(
                        id = local.id,
                        detail = local.detail.toDomain(),
                        isDeleted = local.isDeleted,
                        updatedAt = local.updatedAt,
                        createdAt = local.createdAt,
                    )
            }
        }

        test("domain to local to domain") {
            fixtureMonkey.contactCaseList().forEach { domain ->
                domain.toLocal().toDomain() shouldBe domain
            }
        }
    })
