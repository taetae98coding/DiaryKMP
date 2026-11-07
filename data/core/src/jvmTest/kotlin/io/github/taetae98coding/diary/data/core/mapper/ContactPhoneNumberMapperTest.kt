package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.testing.contact.contactPhoneNumber
import io.github.taetae98coding.diary.core.testing.contact.contactPhoneNumberLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ContactPhoneNumberMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = Arb.contactPhoneNumber().next()

            domain.toLocal() shouldBe ContactPhoneNumberLocalEntity(number = domain.number)
        }

        test("local to domain") {
            val local = Arb.contactPhoneNumberLocalEntity().next()

            local.toDomain() shouldBe ContactPhoneNumber(number = local.number)
        }

        test("domain to local to domain") {
            val domain = Arb.contactPhoneNumber().next()

            domain.toLocal().toDomain() shouldBe domain
        }
    })
