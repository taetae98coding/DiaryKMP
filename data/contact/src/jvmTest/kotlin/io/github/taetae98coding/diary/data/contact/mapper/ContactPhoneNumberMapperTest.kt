package io.github.taetae98coding.diary.data.contact.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ContactPhoneNumberMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = fixtureMonkey.giveMeOne<ContactPhoneNumber>()

            domain.toLocal() shouldBe ContactPhoneNumberLocalEntity(number = domain.number)
        }

        test("local to domain") {
            val local = fixtureMonkey.giveMeOne<ContactPhoneNumberLocalEntity>()

            local.toDomain() shouldBe ContactPhoneNumber(number = local.number)
        }

        test("domain to local to domain") {
            val domain = fixtureMonkey.giveMeOne<ContactPhoneNumber>()

            domain.toLocal().toDomain() shouldBe domain
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
